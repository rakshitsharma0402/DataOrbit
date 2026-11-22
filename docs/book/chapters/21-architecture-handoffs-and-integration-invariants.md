<!-- DataOrbit V1 Architecture & Engineering Handbook · Part V · Implementation Roadmap · PDF page 60. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Architecture handoffs and integration invariants

*The concrete seams that deserve extra scrutiny when individually correct modules begin collaborating.*

## All nine modules must assemble without a cycle

A module can have an impeccable interface and still create a circular Maven dependency when wired into the application. The finished build must keep foundational types independent, perform policy injection in the application, and keep concrete PostgreSQL and filesystem adapters outside their consumers. The application’s responsibility is assembly; it should not become a dumping ground for business logic.

## The upload-attempt attribution test

An old attempt may leave a finalized staged file at the same dataset location a later attempt might use. It is not sufficient to ask “does a file exist for this dataset UUID?” Safe recovery must prove which attempt produced the object by consulting attempt-specific identity and trusted storage evidence. The exact mechanism—object naming, metadata or verified association—is a coding choice, but conclusive attribution is an approved architectural requirement.

## The policy snapshot consistency test

Classification may return only the dataset UUID and tier, but durable classification history must include the actual policy revision, thresholds, size and time. The application must inject the same startup policy snapshot into the classifier and the provenance-recording path. A test should deliberately arrange two distinguishable policy revisions and prove that a dataset is never recorded against the wrong one.

## The final-commit atomicity test

After a verified copy, final dataset state, completed job, processing event and cleanup task must be created atomically. An integration test should inject a failure into one database write and assert that no partial final state commits. It must also assert that the original staged object remains recoverable. This test connects Workflow, Status, Storage Local and Flyway schema constraints in one meaningful end-to-end contract.

> **Engineering rationale**
>
> **Problem:** What is the final consistency criterion?  
> **Selected approach:** Acyclic modules plus proven identities, exact policy provenance and atomic finalization.  
> **Why it fits V1:** Interface correctness alone does not guarantee correctness across handoffs and crashes.  
> **Trade-off:** Cross-module tests are more complex than isolated unit tests.  
> **Verification:** Maintain targeted integration tests for claim fencing, attempt attribution, policy snapshot identity and interrupted commits.
