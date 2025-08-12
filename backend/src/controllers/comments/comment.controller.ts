import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { commentService } from '../../services/comments/comment.service';

export const createComment = asyncErrorHandler(async (req: Request, res: Response) => {
  const { postId } = req.params as { postId: string };
  const { body, parentCommentId } = req.body as { body: string; parentCommentId?: string };
  const input = parentCommentId ? { postId, body, parentCommentId } : { postId, body };
  const comment = await commentService.createComment(req.user!._id, input as any);
  res.status(201).json({ success: true, data: comment });
});

export const listComments = asyncErrorHandler(async (req: Request, res: Response) => {
  const { postId } = req.params as { postId: string };
  const sort = (req.query.sort as 'new'|'top'|'old') || 'new';
  const parentId = (req.query.parentId as string | undefined) || undefined;
  const after = (req.query.after as string | undefined) || undefined;
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 100);
  const comments = await commentService.listComments(postId, sort, parentId, after, limit);
  res.json({ success: true, data: comments });
});

export const voteComment = asyncErrorHandler(async (req: Request, res: Response) => {
  const { commentId } = req.params as { commentId: string };
  const { value } = req.body as { value: 1 | -1 | 0 };
  const updated = await commentService.vote(commentId, req.user!._id, value);
  res.json({ success: true, data: updated });
});