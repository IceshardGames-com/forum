import mongoose, { Document, Schema } from 'mongoose';

export type ReactionType = 'like' | 'dislike';

export interface IForumCommentReaction extends Document {
  _id: string;
  comment: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  type: ReactionType;
  createdAt: Date;
  updatedAt: Date;
}

const forumCommentReactionSchema = new Schema<IForumCommentReaction>({
  comment: {
    type: Schema.Types.ObjectId,
    ref: 'ForumComment',
    required: true,
    index: true,
  },
  user: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  type: {
    type: String,
    enum: ['like', 'dislike'],
    required: true,
    index: true,
  },
}, {
  timestamps: true,
});

forumCommentReactionSchema.index({ comment: 1, user: 1 }, { unique: true });

export const ForumCommentReaction = mongoose.model<IForumCommentReaction>('ForumCommentReaction', forumCommentReactionSchema);
export default ForumCommentReaction;


