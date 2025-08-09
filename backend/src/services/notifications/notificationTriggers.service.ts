import { notificationService, NotificationData } from './notification.service';
import { NotificationType } from '../../models/Notification';
import { createLogger } from '../../utils/logger';
import { SocketManager } from '../../utils/socketManager';

const logger = createLogger();

/**
 * NotificationTriggers - Centralized service for triggering notifications
 * 
 * This service provides a clean interface for other services to trigger
 * notifications without directly coupling to the notification system.
 */
export class NotificationTriggers {
  
  /**
   * Trigger when a friend request is sent
   */
  static async onFriendRequestSent(
    senderId: string, 
    receiverId: string, 
    senderName: string,
    requestId?: string
  ): Promise<void> {
    try {
      await notificationService.sendNotification({
        recipient: receiverId,
        type: 'friend_request' as NotificationType,
        title: 'New Friend Request',
        message: `${senderName} sent you a friend request`,
        link: `/friends/requests`,
        metadata: { 
          senderId, 
          action: 'friend_request',
          senderName 
        }
      }, requestId);

      // Emit real-time friend request update
      SocketManager.emitFriendRequestUpdate(receiverId, 'received', {
        senderId,
        senderName,
        type: 'friend_request'
      });

      logger.info('Friend request notification triggered', {
        senderId,
        receiverId,
        senderName,
      });
    } catch (error) {
      logger.error('Failed to trigger friend request notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        senderId,
        receiverId,
      });
    }
  }

  /**
   * Trigger when a friend request is accepted
   */
  static async onFriendRequestAccepted(
    accepterId: string, 
    requesterId: string, 
    accepterName: string,
    requestId?: string
  ): Promise<void> {
    try {
      await notificationService.sendNotification({
        recipient: requesterId,
        type: 'friend_accepted' as NotificationType,
        title: 'Friend Request Accepted',
        message: `${accepterName} accepted your friend request`,
        link: `/friends`,
        metadata: { 
          accepterId, 
          action: 'friend_accepted',
          accepterName 
        }
      }, requestId);

      // Emit real-time friend request update to both users
      SocketManager.emitFriendRequestUpdate(requesterId, 'accepted', {
        accepterId,
        accepterName,
        type: 'friend_accepted'
      });

      SocketManager.emitFriendRequestUpdate(accepterId, 'accepted', {
        friendId: requesterId,
        type: 'friend_accepted'
      });

      logger.info('Friend request accepted notification triggered', {
        accepterId,
        requesterId,
        accepterName,
      });
    } catch (error) {
      logger.error('Failed to trigger friend request accepted notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        accepterId,
        requesterId,
      });
    }
  }

  /**
   * Trigger when a friend request is declined
   */
  static async onFriendRequestDeclined(
    declinerId: string,
    requesterId: string,
    declinerName: string,
    _requestId?: string
  ): Promise<void> {
    try {
      // Note: Usually you don't send notifications for declined requests
      // as it can be seen as negative UX, but we'll emit a real-time update
      
      SocketManager.emitFriendRequestUpdate(requesterId, 'declined', {
        declinerId,
        declinerName,
        type: 'friend_declined'
      });

      logger.info('Friend request declined update triggered', {
        declinerId,
        requesterId,
        declinerName,
      });
    } catch (error) {
      logger.error('Failed to trigger friend request declined update', {
        error: error instanceof Error ? error.message : 'Unknown error',
        declinerId,
        requesterId,
      });
    }
  }

  /**
   * Trigger when a user is blocked
   */
  static async onUserBlocked(
    blockerId: string,
    blockedUserId: string,
    _requestId?: string
  ): Promise<void> {
    try {
      // Emit real-time update to inform about blocking
      SocketManager.emitFriendRequestUpdate(blockedUserId, 'declined', {
        blockerId,
        type: 'blocked',
        action: 'blocked'
      });

      logger.info('User blocked update triggered', {
        blockerId,
        blockedUserId,
      });
    } catch (error) {
      logger.error('Failed to trigger user blocked update', {
        error: error instanceof Error ? error.message : 'Unknown error',
        blockerId,
        blockedUserId,
      });
    }
  }

  /**
   * Trigger when a game result occurs
   */
  static async onGameResult(
    playerId: string, 
    gameResult: string, 
    opponentName?: string,
    gameId?: string,
    requestId?: string
  ): Promise<void> {
    try {
      await notificationService.sendNotification({
        recipient: playerId,
        type: 'game_feedback' as NotificationType,
        title: 'Game Result',
        message: opponentName 
          ? `You ${gameResult} against ${opponentName}` 
          : `Game completed: ${gameResult}`,
        link: '/games/history',
        metadata: { 
          gameResult, 
          opponentName,
          gameId,
          action: 'game_completed'
        }
      }, requestId);

      logger.info('Game result notification triggered', {
        playerId,
        gameResult,
        opponentName,
        gameId,
      });
    } catch (error) {
      logger.error('Failed to trigger game result notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        playerId,
        gameResult,
      });
    }
  }

  /**
   * Trigger when a new message is received
   */
  static async onMessageReceived(
    recipientId: string, 
    senderId: string, 
    senderName: string, 
    preview: string,
    conversationId?: string,
    requestId?: string
  ): Promise<void> {
    try {
      const truncatedPreview = preview.length > 50 
        ? preview.substring(0, 50) + '...' 
        : preview;

      await notificationService.sendNotification({
        recipient: recipientId,
        type: 'message' as NotificationType,
        title: `Message from ${senderName}`,
        message: truncatedPreview,
        link: `/messages/${senderId}`,
        metadata: { 
          senderId, 
          messagePreview: preview,
          conversationId,
          action: 'message_received'
        }
      }, requestId);

      logger.info('Message notification triggered', {
        recipientId,
        senderId,
        senderName,
        conversationId,
      });
    } catch (error) {
      logger.error('Failed to trigger message notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        recipientId,
        senderId,
      });
    }
  }

  /**
   * Trigger system announcement to multiple users
   */
  static async onSystemAnnouncement(
    userIds: string[], 
    title: string, 
    message: string, 
    link?: string,
    metadata?: Record<string, any>,
    requestId?: string
  ): Promise<void> {
    try {
      const notificationData: Omit<NotificationData, 'recipient'> = {
        type: 'system' as NotificationType,
        title,
        message,
        metadata: { 
          isSystemMessage: true,
          ...metadata
        }
      };

      if (link) {
        notificationData.link = link;
      }

      await notificationService.bulkSendNotifications(userIds, notificationData, requestId);

      logger.info('System announcement triggered', {
        userCount: userIds.length,
        title,
      });
    } catch (error) {
      logger.error('Failed to trigger system announcement', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userCount: userIds.length,
        title,
      });
    }
  }

  /**
   * Trigger welcome notification for new users
   */
  static async onUserRegistered(
    userId: string,
    username: string,
    requestId?: string
  ): Promise<void> {
    try {
      await notificationService.sendNotification({
        recipient: userId,
        type: 'system' as NotificationType,
        title: `Welcome to the Gaming Community, ${username}!`,
        message: 'Explore games, connect with friends, and join the community discussions.',
        link: '/welcome',
        metadata: {
          action: 'user_welcome',
          isWelcomeMessage: true
        }
      }, requestId);

      logger.info('Welcome notification triggered', {
        userId,
        username,
      });
    } catch (error) {
      logger.error('Failed to trigger welcome notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        username,
      });
    }
  }

  /**
   * Trigger community post interaction (like, comment)
   */
  static async onPostInteraction(
    postAuthorId: string,
    interactorId: string,
    interactorName: string,
    interactionType: 'like' | 'comment',
    postTitle: string,
    postId: string,
    requestId?: string
  ): Promise<void> {
    try {
      // Don't notify users about their own interactions
      if (postAuthorId === interactorId) return;

      const messages = {
        like: `${interactorName} liked your post "${postTitle}"`,
        comment: `${interactorName} commented on your post "${postTitle}"`
      };

      await notificationService.sendNotification({
        recipient: postAuthorId,
        type: 'system' as NotificationType,
        title: 'Post Interaction',
        message: messages[interactionType],
        link: `/posts/${postId}`,
        metadata: {
          interactorId,
          interactorName,
          interactionType,
          postId,
          action: 'post_interaction'
        }
      }, requestId);

      logger.info('Post interaction notification triggered', {
        postAuthorId,
        interactorId,
        interactionType,
        postId,
      });
    } catch (error) {
      logger.error('Failed to trigger post interaction notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        postAuthorId,
        interactorId,
        interactionType,
      });
    }
  }
}

export const notificationTriggers = NotificationTriggers;
