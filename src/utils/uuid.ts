import { v4 as uuidv4, v1 as uuidv1 } from 'uuid';

/**
 * Utility class for generating various types of UUIDs
 */
export class UuidService {
  private static instance: UuidService;

  private constructor() {}

  public static getInstance(): UuidService {
    if (!UuidService.instance) {
      UuidService.instance = new UuidService();
    }
    return UuidService.instance;
  }

  /**
   * Generates a random UUID v4
   * Best for: Request IDs, general purpose unique identifiers
   */
  public generateV4(): string {
    return uuidv4();
  }

  /**
   * Generates a time-based UUID v1
   * Best for: Sortable IDs, when you need timestamp ordering
   */
  public generateV1(): string {
    return uuidv1();
  }

  /**
   * Generates a short ID suitable for URLs or display
   * Returns first 8 characters of UUID v4
   */
  public generateShortId(): string {
    return uuidv4().substring(0, 8);
  }

  /**
   * Generates a request ID for tracing
   */
  public generateRequestId(): string {
    return this.generateV4();
  }

  /**
   * Validates if a string is a valid UUID
   */
  public isValidUuid(uuid: string): boolean {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
  }

  /**
   * Validates if a string is a valid UUID v4
   */
  public isValidUuidV4(uuid: string): boolean {
    const uuidV4Regex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidV4Regex.test(uuid);
  }

  /**
   * Formats UUID to remove dashes (for compact storage)
   */
  public removeHyphens(uuid: string): string {
    return uuid.replace(/-/g, '');
  }

  /**
   * Formats UUID to add dashes (from compact format)
   */
  public addHyphens(compactUuid: string): string {
    if (compactUuid.length !== 32) {
      throw new Error('Compact UUID must be exactly 32 characters');
    }
    
    return [
      compactUuid.slice(0, 8),
      compactUuid.slice(8, 12),
      compactUuid.slice(12, 16),
      compactUuid.slice(16, 20),
      compactUuid.slice(20, 32),
    ].join('-');
  }
}

// Export singleton instance
export const uuidService = UuidService.getInstance();

// Export convenience functions
export const generateRequestId = (): string => uuidService.generateRequestId();
export const generateId = (): string => uuidService.generateV4();
export const generateShortId = (): string => uuidService.generateShortId();
export const isValidUuid = (uuid: string): boolean => uuidService.isValidUuid(uuid); 