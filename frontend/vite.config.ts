/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// Dev and preview servers listen only on the loopback address (handbook chapter 17), on a fixed
// port: the backend allows exactly one browser origin (dataorbit.frontend-origin), so the page must
// always be served from http://127.0.0.1:5173. strictPort fails instead of silently moving ports.
const loopback = { host: '127.0.0.1', port: 5173, strictPort: true } as const;

export default defineConfig({
  plugins: [react()],
  server: loopback,
  preview: loopback,
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    restoreMocks: true,
  },
});
