import { Request, Response } from 'express';
import { asyncErrorHandler } from '../middlewares/errorHandler';
import { createLogger } from '../utils/logger';
import { sendOtp, verifyOtp } from '../services/otp.service';
import { User } from '../models/User';

export const requestOtp = asyncErrorHandler(async (req: Request, res: Response) => {
  const logger = createLogger(req.id);
  const { email } = req.body as { email?: string };

  if (!email) {
    res.status(400).json({ success: false, message: 'Email required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }

  const normalizedEmail = email.toLowerCase();
  const user = await User.findOne({ email: normalizedEmail });
    if (!user?.isActive) {
      res.status(403).json({ success: false, message: 'Account is inactive', requestId: req.id, timestamp: new Date().toISOString() });
      return;
    }

  // Send OTP
  await sendOtp(normalizedEmail, req.id);
  logger.info('OTP requested', { email: normalizedEmail });

  res.status(200).json({ success: true, message: 'OTP sent', requestId: req.id, timestamp: new Date().toISOString() });
});

export const confirmOtp = asyncErrorHandler(async (req: Request, res: Response) => {
  const logger = createLogger(req.id);
  const { email, otp } = req.body as { email?: string; otp?: string };

  if (!email || !otp) {
    res.status(400).json({ success: false, message: 'Email and OTP required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }

  const normalizedEmail = email.toLowerCase();
  const isValid = verifyOtp(normalizedEmail, otp, req.id);

  if (!isValid) {
    logger.warn('OTP verification failed', { email: normalizedEmail });
    res.status(400).json({ success: false, message: 'Invalid or expired OTP', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }
  await User.updateOne({ email: normalizedEmail }, { isEmailVerified: true });

  res.status(200).json({ success: true, message: 'Verified successfully', requestId: req.id, timestamp: new Date().toISOString() });
});
