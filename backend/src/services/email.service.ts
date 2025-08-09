import nodemailer from 'nodemailer';
import { envConfig } from '../config/env';
import { createLogger } from '../utils/logger';

export interface SendMailOptions {
  to: string;
  subject: string;
  text?: string;
  html?: string;
}

const transporter = nodemailer.createTransport({
  service: envConfig.EMAIL_SERVICE,
  auth: {
    user: envConfig.EMAIL_USER,
    pass: envConfig.EMAIL_PASS,
  },
});

export const sendMail = async (options: SendMailOptions, requestId?: string): Promise<void> => {
  const logger = createLogger(requestId);
  try {
    await transporter.sendMail({
      from: envConfig.EMAIL_USER,
      to: options.to,
      subject: options.subject,
      text: options.text,
      html: options.html,
    });
    logger.info('Email sent', { to: options.to, subject: options.subject });
  } catch (error) {
    logger.error('Failed to send email', {
      error: error instanceof Error ? error.message : 'Unknown error',
      to: options.to,
      subject: options.subject,
    });
    throw error;
  }
};

export const sendOTPEmail = async (to: string, otp: string, requestId?: string): Promise<void> => {
  const subject = 'Your OTP Verification Code';
  const text = `Your OTP code is: ${otp}`;
  const html = `<h2>Your OTP code is:</h2><p style="font-size:20px;"><b>${otp}</b></p>`;
  await sendMail({ to, subject, text, html }, requestId);
};


