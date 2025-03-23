import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import tailwindcss from "@tailwindcss/vite";
import eslint from 'vite-plugin-eslint';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), eslint()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      "/todolist": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
