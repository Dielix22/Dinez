# ADR-0004: Back-end build and test isolation setup (Maven Wrapper, H2)

Authors:
- @eLDIEGO12

Date: 2026-11-12

## Status

Proposed

## Context

[ADR-0001](ADR-0001-stack-tecnologico.md) chose Java, Spring Boot, PostgreSQL, Spring Data JDBC and Flyway for the back end, leaving the build runner and local/CI test isolation details to be defined when initialising `src/backend/` (see [01 Project Structure](../01_project_structure.md)).

The back end is initialised in `src/backend/` using Spring Boot 4.1.2 and Java 25. Because `src/main/resources/application.yaml` points to a PostgreSQL instance on `localhost:5432` with Flyway enabled, running the baseline context test (`BackendApplicationTests`) fails with `Connection refused` whenever PostgreSQL is not running locally or in CI. The team needs a reproducible setup where a single command builds and tests the project without requiring an external database service.

## Decision

The back end in `src/backend/` uses:

- **Maven with Maven Wrapper (`mvnw` / `mvnw.cmd`)** as the build and test tool so the project compiles and runs tests reproducibly with a single command (`./mvnw test` or `mvnw.cmd test`).
- **Isolated test properties in `src/test/resources/application.yaml`** that override the datasource to an in-memory database (`jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`) and set `spring.flyway.enabled: false` during tests, leaving `src/main/resources/application.yaml` untouched for PostgreSQL and Flyway.
- **H2 Database (`com.h2database:h2`) in `<scope>test</scope>`** in `pom.xml` so Spring Data JDBC can resolve a SQL dialect and load the `ApplicationContext` during tests without packaging H2 into the application artifact.

## Alternatives considered

- **Requiring a running PostgreSQL instance for `mvnw test`:** rejected because baseline context tests would fail on machines or CI runners without an active PostgreSQL service.
- **Removing datasource configuration or modifying `src/main/resources/application.yaml`:** rejected because Spring Data JDBC requires an active `DataSource` at startup to determine the SQL dialect, and `src/main` must remain configured for PostgreSQL and Flyway.
- **Gradle instead of Maven:** Maven with the committed Maven Wrapper provides standard Spring Boot dependency management and reproducible execution across Windows and Unix environments without a pre-installed build tool.

## Consequences

### Positive

- A reproducible build and test verification with a single command (`./mvnw test` or `mvnw.cmd test`) locally and in CI without external database dependencies.
- Complete separation between test execution (`src/test/resources/application.yaml` and `<scope>test</scope>`) and runtime configuration (`src/main` with PostgreSQL and Flyway).

### Negative and trade-offs

- H2 (`MODE=PostgreSQL`) does not replicate 100% of PostgreSQL-specific SQL features or constraints, and Flyway migrations are disabled in the baseline test profile.
- Integration tests that verify actual Flyway migrations or PostgreSQL-specific queries are **not** covered by this setup and will be addressed in a future ADR when persistence repositories are implemented.