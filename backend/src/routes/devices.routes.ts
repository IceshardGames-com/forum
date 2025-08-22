import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams } from '../middlewares/validate';
import {
  registerDevice,
  getUserDevices,
  getDevicesForUser,
  updateDeviceLastSeen,
  deactivateDevice,
} from '../controllers/chat/device.controller';
import Joi from 'joi';
import rateLimit from 'express-rate-limit';

const router = Router();

// All device routes require authentication
router.use(authenticate);

// General device limiter
const devicesLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 300,
  standardHeaders: true,
  legacyHeaders: false,
});
router.use(devicesLimiter);

const deviceRegistrationSchema = Joi.object({
  deviceId: Joi.string().required().min(1).max(255),
  publicKey: Joi.string().required().min(1),
  deviceName: Joi.string().optional().max(100),
  deviceType: Joi.string().valid('android', 'ios', 'web', 'desktop').optional(),
}).options({ stripUnknown: true, abortEarly: false });

const updateLastSeenSchema = Joi.object({
  deviceId: Joi.string().required().min(1).max(255),
}).options({ stripUnknown: true, abortEarly: false });

const userIdParamSchema = Joi.object({
  userId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
}).options({ stripUnknown: true, abortEarly: false });

const deviceIdParamSchema = Joi.object({
  deviceId: Joi.string().required().min(1).max(255),
}).options({ stripUnknown: true, abortEarly: false });

// Stricter limiter for registration
const registerDeviceLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 30,
  standardHeaders: true,
  legacyHeaders: false,
});

// Register or update device
router.post('/register', registerDeviceLimiter, validateBody(deviceRegistrationSchema), registerDevice);

// Get current user's devices
router.get('/my-devices', getUserDevices);

// Get devices for a specific user (for encryption)
router.get('/user/:userId', validateParams(userIdParamSchema), getDevicesForUser);

// Update device last seen timestamp
router.patch('/last-seen', validateBody(updateLastSeenSchema), updateDeviceLastSeen);

// Deactivate a device
router.delete('/:deviceId', validateParams(deviceIdParamSchema), deactivateDevice);

export default router;
