import { Types } from 'mongoose';
import Post, { IPost } from '../../models/Post';
import Community from '../../models/Community';
import CommunityMember from '../../models/CommunityMember';
import PostVote from '../../models/PostVote';
import { AppError } from '../../middlewares/errorHandler';

export interface CreatePostInput {
  communityId: string;
  title: string;
  type: 'text' | 'link' | 'media';
  body?: string;
  url?: string;
  media?: { url: string; type: 'image'|'video' }[];
}

export class PostService {
  private static instance: PostService;
  private constructor() {}
  public static getInstance(): PostService {
    if (!PostService.instance) PostService.instance = new PostService();
    return PostService.instance;
  }

  public async createPost(authorId: string, input: CreatePostInput): Promise<IPost> {
    const community = await Community.findById(input.communityId);
    if (!community) throw new AppError('Community not found', 404, 'COMMUNITY_NOT_FOUND');

    // Posting permissions
    if (community.visibility === 'private' || community.visibility === 'restricted') {
      const isMember = await CommunityMember.exists({ community: community._id, user: new Types.ObjectId(authorId), status: 'active' });
      if (!isMember) throw new AppError('Not allowed to post in this community', 403, 'POST_NOT_ALLOWED');
    }

    const post = await Post.create({
      community: community._id,
      author: new Types.ObjectId(authorId),
      title: input.title,
      type: input.type,
      body: input.body,
      url: input.url,
      media: input.media,
    });

    await Community.updateOne({ _id: community._id }, { $inc: { postCount: 1 } });
    return post;
  }

  public async vote(postId: string, userId: string, value: 1 | -1 | 0): Promise<IPost> {
    const post = await Post.findById(postId);
    if (!post) throw new AppError('Post not found', 404, 'POST_NOT_FOUND');

    const existing = await PostVote.findOne({ post: post._id, user: new Types.ObjectId(userId) });

    let deltaScore = 0;
    let deltaUp = 0;
    let deltaDown = 0;

    if (value === 0) {
      if (existing) {
        deltaUp = existing.value === 1 ? -1 : 0;
        deltaDown = existing.value === -1 ? -1 : 0;
        deltaScore = existing.value === 1 ? -1 : existing.value === -1 ? 1 : 0;
        await existing.deleteOne();
      }
    } else if (!existing) {
      await PostVote.create({ post: post._id, user: new Types.ObjectId(userId), value });
      if (value === 1) { deltaUp = 1; deltaScore = 1; } else { deltaDown = 1; deltaScore = -1; }
    } else if (existing.value !== value) {
      // Switch vote
      deltaUp = value === 1 ? 1 : (existing.value === 1 ? -1 : 0);
      deltaDown = value === -1 ? 1 : (existing.value === -1 ? -1 : 0);
      deltaScore = value === 1 ? 2 : -2; // from -1->1 or 1->-1
      existing.value = value;
      await existing.save();
    }

    if (deltaScore !== 0 || deltaUp !== 0 || deltaDown !== 0) {
      await Post.updateOne({ _id: post._id }, { $inc: { score: deltaScore, upvotes: deltaUp, downvotes: deltaDown } });
    }

    const updated = await Post.findById(post._id);
    return updated!;
  }
}

export const postService = PostService.getInstance();