import { Types } from 'mongoose';
import Friendship, { IFriendship } from '../models/Friendship';
import { createLogger } from '../utils/logger';
import { AppError } from '../middlewares/errorHandler';
import { notificationTriggers } from './notifications/notificationTriggers.service';
import { User } from '../models/User';

export interface PaginationOptions {
  page?: number;
  limit?: number;
}

const toObjectId = (id: string): Types.ObjectId => new Types.ObjectId(id);

export class FriendshipService {
  private static instance: FriendshipService;
  private constructor() {}
  public static getInstance(): FriendshipService {
    if (!FriendshipService.instance) FriendshipService.instance = new FriendshipService();
    return FriendshipService.instance;
  }

  public async sendRequest(requesterId: string, recipientId: string, requestId?: string): Promise<IFriendship> {
    const logger = createLogger(requestId);
    if (requesterId === recipientId) {
      throw new AppError('Cannot send friend request to yourself', 400, 'INVALID_REQUEST');
    }

    const R = toObjectId(requesterId);
    const T = toObjectId(recipientId);
    const existing = await Friendship.findOne({
      $or: [
        { requester: R, recipient: T },
        { requester: T, recipient: R },
      ],
    });

    if (existing) {
      if (existing.status === 'blocked') {
        throw new AppError('Friendship is blocked', 403, 'FRIENDSHIP_BLOCKED');
      }
      throw new AppError('Friend request already exists or you are already friends', 400, 'FRIENDSHIP_EXISTS');
    }

    const doc = await Friendship.create({ requester: R, recipient: T, status: 'pending' });
    logger.info('Friend request created', { requesterId, recipientId, friendshipId: doc._id });

    // Trigger notification
    try {
      const requesterUser = await User.findById(requesterId, { username: 1 });
      if (requesterUser) {
        await notificationTriggers.onFriendRequestSent(
          requesterId,
          recipientId,
          requesterUser.username,
          requestId
        );
      }
    } catch (notificationError) {
      // Log but don't fail the friend request if notification fails
      logger.warn('Failed to trigger friend request notification', {
        error: notificationError instanceof Error ? notificationError.message : 'Unknown error',
        friendshipId: doc._id,
      });
    }

    return doc;
  }

  public async acceptRequest(requestIdParam: string, userId: string, requestId?: string): Promise<void> {
    const updated = await Friendship.findOneAndUpdate(
      { _id: requestIdParam, recipient: toObjectId(userId), status: 'pending' },
      { status: 'accepted' },
      { new: true }
    ).populate('requester', 'username');
    
    if (!updated) {
      throw new AppError('Request not found or already handled', 404, 'REQUEST_NOT_FOUND');
    }

    // Trigger notification
    try {
      const recipientUser = await User.findById(userId, { username: 1 });
      if (recipientUser && updated.requester) {
        await notificationTriggers.onFriendRequestAccepted(
          userId,
          (updated.requester as any)._id.toString(),
          recipientUser.username,
          requestId
        );
      }
    } catch (notificationError) {
      // Log but don't fail the acceptance if notification fails
      console.warn('Failed to trigger friend acceptance notification:', notificationError);
    }
  }

  public async declineRequest(requestIdParam: string, userId: string, requestId?: string): Promise<void> {
    const updated = await Friendship.findOneAndUpdate(
      { _id: requestIdParam, recipient: toObjectId(userId), status: 'pending' },
      { status: 'declined' },
      { new: true }
    ).populate('requester', 'username');
    
    if (!updated) {
      throw new AppError('Request not found or already handled', 404, 'REQUEST_NOT_FOUND');
    }

    // Trigger notification/update
    try {
      const recipientUser = await User.findById(userId, { username: 1 });
      if (recipientUser && updated.requester) {
        await notificationTriggers.onFriendRequestDeclined(
          userId,
          (updated.requester as any)._id.toString(),
          recipientUser.username,
          requestId
        );
      }
    } catch (notificationError) {
      // Log but don't fail the decline if notification fails
      console.warn('Failed to trigger friend decline notification:', notificationError);
    }
  }

  public async cancelRequest(requestIdParam: string, userId: string): Promise<void> {
    const deleted = await Friendship.findOneAndDelete({ _id: requestIdParam, requester: toObjectId(userId), status: 'pending' });
    if (!deleted) {
      throw new AppError('Friend request not found', 404, 'REQUEST_NOT_FOUND');
    }
  }

  public async blockUser(actorUserId: string, otherUserId: string, requestId?: string): Promise<void> {
    if (actorUserId === otherUserId) {
      throw new AppError('Cannot block yourself', 400, 'INVALID_REQUEST');
    }
    const A = toObjectId(actorUserId);
    const B = toObjectId(otherUserId);
    const existing = await Friendship.findOne({
      $or: [
        { requester: A, recipient: B },
        { requester: B, recipient: A },
      ],
    });
    if (!existing) {
      await Friendship.create({ requester: A, recipient: B, status: 'blocked' });
    } else {
      existing.status = 'blocked';
      existing.requester = A;
      existing.recipient = B;
      await existing.save();
    }

    // Trigger notification/update
    try {
      await notificationTriggers.onUserBlocked(actorUserId, otherUserId, requestId);
    } catch (notificationError) {
      // Log but don't fail the block if notification fails
      console.warn('Failed to trigger user blocked notification:', notificationError);
    }
  }

  public async listFriends(userId: string, { page = 1, limit = 20 }: PaginationOptions) {
    const skip = (page - 1) * limit;
    const U = toObjectId(userId);
    const pipeline = [
      { $match: { status: 'accepted', $or: [{ requester: U }, { recipient: U }] } },
      { $lookup: { from: 'users', localField: 'requester', foreignField: '_id', as: 'requesterData' } },
      { $lookup: { from: 'users', localField: 'recipient', foreignField: '_id', as: 'recipientData' } },
      { $unwind: { path: '$requesterData' } },
      { $unwind: { path: '$recipientData' } },
      { $project: { friend: { $cond: [{ $eq: ['$requester', U] }, '$recipientData', '$requesterData'] } } },
      { $project: { _id: '$friend._id', username: '$friend.username', role: '$friend.role' } },
      { $skip: skip },
      { $limit: limit },
    ];
    const results = (await (Friendship as any).aggregate(pipeline)) as any[];
    return results;
  }

  public async listRequests(userId: string, type: 'incoming' | 'outgoing', { page = 1, limit = 20 }: PaginationOptions) {
    const skip = (page - 1) * limit;
    const match: any = { status: 'pending' };
    if (type === 'incoming') match.recipient = toObjectId(userId);
    else match.requester = toObjectId(userId);

    const otherSideField = type === 'incoming' ? 'requester' : 'recipient';
    const pipeline = [
      { $match: match },
      { $sort: { createdAt: -1 } },
      { $skip: skip },
      { $limit: limit },
      {
        $lookup: {
          from: 'users',
          localField: otherSideField,
          foreignField: '_id',
          as: 'user',
        },
      },
      { $unwind: '$user' },
      {
        $project: {
          _id: 1,
          createdAt: 1,
          user: { _id: '$user._id', username: '$user.username', email: '$user.email' },
        },
      },
    ];
    const results = (await (Friendship as any).aggregate(pipeline)) as any[];
    return results;
  }

  public async mutualFriends(userA: string, userB: string, { page = 1, limit = 20 }: PaginationOptions) {
    const skip = (page - 1) * limit;
    const A = toObjectId(userA);
    const B = toObjectId(userB);
    const pipeline = [
      { $match: { status: 'accepted', $or: [{ requester: A }, { recipient: A }] } },
      { $project: { friend: { $cond: [{ $eq: ['$requester', A] }, '$recipient', '$requester'] } } },
      {
        $lookup: {
          from: 'friendships',
          let: { friendId: '$friend' },
          pipeline: [
            {
              $match: {
                $expr: {
                  $and: [
                    { $eq: ['$status', 'accepted'] },
                    { $or: [{ $eq: ['$requester', B] }, { $eq: ['$recipient', B] }] },
                    { $or: [{ $eq: ['$requester', '$$friendId'] }, { $eq: ['$recipient', '$$friendId'] }] },
                  ],
                },
              },
            },
          ],
          as: 'mutual',
        },
      },
      { $match: { 'mutual.0': { $exists: true } } },
      { $skip: skip },
      { $limit: limit },
      { $lookup: { from: 'users', localField: 'friend', foreignField: '_id', as: 'user' } },
      { $unwind: '$user' },
      { $project: { _id: '$user._id', username: '$user.username', email: '$user.email' } },
    ];
    const results = (await (Friendship as any).aggregate(pipeline)) as any[];
    return results;
  }

  public async suggestions(userId: string, { page = 1, limit = 20 }: PaginationOptions) {
    const skip = (page - 1) * limit;
    const U = toObjectId(userId);
    const pipeline = [
      { $match: { status: 'accepted', $or: [{ requester: U }, { recipient: U }] } },
      { $project: { friend: { $cond: [{ $eq: ['$requester', U] }, '$recipient', '$requester'] } } },
      {
        $lookup: {
          from: 'friendships',
          let: { f1: '$friend' },
          pipeline: [
            { $match: { $expr: { $and: [{ $eq: ['$status', 'accepted'] }, { $ne: ['$$f1', U] }, { $ne: ['$requester', U] }, { $ne: ['$recipient', U] }, { $or: [{ $eq: ['$requester', '$$f1'] }, { $eq: ['$recipient', '$$f1'] }] }] } } },
            { $project: { candidate: { $cond: [{ $eq: ['$requester', '$$f1'] }, '$recipient', '$requester'] } } },
          ],
          as: 'fof',
        },
      },
      { $unwind: '$fof' },
      { $replaceRoot: { newRoot: '$fof' } },
      { $group: { _id: '$candidate', mutualCount: { $sum: 1 } } },
      // Exclude already-friends or pending/declined with U
      {
        $lookup: {
          from: 'friendships',
          let: { candidateId: '$_id' },
          pipeline: [
            { $match: { $expr: { $or: [ { $and: [ { $eq: ['$requester', U] }, { $eq: ['$recipient', '$$candidateId'] } ] }, { $and: [ { $eq: ['$requester', '$$candidateId'] }, { $eq: ['$recipient', U] } ] } ] } } },
          ],
          as: 'existingRel',
        },
      },
      { $match: { existingRel: { $size: 0 } } },
      // augment with shared interests score
      { $lookup: { from: 'users', localField: '_id', foreignField: '_id', as: 'candidateUser' } },
      { $unwind: '$candidateUser' },
      { $addFields: { currentUserId: U } },
      { $lookup: { from: 'users', localField: 'currentUserId', foreignField: '_id', as: 'cu' } },
      { $unwind: '$cu' },
      { $addFields: { sharedInterests: { $size: { $setIntersection: ['$candidateUser.interests', '$cu.interests'] } } } },
      { $addFields: { score: { $add: ['$mutualCount', '$sharedInterests'] } } },
      { $sort: { score: -1 } },
      { $skip: skip },
      { $limit: limit },
      { $project: { _id: 1, username: '$candidateUser.username', mutualCount: 1, sharedInterests: 1, score: 1 } },
    ];
    const results = (await (Friendship as any).aggregate(pipeline)) as any[];
    return results;
  }

  public async removeFriend(userId: string, friendId: string): Promise<void> {
    const deleted = await Friendship.findOneAndDelete({
      $or: [
        { requester: toObjectId(userId), recipient: toObjectId(friendId), status: 'accepted' },
        { requester: toObjectId(friendId), recipient: toObjectId(userId), status: 'accepted' },
      ],
    });
    if (!deleted) {
      throw new AppError('Friendship not found', 404, 'FRIENDSHIP_NOT_FOUND');
    }
  }

  public async areFriends(userId: string, otherUserId: string): Promise<boolean> {
    const U = toObjectId(userId);
    const O = toObjectId(otherUserId);
    
    const friendship = await Friendship.findOne({
      $or: [
        { requester: U, recipient: O, status: 'accepted' },
        { requester: O, recipient: U, status: 'accepted' },
      ],
    });
    
    return !!friendship;
  }
}

export const friendshipService = FriendshipService.getInstance();


