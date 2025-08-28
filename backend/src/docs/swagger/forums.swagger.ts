/**
 * @swagger
 * tags:
 *   name: Forums
 *   description: Forum/community endpoints
 */

/**
 * @swagger
 * /api/forums:
 *   post:
 *     summary: Create a forum
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, slug]
 *             properties:
 *               name:
 *                 type: string
 *               slug:
 *                 type: string
 *               description:
 *                 type: string
 *               verified:
 *                 type: boolean
 *               postPermission:
 *                 type: string
 *                 enum: [admin_only, followers, members]
 *     responses:
 *       201:
 *         description: Forum created
 */

/**
 * @swagger
 * /api/forums/slug/{slug}:
 *   get:
 *     summary: Get a forum by slug
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: slug
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Forum retrieved
 */

/**
 * @swagger
 * /api/forums/{forumId}/follow:
 *   post:
 *     summary: Follow a forum
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Followed
 */

/**
 * @swagger
 * /api/forums/{forumId}/unfollow:
 *   post:
 *     summary: Unfollow a forum
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Unfollowed
 */

/**
 * @swagger
 * /api/forums/{forumId}/join:
 *   post:
 *     summary: Join a forum (become a member)
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Joined
 */

/**
 * @swagger
 * /api/forums/{forumId}/leave:
 *   post:
 *     summary: Leave a forum
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Left
 */

/**
 * @swagger
 * /api/forums/{forumId}/posts:
 *   post:
 *     summary: Create a post
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [title, content]
 *             properties:
 *               title:
 *                 type: string
 *               content:
 *                 type: string
 *     responses:
 *       201:
 *         description: Post created
 *   get:
 *     summary: List posts
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Posts
 */

/**
 * @swagger
 * /api/forums/posts/{postId}/comments:
 *   post:
 *     summary: Add comment (nested via parentCommentId)
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: postId
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [content]
 *             properties:
 *               content:
 *                 type: string
 *               parentCommentId:
 *                 type: string
 *                 description: Optional parent comment ObjectId
 *     responses:
 *       201:
 *         description: Comment created
 *   get:
 *     summary: List comments (root or children when parentCommentId provided)
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: postId
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: parentCommentId
 *         schema:
 *           type: string
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Comments
 */

/**
 * @swagger
 * /api/forums/posts/{postId}/like:
 *   post:
 *     summary: Toggle like on a post
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: postId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Toggled
 */

/**
 * @swagger
 * /api/forums/posts/{postId}/dislike:
 *   post:
 *     summary: Toggle dislike on a post
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: postId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Toggled
 */

/**
 * @swagger
 * /api/forums/comments/{commentId}/like:
 *   post:
 *     summary: Toggle like on a comment
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: commentId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Toggled
 */

/**
 * @swagger
 * /api/forums/comments/{commentId}/dislike:
 *   post:
 *     summary: Toggle dislike on a comment
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: commentId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Toggled
 */

/**
 * @swagger
 * /api/forums/{forumId}/members/role:
 *   post:
 *     summary: Change member role (owner only)
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: forumId
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [userId, role]
 *             properties:
 *               userId:
 *                 type: string
 *               role:
 *                 type: string
 *                 enum: [admin, moderator, member]
 *     responses:
 *       200:
 *         description: Role updated
 */

/**
 * @swagger
 * /api/forums/interactions/bulk:
 *   post:
 *     summary: Process multiple interactions in one request
 *     tags: [Forums]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [operations]
 *             properties:
 *               operations:
 *                 type: array
 *                 minItems: 1
 *                 items:
 *                   oneOf:
 *                     - type: object
 *                       required: [op, postId, type]
 *                       properties:
 *                         op:
 *                           type: string
 *                           enum: [post_reaction]
 *                         postId:
 *                           type: string
 *                         type:
 *                           type: string
 *                           enum: [like, dislike]
 *                     - type: object
 *                       required: [op, commentId, type]
 *                       properties:
 *                         op:
 *                           type: string
 *                           enum: [comment_reaction]
 *                         commentId:
 *                           type: string
 *                         type:
 *                           type: string
 *                           enum: [like, dislike]
 *                     - type: object
 *                       required: [op, postId]
 *                       properties:
 *                         op:
 *                           type: string
 *                           enum: [post_share]
 *                         postId:
 *                           type: string
 *     responses:
 *       200:
 *         description: Bulk interactions processed
 */


