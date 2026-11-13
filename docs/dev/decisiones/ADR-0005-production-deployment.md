# ADR-0005: Production deployment as a single Docker container (back end serving the built front end, external database)

Authors:
- @hoverwars

Date: 2026-11-12

## Status

Proposed

## Context

[ADR-0001](ADR-0001-stack-tecnologico.md) chose a Java / Spring Boot back end, a React front end and PostgreSQL. The course statement ([project.md](../../../project.md), §5) requires a **monolith: a single repository, a single deployable artifact, a single running process**, and states that the interface must be *server-rendered or otherwise integrated into the same deployable artifact* — "a separate SPA-style frontend consuming a REST API as an internal boundary is not allowed". Deployment itself is optional (§25), but when done it must use free services.

Two facts shape the deployment:

- The React front end is built with Vite and produces static files. Deploying them on their own (another web server or container) would be exactly the separate SPA the instructor forbids.
- The instructor provides a PostgreSQL database for the teams. The application only needs to be given its connection settings.

The team also needs deployment to be automatic after each merged pull request (see [02 Collaboration Workflow](../02_collaboration_workflow.md)), so that `main` is always what runs.

## Decision

1. **One Docker container, `g1_team_3_app`, runs the whole product.** A multi-stage `infra/Dockerfile` builds the front end (Node), copies its output into `src/main/resources/static` of the back end, packages one Spring Boot jar (JDK + Maven Wrapper) and runs it on a minimal JRE image. The back end serves the front end files itself (`StaticFrontendConfig`), with a fallback to `index.html` for client-side routes (`SpaFallbackResourceResolver`). Front end and API share one origin, one process and one artifact.
2. **No database container.** `infra/compose.yml` defines only the `app` service. The external database is reached through the `DB_URL`, `DB_USER` and `DB_PASSWORD` variables, injected from GitHub secrets into an `.env` file on the server. Flyway applies the migrations when the application starts.
3. **Automatic deployment on every push to `main`** (a merged PR) with a GitHub Actions workflow: build the image, copy it to the server over SSH (`docker save | ssh docker load`), upload the compose file and `.env`, run `docker compose up -d` and wait for the health check. Authentication is SSH with a user and a private key stored as a secret; no container registry is used.

The full procedure and the required secrets are described in [Deployment](../code/deploy.md).

## Alternatives considered

- **Separate front-end container (nginx) plus back-end container:** rejected: it is the separate SPA that `project.md` §5 forbids, and it adds a second artifact and a reverse proxy or CORS configuration.
- **Keep the SPA but only serve the files from a directory next to the jar:** rejected: the jar would no longer be the single deployable artifact, and the front end could drift from the back-end version.
- **A PostgreSQL container in the compose file:** rejected: the instructor already provides a database; running our own would duplicate it, require volumes, backups and credentials management, and does not match the target environment.
- **Publishing the image to a container registry (GHCR, Docker Hub):** rejected for now: it adds a registry account, credentials on the server and a retention policy for a single-server, single-image project. Transferring the image over the existing SSH connection needs no extra service. It can be revisited if several servers are needed.
- **Building the image on the server (git pull + docker build):** rejected: it needs the repository and build resources on the server and makes the deployed version depend on the server state.
- **Password authentication over SSH:** rejected: key-based authentication is safer and can be restricted to a dedicated deployment user.

## Consequences

### Positive

- Complies with the monolith and "integrated interface" rules of the course: one jar, one container, one process.
- No CORS configuration is needed and the front end always matches the API version.
- The server only needs Docker and an SSH user; the database stays outside the deployment.
- Every merged PR is deployed identically and reproducibly; the image is tagged with the commit SHA for traceability.

### Negative and trade-offs

- A front end change rebuilds and redeploys the whole container (acceptable for a single-team project).
- The image is transferred on every deployment instead of being pulled from a registry layer cache; this costs time and bandwidth.
- The deployment depends on the instructor's database being reachable from the server; if it is down the application cannot start (Flyway runs at startup).
- Tests are not re-run during the build of the image: they are expected to have passed in the pull request CI, which is a required status check on `main`.
- Deployment has no automatic rollback: restoring a previous version means redeploying an earlier commit (the previous image stays on the server until it is pruned).
- Values written to `.env` are single-quoted, so a secret must not contain a single quote character.
