import { Router } from 'express';
import { listInterests } from '../controllers/interest.controller';
import { validateQuery } from '../middlewares/validate';
import Joi from 'joi';

const router = Router();

const listQuerySchema = Joi.object({
  q: Joi.string().max(100).optional(),
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(50).default(20),
}).options({ stripUnknown: true, abortEarly: false });

router.get('/', validateQuery(listQuerySchema), listInterests);

export default router;


