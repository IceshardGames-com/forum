/**
 * @swagger
 * tags:
 *   name: Friends
 *   description: Friendship requests, lists, and suggestions
 */

/**
 * @swagger
 * /api/friends/request:
 *   post:
 *     summary: Send a friend request
 *     description: Creates a pending friend request to another user.
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [recipientId]
 *             properties:
 *               recipientId:
 *                 type: string
 *                 description: User ID to send request to
 *                 example: "507f1f77bcf86cd799439011"
 *     responses:
 *       201:
 *         description: Friend request created
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     request:
 *                       $ref: '#/components/schemas/Friendship'
 *                 message:
 *                   type: string
 *                   example: "Friend request created"
 *       400:
 *         description: Validation error or duplicate
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */

/**
 * @swagger
 * /api/friends/accept/{id}:
 *   patch:
 *     summary: Accept a friend request
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: Friend request ID
 *     responses:
 *       200:
 *         description: Friend request accepted
 *       404:
 *         description: Request not found
 */

/**
 * @swagger
 * /api/friends/decline/{id}:
 *   patch:
 *     summary: Decline a friend request
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Friend request declined
 *       404:
 *         description: Request not found
 */

/**
 * @swagger
 * /api/friends/cancel/{id}:
 *   delete:
 *     summary: Cancel a sent friend request
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Friend request canceled
 *       404:
 *         description: Request not found
 */

/**
 * @swagger
 * /api/friends/block:
 *   post:
 *     summary: Block a user
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [userId]
 *             properties:
 *               userId:
 *                 type: string
 *                 example: "507f1f77bcf86cd799439022"
 *     responses:
 *       200:
 *         description: User blocked
 */

/**
 * @swagger
 * /api/friends:
 *   get:
 *     summary: List friends
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
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
 *         description: Friends list
 */

/**
 * @swagger
 * /api/friends/requests:
 *   get:
 *     summary: List friend requests
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: type
 *         schema:
 *           type: string
 *           enum: [incoming, outgoing]
 *           default: incoming
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
 *         description: Requests list
 */

/**
 * @swagger
 * /api/friends/mutual/{userId}:
 *   get:
 *     summary: Get mutual friends with a user
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
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
 *         description: Mutual friends list
 */

/**
 * @swagger
 * /api/friends/suggestions:
 *   get:
 *     summary: Get friend suggestions
 *     tags: [Friends]
 *     security:
 *       - bearerAuth: []
 *     parameters:
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
 *         description: Suggestions list
 */

/**
 * @swagger
 * components:
 *   schemas:
 *     Friendship:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *           example: "64d2f1e9c7b2a1f0c9a1e2b3"
 *         requester:
 *           type: string
 *         recipient:
 *           type: string
 *         status:
 *           type: string
 *           enum: [pending, accepted, declined, blocked]
 *         createdAt:
 *           type: string
 *           format: date-time
 *         updatedAt:
 *           type: string
 *           format: date-time
 */


