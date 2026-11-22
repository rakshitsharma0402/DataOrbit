# DataOrbit V1 Architecture & Engineering Handbook

The handbook is the approved design baseline for DataOrbit V1. It comes in two forms:

- **[`DataOrbit-V1-Handbook.pdf`](DataOrbit-V1-Handbook.pdf)** is the authoritative, human-readable edition. It contains every diagram and the concept artwork.
- **[`chapters/`](chapters/)** has the same text converted to Markdown, one file per chapter. These files are for searching, linking from issues and PRs, reviewing in diffs, and for Claude Code to read just the chapter it needs.

| | |
|---|---|
| Software release | DataOrbit V1 (planned) |
| Document revision | First book-style edition · 10 November 2026 |
| Architecture status | Approved design baseline |
| Implementation status | Not yet implemented or verified |

**How to read the labels.** Engineering rules described as "approved" are confirmed design constraints. Code and SQL snippets marked "illustrative" show intended implementation shapes. Alternative technologies and future features are learning context, not newly approved changes.

## About the Markdown edition

- The Markdown was converted from the PDF. Text, tables, code samples, scenarios, rationale boxes and drill cards are kept. **Diagrams are not converted:** each figure keeps its caption and the PDF page number, so open the PDF for the picture.
- If the Markdown and the PDF ever disagree, **the PDF wins**. Fix the Markdown to match, in a `docs(book):` commit.
- Each chapter starts with a comment giving its part and PDF page.
- A new handbook revision replaces both the PDF and the Markdown in the same commit.

## Reading routes

- **New to the project?** Begin with the product story, user journeys and system context (chapters 01–05). Later chapters assume those identities and failure distinctions.
- **Writing Java or SQL?** Use the component chapters (07–16) as contract references. Work through the seven milestones (chapter 22) with small tests and observable outcomes.
- **Reviewing safety?** Read the recovery chapter, failure windows and the testing catalog (18, 19, 25, 28, 29). Check both committed PostgreSQL state and filesystem evidence.
- **Looking for historical approvals?** The crosswalk (31) maps the original decision numbers to their chapters.

## Which chapter to read for which work

| Working on | Read first | Also relevant |
|---|---|---|
| Maven modules, dependency direction | 05 | 04, 21 |
| `storage-api`, `storage-local` | 10 | 27, 17, 29 |
| `validation` (CSV parser and rules) | 08 | 28, 25 |
| `classification` | 09 | 14 |
| `ingestion` (initialize, upload) | 07 | 10, 15, 18 |
| `workflow` (execute, jobs, retry) | 11 | 18, 21, 29 |
| `status` (state, events, persistence) | 12, 13 | 24 |
| `configuration` and policies | 14 | 09 |
| Flyway schema and SQL | 13 | 27 |
| REST controllers and errors | 15 | 12 |
| React frontend | 16 | 15, 12 |
| Security defaults | 17 | 10 |
| Startup, backup, operations | 20 | 18 |
| Tests and test IDs | 19, 25 | 28, 29 |
| Milestone scope and acceptance evidence | 22 | 23 |

## Contents

### Part I · Product & Purpose — *Understanding DataOrbit*

The product problem, the intended user experience, the deliberate limits of V1 and the guarantees that govern every later design.

1. [A system for trustworthy CSV handling](chapters/01-a-system-for-trustworthy-csv-handling.md) (p. 6)
2. [Scope, user journeys and acceptance](chapters/02-scope-user-journeys-and-acceptance.md) (p. 8)
3. [A walkthrough before the implementation](chapters/03-a-walkthrough-before-the-implementation.md) (p. 10)

### Part II · Architecture & Technology — *Designing the System*

From the user interface to durable records and verified bytes: the deliberate boundaries of the modular monolith.

4. [Architecture from the outside in](chapters/04-architecture-from-the-outside-in.md) (p. 13)
5. [The modular monolith and its boundaries](chapters/05-the-modular-monolith-and-its-boundaries.md) (p. 15)
6. [Why these technologies fit the design](chapters/06-why-these-technologies-fit-the-design.md) (p. 17)

### Part III · Backend & Frontend Specification — *Engineering the Components*

The concrete contracts, state transitions, constraints and cross-module handoffs that make the V1 system implementable.

7. [Streaming ingestion and upload ownership](chapters/07-streaming-ingestion-and-upload-ownership.md) (p. 20)
8. [CSV validation as a bounded language](chapters/08-csv-validation-as-a-bounded-language.md) (p. 23)
9. [Deterministic storage-tier classification](chapters/09-deterministic-storage-tier-classification.md) (p. 27)
10. [The storage abstraction and integrity protocol](chapters/10-the-storage-abstraction-and-integrity-protocol.md) (p. 29)
11. [Workflow, durable state and concurrency](chapters/11-workflow-durable-state-and-concurrency.md) (p. 32)
12. [Status, events and the user-visible lifecycle](chapters/12-status-events-and-the-user-visible-lifecycle.md) (p. 35)
13. [PostgreSQL as the lifecycle ledger](chapters/13-postgresql-as-the-lifecycle-ledger.md) (p. 37)
14. [Startup configuration and policy snapshots](chapters/14-startup-configuration-and-policy-snapshots.md) (p. 40)
15. [REST contracts and failure semantics](chapters/15-rest-contracts-and-failure-semantics.md) (p. 42)
16. [React interaction and status polling](chapters/16-react-interaction-and-status-polling.md) (p. 46)

### Part IV · Safety & Testing — *Reliability and Verification*

Making failures observable, preserving evidence, validating security boundaries and proving the design through tests.

17. [Localhost security and defensive defaults](chapters/17-localhost-security-and-defensive-defaults.md) (p. 51)
18. [Failure recovery and reconciliation](chapters/18-failure-recovery-and-reconciliation.md) (p. 53)
19. [Testing the contracts instead of testing appearances](chapters/19-testing-the-contracts-instead-of-testing-appearances.md) (p. 55)
20. [Operating the local system responsibly](chapters/20-operating-the-local-system-responsibly.md) (p. 57)

### Part V · Implementation Roadmap — *From Architecture to Implementation*

An incremental, learning-oriented build sequence with explicit proof before each milestone is considered complete.

21. [Architecture handoffs and integration invariants](chapters/21-architecture-handoffs-and-integration-invariants.md) (p. 60)
22. [Seven milestones to a working system](chapters/22-seven-milestones-to-a-working-system.md) (p. 62)
23. [A practical reader's learning guide](chapters/23-a-practical-readers-learning-guide.md) (p. 65)

### Reference material — *Appendices and References*

Compact technical catalogs, testing matrices, terminology and the historical decision crosswalk, kept out of the narrative chapters.

24. [Contract and state reference](chapters/24-contract-and-state-reference.md) (p. 67)
25. [Verification catalog](chapters/25-verification-catalog.md) (p. 69)
26. [Glossary and quick technical definitions](chapters/26-glossary-and-quick-technical-definitions.md) (p. 71)
27. [Service contract atlas and Java design sketches](chapters/27-service-contract-atlas-and-java-design-sketches.md) (p. 73)
28. [CSV conformance workbook](chapters/28-csv-conformance-workbook.md) (p. 76)
29. [Failure-injection and recovery drill cards](chapters/29-failure-injection-and-recovery-drill-cards.md) (p. 79)
30. [Technical references and further reading](chapters/30-technical-references-and-further-reading.md) (p. 82)
31. [Historical design crosswalk](chapters/31-historical-design-crosswalk.md) (p. 83)
