import { Request, Response } from 'express';
import { notificationService } from '../../services/notifications/notification.service';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';
import { NotificationType } from '../../models/Notification';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const getNotifications = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 50);
  const type = req.query.type as NotificationType | undefined;

  const data = await notificationService.getNotifications(req.user!._id, { page, limit }, type);

  logger.info('Notifications retrieved', {
    userId: req.user!._id,
    page,
    limit,
    total: data.total,
    unreadCount: data.unreadCount,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      notifications: data.notifications,
      pagination: {
        page,
        limit,
        total: data.total,
      },
      unreadCount: data.unreadCount,
    },
    message: 'Notifications retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getUnreadNotifications = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 50);

  const notifications = await notificationService.getUnreadNotifications(req.user!._id, { page, limit });

  const response: ApiResponse = {
    success: true,
    data: {
      notifications,
      pagination: { page, limit },
    },
    message: 'Unread notifications retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getUnreadCount = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const count = await notificationService.getUnreadCount(req.user!._id);

  const response: ApiResponse = {
    success: true,
    data: { count },
    message: 'Unread count retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const markAsRead = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { id } = req.params as { id: string };

  await notificationService.markAsRead(id, req.user!._id, req.id);

  const response: ApiResponse = {
    success: true,
    message: 'Notification marked as read',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const markAllAsRead = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const modifiedCount = await notificationService.markAllAsRead(req.user!._id, req.id);

  logger.info('All notifications marked as read', {
    userId: req.user!._id,
    modifiedCount,
  });

  const response: ApiResponse = {
    success: true,
    data: { modifiedCount },
    message: 'All notifications marked as read',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const deleteNotification = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { id } = req.params as { id: string };

  await notificationService.deleteNotification(id, req.user!._id, req.id);

  const response: ApiResponse = {
    success: true,
    message: 'Notification deleted successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
