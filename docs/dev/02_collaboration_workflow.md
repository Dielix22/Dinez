# Collaboration Workflow

| | |
|---|---|
| **Project** | Adres en Urgencias (repository `Dinez`) |
| **Status** | Draft — proposal for team review |
| **Scope** | Branching model, commits, pull requests, code review, CI, work tracking |
| **Related** | [01 Project Structure](01_project_structure.md) · [03 Coding Conventions](03_coding_conventions.md) |

---

## 1. Purpose and Constraints

The workflow must satisfy the course rules and remain light enough for five students working part-time.

| Course rule (`project.md`) | Consequence for the workflow |
|---|---|
| §4.1 Real history, each member commits under their own identity | Every member configures their own `user.name`/`user.email`; no shared accounts; no one commits on behalf of others. |
| §4.2 No direct commits to `main`; every PR approved by at least one other member | Branch protection on `main`; mandatory review. |
| §4.3 ≥ 80 % coverage on business logic | Coverage gate in CI. |
| §22 Any member may be questioned about any code | Review is also a *knowledge-sharing* mechanism, not only a quality gate. |
| §23 AI use is allowed but must be understood and disclosed | AI-assisted work follows the same review rules and is disclosed (§7). |

**Chosen model: GitHub Flow** — a single long-lived branch (`main`), short-lived topic branches, pull requests. Git Flow (`develop`, `release/*`, `hotfix/*`) is deliberately *not* used: the product is a continuously evolving service with five deliveries, and the extra branches would add merge overhead without adding safety.

---

## 2. Branching

### 2.1 Branches

| Branch | Lifetime | Rules |
|---|---|---|
| `main` | Permanent | Always buildable and always passing tests. Protected. Every delivery (E1–E5) is marked with a tag. |
| Topic branches | Hours to a few days | One branch = one purpose = one PR. Deleted after merge. |

Aim for branches that live **no longer than three working days**. A branch that lives longer should be split, or its pending work merged behind an incomplete-but-harmless state.

### 2.2 Naming

```text
<type>/<issue-number>-<short-kebab-description>
```

| Type | Use | Example |
|---|---|---|
| `feat` | New behaviour (a slice of a requirement) | `feat/42-assign-turn-number` |
| `fix` | Defect correction | `fix/57-duplicate-patient-counter` |
| `refactor` | Restructuring without behaviour change | `refactor/61-extract-wait-limits` |
| `test` | Tests only | `test/63-req11-edge-cases` |
| `docs` | Documentation only | `docs/45-req-12-wording` |
| `chore` | Build, CI, tooling, dependencies | `chore/12-add-spotless` |

Rules: lower case, ASCII, hyphens, no personal prefixes. The issue number is optional only for trivial `docs`/`chore` branches. (Existing branches such as `doc/…` and `docs/…` remain as they are; the convention applies from now on.)

### 2.3 Keeping a branch current

Before opening a PR and again before merging, bring the branch up to date with `main`:

```bash
git fetch origin
git rebase origin/main        # preferred for private, unreviewed branches
# or: git merge origin/main   # when others already pushed to the branch
```

**Never rebase or force-push a branch someone else is reviewing or has pushed to**, without telling them. Use `--force-with-lease`, never plain `--force`. Never force-push `main` (blocked by protection).

---

## 3. Commits

### 3.1 Granularity

- A commit is a **coherent step that builds and passes tests** where practical.
- Prefer several small commits to one large commit; do not mix formatting changes, refactors and behaviour changes in the same commit.
- Commit the test with the code it covers, or before it.
- Commit early locally; clean up (`git rebase -i` on your own branch) before requesting review if the history is noisy.

### 3.2 What never goes in a commit

Secrets, credentials, personal data (the product handles *simulated* patient data only — never real patient data), generated artifacts (build output, dependency folders), IDE settings, runtime database files.

---

## 4. Pull Requests

### 4.1 Opening

1. Push the branch and open a PR against `main`.
2. Give it a short, descriptive title that says what the change does.
3. In the description, explain what changes and why, link the issue (`Closes #NN`), and list the requirements or business rules touched and how the change was tested.

### 4.2 Size and scope

| Guideline | Target |
|---|---|
| Size | ≤ **400 changed lines** (excluding generated files and pure renames). Larger PRs must be split or justified in the description. |
| Scope | One purpose. "While I was there" fixes go to a separate PR. |
| Vertical slice | A feature PR should deliver a working slice or a clearly delimited step towards one. |

### 4.3 Merge requirements (enforced by branch protection on `main`)

- [ ] At least **1 approving review** from a member other than the author.
- [ ] CI is green (build, tests, formatting, architecture rules, coverage gate).
- [ ] Branch is up to date with `main`.
- [ ] All review conversations resolved.
- [ ] No direct pushes to `main`, including by administrators; force-push and deletion disabled.

### 4.4 Merge method

**Squash and merge**, with the PR title as the final commit message and the PR body as the description. Rationale: one reviewed PR = one atomic, revertible commit on `main`, and a readable history. The author of the PR remains the commit author, so individual contribution stays visible (`project.md` §4.1). The topic branch is deleted automatically.

Exception: merge-commit (non-squash) may be used for a long branch whose individual commits are deliberately curated; the reviewer says so in the PR.

Who merges: **the author**, once the requirements above are met. Reviewers approve; they do not merge other people's work.

---

## 5. Code Review

### 5.1 Principles

- Review the **code and the reasoning**, never the person.
- The goal is shared understanding: after approving, the reviewer must be able to explain the change at a defence (`project.md` §22).
- A review comment is a question or a suggestion; the author decides unless it points to a defect, a broken rule or a missing test.

### 5.2 Assigning reviewers

| Rule | Detail |
|---|---|
| Reviewer | One member other than the author, chosen **round-robin** so that knowledge circulates; the author requests the review explicitly. |
| Cross-component changes | Add a second reviewer who owns the other component. |
| Architecture-affecting changes (new dependency arrow, new component, ADR) | **Two approvals**, including one from a member who did not take part in writing it. |
| Avoid | The same pair always reviewing each other. |

### 5.3 Service levels

| Event | Target |
|---|---|
| First review response | within **48 hours** (working days) |
| Author reply to comments | within 48 hours |
| Stale PR (no activity 5 days) | Raised at the next team sync; owner decides to finish, split or close it. |

Delivery weeks may shorten these targets; agree on it in the team channel.

### 5.4 Review checklist

**Correctness and rules**
- Does the change implement the requirement/business rule it claims, including the edge cases (empty queue, duplicate patient, limit exactly reached, level change)?
- Is the business logic in `domain`, free of framework code?

**Tests**
- Are there tests for normal cases, edge cases and error cases — not only for coverage? Would a test fail if the rule were broken?
- Are tests deterministic (injected `Clock`, no sleeps, no order dependence)?

**Design**
- Does it respect layer and component dependency rules? Does it match the design document, or update it?
- Naming, size of methods/classes, duplication (see [03 Coding Conventions](03_coding_conventions.md)).

**Hygiene**
- No leftover debug code, commented-out code, unused imports, TODOs without an issue number.
- Documentation, seed data and migrations updated when needed.

### 5.5 Approving

Approve when you would be comfortable defending the change yourself. "LGTM" without having read the diff is not an approval. If you could not review properly (time, unfamiliarity), say so and ask someone else.

---

## 6. Continuous Integration

A GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every pull request and on every push to `main`:

1. Check out and set up the Java and Node environments with dependency caching.
2. Run the **same commands documented in the README** for the back end and the front end, which together include:
   - compilation / build,
   - unit, integration and architecture tests,
   - formatting and lint checks,
   - coverage gate: ≥ **80 %** line coverage on back-end `domain` classes.
3. Upload the test and coverage reports as build artifacts.

The concrete tools are chosen at bootstrap.

The CI job is a **required status check**. A red `main` is fixed immediately by whoever broke it — revert first, investigate afterwards.

---

## 7. AI-Assisted Work

AI assistants are allowed (`project.md` §23). To keep that compatible with individual defences:

1. The **author is fully responsible** for AI-generated code, tests and documents; the review standards are identical.
2. The author must be able to explain every line of the change. A reviewer may ask them to do so.
3. Record tools and purposes in the README's AI-use section; update it when usage changes.
4. If an AI tool contributed substantially to a commit, add a trailer: `Co-authored-by: <tool> <noreply@example.com>`; otherwise no trailer is required.
5. Never paste real patient data, credentials or other sensitive data into an AI tool.
