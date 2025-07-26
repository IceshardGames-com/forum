import { Request, Response, NextFunction } from 'express';
import { createLogger } from '../utils/logger';
import { UserRole } from '../models/User';

/**
 * Custom error class for authorization errors
 */
export class AuthorizationError extends Error {
  public statusCode: number;
  public code: string;

  constructor(message: string, code: string = 'AUTHORIZATION_FAILED') {
    super(message);
    this.name = 'AuthorizationError';
    this.statusCode = 403;
    this.code = code;
  }
}

/**
 * Middleware to ensure user has admin privileges
 */
export const requireAdmin = (
  req: Request,
  _res: Response,
  next: NextFunction
): void => {
  const logger = createLogger(req.id);

  try {
    // Check if user is authenticated
    if (!req.user) {
      logger.warn('Admin access denied: User not authenticated');
      throw new AuthorizationError('Authentication required', 'AUTHENTICATION_REQUIRED');
    }

    // Check if user has admin role
    if (req.user.role !== UserRole.ADMIN) {
      logger.warn('Admin access denied: Insufficient privileges', {
        userId: req.user._id,
        userRole: req.user.role,
      });
      
      throw new AuthorizationError('Admin privileges required', 'ADMIN_REQUIRED');
    }

    next();
  } catch (error) {
    next(error);
  }
};

/**
 * Middleware to ensure user has developer or admin privileges
 */
export const requireDeveloper = (
  req: Request,
  _res: Response,
  next: NextFunction
): void => {
  const logger = createLogger(req.id);

  try {
    // Check if user is authenticated
    if (!req.user) {
      logger.warn('Developer access denied: User not authenticated');
      throw new AuthorizationError('Authentication required', 'AUTHENTICATION_REQUIRED');
    }

    // Check if user has developer or admin role
    if (req.user.role !== UserRole.DEVELOPER && req.user.role !== UserRole.ADMIN) {
      logger.warn('Developer access denied: Insufficient privileges', {
        userId: req.user._id,
        userRole: req.user.role,
      });
      
      throw new AuthorizationError('Developer privileges required', 'DEVELOPER_REQUIRED');
    }

    next();
  } catch (error) {
    next(error);
  }
};

/**
 * Middleware to ensure user has elevated privileges (developer or admin)
 * Alias for requireDeveloper
 */
export const requireElevated = requireDeveloper;

/**
 * Middleware to ensure user has privileged access (developer or admin)
 * Alias for requireDeveloper
 */
export const requirePrivileged = requireDeveloper;

/**
 * Middleware to check if user has specific role(s)
 */
export const requireRole = (allowedRoles: UserRole[]) => {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const logger = createLogger(req.id);

    try {
      // Check if user is authenticated
      if (!req.user) {
        logger.warn('Role-based access denied: User not authenticated', {
          requiredRoles: allowedRoles,
        });
        
        throw new AuthorizationError('Authentication required', 'AUTHENTICATION_REQUIRED');
      }

      // Check if user has one of the allowed roles
      if (!allowedRoles.includes(req.user.role)) {
        logger.warn('Role-based access denied: Insufficient privileges', {
          userId: req.user._id,
          userRole: req.user.role,
          requiredRoles: allowedRoles,
        });
        
        throw new AuthorizationError(
          `One of these roles required: ${allowedRoles.join(', ')}`,
          'ROLE_REQUIRED'
        );
      }

      next();
    } catch (error) {
      next(error);
    }
  };
};

/**
 * Middleware to check if user owns the resource or has elevated privileges
 * Requires a function to extract the resource owner ID from the request
 */
export const requireOwnershipOrElevated = (
  getResourceOwnerId: (req: Request) => string | undefined
) => {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const logger = createLogger(req.id);

    try {
      // Check if user is authenticated
      if (!req.user) {
        logger.warn('Ownership access denied: User not authenticated');
        throw new AuthorizationError('Authentication required', 'AUTHENTICATION_REQUIRED');
      }

      // Check if user has elevated privileges
      if (req.user.role === UserRole.ADMIN || req.user.role === UserRole.DEVELOPER) {
        return next();
      }

      // Check ownership
      const resourceOwnerId = getResourceOwnerId(req);
      
      if (!resourceOwnerId) {
        logger.warn('Ownership access denied: Cannot determine resource owner', {
          userId: req.user._id,
        });
        
        throw new AuthorizationError('Cannot determine resource ownership', 'OWNERSHIP_UNKNOWN');
      }

      if (req.user._id.toString() !== resourceOwnerId) {
        logger.warn('Ownership access denied: User does not own resource', {
          userId: req.user._id,
          resourceOwnerId,
        });
        
        throw new AuthorizationError('Access denied: Resource ownership required', 'OWNERSHIP_REQUIRED');
      }

      next();
    } catch (error) {
      next(error);
    }
  };
};

/**
 * Utility function to get user ID from request params
 */
export const getUserIdFromParams = (req: Request): string | undefined => {
  return req.params.userId || req.params.id;
};

/**
 * Utility function to get user ID from request body
 */
export const getUserIdFromBody = (req: Request): string | undefined => {
  return req.body.userId || req.body.id;
};

// Export commonly used middleware combinations
export const adminMiddleware = requireAdmin;
export const developerMiddleware = requireDeveloper;

// Export default
export default requireAdmin; 