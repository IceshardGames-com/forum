import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody } from '../middlewares/validate';
import {
  getNotificationPreferences,
  updateNotificationPreferences,
  resetNotificationPreferences,
} from '../controllers/notificationPreference.controller';
import Joi from 'joi';

const router = Router();

// All routes require authentication
router.use(authenticate);

const updatePreferencesSchema = Joi.object({
  preferences: Joi.object({
    friend_request: Joi.boolean(),
    friend_accepted: Joi.boolean(),
    system: Joi.boolean(),
    game_feedback: Joi.boolean(),
    message: Joi.boolean(),
  }).min(1),
  emailNotifications: Joi.boolean(),
  pushNotifications: Joi.boolean(),
}).min(1).options({ stripUnknown: true, abortEarly: false });

// Get user notification preferences
router.get('/', getNotificationPreferences);

// Update user notification preferences
router.put('/', validateBody(updatePreferencesSchema), updateNotificationPreferences);

// Reset notification preferences to defaults
router.post('/reset', resetNotificationPreferences);

export default router;
