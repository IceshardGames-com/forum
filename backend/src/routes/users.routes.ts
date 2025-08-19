import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateQuery, validateParams } from '../middlewares/validate';
import {
  searchUsers,
  getUserSuggestions,
  getUserById,
  getUsersByRole,
} from '../controllers/user/userSearch.controller';
import {
  searchUsersValidation,
  paginationValidation,
  userIdValidation,
  roleValidation,
} from '../validations/userSearch.validation';

const router = Router();

// All user routes require authentication
router.use(authenticate);

/**
 * User Search Routes
 */

// Search users by username/email
router.get('/search', validateQuery(searchUsersValidation), searchUsers);

// Get user suggestions based on interests
router.get('/suggestions', validateQuery(paginationValidation), getUserSuggestions);

// Get user profile by ID
router.get('/:userId', validateParams(userIdValidation), getUserById);

// Get users by role
router.get('/by-role/:role', 
  validateParams(roleValidation), 
  validateQuery(paginationValidation), 
  getUsersByRole
);

export default router;
