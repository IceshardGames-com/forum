import crypto from 'crypto';
import { envConfig } from '../../config/env';
import { createLogger } from '../../utils/logger';
import { sendOTPEmail } from './email.service';

interface OtpRecord {
  hashedOtp: string;
  expiresAt: number;
  attempts: number;
  lastSentAt: number;
}

const otpStore: Record<string, OtpRecord> = {};

const hashOtp = (otp: string): string => {
  return crypto.createHash('sha256').update(otp).digest('hex');
};

const generateOtp = (length: number): string => {
  // Generate cryptographically secure numeric OTP
  let otp = '';
  for (let i = 0; i < length; i++) {
    otp += crypto.randomInt(0, 10).toString();
  }
  return otp;
};

export const sendOtp = async (email: string, requestId?: string): Promise<void> => {
  const logger = createLogger(requestId);

  const existing = otpStore[email];
  const now = Date.now();
  if (existing && now - existing.lastSentAt < envConfig.OTP_RESEND_COOLDOWN_MS) {
    const msLeft = envConfig.OTP_RESEND_COOLDOWN_MS - (now - existing.lastSentAt);
    logger.warn('OTP send throttled', { email, msLeft });
    throw new Error('Please wait before requesting a new OTP');
  }

  const otp = generateOtp(envConfig.OTP_LENGTH);
  const hashedOtp = hashOtp(otp);

  otpStore[email] = {
    hashedOtp,
    expiresAt: now + envConfig.OTP_EXPIRY_MINUTES * 60 * 1000,
    attempts: 0,
    lastSentAt: now,
  };

  await sendOTPEmail(email, otp, requestId);
  logger.info('OTP generated and email queued', { email });
};

export const verifyOtp = (email: string, otp: string, requestId?: string): boolean => {
  const logger = createLogger(requestId);
  const record = otpStore[email];
  if (!record) {
    logger.warn('OTP verify failed: no record', { email });
    return false;
  }

  const now = Date.now();
  if (now > record.expiresAt) {
    delete otpStore[email];
    logger.warn('OTP verify failed: expired', { email });
    return false;
  }

  if (record.attempts >= envConfig.OTP_MAX_ATTEMPTS) {
    delete otpStore[email];
    logger.warn('OTP verify failed: too many attempts', { email });
    return false;
  }

  record.attempts += 1;
  const providedHash = hashOtp(otp);
  const isMatch = crypto.timingSafeEqual(Buffer.from(record.hashedOtp), Buffer.from(providedHash));

  if (!isMatch) {
    logger.warn('OTP verify failed: mismatch', { email, attempts: record.attempts });
    return false;
  }

  delete otpStore[email];
  logger.info('OTP verified successfully', { email });
  return true;
};

export const clearOtp = (email: string): void => {
  delete otpStore[email];
};


