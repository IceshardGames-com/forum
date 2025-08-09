import { createLogger } from './logger';
import { INotification } from '../models/Notification';

const logger = createLogger();

/**
 * SocketManager - Handles real-time events via Socket.IO
 * 
 * This utility class provides a clean interface for emitting real-time
 * notifications and events to connected clients.
 */
export class SocketManager {
  /**
   * Get the Socket.IO instance from global scope
   * Note: io is set in server.ts startup
   */
  private static getIO() {
    const io = (global as any).io;
    if (!io) {
      logger.warn('Socket.IO instance not available - real-time features disabled');
      return null;
    }
    return io;
  }

  /**
   * Emit a new notification to a specific user
   */
  static emitNotification(userId: string, notification: INotification): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('newNotification', {
        id: notification._id,
        type: notification.type,
        title: notification.title,
        message: notification.message,
        link: notification.link,
        metadata: notification.metadata,
        isRead: notification.isRead,
        createdAt: notification.createdAt,
      });

      logger.info('Socket.IO: Notification emitted', {
        userId,
        notificationId: notification._id,
        type: notification.type,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        notificationId: notification._id,
      });
    }
  }

  /**
   * Emit updated unread count to a specific user
   */
  static emitUnreadCount(userId: string, count: number): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('unreadCount', { count });

      logger.info('Socket.IO: Unread count emitted', {
        userId,
        count,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit unread count', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        count,
      });
    }
  }

  /**
   * Emit notification read status to a specific user
   */
  static emitNotificationRead(userId: string, notificationId: string): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('notificationRead', { 
        notificationId,
        timestamp: new Date().toISOString(),
      });

      logger.info('Socket.IO: Notification read status emitted', {
        userId,
        notificationId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit notification read status', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        notificationId,
      });
    }
  }

  /**
   * Emit all notifications marked as read
   */
  static emitAllNotificationsRead(userId: string): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('allNotificationsRead', {
        timestamp: new Date().toISOString(),
      });

      logger.info('Socket.IO: All notifications read emitted', {
        userId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit all notifications read', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
    }
  }

  /**
   * Emit notification deletion to a specific user
   */
  static emitNotificationDeleted(userId: string, notificationId: string): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('notificationDeleted', {
        notificationId,
        timestamp: new Date().toISOString(),
      });

      logger.info('Socket.IO: Notification deletion emitted', {
        userId,
        notificationId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit notification deletion', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        notificationId,
      });
    }
  }

  /**
   * Emit friend request status updates
   */
  static emitFriendRequestUpdate(userId: string, event: 'sent' | 'received' | 'accepted' | 'declined', data: any): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('friendRequestUpdate', {
        event,
        data,
        timestamp: new Date().toISOString(),
      });

      logger.info('Socket.IO: Friend request update emitted', {
        userId,
        event,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit friend request update', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        event,
      });
    }
  }

  /**
   * Get connected users count (for monitoring)
   */
  static getConnectedUsersCount(): number {
    const io = this.getIO();
    if (!io) return 0;

    try {
      return io.engine.clientsCount;
    } catch (error) {
      logger.error('Socket.IO: Failed to get connected users count', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
      return 0;
    }
  }

  /**
   * Check if a user is currently connected
   */
  static isUserConnected(userId: string): boolean {
    const io = this.getIO();
    if (!io) return false;

    try {
      const room = io.sockets.adapter.rooms.get(userId);
      return room && room.size > 0;
    } catch (error) {
      logger.error('Socket.IO: Failed to check user connection', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      return false;
    }
  }

  /**
   * Emit encrypted chat message to specific device
   */
  static emitChatMessage(deviceId: string, messageData: any): void {
    const io = this.getIO();
    if (!io) return;

    try {
      const deviceRoom = `device:${deviceId}`;
      io.to(deviceRoom).emit('message:new', messageData);

      logger.info('Socket.IO: Chat message emitted', {
        deviceId,
        messageId: messageData.messageId,
        conversationId: messageData.conversationId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit chat message', {
        error: error instanceof Error ? error.message : 'Unknown error',
        deviceId,
        messageId: messageData.messageId,
      });
    }
  }

  /**
   * Emit message delivery confirmation
   */
  static emitMessageDelivery(userId: string, deliveryData: any): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('message:delivered', deliveryData);

      logger.info('Socket.IO: Message delivery emitted', {
        userId,
        messageId: deliveryData.messageId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit message delivery', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        messageId: deliveryData.messageId,
      });
    }
  }

  /**
   * Emit message read confirmation
   */
  static emitMessageRead(userId: string, readData: any): void {
    const io = this.getIO();
    if (!io) return;

    try {
      io.to(userId).emit('message:read', readData);

      logger.info('Socket.IO: Message read emitted', {
        userId,
        messageId: readData.messageId,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit message read', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        messageId: readData.messageId,
      });
    }
  }

  /**
   * Emit typing indicator
   */
  static emitTyping(conversationId: string, userId: string, isTyping: boolean): void {
    const io = this.getIO();
    if (!io) return;

    try {
      const conversationRoom = `conversation:${conversationId}`;
      io.to(conversationRoom).emit('typing', {
        userId,
        isTyping,
        timestamp: new Date().toISOString(),
      });

      logger.debug('Socket.IO: Typing indicator emitted', {
        conversationId,
        userId,
        isTyping,
      });
    } catch (error) {
      logger.error('Socket.IO: Failed to emit typing indicator', {
        error: error instanceof Error ? error.message : 'Unknown error',
        conversationId,
        userId,
      });
    }
  }
}
