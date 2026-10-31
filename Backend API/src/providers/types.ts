/**
 * Contract between Backend API and a content provider.
 *
 * Providers run server-side only. The Android app never loads provider code.
 */

export const SUPPORTED_API_VERSION = 1;

export type MediaType = 'movie' | 'series';

export interface ProviderManifest {
  /** Unique, lowercase, no spaces. Used in URLs. */
  id: string;
  /** Shown in the app source picker. */
  name: string;
  /** Bump when provider behavior changes. */
  version: number;
  /** Must equal SUPPORTED_API_VERSION. */
  apiVersion: number;
  /** BCP 47 tags, e.g. ["id", "en"]. */
  langs: string[];
  capabilities: {
    home: boolean;
    search: boolean;
    detail: boolean;
    resolve: boolean;
  };
  /** Outbound hosts this provider may contact. */
  domains: string[];
  enabled: boolean;
}

export interface CatalogItem {
  /** Provider-opaque key used by `detail`. */
  id: string;
  /** Provider-opaque key used by `resolve`. */
  playId: string;
  type: MediaType;
  title: string;
  year?: number;
  poster?: string;
  backdrop?: string;
  rating?: number;
  synopsis?: string;
}

export interface Episode {
  id: string;
  playId: string;
  season: number;
  episode: number;
  title?: string;
  still?: string;
}

export interface Detail extends CatalogItem {
  genres?: string[];
  runtimeMinutes?: number;
  episodes?: Episode[];
}

export interface SubtitleRef {
  lang: string;
  label: string;
  url: string;
}

export interface PlayInfo {
  title: string;
  hlsUrl: string;
  subtitles: SubtitleRef[];
  durationSec?: number;
  /** ISO timestamp when the stream token stops working, if known. */
  expiresAt?: string;
}

export interface Provider {
  manifest: ProviderManifest;
  home?(page: number): Promise<CatalogItem[]>;
  search?(query: string, page: number): Promise<CatalogItem[]>;
  detail?(id: string, type: MediaType): Promise<Detail>;
  resolve?(playId: string, type: MediaType | 'episode'): Promise<PlayInfo>;
  health?(): Promise<boolean>;
}

export class ProviderError extends Error {
  constructor(
    message: string,
    readonly status = 502
  ) {
    super(message);
    this.name = 'ProviderError';
  }
}
