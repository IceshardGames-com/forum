import mongoose, { Schema, Document } from 'mongoose';

export type CommunityRole = 'owner' | 'moderator' | 'member';
export type MembershipStatus = 'active' | 'pending' | 'banned';

export interface ICommunityMember extends Document {
  community: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  role: CommunityRole;
  status: MembershipStatus;
  invitedBy?: mongoose.Types.ObjectId;
  createdAt: Date;
  updatedAt: Date;
}

const CommunityMemberSchema = new Schema<ICommunityMember>({
  community: { type: Schema.Types.ObjectId, ref: 'Community', required: true },
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  role: { type: String, enum: ['owner','moderator','member'], default: 'member' },
  status: { type: String, enum: ['active','pending','banned'], default: 'active' },
  invitedBy: { type: Schema.Types.ObjectId, ref: 'User' },
}, { timestamps: true });

CommunityMemberSchema.index({ community: 1, user: 1 }, { unique: true });
CommunityMemberSchema.index({ user: 1, status: 1 });
CommunityMemberSchema.index({ community: 1, role: 1 });

export const CommunityMember = mongoose.model<ICommunityMember>('CommunityMember', CommunityMemberSchema);
export default CommunityMember;