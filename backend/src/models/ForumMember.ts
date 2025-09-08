import mongoose, { Document, Schema } from 'mongoose';

export enum ForumMemberRole {
  OWNER = 'owner',
  ADMIN = 'admin',
  MODERATOR = 'moderator',
  MEMBER = 'member',
}

export interface IForumMember extends Document {
  _id: string;
  forum: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  role: ForumMemberRole;
  isFollower: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const forumMemberSchema = new Schema<IForumMember>({
  forum: {
    type: Schema.Types.ObjectId,
    ref: 'Forum',
    required: true,
    index: true,
  },
  user: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  role: {
    type: String,
    enum: Object.values(ForumMemberRole),
    default: ForumMemberRole.MEMBER,
    index: true,
  },
  isFollower: {
    type: Boolean,
    default: true,
    index: true,
  },
}, {
  timestamps: true,
});

forumMemberSchema.index({ forum: 1, user: 1 }, { unique: true });

export const ForumMember = mongoose.model<IForumMember>('ForumMember', forumMemberSchema);
export default ForumMember;

