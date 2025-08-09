import mongoose, { Schema, Document } from 'mongoose';

export interface INotificationPreference extends Document {
  user: mongoose.Types.ObjectId;
  preferences: {
    friend_request: boolean;
    friend_accepted: boolean;
    system: boolean;
    game_feedback: boolean;
    message: boolean;
  };
  emailNotifications: boolean;
  pushNotifications: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const NotificationPreferenceSchema = new Schema<INotificationPreference>(
  {
    user: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      unique: true,
      index: true,
    },
    preferences: {
      friend_request: { type: Boolean, default: true },
      friend_accepted: { type: Boolean, default: true },
      system: { type: Boolean, default: true },
      game_feedback: { type: Boolean, default: true },
      message: { type: Boolean, default: true },
    },
    emailNotifications: {
      type: Boolean,
      default: false, // Default to false for privacy
    },
    pushNotifications: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
  }
);

export const NotificationPreference = mongoose.model<INotificationPreference>(
  'NotificationPreference',
  NotificationPreferenceSchema
);

export default NotificationPreference;
