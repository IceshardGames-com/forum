import { Request, Response, NextFunction } from 'express';
import { generateRequestId } from '../utils/uuid';
import { createLogger } from '../utils/logger';

// Extend Request interface to include request ID
declare global {
  namespace Express {
    interface Request {
      id?: string;
      startTime?: number;
    }
  }
}

/**
 * Interface for request logging options
 */
export interface RequestLoggerOptions {
  logBody?: boolean;
  logHeaders?: boolean;
  logQuery?: boolean;
  logParams?: boolean;
  logResponse?: boolean;
  skipSuccessfulGET?: boolean;
  skipPaths?: string[];
  sensitiveFields?: string[];
}

/**
 * Default configuration for request logging
 */
const defaultOptions: RequestLoggerOptions = {
  logBody: true,
  logHeaders: false,
  logQuery: true,
  logParams: true,
  logResponse: false,
  skipSuccessfulGET: false,
  skipPaths: ['/health', '/favicon.ico'],
  sensitiveFields: ['password', 'token', 'authorization', 'cookie'],
};

/**
 * Sanitizes sensitive data from objects
 */
const sanitizeData = (data: any, sensitiveFields: string[]): any => {
  if (!data || typeof data !== 'object') {
    return data;
  }

  const sanitized = { ...data };
  
  for (const field of sensitiveFields) {
    if (sanitized[field]) {
      sanitized[field] = '[REDACTED]';
    }
  }

  return sanitized;
};

/**
 * Determines if request should be skipped from logging
 */
const shouldSkipLogging = (
  req: Request,
  res: Response,
  options: RequestLoggerOptions
): boolean => {
  const { skipPaths, skipSuccessfulGET } = options;

  // Skip specified paths
  if (skipPaths && skipPaths.includes(req.path)) {
    return true;
  }

  // Skip successful GET requests if configured
  if (skipSuccessfulGET && req.method === 'GET' && res.statusCode < 400) {
    return true;
  }

  return false;
};

/**
 * Creates request logger middleware with custom options
 */
export const createRequestLogger = (options: Partial<RequestLoggerOptions> = {}) => {
  const config = { ...defaultOptions, ...options };

  return (req: Request, res: Response, next: NextFunction): void => {
    // Generate unique request ID
    req.id = generateRequestId();
    req.startTime = Date.now();

    const requestLogger = createLogger(req.id);

    // Prepare request data for logging
    const requestData: any = {
      requestId: req.id,
      method: req.method,
      path: req.path,
      url: req.url,
      userAgent: req.get('User-Agent'),
      ip: req.ip,
      userId: req.user?._id,
      username: req.user?.username,
    };

    // Add optional request data
    if (config.logQuery && Object.keys(req.query).length > 0) {
      requestData.query = sanitizeData(req.query, config.sensitiveFields || []);
    }

    if (config.logParams && Object.keys(req.params).length > 0) {
      requestData.params = req.params;
    }

    if (config.logHeaders) {
      requestData.headers = sanitizeData(req.headers, config.sensitiveFields || []);
    }

    if (config.logBody && req.body && Object.keys(req.body).length > 0) {
      requestData.body = sanitizeData(req.body, config.sensitiveFields || []);
    }

    // Log the incoming request
    requestLogger.http('Incoming request', requestData);

    // Capture the original res.end function
    const originalEnd = res.end;
    let responseLogged = false;

    // Override res.end to log response
    res.end = function(chunk?: any, encoding?: any): Response {
      if (!responseLogged) {
        responseLogged = true;
        
        const duration = req.startTime ? Date.now() - req.startTime : 0;
        
        // Check if we should skip logging this request
        if (!shouldSkipLogging(req, res, config)) {
          const responseData: any = {
            requestId: req.id,
            method: req.method,
            path: req.path,
            statusCode: res.statusCode,
            duration: `${duration}ms`,
            contentLength: res.get('Content-Length'),
            userId: req.user?._id,
            username: req.user?.username,
          };

          // Add response body if configured (be careful with large responses)
          if (config.logResponse && chunk && chunk.length < 1000) {
            try {
              responseData.responseBody = JSON.parse(chunk.toString());
            } catch (error) {
              // If not JSON, just include a substring
              responseData.responseBody = chunk.toString().substring(0, 200);
            }
          }

          // Log based on status code
          if (res.statusCode >= 500) {
            requestLogger.error('Request completed with server error', responseData);
          } else if (res.statusCode >= 400) {
            requestLogger.warn('Request completed with client error', responseData);
          } else {
            requestLogger.info('Request completed successfully', responseData);
          }
        }
      }

      // Call the original end function
      return originalEnd.call(this, chunk, encoding);
    };

    next();
  };
};

/**
 * Default request logger middleware
 */
export const requestLogger = createRequestLogger();

/**
 * Minimal request logger (only logs errors and warnings)
 */
export const minimalRequestLogger = createRequestLogger({
  logBody: false,
  logHeaders: false,
  logQuery: false,
  logParams: false,
  skipSuccessfulGET: true,
});

/**
 * Detailed request logger (logs everything)
 */
export const detailedRequestLogger = createRequestLogger({
  logBody: true,
  logHeaders: true,
  logQuery: true,
  logParams: true,
  logResponse: true,
  skipSuccessfulGET: false,
});

/**
 * Production request logger (minimal sensitive data)
 */
export const productionRequestLogger = createRequestLogger({
  logBody: false,
  logHeaders: false,
  logQuery: true,
  logParams: true,
  logResponse: false,
  skipSuccessfulGET: true,
  sensitiveFields: [
    'password',
    'token',
    'authorization',
    'cookie',
    'x-api-key',
    'jwt',
    'secret',
  ],
});

/**
 * Health check middleware that bypasses logging
 */
export const healthCheck = (req: Request, res: Response, next: NextFunction): void => {
  if (req.path === '/health' || req.path === '/ping') {
    res.status(200).json({
      status: 'ok',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: process.env.NODE_ENV || 'development',
    });
    return;
  }
  next();
};

export default requestLogger; 