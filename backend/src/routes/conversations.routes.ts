import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import {
  createOrGetConversation,
  getUserConversations,
  getConversation,
  deleteConversation,
  getUnreadCount,
} from '../controllers/chat/conversation.controller';
import Joi from 'joi';

const router = Router();

// All conversation routes require authentication
router.use(authenticate);

const createConversationSchema = Joi.object({
  otherUserId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
}).options({ stripUnknown: true, abortEarly: false });

const conversationIdParamSchema = Joi.object({
  conversationId: Joi.string().pattern(/^[0-9a-fA-F]{24}$/).required(),
}).options({ stripUnknown: true, abortEarly: false });

const paginationQuerySchema = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
}).options({ stripUnknown: true, abortEarly: false });

// Create or get existing conversation
router.post('/', validateBody(createConversationSchema), createOrGetConversation);

// Get user's conversations
router.get('/', validateQuery(paginationQuerySchema), getUserConversations);

// Get unread message count
router.get('/unread-count', getUnreadCount);

// Get specific conversation
router.get('/:conversationId', validateParams(conversationIdParamSchema), getConversation);

// Delete conversation for user
router.delete('/:conversationId', validateParams(conversationIdParamSchema), deleteConversation);

export default router;
