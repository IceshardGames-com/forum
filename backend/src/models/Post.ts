import mongoose, { Schema, Document } from 'mongoose';

export type PostType = 'text' | 'link' | 'media';

export interface IPost extends Document {
  community: mongoose.Types.ObjectId;
  author: mongoose.Types.ObjectId;
  title: string;
  type: PostType;
  body?: string;
  url?: string;
  media?: { url: string; type: 'image'|'video' }[];
  flairId?: mongoose.Types.ObjectId;
  score: number;
  upvotes: number;
  downvotes: number;
  commentCount: number;
  isDeleted: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const PostSchema = new Schema<IPost>({
  community: { type: Schema.Types.ObjectId, ref: 'Community', required: true },
  author: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  title: { type: String, required: true, maxlength: 300 },
  type: { type: String, enum: ['text','link','media'], default: 'text' },
  body: { type: String, maxlength: 40000 },
  url: { type: String },
  media: [{ url: String, type: { type: String, enum: ['image','video'] } }],
  flairId: { type: Schema.Types.ObjectId },
  score: { type: Number, default: 0 },
  upvotes: { type: Number, default: 0 },
  downvotes: { type: Number, default: 0 },
  commentCount: { type: Number, default: 0 },
  isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

PostSchema.index({ community: 1, createdAt: -1 });
PostSchema.index({ community: 1, score: -1, createdAt: -1 });
PostSchema.index({ author: 1, createdAt: -1 });

export const Post = mongoose.model<IPost>('Post', PostSchema);
export default Post;