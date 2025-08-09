import { Request, Response } from 'express';
import { asyncErrorHandler } from '../middlewares/errorHandler';
import { createLogger } from '../utils/logger';
import { sendOtp, verifyOtp } from '../services/otp.service';

export const requestOtp = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const { email } = req.body as { email?: string };
  if (!email) {
    res.status(400).json({ success: false, message: 'Email required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }


  await sendOtp(email.toLowerCase(), req.id);
  logger.info('OTP requested', { email });
  res.status(200).json({ success: true, message: 'OTP sent', requestId: req.id, timestamp: new Date().toISOString() });
});

export const confirmOtp = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const { email, otp } = req.body as { email?: string; otp?: string };
  if (!email || !otp) {
    res.status(400).json({ success: false, message: 'Email and OTP required', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }

  const ok = verifyOtp(email.toLowerCase(), otp, req.id);
  if (!ok) {
    logger.warn('OTP verification failed', { email });
    res.status(400).json({ success: false, message: 'Invalid or expired OTP', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }

  res.status(200).json({ success: true, message: 'Verified successfully', requestId: req.id, timestamp: new Date().toISOString() });
});


