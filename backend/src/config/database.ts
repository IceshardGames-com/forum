import mongoose from 'mongoose';
import { envConfig } from './env';
import { logger } from '../utils/logger';

export class DatabaseConnection {
  private static instance: DatabaseConnection;
  private connection: typeof mongoose | null = null;

  private constructor() {}

  public static getInstance(): DatabaseConnection {
    if (!DatabaseConnection.instance) {
      DatabaseConnection.instance = new DatabaseConnection();
    }
    return DatabaseConnection.instance;
  }

  public async connect(): Promise<void> {
    try {
      if (this.connection) {
        logger.info('Database already connected');
        return;
      }

      const connectionOptions: mongoose.ConnectOptions = {
        maxPoolSize: 10,
        serverSelectionTimeoutMS: 5000,
        socketTimeoutMS: 45000,
        bufferCommands: false,
      };

      logger.info('Connecting to MongoDB...', { 
        uri: envConfig.MONGODB_URI.replace(/\/\/[^:]+:[^@]+@/, '//***:***@') // Hide credentials in logs
      });

      this.connection = await mongoose.connect(envConfig.MONGODB_URI, connectionOptions);

      // Set up event listeners
      mongoose.connection.on('connected', () => {
        logger.info('MongoDB connected successfully', { database: envConfig.DB_NAME });
      });

      mongoose.connection.on('error', (error) => {
        logger.error('MongoDB connection error', { error: error.message });
      });

      mongoose.connection.on('disconnected', () => {
        logger.warn('MongoDB disconnected');
      });

      // Graceful shutdown
      process.on('SIGINT', this.gracefulShutdown.bind(this));
      process.on('SIGTERM', this.gracefulShutdown.bind(this));

    } catch (error) {
      logger.error('Failed to connect to MongoDB', { 
        error: error instanceof Error ? error.message : 'Unknown error',
        uri: envConfig.MONGODB_URI.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')
      });
      throw error;
    }
  }

  public async disconnect(): Promise<void> {
    try {
      if (this.connection) {
        await mongoose.disconnect();
        this.connection = null;
        logger.info('MongoDB disconnected successfully');
      }
    } catch (error) {
      logger.error('Error disconnecting from MongoDB', { 
        error: error instanceof Error ? error.message : 'Unknown error' 
      });
      throw error;
    }
  }

  public isConnected(): boolean {
    return mongoose.connection.readyState === 1;
  }

  public getConnection(): typeof mongoose | null {
    return this.connection;
  }

  private async gracefulShutdown(signal: string): Promise<void> {
    logger.info(`Received ${signal}. Starting graceful shutdown...`);
    try {
      await this.disconnect();
      process.exit(0);
    } catch (error) {
      logger.error('Error during graceful shutdown', { 
        error: error instanceof Error ? error.message : 'Unknown error' 
      });
      process.exit(1);
    }
  }
}

// Export singleton instance
export const database = DatabaseConnection.getInstance(); 