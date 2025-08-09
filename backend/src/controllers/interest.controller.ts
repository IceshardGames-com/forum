import { Request, Response } from 'express';
import GamingInterest from '../models/GamingInterest';
import { asyncErrorHandler } from '../middlewares/errorHandler';
import { createLogger } from '../utils/logger';

export const listInterests = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const logger = createLogger(req.id);
  const q = (req.query.q as string | undefined)?.trim().toLowerCase();
  const page = Math.max(parseInt((req.query.page as string) || '1', 10), 1);
  const limit = Math.min(Math.max(parseInt((req.query.limit as string) || '20', 10), 1), 50);
  const skip = (page - 1) * limit;

  const filter: any = {};
  if (q && q.length > 0) {
    // Search over label and value (case-insensitive)
    filter.$or = [
      { label: { $regex: q, $options: 'i' } },
      { value: { $regex: q, $options: 'i' } },
    ];
  }

  const [items, total] = await Promise.all([
    GamingInterest.find(filter)
      .sort({ label: 1 })
      .skip(skip)
      .limit(limit)
      .lean(),
    GamingInterest.countDocuments(filter),
  ]);

  logger.info('Interests listed', { q, page, limit, total });
  res.status(200).json({
    success: true,
    data: { items, page, limit, total },
    message: 'Interests retrieved',
    requestId: req.id,
    timestamp: new Date().toISOString(),
  });
});


