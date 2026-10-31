import { LRUCache } from 'lru-cache';

export class CacheService {
  private cache: LRUCache<string, any>;

  constructor(maxItems = 1000, defaultTtlMs = 15 * 60 * 1000) {
    this.cache = new LRUCache({
      max: maxItems,
      ttl: defaultTtlMs,
    });
  }

  get<T>(key: string): T | undefined {
    return this.cache.get(key) as T | undefined;
  }

  set<T>(key: string, value: T, ttlMs?: number): void {
    if (ttlMs !== undefined) {
      this.cache.set(key, value, { ttl: ttlMs });
    } else {
      this.cache.set(key, value);
    }
  }

  has(key: string): boolean {
    return this.cache.has(key);
  }

  delete(key: string): boolean {
    return this.cache.delete(key);
  }

  clear(): void {
    this.cache.clear();
  }
}

export const globalCache = new CacheService();
