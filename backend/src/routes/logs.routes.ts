import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { requireDeveloper } from '../middlewares/admin';
import { validateQuery } from '../middlewares/validate';
import { logsQueryValidation } from '../validations/logs.validation';
import { listLogs } from '../controllers/admin/logs.controller';

const router = Router();

router.use(authenticate);
router.use(requireDeveloper); // only developers/admins can read logs

router.get('/', validateQuery(logsQueryValidation), listLogs);

export default router;

