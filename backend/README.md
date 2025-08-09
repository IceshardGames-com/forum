# Game Feedback Collector Backend

A modular, clean, and secure TypeScript backend for a community-driven feedback platform where gamers submit feedback to game developers, who can view and analyze it in real time.

## Features

### Phase 1 - Authentication Module ✅

- **User Registration** with role-based access control (Gamer, Developer, Admin)
- **JWT-based Authentication** with secure token management
- **User Login/Logout** with optional "remember me" functionality
- **Password Security** with bcrypt hashing and strong validation
- **Profile Management** with secure update capabilities
- **Role-based Authorization** middleware for different access levels

### 🔐 Security Features

- **JWT Authentication** with configurable expiration
- **Password Hashing** using bcrypt with configurable salt rounds
- **Request Rate Limiting** to prevent abuse
- **Input Validation** using Joi schemas
- **CORS Protection** with configurable origins
- **Helmet Security** headers
- **Request ID Tracking** for enhanced logging and debugging
- **Structured Logging** with Winston
- **Global Error Handling** with detailed error responses

### 📚 Documentation

- **Swagger/OpenAPI 3.0** documentation at `/api/docs`
- **Comprehensive API Documentation** with examples
- **Separate Schema Definitions** for clean code organization
- **Interactive API Testing** through Swagger UI

## 🛠 Technology Stack

| Purpose | Package | Version |
|---------|---------|---------|
| Runtime | Node.js | >=18.0.0 |
| Framework | Express.js | ^4.18.2 |
| Language | TypeScript | ^5.3.3 |
| Database | MongoDB via Mongoose | ^8.0.3 |
| Authentication | jsonwebtoken | ^9.0.2 |
| Password Hashing | bcrypt | ^5.1.1 |
| Validation | Joi | ^17.11.0 |
| Logging | Winston | ^3.11.0 |
| Documentation | Swagger | ^6.2.8 |
| Security | Helmet, CORS | Latest |
| Development | ts-node-dev | ^2.0.0 |

## 📁 Project Structure

```
backend/
├── src/
│   ├── config/           # Configuration files
│   │   ├── env.ts        # Environment configuration
│   │   ├── database.ts   # MongoDB connection
│   │   └── swagger.ts    # API documentation config
│   ├── controllers/      # Request handlers
│   │   └── auth.controller.ts
│   ├── routes/           # Express route definitions
│   │   └── auth.routes.ts
│   ├── services/         # Business logic
│   │   └── auth.service.ts
│   ├── models/           # Mongoose models
│   │   └── User.ts
│   ├── middlewares/      # Custom middleware
│   │   ├── auth.ts       # Authentication middleware
│   │   ├── admin.ts      # Authorization middleware
│   │   ├── validate.ts   # Input validation
│   │   ├── requestLogger.ts # Request logging
│   │   └── errorHandler.ts  # Global error handling
│   ├── validations/      # Joi validation schemas
│   │   └── auth.validation.ts
│   ├── utils/            # Utility functions
│   │   ├── logger.ts     # Winston logger setup
│   │   ├── jwt.ts        # JWT helper functions
│   │   └── uuid.ts       # UUID generation
│   ├── app.ts            # Express app configuration
│   └── server.ts         # Server entry point
├── logs/                 # Log files (auto-created)
├── package.json
├── tsconfig.json
└── README.md
```

## Quick Start

### Prerequisites

- Node.js (>=18.0.0)
- MongoDB (local or Atlas)
- npm or yarn

### Installation

1. **Clone and Install Dependencies**
   ```bash
   git clone <repository-url>
   cd backend
   npm install
   ```

2. **Environment Setup**
   
   Create a `.env` file in the root directory:
   ```env
   # Server Configuration
   PORT=3000
   NODE_ENV=development

   # Database Configuration
   MONGODB_URI=mongodb://localhost:27017/game-feedback-collector
   DB_NAME=game-feedback-collector

   # JWT Configuration
   JWT_SECRET=your-super-secret-jwt-key-change-in-production
   JWT_EXPIRES_IN=7d
   JWT_REFRESH_EXPIRES_IN=30d

   # Security Configuration
   BCRYPT_ROUNDS=12

   # CORS Configuration
   CORS_ORIGIN=http://localhost:3001
   CORS_CREDENTIALS=true

   # Rate Limiting
   RATE_LIMIT_WINDOW_MS=900000
   RATE_LIMIT_MAX_REQUESTS=100

   # Logging Configuration
   LOG_LEVEL=info
   LOG_FORMAT=json

   # Email Configuration (for OTP/email)
   EMAIL_USER=
   EMAIL_PASS=
   EMAIL_SERVICE=gmail

   # OTP Configuration
   OTP_LENGTH=6
   OTP_EXPIRY_MINUTES=5
   OTP_RESEND_COOLDOWN_MS=60000
   OTP_MAX_ATTEMPTS=5
   ```

3. **Start Development Server**
   ```bash
   npm run dev
   ```

4. **Access the Application**
   - API Base URL: `http://localhost:3000`
   - API Documentation: `http://localhost:3000/api/docs`
   - Health Check: `http://localhost:3000/health`

### Production Build

```bash
npm run build
npm start
```

## 📖 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | User login | ❌ |
| POST | `/api/auth/logout` | User logout | ✅ |
| GET | `/api/auth/profile` | Get user profile | ✅ |
| PUT | `/api/auth/profile` | Update user profile | ✅ |
| PUT | `/api/auth/change-password` | Change password | ✅ |
| GET | `/api/auth/validate` | Validate token | ✅ |
| GET | `/api/auth/health` | Auth service health | ❌ |

### Sample Requests

#### Register User
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "gamer123",
    "email": "gamer@example.com",
    "password": "Password123!",
    "confirmPassword": "Password123!",
    "role": "gamer"
  }'
```

#### Login User
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "gamer@example.com",
    "password": "Password123!",
    "rememberMe": false
  }'
```

#### Access Protected Route
```bash
curl -X GET http://localhost:3000/api/auth/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response Format

All API responses follow a consistent structure:

**Success Response:**
```json
{
  "success": true,
  "data": {
    "user": {...},
    "tokens": {...}
  },
  "message": "Operation completed successfully",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2023-12-01T10:30:00.000Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {...},
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2023-12-01T10:30:00.000Z",
    "path": "/api/auth/register",
    "method": "POST"
  }
}
```

## 🔒 Security

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (@$!%*?&)

### JWT Token Security

- Configurable expiration times
- Secure signing algorithm (HS256)
- Request ID tracking for audit trails
- Token validation on every protected request

### Rate Limiting

- 100 requests per 15 minutes per IP (configurable)
- Excluded paths: `/health`, `/ping`
- Returns `429 Too Many Requests` when exceeded

## 🔧 Development

### Available Scripts

```bash
npm run dev          # Start development server with hot reload
npm run build        # Build TypeScript to JavaScript
npm start           # Start production server
npm run clean       # Remove build artifacts
npm run typecheck   # Run TypeScript type checking
```

### Code Quality Guidelines

- **Clean Code Principles**: Constants over magic numbers, meaningful names
- **Single Responsibility**: Each function does one thing
- **DRY Principle**: Don't repeat yourself
- **Type Safety**: Full TypeScript coverage with strict mode
- **Error Handling**: Comprehensive error handling with structured responses
- **Logging**: Structured logging with request ID tracing

### Adding New Features

1. **Create Model** (if needed) in `/src/models/`
2. **Add Validation Schema** in `/src/validations/`
3. **Implement Service** in `/src/services/`
4. **Create Controller** in `/src/controllers/`
5. **Define Routes** in `/src/routes/`
6. **Add Swagger Documentation** inline with routes
7. **Register Routes** in `/src/app.ts`

## 🔍 Monitoring & Logging

### Structured Logging

All logs include:
- Request ID for tracing
- User context (when available)
- Timestamp
- Log level
- Structured metadata

### Log Levels

- **error**: System errors, failed operations
- **warn**: Warnings, authentication failures
- **info**: General information, successful operations
- **http**: HTTP request/response details
- **debug**: Detailed debugging information

### Health Checks

- **GET /health**: Comprehensive health check with system info
- **GET /ping**: Simple health check returning "pong"

## 🚧 Next Phase Features (Coming Soon)

- [ ] Feedback submission module
- [ ] Game directory management
- [ ] Developer dashboard
- [ ] Real-time notifications
- [ ] Image uploads with compression
- [ ] CSV export functionality
- [ ] Advanced search & filtering
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Two-factor authentication

## 🤝 Contributing

1. Follow the existing code structure and patterns
2. Use TypeScript strict mode
3. Add comprehensive error handling
4. Include Joi validation for all inputs
5. Add Swagger documentation for new endpoints
6. Write meaningful commit messages
7. Test all endpoints thoroughly

## 📝 License

ISC License - see LICENSE file for details.

## 🆘 Support

- **Documentation**: `/api/docs` endpoint
- **Health Check**: `/health` endpoint
- **Request Tracing**: Every request includes a unique `requestId`

---

**Built with ❤️ for the gaming community** 