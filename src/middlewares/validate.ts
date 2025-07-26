import { Request, Response, NextFunction } from 'express';
import Joi from 'joi';
import { createLogger } from '../utils/logger';

// Extend Request interface to include request ID
declare global {
  namespace Express {
    interface Request {
      id?: string;
    }
  }
}

/**
 * Validation targets enum
 */
export enum ValidationTarget {
  BODY = 'body',
  QUERY = 'query',
  PARAMS = 'params',
  HEADERS = 'headers',
}

/**
 * Interface for validation options
 */
export interface ValidationOptions {
  target?: ValidationTarget;
  allowUnknown?: boolean;
  abortEarly?: boolean;
  stripUnknown?: boolean;
}

/**
 * Custom error class for validation errors
 */
export class ValidationError extends Error {
  public statusCode: number;
  public details: Joi.ValidationErrorItem[];

  constructor(message: string, details: Joi.ValidationErrorItem[]) {
    super(message);
    this.name = 'ValidationError';
    this.statusCode = 400;
    this.details = details;
  }
}

/**
 * Middleware to validate request data against Joi schema
 */
export const validate = (
  schema: Joi.ObjectSchema,
  options: ValidationOptions = {}
) => {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const logger = createLogger(req.id);
    
    const {
      target = ValidationTarget.BODY,
      allowUnknown = false,
      abortEarly = false,
      stripUnknown = true,
    } = options;

    // Get the data to validate based on target
    let dataToValidate: any;
    switch (target) {
      case ValidationTarget.BODY:
        dataToValidate = req.body;
        break;
      case ValidationTarget.QUERY:
        dataToValidate = req.query;
        break;
      case ValidationTarget.PARAMS:
        dataToValidate = req.params;
        break;
      case ValidationTarget.HEADERS:
        dataToValidate = req.headers;
        break;
      default:
        dataToValidate = req.body;
    }

    // Validation options
    const validationOptions: Joi.ValidationOptions = {
      allowUnknown,
      abortEarly,
      stripUnknown,
    };

    // Perform validation
    const { error, value } = schema.validate(dataToValidate, validationOptions);

    if (error) {
      logger.warn('Validation failed', {
        target,
        errors: error.details.map(detail => ({
          field: detail.path.join('.'),
          message: detail.message,
        })),
      });

      // Create a user-friendly error message
      const errorMessage = error.details && error.details.length === 1 
        ? error.details[0]?.message || 'Validation failed'
        : 'Validation failed';

      // Throw validation error that will be caught by error handler
      const validationError = new ValidationError(errorMessage, error.details);
      return next(validationError);
    }

    // Replace the original data with validated/sanitized data
    if (value !== undefined) {
      switch (target) {
        case ValidationTarget.BODY:
          req.body = value;
          break;
        case ValidationTarget.QUERY:
          req.query = value;
          break;
        case ValidationTarget.PARAMS:
          req.params = value;
          break;
        case ValidationTarget.HEADERS:
          // Don't replace headers as it might break other functionality
          break;
      }
    }

    next();
  };
};

/**
 * Convenience function for body validation
 */
export const validateBody = (schema: Joi.ObjectSchema, options?: Omit<ValidationOptions, 'target'>) => {
  return validate(schema, { ...options, target: ValidationTarget.BODY });
};

/**
 * Convenience function for query validation
 */
export const validateQuery = (schema: Joi.ObjectSchema, options?: Omit<ValidationOptions, 'target'>) => {
  return validate(schema, { ...options, target: ValidationTarget.QUERY });
};

/**
 * Convenience function for params validation
 */
export const validateParams = (schema: Joi.ObjectSchema, options?: Omit<ValidationOptions, 'target'>) => {
  return validate(schema, { ...options, target: ValidationTarget.PARAMS });
};

/**
 * Convenience function for headers validation
 */
export const validateHeaders = (schema: Joi.ObjectSchema, options?: Omit<ValidationOptions, 'target'>) => {
  return validate(schema, { ...options, target: ValidationTarget.HEADERS });
};

/**
 * Creates a validation middleware for multiple targets
 */
export const validateMultiple = (validations: Array<{
  schema: Joi.ObjectSchema;
  target: ValidationTarget;
  options?: ValidationOptions;
}>) => {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const logger = createLogger(req.id);
    
    try {
      for (const validation of validations) {
        const { schema, target, options = {} } = validation;
        
        let dataToValidate: any;
        switch (target) {
          case ValidationTarget.BODY:
            dataToValidate = req.body;
            break;
          case ValidationTarget.QUERY:
            dataToValidate = req.query;
            break;
          case ValidationTarget.PARAMS:
            dataToValidate = req.params;
            break;
          case ValidationTarget.HEADERS:
            dataToValidate = req.headers;
            break;
        }

        const validationOptions: Joi.ValidationOptions = {
          allowUnknown: options.allowUnknown || false,
          abortEarly: options.abortEarly || false,
          stripUnknown: options.stripUnknown !== undefined ? options.stripUnknown : true,
        };

        const { error, value } = schema.validate(dataToValidate, validationOptions);

        if (error) {
          logger.warn('Multi-validation failed', {
            target,
            errors: error.details.map(detail => ({
              field: detail.path.join('.'),
              message: detail.message,
            })),
          });

          const validationError = new ValidationError(
            `Validation failed for ${target}`,
            error.details
          );
          return next(validationError);
        }

        // Update the request object with validated data
        switch (target) {
          case ValidationTarget.BODY:
            req.body = value;
            break;
          case ValidationTarget.QUERY:
            req.query = value;
            break;
          case ValidationTarget.PARAMS:
            req.params = value;
            break;
        }
      }

      next();
    } catch (error) {
      logger.error('Unexpected error during validation', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
      next(error);
    }
  };
};

export default validate; 