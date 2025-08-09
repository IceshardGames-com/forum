import { Request, Response } from 'express';
import { asyncErrorHandler } from '../middlewares/errorHandler';
import { createLogger } from '../utils/logger';
import { friendshipService } from '../services/friendship.service';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const sendFriendRequest = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const { recipientId } = req.body as { recipientId?: string };
  if (!recipientId) {
    res.status(400).json({ success: false, message: 'recipientId is required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }
  const created = await friendshipService.sendRequest(req.user!._id, recipientId, req.id);
  logger.info('Friend request sent', { friendshipId: created._id });
  const response: ApiResponse = {
    success: true,
    data: { request: created },
    message: 'Friend request created',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(201).json(response);
});

export const acceptFriendRequest = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const id = req.params.id as string;
  await friendshipService.acceptRequest(id, req.user!._id);
  const response: ApiResponse = {
    success: true,
    message: 'Friend request accepted',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const declineFriendRequest = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const id = req.params.id as string;
  await friendshipService.declineRequest(id, req.user!._id);
  const response: ApiResponse = {
    success: true,
    message: 'Friend request declined',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const cancelFriendRequest = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const id = req.params.id as string;
  await friendshipService.cancelRequest(id, req.user!._id);
  const response: ApiResponse = {
    success: true,
    message: 'Friend request canceled',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const blockUser = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { userId } = req.body as { userId?: string };
  if (!userId) {
    res.status(400).json({ success: false, message: 'userId is required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }
  await friendshipService.blockUser(req.user!._id, userId);
  const response: ApiResponse = {
    success: true,
    message: 'User blocked',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const listFriends = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 20, 50);
  const data = await friendshipService.listFriends(req.user!._id, { page, limit });
  const response: ApiResponse = {
    success: true,
    data: { friends: data, page, limit },
    message: 'Friends retrieved',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const listRequests = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const type = (req.query.type as 'incoming' | 'outgoing') || 'incoming';
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 20, 50);
  const data = await friendshipService.listRequests(req.user!._id, type, { page, limit });
  const response: ApiResponse = {
    success: true,
    data: { requests: data, type, page, limit },
    message: 'Friend requests retrieved',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const getMutualFriends = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { userId } = req.params as { userId: string };
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 20, 50);
  const data = await friendshipService.mutualFriends(req.user!._id, userId, { page, limit });
  const response: ApiResponse = {
    success: true,
    data: { users: data, page, limit },
    message: 'Mutual friends retrieved',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const getSuggestions = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const page = Number(req.query.page) || 1;
  const limit = Math.min(Number(req.query.limit) || 20, 50);
  const data = await friendshipService.suggestions(req.user!._id, { page, limit });
  const response: ApiResponse = {
    success: true,
    data: { users: data, page, limit },
    message: 'Friend suggestions retrieved',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});

export const removeFriend = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { userId } = req.body as { userId?: string };
  if (!userId) {
    res.status(400).json({ success: false, message: 'userId is required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }
  await friendshipService.removeFriend(req.user!._id, userId);
  const response: ApiResponse = {
    success: true,
    message: 'Friend removed',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };
  res.status(200).json(response);
});


