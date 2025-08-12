import { Router } from 'express';
import Joi from 'joi';
import { authenticate, optionalAuthenticate } from '../middlewares/auth';
import { validateBody, validateParams } from '../middlewares/validate';
import { createPost, getPost, deletePost, votePost } from '../controllers/posts/post.controller';

const router = Router();

router.post('/', authenticate, validateBody(Joi.object({
  communityId: Joi.string().hex().length(24).required(),
  title: Joi.string().max(300).required(),
  type: Joi.string().valid('text','link','media').required(),
  body: Joi.string().allow('').optional(),
  url: Joi.string().uri().optional(),
  media: Joi.array().items(Joi.object({ url: Joi.string().uri().required(), type: Joi.string().valid('image','video').required() })).optional()
})), createPost);

router.get('/:postId', optionalAuthenticate, validateParams(Joi.object({ postId: Joi.string().hex().length(24).required() })), getPost);

router.delete('/:postId', authenticate, validateParams(Joi.object({ postId: Joi.string().hex().length(24).required() })), deletePost);

router.post('/:postId/vote', authenticate, validateParams(Joi.object({ postId: Joi.string().hex().length(24).required() })), validateBody(Joi.object({ value: Joi.number().valid(1,-1,0).required() })), votePost);

export default router;