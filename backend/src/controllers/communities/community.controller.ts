import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { communityService } from '../../services/communities/community.service';

export const createCommunity = asyncErrorHandler(async (req: Request, res: Response) => {
  const { name, slug, description, visibility, joinMode } = req.body;
  const community = await communityService.createCommunity(req.user!._id, { name, slug, description, visibility, joinMode }, req.id);
  res.status(201).json({ success: true, data: community });
});

export const getCommunityBySlug = asyncErrorHandler(async (req: Request, res: Response) => {
  const { slug } = req.params as { slug: string };
  const community = await communityService.getBySlug(slug);
  if (!community) {
    res.status(404).json({ success: false, error: { message: 'Community not found' } });
    return;
  }
  res.json({ success: true, data: community });
});

export const joinCommunity = asyncErrorHandler(async (req: Request, res: Response) => {
  const { communityId } = req.params as { communityId: string };
  const member = await communityService.join(communityId, req.user!._id);
  res.status(200).json({ success: true, data: member });
});

export const leaveCommunity = asyncErrorHandler(async (req: Request, res: Response) => {
  const { communityId } = req.params as { communityId: string };
  await communityService.leave(communityId, req.user!._id);
  res.status(200).json({ success: true, message: 'Left community' });
});

export const listCommunityPosts = asyncErrorHandler(async (req: Request, res: Response) => {
  const { communityId } = req.params as { communityId: string };
  const sort = (req.query.sort as 'new'|'top') || 'new';
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 50);
  const data = await communityService.listPosts(communityId, sort, page, limit);
  res.json({ success: true, data });
});