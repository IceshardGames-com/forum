import mongoose, { Schema, Document } from 'mongoose';

export interface IComment extends Document {
  post: mongoose.Types.ObjectId;
  community: mongoose.Types.ObjectId;
  author: mongoose.Types.ObjectId;
  parentComment?: mongoose.Types.ObjectId | null;
  ancestors: mongoose.Types.ObjectId[];
  depth: number;
  body: string;
  score: number;
  upvotes: number;
  downvotes: number;
  isDeleted: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const CommentSchema = new Schema<IComment>({
  post: { type: Schema.Types.ObjectId, ref: 'Post', required: true },
  community: { type: Schema.Types.ObjectId, ref: 'Community', required: true },
  author: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  parentComment: { type: Schema.Types.ObjectId, ref: 'Comment', default: null },
  ancestors: [{ type: Schema.Types.ObjectId, ref: 'Comment' }],
  depth: { type: Number, default: 0 },
  body: { type: String, required: true, maxlength: 40000 },
  score: { type: Number, default: 0 },
  upvotes: { type: Number, default: 0 },
  downvotes: { type: Number, default: 0 },
  isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

CommentSchema.index({ post: 1, createdAt: 1 });
CommentSchema.index({ post: 1, score: -1, createdAt: -1 });
CommentSchema.index({ post: 1, ancestors: 1, createdAt: 1 });
CommentSchema.index({ author: 1, createdAt: -1 });

export const Comment = mongoose.model<IComment>('Comment', CommentSchema);
export default Comment;