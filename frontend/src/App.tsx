import { useEffect, useState } from 'react';

import { getVersion } from './api/client.ts';
import type { VersionResponse } from './types/api.ts';

type BackendState =
  | { readonly kind: 'checking' }
  | { readonly kind: 'connected'; readonly info: VersionResponse }
  | { readonly kind: 'unreachable' };

/**
 * Milestone 1 shell: shows which backend this page is talking to.
 *
 * A failed request is reported as "cannot reach the backend", an observation problem. It says
 * nothing about the backend's own state (handbook chapter 16), so the user can simply retry.
 */
export function App() {
  const [state, setState] = useState<BackendState>({ kind: 'checking' });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    getVersion(controller.signal)
      .then((info) => {
        setState({ kind: 'connected', info });
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          console.warn('Version request failed', error);
          setState({ kind: 'unreachable' });
        }
      });
    return () => {
      controller.abort();
    };
  }, [attempt]);

  const retry = () => {
    setState({ kind: 'checking' });
    setAttempt((n) => n + 1);
  };

  return (
    <main>
      <h1>DataOrbit</h1>
      <section aria-labelledby="backend-heading">
        <h2 id="backend-heading">Backend</h2>
        <p role="status" aria-live="polite">
          {state.kind === 'checking' && 'Checking the backend…'}
          {state.kind === 'connected' && (
            <>
              Connected to {state.info.name} version <strong>{state.info.version}</strong>.
            </>
          )}
          {state.kind === 'unreachable' &&
            'Cannot reach the DataOrbit backend at the moment. Check that it is running, then try again.'}
        </p>
        {state.kind === 'unreachable' && (
          <button type="button" onClick={retry}>
            Try again
          </button>
        )}
      </section>
    </main>
  );
}
