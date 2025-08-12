import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { postService } from '../../services/posts/post.service';
import Post from '../../models/Post';
import Community from '../../models/Community';
import CommunityMember from '../../models/CommunityMember';
import { Types } from 'mongoose';

export const createPost = asyncErrorHandler(async (req: Request, res: Response) => {
  const { communityId, title, type, body, url, media } = req.body;
  const post = await postService.createPost(req.user!._id, { communityId, title, type, body, url, media });
  res.status(201).json({ success: true, data: post });
});

export const getPost = asyncErrorHandler(async (req: Request, res: Response) => {
  const { postId } = req.params as { postId: string };
  const post = await Post.findById(postId).lean();
  if (!post || post.isDeleted) {
    res.status(404).json({ success: false, error: { message: 'Post not found' } });
    return;
  }

  // Privacy: if community is private, require membership
  const community = await Community.findById(post.community);
  if (!community) {
    res.status(404).json({ success: false, error: { message: 'Community not found' } });
    return;
  }
  if (community.visibility === 'private') {
    if (!req.user) {
      res.status(403).json({ success: false, error: { message: 'Access denied' } });
      return;
    }
    const isMember = await CommunityMember.exists({ community: community._id, user: new Types.ObjectId(req.user!._id), status: 'active' });
    if (!isMember) {
      res.status(403).json({ success: false, error: { message: 'Access denied' } });
      return;
    }
  }

  res.json({ success: true, data: post });
});

export const deletePost = asyncErrorHandler(async (req: Request, res: Response) => {
  const { postId } = req.params as { postId: string };
  const post = await Post.findById(postId);
  if (!post) {
    res.status(404).json({ success: false, error: { message: 'Post not found' } });
    return;
  }

  const community = await Community.findById(post.community);
  const isAuthor = post.author.toString() === req.user!._id;
  let isMod = false;
  if (community) {
    isMod = await CommunityMember.exists({ community: community._id, user: new Types.ObjectId(req.user!._id), role: { $in: ['owner','moderator'] }, status: 'active' }) as unknown as boolean;
  }

  if (!isAuthor && !isMod) {
    res.status(403).json({ success: false, error: { message: 'Not allowed' } });
    return;
  }

  post.isDeleted = true;
  await post.save();
  res.json({ success: true, message: 'Post deleted' });
});

export const votePost = asyncErrorHandler(async (req: Request, res: Response) => {
  const { postId } = req.params as { postId: string };
  const { value } = req.body as { value: 1 | -1 | 0 };
  const updated = await postService.vote(postId, req.user!._id, value);
  res.json({ success: true, data: updated });
});