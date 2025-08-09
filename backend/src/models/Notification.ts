import mongoose, { Schema, Document } from 'mongoose';

export type NotificationType = 
  | 'friend_request' 
  | 'friend_accepted' 
  | 'system' 
  | 'game_feedback' 
  | 'message';

export interface INotification extends Document {
  recipient: mongoose.Types.ObjectId;
  type: NotificationType;
  title: string;
  message: string;
  link?: string;
  metadata?: Record<string, any>;
  isRead: boolean;
  createdAt: Date;
}

const NotificationSchema = new Schema<INotification>(
  {
    recipient: { 
      type: Schema.Types.ObjectId, 
      ref: 'User', 
      required: true, 
      index: true 
    },
    type: { 
      type: String, 
      enum: ['friend_request', 'friend_accepted', 'system', 'game_feedback', 'message'],
      required: true,
      index: true
    },
    title: { 
      type: String, 
      required: true,
      maxlength: 100
    },
    message: { 
      type: String, 
      required: true,
      maxlength: 500
    },
    link: { 
      type: String,
      maxlength: 200
    },
    metadata: {
      type: Schema.Types.Mixed,
      default: {}
    },
    isRead: { 
      type: Boolean, 
      default: false,
      index: true
    },
  },
  { 
    timestamps: true,
  }
);

// Compound indexes for efficient queries
NotificationSchema.index({ recipient: 1, isRead: 1 });
NotificationSchema.index({ recipient: 1, createdAt: -1 });
NotificationSchema.index({ recipient: 1, type: 1 });

// TTL index for auto-cleanup (30 days)
NotificationSchema.index({ createdAt: 1 }, { expireAfterSeconds: 60 * 60 * 24 * 30 });

export const Notification = mongoose.model<INotification>('Notification', NotificationSchema);
export default Notification;
