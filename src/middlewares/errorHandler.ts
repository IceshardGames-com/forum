import { Request, Response, NextFunction } from 'express';
import { createLogger } from '../utils/logger';
import { ValidationError } from './validate';
import { AuthenticationError } from './auth';
import { AuthorizationError } from './admin';
import { envConfig } from '../config/env';
import mongoose from 'mongoose';

/**
 * Interface for standardized error response
 */
export interface ErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
    details?: any;
    requestId?: string | undefined;
    timestamp: string;
    path?: string;
    method?: string;
  };
}

/**
 * Interface for API error with additional properties
 */
export interface ApiError extends Error {
  statusCode?: number;
  code?: string;
  details?: any;
  isOperational?: boolean;
}

/**
 * Custom application error class
 */
export class AppError extends Error implements ApiError {
  public statusCode: number;
  public code: string;
  public details?: any;
  public isOperational: boolean;

  constructor(
    message: string,
    statusCode: number = 500,
    code: string = 'INTERNAL_ERROR',
    details?: any
  ) {
    super(message);
    this.name = 'AppError';
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
    this.isOperational = true;

    Error.captureStackTrace(this, this.constructor);
  }
}

/**
 * Handles Mongoose validation errors
 */
const handleMongooseValidationError = (error: mongoose.Error.ValidationError): ApiError => {
  const errors = Object.values(error.errors).map(err => ({
    field: err.path,
    message: err.message,
    value: (err as any).value,
  }));

  return new AppError(
    'Validation failed',
    400,
    'VALIDATION_ERROR',
    { errors }
  );
};

/**
 * Handles Mongoose duplicate key errors
 */
const handleMongoDuplicateKeyError = (error: any): ApiError => {
  const keyValue = error.keyValue || {};
  const field = Object.keys(keyValue)[0] || 'unknown';
  const value = keyValue[field];
  
  return new AppError(
    `Duplicate value for field: ${field}`,
    409,
    'DUPLICATE_ERROR',
    { field, value }
  );
};

/**
 * Handles Mongoose cast errors
 */
const handleMongoCastError = (error: mongoose.Error.CastError): ApiError => {
  return new AppError(
    `Invalid ${error.path}: ${error.value}`,
    400,
    'CAST_ERROR',
    { field: error.path, value: error.value, type: error.kind }
  );
};

/**
 * Handles JWT errors
 */
const handleJWTError = (_error: Error): ApiError => {
  return new AppError('Invalid token', 401, 'TOKEN_INVALID');
};

/**
 * Handles JWT expired errors
 */
const handleJWTExpiredError = (_error: Error): ApiError => {
  return new AppError('Token expired', 401, 'TOKEN_EXPIRED');
};

/**
 * Determines if error should be exposed to client
 */
const isOperationalError = (error: any): boolean => {
  if (error.isOperational !== undefined) {
    return error.isOperational;
  }

  // Consider these errors as operational (safe to expose)
  const operationalErrors = [
    ValidationError,
    AuthenticationError,
    AuthorizationError,
    AppError,
  ];

  return operationalErrors.some(ErrorClass => error instanceof ErrorClass);
};

/**
 * Formats error for client response
 */
const formatErrorResponse = (
  error: ApiError,
  req: Request,
  includeStack: boolean = false
): ErrorResponse => {
  const response: ErrorResponse = {
    success: false,
    error: {
      code: error.code || 'INTERNAL_ERROR',
      message: error.message || 'An error occurred',
      requestId: req.id,
      timestamp: new Date().toISOString(),
      path: req.path,
      method: req.method,
    },
  };

  // Add details for operational errors
  if (isOperationalError(error) && error.details) {
    response.error.details = error.details;
  }

  // Add stack trace in development
  if (includeStack && error.stack) {
    (response.error as any).stack = error.stack;
  }

  return response;
};

/**
 * Processes and converts various error types to ApiError
 */
const processError = (error: any): ApiError => {
  // Already processed API errors
  if (error instanceof AppError || 
      error instanceof ValidationError || 
      error instanceof AuthenticationError || 
      error instanceof AuthorizationError) {
    return error as ApiError;
  }

  // Mongoose errors
  if (error.name === 'ValidationError') {
    return handleMongooseValidationError(error);
  }

  if (error.code === 11000) {
    return handleMongoDuplicateKeyError(error);
  }

  if (error.name === 'CastError') {
    return handleMongoCastError(error);
  }

  // JWT errors
  if (error.name === 'JsonWebTokenError') {
    return handleJWTError(error);
  }

  if (error.name === 'TokenExpiredError') {
    return handleJWTExpiredError(error);
  }

  // Default to internal server error
  return new AppError(
    envConfig.NODE_ENV === 'production' ? 'Something went wrong' : error.message,
    500,
    'INTERNAL_ERROR'
  );
};

/**
 * Global error handling middleware
 */
export const errorHandler = (
  error: any,
  req: Request,
  res: Response,
  _next: NextFunction
): void => {
  const logger = createLogger(req.id);

  // Process the error
  const processedError = processError(error);
  const statusCode = processedError.statusCode || 500;

  // Log the error
  const logData = {
    requestId: req.id,
    error: {
      name: error.name,
      message: error.message,
      code: processedError.code,
      statusCode,
    },
    request: {
      method: req.method,
      path: req.path,
      url: req.url,
      userAgent: req.get('User-Agent'),
      ip: req.ip,
      userId: req.user?._id,
      username: req.user?.username,
    },
    stack: error.stack,
  };

  // Log based on severity
  if (statusCode >= 500) {
    logger.error('Internal server error', logData);
  } else if (statusCode >= 400) {
    logger.warn('Client error', logData);
  } else {
    logger.info('Request error', logData);
  }

  // Format response
  const includeStack = envConfig.NODE_ENV === 'development';
  const errorResponse = formatErrorResponse(processedError, req, includeStack);

  // Send error response
  res.status(statusCode).json(errorResponse);
};

/**
 * Handles 404 Not Found errors
 */
export const notFoundHandler = (req: Request, _res: Response, next: NextFunction): void => {
  const error = new AppError(
    `Route not found: ${req.method} ${req.path}`,
    404,
    'ROUTE_NOT_FOUND',
    {
      method: req.method,
      path: req.path,
      availableRoutes: [], // Could be populated with actual routes
    }
  );

  next(error);
};

/**
 * Async error wrapper to catch async errors and pass to error handler
 */
export const asyncErrorHandler = (fn: Function) => {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
};

/**
 * Creates an operational error
 */
export const createError = (
  message: string,
  statusCode: number = 400,
  code?: string,
  details?: any
): AppError => {
  return new AppError(message, statusCode, code, details);
};

/**
 * Creates a validation error
 */
export const createValidationError = (message: string, details?: any): AppError => {
  return new AppError(message, 400, 'VALIDATION_ERROR', details);
};

/**
 * Creates an authentication error
 */
export const createAuthError = (message: string = 'Authentication failed'): AppError => {
  return new AppError(message, 401, 'AUTHENTICATION_ERROR');
};

/**
 * Creates an authorization error
 */
export const createAuthorizationError = (message: string = 'Access denied'): AppError => {
  return new AppError(message, 403, 'AUTHORIZATION_ERROR');
};

/**
 * Creates a not found error
 */
export const createNotFoundError = (resource: string = 'Resource'): AppError => {
  return new AppError(`${resource} not found`, 404, 'NOT_FOUND');
};

/**
 * Creates a conflict error
 */
export const createConflictError = (message: string, details?: any): AppError => {
  return new AppError(message, 409, 'CONFLICT_ERROR', details);
};

// Export error classes and utilities
export {
  ValidationError,
  AuthenticationError,
  AuthorizationError,
};

export default errorHandler; 