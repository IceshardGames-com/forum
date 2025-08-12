import mongoose, { Schema, Document } from 'mongoose';

export type CommunityVisibility = 'public' | 'private' | 'restricted';
export type CommunityJoinMode = 'open' | 'request' | 'invite';

export interface ICommunity extends Document {
  name: string;
  slug: string;
  description?: string;
  visibility: CommunityVisibility;
  joinMode: CommunityJoinMode;
  owner: mongoose.Types.ObjectId;
  moderators: mongoose.Types.ObjectId[];
  memberCount: number;
  postCount: number;
  createdAt: Date;
  updatedAt: Date;
}

const CommunitySchema = new Schema<ICommunity>({
  name: { type: String, required: true, trim: true, maxlength: 60 },
  slug: { type: String, required: true, unique: true, lowercase: true, trim: true },
  description: { type: String, maxlength: 40000 },
  visibility: { type: String, enum: ['public','private','restricted'], default: 'public' },
  joinMode: { type: String, enum: ['open','request','invite'], default: 'open' },
  owner: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  moderators: [{ type: Schema.Types.ObjectId, ref: 'User' }],
  memberCount: { type: Number, default: 1 },
  postCount: { type: Number, default: 0 },
}, { timestamps: true });

CommunitySchema.index({ slug: 1 }, { unique: true });
CommunitySchema.index({ visibility: 1, createdAt: -1 });

export const Community = mongoose.model<ICommunity>('Community', CommunitySchema);
export default Community;