import Joi from 'joi';
import { ForumPostPermission } from '../models/Forum';

export const createForumValidation = Joi.object({
  name: Joi.string().min(3).max(64).required(),
  slug: Joi.string().pattern(/^[a-z0-9-]+$/).min(3).max(64).required(),
  description: Joi.string().max(512).optional().allow('', null),
  verified: Joi.boolean().optional(),
  postPermission: Joi.string().valid(...Object.values(ForumPostPermission)).optional(),
});

export const forumIdParamValidation = Joi.object({
  forumId: Joi.string().hex().length(24).required(),
});

export const forumSlugParamValidation = Joi.object({
  slug: Joi.string().pattern(/^[a-z0-9-]+$/).required(),
});

export const createPostValidation = Joi.object({
  title: Joi.string().min(1).max(140).required(),
  content: Joi.string().min(1).max(10000).required(),
});

export const paginationValidation = Joi.object({
  page: Joi.number().integer().min(1).optional(),
  limit: Joi.number().integer().min(1).max(100).optional(),
});

// Query validation for listing comments: supports optional parentCommentId
export const listCommentsQueryValidation = Joi.object({
  page: Joi.number().integer().min(1).optional(),
  limit: Joi.number().integer().min(1).max(100).optional(),
  parentCommentId: Joi.alternatives().try(
    Joi.string().hex().length(24),
    Joi.valid('null') // allow explicit 'null' to mean top-level
  ).optional(),
});

export const postIdParamValidation = Joi.object({
  postId: Joi.string().hex().length(24).required(),
});

export const commentIdParamValidation = Joi.object({
  commentId: Joi.string().hex().length(24).required(),
});

export const addCommentValidation = Joi.object({
  content: Joi.string().min(1).max(5000).required(),
  parentCommentId: Joi.string().hex().length(24).optional(),
});

export const changeMemberRoleValidation = Joi.object({
  userId: Joi.string().hex().length(24).required(),
  role: Joi.string().valid('admin', 'moderator', 'member').required(),
});

export const bulkInteractionsValidation = Joi.object({
  operations: Joi.array().min(1).items(
    Joi.alternatives().try(
      Joi.object({
        op: Joi.string().valid('post_reaction').required(),
        postId: Joi.string().hex().length(24).required(),
        type: Joi.string().valid('like', 'dislike').required(),
      }),
      Joi.object({
        op: Joi.string().valid('comment_reaction').required(),
        commentId: Joi.string().hex().length(24).required(),
        type: Joi.string().valid('like', 'dislike').required(),
      }),
      Joi.object({
        op: Joi.string().valid('post_share').required(),
        postId: Joi.string().hex().length(24).required(),
      }),
    )
  ).required(),
});

