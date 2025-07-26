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

  private log(level: string, message: string, meta?: Record<string, any>): void {
    const logData = {
      message,
      requestId: this.requestId,
      ...meta,
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