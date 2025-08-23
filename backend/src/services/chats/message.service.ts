import Message, { IMessage, IMessagePayload } from '../../models/Message';
import { conversationService } from './conversation.service';
import { deviceService } from './device.service';
import { friendshipService } from '../friendship.service';
import { createLogger } from '../../utils/logger';
import { AppError } from '../../middlewares/errorHandler';
import { Types } from 'mongoose';
import { SocketManager } from '../../utils/socketManager';

export interface SendMessageData {
  conversationId: string;
  payloads: IMessagePayload[];
  messageType?: 'text' | 'image' | 'file' | 'system';
  replyTo?: string;
}

export interface PaginationOptions {
  page?: number;
  limit?: number;
}

export class MessageService {
  private static instance: MessageService;

  private constructor() {}

  public static getInstance(): MessageService {
    if (!MessageService.instance) {
      MessageService.instance = new MessageService();
    }
    return MessageService.instance;
  }

  /**
   * Send an encrypted message in a conversation
   */
  public async sendMessage(
    senderId: string,
    messageData: SendMessageData,
    requestId?: string
  ): Promise<IMessage> {
    const loggerWithId = createLogger(requestId);

    try {
      const { conversationId, payloads, messageType = 'text', replyTo } = messageData;

      // Validate payloads
      if (!payloads || payloads.length === 0) {
        throw new AppError('Message payloads are required', 400, 'INVALID_PAYLOADS');
      }

      // Get and validate conversation
      const conversation = await conversationService.getConversationById(
        conversationId,
        senderId,
        requestId
      );

      // Extract the other participant ID (handle both ObjectId and populated user object)
      const otherParticipant = conversation.participants.find((p: any) => {
        const pId = p._id ? p._id.toString() : p.toString();
        return pId !== senderId.toString();
      });
      
      if (!otherParticipant) {
        throw new AppError('Invalid conversation participants', 400, 'INVALID_CONVERSATION');
      }
      
      // Get the actual ID string from the participant (could be ObjectId or populated user object)
      const otherParticipantId = otherParticipant._id ? otherParticipant._id.toString() : otherParticipant.toString();

      // Check friendship status
      const areFriends = await friendshipService.areFriends(
        senderId.toString(),
        otherParticipantId
      );

      if (!areFriends) {
        if (conversation.initiatedBy && conversation.initiatedBy.toString() !== senderId) {
          throw new AppError(
            'You cannot send messages to this user',
            403,
            'MESSAGING_NOT_ALLOWED'
          );
        }

        const sentMessagesCount = await Message.countDocuments({
          conversationId: new Types.ObjectId(conversationId.toString()),
          sender: new Types.ObjectId(senderId.toString()),
        });

        if (sentMessagesCount >= 1) {
          throw new AppError(
            'You have already sent an initiation message to this user',
            403,
            'INITIATION_LIMIT_REACHED'
          );
        }

        if (!conversation.initiatedBy) {
          conversation.initiatedBy = new Types.ObjectId(senderId.toString());
          await conversation.save();
        }
      } else {
        if (!conversation.isFriendBased) {
          await conversationService.markAsFriendBased(conversationId, requestId);
        }
      }

      // Validate device IDs
      const recipientDevices = await deviceService.getUserDevices(
        otherParticipantId,
        requestId
      );

      const recipientDeviceIds = new Set(recipientDevices.map(d => d.deviceId));

      for (const payload of payloads) {
        if (!recipientDeviceIds.has(payload.deviceId)) {
          throw new AppError(
            `Invalid device ID: ${payload.deviceId}`,
            400,
            'INVALID_DEVICE_ID'
          );
        }
      }

      // Create message
      const conversationIdString = conversationId.toString();
      const senderIdString = senderId.toString();
      const conversationObjectId = new Types.ObjectId(conversationIdString);
      const senderObjectId = new Types.ObjectId(senderIdString);
      let replyToObjectId = replyTo ? new Types.ObjectId(replyTo.toString()) : undefined;

      const message = await Message.create({
        conversationId: conversationObjectId,
        sender: senderObjectId,
        payloads,
        messageType,
        replyTo: replyToObjectId,
      });

      // Update last message
      await conversationService.updateLastMessage(
        conversationId,
        (message._id as Types.ObjectId).toString(),
        requestId
      );

      // Notify recipient
      try {
        for (const payload of payloads) {
          SocketManager.emitChatMessage(payload.deviceId, {
            conversationId,
            messageId: (message._id as Types.ObjectId).toString(),
            senderId,
            messageType,
            payload,
            createdAt: message.createdAt,
          });
        }
      } catch (socketError) {
        loggerWithId.warn('Failed to send real-time message notification', {
          error: socketError instanceof Error ? socketError.message : 'Unknown error',
          messageId: message._id,
        });
      }

      loggerWithId.info('Message sent successfully', {
        messageId: message._id,
        conversationId,
        senderId,
        recipientId: otherParticipantId,
        messageType,
        payloadCount: payloads.length,
        areFriends,
      });

      return message;
    } catch (error) {
      loggerWithId.error('Failed to send message', {
        error: error instanceof Error ? error.message : 'Unknown error',
        senderId,
        conversationId: messageData.conversationId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to send message', 500, 'MESSAGE_SEND_ERROR');
    }
  }
  

  /**
   * Get messages for a conversation with pagination
   */
  public async getConversationMessages(
    conversationId: string,
    userId: string,
    { page = 1, limit = 50 }: PaginationOptions = {},
    requestId?: string
  ): Promise<{ messages: IMessage[]; total: number }> {
    const loggerWithId = createLogger(requestId);

    try {
      // Verify user has access to this conversation
      await conversationService.getConversationById(conversationId, userId, requestId);

      const skip = (page - 1) * limit;

      const [messages, total] = await Promise.all([
        Message.find({ conversationId: new Types.ObjectId(conversationId) })
          .populate('sender', 'username email avatar')
          .sort({ createdAt: -1 })
          .skip(skip)
          .limit(limit)
          .lean(),
        Message.countDocuments({ conversationId: new Types.ObjectId(conversationId) }),
      ]);

      loggerWithId.info('Messages retrieved successfully', {
        conversationId,
        userId,
        count: messages.length,
        total,
        page,
        limit,
      });

      return { messages: messages.reverse(), total };
    } catch (error) {
      loggerWithId.error('Failed to get conversation messages', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to retrieve messages', 500, 'MESSAGE_FETCH_ERROR');
    }
  }

  /**
   * Mark message as delivered for a user
   */
  public async markAsDelivered(
    messageId: string,
    userId: string,
    requestId?: string
  ): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      const message = await Message.findById(messageId);

      if (!message) {
        throw new AppError('Message not found', 404, 'MESSAGE_NOT_FOUND');
      }

      // Verify user is a participant in the conversation
      await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      const userObjectId = new Types.ObjectId(userId);

      if (!message.deliveredTo.some(id => id.equals(userObjectId))) {
        message.deliveredTo.push(userObjectId);
        await message.save();

        // Emit delivery confirmation
        try {
          SocketManager.emitMessageDelivery(message.sender.toString(), {
            messageId: (message._id as Types.ObjectId).toString(),
            deliveredBy: userId,
            deliveredAt: new Date(),
          });
        } catch (socketError) {
          loggerWithId.warn('Failed to emit delivery confirmation', {
            error: socketError instanceof Error ? socketError.message : 'Unknown error',
            messageId,
          });
        }
      }

      loggerWithId.info('Message marked as delivered', {
        messageId,
        userId,
      });
    } catch (error) {
      loggerWithId.error('Failed to mark message as delivered', {
        error: error instanceof Error ? error.message : 'Unknown error',
        messageId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to mark as delivered', 500, 'DELIVERY_UPDATE_ERROR');
    }
  }

  /**
   * Mark message as read for a user
   */
  public async markAsRead(
    messageId: string,
    userId: string,
    requestId?: string
  ): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      const message = await Message.findById(messageId);

      if (!message) {
        throw new AppError('Message not found', 404, 'MESSAGE_NOT_FOUND');
      }

      // Verify user is a participant in the conversation
      await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      const userObjectId = new Types.ObjectId(userId);

      // Mark as delivered first if not already
      if (!message.deliveredTo.some(id => id.equals(userObjectId))) {
        message.deliveredTo.push(userObjectId);
      }

      // Mark as read
      if (!message.readBy.some(id => id.equals(userObjectId))) {
        message.readBy.push(userObjectId);
        await message.save();

        // Emit read confirmation
        try {
          SocketManager.emitMessageRead(message.sender.toString(), {
            messageId: (message._id as Types.ObjectId).toString(),
            readBy: userId,
            readAt: new Date(),
          });
        } catch (socketError) {
          loggerWithId.warn('Failed to emit read confirmation', {
            error: socketError instanceof Error ? socketError.message : 'Unknown error',
            messageId,
          });
        }
      }

      loggerWithId.info('Message marked as read', {
        messageId,
        userId,
      });
    } catch (error) {
      loggerWithId.error('Failed to mark message as read', {
        error: error instanceof Error ? error.message : 'Unknown error',
        messageId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to mark as read', 500, 'READ_UPDATE_ERROR');
    }
  }

  /**
   * Get message payload for a specific device
   */
  public async getMessageForDevice(
    messageId: string,
    deviceId: string,
    userId: string,
    requestId?: string
  ): Promise<IMessagePayload> {
    const loggerWithId = createLogger(requestId);

    try {
      const message = await Message.findById(messageId);

      if (!message) {
        throw new AppError('Message not found', 404, 'MESSAGE_NOT_FOUND');
      }

      // Verify user is a participant in the conversation
      await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      // Find payload for this device
      const payload = message.payloads.find(p => p.deviceId === deviceId);

      if (!payload) {
        throw new AppError('Payload not found for device', 404, 'PAYLOAD_NOT_FOUND');
      }

      loggerWithId.info('Message payload retrieved', {
        messageId,
        deviceId,
        userId,
      });

      return payload;
    } catch (error) {
      loggerWithId.error('Failed to get message for device', {
        error: error instanceof Error ? error.message : 'Unknown error',
        messageId,
        deviceId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to retrieve message payload', 500, 'PAYLOAD_FETCH_ERROR');
    }
  }
}

export const messageService = MessageService.getInstance();