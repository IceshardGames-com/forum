import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
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
} from '../validations/forum.validation';

const router = Router();

router.use(authenticate);

// Forums
router.post('/', validateBody(createForumValidation), createForum);
router.get('/slug/:slug', validateParams(forumSlugParamValidation), getForumBySlug);

// Follow/Join
router.post('/:forumId/follow', validateParams(forumIdParamValidation), followForum);
router.post('/:forumId/unfollow', validateParams(forumIdParamValidation), unfollowForum);
router.post('/:forumId/join', validateParams(forumIdParamValidation), joinForum);
router.post('/:forumId/leave', validateParams(forumIdParamValidation), leaveForum);

// Posts
router.post('/:forumId/posts', validateParams(forumIdParamValidation), validateBody(createPostValidation), createPost);
router.get('/:forumId/posts', validateParams(forumIdParamValidation), validateQuery(paginationValidation), listPosts);
router.post('/posts/:postId/like', validateParams(postIdParamValidation), likePost);
router.post('/posts/:postId/dislike', validateParams(postIdParamValidation), dislikePost);
router.post('/posts/:postId/share', validateParams(postIdParamValidation), sharePost);

// Comments
router.post('/posts/:postId/comments', validateParams(postIdParamValidation), validateBody(addCommentValidation), addComment);
router.get('/posts/:postId/comments', validateParams(postIdParamValidation), validateQuery(paginationValidation), listComments);
router.post('/comments/:commentId/like', validateParams(commentIdParamValidation), likeComment);
router.post('/comments/:commentId/dislike', validateParams(commentIdParamValidation), dislikeComment);

export default router;

