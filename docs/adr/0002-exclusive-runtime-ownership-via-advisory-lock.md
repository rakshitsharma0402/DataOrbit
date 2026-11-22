# ADR-0002: Exclusive runtime ownership via PostgreSQL advisory lock

- Status: Accepted
- Date: 2026-11-23
- Handbook references: "Single active backend ownership", "Failure recovery and reconciliation", test row "Two backend processes use the same data root".

## Context

V1 assumes exactly one active backend per database and storage root. The handbook requires that startup establishes exclusive ownership before accepting uploads or launching destructive maintenance, and that a stale owner can never commit a claim after replacement. It also forbids inferring that an owner is dead from elapsed time.

## Decision

- At startup, after migrations and before accepting work, acquire a session-level PostgreSQL advisory lock with `pg_try_advisory_lock(<fixed bigint key>)`.
- Hold the lock on **one dedicated JDBC connection that is not part of the connection pool** and stays open for the lifetime of the process.
- If the lock is not obtained, startup fails with a clear message (the "second start rejected" test).
- Generate the application-run ID (a random UUID) only **after** the lock is held. Every upload attempt claim, job claim and maintenance claim records it as its owner.
- Because the lock proves no other live backend exists, any claim owned by a different run ID is by definition left by a dead run. Reconciliation then **inspects** its evidence; it never blindly reclaims or deletes (the handbook's evidence-first rules still apply in full).
- A lightweight liveness check on the lock connection runs periodically. If the connection is lost, the application stops accepting work and shuts down, because the lock may have been released and another instance may take over. This checks our own lock, not other processes, so it is not a time-based guess about someone else's death.

## Alternatives considered

- **Lock file in the storage root:** protects the filesystem but not the database; fails if the two are mismatched.
- **Lease row with heartbeat and expiry:** reintroduces timeouts and "assume dead after N seconds", which the design explicitly avoids.

## Consequences

- No extra table (the five approved tables stay as they are).
- Connection poolers that multiplex sessions (for example PgBouncer in transaction mode) would break the lock; not relevant for the V1 localhost deployment.
- Tests need a way to start a second instance (or simulate one with a second connection) against the same database.

## Verification

Second-start rejection test; stale-owner fencing (UPL-02); restart reconciliation drills ("Old upload owner resumes", "Unclear recovery evidence").
