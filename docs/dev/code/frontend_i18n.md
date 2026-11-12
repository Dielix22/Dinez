# User-facing Text (i18n) in the Front End

Everything the user reads is in **Spanish** and lives in one file: `src/frontend/src/i18n/messages.ts`. Code, comments and identifiers stay in English ([03 Coding Conventions](../03_coding_conventions.md) §2).

There is no i18n library: `messages.ts` is a plain object. If we ever need a second language, we will add a library through an ADR.

---

## Use a message

```tsx
import { MESSAGES } from "../i18n/messages";

<Typography>{MESSAGES.helloWorld}</Typography>
```

Never write Spanish text directly in a component, a test or a back-end response.

## Add a message

1. Add a key in `messages.ts`, with a `camelCase` English name that says what the text is for, not what it says (`emptyQueue`, not `noHayPacientes`).
2. Use it with `MESSAGES.<key>`.

```ts
export const MESSAGES = {
  helloWorld: "¡Hola mundo!",
  emptyQueue: "No hay pacientes en espera.",
} as const;
```

## Text with a value

Use a function, so the value is typed and the sentence stays in one place:

```ts
ticketAssigned: (code: string) => `Turno asignado: ${code}`,
```

```tsx
MESSAGES.ticketAssigned(ticket.code);
```

## Back-end errors

The back end returns a stable error **code** (for example `DUPLICATE_PATIENT`), never Spanish text ([03 Coding Conventions](../03_coding_conventions.md) §5.4). The front end maps the code to a message:

```ts
export const ERROR_MESSAGES: Record<string, string> = {
  DUPLICATE_PATIENT: "El paciente ya tiene un turno.",
};
```

Always provide a generic fallback message for unknown codes.

## Rules

- Group related keys with a prefix once the file grows (`queue…`, `error…`), or split by screen into a few files in `i18n/`.
- Do not put business data (triage levels, wait limits) here: it comes from the back end.
- In tests, import the same `MESSAGES` instead of repeating the Spanish text, so a wording change does not break them (see `tests/HelloWorldPage.test.tsx`).
