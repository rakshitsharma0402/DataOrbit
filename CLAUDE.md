# CLAUDE.md — working agreement for DataOrbit

This file is read at the start of every Claude Code session in this repository. Keep it accurate; update it when commands or conventions change.

## What this project is

DataOrbit V1: a local, single-user CSV ingestion, validation and tiered-storage system (Spring Boot modular monolith, PostgreSQL, local filesystem, React). It is a learning-oriented project built **milestone by milestone** in small, tested slices. Briefly explain new concepts as they appear (what a Maven module is, why a transaction boundary sits where it does) without turning replies into lectures.

## Source of truth, in order

1. `docs/book/`: the approved Architecture & Engineering Handbook. Approved invariants are not up for casual change.
   - Read the Markdown chapters in `docs/book/chapters/`, not the PDF. `docs/book/README.md` has a "which chapter for which work" table: before starting work on a component, read its chapter there.
   - Open `docs/book/DataOrbit-V1-Handbook.pdf` only to look at a diagram (each figure in the Markdown gives its PDF page) or to settle a doubt. The PDF is authoritative if the two disagree.
   - Do not load the whole handbook at once, and do not `@`-import it into this file.
2. `docs/adr/` — implementation-time decisions (JdbcClient + Flyway, advisory-lock ownership, attempt ID in object keys, parser deferred, versioning, Java 25 baseline).
3. `docs/engineering/GitHub-Engineering-Standard-v1.0.md` — workflow rules.
4. `docs/planning/` — milestones and backlog.

If code, handbook and ADRs disagree, stop and flag it. Do not silently pick one. A change that alters an approved invariant gets the `architecture change` label and review before implementation.

## Commands

Toolchain and local PostgreSQL setup: `docs/setup.md`. Keep this section current as Milestone 1 adds run commands (backend, frontend).

- Backend build and tests (all nine modules, including the dependency-direction check): `mvn -f backend/pom.xml verify`
- Show the module dependency graph: `mvn -f backend/pom.xml dependency:tree`
- Database: local PostgreSQL, `dataorbit` (development) and `dataorbit_test` (tests), both owned by role `dataorbit`. The backend needs `DATAORBIT_DB_PASSWORD` set or startup fails. Tests that need an empty database use `TemporarySchema` (a throwaway schema, dropped afterwards); never point a test at the development database.
- Migrations: `backend/dataorbit-app/src/main/resources/db/migration/V<n>__<description>.sql`. Never edit a merged migration; add a new version. JPA/Hibernate and jOOQ are banned by the build (ADR-0001).
- Run the backend (after `mvn -f backend/pom.xml package`): `java -jar backend/dataorbit-app/target/dataorbit-app-0.1.0-SNAPSHOT.jar`. It listens on `http://127.0.0.1:8080`; check it with `curl http://127.0.0.1:8080/api/v1/version`.
- Startup settings: `backend/dataorbit-app/src/main/resources/application.yml`. `server.address` must be a literal loopback IP and `dataorbit.frontend-origin` a loopback http(s) origin, or startup fails. Override with environment variables or `--name=value` arguments, never by committing real credentials.
- Frontend (`frontend/`, Vite + React + TypeScript 6, npm with exact versions and a lockfile): install `npm --prefix frontend ci`; dev server `npm --prefix frontend run dev` (always `http://127.0.0.1:5173`, the backend's allowed origin); checks `npm --prefix frontend run typecheck`, `run lint`, `test`, `run build`. TypeScript stays on 6.0.x until typescript-eslint supports 7.
- CI (`.github/workflows/ci.yml`) runs the same checks on every pull request and push to `main`: job `backend` (JDK 25, PostgreSQL 18.6 service with `dataorbit_test`, `mvn verify`) and job `frontend` (Node 24: `npm ci`, typecheck, lint, test, build). Both are required on `main`. Actions are pinned to full commit SHAs; keep the version comment when updating.
- The API allows exactly one browser origin, `dataorbit.frontend-origin` (CORS for `/api/**`, methods GET, POST, PUT). Other origins get 403.
- In tests that start the application with custom settings, pass them as command-line arguments (`SpringApplicationBuilder.run("--name=value")`). `SpringApplicationBuilder.properties(...)` only sets defaults, which `application.yml` overrides.

Each module's allowed DataOrbit dependencies are an allow-list in its `pom.xml` (`enforce-module-direction`, Maven Enforcer `bannedDependencies`). Change a module's dependencies only together with that allow-list, and only as chapter 05 permits.

## Architecture invariants (do not violate)

- Nine Maven modules with an **acyclic** dependency graph. `dataorbit-app` only assembles; no business logic there. Classification receives its policy by injection; it never reads configuration.
- Business contracts use `StorageReference(provider, objectKey)`, never an absolute `Path`. Only `storage-local` turns a key into a filesystem path. No traversal, no escaping symlinks. The user's filename is never part of an object key.
- Never hold a database transaction across file I/O: short claim transaction, then external work, then short final commit. Dataset update, job completion, event and cleanup task commit **atomically**.
- Order for retention: stream to temp, verify size and SHA-256, finalize, commit to PostgreSQL, **then** clean up staging. Never delete the source on uncertain evidence.
- Every claim is conditional and fenced (dataset, state, attempt, run owner) and must verify that exactly one row changed.
- Never infer safety from elapsed time or file age. Ambiguous evidence means `RECOVERY_REQUIRED` or a `BLOCKED` task, with bytes preserved.
- Invalid CSV content is not an operational failure: a quarantined invalid file completes its job. Upload state, job state and dataset disposition are separate dimensions; never collapse them.
- Stream everything: no `readAllBytes()` on uploads, close every stream (try-with-resources), enforce limits during tokenization, not after.
- The UI shows real upload progress only for browser transfer. Never invent processing percentages. A lost response is uncertainty: check Status before assuming failure.
- Classification boundaries use exact decimal bytes (1 MB = 1,000,000). The record limit is 4,194,304 bytes (binary). Name constants clearly so these are never confused.
- Do not log raw CSV content, secrets or full local paths in user-visible text. Public errors use stable codes, no stack traces.

## Workflow

- `main` only; branches are `<type>/<short-description>` (see `CONTRIBUTING.md`). Do not create `develop`.
- Conventional Commits `type(scope): description`; one logical change per commit.
- Pull requests use **merge commits**. Never squash. Never delete merged branches.
- Titles for issues: `[FEATURE]`, `[BUG]`, `[TASK]`, `[RESEARCH]`, `[DOCS]`. No priority labels or fields.
- Work one backlog slice at a time. A milestone is complete only when its acceptance evidence (see `docs/planning/milestones.md`) is demonstrated, not when classes exist.
- Add tests with the code. Use the handbook's test IDs in test names where they apply. Destructive or crash-simulating tests use temporary roots only.
- Update `CHANGELOG.md` for notable changes; update this file and the README when commands or layout change.

## Do not

- Commit secrets, credentials or real connection strings.
- Edit the handbook (PDF or chapters) except to fix a Markdown conversion error against the PDF.
- Add dependencies, frameworks or modules the handbook and ADRs do not call for without raising it first.
- Present illustrative handbook snippets (SQL, DTOs, records) as final; they are shapes to implement and test.
- Claim something works until a test or a run demonstrates it.
