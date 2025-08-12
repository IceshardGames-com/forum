import { Types } from 'mongoose';
import Community, { ICommunity, CommunityJoinMode, CommunityVisibility } from '../../models/Community';
import CommunityMember, { ICommunityMember } from '../../models/CommunityMember';
import Post, { IPost } from '../../models/Post';
import { AppError } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';

export interface CreateCommunityInput {
  name: string;
  slug: string;
  description?: string;
  visibility?: CommunityVisibility;
  joinMode?: CommunityJoinMode;
}

export class CommunityService {
  private static instance: CommunityService;
  private constructor() {}
  public static getInstance(): CommunityService {
    if (!CommunityService.instance) CommunityService.instance = new CommunityService();
    return CommunityService.instance;
  }

  public async createCommunity(ownerId: string, input: CreateCommunityInput, requestId?: string): Promise<ICommunity> {
    const logger = createLogger(requestId);
    const exists = await Community.findOne({ slug: input.slug.toLowerCase() });
    if (exists) throw new AppError('Slug already taken', 400, 'SLUG_TAKEN');

    const community = await Community.create({
      name: input.name,
      slug: input.slug.toLowerCase(),
      description: input.description,
      visibility: input.visibility ?? 'public',
      joinMode: input.joinMode ?? 'open',
      owner: new Types.ObjectId(ownerId),
      moderators: [new Types.ObjectId(ownerId)],
      memberCount: 1,
    });

    await CommunityMember.create({
      community: community._id,
      user: new Types.ObjectId(ownerId),
      role: 'owner',
      status: 'active',
    });

    logger.info('Community created', { communityId: community._id, ownerId });
    return community;
  }

  public async getBySlug(slug: string): Promise<ICommunity | null> {
    return Community.findOne({ slug: slug.toLowerCase() });
  }

  public async isMember(communityId: string, userId: string): Promise<boolean> {
    const cm = await CommunityMember.findOne({ community: new Types.ObjectId(communityId), user: new Types.ObjectId(userId), status: 'active' });
    return !!cm;
  }

  public async join(communityId: string, userId: string): Promise<ICommunityMember> {
    const community = await Community.findById(communityId);
    if (!community) throw new AppError('Community not found', 404, 'COMMUNITY_NOT_FOUND');

    const existing = await CommunityMember.findOne({ community: community._id, user: new Types.ObjectId(userId) });
    if (existing) return existing;

    if (community.joinMode === 'open') {
      const member = await CommunityMember.create({ community: community._id, user: new Types.ObjectId(userId), role: 'member', status: 'active' });
      await Community.updateOne({ _id: community._id }, { $inc: { memberCount: 1 } });
      return member;
    }

    // request or invite
    const status = community.joinMode === 'request' ? 'pending' : 'pending';
    return CommunityMember.create({ community: community._id, user: new Types.ObjectId(userId), role: 'member', status });
  }

  public async leave(communityId: string, userId: string): Promise<void> {
    const membership = await CommunityMember.findOneAndDelete({ community: new Types.ObjectId(communityId), user: new Types.ObjectId(userId) });
    if (membership && membership.status === 'active') {
      await Community.updateOne({ _id: new Types.ObjectId(communityId) }, { $inc: { memberCount: -1 } });
    }
  }

  public async listPosts(communityId: string, sort: 'new' | 'top' = 'new', page = 1, limit = 20): Promise<{ posts: IPost[]; total: number; }> {
    const skip = (page - 1) * limit;
    const sortStage: Record<string, 1 | -1> = sort === 'top' ? { score: -1, createdAt: -1 } : { createdAt: -1 };
    const [posts, total] = await Promise.all([
      Post.find({ community: new Types.ObjectId(communityId), isDeleted: false })
        .sort(sortStage)
        .skip(skip)
        .limit(limit)
        .lean(),
      Post.countDocuments({ community: new Types.ObjectId(communityId), isDeleted: false })
    ]);
    return { posts: posts as IPost[], total };
  }
}

export const communityService = CommunityService.getInstance();