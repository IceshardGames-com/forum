import mongoose, { Schema, Document, Types } from 'mongoose';

export interface IConversation extends Document {
  participants: [Types.ObjectId, Types.ObjectId];
  isFriendBased: boolean;
  initiatedBy: Types.ObjectId | null;
  lastMessage?: Types.ObjectId;
  lastMessageAt?: Date;
  isActive: boolean;
  deletedBy?: Types.ObjectId[];
  createdAt: Date;
  updatedAt: Date;
}

const ConversationSchema = new Schema<IConversation>(
  {
    participants: {
      type: [{ type: Schema.Types.ObjectId, ref: 'User' }],
      required: true,
      validate: {
        validator: function(v: Types.ObjectId[]) {
          return v.length === 2;
        },
        message: 'Conversation must have exactly 2 participants'
      },
    },
    isFriendBased: {
      type: Boolean,
      required: true,
      default: false,
      index: true,
    },
    initiatedBy: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      default: null,
    },
    lastMessage: {
      type: Schema.Types.ObjectId,
      ref: 'Message',
    },
    lastMessageAt: {
      type: Date,
      index: true,
    },
    isActive: {
      type: Boolean,
      default: true,
      index: true,
    },
    deletedBy: [{
      type: Schema.Types.ObjectId,
      ref: 'User',
    }],
  },
  {
    timestamps: true,
  }
);

// Compound indexes for efficient queries
ConversationSchema.index({ participants: 1 }, { unique: true });
ConversationSchema.index({ participants: 1, isActive: 1 });
ConversationSchema.index({ lastMessageAt: -1, isActive: 1 });
ConversationSchema.index({ isFriendBased: 1, isActive: 1 });

// Pre-save middleware to sort participants for consistent indexing
ConversationSchema.pre('save', function(next) {
  if (this.isModified('participants')) {
    this.participants.sort((a, b) => a.toString().localeCompare(b.toString()));
  }
  next();
});

// Instance method to check if user is participant
ConversationSchema.methods.isParticipant = function(userId: string): boolean {
  return this.participants.some((p: Types.ObjectId) => p.toString() === userId);
};

// Instance method to get other participant
ConversationSchema.methods.getOtherParticipant = function(userId: string): Types.ObjectId | null {
  const other = this.participants.find((p: Types.ObjectId) => p.toString() !== userId);
  return other || null;
};

// Instance method to check if conversation is deleted for user
ConversationSchema.methods.isDeletedFor = function(userId: string): boolean {
  return this.deletedBy?.some((id: Types.ObjectId) => id.toString() === userId) || false;
};

export const Conversation = mongoose.model<IConversation>('Conversation', ConversationSchema);
export default Conversation;
