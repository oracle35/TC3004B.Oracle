import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import tailwindcss from "@tailwindcss/vite";

// No need to manually call loadEnv here for this purpose

export default defineConfig(() => {
  const rootDir = path.resolve(__dirname, "../..");

  return {
    envDir: rootDir,

    plugins: [react(), tailwindcss()],
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
  };
});
