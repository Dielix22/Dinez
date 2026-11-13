# ADR-0006: API and screen in front of the ticket assigner

Authors:
- @Dielix22

Date: 2026-11-12

## Status

Proposed

> Number 0005 is used by the deployment ADR of pull request #34.

## Context

The first vertical slice implements REQ-04 (assign a ticket from the triage result). This change contains the whole slice: the domain of the ticket assigner (`TicketAssigner`, `PriorityQueueManager`, `LevelQueue`), the API that calls it and the front end screen. The modules that would call the assigner in the design (Triage, Registration) do not exist yet, and the design defines `asignarTurno(idPaciente, nivel)` between internal modules but not its HTTP form. **The design documents were not modified.**

## Decision

1. **The screen plays the role of Triage.** The page `/tickets` sends `patientId`, `document`, `firstName`, `lastName` and `level` to `POST /api/tickets`.
2. **The API stores the patient and then calls the assigner**, in that order: insert (or update) the row in `patient`, then `TicketAssigner.assignTicket(patientId, level)`. The domain operation keeps the two parameters of the design contract.
3. **The API contains no ticket rule.** Number, queue position and duplicate detection are decided by the domain classes. The API only orchestrates, translates HTTP and converts times.
4. **Level 1 is answered by the API as a success without ticket** (`200`, `immediateCare: true`), following REQ-04 scenario 2: the patient is stored with level 1 and the assigner is not called, because the assigner only accepts levels 2 to 5.
5. **A patient who is already waiting is rejected before anything is stored**, whatever the new level (the API asks `PriorityQueueManager.patientExists`). Changing the level of a waiting patient is REQ-05.
6. **Error codes are the ones of the domain exceptions**: `DATOS_INVALIDOS` (`400`) and `PACIENTE_DUPLICADO` (`409`).
7. **The hospital's time zone decides the day** (`app.time-zone`, default `America/Bogota`). The domain works with hospital local time; the API returns `arrivalTime` as a UTC instant.
8. **Patient persistence uses `JdbcClient` with explicit SQL** behind the `PatientRepository` interface, so that the domain class `Patient` carries no framework annotations.

Besides the assignment, only `GET /api/tickets` (list the queue) is added.

## Alternatives considered

- **Let the assigner receive the patient data and store it:** fewer classes in the API, but the domain operation would no longer match the design contract `asignarTurno(idPaciente, nivel)`.
- **Reject level 1 as invalid data**, as the assigner does: simpler, but contradicts REQ-04 scenario 2 and loses the level of the patient (BR-TRG-03).
- **Preload patients as seed data and send only `patientId` and `level`:** the demo could only use the preloaded patients.

## Consequences

### Positive

- The API can be explained in two steps (store the patient, call the assigner) and matches the team's module diagram.
- A rejected request leaves nothing in the database.

### Negative and trade-offs

- **Tickets, queues and daily counters are only in memory** (they belong to the domain objects). A restart empties the queue and restarts the numbering, which does not meet CON-TRG-03 yet. The tables `ticket` and `ticket_counter` exist but are not written.
- Because of that in-memory state, the service processes one request at a time and the application must run as a single instance.
- The patient row and the in-memory ticket are not updated atomically: if the database commit failed after the assigner accepted the ticket, the ticket would exist without its patient row.
- The level 1 decision lives in the API, not in the domain; it should move to the domain when Triage is designed.
- The SQL of `JdbcPatientRepository` uses PostgreSQL syntax (`ON CONFLICT`) and is not exercised by the automated tests (the test profile uses H2 without the schema, [ADR-0004](ADR-0004-backend-tooling.md)).
