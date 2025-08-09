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
   * Send encrypted message with friend validation and one-initiation rule
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

      const otherParticipantId = conversation.participants.find((p: any) => p.toString() !== senderId);
      if (!otherParticipantId) {
        throw new AppError('Invalid conversation participants', 400, 'INVALID_CONVERSATION');
      }

      // Check friendship status and apply one-initiation rule
      const areFriends = await friendshipService.areFriends(
        senderId,
        otherParticipantId.toString()
      );

      if (!areFriends) {
        // Apply one-initiation rule for non-friends
        if (conversation.initiatedBy && conversation.initiatedBy.toString() !== senderId) {
          throw new AppError(
            'You cannot send messages to this user',
            403,
            'MESSAGING_NOT_ALLOWED'
          );
        }

        // Check if sender already sent a message (initiation limit)
        const sentMessagesCount = await Message.countDocuments({
          conversationId: new Types.ObjectId(conversationId),
          sender: new Types.ObjectId(senderId),
        });

        if (sentMessagesCount >= 1) {
          throw new AppError(
            'You have already sent an initiation message to this user',
            403,
            'INITIATION_LIMIT_REACHED'
          );
        }

        // Update conversation to mark initiator
        if (!conversation.initiatedBy) {
          conversation.initiatedBy = new Types.ObjectId(senderId);
          await conversation.save();
        }
      } else {
        // If users are friends but conversation isn't marked as such, update it
        if (!conversation.isFriendBased) {
          await conversationService.markAsFriendBased(conversationId, requestId);
        }
      }

      // Validate that all device IDs in payloads exist and belong to the recipient
      const recipientDevices = await deviceService.getUserDevices(
        otherParticipantId.toString(),
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
      const message = await Message.create({
        conversationId: new Types.ObjectId(conversationId),
        sender: new Types.ObjectId(senderId),
        payloads,
        messageType,
        replyTo: replyTo ? new Types.ObjectId(replyTo) : undefined,
      });

      // Update conversation last message
      await conversationService.updateLastMessage(
        conversationId,
        (message._id as Types.ObjectId).toString(),
        requestId
      );

      // Send real-time notifications to recipient devices
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
        recipientId: otherParticipantId.toString(),
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
   * Get messages in a conversation with pagination
   */
  public async getConversationMessages(
    conversationId: string,
    userId: string,
    { page = 1, limit = 50 }: PaginationOptions = {},
    requestId?: string
  ): Promise<{ messages: IMessage[]; total: number }> {
    const loggerWithId = createLogger(requestId);

    try {
      // Validate conversation access
      await conversationService.getConversationById(conversationId, userId, requestId);

      const skip = (page - 1) * limit;

      const [messages, total] = await Promise.all([
        Message.find({ conversationId: new Types.ObjectId(conversationId) })
          .populate('sender', 'username avatar')
          .populate('replyTo', 'sender createdAt')
          .sort({ createdAt: -1 })
          .skip(skip)
          .limit(limit)
          .lean(),
        Message.countDocuments({ conversationId: new Types.ObjectId(conversationId) }),
      ]);

      loggerWithId.info('Conversation messages retrieved', {
        conversationId,
        userId,
        page,
        limit,
        total,
        retrieved: messages.length,
      });

      return { messages: messages.reverse() as IMessage[], total }; // Reverse to get chronological order
    } catch (error) {
      loggerWithId.error('Failed to get conversation messages', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to get messages', 500, 'MESSAGE_FETCH_ERROR');
    }
  }

  /**
   * Mark message as delivered
   */
  public async markMessageDelivered(
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

      // Check if user is a participant in the conversation
      const conversation = await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      const isParticipant = conversation.participants.some((p: any) => p.toString() === userId);
      if (!isParticipant) {
        throw new AppError('Access denied', 403, 'ACCESS_DENIED');
      }

      // Mark as delivered
      if (!message.deliveredTo.some((id: any) => id.toString() === userId)) {
        message.deliveredTo.push(new Types.ObjectId(userId));
      }
      await message.save();

      // Emit delivery confirmation to sender
      const senderId = message.sender.toString();
      if (senderId !== userId) {
        try {
          SocketManager.emitMessageDelivery(senderId, {
            messageId: (message._id as Types.ObjectId).toString(),
            conversationId: message.conversationId.toString(),
            deliveredBy: userId,
            deliveredAt: new Date(),
          });
        } catch (socketError) {
          loggerWithId.warn('Failed to send delivery confirmation', {
            error: socketError instanceof Error ? socketError.message : 'Unknown error',
            messageId,
          });
        }
      }

      loggerWithId.debug('Message marked as delivered', {
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
      throw new AppError('Failed to mark message as delivered', 500, 'MESSAGE_DELIVERY_ERROR');
    }
  }

  /**
   * Mark message as read
   */
  public async markMessageRead(
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

      // Check if user is a participant in the conversation
      const conversation = await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      const isParticipant = conversation.participants.some((p: any) => p.toString() === userId);
      if (!isParticipant) {
        throw new AppError('Access denied', 403, 'ACCESS_DENIED');
      }

      // Mark as read (also marks as delivered)
      if (!message.readBy.some((id: any) => id.toString() === userId)) {
        message.readBy.push(new Types.ObjectId(userId));
      }
      if (!message.deliveredTo.some((id: any) => id.toString() === userId)) {
        message.deliveredTo.push(new Types.ObjectId(userId));
      }
      await message.save();

      // Emit read confirmation to sender
      const senderId = message.sender.toString();
      if (senderId !== userId) {
        try {
          SocketManager.emitMessageRead(senderId, {
            messageId: (message._id as Types.ObjectId).toString(),
            conversationId: message.conversationId.toString(),
            readBy: userId,
            readAt: new Date(),
          });
        } catch (socketError) {
          loggerWithId.warn('Failed to send read confirmation', {
            error: socketError instanceof Error ? socketError.message : 'Unknown error',
            messageId,
          });
        }
      }

      loggerWithId.debug('Message marked as read', {
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
      throw new AppError('Failed to mark message as read', 500, 'MESSAGE_READ_ERROR');
    }
  }

  /**
   * Get message payload for specific device
   */
  public async getMessageForDevice(
    messageId: string,
    deviceId: string,
    userId: string,
    requestId?: string
  ): Promise<IMessagePayload | null> {
    const loggerWithId = createLogger(requestId);

    try {
      const message = await Message.findById(messageId);
      if (!message) {
        throw new AppError('Message not found', 404, 'MESSAGE_NOT_FOUND');
      }

      // Verify device belongs to user
      const device = await deviceService.getDeviceById(deviceId, requestId);
      if (!device || device.user.toString() !== userId) {
        throw new AppError('Invalid device', 403, 'INVALID_DEVICE');
      }

      // Check conversation access
      await conversationService.getConversationById(
        message.conversationId.toString(),
        userId,
        requestId
      );

      return message.payloads.find((p: IMessagePayload) => p.deviceId === deviceId) || null;
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
      throw new AppError('Failed to get message for device', 500, 'MESSAGE_DEVICE_ERROR');
    }
  }
}

export const messageService = MessageService.getInstance();
