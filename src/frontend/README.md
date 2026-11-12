# Frontend

React + TypeScript + Vite, React Router and Material UI (MUI).

Requires Node.js 22.12+ (developed with Node 24). Full guide: `docs/dev/code/frontend_setup.md`.

| Goal                        | Command (from `src/frontend/`) |
| --------------------------- | ------------------------------ |
| Install dependencies        | `npm ci`                       |
| Run (http://localhost:5173) | `npm run dev`                  |
| Build                       | `npm run build`                |
| Run tests                   | `npm test`                     |
| Check formatting            | `npm run format:check`         |
| Fix formatting              | `npm run format`               |

Layout: application code in `src/`, tests in `tests/`. User-facing text is Spanish and lives in `src/i18n/messages.ts`.
