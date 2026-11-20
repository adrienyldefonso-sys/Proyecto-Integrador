import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const django = { target: 'http://127.0.0.1:8000', changeOrigin: false }

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': django,
      '/admin': django,
      '/media': django,
      '/static': django,
    },
  },
})