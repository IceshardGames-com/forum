import { Router } from 'express';
import Joi from 'joi';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import { createCommunity, getCommunityBySlug, joinCommunity, leaveCommunity, listCommunityPosts } from '../controllers/communities/community.controller';

const router = Router();

router.use(authenticate);

const createSchema = Joi.object({
  name: Joi.string().min(3).max(60).required(),
  slug: Joi.string().regex(/^[a-z0-9_]+$/).min(3).max(30).required(),
  description: Joi.string().max(40000).optional(),
  visibility: Joi.string().valid('public','private','restricted').optional(),
  joinMode: Joi.string().valid('open','request','invite').optional(),
});

router.post('/', validateBody(createSchema), createCommunity);

router.get('/slug/:slug', validateParams(Joi.object({ slug: Joi.string().required() })), getCommunityBySlug);

router.post('/:communityId/join', validateParams(Joi.object({ communityId: Joi.string().hex().length(24).required() })), joinCommunity);
router.post('/:communityId/leave', validateParams(Joi.object({ communityId: Joi.string().hex().length(24).required() })), leaveCommunity);

router.get('/:communityId/posts',
  validateParams(Joi.object({ communityId: Joi.string().hex().length(24).required() })),
  validateQuery(Joi.object({ sort: Joi.string().valid('new','top').default('new'), page: Joi.number().min(1).default(1), limit: Joi.number().min(1).max(50).default(20) })),
  listCommunityPosts
);

export default router;