# DataOrbit

An intelligent CSV data-management and tiered-storage system. DataOrbit V1 accepts analytical CSV files in a browser, validates them strictly, classifies valid datasets into simulated **HOT / WARM / COLD** storage tiers by exact byte size, quarantines invalid files for seven days, and keeps an auditable history of everything that happened.

> **Status:** the V1 architecture is approved; implementation has not started. Nothing in this README is described as working until its milestone is complete and tested.

## What V1 delivers

- **Three-phase upload:** initialize a dataset (UUID), stream the bytes (size and SHA-256 recorded), then execute processing deliberately.
- **Strict CSV validation:** UTF-8, comma-separated, strict quoting, ten stable diagnostic codes, bounded memory.
- **Deterministic placement:** HOT up to 10,000,000 bytes, WARM up to 100,000,000, COLD above; the policy used is recorded with every decision.
- **Safe retention:** copy, verify checksum, commit to PostgreSQL, and only then clean up staging.
- **Recovery first:** interrupted uploads and jobs are reconciled from evidence, never guessed.
- **Browser UI:** real upload progress, a fixed processing diagram, dataset inventory, history and download.

Out of scope for V1: authentication, cloud storage, background workers, ML-based placement, resumable uploads, user-initiated deletion. See the handbook for the full list.

## Architecture at a glance

One Spring Boot backend (a modular monolith of nine Maven modules), one React app, PostgreSQL for lifecycle state, and the local filesystem for file bytes.

| Module | Responsibility |
|---|---|
| `dataorbit-app` | Entry point, REST adapters, wiring, concrete database adapters |
| `workflow` | Processing sequence, jobs, retry and recovery decisions |
| `ingestion` | Initialize and upload, attempt claims, verified STAGED metadata |
| `validation` | Strict streaming CSV interpretation and issue reporting |
| `classification` | Pure size-to-tier decision |
| `storage-api` | Storage contracts (`StorageReference`, `StorageTier`, `StorageService`) |
| `storage-local` | Safe local-disk adapter with checksum-verified copies |
| `status` | Operational persistence contracts, jobs, events, tasks, read-only status |
| `configuration` | Validated startup settings and immutable policy revisions |

**Stack:** Java 25 ([ADR-0006](docs/adr/0006-java-25-baseline.md)), Spring Boot, Maven, PostgreSQL, Flyway, React, TypeScript, React Flow.

## Repository layout

```text
DataOrbit/
├── backend/          Maven parent project and the nine modules
├── frontend/         React + TypeScript application
├── experiments/      Throwaway investigations (e.g. CSV parser conformance)
├── docs/
│   ├── book/         Architecture & Engineering Handbook (PDF + Markdown chapters)
│   ├── adr/          Architecture decision records
│   ├── planning/     Milestones, issue backlog, GitHub setup
│   └── engineering/  GitHub Engineering Standard
├── scripts/          Repository automation
└── .github/          Issue and pull request templates, workflows
```

## Roadmap

| # | Milestone | Releases |
|---|---|---|
| 1 | Project Foundation | |
| 2 | Storage Foundation | |
| 3 | CSV Validation & Classification | |
| 4 | Ingestion & Persistence | |
| 5 | Workflow, Status & Recovery | |
| 6 | React Frontend | `v0.1.0` |
| 7 | Integration Testing & V1 Hardening | `v0.9.0` (release candidate), `v1.0.0` |

Details and acceptance evidence: [`docs/planning/milestones.md`](docs/planning/milestones.md).

## Getting started

DataOrbit runs entirely on your own machine: a Spring Boot backend on `127.0.0.1:8080`, a React frontend on `127.0.0.1:5173` and a local PostgreSQL database. Nothing listens on your network.

### 1. Prepare your machine (once)

Follow [`docs/setup.md`](docs/setup.md):

- Install JDK 25, Maven 3.9+, Node.js 22.12+ (24 recommended), PostgreSQL and Git.
- Create the `dataorbit` role and the `dataorbit` and `dataorbit_test` databases.
- Export the database password in your shell (never commit it):

```bash
export DATAORBIT_DB_PASSWORD='<your password>'
```

### 2. Build and test the backend

From the repository root:

```bash
mvn -f backend/pom.xml verify
```

This builds all nine modules, checks the module dependency rules and runs the backend tests against `dataorbit_test`. It produces the executable jar.

### 3. Start the backend

```bash
java -jar backend/dataorbit-app/target/dataorbit-app-0.1.0-SNAPSHOT.jar
```

On startup the backend checks its settings, then Flyway applies any pending database migrations. If a setting is missing or invalid (for example `DATAORBIT_DB_PASSWORD`), it stops with a short "APPLICATION FAILED TO START" report naming the setting. Check that it is up:

```bash
curl http://127.0.0.1:8080/api/v1/version
```

It answers `{"name":"DataOrbit","version":"0.1.0-SNAPSHOT"}`. To see the applied migrations:

```bash
psql -h 127.0.0.1 -U dataorbit -d dataorbit -c 'select version, description, success from flyway_schema_history;'
```

### 4. Start the frontend

In a second terminal, install the exact dependency versions from the lockfile, then start the dev server:

```bash
npm --prefix frontend ci
```

```bash
npm --prefix frontend run dev
```

Open `http://127.0.0.1:5173`. The page shows "Connected to DataOrbit version 0.1.0-SNAPSHOT". If the backend is not running, it says it cannot reach the backend and offers **Try again**.

### 5. Check the frontend

These are the same checks CI runs:

```bash
npm --prefix frontend run typecheck
```

```bash
npm --prefix frontend run lint
```

```bash
npm --prefix frontend test
```

```bash
npm --prefix frontend run build
```

### Settings

Defaults live in `backend/dataorbit-app/src/main/resources/application.yml`. Override any of them with an environment variable or a command-line argument, for example `--server.port=9090`.

| Setting | Default | Rule |
|---|---|---|
| `server.address` | `127.0.0.1` | Required; must be a literal loopback IP |
| `server.port` | `8080` | |
| `spring.datasource.url` | `jdbc:postgresql://127.0.0.1:5432/dataorbit` | |
| `spring.datasource.password` | from `DATAORBIT_DB_PASSWORD` | Required; no default |
| `dataorbit.frontend-origin` | `http://127.0.0.1:5173` | Required; the only browser origin the API accepts |

### Continuous integration

Every pull request and every push to `main` runs the `backend` and `frontend` jobs in [`.github/workflows/ci.yml`](.github/workflows/ci.yml). Both must pass before a pull request can merge into `main`.

## Documentation

- [Engineering Handbook](docs/book/README.md): the approved design baseline, as a [PDF](docs/book/DataOrbit-V1-Handbook.pdf) and as [Markdown chapters](docs/book/chapters/)
- [Architecture decision records](docs/adr/README.md)
- [Milestones](docs/planning/milestones.md) and [initial backlog](docs/planning/initial-issues.md)
- [GitHub Engineering Standard](docs/engineering/GitHub-Engineering-Standard-v1.0.md) and [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`CHANGELOG.md`](CHANGELOG.md)

## Versioning

Semantic Versioning with `v`-prefixed tags. See [ADR-0005](docs/adr/0005-versioning-and-release-mapping.md). The software version, the handbook revision and the implementation milestone are tracked separately.

## License

Released under the [MIT License](LICENSE).
