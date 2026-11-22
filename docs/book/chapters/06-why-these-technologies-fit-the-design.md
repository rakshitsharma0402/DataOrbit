<!-- DataOrbit V1 Architecture & Engineering Handbook · Part II · Architecture & Technology · PDF page 17. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Why these technologies fit the design

*The rationale for the chosen stack, and the limits of each choice.*

## Java, Spring Boot and Maven

Java is used for the long-running backend because it provides mature stream and I/O primitives, strong typing, well-understood exceptions and a broad testing ecosystem. Spring Boot supplies request handling, dependency injection, configuration binding, database integrations and application startup management. Maven defines reproducible builds and makes the nine-module compile-time structure explicit.

The initial development baseline proposes Java 21 LTS and compatible current tooling, with exact framework and plugin versions to be pinned during the project foundation milestone. Dependency versions are not architectural decisions; compatibility and reproducibility must be demonstrated by an actual build.

## React, TypeScript and React Flow

React manages reusable UI components, TypeScript defines data contracts at compile time and React Flow renders a fixed, inspectable representation of the processing pipeline. The graph is not a drag-and-drop workflow authoring tool in V1. Its nodes are clickable for details, while the server remains authoritative for actual processing states.

## PostgreSQL, Flyway and local disk

PostgreSQL provides transactionally reliable ownership claims, history, foreign keys and indexes. Flyway migrates the schema in versioned steps so existing datasets are not silently erased during upgrades. File bytes are retained outside PostgreSQL in a configurable local storage root; the API persists a provider/key reference instead of an absolute operating-system path.

## Synchronous processing as a conscious trade-off

The Execute endpoint waits while Workflow processes the dataset. This is easy to understand because one Java call coordinates the stages. It also creates practical concerns: HTTP timeouts, browser disconnects and long-running requests. Those do not erase the durable job. React polls the separate status endpoint every one to two seconds; it can show the committed stage while Execute is still running.

| Choice | Benefit | Accepted V1 limitation |
|---|---|---|
| Modular monolith | Simple deployment and testable module boundaries | No independent module scaling |
| Synchronous Execute | Direct orchestration and simpler failure flow | Long HTTP request and timeout management |
| Local simulated tiers | Transparent storage behavior with no cloud dependencies | Does not model real cloud retrieval costs or latency |
| Size-based placement | Deterministic, bounded and testable | No access-frequency optimization |
| Read-only polling | Familiar HTTP and durable status | Not event push and not exact continuous percentage |
