import Conversation, { IConversation } from '../../models/Conversation';
import { createLogger } from '../../utils/logger';
import { AppError } from '../../middlewares/errorHandler';
import { Types } from 'mongoose';
import { friendshipService } from '../friendship.service';

export interface PaginationOptions {
  page?: number;
  limit?: number;
}

export class ConversationService {
  private static instance: ConversationService;

  private constructor() {}

  public static getInstance(): ConversationService {
    if (!ConversationService.instance) {
      ConversationService.instance = new ConversationService();
    }
    return ConversationService.instance;
  }

  /**
   * Create or get existing conversation between two users
   */
  public async createOrGetConversation(
    userId: string,
    otherUserId: string,
    requestId?: string
  ): Promise<IConversation> {
    const loggerWithId = createLogger(requestId);

    try {
      if (userId === otherUserId) {
        throw new AppError('Cannot create conversation with yourself', 400, 'INVALID_PARTICIPANTS');
      }

      // Sort participant IDs for consistent lookup
      const participants = [userId, otherUserId].sort();
      const participantIds = participants.map(id => new Types.ObjectId(id));

      // Check if conversation already exists
      let conversation = await Conversation.findOne({
        participants: participantIds,
        isActive: true,
      });

      if (conversation) {
        // Check if conversation is deleted for the requesting user
        const isDeletedForUser = conversation.deletedBy?.some(id => id.toString() === userId) || false;
        if (isDeletedForUser) {
          // Restore conversation for this user
          conversation.deletedBy = conversation.deletedBy?.filter(
            id => id.toString() !== userId
          ) || [];
          await conversation.save();
        }

        loggerWithId.info('Existing conversation retrieved', {
          conversationId: conversation._id,
          participants,
        });

        return conversation;
      }

      // Check friendship status
      const areFriends = await friendshipService.areFriends(userId, otherUserId);

      // Create new conversation
      conversation = await Conversation.create({
        participants: participantIds,
        isFriendBased: areFriends,
        initiatedBy: areFriends ? null : new Types.ObjectId(userId),
        isActive: true,
      });

      loggerWithId.info('New conversation created', {
        conversationId: conversation._id,
        participants,
        isFriendBased: areFriends,
        initiatedBy: conversation.initiatedBy,
      });

      return conversation;
    } catch (error) {
      loggerWithId.error('Failed to create or get conversation', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        otherUserId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to create conversation', 500, 'CONVERSATION_ERROR');
    }
  }

  /**
   * Get user's conversations with pagination
   */
  public async getUserConversations(
    userId: string,
    { page = 1, limit = 20 }: PaginationOptions = {},
    requestId?: string
  ): Promise<{ conversations: IConversation[]; total: number }> {
    const loggerWithId = createLogger(requestId);

    try {
      const skip = (page - 1) * limit;
      const userObjectId = new Types.ObjectId(userId);

      const pipeline = [
        {
          $match: {
            participants: userObjectId,
            isActive: true,
            $or: [
              { deletedBy: { $exists: false } },
              { deletedBy: { $ne: userObjectId } },
            ],
          },
        },
        {
          $lookup: {
            from: 'users',
            localField: 'participants',
            foreignField: '_id',
            as: 'participantDetails',
            pipeline: [
              { $project: { username: 1, email: 1, avatar: 1 } },
            ],
          },
        },
        {
          $lookup: {
            from: 'messages',
            localField: 'lastMessage',
            foreignField: '_id',
            as: 'lastMessageDetails',
          },
        },
        {
          $addFields: {
            otherParticipant: {
              $arrayElemAt: [
                {
                  $filter: {
                    input: '$participantDetails',
                    cond: { $ne: ['$$this._id', userObjectId] },
                  },
                },
                0,
              ],
            },
            lastMessageDetail: { $arrayElemAt: ['$lastMessageDetails', 0] },
          },
        },
        {
          $sort: { lastMessageAt: -1 as const, createdAt: -1 as const },
        },
        {
          $facet: {
            conversations: [{ $skip: skip }, { $limit: limit }],
            totalCount: [{ $count: 'count' }],
          },
        },
      ];

      const result = await Conversation.aggregate(pipeline);
      const conversations = result[0].conversations || [];
      const total = result[0].totalCount[0]?.count || 0;

      loggerWithId.info('User conversations retrieved', {
        userId,
        page,
        limit,
        total,
        retrieved: conversations.length,
      });

      return { conversations, total };
    } catch (error) {
      loggerWithId.error('Failed to get user conversations', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to get conversations', 500, 'CONVERSATION_FETCH_ERROR');
    }
  }

  /**
   * Get conversation by ID with validation
   */
  public async getConversationById(
    conversationId: string,
    userId: string,
    requestId?: string
  ): Promise<IConversation> {
    const loggerWithId = createLogger(requestId);

    try {
      const conversation = await Conversation.findById(conversationId)
        .populate('participants', 'username email avatar');

      if (!conversation) {
        throw new AppError('Conversation not found', 404, 'CONVERSATION_NOT_FOUND');
      }

      const isParticipant = conversation.participants.some((p: any) => p.toString() === userId);
      if (!isParticipant) {
        throw new AppError('Access denied', 403, 'ACCESS_DENIED');
      }

      const isDeletedForUser = conversation.deletedBy?.some(id => id.toString() === userId) || false;
      if (!conversation.isActive || isDeletedForUser) {
        throw new AppError('Conversation not accessible', 404, 'CONVERSATION_NOT_ACCESSIBLE');
      }

      return conversation;
    } catch (error) {
      loggerWithId.error('Failed to get conversation by ID', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to get conversation', 500, 'CONVERSATION_FETCH_ERROR');
    }
  }

  /**
   * Update conversation last message
   */
  public async updateLastMessage(
    conversationId: string,
    messageId: string,
    requestId?: string
  ): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      await Conversation.updateOne(
        { _id: conversationId },
        {
          lastMessage: new Types.ObjectId(messageId),
          lastMessageAt: new Date(),
        }
      );

      loggerWithId.debug('Conversation last message updated', {
        conversationId,
        messageId,
      });
    } catch (error) {
      loggerWithId.error('Failed to update conversation last message', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        messageId,
      });
      // Don't throw error as this is not critical
    }
  }

  /**
   * Delete conversation for user (soft delete)
   */
  public async deleteConversationForUser(
    conversationId: string,
    userId: string,
    requestId?: string
  ): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      const conversation = await this.getConversationById(conversationId, userId, requestId);

      // Add user to deletedBy array
      if (!conversation.deletedBy) {
        conversation.deletedBy = [];
      }

      const userObjectId = new Types.ObjectId(userId);
      if (!conversation.deletedBy.some(id => id.equals(userObjectId))) {
        conversation.deletedBy.push(userObjectId);
      }

      // If both participants have deleted, mark conversation as inactive
      if (conversation.deletedBy.length >= 2) {
        conversation.isActive = false;
      }

      await conversation.save();

      loggerWithId.info('Conversation deleted for user', {
        conversationId,
        userId,
        totalDeleted: conversation.deletedBy.length,
        isActive: conversation.isActive,
      });
    } catch (error) {
      loggerWithId.error('Failed to delete conversation for user', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to delete conversation', 500, 'CONVERSATION_DELETE_ERROR');
    }
  }

  /**
   * Get unread message count for user across all conversations
   */
  public async getUnreadCount(userId: string, requestId?: string): Promise<number> {
    const loggerWithId = createLogger(requestId);

    try {
      const userObjectId = new Types.ObjectId(userId);

      const pipeline = [
        {
          $match: {
            participants: userObjectId,
            isActive: true,
            $or: [
              { deletedBy: { $exists: false } },
              { deletedBy: { $ne: userObjectId } },
            ],
          },
        },
        {
          $lookup: {
            from: 'messages',
            localField: '_id',
            foreignField: 'conversationId',
            as: 'messages',
            pipeline: [
              {
                $match: {
                  sender: { $ne: userObjectId },
                  readBy: { $ne: userObjectId },
                },
              },
              { $count: 'unreadCount' },
            ],
          },
        },
        {
          $addFields: {
            unreadCount: { $arrayElemAt: ['$messages.unreadCount', 0] },
          },
        },
        {
          $group: {
            _id: null,
            totalUnread: { $sum: { $ifNull: ['$unreadCount', 0] } },
          },
        },
      ];

      const result = await Conversation.aggregate(pipeline);
      const unreadCount = result[0]?.totalUnread || 0;

      loggerWithId.debug('Unread count calculated', {
        userId,
        unreadCount,
      });

      return unreadCount;
    } catch (error) {
      loggerWithId.error('Failed to get unread count', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to get unread count', 500, 'UNREAD_COUNT_ERROR');
    }
  }

  /**
   * Mark conversation as friend-based when users become friends
   */
  public async markAsFriendBased(conversationId: string, requestId?: string): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      await Conversation.updateOne(
        { _id: conversationId },
        { isFriendBased: true, initiatedBy: null }
      );

      loggerWithId.info('Conversation marked as friend-based', {
        conversationId,
      });
    } catch (error) {
      loggerWithId.error('Failed to mark conversation as friend-based', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
      });
      // Don't throw error as this is not critical
    }
  }
}

export const conversationService = ConversationService.getInstance();
