import { Router } from 'express';
import { authenticate } from '../middlewares/auth';
import { validateBody } from '../middlewares/validate';
import {
  sendFriendRequest,
  acceptFriendRequest,
  declineFriendRequest,
  cancelFriendRequest,
  blockUser,
  listFriends,
  listRequests,
  getMutualFriends,
  getSuggestions,
  removeFriend,
} from '../controllers/social/friendship.controller';
import { blockUserValidation, sendRequestValidation } from '../validations/friendship.validation';

const router = Router();

router.use(authenticate);

// Create / manage requests
router.post('/request', validateBody(sendRequestValidation), sendFriendRequest);
router.patch('/accept/:id', acceptFriendRequest);
router.patch('/decline/:id', declineFriendRequest);
router.delete('/cancel/:id', cancelFriendRequest);

// Block
router.post('/block', validateBody(blockUserValidation), blockUser);
// Remove
router.post('/remove', validateBody(blockUserValidation), removeFriend);

// Lists
router.get('/', listFriends);
router.get('/requests', listRequests); // ?type=incoming|outgoing
router.get('/mutual/:userId', getMutualFriends);
router.get('/suggestions', getSuggestions);

export default router;


