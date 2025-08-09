import NotificationPreference, { INotificationPreference } from '../models/NotificationPreference';
import { createLogger } from '../utils/logger';
import { AppError } from '../middlewares/errorHandler';
import { Types } from 'mongoose';
import { NotificationType } from '../models/Notification';

const logger = createLogger();

export interface NotificationPreferenceUpdate {
  preferences?: {
    friend_request?: boolean;
    friend_accepted?: boolean;
    system?: boolean;
    game_feedback?: boolean;
    message?: boolean;
  };
  emailNotifications?: boolean;
  pushNotifications?: boolean;
}

export class NotificationPreferenceService {
  private static instance: NotificationPreferenceService;

  private constructor() {}

  public static getInstance(): NotificationPreferenceService {
    if (!NotificationPreferenceService.instance) {
      NotificationPreferenceService.instance = new NotificationPreferenceService();
    }
    return NotificationPreferenceService.instance;
  }

  /**
   * Get user notification preferences (create defaults if not exist)
   */
  public async getUserPreferences(userId: string): Promise<INotificationPreference> {
    try {
      let preferences = await NotificationPreference.findOne({ 
        user: new Types.ObjectId(userId) 
      });

      if (!preferences) {
        // Create default preferences for new user
        preferences = await NotificationPreference.create({
          user: new Types.ObjectId(userId),
          preferences: {
            friend_request: true,
            friend_accepted: true,
            system: true,
            game_feedback: true,
            message: true,
          },
          emailNotifications: false,
          pushNotifications: true,
        });

        logger.info('Default notification preferences created', { userId });
      }

      return preferences;
    } catch (error) {
      logger.error('Failed to get notification preferences', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to get notification preferences', 500, 'PREFERENCES_ERROR');
    }
  }

  /**
   * Update user notification preferences
   */
  public async updateUserPreferences(
    userId: string,
    updates: NotificationPreferenceUpdate
  ): Promise<INotificationPreference> {
    try {
      const updateData: any = {};

      if (updates.preferences) {
        Object.keys(updates.preferences).forEach(key => {
          const value = updates.preferences![key as keyof typeof updates.preferences];
          if (value !== undefined) {
            updateData[`preferences.${key}`] = value;
          }
        });
      }

      if (updates.emailNotifications !== undefined) {
        updateData.emailNotifications = updates.emailNotifications;
      }

      if (updates.pushNotifications !== undefined) {
        updateData.pushNotifications = updates.pushNotifications;
      }

      const preferences = await NotificationPreference.findOneAndUpdate(
        { user: new Types.ObjectId(userId) },
        { $set: updateData },
        { new: true, upsert: true }
      );

      if (!preferences) {
        throw new AppError('Failed to update preferences', 500, 'UPDATE_FAILED');
      }

      logger.info('Notification preferences updated', {
        userId,
        updates: Object.keys(updateData),
      });

      return preferences;
    } catch (error) {
      logger.error('Failed to update notification preferences', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        updates,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to update notification preferences', 500, 'PREFERENCES_ERROR');
    }
  }

  /**
   * Check if user has enabled notifications for a specific type
   */
  public async isNotificationEnabled(
    userId: string,
    notificationType: NotificationType
  ): Promise<boolean> {
    try {
      const preferences = await this.getUserPreferences(userId);
      return preferences.preferences[notificationType] || false;
    } catch (error) {
      logger.error('Failed to check notification preference', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        notificationType,
      });
      // Default to true if we can't check preferences (fail open)
      return true;
    }
  }

  /**
   * Check if user has enabled push notifications
   */
  public async isPushNotificationEnabled(userId: string): Promise<boolean> {
    try {
      const preferences = await this.getUserPreferences(userId);
      return preferences.pushNotifications;
    } catch (error) {
      logger.error('Failed to check push notification preference', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      // Default to true if we can't check preferences
      return true;
    }
  }

  /**
   * Check if user has enabled email notifications
   */
  public async isEmailNotificationEnabled(userId: string): Promise<boolean> {
    try {
      const preferences = await this.getUserPreferences(userId);
      return preferences.emailNotifications;
    } catch (error) {
      logger.error('Failed to check email notification preference', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      // Default to false for email (privacy)
      return false;
    }
  }

  /**
   * Reset user preferences to defaults
   */
  public async resetToDefaults(userId: string): Promise<INotificationPreference> {
    try {
      const preferences = await NotificationPreference.findOneAndUpdate(
        { user: new Types.ObjectId(userId) },
        {
          $set: {
            preferences: {
              friend_request: true,
              friend_accepted: true,
              system: true,
              game_feedback: true,
              message: true,
            },
            emailNotifications: false,
            pushNotifications: true,
          }
        },
        { new: true, upsert: true }
      );

      if (!preferences) {
        throw new AppError('Failed to reset preferences', 500, 'RESET_FAILED');
      }

      logger.info('Notification preferences reset to defaults', { userId });

      return preferences;
    } catch (error) {
      logger.error('Failed to reset notification preferences', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to reset notification preferences', 500, 'PREFERENCES_ERROR');
    }
  }

  /**
   * Delete user preferences (for account deletion)
   */
  public async deleteUserPreferences(userId: string): Promise<void> {
    try {
      await NotificationPreference.deleteOne({
        user: new Types.ObjectId(userId),
      });

      logger.info('Notification preferences deleted', { userId });
    } catch (error) {
      logger.error('Failed to delete notification preferences', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to delete notification preferences', 500, 'DELETE_FAILED');
    }
  }

  /**
   * Get notification preferences for multiple users (for bulk operations)
   */
  public async getBulkPreferences(
    userIds: string[]
  ): Promise<Map<string, INotificationPreference>> {
    try {
      const objectIds = userIds.map(id => new Types.ObjectId(id));
      const preferences = await NotificationPreference.find({
        user: { $in: objectIds }
      });

      const preferenceMap = new Map<string, INotificationPreference>();
      preferences.forEach(pref => {
        preferenceMap.set(pref.user.toString(), pref);
      });

      // Create default preferences for users who don't have them
      const missingUserIds = userIds.filter(id => !preferenceMap.has(id));
      for (const userId of missingUserIds) {
        const defaultPrefs = await this.getUserPreferences(userId);
        preferenceMap.set(userId, defaultPrefs);
      }

      return preferenceMap;
    } catch (error) {
      logger.error('Failed to get bulk notification preferences', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userCount: userIds.length,
      });
      throw new AppError('Failed to get bulk notification preferences', 500, 'BULK_PREFERENCES_ERROR');
    }
  }
}

export const notificationPreferenceService = NotificationPreferenceService.getInstance();
