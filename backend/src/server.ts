import { createServer } from 'http';
import { Server } from 'socket.io';
import app from './app';
import { envConfig, validateEnv } from './config/env';
import { database } from './config/database';
import { logger } from './utils/logger';
import { jwtService } from './utils/jwt';
import { deviceService } from './services/chats/device.service';

/**
 * Start the server
 */
const startServer = async (): Promise<void> => {
  try {
    // Validate environment variables
    validateEnv();
    logger.info('Environment variables validated successfully');

    // Connect to MongoDB
    await database.connect();
    logger.info('Database connected successfully');

    // Create logs directory if it doesn't exist
    const fs = require('fs');
    if (!fs.existsSync('logs')) {
      fs.mkdirSync('logs');
      logger.info('Logs directory created');
    }

    // Create HTTP server and setup Socket.IO
    const httpServer = createServer(app);
    
    // Setup Socket.IO with CORS
    const io = new Server(httpServer, {
      cors: {
        origin: envConfig.CLIENT_URL || "http://localhost:3000",
        methods: ["GET", "POST"],
        credentials: true
      }
    });

    // Socket.IO auth middleware (JWT)
    io.use((socket, next) => {
      try {
        const authToken = (socket.handshake.auth && socket.handshake.auth.token)
          || (socket.handshake.headers && typeof socket.handshake.headers.authorization === 'string'
              ? socket.handshake.headers.authorization.replace(/^Bearer\s+/i, '')
              : undefined);

        if (!authToken) {
          return next(new Error('Unauthorized'));
        }

        const payload = jwtService.verifyToken(authToken);
        // Attach userId to socket
        (socket as any).userId = payload.userId;
        return next();
      } catch (err) {
        return next(new Error('Unauthorized'));
      }
    });

    // Socket.IO connection handling
    io.on('connection', (socket) => {
      logger.info('Socket.IO: User connected', { socketId: socket.id });
      
      // Auto-join authenticated user's personal room
      const userId = (socket as any).userId as string | undefined;
      if (userId) {
        socket.join(userId);
        logger.info('Socket.IO: User auto-joined personal room', { socketId: socket.id, userId });
      }

      // Backwards-compat join event (ignores provided userId)
      socket.on('join', () => {
        if (userId) {
          socket.join(userId);
          logger.info('Socket.IO: (Deprecation) join called - joined authenticated room', { socketId: socket.id, userId });
        }
      });

      // Secure device room join (verify ownership)
      socket.on('join:device', async (deviceId: string) => {
        try {
          if (!userId || !deviceId) return;
          const device = await deviceService.getDeviceById(deviceId);
          if (device && device.user.toString() === userId) {
            const room = `device:${deviceId}`;
            socket.join(room);
            logger.info('Socket.IO: Joined device room', { socketId: socket.id, userId, deviceId });
          } else {
            logger.warn('Socket.IO: Device join denied (ownership)', { socketId: socket.id, userId, deviceId });
          }
        } catch (e) {
          logger.warn('Socket.IO: Device join error', { error: e instanceof Error ? e.message : 'Unknown', deviceId, userId });
        }
      });

      socket.on('leave:device', (deviceId: string) => {
        if (!deviceId) return;
        const room = `device:${deviceId}`;
        socket.leave(room);
        logger.info('Socket.IO: Left device room', { socketId: socket.id, userId, deviceId });
      });

      socket.on('disconnect', (reason) => {
        logger.info('Socket.IO: User disconnected', { 
          socketId: socket.id, 
          reason 
        });
      });
    });

    // Make io available globally for services
    (global as any).io = io;

    // Start the server
    const server = httpServer.listen(envConfig.PORT, () => {
      logger.info('Server started successfully', {
        port: envConfig.PORT,
        environment: envConfig.NODE_ENV,
        docs: `http://localhost:${envConfig.PORT}/api/docs`,
        health: `http://localhost:${envConfig.PORT}/health`,
        socketio: 'Socket.IO enabled',
      });
    });

    // Graceful shutdown handling
    const gracefulShutdown = (signal: string) => {
      logger.info(`Received ${signal}. Starting graceful shutdown...`);
      
      // Close Socket.IO server
      io.close(() => {
        logger.info('Socket.IO server closed');
      });
      
      server.close(async (err) => {
        if (err) {
          logger.error('Error during server shutdown', { error: err.message });
          process.exit(1);
        }

        logger.info('HTTP server closed');

        try {
          await database.disconnect();
          logger.info('Database disconnected successfully');
          logger.info('Graceful shutdown completed');
          process.exit(0);
        } catch (dbError) {
          logger.error('Error disconnecting from database', { 
            error: dbError instanceof Error ? dbError.message : 'Unknown error' 
          });
          process.exit(1);
        }
      });

      // Force shutdown after 30 seconds
      setTimeout(() => {
        logger.error('Could not close connections in time, forcefully shutting down');
        process.exit(1);
      }, 30000);
    };

    // Listen for termination signals
    process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
    process.on('SIGINT', () => gracefulShutdown('SIGINT'));

    // Handle uncaught exceptions
    process.on('uncaughtException', (error: Error) => {
      logger.error('Uncaught Exception', {
        error: error.message,
        stack: error.stack,
      });
      gracefulShutdown('UNCAUGHT_EXCEPTION');
    });

    // Handle unhandled promise rejections
    process.on('unhandledRejection', (reason: any, _promise: Promise<any>) => {
      logger.error('Unhandled Rejection', {
        reason: reason instanceof Error ? reason.message : reason,
        stack: reason instanceof Error ? reason.stack : undefined,
      });
      gracefulShutdown('UNHANDLED_REJECTION');
    });

  } catch (error) {
    logger.error('Failed to start server', {
      error: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : undefined,
    });
    process.exit(1);
  }
};

// Start the server if this file is run directly
if (require.main === module) {
  startServer();
}

export default startServer; 