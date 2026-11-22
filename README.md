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

Build, run and test instructions will be added in Milestone 1, once there is something to run.

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
