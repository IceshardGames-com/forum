import { Request, Response, NextFunction } from 'express';
import { jwtService, JwtPayload } from '../utils/jwt';
import { createLogger } from '../utils/logger';
import { User, IUser } from '../models/User';

// Extend Request interface to include user and decoded token
declare global {
  namespace Express {
    interface Request {
      user?: IUser;
      token?: JwtPayload;
      id?: string;
    }
  }
}

/**
 * Custom error class for authentication errors
 */
export class AuthenticationError extends Error {
  public statusCode: number;
  public code: string;

  constructor(message: string, code: string = 'AUTHENTICATION_FAILED') {
    super(message);
    this.name = 'AuthenticationError';
    this.statusCode = 401;
    this.code = code;
  }
}

/**
 * Middleware to authenticate requests using JWT tokens
 */
export const authenticate = async (
  req: Request,
  _res: Response,
  next: NextFunction
): Promise<void> => {
  const logger = createLogger(req.id);

  try {
    // Extract token from Authorization header
    const authHeader = req.headers.authorization;
    const token = jwtService.extractTokenFromHeader(authHeader);

    if (!token) {
      logger.warn('Authentication failed: No token provided', {
        path: req.path,
        method: req.method,
      });
      
      throw new AuthenticationError('Access token is required', 'TOKEN_MISSING');
    }

    // Verify the token
    let decoded: JwtPayload;
    try {
      decoded = jwtService.verifyToken(token);
    } catch (error) {
      logger.warn('Authentication failed: Invalid token', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });

      const errorMessage = error instanceof Error ? error.message : 'Invalid token';
      throw new AuthenticationError(errorMessage, 'TOKEN_INVALID');
    }

    // Find the user in the database
    const user = await User.findById(decoded.userId).select('+password');
    
    if (!user) {
      logger.warn('Authentication failed: User not found', {
        userId: decoded.userId,
      });
      
      throw new AuthenticationError('User not found', 'USER_NOT_FOUND');
    }

    // Check if user is active
    if (!user.isActive) {
      logger.warn('Authentication failed: Account deactivated', {
        userId: user._id,
      });
      
      throw new AuthenticationError('Account is deactivated', 'ACCOUNT_DEACTIVATED');
    }

    // Attach user and token to request object
    req.user = user;
    req.token = decoded;

    next();
  } catch (error) {
    if (error instanceof AuthenticationError) {
      return next(error);
    }
    
    logger.error('Unexpected error during authentication', {
      error: error instanceof Error ? error.message : 'Unknown error',
      path: req.path,
      method: req.method,
    });
    
    next(new AuthenticationError('Authentication failed', 'AUTHENTICATION_ERROR'));
  }
};

/**
 * Optional authentication middleware - doesn't fail if no token is provided
 * Useful for endpoints that can work with or without authentication
 */
export const optionalAuthenticate = async (
  req: Request,
  _res: Response,
  next: NextFunction
): Promise<void> => {
  const logger = createLogger(req.id);

  try {
    const authHeader = req.headers.authorization;
    const token = jwtService.extractTokenFromHeader(authHeader);

    // If no token provided, continue without authentication
    if (!token) {
      return next();
    }

    // Try to verify the token
    try {
      const decoded = jwtService.verifyToken(token);
      const user = await User.findById(decoded.userId);

      if (user && user.isActive) {
        req.user = user;
        req.token = decoded;
      }
    } catch (error) {
      // Silently continue without authentication for optional auth
    }

    next();
  } catch (error) {
    logger.error('Unexpected error during optional authentication', {
      error: error instanceof Error ? error.message : 'Unknown error',
    });
    
    // For optional auth, continue even on unexpected errors
    next();
  }
};

/**
 * Middleware to ensure user is authenticated (alias for authenticate)
 */
export const requireAuth = authenticate;

/**
 * Middleware to ensure user is logged in (alias for authenticate)
 */
export const ensureAuthenticated = authenticate;

export default authenticate; 