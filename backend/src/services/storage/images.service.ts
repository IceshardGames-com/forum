import axios from 'axios';
import { envConfig } from '../../config/env';

export interface CreateDirectUploadParams {
  expiry?: number; // seconds, max 3600
  requireSignedURLs?: boolean;
  metadata?: Record<string, string>;
}

export interface DirectUploadResult {
  uploadURL: string;
  id: string; // image id to use later in delivery URL
}

export interface DeleteImageResult {
  id: string;
  deleted: boolean;
}

const BASE = `https://api.cloudflare.com/client/v4/accounts/${envConfig.CF_IMAGES_ACCOUNT_ID}/images/v2/direct_upload`;

export const imagesService = {
  async createDirectUpload(params: CreateDirectUploadParams = {}): Promise<DirectUploadResult> {
    const body: any = {};
    if (params.expiry) body.expiry = Math.min(params.expiry, 3600);
    if (params.requireSignedURLs !== undefined) body.requireSignedURLs = params.requireSignedURLs;
    if (params.metadata) body.metadata = params.metadata;

    const res = await axios.post(
      BASE,
      body,
      {
        headers: {
          Authorization: `Bearer ${envConfig.CF_IMAGES_API_TOKEN}`,
          'Content-Type': 'application/json',
        },
      }
    );

    if (!res.data?.success) {
      throw new Error('Failed to create Cloudflare Images direct upload');
    }

    const id = res.data?.result?.id || res.data?.result?.image?.id;
    const uploadURL = res.data?.result?.uploadURL;
    return { id, uploadURL };
  },

  getDeliveryUrl(imageId: string, variant?: string): string {
    const base = envConfig.CF_IMAGES_DELIVERY_URL.replace(/\/$/, '');
    return variant ? `${base}/${imageId}/${variant}` : `${base}/${imageId}`;
  },

  getGifUrl(imageId: string): string {
    // Expect a variant named 'gif' configured in Cloudflare Images variants
    return this.getDeliveryUrl(imageId, 'gif');
  },
};

export default imagesService;

