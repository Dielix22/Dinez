# ADR-0008: Back-end structure, and where repositories and entities live

Authors:
- @hoverwars

Date: 2026-11-25

## Status

Proposed

Supersedes the parts of [ADR-0007](ADR-0007-persistence-layer.md) about the location of the repository interfaces and their adapters (decisions 1 and 2). The rest of ADR-0007 remains valid.

## Context

The structure of the back end was not established, which caused problems during implementation. [ADR-0007](ADR-0007-persistence-layer.md) added the persistence layer on top of that unsettled structure, with the following flaws:

| ADR-0007 | What was wrong |
|---|---|
| Decision 1: the domain classes depend on repository interfaces (ports) | The interfaces of the persistence layer live outside it. `TicketRepository`, `TicketCounterRepository` and `WaitLimitRepository` are in `<module>/domain`, while `PatientRepository` is in `turn/application`. |
| Decision 2: the adapters are in `queue/persistence`, `turn/persistence` and `wait/persistence` | A repository is split between two packages: its interface in one, its implementation in another. There is no single place to look for persistence code. |
| Decisions and alternatives: only the domain classes and the adapters are described | No place is defined for entities, nor how they differ from the other models of the application. |

The team has now defined the structure in [01 Project Structure §2.1](../01_project_structure.md).

## Decision

1. **The code is organised by layer first, then by module.** `com.adres.urgencias` contains `config`, `presentation`, `service`, `model` and `persistence`. `config` has no subfolders.
2. **In the four layer folders, a class with a business link with a module goes in the subfolder of that module**, even if other modules use it. Only classes without a business link stay at the root of the layer folder. The `shared` package disappears.
3. **Repositories, interfaces and implementations alike, live in `persistence/<module>/repository`.** The services depend on the interfaces. This replaces decisions 1 and 2 of ADR-0007.
4. **Entities live in `persistence/<module>/entities`** and are separate from the classes of `model`, which are not entities.
5. **The `domain` and `application` packages are replaced** by `service` (application logic) and `model`. Spring wiring such as `TurnConfig` goes in `config`.

Unchanged from ADR-0007: the database is the source of truth, `JdbcClient` with explicit SQL, one transaction per assignment, concurrency handled by the database, the removal of `LevelQueue`, wait limits read on demand, and integration tests on PostgreSQL with Testcontainers.

## Alternatives considered

- **Keep the module-first layout (`<module>/<layer>`) and only document it:** no code to move, but repositories stay split between several packages. Rejected.
- **Keep the repository interfaces next to the services:** the logic would not depend on the persistence package, but a repository would still be split between two places. Rejected.

## Consequences

### Positive

- Each kind of class has one defined place.
- All persistence code is under `persistence`, with repositories and entities kept apart.
- The services can still be tested without a database, with in-memory implementations of the repository interfaces.

### Negative and trade-offs

- The services now depend on the `persistence` package: the isolation of the business logic relies on using the interfaces only, never a `Jdbc...` class or an entity-specific technology.
- Deciding which module a class belongs to requires a judgement on its business link.
- The existing code does not follow this structure yet and will be aligned later. Until then, the paths in [Ticket API §4](../code/ticket_api.md) describe the current code.