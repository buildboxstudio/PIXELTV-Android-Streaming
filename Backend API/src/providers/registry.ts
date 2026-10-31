import {
  Provider,
  ProviderError,
  ProviderManifest,
  SUPPORTED_API_VERSION,
} from './types.js';

const providers = new Map<string, Provider>();

export function registerProvider(provider: Provider): void {
  const { manifest } = provider;
  if (manifest.apiVersion !== SUPPORTED_API_VERSION) {
    throw new Error(
      `Provider "${manifest.id}" uses apiVersion ${manifest.apiVersion}, expected ${SUPPORTED_API_VERSION}`
    );
  }
  if (providers.has(manifest.id)) {
    throw new Error(`Provider "${manifest.id}" is already registered`);
  }
  providers.set(manifest.id, provider);
}

export function getProvider(id: string): Provider | undefined {
  return providers.get(id);
}

export function enabledProviders(): Provider[] {
  return [...providers.values()].filter((p) => p.manifest.enabled);
}

export async function listSources(): Promise<
  (ProviderManifest & { healthy: boolean | null })[]
> {
  return Promise.all(
    [...providers.values()].map(async (provider) => {
      let healthy: boolean | null = null;
      if (provider.health) {
        try {
          healthy = await withTimeout(() => provider.health!(), 5000);
        } catch {
          healthy = false;
        }
      }
      return { ...provider.manifest, healthy };
    })
  );
}

export async function withTimeout<T>(
  fn: () => Promise<T>,
  timeoutMs = 20_000
): Promise<T> {
  let timer: NodeJS.Timeout | undefined;
  try {
    return await Promise.race([
      fn(),
      new Promise<never>((_, reject) => {
        timer = setTimeout(
          () => reject(new ProviderError(`Provider timed out after ${timeoutMs}ms`, 504)),
          timeoutMs
        );
      }),
    ]);
  } finally {
    if (timer) clearTimeout(timer);
  }
}

export function requireProvider(id: string | undefined): Provider {
  if (!id) throw new ProviderError('source is required', 400);
  const provider = providers.get(id);
  if (!provider) throw new ProviderError(`Unknown source "${id}"`, 404);
  if (!provider.manifest.enabled) throw new ProviderError(`Source "${id}" is disabled`, 503);
  return provider;
}
