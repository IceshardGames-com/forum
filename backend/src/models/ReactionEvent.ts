import mongoose, { Document, Schema } from 'mongoose';

export type ReactionTargetType = 'post' | 'comment';
export type ReactionOp = 'set_like' | 'set_dislike' | 'unset';

export interface IReactionEvent extends Document {
  _id: string;
  targetType: ReactionTargetType;
  targetId: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  op: ReactionOp;
  createdAt: Date;
}

const reactionEventSchema = new Schema<IReactionEvent>({
  targetType: { type: String, enum: ['post', 'comment'], required: true, index: true },
  targetId: { type: Schema.Types.ObjectId, required: true, index: true },
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  op: { type: String, enum: ['set_like', 'set_dislike', 'unset'], required: true },
}, { timestamps: { createdAt: true, updatedAt: false } });

reactionEventSchema.index({ targetType: 1, targetId: 1, createdAt: -1 });

export const ReactionEvent = mongoose.model<IReactionEvent>('ReactionEvent', reactionEventSchema);
export default ReactionEvent;


