import mongoose, { Schema, Document } from 'mongoose';

export interface ICommentVote extends Document {
  comment: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  value: 1 | -1;
  createdAt: Date;
  updatedAt: Date;
}

const CommentVoteSchema = new Schema<ICommentVote>({
  comment: { type: Schema.Types.ObjectId, ref: 'Comment', required: true },
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  value: { type: Number, enum: [1, -1], required: true },
}, { timestamps: true });

CommentVoteSchema.index({ comment: 1, user: 1 }, { unique: true });

export const CommentVote = mongoose.model<ICommentVote>('CommentVote', CommentVoteSchema);
export default CommentVote;