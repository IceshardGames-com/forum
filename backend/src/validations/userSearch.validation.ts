import Joi from 'joi';

const objectId = Joi.string().length(24).hex();

/**
 * Validation for user search query parameters
 */
export const searchUsersValidation = Joi.object({
  q: Joi.string()
    .min(2)
    .max(50)
    .trim()
    .required()
    .messages({
      'string.min': 'Search query must be at least 2 characters long',
      'string.max': 'Search query cannot exceed 50 characters',
      'any.required': 'Search query is required',
    }),
  page: Joi.number()
    .integer()
    .min(1)
    .default(1)
    .messages({
      'number.base': 'Page must be a number',
      'number.integer': 'Page must be an integer',
      'number.min': 'Page must be at least 1',
    }),
  limit: Joi.number()
    .integer()
    .min(1)
    .max(50)
    .default(20)
    .messages({
      'number.base': 'Limit must be a number',
      'number.integer': 'Limit must be an integer',
      'number.min': 'Limit must be at least 1',
      'number.max': 'Limit cannot exceed 50',
    }),
  role: Joi.string()
    .valid('gamer', 'developer', 'admin')
    .optional()
    .messages({
      'any.only': 'Role must be one of: gamer, developer, admin',
    }),
  includeInactive: Joi.boolean()
    .default(false)
    .messages({
      'boolean.base': 'includeInactive must be a boolean',
    }),
}).options({ stripUnknown: true, abortEarly: false });

/**
 * Validation for pagination parameters (for suggestions and other endpoints)
 */
export const paginationValidation = Joi.object({
  page: Joi.number()
    .integer()
    .min(1)
    .default(1)
    .messages({
      'number.base': 'Page must be a number',
      'number.integer': 'Page must be an integer',
      'number.min': 'Page must be at least 1',
    }),
  limit: Joi.number()
    .integer()
    .min(1)
    .max(50)
    .default(20)
    .messages({
      'number.base': 'Limit must be a number',
      'number.integer': 'Limit must be an integer',
      'number.min': 'Limit must be at least 1',
      'number.max': 'Limit cannot exceed 50',
    }),
}).options({ stripUnknown: true, abortEarly: false });

/**
 * Validation for user ID parameter
 */
export const userIdValidation = Joi.object({
  userId: objectId.required().messages({
    'string.length': 'User ID must be a valid 24-character ObjectId',
    'string.hex': 'User ID must be a valid hexadecimal string',
    'any.required': 'User ID is required',
  }),
}).options({ stripUnknown: true, abortEarly: false });

/**
 * Validation for role parameter
 */
export const roleValidation = Joi.object({
  role: Joi.string()
    .valid('gamer', 'developer', 'admin')
    .required()
    .messages({
      'any.only': 'Role must be one of: gamer, developer, admin',
      'any.required': 'Role is required',
    }),
}).options({ stripUnknown: true, abortEarly: false });

/**
 * Combined validation for search with query parameters
 */
export const searchQueryValidation = Joi.object({
  // Query params
  q: Joi.string()
    .min(2)
    .max(50)
    .trim()
    .required(),
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
  role: Joi.string().valid('gamer', 'developer', 'admin').optional(),
  includeInactive: Joi.boolean().default(false),
}).options({ stripUnknown: true, abortEarly: false });
