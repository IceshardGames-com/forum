import mongoose, { Document, Schema } from 'mongoose';

export interface IForumPost extends Document {
  _id: string;
  forum: mongoose.Types.ObjectId;
  author: mongoose.Types.ObjectId;
  title: string;
  content: string;
  likes: number;
  dislikes: number;
  shares: number;
  createdAt: Date;
  updatedAt: Date;
  imageIds?: string[]; // Cloudflare Images IDs
  mediaR2Keys?: string[]; // R2 keys for attachments (images/gifs/files)
}

const forumPostSchema = new Schema<IForumPost>({
  forum: {
    type: Schema.Types.ObjectId,
    ref: 'Forum',
    required: true,
    index: true,
  },
  author: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  title: {
    type: String,
    required: true,
    trim: true,
    minlength: 1,
    maxlength: 140,
  },
  content: {
    type: String,
    required: true,
    maxlength: 10000,
  },
  likes: {
    type: Number,
    default: 0,
  },
  dislikes: {
    type: Number,
    default: 0,
  },
  shares: {
    type: Number,
    default: 0,
  },
  imageIds: {
    type: [String],
    default: [],
  },
  mediaR2Keys: {
    type: [String],
    default: [],
  },
}, {
  timestamps: true,
});

forumPostSchema.index({ forum: 1, createdAt: -1 });
forumPostSchema.index({ author: 1, createdAt: -1 });

export const ForumPost = mongoose.model<IForumPost>('ForumPost', forumPostSchema);
export default ForumPost;

