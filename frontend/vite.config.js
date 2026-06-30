import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// During `npm run dev`, the app is served at http://localhost:5173 and any
// request to /api is proxied to the Spring Boot backend on :8080.
// That avoids CORS entirely — the browser thinks everything is same-origin.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
