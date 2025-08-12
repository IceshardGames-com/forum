import mongoose, { Schema, Document } from 'mongoose';

export interface IPostVote extends Document {
  post: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  value: 1 | -1;
  createdAt: Date;
  updatedAt: Date;
}

const PostVoteSchema = new Schema<IPostVote>({
  post: { type: Schema.Types.ObjectId, ref: 'Post', required: true },
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  value: { type: Number, enum: [1, -1], required: true },
}, { timestamps: true });

PostVoteSchema.index({ post: 1, user: 1 }, { unique: true });

export const PostVote = mongoose.model<IPostVote>('PostVote', PostVoteSchema);
export default PostVote;