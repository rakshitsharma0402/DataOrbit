<!-- DataOrbit V1 Architecture & Engineering Handbook · Part V · Implementation Roadmap · PDF page 65. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# A practical reader’s learning guide

*How to study the architecture and use it as an implementation companion.*

## Read in dependency order, revisit by feature

A first-time reader should start with product scope and architecture, then read Ingestion, CSV Validation, Classification and Storage before jumping to Workflow. Database and Status become easier to understand once lifecycle transitions are familiar. REST and React form the presentation boundary. Recovery and testing chapters should be revisited whenever implementation touches an operation that could lose or delete bytes.

## Translate prose into four kinds of code

| Book artifact | Implementation artifact | Simple check |
|---|---|---|
| Module responsibility | Maven module and Java package | Is the dependency direction acyclic? |
| Contract record or interface | Java type with constructor invariants | Do bad arguments fail immediately? |
| State transition | Transactional command and event append | Can a stale owner update it? |
| Failure scenario | JUnit/integration test and fixtures | Are source bytes and committed state safe? |
| UI behavior description | React component and typed DTO | Does the display match committed server state? |

## Small examples are teaching instruments

Code examples in this book are intentionally compact. They communicate the approved shape and engineering constraints, not a complete compilable application. During implementation, import statements, exception types, Spring annotations, validation annotations, exact persistence technology and dependency versions will be settled through working code and tests. A narrative statement is considered implemented only when observable tests support it.

## Evolution after V1

The architecture creates extension points for richer placement policies, more input formats, alternative storage adapters and asynchronous processing. Those are *future possibilities*, not an authorization to quietly change the V1 contract. A material change must be reviewed against the approved invariants— identity, checksum evidence, policy provenance, transaction ordering, safety and user-visible status— before it is adopted.

### Recommended starting action

Begin Milestone One by verifying the installed Java compiler, Maven, Node/npm, PostgreSQL and Git. Create the parent Maven build and first executable Spring Boot entry point, then add one tested slice at a time.
