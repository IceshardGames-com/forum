import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { envConfig } from '../../config/env';

export interface PresignPutParams {
  keyPrefix?: string;
  contentType: string;
  expiresInSeconds?: number;
}

export interface PresignPutResult {
  key: string;
  url: string;
  method: 'PUT';
  headers: Record<string, string>;
  publicUrl?: string;
}

const s3Client = new S3Client({
  region: 'auto',
  endpoint: `https://${envConfig.R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
  credentials: {
    accessKeyId: envConfig.R2_ACCESS_KEY_ID,
    secretAccessKey: envConfig.R2_SECRET_ACCESS_KEY,
  },
});

const randomKeySuffix = (): string => Math.random().toString(36).slice(2);

export const r2Service = {
  async presignPut({ keyPrefix = 'uploads', contentType, expiresInSeconds = 300 }: PresignPutParams): Promise<PresignPutResult> {
    const safePrefix = keyPrefix.replace(/[^a-zA-Z0-9/_-]/g, '').replace(/\/+/, '/');
    const key = `${safePrefix}/${Date.now()}-${randomKeySuffix()}`;

    const command = new PutObjectCommand({
      Bucket: envConfig.R2_BUCKET_NAME,
      Key: key,
      ContentType: contentType,
      ACL: undefined,
    });

    const url = await getSignedUrl(s3Client, command, { expiresIn: expiresInSeconds });

    const result: PresignPutResult = {
      key,
      url,
      method: 'PUT',
      headers: { 'Content-Type': contentType },
    };
    if (envConfig.R2_PUBLIC_BASE_URL) {
      result.publicUrl = `${envConfig.R2_PUBLIC_BASE_URL.replace(/\/$/, '')}/${key}`;
    }
    return result;
  },
};

export default r2Service;

