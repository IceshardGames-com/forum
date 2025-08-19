import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';
import { userSearchService, UserSearchOptions } from '../../services/userSearch.service';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

/**
 * Search users by username or email
 * GET /api/users/search?q=username&page=1&limit=20&role=gamer&includeInactive=false
 */
export const searchUsers = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  
  // Extract query parameters
  const query = (req.query.q as string) || '';
  const page = parseInt((req.query.page as string) || '1', 10);
  const limit = parseInt((req.query.limit as string) || '20', 10);
  const role = req.query.role as string;
  const includeInactive = req.query.includeInactive === 'true';

  // Validate query parameter
  if (!query || query.trim().length < 2) {
    res.status(400).json({
      success: false,
      message: 'Search query must be at least 2 characters long',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  const options: UserSearchOptions = {
    page,
    limit,
    role: role || undefined,
    includeInactive,
  };

  const result = await userSearchService.searchUsers(
    query,
    req.user!._id,
    options,
    req.id || undefined
  );

  logger.info('User search completed', {
    query,
    resultCount: result.users.length,
    total: result.pagination.total,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      users: result.users,
      pagination: result.pagination,
      query,
      filters: {
        role,
        includeInactive,
      },
    },
    message: 'Users retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

/**
 * Get user suggestions based on interests and activity
 * GET /api/users/suggestions?page=1&limit=20
 */
export const getUserSuggestions = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  
  const page = parseInt((req.query.page as string) || '1', 10);
  const limit = parseInt((req.query.limit as string) || '20', 10);

  const options = {
    page,
    limit,
  };

  const result = await userSearchService.getUserSuggestions(
    req.user!._id,
    options,
    req.id || undefined
  );

  logger.info('User suggestions generated', {
    resultCount: result.users.length,
    total: result.pagination.total,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      users: result.users,
      pagination: result.pagination,
    },
    message: 'User suggestions retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

/**
 * Get user profile by ID
 * GET /api/users/:userId
 */
export const getUserById = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const { userId } = req.params;

  // Validate userId format (basic check)
  if (!userId || userId.length !== 24) {
    res.status(400).json({
      success: false,
      message: 'Invalid user ID format',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  const user = await userSearchService.getUserById(userId, req.id || undefined);

  if (!user) {
    res.status(404).json({
      success: false,
      message: 'User not found',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  // Remove sensitive information for non-self requests
  const sanitizedUser = {
    ...user,
    email: user._id === req.user!._id ? user.email : undefined, // Only show email to the user themselves
  };

  logger.info('User profile retrieved', {
    userId,
    username: user.username,
    requestedBy: req.user!._id,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      user: sanitizedUser,
    },
    message: 'User profile retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

/**
 * Get users by role
 * GET /api/users/by-role/:role?page=1&limit=20
 */
export const getUsersByRole = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  
  const { role } = req.params;
  const page = parseInt((req.query.page as string) || '1', 10);
  const limit = parseInt((req.query.limit as string) || '20', 10);

  // Validate role
  const validRoles = ['gamer', 'developer', 'admin'];
  if (!role || !validRoles.includes(role)) {
    res.status(400).json({
      success: false,
      message: `Invalid role. Must be one of: ${validRoles.join(', ')}`,
      requestId: req.id,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  const options: UserSearchOptions = {
    page,
    limit,
    role: role || undefined,
    includeInactive: false, // Only active users by default
  };

  const result = await userSearchService.searchUsers(
    '', // Empty query to get all users of the role
    req.user!._id,
    options,
    req.id || undefined
  );

  logger.info('Users retrieved by role', {
    role,
    resultCount: result.users.length,
    total: result.pagination.total,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      users: result.users,
      pagination: result.pagination,
      role,
    },
    message: `${role} users retrieved successfully`,
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
