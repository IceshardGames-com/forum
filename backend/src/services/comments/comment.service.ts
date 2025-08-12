import { Types } from 'mongoose';
import Comment, { IComment } from '../../models/Comment';
import Post from '../../models/Post';
import Community from '../../models/Community';
import CommentVote from '../../models/CommentVote';
import { AppError } from '../../middlewares/errorHandler';

export interface CreateCommentInput {
  postId: string;
  body: string;
  parentCommentId?: string;
}

export class CommentService {
  private static instance: CommentService;
  private constructor() {}
  public static getInstance(): CommentService {
    if (!CommentService.instance) CommentService.instance = new CommentService();
    return CommentService.instance;
  }

  public async createComment(authorId: string, input: CreateCommentInput): Promise<IComment> {
    const post = await Post.findById(input.postId);
    if (!post || post.isDeleted) throw new AppError('Post not found', 404, 'POST_NOT_FOUND');
    const community = await Community.findById(post.community);
    if (!community) throw new AppError('Community not found', 404, 'COMMUNITY_NOT_FOUND');

    let ancestors: Types.ObjectId[] = [];
    let depth = 0;
    let parentComment: Types.ObjectId | undefined = undefined;

    if (input.parentCommentId) {
      const parent = await Comment.findById(input.parentCommentId);
      if (!parent) throw new AppError('Parent comment not found', 404, 'COMMENT_NOT_FOUND');
      ancestors = [...(parent.ancestors as any), parent._id as Types.ObjectId];
      depth = (parent.depth || 0) + 1;
      parentComment = parent._id as Types.ObjectId;
    }

    const comment = await Comment.create({
      post: post._id,
      community: post.community,
      author: new Types.ObjectId(authorId),
      parentComment: parentComment || null,
      ancestors,
      depth,
      body: input.body,
    });

    await Post.updateOne({ _id: post._id }, { $inc: { commentCount: 1 } });
    return comment;
  }

  public async listComments(postId: string, sort: 'new'|'top'|'old' = 'new', parentId?: string, after?: string, limit = 20): Promise<IComment[]> {
    const query: any = { post: new Types.ObjectId(postId) };
    if (parentId) {
      query.ancestors = new Types.ObjectId(parentId);
    }

    const sortStage: Record<string, 1 | -1> = sort === 'top' ? { score: -1, createdAt: -1 } : sort === 'old' ? { createdAt: 1 } : { createdAt: -1 };
    if (after) {
      query.createdAt = { $lt: new Date(after) };
    }

    const comments = await Comment.find(query)
      .sort(sortStage)
      .limit(limit)
      .lean();

    return comments as IComment[];
  }

  public async vote(commentId: string, userId: string, value: 1 | -1 | 0): Promise<IComment> {
    const comment = await Comment.findById(commentId);
    if (!comment) throw new AppError('Comment not found', 404, 'COMMENT_NOT_FOUND');

    const existing = await CommentVote.findOne({ comment: comment._id, user: new Types.ObjectId(userId) });

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
      await CommentVote.create({ comment: comment._id, user: new Types.ObjectId(userId), value });
      if (value === 1) { deltaUp = 1; deltaScore = 1; } else { deltaDown = 1; deltaScore = -1; }
    } else if (existing.value !== value) {
      deltaUp = value === 1 ? 1 : (existing.value === 1 ? -1 : 0);
      deltaDown = value === -1 ? 1 : (existing.value === -1 ? -1 : 0);
      deltaScore = value === 1 ? 2 : -2;
      existing.value = value;
      await existing.save();
    }

    if (deltaScore !== 0 || deltaUp !== 0 || deltaDown !== 0) {
      await Comment.updateOne({ _id: comment._id }, { $inc: { score: deltaScore, upvotes: deltaUp, downvotes: deltaDown } });
    }

    const updated = await Comment.findById(comment._id);
    return updated!;
  }
}

export const commentService = CommentService.getInstance();