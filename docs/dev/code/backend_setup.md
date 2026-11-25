# Back-end Setup and Execution

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Scope** | Tools required, how to configure, run and test the back end |
| **Related** | [Database Migrations](database_migrations.md) · [Ticket API](ticket_api.md) · [03 Coding Conventions](../03_coding_conventions.md) · [ADR-0004](../decisiones/ADR-0004-backend-tooling.md) · [ADR-0007](../decisiones/ADR-0007-persistence-layer.md) |

The back end lives in `src/backend/`. It is a Spring Boot project: every `mvnw` command below is run **from that folder**.

---

## 1. Required software

| Software | Version | Notes |
|---|---|---|
| JDK | **25** | |
| PostgreSQL | 17 recommended (the version used by the tests) | Needed to run the application. It can be installed locally or run with Docker (see §2.1). |
| Docker | Any recent version | Optional to run the application, **needed to run all the tests** (see §4). |
| Git | Any recent version | |

Maven does not need to be installed: the Maven Wrapper (`mvnw`, `mvnw.cmd`) downloads the right version the first time it runs, which requires a network connection.

Check your installation:

```bash
java -version   # 25
```

---

## 2. Run the back end

### 2.1 Create the database (first time)

The database `urgencias_db` must exist before the application starts. The tables are created by the application itself, not the database.

With a local PostgreSQL:

```bash
psql -U postgres -c "CREATE DATABASE urgencias_db;"
```

Or with Docker, which creates a server and the database matching the default settings:

```bash
docker run -d --name urgencias-db -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=urgencias_db postgres:17-alpine
```

Later, start this container again with `docker start urgencias-db`.

### 2.2 Environment variables

**Every database setting has a default value, defined in [`application.yaml`](../../../src/backend/src/main/resources/application.yaml).** If your PostgreSQL matches the defaults, there is nothing to configure.

| Environment variable | Default value | Meaning |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/urgencias_db` | JDBC URL of the database |
| `DB_USER` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |

In `application.yaml`, `${DB_USER:postgres}` means: use the variable `DB_USER` if it is set, otherwise `postgres`.

To override a value, set the variable in the terminal that starts the application (or in the run configuration of your IDE):

```bash
# Mac/Linux
export DB_PASSWORD='my_password'
```

```powershell
# Windows PowerShell
$env:DB_PASSWORD = 'my_password'
```

```bat
:: Windows CMD
set DB_PASSWORD=my_password
```

The back end **does not read `.env` files**: a `.env` file in `src/backend/` has no effect. Do not write your own password in `application.yaml`, which is committed.

### 2.3 Start the application

```bash
cd src/backend

# Mac/Linux
sh mvnw spring-boot:run

# Windows (PowerShell or CMD)
.\mvnw.cmd spring-boot:run
```

At start-up, Flyway applies the pending migrations (see [Database Migrations](database_migrations.md)). The application is ready when the log shows `Started BackendApplication`.

Check that it answers by opening <http://localhost:8080/api/helloworld>: it returns a JSON with `"status": "success"`. Stop the application with `Ctrl+C`. Restart it after changing the code.

In development this serves only the API. To use the screens, also start the front end (see [Front-end Setup](frontend_setup.md)).

---

## 3. Commands

On Windows, replace `sh mvnw` with `.\mvnw.cmd`.

| Goal | Command |
|---|---|
| Run the application | `sh mvnw spring-boot:run` |
| Run all tests | `sh mvnw test` |
| Run one test class | `sh mvnw test "-Dtest=TicketAssignerTest"` |
| Test and build the jar (output in `target/`) | `sh mvnw package` |
| Delete the build output | `sh mvnw clean` |

---

## 4. Tests

The tests do not use your local database, and the environment variables of §2.2 are not needed.

| Kind of test | Needs Docker |
|---|---|
| Domain, service and web layer tests | No |
| Tests that load the whole application (`BackendApplicationTests`, `PersistenceIntegrationTest`), on a disposable PostgreSQL container (Testcontainers) | Yes |

**Without Docker running, the second group is skipped and the build still succeeds.** Check the number of skipped tests in the Maven summary: a green build with skipped tests has not verified the persistence layer or the migrations.

Before opening a PR, run `sh mvnw test` with Docker running; it must pass. CI runs the same tests on every PR that changes the back end.

---

## 5. Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| `release version 25 not supported` | Maven uses an older JDK. Point `JAVA_HOME` to JDK 25 and open a new terminal. |
| `./mvnw: Permission denied` (Mac/Linux) | The script is not executable in the repository. Run it with `sh mvnw`. |
| `mvnw.cmd` is not recognized (PowerShell) | Add the path: `.\mvnw.cmd`. |
| `Connection to localhost:5432 refused` | PostgreSQL is not running, or `DB_URL` is wrong. |
| `database "urgencias_db" does not exist` | Create it (see §2.1). |
| `password authentication failed for user "postgres"` | Your password is not the default one: set `DB_PASSWORD` (see §2.2). |
| `Port 8080 was already in use` | Another application uses the port, often a back end still running. Stop it: the front end expects the back end on port 8080. |
| Flyway errors at start-up | See [Database Migrations, Troubleshooting](database_migrations.md#4-troubleshooting). |
