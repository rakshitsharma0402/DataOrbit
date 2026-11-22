# ADR-0001: Persistence with Spring `JdbcClient` and Flyway

- Status: Accepted
- Date: 2026-11-23
- Handbook references: "PostgreSQL as the lifecycle ledger", "Service contract atlas" (conditional claim in SQL); the handbook defers the JPA/JDBC choice to coding.

## Context

DataOrbit's correctness depends on conditional SQL: claims such as `UPDATE ... WHERE state = 'INITIALIZED' AND active_upload_attempt_id IS NULL`, fenced transitions that assert the expected job and owner, a partial unique index (`one_active_job_per_dataset`), and short explicit transaction boundaries. A claim is successful only if **exactly one row** changed. The handbook also forbids Hibernate from generating or altering production tables; Flyway owns the schema.

## Decision

- Use Spring's `JdbcClient` (plain SQL, explicit row mapping) for all persistence in `status`, `configuration` and the concrete adapters assembled in `dataorbit-app`.
- Use Flyway versioned SQL migrations for every schema change. Migrations are never edited after merge.
- Wrap "must change exactly one row" in one small helper, so a zero-row or multi-row update is always treated as a conflict or defect, never as success.
- Use `TIMESTAMPTZ` columns and `java.time.Instant` throughout.
- Do not use JPA/Hibernate or jOOQ in V1.

## Alternatives considered

- **JPA/Hibernate:** convenient for plain CRUD, but its change tracking and optimistic-locking model hide the exact conditional updates this design relies on, and it makes transaction boundaries harder to reason about.
- **jOOQ:** type-safe SQL and a good fit technically, but adds code-generation build setup that is not needed at this stage.

## Consequences

- More hand-written SQL and row mappers; every query is visible and testable.
- Repository integration tests run against real PostgreSQL (Testcontainers or a CI service container), not an in-memory substitute.
- Persistence stays behind the contracts owned by `status` and `configuration`; no other module touches SQL.

## Verification

Integration tests for claim conflicts (UPL-01, JOB-01), atomic final commit rollback (DB-01, DB-02) and migration application from empty (Milestone 1 acceptance evidence).
