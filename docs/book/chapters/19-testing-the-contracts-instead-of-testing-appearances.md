<!-- DataOrbit V1 Architecture & Engineering Handbook · Part IV · Safety & Testing · PDF page 55. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Testing the contracts instead of testing appearances

*A multilayer verification program that corresponds to the exact design promises.*

## The testing pyramid for DataOrbit

Unit tests verify small deterministic rules and constructor invariants. Contract tests verify that implementations honor module interfaces. Repository integration tests exercise real PostgreSQL constraints and transactions. Storage tests exercise real temporary directories and stream failures. End-to-end tests send HTTP requests through the runnable application and inspect both database and filesystem results. React tests verify user-visible states and safe behavior after lost network responses.

## CSV parser conformance laboratory

The parser-selection experiment is created in an `experiments/` area outside the nine production Maven modules. Each candidate adapter must face identical JUnit 5 fixtures and must pass required strictness and resource-safety gates before performance is compared. A fast parser that accepts forbidden quote syntax or allocates unbounded memory is not suitable.

| Fixture | Expected result | Boundary tested |
|---|---|---|
| UTF-8 BOM followed by a valid header | VALID | Single leading BOM allowed |
| Malformed UTF-8 continuation byte | INVALID_ENCODING | Strict decoder; no replacement characters |
| `Name,name` headers | DUPLICATE_HEADER | Case folding |
| Canonically equivalent composed/decomposed header | DUPLICATE_HEADER | NFC comparison |
| Header only, no data | VALID + NO_DATA_RECORDS | Warning-only outcome |
| Quoted embedded LF and CRLF | VALID | Physical versus logical line |
| Whitespace-only physical lines outside quotes | Ignored | Blank line semantics |
| Whitespace after closing quote | MALFORMED_CSV | Strict quote boundaries |
| Exactly 1,000 fields | Within column cap | Trailing empty fields count |
| 1,001 fields | TOO_MANY_COLUMNS | Proactive tokenizer limit |
| Field at 1,000,000 UTF-16 units | Within candidate cap | Decoded-unit accounting |
| Record at 4,194,305 source bytes | RECORD_TOO_LARGE | Raw-byte accounting |
| Multiple errors after diagnostic budget exhausted | INVALID with issuesTruncated | Error-priority bounded collection |

## Storage and database integration contracts

| Proof family | Representative verification |
|---|---|
| Integrity | Stage, open, transfer and quarantine preserve exact bytes and SHA-256; checksum mismatches never finalize success |
| Resource safety | Streams close on success, invalid CSV, download disconnect and injected I/O exceptions |
| Ownership | Concurrent upload/job claims yield one winner and an explicit loser conflict |
| Atomicity | State and event commit together; final dataset/job/event/task either all commit or all roll back |
| Recovery | Restart during every key interruption window; preserve ambiguous source and avoid double processing |
| Retention | Seven-day timestamp persists across restart; expiry task is fenced and idempotent |
| Provenance | Final classified dataset records the same immutable thresholds actually used by classifier |
| API | 409, 413, pagination, valid downloads and safe errors conform to typed DTO contracts |

## Failure injection is first-class evidence

A successful happy-path demo does not establish reliability. Tests should deliberately fail during stream reads, temporary writes, checksum comparison, file finalization, SQL commit, task deletion and HTTP response delivery. Each failure test must assert both what was committed and what physical evidence remains. After a simulated restart, the application should recover safe cases, block ambiguous ones, and never execute a destructive action solely because a timeout elapsed.

## Test names and evidence must be reproducible

Every significant scenario should have a stable test identifier, input fixture, initial database/filesystem state, injected fault when relevant, expected committed state, expected physical objects and rationale. Test evidence belongs in the repository or build output; the book supplies a plan, not a claim that those tests already exist or have passed.

### Implementation status

At publication, the architecture has been approved, while the codebase, CSV parser experiment, migrations, tests and operational drills remain to be implemented and verified. All test scenarios in this chapter are acceptance intentions.
