import Joi from 'joi';

export const logsQueryValidation = Joi.object({
  file: Joi.string().valid('combined', 'error').default('combined'),
  level: Joi.string().valid('error', 'warn', 'info', 'http', 'debug').optional(),
  q: Joi.string().min(1).max(256).optional(),
  requestId: Joi.string().min(1).max(128).optional(),
  from: Joi.date().iso().optional(),
  to: Joi.date().iso().optional(),
  order: Joi.string().valid('asc', 'desc').default('desc'),
  limit: Joi.number().integer().min(1).max(500).default(100),
}).options({ stripUnknown: true, abortEarly: false });

export default { logsQueryValidation };

