import Joi from 'joi';

const objectId = Joi.string().length(24).hex();

export const sendRequestValidation = Joi.object({
  recipientId: objectId.required(),
}).options({ stripUnknown: true, abortEarly: false });

export const blockUserValidation = Joi.object({
  userId: objectId.required(),
}).options({ stripUnknown: true, abortEarly: false });

export const listValidation = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
}).options({ stripUnknown: true, abortEarly: false });


