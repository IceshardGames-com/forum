import { Request, Response } from 'express';
import { conversationService } from '../../services/chats/conversation.service';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const createOrGetConversation = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;
  const { otherUserId } = req.body;

  const conversation = await conversationService.createOrGetConversation(
    userId,
    otherUserId,
    req.id
  );

  logger.info('Conversation created or retrieved', {
    conversationId: conversation._id,
    userId,
    otherUserId,
    isFriendBased: conversation.isFriendBased,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      conversationId: conversation._id,
      participants: conversation.participants,
      isFriendBased: conversation.isFriendBased,
      initiatedBy: conversation.initiatedBy,
      lastMessageAt: conversation.lastMessageAt,
      createdAt: conversation.createdAt,
    },
    message: 'Conversation ready',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getUserConversations = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 50);

  const { conversations, total } = await conversationService.getUserConversations(
    userId,
    { page, limit },
    req.id
  );

  const response: ApiResponse = {
    success: true,
    data: {
      conversations: conversations.map(conv => ({
        conversationId: conv._id,
        otherParticipant: (conv as any).otherParticipant,
        lastMessage: (conv as any).lastMessageDetail ? {
          messageId: (conv as any).lastMessageDetail._id,
          messageType: (conv as any).lastMessageDetail.messageType,
          createdAt: (conv as any).lastMessageDetail.createdAt,
          // Note: we don't return encrypted content in conversation list
        } : null,
        lastMessageAt: conv.lastMessageAt,
        isFriendBased: conv.isFriendBased,
        createdAt: conv.createdAt,
      })),
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    },
    message: 'Conversations retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getConversation = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { conversationId } = req.params as { conversationId: string };

  const conversation = await conversationService.getConversationById(
    conversationId,
    userId,
    req.id
  );

  const response: ApiResponse = {
    success: true,
    data: {
      conversationId: conversation._id,
      participants: conversation.participants,
      isFriendBased: conversation.isFriendBased,
      initiatedBy: conversation.initiatedBy,
      lastMessageAt: conversation.lastMessageAt,
      createdAt: conversation.createdAt,
    },
    message: 'Conversation retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const deleteConversation = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;
  const { conversationId } = req.params as { conversationId: string };

  await conversationService.deleteConversationForUser(conversationId, userId, req.id);

  logger.info('Conversation deleted for user', {
    conversationId,
    userId,
  });

  const response: ApiResponse = {
    success: true,
    message: 'Conversation deleted successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getUnreadCount = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;

  const unreadCount = await conversationService.getUnreadCount(userId, req.id);

  const response: ApiResponse = {
    success: true,
    data: {
      unreadCount,
    },
    message: 'Unread count retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
