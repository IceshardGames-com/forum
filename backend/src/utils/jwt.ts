import jwt from 'jsonwebtoken';
import { envConfig } from '../config/env';
import { logger } from './logger';

export interface JwtPayload {
  userId: string;
  email: string;
  role: string;
  iat?: number;
  exp?: number;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export class JwtService {
  private static instance: JwtService;

  private constructor() {}

  public static getInstance(): JwtService {
    if (!JwtService.instance) {
      JwtService.instance = new JwtService();
    }
    return JwtService.instance;
  }

  /**
   * Signs a JWT token with the provided payload
   */
  public signToken(payload: Omit<JwtPayload, 'iat' | 'exp'>, expiresIn?: string): string {
    try {
      const token = jwt.sign(
        payload,
        envConfig.JWT_SECRET,
        {
          expiresIn: expiresIn || envConfig.JWT_EXPIRES_IN,
          issuer: 'game-feedback-collector',
          audience: 'game-feedback-collector-users',
        } as jwt.SignOptions
      );
      
      return token;
    } catch (error) {
      logger.error('Failed to sign JWT token', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId: payload.userId,
      });
      throw new Error('Token signing failed');
    }
  }

  /**
   * Verifies and decodes a JWT token
   */
  public verifyToken(token: string): JwtPayload {
    try {
      const decoded = jwt.verify(token, envConfig.JWT_SECRET, {
        issuer: 'game-feedback-collector',
        audience: 'game-feedback-collector-users',
      }) as JwtPayload;
      
      return decoded;
    } catch (error) {
      if (error instanceof jwt.TokenExpiredError) {
        logger.warn('JWT token expired');
        throw new Error('Token expired');
      } else if (error instanceof jwt.JsonWebTokenError) {
        logger.warn('Invalid JWT token', { 
          error: error.message,
        });
        throw new Error('Invalid token');
      } else {
        logger.error('JWT verification failed', {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new Error('Token verification failed');
      }
    }
  }

  /**
   * Generates both access and refresh tokens
   */
  public generateTokenPair(payload: Omit<JwtPayload, 'iat' | 'exp'>): TokenPair {
    try {
      const accessToken = this.signToken(payload, envConfig.JWT_EXPIRES_IN);
      const refreshToken = this.signToken(payload, envConfig.JWT_REFRESH_EXPIRES_IN);
      
      return {
        accessToken,
        refreshToken,
      };
    } catch (error) {
      logger.error('Failed to generate token pair', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId: payload.userId,
      });
      throw new Error('Token generation failed');
    }
  }

  /**
   * Extracts token from Authorization header
   */
  public extractTokenFromHeader(authHeader?: string): string | null {
    if (!authHeader) {
      return null;
    }

    const parts = authHeader.split(' ');
    if (parts.length !== 2 || parts[0] !== 'Bearer') {
      logger.warn('Invalid authorization header format');
      return null;
    }

    return parts[1] || null;
  }

  /**
   * Checks if a token is expired without throwing an error
   */
  public isTokenExpired(token: string): boolean {
    try {
      jwt.verify(token, envConfig.JWT_SECRET);
      return false;
    } catch (error) {
      if (error instanceof jwt.TokenExpiredError) {
        return true;
      }
      // For other errors, consider token as invalid/expired
      return true;
    }
  }

  /**
   * Decodes token without verification (useful for extracting payload from expired tokens)
   */
  public decodeToken(token: string): JwtPayload | null {
    try {
      const decoded = jwt.decode(token) as JwtPayload;
      return decoded;
    } catch (error) {
      logger.warn('Failed to decode JWT token', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
      return null;
    }
  }
}

// Export singleton instance
export const jwtService = JwtService.getInstance(); 