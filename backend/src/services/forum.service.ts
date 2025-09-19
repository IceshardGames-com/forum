import { v4 as uuidv4 } from 'uuid';
import mongoose from 'mongoose';
import Forum, { IForum, ForumPostPermission } from '../models/Forum';
import ForumMember, { IForumMember, ForumMemberRole } from '../models/ForumMember';
import ForumPost, { IForumPost } from '../models/ForumPost';
import ForumComment, { IForumComment } from '../models/ForumComment';
import ForumPostReaction from '../models/ForumPostReaction';
import ForumCommentReaction from '../models/ForumCommentReaction';
import ReactionEvent from '../models/ReactionEvent';
import sanitizeHtml from 'sanitize-html';

type Pagination = { page?: number; limit?: number };

const ensureObjectId = (id: string): mongoose.Types.ObjectId => {
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new Error(`Invalid ObjectId string: ${id}`);
  }
  return new mongoose.Types.ObjectId(id);
};

const canUserPost = async (forum: IForum, userId: string): Promise<boolean> => {
  if (!userId) return false;
  if (forum.owner.toString() === userId) return true;
  const member = await ForumMember.findOne({ forum: forum._id, user: userId });
  if (!member) return forum.postPermission === ForumPostPermission.FOLLOWERS ? false : false;
  if (forum.postPermission === ForumPostPermission.ADMIN_ONLY) {
    return member.role === ForumMemberRole.ADMIN || member.role === ForumMemberRole.OWNER;
  }
  if (forum.postPermission === ForumPostPermission.FOLLOWERS) {
    return member.isFollower === true;
  }
  return true; // MEMBERS
};

const canUserComment = async (forum: IForum, userId: string): Promise<boolean> => {
  // For now mirror post permissions. Adjust if separate comment policy is desired.
  return canUserPost(forum, userId);
};

const sanitizeText = (text: string): string => {
  // Strip HTML to plain text. Adjust allowedTags/attributes if you want basic formatting.
  return sanitizeHtml(text, { allowedTags: [], allowedAttributes: {} }).trim();
};

export const forumService = {
  async createForum(ownerId: string, data: { name: string; slug: string; description?: string; verified?: boolean; postPermission?: ForumPostPermission }): Promise<IForum> {
    const forum = await Forum.create({
      uuid: uuidv4(),
      name: data.name,
      slug: data.slug,
      description: data.description,
      owner: ensureObjectId(ownerId),
      verified: Boolean(data.verified) || false,
      postPermission: data.postPermission || ForumPostPermission.MEMBERS,
    });

    await ForumMember.create({ forum: forum._id, user: ownerId, role: ForumMemberRole.OWNER, isFollower: true });
    await Forum.updateOne({ _id: forum._id }, { $inc: { membersCount: 1, followersCount: 1 } });

    return forum;
  },

  async getForumBySlug(slug: string): Promise<IForum | null> {
    return Forum.findOne({ slug });
  },

  async followForum(userId: string, forumId: string): Promise<IForumMember> {
    const existing = await ForumMember.findOne({ forum: forumId, user: userId });
    if (existing) {
      if (!existing.isFollower) {
        existing.isFollower = true;
        await existing.save();
        await Forum.updateOne({ _id: forumId }, { $inc: { followersCount: 1 } });
      }
      return existing;
    }
    const member = await ForumMember.create({ forum: forumId, user: userId, role: ForumMemberRole.MEMBER, isFollower: true });
    await Forum.updateOne({ _id: forumId }, { $inc: { followersCount: 1, membersCount: 1 } });
    return member;
  },

  async unfollowForum(userId: string, forumId: string): Promise<void> {
    const existing = await ForumMember.findOne({ forum: forumId, user: userId });
    if (!existing) return;
    if (existing.isFollower) {
      existing.isFollower = false;
      await existing.save();
      await Forum.updateOne({ _id: forumId }, { $inc: { followersCount: -1 } });
    }
  },

  async joinForum(userId: string, forumId: string): Promise<IForumMember> {
    const existing = await ForumMember.findOne({ forum: forumId, user: userId });
    if (existing) return existing;
    const member = await ForumMember.create({ forum: forumId, user: userId, role: ForumMemberRole.MEMBER, isFollower: true });
    await Forum.updateOne({ _id: forumId }, { $inc: { membersCount: 1, followersCount: 1 } });
    return member;
  },

  async leaveForum(userId: string, forumId: string): Promise<void> {
    const res = await ForumMember.findOneAndDelete({ forum: forumId, user: userId });
    if (res) {
      await Forum.updateOne({ _id: forumId }, { $inc: { membersCount: -1, followersCount: res.isFollower ? -1 : 0 } });
    }
  },

  async createPost(userId: string, forumId: string, data: { title: string; content: string }): Promise<IForumPost> {
    const forum = await Forum.findById(forumId);
    if (!forum) throw new Error('Forum not found');
    const allowed = await canUserPost(forum, userId);
    if (!allowed) throw new Error('Not allowed to post in this forum');
    const title = sanitizeText(data.title);
    const content = sanitizeText(data.content);
    const created = await ForumPost.create({ forum: forumId, author: userId, title, content });
    const populated = await ForumPost.findById(created._id)
      .populate({ path: 'author', select: 'username displayName avatar' });
    return populated as unknown as IForumPost;
  },

  async listPosts(forumId: string, { page = 1, limit = 20 }: Pagination): Promise<IForumPost[]> {
    return ForumPost.find({ forum: forumId })
      .populate({ path: 'author', select: 'username displayName avatar' })
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(Math.min(limit, 50));
  },

  async togglePostReaction(userId: string, postId: string, type: 'like' | 'dislike'): Promise<void> {
    // Permission: must be at least a follower/member according to forum policy
    const post = await ForumPost.findById(postId);
    if (!post) throw new Error('Post not found');
    const forum = await Forum.findById(post.forum);
    if (!forum) throw new Error('Forum not found');
    const allowed = await canUserComment(forum, userId);
    if (!allowed) throw new Error('Not allowed to react in this forum');

    const session = await mongoose.startSession();
    session.startTransaction();
    try {
      const existing = await ForumPostReaction.findOne({ post: postId, user: userId }).session(session);

      let incLikes = 0;
      let incDislikes = 0;

      if (!existing) {
        // none -> like/dislike
        if (type === 'like') incLikes = 1; else incDislikes = 1;
        await ForumPostReaction.create([{ post: postId, user: userId, type }], { session });
        await ReactionEvent.create([{ targetType: 'post', targetId: postId as any, user: userId as any, op: type === 'like' ? 'set_like' : 'set_dislike' }], { session });
      } else if (existing.type === type) {
        // like -> like (toggle off) OR dislike -> dislike (toggle off)
        if (type === 'like') incLikes = -1; else incDislikes = -1;
        await existing.deleteOne({ session });
        await ReactionEvent.create([{ targetType: 'post', targetId: postId as any, user: userId as any, op: 'unset' }], { session });
      } else {
        // switch like <-> dislike
        if (type === 'like') { incLikes = 1; incDislikes = -1; } else { incLikes = -1; incDislikes = 1; }
        existing.type = type;
        await existing.save({ session });
        await ReactionEvent.create([{ targetType: 'post', targetId: postId as any, user: userId as any, op: type === 'like' ? 'set_like' : 'set_dislike' }], { session });
      }

      const update: any = {};
      if (incLikes) update.likes = incLikes;
      if (incDislikes) update.dislikes = (update.dislikes || 0) + incDislikes;
      if (Object.keys(update).length) {
        await ForumPost.updateOne({ _id: postId }, { $inc: update }, { session });
      }

      await session.commitTransaction();
    } finally {
      session.endSession();
    }
  },

  async sharePost(postId: string): Promise<void> {
    await ForumPost.updateOne({ _id: postId }, { $inc: { shares: 1 } });
  },

  async addComment(userId: string, postId: string, content: string, parentCommentId?: string | null): Promise<IForumComment> {
    const post = await ForumPost.findById(postId);
    if (!post) throw new Error('Post not found');
    const forum = await Forum.findById(post.forum);
    if (!forum) throw new Error('Forum not found');
    const allowed = await canUserComment(forum, userId);
    if (!allowed) throw new Error('Not allowed to comment in this forum');
    // Validate parent comment, if provided
    let parentRef: mongoose.Types.ObjectId | null = null;
    if (parentCommentId) {
      const parent = await ForumComment.findById(parentCommentId);
      if (!parent) throw new Error('Parent comment not found');
      if (parent.post.toString() !== postId.toString()) throw new Error('Parent comment does not belong to this post');
      parentRef = ensureObjectId(parent._id as any);
    }

    const sanitized = sanitizeText(content);
    const comment = await ForumComment.create({
      post: postId,
      author: userId,
      content: sanitized,
      parentComment: parentRef,
    });
    // Return with author populated for consistency with listComments
    const populated = await ForumComment.findById(comment._id)
      .populate({ path: 'author', select: 'username displayName avatar' });
    return populated as unknown as IForumComment;
  },

  async listComments(postId: string, { page = 1, limit = 50 }: Pagination, parentCommentId?: string | null): Promise<IForumComment[]> {
    const query: any = { post: postId };
    if (parentCommentId === undefined) {
      query.parentComment = null;
    } else {
      query.parentComment = parentCommentId ? ensureObjectId(parentCommentId) : null;
    }
    return ForumComment.find(query)
      .populate({ path: 'author', select: 'username displayName avatar' })
      .sort({ createdAt: 1 })
      .skip((page - 1) * limit)
      .limit(Math.min(limit, 100));
  },

  async toggleCommentReaction(userId: string, commentId: string, type: 'like' | 'dislike'): Promise<void> {
    // Permission: same as comment/post within the forum
    const comment = await ForumComment.findById(commentId);
    if (!comment) throw new Error('Comment not found');
    const post = await ForumPost.findById(comment.post);
    if (!post) throw new Error('Post not found');
    const forum = await Forum.findById(post.forum);
    if (!forum) throw new Error('Forum not found');
    const allowed = await canUserComment(forum, userId);
    if (!allowed) throw new Error('Not allowed to react in this forum');

    const session = await mongoose.startSession();
    session.startTransaction();
    try {
      const existing = await ForumCommentReaction.findOne({ comment: commentId, user: userId }).session(session);

      let incLikes = 0;
      let incDislikes = 0;

      if (!existing) {
        if (type === 'like') incLikes = 1; else incDislikes = 1;
        await ForumCommentReaction.create([{ comment: commentId, user: userId, type }], { session });
        await ReactionEvent.create([{ targetType: 'comment', targetId: commentId as any, user: userId as any, op: type === 'like' ? 'set_like' : 'set_dislike' }], { session });
      } else if (existing.type === type) {
        if (type === 'like') incLikes = -1; else incDislikes = -1;
        await existing.deleteOne({ session });
        await ReactionEvent.create([{ targetType: 'comment', targetId: commentId as any, user: userId as any, op: 'unset' }], { session });
      } else {
        if (type === 'like') { incLikes = 1; incDislikes = -1; } else { incLikes = -1; incDislikes = 1; }
        existing.type = type;
        await existing.save({ session });
        await ReactionEvent.create([{ targetType: 'comment', targetId: commentId as any, user: userId as any, op: type === 'like' ? 'set_like' : 'set_dislike' }], { session });
      }

      const update: any = {};
      if (incLikes) update.likes = incLikes;
      if (incDislikes) update.dislikes = (update.dislikes || 0) + incDislikes;
      if (Object.keys(update).length) {
        await ForumComment.updateOne({ _id: commentId }, { $inc: update }, { session });
      }

      await session.commitTransaction();
    } finally {
      session.endSession();
    }
  },

  async changeMemberRole(requesterId: string, forumId: string, targetUserId: string, role: ForumMemberRole): Promise<IForumMember> {
    const forum = await Forum.findById(forumId);
    if (!forum) throw new Error('Forum not found');
    if (forum.owner.toString() !== requesterId) throw new Error('Only owner can manage roles');
    if (role === ForumMemberRole.OWNER) throw new Error('Transferring ownership not supported');
    const member = await ForumMember.findOneAndUpdate(
      { forum: forumId, user: targetUserId },
      { $set: { role } },
      { new: true, upsert: false }
    );
    if (!member) throw new Error('Member not found');
    return member;
  },
};

export default forumService;

