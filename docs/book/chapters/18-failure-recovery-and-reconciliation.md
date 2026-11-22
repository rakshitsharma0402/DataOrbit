<!-- DataOrbit V1 Architecture & Engineering Handbook · Part IV · Safety & Testing · PDF page 53. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Failure recovery and reconciliation

*How DataOrbit interprets evidence when bytes and database checkpoints disagree.*

## Recovery starts from facts, not assumptions

A crashed process leaves at least two kinds of evidence: durable PostgreSQL records and objects visible through Storage inspection. The database can prove who held a claim, which transition committed and which task is pending. Storage evidence can prove whether an object is temporary, finalized, missing or unreadable, along with observed size and checksum when available. Neither source alone justifies deleting another object.

> **FIGURE 13. Recovery decisions depend on provable outcomes, safe isolation or ambiguous evidence that must be preserved.**  
> *(Diagram: see page 53 of the PDF.)*

## The four dangerous interruption windows

| Interruption window | Database view | Storage evidence | Safe policy |
|---|---|---|---|
| During UPLOADING | UPLOADING with attempt/run owner | Partial, finalized, missing or unreadable staging object | Inspect attempt attribution; do not reclaim by age alone |
| After verified staging, before upload metadata commit | Claim not STAGED | Finalized staged object may exist | Reconcile only when attributable and checksum-verifiable |
| After verified retention, before final DB commit | Job still active / old dataset disposition | Verified destination plus staging may exist | Preserve source; finish only provably safe metadata recovery |
| After final commit, before staging cleanup | COMPLETED / STORED or QUARANTINED | Verified destination and obsolete staging | Durable maintenance may safely remove staged source |
| After quarantine delete, before EXPIRED commit | QUARANTINED / cleanup in progress | Quarantine object absent | Verify removal identity before committing EXPIRED |

## Durable maintenance is also recoverable

A task may be PENDING, RUNNING, RETRY_PENDING, BLOCKED or COMPLETED. A previous-run RUNNING claim is not blindly re-executed. Startup inspection checks whether the removal or cleanup has already occurred, whether the target is still the same verified object, and whether the current state authorizes the operation. It is acceptable for a successful job to have cleanupPending true; it is not acceptable for deletion to proceed without sufficient evidence.

## What a blocked case communicates

A blocked operation is not a lost file. It is an explicit refusal to guess. The user and developer should be able to see a safe summary such as “Recovery requires review because the staged object cannot be attributed to the expected upload attempt.” Internal logs may contain diagnostic IDs. A manual retry is offered only after reconciliation establishes eligibility and cannot overlap with a retained uncertain old attempt.

### SCENARIO · Quarantine object missing after startup

1. PostgreSQL shows a QUARANTINED dataset whose maintenance task was RUNNING in a previous process.
2. Storage inspection reports the authorized quarantine object is absent.
3. The reconciler verifies the exact expected object identity and the task’s deletion intent.
4. It may finish the EXPIRED metadata transition only if the prior deletion is provable; otherwise it leaves the task blocked.

**Required outcome:** The application must not relabel an unrelated missing or unreadable file as safely expired.

> **Engineering rationale**
>
> **Problem:** Why preserve uncertain files?  
> **Selected approach:** Favor evidence preservation and explicit blocked states over speculative cleanup.  
> **Why it fits V1:** The cost of temporary disk usage is lower than permanent accidental loss or false metadata.  
> **Trade-off:** Recovery may require operator attention instead of always being automatic.  
> **Verification:** Crash-window, file-identity, checksum and stale-owner tests must show no unauthorized deletion.
