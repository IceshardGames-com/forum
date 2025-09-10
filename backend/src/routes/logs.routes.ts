import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { requireDeveloper } from '../middlewares/admin';
import { validateQuery } from '../middlewares/validate';
import { logsQueryValidation } from '../validations/logs.validation';
import { listLogs } from '../controllers/admin/logs.controller';
import rateLimit from 'express-rate-limit';

const router = Router();

router.use(authenticate);
router.use(requireDeveloper); // only developers/admins can read logs

// Route-level rate limit for logs API
router.use(rateLimit({ windowMs: 60 * 1000, max: 20, standardHeaders: true, legacyHeaders: false }));

router.get('/', validateQuery(logsQueryValidation), listLogs);

export default router;

