# Project Structure

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Status** | Draft — proposal for team review |
| **Scope** | Repository layout, code organisation, test layout, documentation placement |
| **Related** | [02 Collaboration Workflow](02_collaboration_workflow.md) · [03 Coding Conventions](03_coding_conventions.md) |

---

## 1. Purpose and Guiding Principles

This document defines how the repository is organised so that every team member can find, run, test and explain any part of the product. It follows the stack decision in [ADR-0001](decisiones/ADR-0001-stack-tecnologico.md) (a monolith with a Java back end and a React front end) and the accepted design documents in `docs/dev/design/`. Specific tools (build tool, libraries, linters) are deliberately **not** fixed here; they will be chosen later and documented in the README.

| # | Principle | Why |
|---|---|---|
| P1 | **One repository** for back end, front end and documentation. | Single history, atomic changes across layers, simple review. |
| P2 | **Business rules are isolated from infrastructure** and testable without a server, a database or the network. | Course requirement and ADR-0001. |
| P3 | **The code mirrors the documented architecture.** A component in the design diagram can be located in the repository by name. | Architecture documentation must match the code. |
| P4 | **Vertical slices.** Each increment crosses interface → presentation → service and model → persistence → tests. | Iterative, incremental development. |
| P5 | **Reproducible setup.** A newcomer can clone, run and test following the README alone. | Verifiable README. |
| P6 | **Minimal ceremony.** Five students, one semester: the simplest structure that satisfies P1–P5. | Team context. |

---

## 2. Repository Layout

```text
Dinez/
├── .github/
│   └── workflows/                    # CI definition (build + tests on every PR) and deployment
├── infra/                            # Dockerfile and docker compose for production
├── src/
│   ├── backend/                      # Java application
│   │   └── src/
│   │       ├── main/java/com/adres/urgencias/   # application code (see §2.1)
│   │       └── test/java/com/adres/urgencias/   # back-end tests
│   └── frontend/                     # React + TypeScript application (Vite)
│       ├── src/                      #   application code
│       │   ├── pages/                #     one component per screen
│       │   ├── api/                  #     calls to the back end
│       │   └── i18n/                 #     user-facing text (Spanish)
│       ├── tests/                    #   front-end tests
│       ├── index.html                #   entry point
│       ├── package.json              #   scripts and pinned dependencies
│       ├── package-lock.json         #   lock file (committed)
│       ├── vite.config.ts            #   Vite and Vitest configuration
│       ├── tsconfig.json             #   TypeScript configuration
│       └── README.md                 #   build, run and test commands
├── docs/
│   ├── business/                     # the problem: requirements, user stories, domain material
│   │   └── requirements/
│   ├── dev/                          # the solution and how we build it
│   │   ├── code/                     #   code-related documentation, e.g. how to run the solution
│   │   ├── design/                   #   architecture and detailed design
│   │   └── decisiones/               #   ADR / RFC
│   └── templates/                    # ADR, RFC, requirement and design templates
├── .editorconfig
├── .gitattributes
├── .gitignore
└── README.md                         # what it is, how to run, how to test, AI-use disclosure
```

Rules:

1. **`src/backend/` and `src/frontend/` are independent projects inside one repository**, each with its own dependency file (at the root of its folder), its own test folder next to the application code (`src/test/java/` for the back end, `tests/` for the front end) and its own README section for build, run and test commands. All product code lives under `src/`; the repository root only holds configuration and documentation. A top-level section of the root `README.md` gives the exact commands to run the whole product.
2. **`docs/business/`** holds the *problem* (requirements, user stories, domain material); **`docs/dev/`** holds the *solution and the way we work* (design, decisions, conventions); **`docs/templates/`** holds the document templates. Do not mix them.
3. **Nothing generated or machine-specific is committed**: build output, dependency folders, IDE settings, logs, local database files, `.env` files.
4. **Dependency lock files and any build-tool wrapper are committed**, so that everyone builds with the same versions.
5. **Large binaries are avoided**; the PDFs already in `docs/business` are the exception.
6. The root `README.md` is the entry point for an outsider and is kept in sync with the real commands.

### 2.1 Back-end structure

The back-end code is organised **by layer first, then by module** ([ADR-0008](decisiones/ADR-0008-backend-structure.md)). The package `com.adres.urgencias` contains five folders:

| Folder | Content |
|---|---|
| `config/` | Application configuration. No subfolders |
| `presentation/` | Controllers, requests and responses |
| `service/` | Application logic |
| `model/` | Models used in the application that are **not** entities |
| `persistence/` | Persistence: repositories and entities |

The four layer folders (all but `config/`) are built the same way:

- **At the root:** the general classes of the application, which have no business link with a module.
- **One subfolder per module** (for example `turn`): the classes that have a business link with that module.

`persistence/` is the exception inside its modules: each module subfolder contains two subfolders, **`repository/`** and **`entities/`**, which keep the two apart. General persistence classes stay at the root of `persistence/`.

The tree below illustrates these rules with a few classes of one module; names marked `(example)` do not exist in the code. The existing code does not follow this structure yet; it will be aligned later.

```text
com/adres/urgencias/
├── BackendApplication.java
├── config/                                 # no subfolders
│   └── StaticFrontendConfig.java
├── presentation/
│   ├── ApiExceptionHandler.java            # general
│   └── turn/                               # module "turn"
│       └── TicketController.java
├── service/
│   └── turn/
│       └── TicketAssignmentService.java
├── model/
│   ├── InvalidDataException.java           # general: no business link with a module
│   └── turn/
│       └── Ticket.java                     # used by other modules, belongs to "turn"
└── persistence/
    └── turn/
        ├── repository/
        │   ├── PatientRepository.java      # interface
        │   └── JdbcPatientRepository.java  # implementation
        └── entities/
            └── PatientEntity.java          # (example)
```

Placement rules:

1. A class with a business link with a module goes in that module's subfolder, **even if other modules use it**. Only a class without any business link is general and goes at the root of the layer folder. The component responsibilities of the design documents ([01](design/01_Architectural_and_Detailed_Design_v2.md) §3.3, [02](design/02_Interaction_and_Data_Design_v2.md) §2.3) tell which module a class belongs to.
2. A module only has a subfolder in the layers where it has classes.
3. An entity represents stored data and lives in `persistence/<module>/entities/`. Any other model lives in `model/`.
4. A repository, interface and implementation alike, lives in `persistence/<module>/repository/`.
5. Tests mirror this structure: the test of `service/turn/TicketAssignmentService` is in `service/turn/` under `src/test/java/`.

---

## 3. Build, Run and Test Entry Points

Whatever tools are chosen, the README documents and CI runs exactly the same commands:

| Goal | Expectation |
|---|---|
| Run the back end | One documented command, with demo data, no manual configuration |
| Run the front end | One documented command |
| Run all back-end tests | One command, includes the coverage check |
| Run all front-end tests | One command |
| Check formatting | One command, same one as in CI |

Configuration has working defaults: the application must start without hand-set environment variables. Anything environment-specific (database location, ports) is overridable but never required. No secrets are committed.

### 3.1 Database and seed data

| Item | Convention |
|---|---|
| Schema | Versioned in the repository as ordered, numbered migration scripts. **A merged migration is never edited**; add a new one. |
| Seed data | A separate set of scripts, not applied in automated tests. It covers all five triage levels, expired and non-expired waits, empty and full queues, duplicates and other edge cases. |
| Local setup | Scripted or containerised so that nobody configures the database by hand; documented in the README. |

---

## 4. Documentation Placement

| Content | Location | Format |
|---|---|---|
| Requirements | `docs/business/requirements/` | Markdown, versioned by file suffix (`_v2`) as today |
| Architecture and detailed design | `docs/dev/design/` | Markdown + Mermaid |
| Decisions | `docs/dev/decisiones/` (`ADR-NNNN-…`, `RFC-NNNN-…`) | From `docs/templates/` |
| Engineering conventions | `docs/dev/` | Markdown, English |
| Learning log (what we learned / what changed / evidence, one row per finding) | `docs/business/evolution_log.md` | Markdown table, appended each delivery |

Documentation changes ship in the **same PR** as the code they describe; diagrams must render on GitHub. If the code and a diagram disagree, the PR that introduced the gap is incomplete.

