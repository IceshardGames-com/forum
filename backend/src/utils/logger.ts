import winston from 'winston';
import { envConfig } from '../config/env';

// Define log levels and colors
const logLevels = {
  error: 0,
  warn: 1,
  info: 2,
  http: 3,
  debug: 4,
};

const logColors = {
  error: 'red',
  warn: 'yellow',
  info: 'green',
  http: 'magenta',
  debug: 'white',
};

winston.addColors(logColors);

// Custom format for console output
const consoleFormat = winston.format.combine(
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss:ms' }),
  winston.format.colorize({ all: true }),
  winston.format.printf(
    (info) => `${info.timestamp} ${info.level}: ${info.message} ${
      info.requestId ? `[RequestId: ${info.requestId}]` : ''
    } ${
      Object.keys(info).filter(key => !['timestamp', 'level', 'message', 'requestId'].includes(key)).length > 0
        ? JSON.stringify(Object.fromEntries(Object.entries(info).filter(([key]) => !['timestamp', 'level', 'message', 'requestId'].includes(key))))
        : ''
    }`
  )
);

// JSON format for file output
const fileFormat = winston.format.combine(
  winston.format.timestamp(),
  winston.format.errors({ stack: true }),
  winston.format.json()
);

// Determine which transports to use
const transports = [];

// Console transport for development
if (envConfig.NODE_ENV !== 'production') {
  transports.push(
    new winston.transports.Console({
      format: consoleFormat,
    })
  );
}

// File transports for all environments
transports.push(
  new winston.transports.File({
    filename: 'logs/error.log',
    level: 'error',
    format: fileFormat,
  }),
  new winston.transports.File({
    filename: 'logs/combined.log',
    format: fileFormat,
  })
);

// Create the logger
const logger = winston.createLogger({
  level: envConfig.LOG_LEVEL,
  levels: logLevels,
  transports,
  // Don't exit on handled exceptions
  exitOnError: false,
});

// Create a logger instance with request ID context
export interface LoggerWithContext {
  error: (message: string, meta?: Record<string, any>) => void;
  warn: (message: string, meta?: Record<string, any>) => void;
  info: (message: string, meta?: Record<string, any>) => void;
  http: (message: string, meta?: Record<string, any>) => void;
  debug: (message: string, meta?: Record<string, any>) => void;
}

export class ContextualLogger implements LoggerWithContext {
  constructor(private requestId?: string) {}

  private sanitizeMeta(meta?: Record<string, any>): Record<string, any> | undefined {
    if (!meta || typeof meta !== 'object') return meta;

    const redactKeys = new Set([
      'authorization', 'Authorization', 'password', 'token', 'jwt', 'cookie',
      'refreshToken', 'accessToken', 'apiKey', 'x-api-key', 'secret'
    ]);

    const maskEmail = (val: string): string => {
      const m = val.match(/([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})/);
      if (!m) return val;
      const localRaw = m[1] || '';
      const domain = m[2] || '';
      if (!localRaw || !domain) return val;
      const len = localRaw.length;
      const maskedLocal = len <= 2 ? '*'.repeat(len) : `${localRaw[0]}${'*'.repeat(Math.max(1, len - 2))}${localRaw[len - 1]}`;
      return val.replace(m[0], `${maskedLocal}@${domain}`);
    };

    const recur = (obj: any): any => {
      if (Array.isArray(obj)) return obj.map(recur);
      if (obj && typeof obj === 'object') {
        const out: any = {};
        for (const [k, v] of Object.entries(obj)) {
          if (redactKeys.has(k)) {
            out[k] = '[REDACTED]';
          } else if (typeof v === 'string') {
            out[k] = maskEmail(v);
          } else {
            out[k] = recur(v);
          }
        }
        return out;
      }
      if (typeof obj === 'string') return maskEmail(obj);
      return obj;
    };

    return recur(meta);
  }

  private log(level: string, message: string, meta?: Record<string, any>): void {
    const logData = {
      message,
      requestId: this.requestId,
      ...(this.sanitizeMeta(meta)),
    };
    logger.log(level, logData);
  }

  error(message: string, meta?: Record<string, any>): void {
    this.log('error', message, meta);
  }

  warn(message: string, meta?: Record<string, any>): void {
    this.log('warn', message, meta);
  }

  info(message: string, meta?: Record<string, any>): void {
    this.log('info', message, meta);
  }

  http(message: string, meta?: Record<string, any>): void {
    this.log('http', message, meta);
  }

  debug(message: string, meta?: Record<string, any>): void {
    this.log('debug', message, meta);
  }
}

// Create logger with request ID context
export const createLogger = (requestId?: string): LoggerWithContext => {
  return new ContextualLogger(requestId);
};

// Default logger instance (without request ID)
export { logger };

// Export for backwards compatibility
export default logger; 