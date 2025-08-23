/**
 * @swagger
 * tags:
 *   name: Messages
 *   description: Chat messaging endpoints
 */

/**
 * @swagger
 * /api/messages/conversations/{conversationId}:
 *   post:
 *     summary: Send encrypted message to conversation
 *     description: Send an encrypted message to a specific conversation with device-specific payloads
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: conversationId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the conversation
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - payloads
 *             properties:
 *               payloads:
 *                 type: array
 *                 minItems: 1
 *                 items:
 *                   type: object
 *                   required:
 *                     - deviceId
 *                     - ciphertext
 *                   properties:
 *                     deviceId:
 *                       type: string
 *                       minLength: 1
 *                       maxLength: 255
 *                       description: Target device ID for the encrypted payload
 *                     ciphertext:
 *                       type: string
 *                       minLength: 1
 *                       description: Base64 encoded encrypted message content
 *                     ephemeralPublicKey:
 *                       type: string
 *                       description: Ephemeral public key for forward secrecy
 *                     nonce:
 *                       type: string
 *                       description: Initialization vector for encryption
 *                     metadata:
 *                       type: object
 *                       description: Additional encryption metadata
 *               messageType:
 *                 type: string
 *                 enum: [text, image, file, system]
 *                 default: text
 *                 description: Type of message being sent
 *               replyTo:
 *                 type: string
 *                 pattern: '^[0-9a-fA-F]{24}$'
 *                 description: MongoDB ObjectId of the message being replied to
 *     responses:
 *       201:
 *         description: Message sent successfully
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
 *                     messageId:
 *                       type: string
 *                     conversationId:
 *                       type: string
 *                     messageType:
 *                       type: string
 *                     createdAt:
 *                       type: string
 *                       format: date-time
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Validation error or invalid conversation
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       403:
 *         description: Forbidden - User not allowed to send message to this conversation
 *       404:
 *         description: Conversation not found
 *       429:
 *         description: Rate limit exceeded
 *
 *   get:
 *     summary: Get conversation messages
 *     description: Retrieve paginated messages from a specific conversation
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: conversationId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the conversation
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
 *           maximum: 100
 *           default: 50
 *         description: Number of messages per page
 *     responses:
 *       200:
 *         description: Messages retrieved successfully
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
 *                     messages:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           messageId:
 *                             type: string
 *                           conversationId:
 *                             type: string
 *                           sender:
 *                             type: object
 *                             properties:
 *                               _id:
 *                                 type: string
 *                               username:
 *                                 type: string
 *                               avatar:
 *                                 type: string
 *                           payloads:
 *                             type: array
 *                             description: Encrypted payloads for different devices
 *                           messageType:
 *                             type: string
 *                           replyTo:
 *                             type: object
 *                           isEdited:
 *                             type: boolean
 *                           editedAt:
 *                             type: string
 *                             format: date-time
 *                           deliveredTo:
 *                             type: array
 *                             items:
 *                               type: string
 *                           readBy:
 *                             type: array
 *                             items:
 *                               type: string
 *                           createdAt:
 *                             type: string
 *                             format: date-time
 *                     pagination:
 *                       type: object
 *                       properties:
 *                         page:
 *                           type: integer
 *                         limit:
 *                           type: integer
 *                         total:
 *                           type: integer
 *                         pages:
 *                           type: integer
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Validation error
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       404:
 *         description: Conversation not found
 *
 * /api/messages/{messageId}/delivered:
 *   patch:
 *     summary: Mark message as delivered
 *     description: Mark a specific message as delivered to the current user
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: messageId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the message
 *     responses:
 *       200:
 *         description: Message marked as delivered
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Validation error
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       403:
 *         description: Forbidden - User not a participant in conversation
 *       404:
 *         description: Message not found
 *
 * /api/messages/{messageId}/read:
 *   patch:
 *     summary: Mark message as read
 *     description: Mark a specific message as read by the current user
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: messageId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the message
 *     responses:
 *       200:
 *         description: Message marked as read
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Validation error
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       403:
 *         description: Forbidden - User not a participant in conversation
 *       404:
 *         description: Message not found
 *
 * /api/messages/{messageId}/device/{deviceId}:
 *   get:
 *     summary: Get message payload for specific device
 *     description: Retrieve the encrypted payload for a specific device from a message
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: messageId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the message
 *       - in: path
 *         name: deviceId
 *         required: true
 *         schema:
 *           type: string
 *           minLength: 1
 *           maxLength: 255
 *         description: Device ID to get payload for
 *     responses:
 *       200:
 *         description: Message payload retrieved successfully
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
 *                     payload:
 *                       type: object
 *                       properties:
 *                         deviceId:
 *                           type: string
 *                         ciphertext:
 *                           type: string
 *                         ephemeralPublicKey:
 *                           type: string
 *                         nonce:
 *                           type: string
 *                         metadata:
 *                           type: object
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Validation error
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       403:
 *         description: Forbidden - Device not owned by user or user not in conversation
 *       404:
 *         description: Message or payload not found
 */
