import mongoose, { Schema, Document, Types } from 'mongoose';

export interface IMessagePayload {
  deviceId: string;
  ciphertext: string; // base64 encoded encrypted data
  ephemeralPublicKey?: string; // for forward secrecy (NaCl box)
  nonce?: string; // if required by encryption algorithm
  metadata?: Record<string, any>; // additional encryption metadata
}

export interface IMessage extends Document {
  conversationId: Types.ObjectId;
  sender: Types.ObjectId;
  payloads: IMessagePayload[]; // one encrypted payload per recipient device
  messageType: 'text' | 'image' | 'file' | 'system';
  replyTo?: Types.ObjectId; // reference to replied message
  isEdited: boolean;
  editedAt?: Date;
  deliveredTo: Types.ObjectId[]; // user IDs who have received the message
  readBy: Types.ObjectId[]; // user IDs who have read the message
  createdAt: Date;
  updatedAt: Date;
}

const MessagePayloadSchema = new Schema<IMessagePayload>({
  deviceId: {
    type: String,
    required: true,
  },
  ciphertext: {
    type: String,
    required: true,
  },
  ephemeralPublicKey: {
    type: String,
  },
  nonce: {
    type: String,
  },
  metadata: {
    type: Schema.Types.Mixed,
    default: {},
  },
}, { _id: false });

const MessageSchema = new Schema<IMessage>(
  {
    conversationId: {
      type: Schema.Types.ObjectId,
      ref: 'Conversation',
      required: true,
      index: true,
    },
    sender: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    payloads: {
      type: [MessagePayloadSchema],
      required: true,
      validate: {
        validator: function(v: IMessagePayload[]) {
          return v.length > 0;
        },
        message: 'Message must have at least one payload'
      },
    },
    messageType: {
      type: String,
      enum: ['text', 'image', 'file', 'system'],
      default: 'text',
      index: true,
    },
    replyTo: {
      type: Schema.Types.ObjectId,
      ref: 'Message',
    },
    isEdited: {
      type: Boolean,
      default: false,
    },
    editedAt: {
      type: Date,
    },
    deliveredTo: [{
      type: Schema.Types.ObjectId,
      ref: 'User',
    }],
    readBy: [{
      type: Schema.Types.ObjectId,
      ref: 'User',
    }],
  },
  {
    timestamps: true,
  }
);

// Compound indexes for efficient queries
MessageSchema.index({ conversationId: 1, createdAt: -1 });
MessageSchema.index({ sender: 1, createdAt: -1 });
MessageSchema.index({ conversationId: 1, sender: 1 });
MessageSchema.index({ 'payloads.deviceId': 1 });

// Instance method to check if message is delivered to user
MessageSchema.methods.isDeliveredTo = function(userId: string): boolean {
  return this.deliveredTo.some((id: Types.ObjectId) => id.toString() === userId);
};

// Instance method to check if message is read by user
MessageSchema.methods.isReadBy = function(userId: string): boolean {
  return this.readBy.some((id: Types.ObjectId) => id.toString() === userId);
};

// Instance method to get payload for specific device
MessageSchema.methods.getPayloadForDevice = function(deviceId: string): IMessagePayload | null {
  return this.payloads.find((p: IMessagePayload) => p.deviceId === deviceId) || null;
};

// Instance method to mark as delivered to user
MessageSchema.methods.markDelivered = function(userId: string): void {
  if (!this.isDeliveredTo(userId)) {
    this.deliveredTo.push(new Types.ObjectId(userId));
  }
};

// Instance method to mark as read by user
MessageSchema.methods.markRead = function(userId: string): void {
  if (!this.isReadBy(userId)) {
    this.readBy.push(new Types.ObjectId(userId));
    // Also mark as delivered if not already
    this.markDelivered(userId);
  }
};

export const Message = mongoose.model<IMessage>('Message', MessageSchema);
export default Message;
