import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authenticate } from '../middlewares/auth';
import { validateBody } from '../middlewares/validate';
import {
  registerValidation,
  loginValidation,
  logoutValidation,
  updateProfileValidation,
  changePasswordValidation,
} from '../validations/auth.validation';

const router = Router();

// Authentication routes
router.post('/register', validateBody(registerValidation), authController.register);
router.post('/login', validateBody(loginValidation), authController.login);
router.post('/logout', authenticate, validateBody(logoutValidation), authController.logout);

// Profile routes
router.get('/profile', authenticate, authController.getProfile);
router.put('/profile', authenticate, validateBody(updateProfileValidation), authController.updateProfile);

// Password management
router.put('/change-password', authenticate, validateBody(changePasswordValidation), authController.changePassword);

// Token validation
router.get('/validate', authenticate, authController.validateToken);

// Health check
router.get('/health', authController.healthCheck);

export default router; 