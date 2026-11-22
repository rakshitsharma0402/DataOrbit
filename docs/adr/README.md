# Architecture Decision Records

ADRs record decisions made while implementing DataOrbit. They are numbered, short, and never rewritten after acceptance: a later decision supersedes an earlier one by adding a new ADR and marking the old one `Superseded by ADR-NNNN`.

**Scope.** The engineering handbook (`docs/book/`) is the approved architecture baseline. ADRs cover choices the handbook explicitly leaves to implementation time (persistence technology, ownership mechanism, attribution mechanism, parser selection, version mapping). An ADR must not change an approved invariant. If a change would alter one, label the work `architecture change` and review it before implementing, as the GitHub Engineering Standard requires.

| ADR | Title | Status |
|---|---|---|
| [0001](0001-persistence-with-jdbcclient-and-flyway.md) | Persistence with Spring `JdbcClient` and Flyway | Accepted |
| [0002](0002-exclusive-runtime-ownership-via-advisory-lock.md) | Exclusive runtime ownership via PostgreSQL advisory lock | Accepted |
| [0003](0003-upload-attempt-attribution-in-object-key.md) | Upload attempt attribution through the object key | Accepted |
| [0004](0004-csv-parser-selection-deferred-to-milestone-3.md) | CSV parser selection deferred to Milestone 3 | Accepted (decision deferred) |
| [0005](0005-versioning-and-release-mapping.md) | Versioning and release mapping | Accepted |
| [0006](0006-java-25-baseline.md) | Java 25 as the development baseline | Accepted |

## Template

```markdown
# ADR-NNNN: Title

- Status: Proposed | Accepted | Superseded by ADR-NNNN
- Date: YYYY-MM-DD
- Handbook references: chapters or sections this touches

## Context
## Decision
## Alternatives considered
## Consequences
## Verification
```
