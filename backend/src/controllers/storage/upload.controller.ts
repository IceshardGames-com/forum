import { Request, Response } from 'express';
import { asyncErrorHandler } from '../../middlewares/errorHandler';
import r2Service from '../../services/storage/r2.service';
import imagesService from '../../services/storage/images.service';

export const presignR2Put = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const contentType = (req.body?.contentType as string) || 'application/octet-stream';
  const keyPrefix = (req.body?.keyPrefix as string) || 'uploads';
  const presigned = await r2Service.presignPut({ contentType, keyPrefix, expiresInSeconds: 300 });
  res.status(200).json({ success: true, data: presigned, message: 'Presigned URL generated', requestId: req.id, timestamp: new Date().toISOString() });
});

export const createImagesDirectUpload = asyncErrorHandler(async (_req: Request, res: Response): Promise<void> => {
  const result = await imagesService.createDirectUpload({ expiry: 600, requireSignedURLs: false });
  res.status(200).json({ success: true, data: result, message: 'Direct upload created', requestId: res.req.id, timestamp: new Date().toISOString() });
});

export default { presignR2Put, createImagesDirectUpload };

