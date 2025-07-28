import Joi from 'joi';
import { UserRole } from '../models/User';

// Password validation requirements
const passwordSchema = Joi.string()
  .min(8)
  .max(128)
  .pattern(new RegExp('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]'))
  .required()
  .messages({
    'string.min': 'Password must be at least 8 characters long',
    'string.max': 'Password cannot exceed 128 characters',
    'string.pattern.base': 
      'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)',
    'any.required': 'Password is required',
  });

// Username validation
const usernameSchema = Joi.string()
  .min(3)
  .max(30)
  .pattern(new RegExp('^[a-zA-Z0-9_]+$'))
  .required()
  .messages({
    'string.min': 'Username must be at least 3 characters long',
    'string.max': 'Username cannot exceed 30 characters',
    'string.pattern.base': 'Username can only contain letters, numbers, and underscores',
    'any.required': 'Username is required',
  });

// Email validation
const emailSchema = Joi.string()
  .email({ tlds: { allow: false } }) // Allow all TLDs
  .max(255)
  .required()
  .messages({
    'string.email': 'Please provide a valid email address',
    'string.max': 'Email cannot exceed 255 characters',
    'any.required': 'Email is required',
  });

// Role validation
const roleSchema = Joi.string()
  .valid(...Object.values(UserRole))
  .default(UserRole.GAMER)
  .messages({
    'any.only': `Role must be one of: ${Object.values(UserRole).join(', ')}`,
  });

/**
 * Validation schema for user registration
 */
export const registerValidation = Joi.object({
  username: usernameSchema,
  email: emailSchema,
  password: passwordSchema,
  confirmPassword: Joi.string()
    .valid(Joi.ref('password'))
    .required()
    .messages({
      'any.only': 'Password confirmation does not match password',
      'any.required': 'Password confirmation is required',
    }),
  role: roleSchema,
}).options({
  stripUnknown: true, // Remove unknown fields
  abortEarly: false, // Return all validation errors, not just the first one
});

/**
 * Validation schema for user login
 */
export const loginValidation = Joi.object({
  // Allow login with either email or username
  email: Joi.string()
    .email({ tlds: { allow: false } })
    .max(255)
    .messages({
      'string.email': 'Please provide a valid email address',
      'string.max': 'Email cannot exceed 255 characters',
    }),
  username: Joi.string()
    .min(3)
    .max(30)
    .pattern(new RegExp('^[a-zA-Z0-9_]+$'))
    .messages({
      'string.min': 'Username must be at least 3 characters long',
      'string.max': 'Username cannot exceed 30 characters',
      'string.pattern.base': 'Username can only contain letters, numbers, and underscores',
    }),
  password: Joi.string()
    .min(1)
    .max(128)
    .required()
    .messages({
      'string.min': 'Password is required',
      'string.max': 'Password cannot exceed 128 characters',
      'any.required': 'Password is required',
    }),
  rememberMe: Joi.boolean()
    .default(false)
    .messages({
      'boolean.base': 'Remember me must be a boolean value',
    }),
})
  .or('email', 'username') // At least one of email or username must be provided
  .messages({
    'object.missing': 'Either email or username is required',
  })
  .options({
    stripUnknown: true,
    abortEarly: false,
  });

/**
 * Validation schema for logout (token-based)
 */
export const logoutValidation = Joi.object({
  refreshToken: Joi.string()
    .optional()
    .messages({
      'string.base': 'Refresh token must be a string',
    }),
}).options({
  stripUnknown: true,
  abortEarly: false,
});

/**
 * Validation schema for password change
 */
export const changePasswordValidation = Joi.object({
  currentPassword: Joi.string()
    .min(1)
    .max(128)
    .required()
    .messages({
      'string.min': 'Current password is required',
      'string.max': 'Current password cannot exceed 128 characters',
      'any.required': 'Current password is required',
    }),
  newPassword: passwordSchema.label('New password'),
  confirmPassword: Joi.string()
    .valid(Joi.ref('newPassword'))
    .required()
    .messages({
      'any.only': 'Password confirmation does not match new password',
      'any.required': 'Password confirmation is required',
    }),
}).options({
  stripUnknown: true,
  abortEarly: false,
});

/**
 * Validation schema for password reset request
 */
export const forgotPasswordValidation = Joi.object({
  email: emailSchema,
}).options({
  stripUnknown: true,
  abortEarly: false,
});

/**
 * Validation schema for password reset
 */
export const resetPasswordValidation = Joi.object({
  token: Joi.string()
    .required()
    .messages({
      'any.required': 'Reset token is required',
    }),
  password: passwordSchema,
  confirmPassword: Joi.string()
    .valid(Joi.ref('password'))
    .required()
    .messages({
      'any.only': 'Password confirmation does not match password',
      'any.required': 'Password confirmation is required',
    }),
}).options({
  stripUnknown: true,
  abortEarly: false,
});

/**
 * Validation schema for user profile update
 */
export const updateProfileValidation = Joi.object({
  username: usernameSchema.optional(),
  email: emailSchema.optional(),
  // Don't allow role updates through profile update
}).min(1) // At least one field must be provided
  .options({
    stripUnknown: true,
    abortEarly: false,
  });

/**
 * Validation schema for admin role assignment
 */
export const assignRoleValidation = Joi.object({
  userId: Joi.string()
    .required()
    .messages({
      'any.required': 'User ID is required',
    }),
  role: Joi.string()
    .valid(...Object.values(UserRole))
    .required()
    .messages({
      'any.only': `Role must be one of: ${Object.values(UserRole).join(', ')}`,
      'any.required': 'Role is required',
    }),
}).options({
  stripUnknown: true,
  abortEarly: false,
});

// Export all validation schemas
export const authValidations = {
  register: registerValidation,
  login: loginValidation,
  logout: logoutValidation,
  changePassword: changePasswordValidation,
  forgotPassword: forgotPasswordValidation,
  resetPassword: resetPasswordValidation,
  updateProfile: updateProfileValidation,
  assignRole: assignRoleValidation,
}; 