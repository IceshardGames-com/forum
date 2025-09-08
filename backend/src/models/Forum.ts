import mongoose, { Document, Schema } from 'mongoose';

export enum ForumPostPermission {
  ADMIN_ONLY = 'admin_only',
  FOLLOWERS = 'followers',
  MEMBERS = 'members',
}

export interface IForum extends Document {
  _id: string;
  uuid: string;
  name: string;
  slug: string;
  description?: string;
  owner: mongoose.Types.ObjectId;
  verified: boolean;
  postPermission: ForumPostPermission;
  followersCount: number;
  membersCount: number;
  createdAt: Date;
  updatedAt: Date;
  imageId?: string; // Cloudflare Images ID for forum banner/icon
  imageR2Key?: string; // Optional R2 fallback
}

const forumSchema = new Schema<IForum>({
  uuid: {
    type: String,
    required: true,
    unique: true,
    index: true,
  },
  name: {
    type: String,
    required: true,
    trim: true,
    minlength: 3,
    maxlength: 64,
  },
  slug: {
    type: String,
    required: true,
    unique: true,
    lowercase: true,
    trim: true,
    match: [/^[a-z0-9-]+$/, 'Slug can contain lowercase letters, numbers and hyphens'],
  },
  description: {
    type: String,
    maxlength: 512,
  },
  owner: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  verified: {
    type: Boolean,
    default: false,
    index: true,
  },
  postPermission: {
    type: String,
    enum: Object.values(ForumPostPermission),
    default: ForumPostPermission.MEMBERS,
  },
  followersCount: {
    type: Number,
    default: 0,
  },
  membersCount: {
    type: Number,
    default: 0,
  },
  imageId: {
    type: String,
    default: undefined,
  },
  imageR2Key: {
    type: String,
    default: undefined,
  },
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true },
});

forumSchema.index({ slug: 1 });
forumSchema.index({ owner: 1 });

export const Forum = mongoose.model<IForum>('Forum', forumSchema);
export default Forum;

