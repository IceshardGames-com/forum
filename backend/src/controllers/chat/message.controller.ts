import { Request, Response } from 'express';
import { messageService } from '../../services/chats/message.service';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const sendMessage = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const senderId = req.user!._id;
  const { conversationId } = req.params as { conversationId: string };
  const { payloads, messageType, replyTo } = req.body;

  const message = await messageService.sendMessage(
    senderId,
    {
      conversationId,
      payloads,
      messageType,
      replyTo,
    },
    req.id
  );

  logger.info('Message sent successfully', {
    messageId: message._id,
    conversationId,
    senderId,
    messageType: message.messageType,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      messageId: message._id,
      conversationId: message.conversationId,
      messageType: message.messageType,
      createdAt: message.createdAt,
    },
    message: 'Message sent successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(201).json(response);
});

export const getConversationMessages = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { conversationId } = req.params as { conversationId: string };
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '50', 10), 1), 100);

  const { messages, total } = await messageService.getConversationMessages(
    conversationId,
    userId,
    { page, limit },
    req.id
  );

  const response: ApiResponse = {
    success: true,
    data: {
      messages: messages.map(msg => ({
        messageId: msg._id,
        conversationId: msg.conversationId,
        sender: msg.sender,
        payloads: msg.payloads, // Client will filter for their device
        messageType: msg.messageType,
        replyTo: msg.replyTo,
        isEdited: msg.isEdited,
        editedAt: msg.editedAt,
        deliveredTo: msg.deliveredTo,
        readBy: msg.readBy,
        createdAt: msg.createdAt,
      })),
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    },
    message: 'Messages retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const markMessageDelivered = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { messageId } = req.params as { messageId: string };

  await messageService.markAsDelivered(messageId, userId, req.id);

  const response: ApiResponse = {
    success: true,
    message: 'Message marked as delivered',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const markMessageRead = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { messageId } = req.params as { messageId: string };

  await messageService.markAsRead(messageId, userId, req.id);

  const response: ApiResponse = {
    success: true,
    message: 'Message marked as read',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getMessageForDevice = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { messageId, deviceId } = req.params as { messageId: string; deviceId: string };

  const payload = await messageService.getMessageForDevice(messageId, deviceId, userId, req.id);

  if (!payload) {
    const response: ApiResponse = {
      success: false,
      message: 'Message payload not found for this device',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };
    res.status(404).json(response);
    return;
  }

  const response: ApiResponse = {
    success: true,
    data: {
      payload,
    },
    message: 'Message payload retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
