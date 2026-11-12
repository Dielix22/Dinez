# Front-end Setup and Execution

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Scope** | Tools required, how to run, test and format the front end, project layout |
| **Related** | [01 Project Structure](../01_project_structure.md) · [03 Coding Conventions](../03_coding_conventions.md) · [ADR-0003](../decisiones/ADR-0003-frontend-tooling.md) |

The front end lives in `src/frontend/`. It is an independent project: every command below is run **from that folder**.

---

## 1. Required software

| Software | Version | Notes |
|---|---|---|
| [Node.js](https://nodejs.org/) | **22.12 or newer** (LTS recommended; the project was set up with Node 24) | Includes `npm`. Older versions fail because Vite and Vitest require it. |
| npm | The one bundled with Node | Do not use yarn or pnpm: only `package-lock.json` is committed. |
| Git | Any recent version | |
| A modern browser | Chrome, Firefox or Edge | |

Recommended, not required: VS Code with the *Prettier* and *EditorConfig* extensions, so that formatting follows the project rules while you type.

Check your installation:

```bash
node -v   # v22.12.0 or higher
npm -v
```

No database, back end or environment variable is needed to run the front end for now.

---

## 2. Run the front end

```bash
cd src/frontend
npm ci          # first time, and after every change of package-lock.json
npm run dev
```

Open <http://localhost:5173>. The server reloads the browser automatically when a file is saved. Stop it with `Ctrl+C`.

Use `npm ci` (not `npm install`) to install exactly the versions pinned in `package-lock.json`. Use `npm install --save-exact <package>` only when adding a dependency on purpose (see §5).

---

## 3. Commands

| Goal | Command |
|---|---|
| Install dependencies | `npm ci` |
| Run in development mode | `npm run dev` |
| Type-check and build for production (output in `dist/`) | `npm run build` |
| Run all tests once | `npm test` |
| Check formatting (same check as CI) | `npm run format:check` |
| Fix formatting | `npm run format` |

Before opening a PR, run `npm run format:check`, `npm test` and `npm run build`; all three must pass.

---

## 4. Project layout

```text
src/frontend/
├── index.html              # entry HTML, loads src/main.tsx
├── package.json            # scripts and pinned dependencies
├── package-lock.json       # committed, do not edit by hand
├── vite.config.ts          # Vite and Vitest configuration
├── tsconfig.json           # TypeScript configuration (strict mode)
├── .prettierrc.json        # formatting rules
├── src/
│   ├── main.tsx            # mounts <App /> in the page
│   ├── App.tsx             # router: maps URLs to pages
│   ├── pages/              # one component per screen (HelloWorldPage.tsx)
│   └── i18n/messages.ts    # all user-facing text (Spanish)
└── tests/                  # tests, mirroring src/ (HelloWorldPage.test.tsx)
```

Technologies: React, TypeScript, Vite, React Router and Material UI (MUI). The reasons are in [ADR-0003](../decisiones/ADR-0003-frontend-tooling.md).

---

## 5. Working rules

These rules come from [03 Coding Conventions](../03_coding_conventions.md):

- **Language:** code, comments and identifiers are in English. Text shown to users is Spanish and lives only in `src/i18n/messages.ts`; never write it directly in a component.
- **Naming:** components and their files in `PascalCase`, one component per file (`PriorityQueue.tsx`); hooks start with `use`; variables and props in `camelCase`; constants in `UPPER_SNAKE_CASE`. Domain terms are the same as in the back end (a `Ticket` is a `Ticket`).
- **No business rules in the UI.** The back end is the source of truth; components display data and send user actions.
- **Adding a page:** create `src/pages/MyPage.tsx`, add a `<Route>` in `src/App.tsx`, and add a test in `tests/`.
- **Adding a test:** files go in `tests/` and are named `<Subject>.test.tsx`. Follow Arrange–Act–Assert and test visible behaviour, not implementation.
- **Adding a dependency:** only if it clearly removes work. Install it with `npm install --save-exact <package>` (versions are pinned, no `^`), commit the updated `package-lock.json` and justify it in the PR description. A new library for state or data fetching needs an ADR first.
- **Never commit** `node_modules/`, `dist/`, `.env` files or IDE settings (already in `.gitignore`).

---

## 6. Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| `Unsupported engine` or a syntax error on startup | Node is too old. Install Node 22.12+ and run `npm ci` again. |
| `npm run format:check` reports many files on Windows | Files were checked out with CRLF line endings. The project uses LF: run `npm run format` or set `git config core.autocrlf input`. |
| Port 5173 already in use | Another Vite instance is running. Stop it, or Vite will pick the next free port and print it. |
| Strange errors after switching branches | Delete `node_modules/` and run `npm ci`. |
