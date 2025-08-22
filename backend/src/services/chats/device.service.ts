import Device, { IDevice } from '../../models/Device';
import { createLogger } from '../../utils/logger';
import { AppError } from '../../middlewares/errorHandler';
import { Types } from 'mongoose';

export class DeviceService {
  private static instance: DeviceService;

  private constructor() {}

  public static getInstance(): DeviceService {
    if (!DeviceService.instance) {
      DeviceService.instance = new DeviceService();
    }
    return DeviceService.instance;
  }

  /**
   * Register or update a device for a user
   */
  public async registerDevice(
    userId: string,
    deviceId: string,
    publicKey: string,
    deviceName?: string,
    deviceType?: string,
    requestId?: string
  ): Promise<IDevice> {
    const loggerWithId = createLogger(requestId);

    try {
      // Validate inputs
      if (!deviceId || !publicKey) {
        throw new AppError('Device ID and public key are required', 400, 'INVALID_INPUT');
      }

      // Check if device already exists for this user
      const existingDevice = await Device.findOne({ 
        user: new Types.ObjectId(userId), 
        deviceId 
      });

      if (existingDevice) {
        // Update existing device
        existingDevice.publicKey = publicKey;
        if (deviceName !== undefined) {
          existingDevice.deviceName = deviceName;
        }
        if (deviceType !== undefined) {
          existingDevice.deviceType = deviceType;
        }
        existingDevice.lastSeen = new Date();
        existingDevice.isActive = true;

        const updatedDevice = await existingDevice.save();

        loggerWithId.info('Device updated', {
          userId,
          deviceId,
          deviceType: updatedDevice.deviceType,
        });

        return updatedDevice;
      } else {
        // Create new device
        const newDevice = await Device.create({
          user: new Types.ObjectId(userId),
          deviceId,
          publicKey,
          deviceName: deviceName || `Device ${deviceId.substring(0, 8)}`,
          deviceType: deviceType || 'android',
          lastSeen: new Date(),
          isActive: true,
        });

        loggerWithId.info('Device registered', {
          userId,
          deviceId,
          deviceType: newDevice.deviceType,
        });

        return newDevice;
      }
    } catch (error) {
      loggerWithId.error('Failed to register device', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        deviceId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to register device', 500, 'DEVICE_REGISTRATION_ERROR');
    }
  }

  /**
   * Get all active devices for a user
   */
  public async getUserDevices(userId: string, requestId?: string): Promise<IDevice[]> {
    const loggerWithId = createLogger(requestId);

    try {
      const devices = await Device.find({ 
        user: new Types.ObjectId(userId), 
        isActive: true 
      })
      .select('deviceId publicKey deviceName deviceType lastSeen')
      .sort({ lastSeen: -1 })
      .lean();

      loggerWithId.info('User devices retrieved', {
        userId,
        deviceCount: devices.length,
      });

      return devices as IDevice[];
    } catch (error) {
      loggerWithId.error('Failed to get user devices', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
      });
      throw new AppError('Failed to get user devices', 500, 'DEVICE_FETCH_ERROR');
    }
  }

  /**
   * Get devices for multiple users (for encryption)
   */
  public async getDevicesForUsers(userIds: string[], requestId?: string): Promise<Map<string, IDevice[]>> {
    const loggerWithId = createLogger(requestId);

    try {
      const objectIds = userIds.map(id => new Types.ObjectId(id));
      const devices = await Device.find({ 
        user: { $in: objectIds }, 
        isActive: true 
      })
      .select('user deviceId publicKey deviceName deviceType')
      .lean();

      // Group devices by user ID
      const deviceMap = new Map<string, IDevice[]>();
      userIds.forEach(userId => deviceMap.set(userId, []));

      devices.forEach(device => {
        const userId = device.user.toString();
        const userDevices = deviceMap.get(userId) || [];
        userDevices.push(device as IDevice);
        deviceMap.set(userId, userDevices);
      });

      loggerWithId.info('Devices for users retrieved', {
        userCount: userIds.length,
        totalDevices: devices.length,
      });

      return deviceMap;
    } catch (error) {
      loggerWithId.error('Failed to get devices for users', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userCount: userIds.length,
      });
      throw new AppError('Failed to get devices for users', 500, 'DEVICE_FETCH_ERROR');
    }
  }

  /**
   * Update device last seen timestamp
   */
  public async updateLastSeen(userId: string, deviceId: string, requestId?: string): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      const result = await Device.updateOne(
        { user: new Types.ObjectId(userId), deviceId, isActive: true },
        { lastSeen: new Date() }
      );

      if (result.matchedCount === 0) {
        throw new AppError('Device not found', 404, 'DEVICE_NOT_FOUND');
      }

      loggerWithId.debug('Device last seen updated', { deviceId, userId });
    } catch (error) {
      loggerWithId.error('Failed to update device last seen', {
        error: error instanceof Error ? error.message : 'Unknown error',
        deviceId,
        userId,
      });
      // Don't throw error for this operation as it's not critical
    }
  }

  /**
   * Deactivate a device
   */
  public async deactivateDevice(userId: string, deviceId: string, requestId?: string): Promise<void> {
    const loggerWithId = createLogger(requestId);

    try {
      const result = await Device.updateOne(
        { user: new Types.ObjectId(userId), deviceId },
        { isActive: false, lastSeen: new Date() }
      );

      if (result.matchedCount === 0) {
        throw new AppError('Device not found', 404, 'DEVICE_NOT_FOUND');
      }

      loggerWithId.info('Device deactivated', {
        userId,
        deviceId,
      });
    } catch (error) {
      loggerWithId.error('Failed to deactivate device', {
        error: error instanceof Error ? error.message : 'Unknown error',
        userId,
        deviceId,
      });

      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Failed to deactivate device', 500, 'DEVICE_DEACTIVATION_ERROR');
    }
  }

  /**
   * Get device by ID (for validation)
   */
  public async getDeviceById(deviceId: string, requestId?: string): Promise<IDevice | null> {
    const loggerWithId = createLogger(requestId);

    try {
      const device = await Device.findOne({ deviceId, isActive: true })
        .select('user deviceId publicKey deviceName deviceType lastSeen')
        .lean();

      return device as IDevice | null;
    } catch (error) {
      loggerWithId.error('Failed to get device by ID', {
        error: error instanceof Error ? error.message : 'Unknown error',
        deviceId,
      });
      throw new AppError('Failed to get device', 500, 'DEVICE_FETCH_ERROR');
    }
  }

  /**
   * Clean up inactive devices (called by cron job)
   */
  public async cleanupInactiveDevices(daysInactive: number = 90, requestId?: string): Promise<number> {
    const loggerWithId = createLogger(requestId);

    try {
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - daysInactive);

      const result = await Device.updateMany(
        { 
          lastSeen: { $lt: cutoffDate },
          isActive: true 
        },
        { isActive: false }
      );

      loggerWithId.info('Inactive devices cleaned up', {
        deactivatedCount: result.modifiedCount,
        cutoffDate,
      });

      return result.modifiedCount;
    } catch (error) {
      loggerWithId.error('Failed to cleanup inactive devices', {
        error: error instanceof Error ? error.message : 'Unknown error',
        daysInactive,
      });
      throw new AppError('Failed to cleanup inactive devices', 500, 'DEVICE_CLEANUP_ERROR');
    }
  }
}

export const deviceService = DeviceService.getInstance();
