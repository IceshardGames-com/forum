/**
 * @swagger
 * tags:
 *   name: Users
 *   description: User search, discovery, and profile management
 */

/**
 * @swagger
 * /api/users/search:
 *   get:
 *     summary: Search users by username or email
 *     description: Search for users using username or email with optional filtering by role and activity status
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: q
 *         required: true
 *         schema:
 *           type: string
 *           minLength: 2
 *           maxLength: 50
 *         description: Search query (username or email)
 *         example: "john_doe"
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           minimum: 1
 *           default: 1
 *         description: Page number for pagination
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 50
 *           default: 20
 *         description: Number of results per page
 *       - in: query
 *         name: role
 *         schema:
 *           type: string
 *           enum: [gamer, developer, admin]
 *         description: Filter by user role
 *       - in: query
 *         name: includeInactive
 *         schema:
 *           type: boolean
 *           default: false
 *         description: Include inactive users in results
 *     responses:
 *       200:
 *         description: Users found successfully
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
 *                     users:
 *                       type: array
 *                       items:
 *                         $ref: '#/components/schemas/UserProfile'
 *                     pagination:
 *                       $ref: '#/components/schemas/Pagination'
 *                     query:
 *                       type: string
 *                       example: "john_doe"
 *                     filters:
 *                       type: object
 *                       properties:
 *                         role:
 *                           type: string
 *                           example: "gamer"
 *                         includeInactive:
 *                           type: boolean
 *                           example: false
 *                 message:
 *                   type: string
 *                   example: "Users retrieved successfully"
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       401:
 *         description: Unauthorized
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */

/**
 * @swagger
 * /api/users/suggestions:
 *   get:
 *     summary: Get user suggestions
 *     description: Get personalized user suggestions based on shared interests and activity
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           minimum: 1
 *           default: 1
 *         description: Page number for pagination
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 50
 *           default: 20
 *         description: Number of results per page
 *     responses:
 *       200:
 *         description: User suggestions retrieved successfully
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
 *                     users:
 *                       type: array
 *                       items:
 *                         allOf:
 *                           - $ref: '#/components/schemas/UserProfile'
 *                           - type: object
 *                             properties:
 *                               commonInterests:
 *                                 type: integer
 *                                 description: Number of shared interests
 *                                 example: 3
 *                     pagination:
 *                       $ref: '#/components/schemas/Pagination'
 *                 message:
 *                   type: string
 *                   example: "User suggestions retrieved successfully"
 *       401:
 *         description: Unauthorized
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */

/**
 * @swagger
 * /api/users/{userId}:
 *   get:
 *     summary: Get user profile by ID
 *     description: Retrieve a user's profile information by their ID
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: User ObjectId
 *         example: "64d2f1e9c7b2a1f0c9a1e2b3"
 *     responses:
 *       200:
 *         description: User profile retrieved successfully
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
 *                     user:
 *                       $ref: '#/components/schemas/UserProfile'
 *                 message:
 *                   type: string
 *                   example: "User profile retrieved successfully"
 *       400:
 *         description: Invalid user ID format
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       404:
 *         description: User not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       401:
 *         description: Unauthorized
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */

/**
 * @swagger
 * /api/users/by-role/{role}:
 *   get:
 *     summary: Get users by role
 *     description: Retrieve users filtered by their role
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: role
 *         required: true
 *         schema:
 *           type: string
 *           enum: [gamer, developer, admin]
 *         description: User role to filter by
 *         example: "gamer"
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           minimum: 1
 *           default: 1
 *         description: Page number for pagination
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 50
 *           default: 20
 *         description: Number of results per page
 *     responses:
 *       200:
 *         description: Users retrieved successfully by role
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
 *                     users:
 *                       type: array
 *                       items:
 *                         $ref: '#/components/schemas/UserProfile'
 *                     pagination:
 *                       $ref: '#/components/schemas/Pagination'
 *                     role:
 *                       type: string
 *                       example: "gamer"
 *                 message:
 *                   type: string
 *                   example: "gamer users retrieved successfully"
 *       400:
 *         description: Invalid role
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       401:
 *         description: Unauthorized
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */

/**
 * @swagger
 * components:
 *   schemas:
 *     UserProfile:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *           example: "64d2f1e9c7b2a1f0c9a1e2b3"
 *         username:
 *           type: string
 *           example: "john_doe"
 *         email:
 *           type: string
 *           format: email
 *           example: "john@example.com"
 *           description: "Only visible to the user themselves"
 *         role:
 *           type: string
 *           enum: [gamer, developer, admin]
 *           example: "gamer"
 *         isActive:
 *           type: boolean
 *           example: true
 *         createdAt:
 *           type: string
 *           format: date-time
 *           example: "2023-08-08T10:30:00.000Z"
 *         interests:
 *           type: array
 *           items:
 *             type: string
 *           example: ["64d2f1e9c7b2a1f0c9a1e2b4", "64d2f1e9c7b2a1f0c9a1e2b5"]
 *           description: "Array of ObjectIds referencing GamingInterest documents"
 *     
 *     Pagination:
 *       type: object
 *       properties:
 *         page:
 *           type: integer
 *           example: 1
 *         limit:
 *           type: integer
 *           example: 20
 *         total:
 *           type: integer
 *           example: 150
 *         hasNext:
 *           type: boolean
 *           example: true
 *         hasPrev:
 *           type: boolean
 *           example: false
 */
