import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import {
  sendMessage,
  getConversationMessages,
  markMessageDelivered,
  markMessageRead,
  getMessageForDevice,
} from '../controllers/chat/message.controller';
import Joi from 'joi';
import rateLimit from 'express-rate-limit';

const router = Router();

// All message routes require authentication
router.use(authenticate);

// Apply general rate limiting to all message endpoints
const messagesLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 300,
  standardHeaders: true,
  legacyHeaders: false,
});
router.use(messagesLimiter);

const messagePayloadSchema = Joi.object({
  deviceId: Joi.string().required().min(1).max(255),
  ciphertext: Joi.string().required().min(1),
  ephemeralPublicKey: Joi.string().optional(),
  nonce: Joi.string().optional(),
  metadata: Joi.object().optional(),
}).options({ stripUnknown: true, abortEarly: false });

const sendMessageSchema = Joi.object({
  payloads: Joi.array().items(messagePayloadSchema).min(1).required(),
  messageType: Joi.string().valid('text', 'image', 'file', 'system').optional(),
  replyTo: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).optional(),
}).options({ stripUnknown: true, abortEarly: false });

const conversationIdParamSchema = Joi.object({
  conversationId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
}).options({ stripUnknown: true, abortEarly: false });

const messageIdParamSchema = Joi.object({
  messageId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
}).options({ stripUnknown: true, abortEarly: false });

const messageDeviceParamSchema = Joi.object({
  messageId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
  deviceId: Joi.string().required().min(1).max(255),
}).options({ stripUnknown: true, abortEarly: false });

const messagesPaginationSchema = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(100).default(50),
}).options({ stripUnknown: true, abortEarly: false });

// Stricter limiter for sending messages
const sendMessageLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 60,
  standardHeaders: true,
  legacyHeaders: false,
});

// Send encrypted message to conversation
router.post(
  '/conversations/:conversationId',
  sendMessageLimiter,
  validateParams(conversationIdParamSchema),
  validateBody(sendMessageSchema),
  sendMessage
);

// Get messages in a conversation
router.get(
  '/conversations/:conversationId',
  validateParams(conversationIdParamSchema),
  validateQuery(messagesPaginationSchema),
  getConversationMessages
);

// Mark message as delivered
router.patch(
  '/:messageId/delivered',
  validateParams(messageIdParamSchema),
  markMessageDelivered
);

// Mark message as read
router.patch(
  '/:messageId/read',
  validateParams(messageIdParamSchema),
  markMessageRead
);

// Get message payload for specific device
router.get(
  '/:messageId/device/:deviceId',
  validateParams(messageDeviceParamSchema),
  getMessageForDevice
);

export default router;
