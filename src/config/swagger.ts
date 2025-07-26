import swaggerJsdoc from 'swagger-jsdoc';
import { envConfig } from './env';

/**
 * Swagger/OpenAPI configuration
 */
const swaggerDefinition = {
  openapi: '3.0.0',
  info: {
    title: 'Game Feedback Collector API',
    version: '1.0.0',
    description: 'A community-driven feedback platform where gamers submit feedback to game developers, who can view and analyze it in real time.',
    contact: {
      name: 'API Support',
      email: 'support@gamefeedback.com',
    },
    license: {
      name: 'ISC',
      url: 'https://opensource.org/licenses/ISC',
    },
  },
  servers: [
    {
      url: envConfig.NODE_ENV === 'production' 
        ? 'https://api.gamefeedback.com' 
        : `http://localhost:${envConfig.PORT}`,
      description: envConfig.NODE_ENV === 'production' ? 'Production server' : 'Development server',
    },
  ],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        description: 'Enter JWT token without "Bearer" prefix',
      },
    },
    schemas: {
      // User schema
      User: {
        type: 'object',
        properties: {
          _id: {
            type: 'string',
            description: 'User ID',
            example: '507f1f77bcf86cd799439011',
          },
          username: {
            type: 'string',
            description: 'Unique username',
            example: 'gamer123',
          },
          email: {
            type: 'string',
            format: 'email',
            description: 'User email address',
            example: 'gamer@example.com',
          },
          role: {
            type: 'string',
            enum: ['gamer', 'developer', 'admin'],
            description: 'User role',
            example: 'gamer',
          },
          isActive: {
            type: 'boolean',
            description: 'Whether the user account is active',
            example: true,
          },
          isEmailVerified: {
            type: 'boolean',
            description: 'Whether the user email is verified',
            example: false,
          },
          lastLogin: {
            type: 'string',
            format: 'date-time',
            description: 'Last login timestamp',
            example: '2023-12-01T10:30:00.000Z',
          },
          createdAt: {
            type: 'string',
            format: 'date-time',
            description: 'Account creation timestamp',
            example: '2023-11-01T08:00:00.000Z',
          },
          updatedAt: {
            type: 'string',
            format: 'date-time',
            description: 'Last update timestamp',
            example: '2023-12-01T10:30:00.000Z',
          },
          displayName: {
            type: 'string',
            description: 'Display name (virtual field)',
            example: 'gamer123',
          },
          isPrivileged: {
            type: 'boolean',
            description: 'Whether user has developer or admin privileges (virtual field)',
            example: false,
          },
        },
        required: ['_id', 'username', 'email', 'role', 'isActive', 'isEmailVerified', 'createdAt', 'updatedAt'],
      },

      // JWT Token Payload
      JwtPayload: {
        type: 'object',
        properties: {
          userId: {
            type: 'string',
            description: 'User ID',
            example: '507f1f77bcf86cd799439011',
          },
          email: {
            type: 'string',
            format: 'email',
            description: 'User email',
            example: 'gamer@example.com',
          },
          role: {
            type: 'string',
            enum: ['gamer', 'developer', 'admin'],
            description: 'User role',
            example: 'gamer',
          },
          iat: {
            type: 'number',
            description: 'Token issued at timestamp',
            example: 1701424200,
          },
          exp: {
            type: 'number',
            description: 'Token expiration timestamp',
            example: 1702028800,
          },
        },
        required: ['userId', 'email', 'role'],
      },

      // Token Pair
      TokenPair: {
        type: 'object',
        properties: {
          accessToken: {
            type: 'string',
            description: 'JWT access token',
            example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
          },
          refreshToken: {
            type: 'string',
            description: 'JWT refresh token',
            example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
          },
        },
        required: ['accessToken', 'refreshToken'],
      },

      // Standard API Response
      ApiResponse: {
        type: 'object',
        properties: {
          success: {
            type: 'boolean',
            description: 'Whether the request was successful',
            example: true,
          },
          data: {
            type: 'object',
            description: 'Response data (varies by endpoint)',
          },
          message: {
            type: 'string',
            description: 'Response message',
            example: 'Operation completed successfully',
          },
          requestId: {
            type: 'string',
            description: 'Unique request identifier for tracing',
            example: '550e8400-e29b-41d4-a716-446655440000',
          },
          timestamp: {
            type: 'string',
            format: 'date-time',
            description: 'Response timestamp',
            example: '2023-12-01T10:30:00.000Z',
          },
        },
        required: ['success', 'message', 'timestamp'],
      },

      // Error Response
      ErrorResponse: {
        type: 'object',
        properties: {
          success: {
            type: 'boolean',
            example: false,
            description: 'Always false for error responses',
          },
          error: {
            type: 'object',
            properties: {
              code: {
                type: 'string',
                description: 'Error code',
                example: 'VALIDATION_ERROR',
              },
              message: {
                type: 'string',
                description: 'Error message',
                example: 'Validation failed',
              },
              details: {
                type: 'object',
                description: 'Additional error details',
                example: {
                  field: 'email',
                  value: 'invalid-email',
                },
              },
              requestId: {
                type: 'string',
                description: 'Request ID for tracing',
                example: '550e8400-e29b-41d4-a716-446655440000',
              },
              timestamp: {
                type: 'string',
                format: 'date-time',
                description: 'Error timestamp',
                example: '2023-12-01T10:30:00.000Z',
              },
              path: {
                type: 'string',
                description: 'Request path where error occurred',
                example: '/api/auth/register',
              },
              method: {
                type: 'string',
                description: 'HTTP method',
                example: 'POST',
              },
            },
            required: ['code', 'message', 'timestamp'],
          },
        },
        required: ['success', 'error'],
      },

      // Validation Error Details
      ValidationErrorDetail: {
        type: 'object',
        properties: {
          field: {
            type: 'string',
            description: 'Field that failed validation',
            example: 'email',
          },
          message: {
            type: 'string',
            description: 'Validation error message',
            example: 'Please provide a valid email address',
          },
          value: {
            description: 'The invalid value that was provided',
            example: 'invalid-email-format',
          },
        },
        required: ['field', 'message'],
      },
    },
  },
  tags: [
    {
      name: 'Authentication',
      description: 'User authentication and profile management operations',
    },
    {
      name: 'Health',
      description: 'Service health check endpoints',
    },
  ],
};

/**
 * Swagger JSDoc options
 */
const swaggerOptions: swaggerJsdoc.Options = {
  definition: swaggerDefinition,
  apis: [
    './src/routes/*.ts',
    './src/controllers/*.ts',
    './src/models/*.ts',
    './src/docs/swagger/*.ts',
  ],
};

/**
 * Generate Swagger specification
 */
export const swaggerSpec = swaggerJsdoc(swaggerOptions);

/**
 * Swagger UI options
 */
export const swaggerUiOptions = {
  explorer: true,
  customCss: `
    .swagger-ui .topbar { display: none; }
    .swagger-ui .info .title { color: #2c3e50; }
    .swagger-ui .scheme-container { background: #f8f9fa; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
  `,
  customSiteTitle: 'Game Feedback Collector API',
  customfavIcon: '/favicon.ico',
  swaggerOptions: {
    persistAuthorization: true,
    displayRequestDuration: true,
    filter: true,
    showExtensions: true,
    showCommonExtensions: true,
    docExpansion: 'none',
    defaultModelsExpandDepth: 2,
    defaultModelExpandDepth: 2,
  },
};

export default swaggerSpec; 