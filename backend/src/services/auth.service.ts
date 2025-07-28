import { User, IUser, UserRole } from '../models/User';
import { jwtService, TokenPair } from '../utils/jwt';
import { createLogger } from '../utils/logger';
import { AppError } from '../middlewares/errorHandler';

/**
 * Interface for user registration data
 */
export interface RegisterUserData {
  username: string;
  email: string;
  password: string;
  role?: UserRole;
}

/**
 * Interface for user login data
 */
export interface LoginUserData {
  email?: string;
  username?: string;
  password: string;
  rememberMe?: boolean;
}

/**
 * Interface for authentication response
 */
export interface AuthResponse {
  success: true;
  data: {
    user: Omit<IUser, 'password'>;
    tokens: TokenPair;
  };
  message: string;
}

/**
 * Interface for user profile update
 */
export interface UpdateProfileData {
  username?: string;
  email?: string;
}

/**
 * Authentication service class
 */
export class AuthService {
  private static instance: AuthService;

  private constructor() {}

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  /**
   * Registers a new user
   */
  public async register(userData: RegisterUserData, requestId?: string): Promise<AuthResponse> {
    const logger = createLogger(requestId);

    try {
      const { username, email, password, role = UserRole.GAMER } = userData;

      // Check if user already exists
      const existingUser = await User.findOne({
        $or: [
          { email: email.toLowerCase() },
          { username }
        ]
      });

      if (existingUser) {
        const conflictField = existingUser.email === email.toLowerCase() ? 'email' : 'username';
        
        logger.warn('Registration failed: User already exists', {
          conflictField,
          existingUserId: existingUser._id,
        });

        throw new AppError(
          `User with this ${conflictField} already exists`,
          409,
          'USER_EXISTS',
          { field: conflictField }
        );
      }

      // Create new user
      const newUser = new User({
        username,
        email: email.toLowerCase(),
        password, // Will be hashed by pre-save middleware
        role,
      });

      // Save user to database
      const savedUser = await newUser.save();

      // Generate tokens
      const tokens = jwtService.generateTokenPair({
        userId: savedUser._id.toString(),
        email: savedUser.email,
        role: savedUser.role,
      });

      // Update last login
      savedUser.lastLogin = new Date();
      await savedUser.save();

      return {
        success: true,
        data: {
          user: savedUser.toJSON() as Omit<IUser, 'password'>,
          tokens,
        },
        message: 'User registered successfully',
      };
    } catch (error) {
      logger.error('User registration failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        username: userData.username,
        email: userData.email,
      });

      if (error instanceof AppError) {
        throw error;
      }

      throw new AppError(
        'Registration failed',
        500,
        'REGISTRATION_ERROR'
      );
    }
  }

  /**
   * Authenticates a user and returns tokens
   */
  public async login(loginData: LoginUserData, requestId?: string): Promise<AuthResponse> {
    const logger = createLogger(requestId);

    try {
      const { email, username, password, rememberMe = false } = loginData;

      // Find user by email or username
      let user: IUser | null = null;
      
      if (email) {
        user = await User.findOne({ email: email.toLowerCase() }).select('+password');
      } else if (username) {
        user = await User.findOne({ username }).select('+password');
      }

      if (!user) {
        logger.warn('Login failed: User not found', {
          email,
          username,
        });

        throw new AppError(
          'Invalid credentials',
          401,
          'INVALID_CREDENTIALS'
        );
      }

      // Check if user is active
      if (!user.isActive) {
        logger.warn('Login failed: Account deactivated', {
          userId: user._id,
        });

        throw new AppError(
          'Account is deactivated',
          401,
          'ACCOUNT_DEACTIVATED'
        );
      }

      // Verify password
      const isPasswordValid = await user.comparePassword(password);
      
      if (!isPasswordValid) {
        logger.warn('Login failed: Invalid password', {
          userId: user._id,
        });

        throw new AppError(
          'Invalid credentials',
          401,
          'INVALID_CREDENTIALS'
        );
      }

      // Generate tokens (with longer expiry if rememberMe is true)
      const tokenPayload = {
        userId: user._id.toString(),
        email: user.email,
        role: user.role,
      };

      const tokens = rememberMe 
        ? {
            accessToken: jwtService.signToken(tokenPayload, '30d'),
            refreshToken: jwtService.signToken(tokenPayload, '90d'),
          }
        : jwtService.generateTokenPair(tokenPayload);

      // Update last login
      user.lastLogin = new Date();
      await user.save();

      return {
        success: true,
        data: {
          user: user.toJSON() as Omit<IUser, 'password'>,
          tokens,
        },
        message: 'Login successful',
      };
    } catch (error) {
      logger.error('User login failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        email: loginData.email,
        username: loginData.username,
      });

      if (error instanceof AppError) {
        throw error;
      }

      throw new AppError(
        'Login failed',
        500,
        'LOGIN_ERROR'
      );
    }
  }

  /**
   * Logs out a user (stateless - just returns success message)
   */
  public async logout(user: IUser, requestId?: string): Promise<{ success: true; message: string }> {
    const logger = createLogger(requestId);

    try {
      // In a stateless JWT system, logout is handled on the client side
      // by removing the token. Here we just log the logout event.
      
      // Optional: You could maintain a blacklist of tokens for more security
      // or update user's last activity timestamp

      return {
        success: true,
        message: 'Logged out successfully',
      };
    } catch (error) {
      logger.error('Logout failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId: user._id,
      });

      throw new AppError(
        'Logout failed',
        500,
        'LOGOUT_ERROR'
      );
    }
  }

  /**
   * Gets user profile by ID
   */
  public async getProfile(userId: string, requestId?: string): Promise<Omit<IUser, 'password'>> {
    const logger = createLogger(requestId);

    try {
      const user = await User.findById(userId);

      if (!user) {
        logger.warn('Get profile failed: User not found', { userId });
        throw new AppError('User not found', 404, 'USER_NOT_FOUND');
      }

      if (!user.isActive) {
        logger.warn('Get profile failed: Account deactivated', { userId });
        throw new AppError('Account is deactivated', 401, 'ACCOUNT_DEACTIVATED');
      }

      return user.toJSON() as Omit<IUser, 'password'>;
    } catch (error) {
      logger.error('Get profile failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }

      throw new AppError('Failed to get profile', 500, 'PROFILE_ERROR');
    }
  }

  /**
   * Updates user profile
   */
  public async updateProfile(
    userId: string,
    updateData: UpdateProfileData,
    requestId?: string
  ): Promise<Omit<IUser, 'password'>> {
    const logger = createLogger(requestId);

    try {
      const { username, email } = updateData;

      // Find the user
      const user = await User.findById(userId);

      if (!user) {
        logger.warn('Profile update failed: User not found', { userId });
        throw new AppError('User not found', 404, 'USER_NOT_FOUND');
      }

      // Check for conflicts with existing users
      if (email || username) {
        const conflictQuery: any = {
          _id: { $ne: userId }, // Exclude current user
        };

        const orConditions: any[] = [];
        
        if (email && email !== user.email) {
          orConditions.push({ email: email.toLowerCase() });
        }
        
        if (username && username !== user.username) {
          orConditions.push({ username });
        }

        if (orConditions.length > 0) {
          conflictQuery.$or = orConditions;
          
          const existingUser = await User.findOne(conflictQuery);
          
          if (existingUser) {
            const conflictField = existingUser.email === email?.toLowerCase() ? 'email' : 'username';
            
            logger.warn('Profile update failed: Conflict detected', {
              userId,
              conflictField,
            });

            throw new AppError(
              `User with this ${conflictField} already exists`,
              409,
              'USER_EXISTS',
              { field: conflictField }
            );
          }
        }
      }

      // Update user fields
      if (username) user.username = username;
      if (email) user.email = email.toLowerCase();

      // Save updated user
      const updatedUser = await user.save();

      return updatedUser.toJSON() as Omit<IUser, 'password'>;
    } catch (error) {
      logger.error('Profile update failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }

      throw new AppError('Profile update failed', 500, 'PROFILE_UPDATE_ERROR');
    }
  }

  /**
   * Changes user password
   */
  public async changePassword(
    userId: string,
    currentPassword: string,
    newPassword: string,
    requestId?: string
  ): Promise<{ success: true; message: string }> {
    const logger = createLogger(requestId);

    try {
      // Find user with password
      const user = await User.findById(userId).select('+password');

      if (!user) {
        logger.warn('Password change failed: User not found', { userId });
        throw new AppError('User not found', 404, 'USER_NOT_FOUND');
      }

      // Verify current password
      const isCurrentPasswordValid = await user.comparePassword(currentPassword);
      
      if (!isCurrentPasswordValid) {
        logger.warn('Password change failed: Invalid current password', { userId });
        throw new AppError('Current password is incorrect', 401, 'INVALID_PASSWORD');
      }

      // Update password (will be hashed by pre-save middleware)
      user.password = newPassword;
      await user.save();

      return {
        success: true,
        message: 'Password changed successfully',
      };
    } catch (error) {
      logger.error('Password change failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });

      if (error instanceof AppError) {
        throw error;
      }

      throw new AppError('Password change failed', 500, 'PASSWORD_CHANGE_ERROR');
    }
  }
}

// Export singleton instance
export const authService = AuthService.getInstance(); 