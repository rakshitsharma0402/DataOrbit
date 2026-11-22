# ADR-0006: Java 25 as the development baseline

- Status: Accepted
- Date: 2026-11-23
- Handbook references: "Why these technologies fit the design" (Java baseline); "Technical references and further reading" (Java SE API row).

## Context

The handbook "proposes Java 21 LTS and compatible current tooling" and states that dependency versions are not architectural decisions: they are pinned during the Project Foundation milestone and proven by an actual build. The maintainer's machine has JDK 25 (an LTS release) as its default, and no JDK 21.

## Decision

- Java 25 LTS is the language level and runtime for DataOrbit V1. The Maven build compiles with `--release 25`.
- Local development uses JDK 25. CI uses a JDK 25 distribution as well, so local and CI builds match.
- Framework and plugin versions chosen in Milestone 1 must officially support Java 25.

## Alternatives considered

- **Java 21 LTS, as the handbook proposes.** It would need a second JDK installed and kept selected for this project, for no design benefit.
- **Develop on JDK 25 but compile with `--release 21`.** This keeps the handbook's language level, but tests would run on a different runtime from the one targeted, which weakens the "proven by an actual build" requirement.

## Consequences

- This changes no approved invariant: the handbook leaves exact versions to the foundation milestone.
- Where the handbook or its references mention Java 21 (for example the Java SE 21 API documentation link), read them as Java 25. The handbook text itself is not edited.
- Java 25 language and library features may be used. Anything newer than Java 21 should still be used only where it makes code clearer, so the design stays readable against the handbook's examples.

## Verification

`docs/setup.md` records the verified JDK. The Milestone 1 build (`mvn verify`) and CI succeed on JDK 25 with `--release 25`.
