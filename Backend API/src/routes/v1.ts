import { FastifyInstance, FastifyPluginAsync } from 'fastify';
import { globalCache } from '../services/CacheService.js';
import {
  enabledProviders,
  listSources,
  requireProvider,
  withTimeout,
} from '../providers/registry.js';
import { CatalogItem, MediaType, Provider, ProviderError, SUPPORTED_API_VERSION } from '../providers/types.js';

interface CatalogQuery {
  source?: string;
  type?: string;
  page?: string;
  q?: string;
}

interface SingleQuery {
  source?: string;
  type?: string;
  id?: string;
}

function parseType(value: string | undefined): MediaType | null {
  return value === 'movie' || value === 'series' ? value : null;
}

function parsePlayType(value: string | undefined): MediaType | 'episode' | null {
  return value === 'movie' || value === 'series' || value === 'episode' ? value : null;
}

function isProviderError(err: unknown): err is ProviderError {
  return err instanceof ProviderError;
}

export const v1Routes: FastifyPluginAsync = async (fastify: FastifyInstance) => {
  fastify.get('/api/v1/sources', async () => ({
    apiVersion: SUPPORTED_API_VERSION,
    sources: await listSources(),
  }));

  fastify.get<{ Querystring: CatalogQuery }>('/api/v1/catalog', async (req, reply) => {
    const { source, type, page: rawPage, q } = req.query;
    const page = Math.max(1, Number(rawPage) || 1);
    const query = q?.trim();

    const mediaType = parseType(type);
    if (!query && !mediaType) {
      return reply.status(400).send({ error: 'type must be "movie" or "series"' });
    }

    let providers: Provider[];
    if (source) {
      try {
        providers = [requireProvider(source)];
      } catch (err) {
        const status = isProviderError(err) ? err.status : 500;
        return reply.status(status).send({ error: (err as Error).message });
      }
    } else {
      providers = enabledProviders();
    }

    const cacheKey = `v1:catalog:${source ?? 'all'}:${mediaType ?? 'any'}:${page}:${query ?? ''}`;
    const cached = globalCache.get<{ items: CatalogItem[]; warnings: unknown[] }>(cacheKey);
    if (cached) return cached;

    const items: (CatalogItem & { source: string })[] = [];
    const warnings: { source: string; message: string }[] = [];

    await Promise.all(
      providers.map(async (provider) => {
        const { manifest } = provider;
        try {
          if (query) {
            if (!manifest.capabilities.search || !provider.search) {
              throw new ProviderError(`Source "${manifest.id}" has no search`, 501);
            }
            const found = await withTimeout(() => provider.search!(query, page));
            items.push(...found.map((item) => ({ ...item, source: manifest.id })));
          } else {
            if (!manifest.capabilities.home || !provider.home) {
              throw new ProviderError(`Source "${manifest.id}" has no catalog`, 501);
            }
            const found = await withTimeout(() => provider.home!(page));
            items.push(...found.map((item) => ({ ...item, source: manifest.id })));
          }
        } catch (err) {
          warnings.push({
            source: manifest.id,
            message: err instanceof Error ? err.message : 'Unknown provider error',
          });
        }
      })
    );

    if (items.length === 0) {
      if (warnings.length > 0) {
        return reply.status(502).send({ error: warnings[0].message, warnings });
      }
      return reply.status(502).send({ error: 'No catalog items available', warnings: [] });
    }

    const payload = { items, warnings };
    globalCache.set(cacheKey, payload, 15 * 60 * 1000);
    return payload;
  });

  fastify.get<{ Querystring: SingleQuery }>('/api/v1/detail', async (req, reply) => {
    const { source, id } = req.query;
    const type = parseType(req.query.type);
    if (!type || !id) {
      return reply.status(400).send({ error: 'type and id are required' });
    }

    try {
      const provider = requireProvider(source);
      if (!provider.manifest.capabilities.detail || !provider.detail) {
        return reply.status(501).send({ error: `Source "${provider.manifest.id}" has no detail` });
      }

      const cacheKey = `v1:detail:${provider.manifest.id}:${type}:${id}`;
      const cached = globalCache.get<unknown>(cacheKey);
      if (cached) return cached;

      const detail = await withTimeout(() => provider.detail!(id, type));
      const payload = { ...detail, source: provider.manifest.id };
      globalCache.set(cacheKey, payload, 30 * 60 * 1000);
      return payload;
    } catch (err) {
      const status = isProviderError(err) ? err.status : 502;
      return reply.status(status).send({ error: (err as Error).message });
    }
  });

  fastify.get<{ Querystring: SingleQuery }>('/api/v1/play', async (req, reply) => {
    const { source, id } = req.query;
    const type = parsePlayType(req.query.type);
    if (!type || !id) {
      return reply.status(400).send({ error: 'type (movie, series or episode) and id are required' });
    }

    try {
      const provider = requireProvider(source);
      if (!provider.manifest.capabilities.resolve || !provider.resolve) {
        return reply.status(501).send({ error: `Source "${provider.manifest.id}" has no resolve` });
      }
      return await withTimeout(async () => ({
        ...(await provider.resolve!(id, type)),
        source: provider.manifest.id,
      }));
    } catch (err) {
      const status = isProviderError(err) ? err.status : 502;
      return reply.status(status).send({ error: (err as Error).message });
    }
  });
};
