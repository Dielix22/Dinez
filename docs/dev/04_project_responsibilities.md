# Project Responsibilities

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Status** | Draft — proposal for team review |
| **Scope** | Ownership of each part of the product, assignment of owners and reviewers, fallback rule |
| **Related** | [01 Project Structure](01_project_structure.md) · [02 Collaboration Workflow](02_collaboration_workflow.md) · [03 Coding Conventions](03_coding_conventions.md) |

---

## 1. Purpose

This document defines **who is responsible for what**. When a task touches one of the parts below, the owner of that part is the person who should do it, and the associated reviewer is the person who reviews it. It complements the generic round-robin rule of [02 Collaboration Workflow §5.2](02_collaboration_workflow.md) with a fixed ownership model, so that nobody has to wonder who picks up a task or who reviews it.

Ownership means **primary responsibility**, not exclusivity: every member must still be able to explain any part of the code (`project.md` §22).

---

## 2. The Five Parts

The product is split into five parts. A task that crosses several parts is split along these boundaries (vertical slices, see [01 Project Structure](01_project_structure.md) P4), and each piece goes to the owner of the part concerned.

### 2.1 Persistence (code side)

Scope: the **repository** layer and the **domain** model.

- Domain entities, value objects, enums and domain exceptions (`Ticket`, `TriageLevel`, `LevelQueue`, `DuplicatePatientException`, ...).
- The business invariants enforced by construction (valid-by-construction objects, no framework imports, injected `Clock`), following [03 Coding Conventions §5.2](03_coding_conventions.md).
- Repository interfaces and their implementations: mapping between domain objects and the database, parameterised queries, fake/in-memory implementations used by tests.
- Unit tests of the domain and of the repositories, including the ≥ 80 % coverage gate on `domain`.

Out of scope: the database schema and migration scripts (see §2.4) and orchestration of use cases (see §2.3).

### 2.2 Presentation

Scope: the **back-end interface** exposed to the front end.

- Controllers: parse input, call one service, map the result. No business logic.
- Request and response objects (DTOs) and their mapping to and from domain objects.
- The central error handler: mapping domain exceptions to a single error response format (HTTP status + stable error code + message).
- The API contract (routes, payloads, status codes, error codes) and its documentation, kept in sync with the front end.
- Tests of the web layer.

Out of scope: any business rule (see §2.3) and any rendering concern (see §2.5).

### 2.3 Logic

Scope: the **application / service layer** that implements the use cases of the requirements.

- Services that orchestrate domain objects and repositories to fulfil a requirement (assign a ticket, serve the next patient, change a wait limit, enforce daily counters, ...).
- Applying the business rules and requirements (including priority and wait-limit behaviour) through the domain's named methods, and keeping the traceability from requirement to implementation to test.
- Transaction boundaries and the calls to the single external service, if any, through its adapter.
- Unit tests of the services, with fakes for repositories and a fixed clock.

Out of scope: the definition of domain objects and their invariants (see §2.1), HTTP concerns (see §2.2) and storage concerns (see §2.4).

### 2.4 Persistence and Cloud (database side)

Scope: the **database** and everything needed to run and deploy it.

- The schema: tables, columns, constraints (`NOT NULL`, `UNIQUE`, foreign keys) and indexes, following [03 Coding Conventions §7](03_coding_conventions.md).
- Ordered, numbered **migration scripts**. A merged migration is never edited; a new one is added.
- Seed data scripts (all five triage levels, expired and non-expired waits, empty and full queues, duplicates and edge cases).
- Local database setup (scripted or containerised), `infra/` (Dockerfile, docker compose), the CI/CD workflows in `.github/workflows/` and the deployment to the cloud.
- Environment configuration: overridable defaults, no committed secrets.

Out of scope: Java persistence code (see §2.1).

### 2.5 Front-end

Scope: the **React + TypeScript** application in `src/frontend/`.

- Pages (one component per screen), components, hooks and routing.
- API calls to the back end (`api/`) and their error handling, based on the contract owned by Presentation.
- User-facing text in Spanish, centralised in `i18n/`, including the mapping from stable error codes to messages.
- Front-end tests, accessibility and usability of the screens.
- No business rule is re-implemented in the UI.

Out of scope: any back-end code.

---

## 3. Assignment

The part numbers follow the order of §2.

| # | Part | Owner | Reviewer |
|---|---|---|---|
| 1 | Persistence (code side) | Sebastian Eisneider Rey Pretel (@sereypretel) | Bryan Gustavo Llontop Angulo (@BLlontopSec) |
| 2 | Presentation | Diego Stevan Anacona Velez (@elDIEGO12) | Sebastian Eisneider Rey Pretel (@sereypretel) |
| 3 | Logic | Bryan Gustavo Llontop Angulo (@BLlontopSec) | Diego Ibáñez (@Dielix22) |
| 4 | Persistence and Cloud (database side) | Adrien ROHO (@hoverwars) | Diego Stevan Anacona Velez (@elDIEGO12) |
| 5 | Front-end | Diego Ibáñez (@Dielix22) | Adrien ROHO (@hoverwars) |

### 3.1 When the owner is not involved

If the owner of a part is not involved (unavailable, unresponsive, or blocking the team), **any developer may implement the requested feature, even if it falls outside their own scope**, so that the work is never blocked.

In that case the PR requires **two reviewers**:

1. **The owner of the part**, if they are present; and
2. **The initial reviewer** associated with that part in the table above, **or**, if the initial reviewer is the person who developed the change, **the reviewer associated with the developer** (the reviewer of the part the developer owns).

Example: Adrien (part 4) implements a change in Logic (part 3), whose owner is Bryan. The PR is reviewed by Bryan (owner, if present) and Diego Ibáñez (initial reviewer of Logic).

Example: Diego Ibáñez (part 5) implements a change in Logic (part 3). Diego is the initial reviewer of Logic, so the second reviewer is Adrien ROHO, who is the reviewer associated with Diego's own part. The owner, Bryan, is the first reviewer if present.

These approvals count towards the merge requirements in [02 Collaboration Workflow §4.3](02_collaboration_workflow.md); this rule only defines who the reviewers are.
