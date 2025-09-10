/**
 * @swagger
 * tags:
 *   name: Admin
 *   description: Administrative endpoints
 */

/**
 * @swagger
 * /api/logs:
 *   get:
 *     summary: List server logs (developer/admin only)
 *     tags: [Admin]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: file
 *         schema:
 *           type: string
 *           enum: [combined, error]
 *         description: Which log file to read
 *       - in: query
 *         name: level
 *         schema:
 *           type: string
 *           enum: [error, warn, info, http, debug]
 *       - in: query
 *         name: q
 *         schema:
 *           type: string
 *         description: Case-insensitive substring match across the log JSON
 *       - in: query
 *         name: requestId
 *         schema:
 *           type: string
 *       - in: query
 *         name: from
 *         schema:
 *           type: string
 *           format: date-time
 *       - in: query
 *         name: to
 *         schema:
 *           type: string
 *           format: date-time
 *       - in: query
 *         name: order
 *         schema:
 *           type: string
 *           enum: [asc, desc]
 *         description: Sort by timestamp
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 500
 *     responses:
 *       200:
 *         description: Logs fetched
 */


