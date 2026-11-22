<!-- DataOrbit V1 Architecture & Engineering Handbook · Part V · Implementation Roadmap · PDF page 62. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Seven milestones to a working system

*A sequence chosen to yield tested, demonstrable progress rather than an empty directory tree.*

> **FIGURE 14. The seven approved implementation milestones arranged from foundation to hardening.**  
> *(Diagram: see page 62 of the PDF.)*

## Project foundation

Install and verify Java JDK, Maven, Node/npm, PostgreSQL and Git. Create the parent Maven project and exactly nine backend modules, then establish the Spring Boot entry point, configuration loading and the Flyway baseline. Create a minimal React + TypeScript application. Both processes should start locally, the backend should connect to PostgreSQL, and a simple API health or version request should succeed.

- **Learning focus:** JDK versus JVM, Maven parent versus child module, dependency injection, HTTP requests and database connection strings.
- **Acceptance evidence:** clean repository build, local startup from documented commands, Flyway migration history and basic frontend/backend communication.

## Storage foundation

Implement provider-independent `StorageReference` and Storage API contracts, then the local adapter. Begin with safe staging and closeable reading, add SHA-256 and exact byte enforcement, and then verified tier transfer, quarantine, inspection and authorized cleanup. Use temporary directories and failure-injection tests before any production workflow depends on the adapter.

- **Learning focus:** Java InputStream, try-with-resources, checked I/O exceptions, cryptographic hash digests and file safety.
- **Acceptance evidence:** exact byte round trips, checksum mismatch handling, source preservation and no path escape.

## CSV validation and classification

Build the isolated parser comparison prototype with uniVocity and FastCSV 4.x using shared JUnit 5 fixtures. Select an adapter only after strict conformance and bounded resource behavior are demonstrated. Implement the immutable ValidationResult, issue codes and policy injection. Implement the pure classifier and exact tier boundary tests.

- **Learning focus:** UTF-8 decoding, tokenization, Unicode normalization, Java records, enums and constructor invariants.
- **Acceptance evidence:** the conformance suite passes, candidate memory behavior is measured, and all classification boundaries pass.

## Ingestion and PostgreSQL persistence

Implement datasets, upload claims, staged metadata, application-run fencing, and conditional repository commands against migrated PostgreSQL. Expose Initialize and content Upload HTTP endpoints. Use real stream tests and conflict tests to establish that a dataset cannot be staged twice simultaneously and that success is returned only after durable metadata commit.

- **Learning focus:** SQL transactions, Spring database integration, unique constraints and streaming controller input.
- **Acceptance evidence:** API-driven full upload with correct DB rows, SHA-256 metadata and deterministic 409/413 behavior.

## Workflow, Status and recovery

Implement Execute claims and the seven job states; invoke Validation, Classification and Storage in order. Add the invalid-file quarantine branch, atomic final database commands, append-only events, durable maintenance and startup reconciliation. Exercise every crash window and preserve previous job histories during controlled manual retry.

- **Learning focus:** service orchestration, transactional units of work, conditional updates, retries versus reconciliation.
- **Acceptance evidence:** valid and invalid end-to-end backend paths, injected crash scenarios and durable status history.

## React frontend

Build the upload panel, typed API client, fixed clickable React Flow diagram, Status hook, dataset browser, history views and downloads. Show upload transfer progress from browser events while processing status comes from PostgreSQL. Test lost Execute responses and cleanup of polling subscriptions.

- **Learning focus:** React components and hooks, TypeScript DTOs, asynchronous HTTP, accessible UI state.
- **Acceptance evidence:** a user can upload, execute, inspect and download a file without direct database or filesystem commands.

## Integration testing and hardening

Run the complete conformance and failure-injection suites on real PostgreSQL and local storage. Check a maximum-size upload under realistic resource constraints, restore from a coordinated backup, verify no path traversal, review log hygiene and produce a reproducible local setup guide. Resolve critical safety findings before declaring V1 release-ready.

- **Learning focus:** integration environments, repeatable test fixtures, performance measurement, test evidence and operational runbooks.
- **Acceptance evidence:** a written test report, reproducible startup and restore procedure, and a clearly recorded list of any known noncritical limitations.

## A realistic order of work

Completing a milestone means showing its deliverable and tests, not merely writing classes with TODO methods. Implementation should proceed in small, explained slices so a learner can compile the project, run a focused test and observe one new behavior at a time. The first action after this book is not another architecture meeting: it is development-environment verification and the Maven project foundation.
