import type { VersionResponse } from '../types/api.ts';

/** Backend origin. The backend accepts this page only from its configured frontend origin (CORS). */
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8080';

/** The backend answered, but not with what the contract promises. */
export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * GET /api/v1/version.
 *
 * Throws ApiError for a non-2xx status or an unexpected body, and lets fetch's own TypeError
 * through when the backend cannot be reached at all (network error or CORS rejection).
 */
export async function getVersion(signal?: AbortSignal): Promise<VersionResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/version`, {
    headers: { Accept: 'application/json' },
    ...(signal ? { signal } : {}),
  });
  if (!response.ok) {
    throw new ApiError(response.status, `Backend returned HTTP ${String(response.status)}`);
  }
  const body: unknown = await response.json();
  if (!isVersionResponse(body)) {
    throw new ApiError(response.status, 'Backend returned an unexpected version response');
  }
  return body;
}

function isVersionResponse(value: unknown): value is VersionResponse {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return typeof candidate.name === 'string' && typeof candidate.version === 'string';
}
