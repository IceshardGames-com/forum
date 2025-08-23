/**
 * @swagger
 * tags:
 *   name: Devices
 *   description: Device registration and management for encrypted chat
 */

/**
 * @swagger
 * /api/devices/register:
 *   post:
 *     summary: Register or update device
 *     description: Register a new device for the authenticated user or update existing device
 *     tags: [Devices]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - deviceId
 *               - publicKey
 *             properties:
 *               deviceId:
 *                 type: string
 *                 minLength: 1
 *                 maxLength: 255
 *                 description: Unique identifier for the device
 *               publicKey:
 *                 type: string
 *                 minLength: 1
 *                 description: Public key for end-to-end encryption
 *               deviceName:
 *                 type: string
 *                 maxLength: 100
 *                 description: Human-readable name for the device
 *               deviceType:
 *                 type: string
 *                 enum: [android, ios, web, desktop]
 *                 default: android
 *                 description: Type of device
 *     responses:
 *       200:
 *         description: Device registered or updated successfully
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
 *                     deviceId:
 *                       type: string
 *                     deviceName:
 *                       type: string
 *                     deviceType:
 *                       type: string
 *                     lastSeen:
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
 *         description: Validation error or missing required fields
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *       429:
 *         description: Rate limit exceeded
 *
 * /api/devices/my-devices:
 *   get:
 *     summary: Get current user's devices
 *     description: Retrieve all active devices registered by the authenticated user
 *     tags: [Devices]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: User devices retrieved successfully
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
 *                     devices:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           deviceId:
 *                             type: string
 *                           deviceName:
 *                             type: string
 *                           deviceType:
 *                             type: string
 *                           lastSeen:
 *                             type: string
 *                             format: date-time
 *                     count:
 *                       type: integer
 *                       description: Total number of devices
 *                 message:
 *                   type: string
 *                 requestId:
 *                   type: string
 *                 timestamp:
 *                   type: string
 *                   format: date-time
 *       401:
 *         description: Unauthorized - Invalid or missing token
 *
 * /api/devices/user/{userId}:
 *   get:
 *     summary: Get devices for specific user
 *     description: Retrieve devices for a specific user (includes public keys for encryption)
 *     tags: [Devices]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           pattern: '^[0-9a-fA-F]{24}$'
 *         description: MongoDB ObjectId of the user
 *     responses:
 *       200:
 *         description: User devices retrieved successfully
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
 *                     devices:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           deviceId:
 *                             type: string
 *                           publicKey:
 *                             type: string
 *                             description: Public key for encryption
 *                           deviceName:
 *                             type: string
 *                           deviceType:
 *                             type: string
 *                     count:
 *                       type: integer
 *                       description: Total number of devices
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
 *         description: User not found
 *
 * /api/devices/last-seen:
 *   patch:
 *     summary: Update device last seen timestamp
 *     description: Update the last seen timestamp for a specific device owned by the user
 *     tags: [Devices]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - deviceId
 *             properties:
 *               deviceId:
 *                 type: string
 *                 minLength: 1
 *                 maxLength: 255
 *                 description: ID of the device to update
 *     responses:
 *       200:
 *         description: Device last seen updated successfully
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
 *       404:
 *         description: Device not found
 *
 * /api/devices/{deviceId}:
 *   delete:
 *     summary: Deactivate device
 *     description: Deactivate a specific device owned by the authenticated user
 *     tags: [Devices]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: deviceId
 *         required: true
 *         schema:
 *           type: string
 *           minLength: 1
 *           maxLength: 255
 *         description: ID of the device to deactivate
 *     responses:
 *       200:
 *         description: Device deactivated successfully
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
 *       404:
 *         description: Device not found
 */
