import { Request, Response } from 'express';
import { authService } from '../services/auth.service';
import { createLogger } from '../utils/logger';
import { asyncErrorHandler } from '../middlewares/errorHandler';

/**
 * Interface for standardized API response
 */
interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

/**
 * Authentication controller class
 */
export class AuthController {
  private static instance: AuthController;

  private constructor() {}

  public static getInstance(): AuthController {
    if (!AuthController.instance) {
      AuthController.instance = new AuthController();
    }
    return AuthController.instance;
  }

  /**
   * Handles user registration
   * POST /api/auth/register
   */
  public register = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    const logger = createLogger(req.id);

    const { username, email, password, role } = req.body;

    // Call auth service
    const result = await authService.register(
      { username, email, password, role },
      req.id
    );

    // Prepare response
    const response: ApiResponse = {
      success: true,
      data: {
        user: result.data.user,
        tokens: result.data.tokens,
      },
      message: result.message,
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    logger.info('User registration completed', {
      userId: result.data.user._id,
      username: result.data.user.username,
    });

    res.status(201).json(response);
  });

  /**
   * Handles user login
   * POST /api/auth/login
   */
  public login = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    const logger = createLogger(req.id);

    const { email, username, password, rememberMe } = req.body;

    // Call auth service
    const result = await authService.login(
      { email, username, password, rememberMe },
      req.id
    );

    // Prepare response
    const response: ApiResponse = {
      success: true,
      data: {
        user: result.data.user,
        tokens: result.data.tokens,
      },
      message: result.message,
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    logger.info('User login completed', {
      userId: result.data.user._id,
      username: result.data.user.username,
    });

    res.status(200).json(response);
  });

  /**
   * Handles user logout
   * POST /api/auth/logout
   */
  public logout = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    // Call auth service
    const result = await authService.logout(req.user!, req.id);

    // Prepare response
    const response: ApiResponse = {
      success: true,
      message: result.message,
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    res.status(200).json(response);
  });

  /**
   * Gets user profile
   * GET /api/auth/profile
   */
  public getProfile = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    // Call auth service
    const user = await authService.getProfile(req.user!._id, req.id);

    // Prepare response
    const response: ApiResponse = {
      success: true,
      data: { user },
      message: 'Profile retrieved successfully',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    res.status(200).json(response);
  });

  /**
   * Updates user profile
   * PUT /api/auth/profile
   */
  public updateProfile = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    const logger = createLogger(req.id);

    const { username, email } = req.body;

    // Call auth service
    const user = await authService.updateProfile(
      req.user!._id,
      { username, email },
      req.id
    );

    // Prepare response
    const response: ApiResponse = {
      success: true,
      data: { user },
      message: 'Profile updated successfully',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    logger.info('Profile updated', {
      userId: user._id,
      username: user.username,
    });

    res.status(200).json(response);
  });

  /**
   * Changes user password
   * PUT /api/auth/change-password
   */
  public changePassword = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    const logger = createLogger(req.id);

    const { currentPassword, newPassword } = req.body;

    // Call auth service
    const result = await authService.changePassword(
      req.user!._id,
      currentPassword,
      newPassword,
      req.id
    );

    // Prepare response
    const response: ApiResponse = {
      success: true,
      message: result.message,
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    logger.info('Password changed', {
      userId: req.user?._id,
    });

    res.status(200).json(response);
  });

  /**
   * Validates authentication token
   * GET /api/auth/validate
   */
  public validateToken = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    // If we reach here, the auth middleware has already validated the token
    const response: ApiResponse = {
      success: true,
      data: {
        user: req.user!.toJSON(),
        token: req.token,
      },
      message: 'Token is valid',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    res.status(200).json(response);
  });

  /**
   * Health check for auth service
   * GET /api/auth/health
   */
  public healthCheck = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
    const response: ApiResponse = {
      success: true,
      data: {
        service: 'auth',
        status: 'healthy',
        uptime: process.uptime(),
        timestamp: new Date().toISOString(),
      },
      message: 'Auth service is healthy',
      requestId: req.id,
      timestamp: new Date().toISOString(),
    };

    res.status(200).json(response);
  });
}

// Export singleton instance
export const authController = AuthController.getInstance();

// Export individual methods for use in routes
export const {
  register,
  login,
  logout,
  getProfile,
  updateProfile,
  changePassword,
  validateToken,
  healthCheck,
} = authController;

export default authController; 