import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateQuery } from '../middlewares/validate';
import {
  getNotifications,
  getUnreadNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification,
} from '../controllers/notifications/notification.controller';
import Joi from 'joi';

const router = Router();

// All notification routes require authentication
router.use(authenticate);

const paginationSchema = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
  type: Joi.string().valid('friend_request', 'friend_accepted', 'system', 'game_feedback', 'message').optional(),
}).options({ stripUnknown: true, abortEarly: false });

const unreadPaginationSchema = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
}).options({ stripUnknown: true, abortEarly: false });

// Get all notifications (paginated)
router.get('/', validateQuery(paginationSchema), getNotifications);

// Get unread notifications only
router.get('/unread', validateQuery(unreadPaginationSchema), getUnreadNotifications);

// Get unread count
router.get('/unread/count', getUnreadCount);

// Mark specific notification as read
router.patch('/:id/read', markAsRead);

// Mark all notifications as read
router.patch('/read-all', markAllAsRead);

// Delete specific notification
router.delete('/:id', deleteNotification);

export default router;
