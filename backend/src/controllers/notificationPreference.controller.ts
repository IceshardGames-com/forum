import { Request, Response } from 'express';
import { notificationPreferenceService } from '../services/notificationPreference.service';
import { asyncErrorHandler } from '../middlewares/errorHandler';
import { createLogger } from '../utils/logger';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const getNotificationPreferences = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const preferences = await notificationPreferenceService.getUserPreferences(req.user!._id);

  const response: ApiResponse = {
    success: true,
    data: {
      preferences: preferences.preferences,
      emailNotifications: preferences.emailNotifications,
      pushNotifications: preferences.pushNotifications,
      updatedAt: preferences.updatedAt,
    },
    message: 'Notification preferences retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const updateNotificationPreferences = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;
  
  const updatedPreferences = await notificationPreferenceService.updateUserPreferences(
    userId,
    req.body
  );

  logger.info('Notification preferences updated', {
    userId,
    preferences: req.body,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      preferences: updatedPreferences.preferences,
      emailNotifications: updatedPreferences.emailNotifications,
      pushNotifications: updatedPreferences.pushNotifications,
      updatedAt: updatedPreferences.updatedAt,
    },
    message: 'Notification preferences updated successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const resetNotificationPreferences = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;

  const preferences = await notificationPreferenceService.resetToDefaults(userId);

  logger.info('Notification preferences reset to defaults', { userId });

  const response: ApiResponse = {
    success: true,
    data: {
      preferences: preferences.preferences,
      emailNotifications: preferences.emailNotifications,
      pushNotifications: preferences.pushNotifications,
      updatedAt: preferences.updatedAt,
    },
    message: 'Notification preferences reset to defaults',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
