import { v4 as uuidv4 } from 'uuid';
import mongoose from 'mongoose';
import Forum, { IForum, ForumPostPermission } from '../models/Forum';
import ForumMember, { IForumMember, ForumMemberRole } from '../models/ForumMember';
import ForumPost, { IForumPost } from '../models/ForumPost';
import ForumComment, { IForumComment } from '../models/ForumComment';

type Pagination = { page?: number; limit?: number };

const ensureObjectId = (id: string): mongoose.Types.ObjectId => new mongoose.Types.ObjectId(id);

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
    return ForumPost.create({ forum: forumId, author: userId, title: data.title, content: data.content });
  },

  async listPosts(forumId: string, { page = 1, limit = 20 }: Pagination): Promise<IForumPost[]> {
    return ForumPost.find({ forum: forumId })
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(Math.min(limit, 50));
  },

  async likePost(postId: string): Promise<void> {
    // Simple counter; can be expanded to Reaction model in future
    await ForumPost.updateOne({ _id: postId }, { $inc: { likes: 1 } });
  },

  async dislikePost(postId: string): Promise<void> {
    await ForumPost.updateOne({ _id: postId }, { $inc: { dislikes: 1 } });
  },

  async sharePost(postId: string): Promise<void> {
    await ForumPost.updateOne({ _id: postId }, { $inc: { shares: 1 } });
  },

  async addComment(userId: string, postId: string, content: string, parentCommentId?: string | null): Promise<IForumComment> {
    const post = await ForumPost.findById(postId);
    if (!post) throw new Error('Post not found');
    const comment = await ForumComment.create({
      post: postId,
      author: userId,
      content,
      parentComment: parentCommentId ? ensureObjectId(parentCommentId) : null,
    });
    return comment;
  },

  async listComments(postId: string, { page = 1, limit = 50 }: Pagination, parentCommentId?: string | null): Promise<IForumComment[]> {
    const query: any = { post: postId };
    if (parentCommentId === undefined) {
      query.parentComment = null;
    } else {
      query.parentComment = parentCommentId ? ensureObjectId(parentCommentId) : null;
    }
    return ForumComment.find(query)
      .sort({ createdAt: 1 })
      .skip((page - 1) * limit)
      .limit(Math.min(limit, 100));
  },

  async likeComment(commentId: string): Promise<void> {
    await ForumComment.updateOne({ _id: commentId }, { $inc: { likes: 1 } });
  },

  async dislikeComment(commentId: string): Promise<void> {
    await ForumComment.updateOne({ _id: commentId }, { $inc: { dislikes: 1 } });
  },
};

export default forumService;

