import mongoose, { Schema, Document, Types } from 'mongoose';

export interface IDevice extends Document {
  user: Types.ObjectId;
  deviceId: string;
  publicKey: string;
  deviceName?: string;
  deviceType?: string;
  lastSeen: Date;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const DeviceSchema = new Schema<IDevice>(
  {
    user: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    deviceId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    publicKey: {
      type: String,
      required: true,
    },
    deviceName: {
      type: String,
      maxlength: 100,
    },
    deviceType: {
      type: String,
      enum: ['android', 'ios', 'web', 'desktop'],
      default: 'android',
    },
    lastSeen: {
      type: Date,
      default: Date.now,
    },
    isActive: {
      type: Boolean,
      default: true,
      index: true,
    },
  },
  {
    timestamps: true,
  }
);

// Compound indexes for efficient queries
DeviceSchema.index({ user: 1, isActive: 1 });
DeviceSchema.index({ user: 1, deviceId: 1 }, { unique: true });

// TTL index to clean up inactive devices after 90 days
DeviceSchema.index({ lastSeen: 1 }, { 
  expireAfterSeconds: 60 * 60 * 24 * 90,
  partialFilterExpression: { isActive: false }
});

export const Device = mongoose.model<IDevice>('Device', DeviceSchema);
export default Device;
