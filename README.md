# DataOrbit

## Project overview

DataOrbit is a planned modular, extensible platform for data ingestion, validation,
classification, and intelligent storage management. V1 will be a single-user,
localhost-based web application for managing analytical CSV datasets at the file level.

**Development status:** Requirements and high-level design (HLD) are approved.
No production application or CSV parser conformance prototype has been implemented yet.
All capabilities and architecture described below are planned.

## Vision

Provide a transparent workflow that receives datasets, validates their structure,
classifies them using configurable policies, and places them into appropriate storage
tiers. The long-term vision includes more file formats, data connectors, independent
ETL workers, and increasingly intelligent classification policies.

## Planned V1 features

- Manual CSV uploads with a configurable maximum of 250 MB, using three phases:
  **Initialize → Upload to staging → Execute processing**.
- Streaming file handling without loading complete datasets into Java heap memory.
- Structural CSV validation covering strict UTF-8, headers, column consistency,
  correct quoting, and multiline fields.
- Automatic quarantine of successfully uploaded but invalid CSV files, with seven-day retention.
- Configurable file-size-based classification into HOT, WARM, and COLD storage tiers.
- PostgreSQL-backed dataset metadata, processing jobs, processing events,
  classification policies, and quarantine lifecycle information.
- Local filesystem storage for file bytes through a Storage Abstraction Layer (SAL).
- A visual workflow showing actual backend processing states.
- Browsing, inspection, and download of successfully processed datasets.
- SHA-256 integrity verification during storage operations.
- Recovery and reconciliation for interrupted processing.

V1 has no authentication because it targets single-user localhost operation.
User-initiated dataset deletion, duplicate detection, independent Python ETL workers,
and cloud storage integrations are outside V1 scope.

## Technology stack

| Area | Approved plan |
| --- | --- |
| Frontend | React, TypeScript, and React Flow for the visual workflow |
| Communication | REST/JSON APIs; frontend status polling approximately every 1–2 seconds in V1 |
| Backend | Java, Spring Boot, and a Maven multi-module project |
| Metadata and lifecycle state | PostgreSQL |
| File storage | Local filesystem behind a Storage Abstraction Layer |
| Experiment tooling | Java, Maven, and JUnit 5 |

The first standalone experiment will compare **uniVocity-parsers** and **FastCSV 4.x**
for strict CSV conformance and resource safety. The final parser library has not been selected.

## High-level architecture

The frontend will communicate with one runnable Spring Boot application through REST APIs.
The backend will coordinate ingestion, validation, classification, storage, and status
reporting. PostgreSQL will hold metadata and lifecycle state; actual file bytes will
reside on the local filesystem through the SAL.

The approved backend Maven modules are:

- `dataorbit-app`
- `workflow`
- `ingestion`
- `validation`
- `classification`
- `storage-api`
- `storage-local`
- `status`
- `configuration`

These are planned module boundaries, not existing directories or independently deployed
microservices.

## Planned repository structure

DataOrbit will use one GitHub monorepo. The following is the planned organization;
application and experiment directories have not been scaffolded yet.

```text
DataOrbit/
├── .github/
├── docs/
│   ├── architecture/
│   └── decisions/
├── experiments/
│   └── csv-parser-conformance/
├── dataorbit-backend/
├── dataorbit-frontend/
├── .gitignore
├── .editorconfig
├── README.md
└── CONTRIBUTING.md
```

## Storage-tier model

HOT, WARM, and COLD will be logical tiers simulated by separate directories on the
same local filesystem. They are not separate cloud services or physical storage devices.

| Tier | Proposed V1 file-size policy |
| --- | --- |
| HOT | File size ≤ 10 MB |
| WARM | File size > 10 MB and ≤ 100 MB |
| COLD | File size > 100 MB and ≤ 250 MB |

Classification will be configurable. The precise MB unit convention remains to be
finalized; these thresholds have not been converted to MiB.

The approved runtime directory layout is:

```text
dataorbit-data/
├── staging/
├── hot/
├── warm/
├── cold/
└── quarantine/
```

Files will use full dataset UUIDs, for example:
`dataorbit-data/hot/<dataset-uuid>/<safe-original-filename>`.

The entire `dataorbit-data/` directory is excluded from Git. Actual uploaded datasets,
secrets, database contents, and temporary processing files must never be committed.
Small synthetic CSV fixtures for automated tests remain trackable in test resource
and fixture directories.

## Current development status

- Product requirements and HLD have been approved.
- Core backend module boundaries and Storage API contracts have been designed.
- CSV validation rules and parser safety requirements have been designed.
- The standalone CSV parser conformance experiment has been approved.
- No production application or parser conformance prototype has been implemented yet.
- GitHub repository foundation setup is currently in progress.

Installation, startup, database migration, and test commands will be documented when
runnable application or experiment code exists.

## Roadmap

1. Complete and review the initial repository foundation.
2. Implement the approved standalone CSV parser conformance experiment and use its
   results to select a parser.
3. Build the approved V1 backend modules and frontend workflow.
4. Extend the platform over time toward additional formats, connectors, independent
   ETL workers, and more intelligent classification policies.

## Documentation and contributing

- [Architecture overview](docs/architecture/overview.md)
- [Contributing guide](CONTRIBUTING.md)
