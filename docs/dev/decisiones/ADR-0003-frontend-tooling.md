# ADR-0003: Front-end tooling (TypeScript, Vite, React Router, Material UI)

Authors:
- @hoverwars

Date: 2026-11-12

## Status

Proposed

## Context

[ADR-0001](ADR-0001-stack-tecnologico.md) chose React for the front end but left the surrounding tooling open. Its consequences and [RFC-0001](RFC-0001-stack-tecnologico.md) ask the team to define conventions for components, navigation, state and data consumption before advancing too far, and [03 Coding Conventions](../03_coding_conventions.md) states that the build tool, formatter, linter and test framework are chosen when the project is initialised.

The front end is initialised in `src/frontend/` (see [01 Project Structure](../01_project_structure.md)). The team has uneven experience with React, a single semester to deliver, and needs a reproducible setup (one command to install, run, test and check formatting).

## Decision

The front end in `src/frontend/` uses:

- **TypeScript** as the language (components in `.tsx`).
- **Vite** as build tool and development server.
- **React Router** for navigation.
- **Material UI (MUI)** as the only component library and styling system.
- **Vitest** with React Testing Library for tests, and **Prettier** for formatting.

All dependency versions are pinned and `package-lock.json` is committed.

## Alternatives considered

- **JavaScript instead of TypeScript:** simpler to start, but types catch errors early and document the contract with the back end (for example `Ticket`), which matters in a team with uneven experience.
- **Create React App / Next.js instead of Vite:** Create React App is deprecated; Next.js adds server-side concepts that are not needed here. Vite is light, fast and well documented.
- **Tailwind CSS instead of MUI:** more visual freedom, but more design work and a higher risk of inconsistent screens. MUI provides ready-made, uniform and accessible components. Tailwind combined with MUI was rejected because both compete to style the same elements.
- **Jest instead of Vitest:** Vitest shares the Vite configuration and needs no extra transformation setup.

## Consequences

### Positive

- A running application with one command (`npm run dev`) and the same commands locally and in CI.
- Consistent UI with little design effort; types shared in spirit with the Java domain model.

### Negative and trade-offs

- Four more libraries to keep up to date (React Router, MUI with Emotion, Vitest, Prettier), in addition to the three ecosystems already accepted in ADR-0001.
- MUI's visual identity is recognisable and harder to customise deeply; a very custom screen (for example the public ticket display) may need extra theming work.
- TypeScript adds a learning curve for members who only know JavaScript.
- State management and data-fetching libraries are **not** decided here; they will be decided in a new ADR when the first screen needs them.
- A separate React application served alongside the back end may conflict with the monolith constraint in `project.md` §5; the team considers this settled.
