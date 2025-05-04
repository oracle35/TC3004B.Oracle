# Frontend

The **Frontend** repository is nothing more than a wrapper for the backend. In this case, it is written using *React*, *Typescript* and uses *TailwindCSS* and *MaterialUI* (although the latter one is preferred to include proper components).

## Technologies used

We use the following technologies for the frontend side of the project:

- React
- Vite
- Typescript
- Material UI (MUI) for UI components (preferred)
- Tailwind CSS for utility styling (used less frequently)
- Recharts for charts (in KPI page)
- ESLint for code linting

## How it Works

The frontend is a single-page application (SPA) built with React and TypeScript, using Vite as the build tool and development server.

### Structure

- **`src/`**: Contains all the source code.
  - **`main.tsx`**: The entry point of the application.
  - **`App.tsx`**: The root component, likely handling routing.
  - **`pages/`**: Contains components representing distinct pages or views (e.g., `MainPage`, `LoginPage`, `KPIPage`).
  - **`components/`**: Holds reusable UI components used across different pages (e.g., `TaskTable`, `AddModal`, `NavBar`).
  - **`api/`**: Includes functions responsible for making HTTP requests to the Spring Boot backend API endpoints (e.g., `getTasks`, `updateTask`, `getAiSummary`). These typically use the browser's `fetch` API.
  - **`models/`**: Defines TypeScript interfaces (`Task`, `User`, `Sprint`) that represent the structure of data received from the backend.
  - **`utils/`**: Contains utility or helper functions (e.g., `sprint.ts` for date calculations).
  - **`assets/`**: Static assets like images.
- **`public/`**: Static files that are served directly by the webserver.
- **`index.html`**: The main HTML file where the React app is mounted.
- **`vite.config.ts`**: Configuration for the Vite build tool, including proxy setup for backend API calls during development.
- **`eslint.config.mjs`**: Configuration for ESLint to enforce code style and quality.
- **`tsconfig.json`**: TypeScript compiler configuration.

### Workflow

1. **Development**: Running `npm run dev` starts the Vite development server, providing features like Hot Module Replacement (HMR) for a fast development experience. Vite proxies requests starting with `/api` to the backend server (usually running on port 8080) as configured in `vite.config.ts`.
2. **API Interaction**: Components in `pages/` or `components/` call functions from the `api/` directory to fetch data from or send data to the backend. These functions handle the HTTP requests and responses.
3. **State Management**: Component state is primarily managed using React hooks like `useState`, `useEffect`, and `useMemo`. Data fetched from the API is stored in the state, causing components to re-render when the data changes.
4. **Routing**: (Assuming React Router is used, though not explicitly shown in provided files) React Router likely handles navigation between different pages defined in the `pages/` directory.
5. **Styling**: Components are styled using Material UI components and their `sx` prop, with occasional use of Tailwind CSS utility classes.
6. **Build**: Running `npm run build` uses Vite to compile the TypeScript code, bundle the JavaScript and CSS, and optimize assets for production. The output is placed in the `dist/` directory. This `dist/` directory contains the static files needed to serve the frontend application.
7. **Integration with Backend**: During the backend build process (e.g., using `bin/build.sh` or the Nix build), the contents of the frontend's `dist/` directory are copied into the backend's static resources directory (`MtdrSpring/backend/target/frontend/` or similar). This allows the Spring Boot application to serve the frontend directly from the same origin, avoiding CORS issues in production.
