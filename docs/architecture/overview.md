# DataOrbit V1 architecture overview

## Status and vision

DataOrbit is a planned modular platform for data ingestion, validation,
classification, and intelligent storage management. V1 targets a single user on
localhost, managing analytical CSV datasets at the file level, without authentication.
The application and standalone parser experiment have not been implemented.

Product requirements, HLD, and LLD decisions through #61 are approved. Core backend
module boundaries, Storage API contracts, CSV validation rules, and parser safety
requirements have been designed. Migration of existing decisions into
`docs/decisions/` is deferred; the decisions themselves are not awaiting approval.
This overview summarizes the approved plan rather than replacing detailed decisions.

## Planned components

- **Frontend:** React and TypeScript, with React Flow displaying actual backend
  processing states. It will communicate through REST/JSON APIs and poll processing
  status approximately every 1–2 seconds in V1.
- **Backend:** one runnable Spring Boot application, organized as a Java/Maven
  multi-module build in `dataorbit-backend/`, not independently deployed microservices.
- **Metadata:** PostgreSQL will hold dataset metadata, processing jobs and events,
  classification policies, and quarantine lifecycle state.
- **File bytes:** local filesystem storage behind a Storage Abstraction Layer (SAL).
  HOT, WARM, and COLD are logical tiers on the same filesystem.

The backend will contain exactly these nine approved Maven modules:

1. `dataorbit-app`
2. `workflow`
3. `ingestion`
4. `validation`
5. `classification`
6. `storage-api`
7. `storage-local`
8. `status`
9. `configuration`

These modules and the React/TypeScript app in `dataorbit-frontend/` are planned;
no application scaffolding is present yet.

## Planned processing and storage

Uploads will follow **Initialize → Upload to staging → Execute processing**, with
streaming file handling and a configurable maximum of 250 MB. Structural validation
will cover strict UTF-8, headers, column consistency, CSV quoting, and multiline
fields without loading complete datasets into Java heap memory.

Successfully uploaded but invalid CSV files will be quarantined for seven days.
Valid files will be classified by configurable size policies, stored through the
SAL, and made available to browse, inspect, and download. Storage operations will
verify SHA-256 integrity; recovery and reconciliation will handle interruptions.

The proposed thresholds are HOT ≤ 10 MB, WARM > 10 MB and ≤ 100 MB, and COLD > 100 MB
and ≤ 250 MB. The precise MB unit convention is unresolved; these values are not MiB.

Runtime storage will use ignored `dataorbit-data/` directories: `staging/`, `hot/`,
`warm/`, `cold/`, and `quarantine/`. Stored files will use full dataset UUIDs, for
example `dataorbit-data/hot/<dataset-uuid>/<safe-original-filename>`.

User-initiated dataset deletion, duplicate detection, independent Python ETL workers,
and cloud storage integrations are outside V1 scope. More formats, connectors,
independent ETL workers, and smarter classification policies remain long-term goals.

## Standalone parser experiment

The approved experiment is planned for `experiments/csv-parser-conformance/`, using
Java, Maven, and JUnit 5. It will compare uniVocity-parsers with FastCSV 4.x for
strict CSV conformance and resource safety. No prototype exists yet, and the final
parser library has not been selected.

See the [project README](../../README.md) for the planned monorepo structure and
[contribution guide](../../CONTRIBUTING.md) for workflow and fixture requirements.
