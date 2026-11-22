import { describe, expect, it, vi } from 'vitest';

import { API_BASE_URL, ApiError, getVersion } from './client.ts';

function respond(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('getVersion', () => {
  it('calls the version endpoint on the loopback backend by default', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(respond(200, { name: 'DataOrbit', version: '0.1.0-SNAPSHOT' }));

    await expect(getVersion()).resolves.toEqual({ name: 'DataOrbit', version: '0.1.0-SNAPSHOT' });

    expect(API_BASE_URL).toBe('http://127.0.0.1:8080');
    expect(fetchMock).toHaveBeenCalledWith(
      'http://127.0.0.1:8080/api/v1/version',
      expect.objectContaining({ headers: { Accept: 'application/json' } }),
    );
  });

  it('rejects a non-2xx response with its status', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(respond(503, { code: 'UNAVAILABLE' }));

    await expect(getVersion()).rejects.toEqual(new ApiError(503, 'Backend returned HTTP 503'));
  });

  it('rejects a body that does not match the contract', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(respond(200, { name: 'DataOrbit', version: 1 }));

    await expect(getVersion()).rejects.toBeInstanceOf(ApiError);
  });

  it('lets a network failure through unchanged', async () => {
    const failure = new TypeError('Failed to fetch');
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(failure);

    await expect(getVersion()).rejects.toBe(failure);
  });
});
