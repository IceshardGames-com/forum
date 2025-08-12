import { Router } from 'express';
import Joi from 'joi';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import { createComment, listComments, voteComment } from '../controllers/comments/comment.controller';

const router = Router();

router.post('/posts/:postId/comments', authenticate,
  validateParams(Joi.object({ postId: Joi.string().hex().length(24).required() })),
  validateBody(Joi.object({ body: Joi.string().min(1).max(40000).required(), parentCommentId: Joi.string().hex().length(24).optional() })),
  createComment
);

router.get('/posts/:postId/comments',
  validateParams(Joi.object({ postId: Joi.string().hex().length(24).required() })),
  validateQuery(Joi.object({ sort: Joi.string().valid('new','top','old').default('new'), parentId: Joi.string().hex().length(24).optional(), after: Joi.string().optional(), limit: Joi.number().min(1).max(100).default(20) })),
  listComments
);

router.post('/comments/:commentId/vote', authenticate,
  validateParams(Joi.object({ commentId: Joi.string().hex().length(24).required() })),
  validateBody(Joi.object({ value: Joi.number().valid(1,-1,0).required() })),
  voteComment
);

export default router;