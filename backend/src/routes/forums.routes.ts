import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import rateLimit from 'express-rate-limit';
import {
  createForum,
  getForumBySlug,
  followForum,
  unfollowForum,
  joinForum,
  leaveForum,
  createPost,
  listPosts,
  likePost,
  dislikePost,
  sharePost,
  addComment,
  listComments,
  likeComment,
  dislikeComment,
  changeMemberRole,
  bulkInteractions,
} from '../controllers/forum/forum.controller';
import {
  createForumValidation,
  forumIdParamValidation,
  forumSlugParamValidation,
  createPostValidation,
  paginationValidation,
  postIdParamValidation,
  commentIdParamValidation,
  addCommentValidation,
  changeMemberRoleValidation,
  bulkInteractionsValidation,
} from '../validations/forum.validation';

const router = Router();

router.use(authenticate);

// Forum-specific rate limits
const interactionLimiter = rateLimit({ windowMs: 60 * 1000, max: 60, standardHeaders: true, legacyHeaders: false });
const reactionLimiter = rateLimit({ windowMs: 60 * 1000, max: 120, standardHeaders: true, legacyHeaders: false });

// Forums
router.post('/', validateBody(createForumValidation), createForum);
router.get('/slug/:slug', validateParams(forumSlugParamValidation), getForumBySlug);

// Follow/Join
router.post('/:forumId/follow', validateParams(forumIdParamValidation), followForum);
router.post('/:forumId/unfollow', validateParams(forumIdParamValidation), unfollowForum);
router.post('/:forumId/join', validateParams(forumIdParamValidation), joinForum);
router.post('/:forumId/leave', validateParams(forumIdParamValidation), leaveForum);

// Posts
router.post('/:forumId/posts', validateParams(forumIdParamValidation), validateBody(createPostValidation), interactionLimiter, createPost);
router.get('/:forumId/posts', validateParams(forumIdParamValidation), validateQuery(paginationValidation), interactionLimiter, listPosts);
router.post('/posts/:postId/like', validateParams(postIdParamValidation), reactionLimiter, likePost);
router.post('/posts/:postId/dislike', validateParams(postIdParamValidation), reactionLimiter, dislikePost);
router.post('/posts/:postId/share', validateParams(postIdParamValidation), reactionLimiter, sharePost);

// Comments
router.post('/posts/:postId/comments', validateParams(postIdParamValidation), validateBody(addCommentValidation), interactionLimiter, addComment);
router.get('/posts/:postId/comments', validateParams(postIdParamValidation), validateQuery(paginationValidation), interactionLimiter, listComments);
router.post('/comments/:commentId/like', validateParams(commentIdParamValidation), reactionLimiter, likeComment);
router.post('/comments/:commentId/dislike', validateParams(commentIdParamValidation), reactionLimiter, dislikeComment);

// Moderation
router.post('/:forumId/members/role', validateParams(forumIdParamValidation), validateBody(changeMemberRoleValidation), changeMemberRole);

// Bulk interactions
router.post('/interactions/bulk', validateBody(bulkInteractionsValidation), interactionLimiter, bulkInteractions);

export default router;

