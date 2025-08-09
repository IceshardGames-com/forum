import Notification, { INotification, NotificationType } from '../models/Notification';
import { createLogger } from '../utils/logger';
import { AppError } from '../middlewares/errorHandler';
import { Types } from 'mongoose';
import { SocketManager } from '../utils/socketManager';
import { notificationPreferenceService } from './notificationPreference.service';

export interface NotificationData {
  recipient: string;
  type: NotificationType;
  title: string;
  message: string;
  link?: string;
  metadata?: Record<string, any>;
}

export interface PaginationOptions {
  page?: number;
  limit?: number;
}

export class NotificationService {
  private static instance: NotificationService;

  private constructor() {}

  public static getInstance(): NotificationService {
    if (!NotificationService.instance) {
      NotificationService.instance = new NotificationService();
    }
    return NotificationService.instance;
  }

  /**
   * Send a notification to a user (respects user preferences)
   */
  public async sendNotification(data: NotificationData, requestId?: string): Promise<INotification | null> {
    const logger = createLogger(requestId);

    try {
      // Check if user has enabled this type of notification
      const isEnabled = await notificationPreferenceService.isNotificationEnabled(
        data.recipient,
        data.type
      );

      if (!isEnabled) {
        logger.info('Notification skipped due to user preferences', {
          recipient: data.recipient,
          type: data.type,
        });
        return null;
      }

      const notification = await Notification.create({
        recipient: new Types.ObjectId(data.recipient),
        type: data.type,
        title: data.title,
        message: data.message,
        link: data.link,
        metadata: data.metadata || {},
      });

      logger.info('Notification created', {
        notificationId: notification._id,
        recipient: data.recipient,
        type: data.type,
      });

      // Check if user has enabled push notifications before emitting
      const pushEnabled = await notificationPreferenceService.isPushNotificationEnabled(data.recipient);
      if (pushEnabled) {
        SocketManager.emitNotification(data.recipient, notification);
        
        // Also emit updated unread count
        const unreadCount = await this.getUnreadCount(data.recipient);
        SocketManager.emitUnreadCount(data.recipient, unreadCount);
      }

      return notification;
    } catch (error) {
      logger.error('Failed to send notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        recipient: data.recipient,
        type: data.type,
      });
      throw new AppError('Failed to send notification', 500, 'NOTIFICATION_ERROR');
    }
  }

  /**
   * Mark notification as read
   */
  public async markAsRead(notificationId: string, userId: string, requestId?: string): Promise<void> {
    const logger = createLogger(requestId);

    try {
      const result = await Notification.updateOne(
        { _id: notificationId, recipient: new Types.ObjectId(userId) },
        { isRead: true }
      );

      if (result.matchedCount === 0) {
        throw new AppError('Notification not found', 404, 'NOTIFICATION_NOT_FOUND');
      }

      logger.info('Notification marked as read', {
        notificationId,
        userId,
      });

      // Emit real-time update
      SocketManager.emitNotificationRead(userId, notificationId);
      
      // Emit updated unread count
      const unreadCount = await this.getUnreadCount(userId);
      SocketManager.emitUnreadCount(userId, unreadCount);
    } catch (error) {
      logger.error('Failed to mark notification as read', {
        error: error instanceof Error ? error.message : 'Unknown error',
        notificationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to mark notification as read', 500, 'NOTIFICATION_ERROR');
    }
  }

  /**
   * Mark all notifications as read for a user
   */
  public async markAllAsRead(userId: string, requestId?: string): Promise<number> {
    const logger = createLogger(requestId);

    try {
      const result = await Notification.updateMany(
        { recipient: new Types.ObjectId(userId), isRead: false },
        { isRead: true }
      );

      logger.info('All notifications marked as read', {
        userId,
        modifiedCount: result.modifiedCount,
      });

      // Emit real-time update - unread count is now 0
      SocketManager.emitUnreadCount(userId, 0);
      SocketManager.emitAllNotificationsRead(userId);

      return result.modifiedCount;
    } catch (error) {
      logger.error('Failed to mark all notifications as read', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to mark all notifications as read', 500, 'NOTIFICATION_ERROR');
    }
  }

  /**
   * Get unread notifications for a user
   */
  public async getUnreadNotifications(userId: string, { page = 1, limit = 20 }: PaginationOptions = {}): Promise<INotification[]> {
    const skip = (page - 1) * limit;

    return Notification.find({ 
      recipient: new Types.ObjectId(userId), 
      isRead: false 
    })
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit)
      .lean();
  }

  /**
   * Get all notifications for a user (read and unread)
   */
  public async getNotifications(
    userId: string, 
    { page = 1, limit = 20 }: PaginationOptions = {},
    type?: NotificationType
  ): Promise<{ notifications: INotification[]; total: number; unreadCount: number }> {
    const skip = (page - 1) * limit;
    const filter: any = { recipient: new Types.ObjectId(userId) };
    
    if (type) {
      filter.type = type;
    }

    const [notifications, total, unreadCount] = await Promise.all([
      Notification.find(filter)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .lean(),
      Notification.countDocuments(filter),
      Notification.countDocuments({ 
        recipient: new Types.ObjectId(userId), 
        isRead: false 
      }),
    ]);

    return { notifications, total, unreadCount };
  }

  /**
   * Delete a notification
   */
  public async deleteNotification(notificationId: string, userId: string, requestId?: string): Promise<void> {
    const logger = createLogger(requestId);

    try {
      const result = await Notification.deleteOne({
        _id: notificationId,
        recipient: new Types.ObjectId(userId),
      });

      if (result.deletedCount === 0) {
        throw new AppError('Notification not found', 404, 'NOTIFICATION_NOT_FOUND');
      }

      logger.info('Notification deleted', {
        notificationId,
        userId,
      });

      // Emit real-time update
      SocketManager.emitNotificationDeleted(userId, notificationId);
      
      // Emit updated unread count
      const unreadCount = await this.getUnreadCount(userId);
      SocketManager.emitUnreadCount(userId, unreadCount);
    } catch (error) {
      logger.error('Failed to delete notification', {
        error: error instanceof Error ? error.message : 'Unknown error',
        notificationId,
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to delete notification', 500, 'NOTIFICATION_ERROR');
    }
  }

  /**
   * Get unread count for a user
   */
  public async getUnreadCount(userId: string): Promise<number> {
    return Notification.countDocuments({
      recipient: new Types.ObjectId(userId),
      isRead: false,
    });
  }

  /**
   * Bulk send notifications (for system announcements) - respects user preferences
   */
  public async bulkSendNotifications(
    userIds: string[],
    notificationData: Omit<NotificationData, 'recipient'>,
    requestId?: string
  ): Promise<number> {
    const logger = createLogger(requestId);

    try {
      // Get preferences for all users
      const userPreferences = await notificationPreferenceService.getBulkPreferences(userIds);
      
      // Filter users who have this notification type enabled
      const enabledUserIds = userIds.filter(userId => {
        const preferences = userPreferences.get(userId);
        return preferences?.preferences?.[notificationData.type] ?? true;
      });

      if (enabledUserIds.length === 0) {
        logger.info('Bulk notifications skipped - no users have this type enabled', {
          originalCount: userIds.length,
          type: notificationData.type,
        });
        return 0;
      }

      const notifications = enabledUserIds.map(userId => ({
        recipient: new Types.ObjectId(userId),
        type: notificationData.type,
        title: notificationData.title,
        message: notificationData.message,
        link: notificationData.link,
        metadata: notificationData.metadata || {},
      }));

      const result = await Notification.insertMany(notifications);

      // Emit real-time notifications for users with push enabled
      for (const userId of enabledUserIds) {
        const preferences = userPreferences.get(userId);
        if (preferences?.pushNotifications) {
          const notification = result.find(n => n.recipient.toString() === userId);
          if (notification) {
            SocketManager.emitNotification(userId, notification as INotification);
            
            // Also emit updated unread count
            const unreadCount = await this.getUnreadCount(userId);
            SocketManager.emitUnreadCount(userId, unreadCount);
          }
        }
      }

      logger.info('Bulk notifications sent', {
        count: result.length,
        originalCount: userIds.length,
        type: notificationData.type,
      });

      return result.length;
    } catch (error) {
      logger.error('Failed to send bulk notifications', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userCount: userIds.length,
        type: notificationData.type,
      });
      throw new AppError('Failed to send bulk notifications', 500, 'NOTIFICATION_ERROR');
    }
  }
}

export const notificationService = NotificationService.getInstance();
