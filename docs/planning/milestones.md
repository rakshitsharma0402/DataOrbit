# Milestones

The seven approved implementation milestones, in order. A milestone closes only when its **acceptance evidence** is demonstrated by tests or a documented run. Source: handbook chapter "Seven milestones to a working system". Release mapping: [ADR-0005](../adr/0005-versioning-and-release-mapping.md).

## 1. Project Foundation

Verify the toolchain (JDK, Maven, Node/npm, PostgreSQL, Git). Create the parent Maven project with exactly nine modules, the Spring Boot entry point, configuration loading and the Flyway baseline. Create a minimal React + TypeScript app.
**Learning focus:** JDK vs JVM, Maven parent vs child module, dependency injection, HTTP requests, database connection strings.
**Acceptance evidence:** clean repository build; local startup from documented commands; Flyway migration history; basic frontend/backend communication.

## 2. Storage Foundation

Provider-independent `StorageReference` and Storage API contracts, then the local adapter: safe staging and closeable reading, SHA-256 and exact byte enforcement, verified tier transfer, quarantine, inspection and authorized cleanup. Failure-injection tests before any workflow depends on it.
**Learning focus:** `InputStream`, try-with-resources, checked I/O exceptions, digests, file safety.
**Acceptance evidence:** exact byte round trips; checksum-mismatch handling; source preservation; no path escape.

## 3. CSV Validation & Classification

Parser comparison experiment (uniVocity vs FastCSV 4.x) with shared fixtures; select an adapter only after strict conformance and bounded resource behavior are shown. Immutable `ValidationResult`, issue codes and policy injection. Pure classifier with exact boundary tests. See [ADR-0004](../adr/0004-csv-parser-selection-deferred-to-milestone-3.md).
**Learning focus:** UTF-8 decoding, tokenization, Unicode normalization, records, enums, constructor invariants.
**Acceptance evidence:** conformance suite passes; candidate memory behavior measured; all classification boundaries pass.

## 4. Ingestion & Persistence

Schema (five tables, one-active-job index), startup exclusive ownership ([ADR-0002](../adr/0002-exclusive-runtime-ownership-via-advisory-lock.md)), datasets, upload claims, staged metadata and application-run fencing, conditional repository commands. Initialize and Upload endpoints.
**Learning focus:** SQL transactions, Spring database integration, unique constraints, streaming controller input.
**Acceptance evidence:** API-driven full upload with correct database rows, SHA-256 metadata and deterministic 409/413 behavior.

## 5. Workflow, Status & Recovery

Execute claims and the seven job states; invoke Validation, Classification and Storage in order. Invalid-file quarantine branch, atomic final commands, append-only events, durable maintenance, startup reconciliation, controlled manual retry. Exercise every crash window.
**Learning focus:** service orchestration, transactional units of work, conditional updates, retries vs reconciliation.
**Acceptance evidence:** valid and invalid end-to-end backend paths; injected crash scenarios; durable status history.

## 6. React Frontend

Upload panel, typed API client, fixed clickable React Flow diagram, Status polling hook, dataset browser, history views and downloads. Browser-measured upload progress; processing status from PostgreSQL. Tests for lost Execute responses and polling cleanup.
**Learning focus:** components and hooks, TypeScript DTOs, asynchronous HTTP, accessible UI state.
**Acceptance evidence:** a user can upload, execute, inspect and download a file without direct database or filesystem commands.
**Release:** `v0.1.0`.

## 7. Integration Testing & V1 Hardening

Complete conformance and failure-injection suites on real PostgreSQL and local storage; maximum-size upload under realistic constraints; coordinated backup and restore rehearsal; path-traversal and log-hygiene review; reproducible setup guide. Resolve critical safety findings.
**Learning focus:** integration environments, repeatable fixtures, performance measurement, test evidence, runbooks.
**Acceptance evidence:** a written test report; reproducible startup and restore procedure; a recorded list of known non-critical limitations.
**Releases:** `v0.9.0` (release candidate), then `v1.0.0`.
