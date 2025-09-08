import mongoose, { Document, Schema } from 'mongoose';
import bcrypt from 'bcrypt';
import { envConfig } from '../config/env';

// User roles enum
export enum UserRole {
  GAMER = 'gamer',
  DEVELOPER = 'developer',
  ADMIN = 'admin',
}

// User interface for TypeScript
export interface IUser extends Document {
  _id: string;
  username: string;
  email: string;
  password: string;
  role: UserRole;
  isActive: boolean;
  isEmailVerified: boolean;
  lastLogin?: Date;
  interests?: mongoose.Types.ObjectId[];
  createdAt: Date;
  updatedAt: Date;

  avatarImageId?: string; // Cloudflare Images ID
  avatarR2Key?: string; // Optional fallback stored in R2

  // Instance methods
  comparePassword(candidatePassword: string): Promise<boolean>;
  toJSON(): Omit<IUser, 'password'>;
}

// User schema definition
const userSchema = new Schema<IUser>(
  {
    username: {
      type: String,
      required: [true, 'Username is required'],
      unique: true,
      trim: true,
      minlength: [3, 'Username must be at least 3 characters long'],
      maxlength: [30, 'Username cannot exceed 30 characters'],
      match: [
        /^[a-zA-Z0-9_]+$/,
        'Username can only contain letters, numbers, and underscores',
      ],
    },
    email: {
      type: String,
      required: [true, 'Email is required'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        'Please provide a valid email address',
      ],
    },
    password: {
      type: String,
      required: [true, 'Password is required'],
      minlength: [8, 'Password must be at least 8 characters long'],
      maxlength: [128, 'Password cannot exceed 128 characters'],
      select: false, // Don't include password in queries by default
    },
    role: {
      type: String,
      enum: {
        values: Object.values(UserRole),
        message: 'Role must be one of: gamer, developer, admin',
      },
      required: [true, 'User role is required'],
      default: UserRole.GAMER,
    },
    isActive: {
      type: Boolean,
      default: true,
    },
    isEmailVerified: {
      type: Boolean,
      default: false,
    },
    lastLogin: {
      type: Date,
    },
    interests: [
      {
        type: Schema.Types.ObjectId,
        ref: 'GamingInterest',
      },
    ],
    avatarImageId: {
      type: String,
      default: undefined,
      index: true,
    },
    avatarR2Key: {
      type: String,
      default: undefined,
    },
  },
  {
    timestamps: true, // Automatically adds createdAt and updatedAt
    toJSON: {
      virtuals: true,
      transform: function (_doc, ret) {
        delete (ret as any).password;
        delete (ret as any).__v;
        return ret;
      },
    },
    toObject: {
      virtuals: true,
      transform: function (_doc, ret) {
        delete (ret as any).password;
        delete (ret as any).__v;
        return ret;
      },
    },
  }
);

// Indexes for better performance (email and username already have unique indexes)
userSchema.index({ role: 1 });
userSchema.index({ isActive: 1 });
userSchema.index({ createdAt: -1 });

// Pre-save middleware to hash password
userSchema.pre('save', async function (next) {
  // Only hash the password if it has been modified (or is new)
  if (!this.isModified('password')) {
    return next();
  }

  try {
    // Hash password with configured salt rounds
    const hashedPassword = await bcrypt.hash(this.password, envConfig.BCRYPT_ROUNDS);
    this.password = hashedPassword;
    next();
  } catch (error) {
    next(error as Error);
  }
});

// Instance method to compare password
userSchema.methods.comparePassword = async function (
  candidatePassword: string
): Promise<boolean> {
  try {
    return await bcrypt.compare(candidatePassword, this.password);
  } catch (error) {
    throw new Error('Password comparison failed');
  }
};

// Static methods
userSchema.statics.findByEmail = function (email: string) {
  return this.findOne({ email: email.toLowerCase() });
};

userSchema.statics.findByUsername = function (username: string) {
  return this.findOne({ username });
};

userSchema.statics.findActiveUsers = function () {
  return this.find({ isActive: true });
};

userSchema.statics.findByRole = function (role: UserRole) {
  return this.find({ role, isActive: true });
};

// Virtual for user's full display name (can be enhanced later)
userSchema.virtual('displayName').get(function () {
  return this.username;
});

// Virtual to check if user is privileged (developer or admin)
userSchema.virtual('isPrivileged').get(function () {
  return this.role === UserRole.DEVELOPER || this.role === UserRole.ADMIN;
});

// Virtual for avatar URL
userSchema.virtual('avatar').get(function () {
  try {
    const self = this as any;
    const { CF_IMAGES_DELIVERY_URL, R2_PUBLIC_BASE_URL } = require('../config/env').envConfig;
    if (self.avatarImageId && CF_IMAGES_DELIVERY_URL) {
      const base = CF_IMAGES_DELIVERY_URL.replace(/\/$/, '');
      return `${base}/${self.avatarImageId}/avatar`; // use an Images variant named "avatar"
    }
    if (self.avatarR2Key && R2_PUBLIC_BASE_URL) {
      return `${R2_PUBLIC_BASE_URL.replace(/\/$/, '')}/${self.avatarR2Key}`;
    }
    return undefined;
  } catch (_e) {
    return undefined;
  }
});

// Static interface for model methods
export interface IUserModel extends mongoose.Model<IUser> {
  findByEmail(email: string): Promise<IUser | null>;
  findByUsername(username: string): Promise<IUser | null>;
  findActiveUsers(): Promise<IUser[]>;
  findByRole(role: UserRole): Promise<IUser[]>;
}

// Create and export the model
export const User = mongoose.model<IUser, IUserModel>('User', userSchema);

// Export default
export default User; 