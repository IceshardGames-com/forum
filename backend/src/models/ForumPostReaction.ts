import mongoose, { Document, Schema } from 'mongoose';

export type ReactionType = 'like' | 'dislike';

export interface IForumPostReaction extends Document {
  _id: string;
  post: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  type: ReactionType;
  createdAt: Date;
  updatedAt: Date;
}

const forumPostReactionSchema = new Schema<IForumPostReaction>({
  post: {
    type: Schema.Types.ObjectId,
    ref: 'ForumPost',
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

forumPostReactionSchema.index({ post: 1, user: 1 }, { unique: true });
forumPostReactionSchema.index({ post: 1, type: 1 });

export const ForumPostReaction = mongoose.model<IForumPostReaction>('ForumPostReaction', forumPostReactionSchema);
export default ForumPostReaction;


