import { Types } from 'mongoose';
import { User } from '../models/User';
import { createLogger } from '../utils/logger';
import { AppError } from '../middlewares/errorHandler';

export interface UserSearchOptions {
  page?: number;
  limit?: number;
  role?: string | undefined;
  includeInactive?: boolean;
}

export interface UserSearchResult {
  _id: string;
  username: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: Date;
  interests?: Types.ObjectId[];
}

export class UserSearchService {
  private static instance: UserSearchService;
  
  private constructor() {}
  
  public static getInstance(): UserSearchService {
    if (!UserSearchService.instance) {
      UserSearchService.instance = new UserSearchService();
    }
    return UserSearchService.instance;
  }

  /**
   * Search users by username with advanced filtering options
   */
  public async searchUsers(
    query: string,
    currentUserId: string,
    options: UserSearchOptions = {},
    requestId?: string | undefined
  ): Promise<{
    users: UserSearchResult[];
    pagination: {
      page: number;
      limit: number;
      total: number;
      hasNext: boolean;
      hasPrev: boolean;
    };
  }> {
    const logger = createLogger(requestId);
    
    // Validate and set defaults
    const page = Math.max(options.page || 1, 1);
    const limit = Math.min(Math.max(options.limit || 20, 1), 50);
    const skip = (page - 1) * limit;
    
    // Build search filter
    const searchFilter: any = {
      _id: { $ne: new Types.ObjectId(currentUserId) }, // Exclude current user
    };

    // Add query filter for username search
    if (query && query.trim().length > 0) {
      const searchQuery = query.trim();
      searchFilter.$or = [
        { username: { $regex: searchQuery, $options: 'i' } },
        { email: { $regex: searchQuery, $options: 'i' } },
      ];
    }

    // Filter by role if specified
    if (options.role) {
      searchFilter.role = options.role;
    }

    // Filter by active status (default: only active users)
    if (!options.includeInactive) {
      searchFilter.isActive = true;
    }

    try {
      // Execute search with pagination
      const [users, total] = await Promise.all([
        User.find(searchFilter)
          .select('username email role isActive createdAt interests')
          .sort({ username: 1 }) // Sort alphabetically by username
          .skip(skip)
          .limit(limit)
          .lean<UserSearchResult[]>(),
        User.countDocuments(searchFilter),
      ]);

      const pagination = {
        page,
        limit,
        total,
        hasNext: skip + limit < total,
        hasPrev: page > 1,
      };

      logger.info('User search completed', {
        query,
        resultCount: users.length,
        total,
        page,
        filters: { role: options.role, includeInactive: options.includeInactive },
      });

      return { users, pagination };
    } catch (error) {
      logger.error('User search failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        query,
        options,
      });
      throw new AppError('User search failed', 500, 'SEARCH_ERROR');
    }
  }

  /**
   * Get user suggestions based on interests and activity
   */
  public async getUserSuggestions(
    currentUserId: string,
    options: UserSearchOptions = {},
    requestId?: string | undefined
  ): Promise<{
    users: UserSearchResult[];
    pagination: {
      page: number;
      limit: number;
      total: number;
      hasNext: boolean;
      hasPrev: boolean;
    };
  }> {
    const logger = createLogger(requestId);
    
    const page = Math.max(options.page || 1, 1);
    const limit = Math.min(Math.max(options.limit || 20, 1), 50);
    const skip = (page - 1) * limit;

    try {
      // Get current user's interests
      const currentUser = await User.findById(currentUserId).select('interests');
      if (!currentUser) {
        throw new AppError('User not found', 404, 'USER_NOT_FOUND');
      }

      const pipeline: any[] = [
        // Exclude current user and inactive users
        {
          $match: {
            _id: { $ne: new Types.ObjectId(currentUserId) },
            isActive: true,
          },
        },
      ];

      // If user has interests, prioritize users with similar interests
      if (currentUser.interests && currentUser.interests.length > 0) {
        pipeline.push(
          {
            $addFields: {
              commonInterests: {
                $size: {
                  $setIntersection: ['$interests', currentUser.interests],
                },
              },
            },
          },
          {
            $sort: {
              commonInterests: -1, // Users with more common interests first
              createdAt: -1, // Then by newest users
            },
          }
        );
      } else {
        // If no interests, sort by newest users
        pipeline.push({
          $sort: { createdAt: -1 },
        });
      }

      pipeline.push(
        { $skip: skip },
        { $limit: limit },
        {
          $project: {
            username: 1,
            email: 1,
            role: 1,
            isActive: 1,
            createdAt: 1,
            interests: 1,
            commonInterests: { $ifNull: ['$commonInterests', 0] },
          },
        }
      );

      const [users, totalPipeline] = await Promise.all([
        User.aggregate(pipeline),
        User.aggregate([
          {
            $match: {
              _id: { $ne: new Types.ObjectId(currentUserId) },
              isActive: true,
            },
          },
          { $count: 'total' },
        ]),
      ]);

      const total = totalPipeline[0]?.total || 0;
      const pagination = {
        page,
        limit,
        total,
        hasNext: skip + limit < total,
        hasPrev: page > 1,
      };

      logger.info('User suggestions generated', {
        resultCount: users.length,
        total,
        page,
        userHasInterests: !!(currentUser.interests && currentUser.interests.length > 0),
      });

      return { users, pagination };
    } catch (error) {
      logger.error('User suggestions failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        currentUserId,
        options,
      });
      throw new AppError('Failed to get user suggestions', 500, 'SUGGESTIONS_ERROR');
    }
  }

  /**
   * Get user details by ID (for profile viewing)
   */
  public async getUserById(
    userId: string,
    requestId?: string | undefined
  ): Promise<UserSearchResult | null> {
    const logger = createLogger(requestId);

    try {
      const user = await User.findById(userId)
        .select('username email role isActive createdAt interests')
        .lean<UserSearchResult>();

      if (!user) {
        logger.warn('User not found', { userId });
        return null;
      }

      logger.info('User retrieved', { userId, username: user.username });
      return user;
    } catch (error) {
      logger.error('Get user by ID failed', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to get user', 500, 'GET_USER_ERROR');
    }
  }
}

export const userSearchService = UserSearchService.getInstance();
