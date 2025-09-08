import dotenv from 'dotenv';

dotenv.config();

export interface EnvConfig {
  // Server Configuration
  PORT: number;
  NODE_ENV: string;

  // Database Configuration
  MONGODB_URI: string;
  DB_NAME: string;

  // JWT Configuration
  JWT_SECRET: string;
  JWT_EXPIRES_IN: string;
  JWT_REFRESH_EXPIRES_IN: string;

  // Security Configuration
  BCRYPT_ROUNDS: number;

  // CORS Configuration
  CORS_ORIGIN: string;
  CORS_CREDENTIALS: boolean;

  // Rate Limiting
  RATE_LIMIT_WINDOW_MS: number;
  RATE_LIMIT_MAX_REQUESTS: number;

  // Logging Configuration
  LOG_LEVEL: string;
  LOG_FORMAT: string;

  // Email Configuration
  EMAIL_USER: string;
  EMAIL_PASS: string;
  EMAIL_SERVICE: string;

  // OTP Configuration
  OTP_LENGTH: number;
  OTP_EXPIRY_MINUTES: number;
  OTP_RESEND_COOLDOWN_MS: number;
  OTP_MAX_ATTEMPTS: number;

  // Socket.IO Configuration
  CLIENT_URL: string;

  // Cloudflare R2 (S3-compatible) Configuration
  R2_ACCOUNT_ID: string;
  R2_ACCESS_KEY_ID: string;
  R2_SECRET_ACCESS_KEY: string;
  R2_BUCKET_NAME: string;
  R2_PUBLIC_BASE_URL: string;

  // Cloudflare Images Configuration
  CF_IMAGES_ACCOUNT_ID: string;
  CF_IMAGES_API_TOKEN: string;
  CF_IMAGES_DELIVERY_URL: string;
}

const getEnvValue = (key: string, defaultValue?: string): string => {
  const value = process.env[key];
  if (!value && !defaultValue) {
    throw new Error(`Environment variable ${key} is required but not set`);
  }
  return value || defaultValue!;
};

const getEnvNumber = (key: string, defaultValue?: number): number => {
  const value = process.env[key];
  if (!value && defaultValue === undefined) {
    throw new Error(`Environment variable ${key} is required but not set`);
  }
  const numValue = Number(value || defaultValue);
  if (isNaN(numValue)) {
    throw new Error(`Environment variable ${key} must be a valid number`);
  }
  return numValue;
};

const getEnvBoolean = (key: string, defaultValue?: boolean): boolean => {
  const value = process.env[key];
  if (!value && defaultValue === undefined) {
    throw new Error(`Environment variable ${key} is required but not set`);
  }
  return value === 'true' || (defaultValue || false);
};

export const envConfig: EnvConfig = {
  // Server Configuration
  PORT: getEnvNumber('PORT', 3000),
  NODE_ENV: getEnvValue('NODE_ENV', 'development'),

  // Database Configuration
  MONGODB_URI: getEnvValue('MONGODB_URI', 'mongodb://localhost:27017/game-feedback-collector'),
  DB_NAME: getEnvValue('DB_NAME', 'game-feedback-collector'),

  // JWT Configuration
  JWT_SECRET: getEnvValue('JWT_SECRET', 'your-super-secret-jwt-key-please-change-in-production'),
  JWT_EXPIRES_IN: getEnvValue('JWT_EXPIRES_IN', '7d'),
  JWT_REFRESH_EXPIRES_IN: getEnvValue('JWT_REFRESH_EXPIRES_IN', '30d'),

  // Security Configuration
  BCRYPT_ROUNDS: getEnvNumber('BCRYPT_ROUNDS', 12),

  // CORS Configuration
  CORS_ORIGIN: getEnvValue('CORS_ORIGIN', 'http://localhost:3001'),
  CORS_CREDENTIALS: getEnvBoolean('CORS_CREDENTIALS', true),

  // Rate Limiting
  RATE_LIMIT_WINDOW_MS: getEnvNumber('RATE_LIMIT_WINDOW_MS', 900000), // 15 minutes
  RATE_LIMIT_MAX_REQUESTS: getEnvNumber('RATE_LIMIT_MAX_REQUESTS', 100),

  // Logging Configuration
  LOG_LEVEL: getEnvValue('LOG_LEVEL', 'info'),
  LOG_FORMAT: getEnvValue('LOG_FORMAT', 'json'),

  // Email Configuration
  EMAIL_USER: getEnvValue('EMAIL_USER'),
  EMAIL_PASS: getEnvValue('EMAIL_PASS'),
  EMAIL_SERVICE: getEnvValue('EMAIL_SERVICE', 'gmail'),

  // OTP Configuration
  OTP_LENGTH: getEnvNumber('OTP_LENGTH', 6),
  OTP_EXPIRY_MINUTES: getEnvNumber('OTP_EXPIRY_MINUTES', 5),
  OTP_RESEND_COOLDOWN_MS: getEnvNumber('OTP_RESEND_COOLDOWN_MS', 60000),
  OTP_MAX_ATTEMPTS: getEnvNumber('OTP_MAX_ATTEMPTS', 5),

    // Socket.IO Configuration
  CLIENT_URL: getEnvValue('CLIENT_URL', process.env.NODE_ENV === 'production' ? 'https://forum-sjpj.onrender.com' : 'http://localhost:3000'),

  // Cloudflare R2 (S3-compatible) Configuration
  R2_ACCOUNT_ID: getEnvValue('R2_ACCOUNT_ID', ''),
  R2_ACCESS_KEY_ID: getEnvValue('R2_ACCESS_KEY_ID', ''),
  R2_SECRET_ACCESS_KEY: getEnvValue('R2_SECRET_ACCESS_KEY', ''),
  R2_BUCKET_NAME: getEnvValue('R2_BUCKET_NAME', ''),
  R2_PUBLIC_BASE_URL: getEnvValue('R2_PUBLIC_BASE_URL', ''),

  // Cloudflare Images Configuration
  CF_IMAGES_ACCOUNT_ID: getEnvValue('CF_IMAGES_ACCOUNT_ID', ''),
  CF_IMAGES_API_TOKEN: getEnvValue('CF_IMAGES_API_TOKEN', ''),
  CF_IMAGES_DELIVERY_URL: getEnvValue('CF_IMAGES_DELIVERY_URL', ''),
};

// Validate critical environment variables on startup
export const validateEnv = (): void => {
  const requiredVars = ['JWT_SECRET'];
  
  for (const varName of requiredVars) {
    if (!process.env[varName] && envConfig.NODE_ENV === 'production') {
      throw new Error(`Required environment variable ${varName} is not set for production`);
    }
  }

  if (envConfig.NODE_ENV === 'production' && envConfig.JWT_SECRET === 'your-super-secret-jwt-key-please-change-in-production') {
    throw new Error('JWT_SECRET must be changed from default value in production');
  }

  // In production, require email credentials for OTP/email flows
  if (envConfig.NODE_ENV === 'production') {
    const emailVars = ['EMAIL_USER', 'EMAIL_PASS'];
    for (const v of emailVars) {
      if (!process.env[v]) {
        throw new Error(`Required environment variable ${v} is not set for production`);
      }
    }

    // In production, require Cloudflare configurations
    const r2Vars = ['R2_ACCOUNT_ID', 'R2_ACCESS_KEY_ID', 'R2_SECRET_ACCESS_KEY', 'R2_BUCKET_NAME'];
    for (const v of r2Vars) {
      if (!process.env[v]) {
        throw new Error(`Required environment variable ${v} is not set for production`);
      }
    }

    const imagesVars = ['CF_IMAGES_ACCOUNT_ID', 'CF_IMAGES_API_TOKEN', 'CF_IMAGES_DELIVERY_URL'];
    for (const v of imagesVars) {
      if (!process.env[v]) {
        throw new Error(`Required environment variable ${v} is not set for production`);
      }
    }
  }
}; 