import mongoose from 'mongoose';
import ForumPost from '../models/ForumPost';
import ForumComment from '../models/ForumComment';
import ForumPostReaction from '../models/ForumPostReaction';
import ForumCommentReaction from '../models/ForumCommentReaction';
import { logger } from '../utils/logger';

// Aggregates reaction counts periodically to keep counters consistent.
export const runReactionsAggregation = async (): Promise<void> => {
  try {
    // Aggregate posts
    const postAgg = await ForumPostReaction.aggregate([
      { $group: { _id: { post: '$post', type: '$type' }, count: { $sum: 1 } } },
    ]);
    const postCounts: Record<string, { likes: number; dislikes: number }> = {};
    for (const row of postAgg) {
      const key = String(row._id.post);
      if (!postCounts[key]) postCounts[key] = { likes: 0, dislikes: 0 };
      if (row._id.type === 'like') postCounts[key].likes = row.count;
      if (row._id.type === 'dislike') postCounts[key].dislikes = row.count;
    }
    const postBulk = Object.entries(postCounts).map(([postId, counts]) => ({
      updateOne: {
        filter: { _id: new mongoose.Types.ObjectId(postId) },
        update: { $set: { likes: counts.likes, dislikes: counts.dislikes } },
      },
    }));
    if (postBulk.length) await ForumPost.bulkWrite(postBulk);

    // Aggregate comments
    const commentAgg = await ForumCommentReaction.aggregate([
      { $group: { _id: { comment: '$comment', type: '$type' }, count: { $sum: 1 } } },
    ]);
    const commentCounts: Record<string, { likes: number; dislikes: number }> = {};
    for (const row of commentAgg) {
      const key = String(row._id.comment);
      if (!commentCounts[key]) commentCounts[key] = { likes: 0, dislikes: 0 };
      if (row._id.type === 'like') commentCounts[key].likes = row.count;
      if (row._id.type === 'dislike') commentCounts[key].dislikes = row.count;
    }
    const commentBulk = Object.entries(commentCounts).map(([commentId, counts]) => ({
      updateOne: {
        filter: { _id: new mongoose.Types.ObjectId(commentId) },
        update: { $set: { likes: counts.likes, dislikes: counts.dislikes } },
      },
    }));
    if (commentBulk.length) await ForumComment.bulkWrite(commentBulk);

    logger.info('Reactions aggregation completed', { posts: postBulk.length, comments: commentBulk.length });
  } catch (error) {
    logger.error('Reactions aggregation failed', { error: (error as Error).message });
  }
};

// Helper to start periodic aggregation within the running process (optional)
export const startReactionsWorker = (intervalMs = 30000): void => {
  setInterval(() => {
    void runReactionsAggregation();
  }, Math.max(10000, intervalMs));
};


