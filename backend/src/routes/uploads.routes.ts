import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody } from '../middlewares/validate';
import Joi from 'joi';
import rateLimit from 'express-rate-limit';
import { presignR2Put, createImagesDirectUpload } from '../controllers/storage/upload.controller';

const router = Router();

router.use(authenticate);

const limiter = rateLimit({ windowMs: 60 * 1000, max: 60, standardHeaders: true, legacyHeaders: false });
router.use(limiter);

const presignSchema = Joi.object({
  contentType: Joi.string().min(1).required(),
  keyPrefix: Joi.string().min(1).max(128).default('uploads'),
}).options({ stripUnknown: true, abortEarly: false });

router.post('/r2/presign', validateBody(presignSchema), presignR2Put);

router.post('/images/direct-upload', createImagesDirectUpload);

export default router;

