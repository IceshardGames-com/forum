/**
 * @swagger
 * tags:
 *   name: Notification Preferences
 *   description: User notification preferences management
 */

/**
 * @swagger
 * /api/notifications/preferences:
 *   get:
 *     summary: Get user notification preferences
 *     description: Returns the current notification preferences for the authenticated user.
 *     tags: [Notification Preferences]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Notification preferences retrieved successfully
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
 *                     preferences:
 *                       type: object
 *                       properties:
 *                         friend_request:
 *                           type: boolean
 *                         friend_accepted:
 *                           type: boolean
 *                         system:
 *                           type: boolean
 *                         game_feedback:
 *                           type: boolean
 *                         message:
 *                           type: boolean
 *                     emailNotifications:
 *                       type: boolean
 *                     pushNotifications:
 *                       type: boolean
 *                     updatedAt:
 *                       type: string
 *                       format: date-time
 *   put:
 *     summary: Update user notification preferences
 *     description: Updates the notification preferences for the authenticated user.
 *     tags: [Notification Preferences]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               preferences:
 *                 type: object
 *                 properties:
 *                   friend_request:
 *                     type: boolean
 *                   friend_accepted:
 *                     type: boolean
 *                   system:
 *                     type: boolean
 *                   game_feedback:
 *                     type: boolean
 *                   message:
 *                     type: boolean
 *               emailNotifications:
 *                 type: boolean
 *               pushNotifications:
 *                 type: boolean
 *     responses:
 *       200:
 *         description: Notification preferences updated successfully
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
 *                     preferences:
 *                       type: object
 *                       properties:
 *                         friend_request:
 *                           type: boolean
 *                         friend_accepted:
 *                           type: boolean
 *                         system:
 *                           type: boolean
 *                         game_feedback:
 *                           type: boolean
 *                         message:
 *                           type: boolean
 *                     emailNotifications:
 *                       type: boolean
 *                     pushNotifications:
 *                       type: boolean
 *                     updatedAt:
 *                       type: string
 *                       format: date-time

/**
 * @swagger
 * /api/notifications/preferences/reset:
 *   post:
 *     summary: Reset notification preferences to defaults
 *     description: Resets all notification preferences to default values.
 *     tags: [Notification Preferences]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Notification preferences reset successfully
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
 *                     preferences:
 *                       type: object
 *                       properties:
 *                         friend_request:
 *                           type: boolean
 *                         friend_accepted:
 *                           type: boolean
 *                         system:
 *                           type: boolean
 *                         game_feedback:
 *                           type: boolean
 *                         message:
 *                           type: boolean
 *                     emailNotifications:
 *                       type: boolean
 *                     pushNotifications:
 *                       type: boolean
 *                     updatedAt:
 *                       type: string
 *                       format: date-time
 */
