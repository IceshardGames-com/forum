import express, { Application, Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import swaggerUi from 'swagger-ui-express';

// Import configurations
import { envConfig } from './config/env';
import { swaggerSpec, swaggerUiOptions } from './config/swagger';

// Import middlewares
import { requestLogger, healthCheck } from './middlewares/requestLogger';
import { errorHandler, notFoundHandler } from './middlewares/errorHandler';

// Import routes
import authRoutes from './routes/auth.routes';

/**
 * Express application factory
 */
export const createApp = (): Application => {
  const app = express();

  // Trust proxy (important for rate limiting and IP detection)
  app.set('trust proxy', 1);

  // Security middleware
  app.use(helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        styleSrc: ["'self'", "'unsafe-inline'", "fonts.googleapis.com"],
        fontSrc: ["'self'", "fonts.gstatic.com"],
        scriptSrc: ["'self'"],
        imgSrc: ["'self'", "data:", "https:"],
      },
    },
    crossOriginEmbedderPolicy: false,
  }));

  // CORS configuration
  app.use(cors({
    origin: (origin, callback) => {
      // Allow requests with no origin (mobile apps, Postman, etc.)
      if (!origin) return callback(null, true);
      
      // Allow localhost in development
      if (envConfig.NODE_ENV === 'development') {
        return callback(null, true);
      }
      
      // Production CORS configuration
      const allowedOrigins = [envConfig.CORS_ORIGIN];
      if (allowedOrigins.includes(origin)) {
        return callback(null, true);
      }
      
      return callback(new Error('Not allowed by CORS'), false);
    },
    credentials: envConfig.CORS_CREDENTIALS,
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
    exposedHeaders: ['X-Total-Count', 'X-Page-Count'],
  }));

  // Rate limiting
  const limiter = rateLimit({
    windowMs: envConfig.RATE_LIMIT_WINDOW_MS,
    max: envConfig.RATE_LIMIT_MAX_REQUESTS,
    message: {
      success: false,
      error: {
        code: 'RATE_LIMIT_EXCEEDED',
        message: 'Too many requests from this IP, please try again later',
        timestamp: new Date().toISOString(),
      },
    },
    standardHeaders: true,
    legacyHeaders: false,
    // Skip rate limiting for health checks
    skip: (req) => req.path === '/health' || req.path === '/ping',
  });

  app.use(limiter);

  // Body parsing middleware
  app.use(express.json({ 
    limit: '10mb',
  }));

  app.use(express.urlencoded({ extended: true, limit: '10mb' }));

  // Request logging middleware
  app.use(requestLogger);

  // Health check middleware (before request logging)
  app.use(healthCheck);

  // API Documentation
  app.use('/api/docs', swaggerUi.serve);
  app.get('/api/docs', swaggerUi.setup(swaggerSpec, swaggerUiOptions));

  // Serve swagger.json for external tools
  app.get('/api/docs/swagger.json', (_req: Request, res: Response) => {
    res.setHeader('Content-Type', 'application/json');
    res.send(swaggerSpec);
  });

  // API Routes
  app.use('/api/auth', authRoutes);

  // Root endpoint
  app.get('/', (req: Request, res: Response) => {
    res.json({
      success: true,
      data: {
        name: 'Game Feedback Collector API',
        version: '1.0.0',
        description: 'A community-driven feedback platform for gamers and developers',
        documentation: `${req.protocol}://${req.get('host')}/api/docs`,
        health: `${req.protocol}://${req.get('host')}/health`,
        endpoints: {
          auth: `${req.protocol}://${req.get('host')}/api/auth`,
        },
      },
      message: 'Welcome to Game Feedback Collector API',
      timestamp: new Date().toISOString(),
    });
  });

  // Global health check endpoint
  app.get('/health', (_req: Request, res: Response) => {
    res.json({
      success: true,
      data: {
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        memory: process.memoryUsage(),
        environment: envConfig.NODE_ENV,
        version: '1.0.0',
      },
      message: 'Service is healthy',
      timestamp: new Date().toISOString(),
    });
  });

  // Ping endpoint for simple health checks
  app.get('/ping', (_req: Request, res: Response) => {
    res.status(200).send('pong');
  });

  // Handle 404 for undefined routes
  app.use(notFoundHandler);

  // Global error handler (must be last)
  app.use(errorHandler);

  return app;
};

// Create and export the app instance
const app = createApp();

export default app; 