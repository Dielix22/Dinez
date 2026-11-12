# Coding Conventions

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Status** | Draft — proposal for team review |
| **Scope** | Language, naming, code style, error handling, testing, documentation in code |
| **Related** | [01 Project Structure](01_project_structure.md) · [02 Collaboration Workflow](02_collaboration_workflow.md) |

> These conventions follow [ADR-0001](decisiones/ADR-0001-stack-tecnologico.md): **Java** for the back end and **React** for the front end. Tools (formatter, linter, test framework, build tool) are not fixed here; they will be chosen when the projects are initialised.

---

## 1. Principles

1. **Readability over cleverness.** Any member may be asked to explain code written by another (`project.md` §22).
2. **Automate what can be automated.** Once a formatter is chosen, style is not discussed in review.
3. **Names are the first documentation.** The domain vocabulary of the design documents is used as-is in the code.
4. **Business rules are explicit, small and tested.**
5. **Be consistent**, even where a rule is arbitrary; consistency is cheaper than debate.

---

## 2. Language and Vocabulary

**Everything is written in English**: code (classes, methods, fields, variables, database objects), comments, Javadoc, commit and PR text, `docs/dev`, and the error messages in logs. User-facing text (screens, messages) is the only exception: it is shown to Colombian hospital staff, so it is in Spanish, centralised in the front end and never hard-coded in back-end logic.

Business documents in `docs/business` keep their original language and may still contain Spanish terms; this is accepted.

**Glossary.** Spanish terms from the business and design documents map to one fixed English name in the code. Keep the table up to date when a new domain concept appears.

| Spanish (business / design) | English (code) |
|---|---|
| Turno | `Ticket` |
| Paciente | `Patient` |
| Cola por nivel | `LevelQueue` |
| Nivel de triage | `TriageLevel` |
| Tiempo de espera | `WaitTime` |
| Límite de espera | `WaitLimit` |

Use one word per concept: *patient*, not sometimes *user* or *client*.

---

## 3. Formatting

- Shared basics: UTF-8, LF line endings, no tabs, final newline, braces always present, imports sorted and never wildcard.
- A formatter per language will be chosen at initialisation; the same check runs locally and in CI. Run it before committing.

---

## 4. Naming

### 4.1 Java

| Element | Convention | Example |
|---|---|---|
| Package | lower case, no underscores | `queue.domain` |
| Class / record / enum / interface | `PascalCase`, nouns | `LevelQueue`, `TriageLevel` |
| Method | `camelCase`, verbs; boolean queries read as questions | `assignTicket`, `hasExceededLimit`, `patientExists` |
| Field / variable / parameter | `camelCase`, meaningful | `waitLimit`, `arrivalTime` |
| Constant (`static final`) and enum constant | `UPPER_SNAKE_CASE` | `NIVEL_1` |
| Test class | `<ClassUnderTest>Test` | `PriorityQueueManagerTest` |
| Test method | Behaviour in plain words | `servesExpiredTicketBeforeHigherLevel` |

- No obscure abbreviations (`id`, `url` are fine; `gcp` is not a name).
- No type information in names (`ticketList`): `tickets` is enough.
- Domain classes use domain verbs (`assignTicket`, `removePatient`, `changeLimit`), not CRUD verbs (`save`, `delete`, `update`), which belong to repositories.
- Booleans are positive (`isEmpty`, not `isNotEmpty`).

### 4.2 Database

SQL tables and columns in `snake_case`, singular table names, English names following the glossary (`ticket`, `level_queue`, `arrival_time`).

### 4.3 React

| Element | Convention | Example |
|---|---|---|
| Component and its file | `PascalCase`, one component per file | `PriorityQueue.jsx` |
| Hook | `use` prefix, `camelCase` | `usePriorityQueue` |
| Variables, props, other modules | `camelCase` | `onTicketSelected` |
| Constants | `UPPER_SNAKE_CASE` | `REFRESH_INTERVAL_MS` |

Domain terms keep the same English names as the back end (a `Ticket` is a `Ticket` on both sides). No business rule is re-implemented in the UI; user-facing text is Spanish and centralised (see §2).

---

## 5. Code Style and Design Rules

### 5.1 Size

| Element | Guideline |
|---|---|
| Method | ≤ 50 lines |
| Class | ≤ 500 lines |
| File | One top-level type per file |

These are triggers for a conversation in review, not mechanical limits. `PriorityQueueManager` is flagged in the design as the class most likely to grow.

### 5.2 Domain layer rules

1. **No framework imports** in the domain (web, persistence, serialisation): business rules must be testable on their own.
2. **No hidden dependencies.** No `LocalDateTime.now()`, `Math.random()` or similar direct calls in domain code; receive the clock (or other source) through the constructor. REQ-11 depends on elapsed time and its tests must be deterministic.
3. **Valid by construction.** Constructors validate invariants and throw a domain exception; an invalid `Ticket` cannot exist.
4. **Encapsulation.** Fields are `private`; expose behaviour (`ticket.hasExceededLimit(...)`), not state.
5. **Business rules are named methods**, not inline conditions: `if (ticket.hasExceededLimit(limit, now))`, not `if (now - t.h > l)`.
6. **Never return `null` from public methods;** use `Optional` for "may be absent" (`nextPatient()` → `Optional<Ticket>`).
7. Keep the design's **dependency directions** (`ticket → queue → wait`).

### 5.3 General rules

- No magic numbers or strings: name them (constant, enum or configuration).
- No commented-out code (Git remembers), no dead code, no unused imports.
- Do not catch `Exception`/`Throwable` and never swallow an exception silently.
- No `System.out`; use a logging library. No personal patient data in logs.
- Controllers contain no business logic: parse input, call one service, map the result.

### 5.4 Error handling

| Situation | Mechanism |
|---|---|
| Violated business rule or invalid input in the domain | Unchecked domain exceptions with a precise name: `DuplicatePatientException`, `InvalidDataException` |
| Expected "nothing there" | `Optional` / empty result (e.g. no patient waiting), not an exception |
| Web layer | One central handler maps domain exceptions to a single error response format (HTTP status + stable error code + message) |

The error **code** is stable; the front end maps it to the Spanish text shown to users.

---

## 6. Testing Conventions

- **Business logic is tested without a framework, a database or I/O:** instantiate the objects directly and pass a fixed clock.
- **Test behaviour, not implementation:** assert on observable results.
- **Arrange–Act–Assert**, separated by blank lines.
- **Deterministic and independent:** no sleeps, no shared mutable state, no dependence on execution order or on the real time zone.
- Every business rule has tests for the normal case, edge cases (empty, single element, limit *exactly* reached, duplicate, level change), invalid input and expected errors. Coverage ≥ 80 % is a floor, not a goal.
- **Traceability:** name the requirement or business-rule ID in the test and in the Javadoc of the implementing method, so a rule can be walked from requirement to implementation to test (`project.md` §20).

---

## 7. Database

- Schema changes only through new, numbered migration scripts; never alter a merged one.
- Declare constraints in the schema (`NOT NULL`, `UNIQUE`, foreign keys) in addition to validating in the domain; the domain is the source of truth.
- Parameterised queries only, never string concatenation with user input.
- No business logic in triggers or stored procedures: rules live in the domain so they can be tested without a database (`project.md` §8).

---

## 8. Documentation Conventions

- Comments explain **why**, not what. Public domain classes and methods carry Javadoc stating the rule or responsibility and the requirement ID.
- `docs/dev` is written in English; business documents keep their versioning convention and may contain Spanish terms.
- Diagrams in **Mermaid**, updated in the same PR as the code they describe.
- A decision that changes the architecture or the stack needs an ADR **before** the code (templates in `docs/templates/`).

---

## 9. Dependencies

- Add a dependency only if it clearly removes work; justify it in the PR description.
- Versions are pinned and lock files committed; no `latest`.
- Business-relevant values (wait limits per level, REQ-10) are **data**, not constants buried in code.
- An external service (at most one, `project.md` §9) is accessed through a purpose-built adapter with a fake implementation for tests.
