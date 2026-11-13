# ADR-0007: Persistence layer for tickets, counters and wait limits

Authors:
- @hoverwars

Date: 2026-11-13

## Status

Proposed

## Context

[ADR-0006](ADR-0006-ticket-assignment-api.md) delivered the first slice with the tickets, the queues and the daily counters **in memory** (`LevelQueue`, the counters of `TicketAssigner`, the wait limits hard-coded in `TurnConfig`). Only the patient was stored. A restart empties the queue and restarts the numbering, which does not meet CON-TRG-03, and the service had to be `synchronized` and run as a single instance.

[02 Interaction & Data Design v2](../design/02_Interaction_and_Data_Design_v2.md) already defines the target: `GestionadorColaPrioridad` and `AsignadorDeTurno` are facades with no state of their own (§2.1), the queue is represented by the columns `level`, `position` and `exit_time` of `ticket` (§2.2), and assigning a ticket is one transaction that increments the counter and inserts the ticket, with a `ROLLBACK` on a duplicate (§2.4, §3). The schema (migrations V1 to V3) already contains these tables and constraints.

[ADR-0004](ADR-0004-backend-tooling.md) left integration tests against real PostgreSQL for "a future ADR when persistence repositories are implemented". H2 cannot run the partial unique indexes nor `INSERT ... ON CONFLICT ... RETURNING` used by the schema.

## Decision

1. **The database is the source of truth.** The domain classes keep no queue or counter state; they depend on three ports (interfaces without framework imports): `TicketRepository`, `TicketCounterRepository` and `WaitLimitRepository`.
2. **Adapters use `JdbcClient` with explicit SQL** (`queue/persistence`, `turn/persistence`, `wait/persistence`), as already done for `PatientRepository`.
3. **One transaction per assignment** (`TicketAssignmentService.assignTicket`): patient, counter increment and ticket insert commit together or not at all. The `synchronized` modifier is removed.
4. **Concurrency is handled by the database**: the counter upsert locks the `(ticket_date, level)` row, which serialises requests of the same level; the partial unique indexes are the safety net, and a violation of `uq_ticket_waiting_patient` is translated by the adapter into `DuplicatePatientException`.
5. **`LevelQueue` is removed**: the per-level queue is the set of waiting rows of `ticket` ordered by `position` (design 2 v2 §2.2).
6. **`WaitTimeManager` reads the limits on demand** from `wait_limit`, so that no database access happens when the beans are built. The values hard-coded in `TurnConfig` are removed (V3 is the single source of the defaults).
7. **Integration tests run against PostgreSQL with Testcontainers and Flyway enabled.** Unit tests of the domain keep using in-memory fakes of the ports, without Spring or database. **H2 and `src/test/resources/application.yaml` are removed**: the test that loads the whole application (`BackendApplicationTests`) also runs on the PostgreSQL container, so no test runs on a database different from production. Web slice tests (`@WebMvcTest`) need no database. This supersedes the H2 part of [ADR-0004](ADR-0004-backend-tooling.md); the Maven Wrapper part stays.

No new migration is needed. The trigger of design 2 v2 §2.4 ("ticket level equals patient level while waiting") is not created, following [03 Coding Conventions §7](../03_coding_conventions.md): the rule is kept by the single transaction.

## Alternatives considered

- **Keep the in-memory domain and write through to the database, reloading at start-up:** less refactoring, but two sources of truth, still a single instance and no atomicity. Rejected.
- **Run integration tests on H2:** no Docker needed, but it cannot execute the partial unique indexes or the counter upsert, so the tests would not prove the real behaviour. Rejected.
- **Spring Data JDBC entities on the domain classes:** would put persistence annotations in the domain (conventions §5.2). Rejected.

## Consequences

### Positive

- The queue and the numbering survive a restart; several instances could run (the database serialises the counter).
- Duplicate rejection leaves the counter untouched, as the contract in the design requires.
- The limitations of ADR-0006 on in-memory state, `synchronized` and non-atomic patient/ticket writes no longer apply.

### Negative and trade-offs

- New test dependencies (Testcontainers, versions managed by the Spring Boot BOM) replace H2. Docker is needed to run the tests that load the application; they are skipped without Docker (the domain unit tests still run) and run in CI.
- Every assignment issues a few queries instead of working in memory.
- `ColaPorNivel` of the design has no class any more; it exists only as queries.
