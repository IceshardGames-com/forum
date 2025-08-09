/**
 * @swagger
 * tags:
 *   name: Interests
 *   description: Gaming interests catalog
 */

/**
 * @swagger
 * /api/interests:
 *   get:
 *     summary: List gaming interests
 *     description: Returns paginated gaming interests with optional search over label and value.
 *     tags: [Interests]
 *     security: []
 *     parameters:
 *       - in: query
 *         name: q
 *         schema:
 *           type: string
 *         description: Search term (matches label or value, case-insensitive)
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *     responses:
 *       200:
 *         description: Interests retrieved
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 data:
 *                   type: object
 *                   properties:
 *                     items:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           _id:
 *                             type: string
 *                           label:
 *                             type: string
 *                           value:
 *                             type: string
 *                     page:
 *                       type: integer
 *                     limit:
 *                       type: integer
 *                     total:
 *                       type: integer
 *                 message:
 *                   type: string
 */


