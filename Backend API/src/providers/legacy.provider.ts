import { IdlixScraperEngine } from '../engine/IdlixScraperEngine.js';
import {
  CatalogItem,
  Detail,
  Episode,
  MediaType,
  PlayInfo,
  Provider,
  ProviderError,
} from './types.js';

const engine = IdlixScraperEngine.getInstance();

const POSTER = 'https://image.tmdb.org/t/p/w342';
const BACKDROP = 'https://image.tmdb.org/t/p/w1280';

function yearOf(raw: any): number | undefined {
  const date: string | undefined = raw?.releaseDate ?? raw?.firstAirDate ?? raw?.airDate;
  const year = date ? Number(date.slice(0, 4)) : NaN;
  return Number.isFinite(year) ? year : undefined;
}

function ratingOf(raw: any): number | undefined {
  const value = Number(raw?.voteAverage);
  return Number.isFinite(value) ? value : undefined;
}

function image(base: string, path: unknown): string | undefined {
  return typeof path === 'string' && path.length > 0 ? `${base}${path}` : undefined;
}

function toItem(raw: any, type: MediaType): CatalogItem {
  const slug: string = typeof raw?.slug === 'string' ? raw.slug : '';
  const numericId = raw?.id != null ? String(raw.id) : '';
  return {
    id: slug || numericId,
    playId: numericId || slug,
    type,
    title: typeof raw?.title === 'string' ? raw.title : '',
    year: yearOf(raw),
    poster: image(POSTER, raw?.posterPath),
    backdrop: image(BACKDROP, raw?.backdropPath),
    rating: ratingOf(raw),
    synopsis: typeof raw?.overview === 'string' ? raw.overview : undefined,
  };
}

function toEpisode(raw: any, seasonNumber: number): Episode {
  const numericId = raw?.id != null ? String(raw.id) : '';
  return {
    id: numericId,
    playId: numericId,
    season: Number(raw?.seasonNumber ?? seasonNumber) || seasonNumber,
    episode: Number(raw?.episodeNumber) || 1,
    title: typeof raw?.name === 'string' ? raw.name : undefined,
    still: image('https://image.tmdb.org/t/p/w500', raw?.stillPath),
  };
}

async function fetchData(path: string): Promise<any> {
  const result = await engine.executeFetch(path);
  if (result.status !== 200 || result.data == null) {
    throw new ProviderError(
      `Upstream returned ${result.status} for ${path.split('?')[0]}`,
      result.status === 403 ? 502 : result.status || 502
    );
  }
  return result.data;
}

export const legacyProvider: Provider = {
  manifest: {
    id: 'legacy',
    name: 'Pixel TV (legacy)',
    version: 1,
    apiVersion: 1,
    langs: ['id', 'en'],
    capabilities: { home: true, search: true, detail: true, resolve: true },
    domains: [new URL(engine.baseUrl).host],
    enabled: true,
  },

  async home(page: number): Promise<CatalogItem[]> {
    const limit = 20;
    const [movies, series] = await Promise.all([
      fetchData(`/api/movies?page=${page}&limit=${limit}`).catch(() => null),
      fetchData(`/api/series?page=${page}&limit=${limit}`).catch(() => null),
    ]);

    const items: CatalogItem[] = [
      ...(Array.isArray(movies?.data) ? movies.data.map((m: any) => toItem(m, 'movie')) : []),
      ...(Array.isArray(series?.data) ? series.data.map((s: any) => toItem(s, 'series')) : []),
    ];

    if (items.length === 0) {
      throw new ProviderError('Upstream returned no catalog items');
    }
    return items;
  },

  async search(query: string, _page: number): Promise<CatalogItem[]> {
    const data = await fetchData(`/api/search?q=${encodeURIComponent(query)}`);
    const results: any[] = Array.isArray(data?.results) ? data.results : [];
    return results.map((raw) =>
      toItem(raw, raw?.contentType === 'tv_series' ? 'series' : 'movie')
    );
  },

  async detail(id: string, type: MediaType): Promise<Detail> {
    const path = type === 'series' ? `/api/series/${encodeURIComponent(id)}` : `/api/movies/${encodeURIComponent(id)}`;
    const raw = await fetchData(path);

    const base = toItem(raw, type);
    const detail: Detail = {
      ...base,
      genres: Array.isArray(raw?.genres)
        ? raw.genres.map((g: any) => g?.name).filter((n: unknown): n is string => typeof n === 'string')
        : undefined,
      runtimeMinutes: Number(raw?.runtime ?? raw?.duration) || undefined,
    };

    if (type === 'series') {
      const seasonNumber = Number(raw?.defaultSeason?.seasonNumber) || 1;
      const rawEpisodes: any[] = Array.isArray(raw?.defaultSeason?.episodes)
        ? raw.defaultSeason.episodes
        : [];
      detail.episodes = rawEpisodes.map((e) => toEpisode(e, seasonNumber));
    }

    return detail;
  },

  async resolve(playId: string, type: MediaType | 'episode'): Promise<PlayInfo> {
    const contentType = type === 'series' ? 'tv_series' : type;
    const playback = await engine.resolvePlayback(contentType, playId);

    const subtitles = (playback.subtitles ?? []).map((sub) => ({
      lang: sub.lang,
      label: sub.label,
      url: sub.path,
    }));

    if (!playback.hlsMasterUrl) {
      throw new ProviderError('Upstream returned no HLS URL');
    }

    return {
      title: playback.title ?? '',
      hlsUrl: playback.hlsMasterUrl,
      subtitles,
      durationSec: playback.durationSec || undefined,
    };
  },

  async health(): Promise<boolean> {
    return engine.getSessionStatus().isReady;
  },
};
