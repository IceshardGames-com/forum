import mongoose, { Document, Schema } from 'mongoose';

export interface IForumComment extends Document {
  _id: string;
  post: mongoose.Types.ObjectId;
  author: mongoose.Types.ObjectId;
  parentComment?: mongoose.Types.ObjectId | null;
  content: string;
  likes: number;
  dislikes: number;
  createdAt: Date;
  updatedAt: Date;
}

const forumCommentSchema = new Schema<IForumComment>({
  post: {
    type: Schema.Types.ObjectId,
    ref: 'ForumPost',
    required: true,
    index: true,
  },
  author: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  parentComment: {
    type: Schema.Types.ObjectId,
    ref: 'ForumComment',
    default: null,
    index: true,
  },
  content: {
    type: String,
    required: true,
    maxlength: 5000,
  },
  likes: {
    type: Number,
    default: 0,
  },
  dislikes: {
    type: Number,
    default: 0,
  },
}, {
  timestamps: true,
});

forumCommentSchema.index({ post: 1, parentComment: 1, createdAt: 1 });

export const ForumComment = mongoose.model<IForumComment>('ForumComment', forumCommentSchema);
export default ForumComment;

