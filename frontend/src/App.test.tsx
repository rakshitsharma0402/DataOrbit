import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { App } from './App.tsx';

function versionResponse(): Response {
  return new Response(JSON.stringify({ name: 'DataOrbit', version: '0.1.0-SNAPSHOT' }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('App', () => {
  it('shows the backend version once the backend answers', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(versionResponse());

    render(<App />);

    expect(screen.getByRole('status')).toHaveTextContent('Checking the backend');
    expect(await screen.findByText('0.1.0-SNAPSHOT')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Connected to DataOrbit version 0.1.0-SNAPSHOT.');
    expect(screen.queryByRole('button', { name: 'Try again' })).not.toBeInTheDocument();
  });

  it('reports an unreachable backend as an observation problem and lets the user retry', async () => {
    vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockRejectedValueOnce(new TypeError('Failed to fetch'))
      .mockResolvedValueOnce(versionResponse());

    render(<App />);

    expect(await screen.findByText(/Cannot reach the DataOrbit backend/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Try again' }));
    expect(screen.getByRole('status')).toHaveTextContent('Checking the backend');

    expect(await screen.findByText('0.1.0-SNAPSHOT')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
