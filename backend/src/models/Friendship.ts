import mongoose, { Document, Schema, Types } from 'mongoose';

export type FriendshipStatus = 'pending' | 'accepted' | 'declined' | 'blocked';

export interface IFriendship extends Document {
  requester: Types.ObjectId;
  recipient: Types.ObjectId;
  status: FriendshipStatus;
  createdAt: Date;
  updatedAt: Date;
}

const FriendshipSchema = new Schema<IFriendship>(
  {
    requester: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    recipient: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    status: { type: String, enum: ['pending', 'accepted', 'declined', 'blocked'], required: true, default: 'pending' },
  },
  { timestamps: true }
);

// Prevent duplicate relationships in either direction
FriendshipSchema.index({ requester: 1, recipient: 1 }, { unique: true });
// Common query patterns
FriendshipSchema.index({ recipient: 1, status: 1 });
FriendshipSchema.index({ requester: 1, status: 1 });
FriendshipSchema.index({ status: 1 });

export const Friendship = mongoose.model<IFriendship>('Friendship', FriendshipSchema);

export default Friendship;


