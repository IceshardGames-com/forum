import { Request, Response } from 'express';
import fs from 'fs';
import readline from 'readline';
import path from 'path';
import { asyncErrorHandler } from '../../middlewares/errorHandler';

type LogRecord = Record<string, any> & { timestamp?: string; level?: string; message?: string; requestId?: string };

const resolveLogPath = (file: 'combined' | 'error'): string => {
  // Logs are written by Winston to `logs/*.log` relative to the server CWD
  return path.join(process.cwd(), 'logs', file === 'error' ? 'error.log' : 'combined.log');
};

export const listLogs = asyncErrorHandler(async (req: Request, res: Response): Promise<void> => {
  const file = (req.query.file as string) === 'error' ? 'error' : 'combined';
  const level = (req.query.level as string) || undefined;
  const q = (req.query.q as string) || undefined;
  const requestId = (req.query.requestId as string) || undefined;
  const order = ((req.query.order as string) === 'asc' ? 'asc' : 'desc');
  const limit = Math.min(Number(req.query.limit) || 100, 500);
  const from = req.query.from ? new Date(String(req.query.from)) : undefined;
  const to = req.query.to ? new Date(String(req.query.to)) : undefined;

  const logPath = resolveLogPath(file);
  if (!fs.existsSync(logPath)) {
    res.status(200).json({ success: true, data: { items: [], file }, message: 'No log file', requestId: req.id, timestamp: new Date().toISOString() });
    return;
  }

  const items: LogRecord[] = [];

  const stream = fs.createReadStream(logPath, { encoding: 'utf8' });
  const rl = readline.createInterface({ input: stream, crlfDelay: Infinity });

  for await (const line of rl) {
    if (!line.trim()) continue;
    let rec: LogRecord | null = null;
    try {
      rec = JSON.parse(line);
    } catch {
      continue;
    }

    if (rec) {
      // time filter
      if (from || to) {
        const ts = rec.timestamp ? new Date(rec.timestamp) : undefined;
        if (!ts) continue;
        if (from && ts < from) continue;
        if (to && ts > to) continue;
      }

      if (level && rec.level !== level) continue;
      if (requestId && rec.requestId !== requestId) continue;
      if (q) {
        const hay = JSON.stringify(rec).toLowerCase();
        if (!hay.includes(q.toLowerCase())) continue;
      }
      items.push(rec);
    }
  }

  // order and limit
  items.sort((a, b) => {
    const ta = a.timestamp ? new Date(a.timestamp).getTime() : 0;
    const tb = b.timestamp ? new Date(b.timestamp).getTime() : 0;
    return order === 'asc' ? ta - tb : tb - ta;
  });

  const sliced = items.slice(0, limit);

  res.status(200).json({ success: true, data: { items: sliced, file, count: sliced.length }, message: 'Logs fetched', requestId: req.id, timestamp: new Date().toISOString() });
});

export default { listLogs };

