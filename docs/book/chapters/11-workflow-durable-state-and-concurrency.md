<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 32. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Workflow, durable state and concurrency

*Processing as an explicit state machine with database-guarded ownership and crash-safe checkpoints.*

## Execute is a durable operation, not a UI animation

service creates a processing-job UUID, claims one active job for the dataset, and records transitions as

`PLETED` and `FAILED`. The frontend’s diagram reflects those persisted states; it does not start or finish phases independently.

> **FIGURE 8. The valid and invalid branches converge on COMPLETED after successful durable storage or quarantine.**  
> *(Diagram: see page 32 of the PDF.)*

## An invalid file is not necessarily a failed job

Validation returns VALID or INVALID to describe the staged file’s content. Job state describes whether Workflow carried out the required processing. Thus a malformed CSV can follow `VALIDATING → QUARANTINING → COMPLETED` and leave the dataset with `QUARANTINED` disposition. An exception opening storage, a failed integrity check or a classifier defect instead results in an operational failure and ordinarily a `FAILED` job, with staging retained for safe recovery.

## Atomic claims and fenced transitions

A PostgreSQL partial unique index prevents more than one active processing job per dataset. Workflow’s claim includes the dataset UUID, a new job UUID, an application-run owner and the expected eligible dataset state. Every later checkpoint or finalization update must assert the expected job and owner. A stale request that resumes after ownership changed cannot silently advance the current job.

The system also distinguishes a completed historical job from current dataset disposition. For instance, a quarantined file may later expire: the job remains COMPLETED, while the dataset changes from QUARANTINED to EXPIRED through maintenance. Old job/event rows remain inspectable across manual retries.

## Transactions should protect facts, not hold a stream open

> **FIGURE 9. Three deliberate regions: short claim transaction, long external processing, short final metadata commit.**  
> *(Diagram: see page 33 of the PDF.)*

A state transition and its processing event are one atomic transaction. After a successful file copy, finalization commits all related facts together: dataset retained reference and disposition, job completion, a corresponding event and a pending staging-cleanup task. Classification provenance is committed using the exact effective policy snapshot. Only after the commit may cleanup proceed. A failed cleanup does not undo successful processing; Status reports `cleanupPending`.

## Manual retry and startup reconciliation

No full pipeline is automatically rerun after a crash. Reconciliation inspects the most recent database checkpoint and Storage evidence. Where a safe outcome can be proven, it may finish limited metadata recovery. If the outcome cannot be established, it preserves evidence and blocks unsafe continuation. An explicit manual retry creates a new processing-job UUID while retaining the history of previous attempts. It must not run until previous file/ownership evidence has been reconciled.

### SCENARIO · Two Execute requests race

1. Two requests attempt to claim processing for the same staged dataset.
2. A conditional database claim and partial unique active-job index allow only one active job.
3. The rejected request returns a lifecycle conflict; it does not start a second validation stream.
4. The frontend refreshes Status instead of treating the conflict as permission to retry automatically.

**Required outcome:** Exactly one active job exists and history is not corrupted by a duplicate invocation.

> **Engineering rationale**
>
> **Problem:** Why not a long all-or-nothing transaction?  
> **Selected approach:** Short transaction checkpoints around external I/O.  
> **Why it fits V1:** An SQL transaction cannot roll back a filesystem copy, and holding locks across a 250 MB stream would harm liveness.  
> **Trade-off:** Recovery logic must explicitly reconcile filesystem/database gaps.  
> **Verification:** Simulate interruption before claim, during validation, after copy, during final commit and after commit before cleanup.
