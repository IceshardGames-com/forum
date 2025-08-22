import { Request, Response } from 'express';
import { deviceService } from '../../services/chats/device.service';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import { createLogger } from '../../utils/logger';

interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message: string;
  requestId?: string | undefined;
  timestamp: string;
}

export const registerDevice = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;
  const { deviceId, publicKey, deviceName, deviceType } = req.body;

  const device = await deviceService.registerDevice(
    userId,
    deviceId,
    publicKey,
    deviceName,
    deviceType,
    req.id
  );

  logger.info('Device registered successfully', {
    userId,
    deviceId: device.deviceId,
    deviceType: device.deviceType,
  });

  const response: ApiResponse = {
    success: true,
    data: {
      deviceId: device.deviceId,
      deviceName: device.deviceName,
      deviceType: device.deviceType,
      lastSeen: device.lastSeen,
    },
    message: 'Device registered successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getUserDevices = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;

  const devices = await deviceService.getUserDevices(userId, req.id);

  const response: ApiResponse = {
    success: true,
    data: {
      devices: devices.map(device => ({
        deviceId: device.deviceId,
        deviceName: device.deviceName,
        deviceType: device.deviceType,
        lastSeen: device.lastSeen,
      })),
      count: devices.length,
    },
    message: 'Devices retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const getDevicesForUser = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const { userId } = req.params as { userId: string };

  // Get devices with public keys for encryption
  const devices = await deviceService.getUserDevices(userId, req.id);

  const response: ApiResponse = {
    success: true,
    data: {
      devices: devices.map(device => ({
        deviceId: device.deviceId,
        publicKey: device.publicKey,
        deviceName: device.deviceName,
        deviceType: device.deviceType,
      })),
      count: devices.length,
    },
    message: 'User devices retrieved successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const updateDeviceLastSeen = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const userId = req.user!._id;
  const { deviceId } = req.body;

  await deviceService.updateLastSeen(userId, deviceId, req.id);

  const response: ApiResponse = {
    success: true,
    message: 'Device last seen updated',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});

export const deactivateDevice = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const userId = req.user!._id;
  const { deviceId } = req.params as { deviceId: string };

  await deviceService.deactivateDevice(userId, deviceId, req.id);

  logger.info('Device deactivated', {
    userId,
    deviceId,
  });

  const response: ApiResponse = {
    success: true,
    message: 'Device deactivated successfully',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  };

  res.status(200).json(response);
});
