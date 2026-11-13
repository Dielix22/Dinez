# Database Migrations (Flyway)

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Scope** | How the schema is versioned, applied at start-up, and how to add a new migration |
| **Related** | [01 Project Structure §3.1](../01_project_structure.md) · [03 Coding Conventions §7](../03_coding_conventions.md) · [ADR-0004](../decisiones/ADR-0004-backend-tooling.md) |

---

## 1. How it works

- The database is PostgreSQL; the back end (`src/backend/`) uses Spring Boot, JDBC and **Flyway**.
- Migration scripts live in `src/backend/src/main/resources/db/migration/`.
- Flyway runs **automatically when the back end starts**, before the application serves requests. It applies, in version order, every script not yet recorded in the `flyway_schema_history` table. Nothing has to be run by hand.
- Connection settings are in `src/backend/src/main/resources/application.yaml`. Each one has a default and can be overridden by an environment variable:

| Setting | Environment variable | Default |
|---|---|---|
| JDBC URL | `DB_URL` | `jdbc:postgresql://localhost:5432/urgencias_db` |
| User | `DB_USER` | `postgres` |
| Password | `DB_PASSWORD` | `postgres` |

- The database `urgencias_db` must exist (Flyway creates tables, not the database itself):

```sql
CREATE DATABASE urgencias_db;
```

- On a database whose schema is **not empty** and has no `flyway_schema_history` table (e.g. the shared database provided for production), Flyway would refuse to start. `baseline-on-migrate: true` with `baseline-version: 0` makes it record a baseline at version 0 and then apply **all** migrations (V1 onwards). If an existing object has the same name as one created by a migration, the migration fails and must be fixed with a new migration or by using another schema.

- Domain unit tests use no database. Tests that load the application run on a disposable PostgreSQL container (Testcontainers) with Flyway enabled, so every migration is exercised; they need Docker and are skipped without it (see [ADR-0007](../decisiones/ADR-0007-persistence-layer.md)). H2 is no longer used.

## 2. Current migrations

| Version | File | Content |
|---|---|---|
| 1 | `V1__create_schema.sql` | Tables, constraints and indexes of the data model (`02_Interaction_and_Data_Design_v2.md`, §2.2 and §2.4) |
| 2 | `V2__insert_reference_data.sql` | The five triage levels and the queues of levels 2 to 5 |
| 3 | `V3__insert_default_wait_limits.sql` | **Provisional** wait limits per level (to be confirmed by the business) |

## 3. Adding a new migration

1. **Never edit a migration that is merged** (or already applied on anyone's database). Flyway stores a checksum of each applied script and refuses to start if it changed. To fix a mistake, add a new migration.
2. **Pick the next version number.** Look at the highest `V<n>` in `db/migration/` on the target branch and use `n + 1`.
3. **Create the file** in `src/backend/src/main/resources/db/migration/`, named:

   ```text
   V<version>__<short_description_in_snake_case>.sql
   ```

   Note the **two underscores** after the version. Examples: `V4__add_ticket_called_at.sql`, `V5__create_index_ticket_arrival_time.sql`.
4. **Write plain SQL**, one logical change per file, following the database conventions:
   - English `snake_case`, singular table names (`ticket`, `level_queue`).
   - Named constraints with a prefix: `pk_`, `fk_`, `uq_`, `ck_`; indexes with `uq_` or `ix_`.
   - Declare `NOT NULL`, `UNIQUE`, foreign keys and `CHECK`s in the schema.
   - No business logic in triggers or stored procedures.
   - Start the file with a comment saying why the change is needed.
5. **Test it on a throwaway database** before opening the PR:

   ```bash
   # from src/backend/
   ./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
   ```

   The log shows `Migrating schema "public" to version "<n> - <description>"`. To start again from scratch, drop and recreate `urgencias_db`.
6. **Commit the script in the same PR** as the code that needs it, and update the data model document if the model changed.

### Changing existing data

Seed or reference data that the application needs in every environment (such as the triage levels) is a versioned migration. Demo and test data are **not** migrations: they go in a separate set of seed scripts (see [Project Structure §3.1](../01_project_structure.md)).

### Two developers, same version number

If two branches both add `V4__...`, the second one to merge must renumber its file to the next free version before merging. Never merge two scripts with the same version.

## 4. Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `Connection refused` at start-up | PostgreSQL is not running, or `DB_URL` is wrong. |
| `database "urgencias_db" does not exist` | Create it (see §1). |
| `Validate failed: Migration checksum mismatch` | An applied migration was edited. Restore the original file and put the change in a new migration; on a local throwaway database you may instead drop and recreate it. |
| `Detected resolved migration not applied to database` | A migration with a lower version than the latest applied one was added. Renumber it to a higher version. |
