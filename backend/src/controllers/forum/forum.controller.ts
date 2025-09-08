import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import forumService from '../../services/forum.service';
import { ForumPostPermission } from '../../models/Forum';
import { ForumMemberRole } from '../../models/ForumMember';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const createForum = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { name, slug, description, verified, postPermission, imageId, imageR2Key } = req.body as { name: string; slug: string; description?: string; verified?: boolean; postPermission?: ForumPostPermission; imageId?: string; imageR2Key?: string };
  const payload: { name: string; slug: string; description?: string; verified?: boolean; postPermission?: ForumPostPermission; imageId?: string; imageR2Key?: string } = { name, slug };
  if (description !== undefined) payload.description = description;
  if (verified !== undefined) payload.verified = verified;
  if (postPermission !== undefined) payload.postPermission = postPermission;
  if (imageId !== undefined) payload.imageId = imageId;
  if (imageR2Key !== undefined) payload.imageR2Key = imageR2Key;
  const forum = await forumService.createForum(req.user!._id, payload);
  const response: ApiResponse = { success: true, data: { forum }, message: 'Forum created', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(201).json(response);
});

export const getForumBySlug = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { slug } = req.params as { slug: string };
  const forum = await forumService.getForumBySlug(slug);
  if (!forum) { res.status(404).json({ success: false, message: 'Forum not found', requestId: req.id, timestamp: new Date().toISOString() }); return; }
  const response: ApiResponse = { success: true, data: { forum }, message: 'Forum retrieved', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const followForum = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  const member = await forumService.followForum(req.user!._id, forumId);
  const response: ApiResponse = { success: true, data: { member }, message: 'Followed forum', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const unfollowForum = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  await forumService.unfollowForum(req.user!._id, forumId);
  const response: ApiResponse = { success: true, message: 'Unfollowed forum', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const joinForum = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  const member = await forumService.joinForum(req.user!._id, forumId);
  const response: ApiResponse = { success: true, data: { member }, message: 'Joined forum', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const leaveForum = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  await forumService.leaveForum(req.user!._id, forumId);
  const response: ApiResponse = { success: true, message: 'Left forum', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const createPost = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  const { title, content, imageIds, mediaR2Keys } = req.body as { title: string; content: string; imageIds?: string[]; mediaR2Keys?: string[] };
  const payload: { title: string; content: string; imageIds?: string[]; mediaR2Keys?: string[] } = { title, content };
  if (Array.isArray(imageIds)) payload.imageIds = imageIds;
  if (Array.isArray(mediaR2Keys)) payload.mediaR2Keys = mediaR2Keys;
  const post = await forumService.createPost(req.user!._id, forumId, payload);
  const response: ApiResponse = { success: true, data: { post }, message: 'Post created', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(201).json(response);
});

export const listPosts = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 20, 50);
  const posts = await forumService.listPosts(forumId, { page, limit });
  const response: ApiResponse = { success: true, data: { posts, page, limit }, message: 'Posts retrieved', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const likePost = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { postId } = req.params as { postId: string };
  await forumService.togglePostReaction(req.user!._id, postId, 'like');
  const response: ApiResponse = { success: true, message: 'Post like toggled', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const dislikePost = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { postId } = req.params as { postId: string };
  await forumService.togglePostReaction(req.user!._id, postId, 'dislike');
  const response: ApiResponse = { success: true, message: 'Post dislike toggled', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const sharePost = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { postId } = req.params as { postId: string };
  await forumService.sharePost(postId);
  const response: ApiResponse = { success: true, message: 'Post shared', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

type BulkInteractionOp =
  | { op: 'post_reaction'; postId: string; type: 'like' | 'dislike' }
  | { op: 'comment_reaction'; commentId: string; type: 'like' | 'dislike' }
  | { op: 'post_share'; postId: string };

export const bulkInteractions = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const ops = (req.body?.operations || []) as BulkInteractionOp[];
  if (!Array.isArray(ops) || ops.length === 0) {
    res.status(400).json({ success: false, message: 'operations array required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }
  const userId = req.user!._id;
  for (const op of ops) {
    if (op.op === 'post_reaction') {
      await forumService.togglePostReaction(userId, op.postId, op.type);
    } else if (op.op === 'comment_reaction') {
      await forumService.toggleCommentReaction(userId, op.commentId, op.type);
    } else if (op.op === 'post_share') {
      await forumService.sharePost(op.postId);
    }
  }
  const response: ApiResponse = { success: true, message: 'Bulk interactions processed', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const addComment = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { postId } = req.params as { postId: string };
  const { content, parentCommentId } = req.body as { content: string; parentCommentId?: string | null };
  const comment = await forumService.addComment(req.user!._id, postId, content, parentCommentId);
  const response: ApiResponse = { success: true, data: { comment }, message: 'Comment added', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(201).json(response);
});

export const listComments = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { postId } = req.params as { postId: string };
  const parentCommentId = (req.query.parentCommentId as string | undefined) ?? undefined;
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 50, 100);
  const comments = await forumService.listComments(postId, { page, limit }, parentCommentId ?? undefined);
  const response: ApiResponse = { success: true, data: { comments, page, limit }, message: 'Comments retrieved', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const likeComment = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { commentId } = req.params as { commentId: string };
  await forumService.toggleCommentReaction(req.user!._id, commentId, 'like');
  const response: ApiResponse = { success: true, message: 'Comment like toggled', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const dislikeComment = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { commentId } = req.params as { commentId: string };
  await forumService.toggleCommentReaction(req.user!._id, commentId, 'dislike');
  const response: ApiResponse = { success: true, message: 'Comment dislike toggled', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

export const changeMemberRole = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { forumId } = req.params as { forumId: string };
  const { userId, role } = req.body as { userId: string; role: ForumMemberRole };
  const member = await forumService.changeMemberRole(req.user!._id, forumId, userId, role);
  const response: ApiResponse = { success: true, data: { member }, message: 'Member role updated', requestId: req.id, timestamp: new Date().toISOString() };
  res.status(200).json(response);
});

