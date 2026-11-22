<!-- DataOrbit V1 Architecture & Engineering Handbook · Part I · Product & Purpose · PDF page 8. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Scope, user journeys and acceptance

*The boundaries that make the first version small enough to build yet rigorous enough to trust.*

## One local user and one runtime owner

V1 is operated by a single user on a local machine. The frontend and backend bind to loopback by default. There is no authentication, role management or public hosting requirement. The backend is one running Spring Boot process, even though its code is separated into nine Maven modules. A second simultaneously active backend must not modify the same PostgreSQL and filesystem state.

The application enforces a configurable upload ceiling, initially **250 MB**. Initialize may reject a declared oversize file early; however, the actual stream counter is authoritative. A rejected oversized upload is not an invalid CSV and is not quarantined. V1 supports full-file reupload after safe recovery, not resumable chunk transfer.

## What the user can do

| User action | Observable result | Design implication |
|---|---|---|
| Initialize an upload | Receive a durable dataset UUID | Identity exists before bytes arrive |
| Upload the CSV | See real browser transfer progress; reach STAGED | Upload receipt is not content validation |
| Execute processing | Follow validation / classification / storage or quarantine | Synchronous execution; independent status reads |
| Inspect a dataset | See current state, disposition, tier, diagnostics and history | Current state and immutable events are separate |
| Download a valid stored file | Receive an attachment streamed from trusted storage | The UI never receives a local filesystem path |
| Retry a safe failed attempt | Create a new processing job while retaining history | Ownership and reconciliation guard eligibility |

## Acceptance boundaries

A successful valid-file path must end with a checksum-verified retained object, an atomic database record of `STORED`, an appropriate tier and a completed processing job. A successfully quarantined invalid file must end with a verified quarantine object, `QUARANTINED` disposition, a completed job and readable validation issues. A system failure must not masquerade as ordinary invalid content, discard the original bytes, or silently report success.

The user can browse existing datasets and job/event histories. They cannot delete arbitrary retained datasets through the V1 UI. Deletion is reserved for safe retention expiration of quarantined objects and specifically authorized cleanup of superseded staging files.

## What does not belong in V1

| Out of scope | Why it remains deferred |
|---|---|
| Multi-user authentication / authorization | The product runs locally for one user; introducing roles would expand the threat and implementation model |
| Public deployment or cloud storage as the default | The storage abstraction preserves future options without requiring network storage |
| Background task broker or distributed workers | One synchronous workflow avoids queue orchestration in the learning-focused first release |
| Python ETL worker and more source formats | CSV ingestion and durable tiering establish the foundation first |
| Automatic reclassification / policy editor | V1 uses immutable startup policy; historical decisions stay explainable |
| User-controlled deletion and resumable upload chunks | These require additional destructive-operation or partial-transfer guarantees |

> **Engineering rationale**
>
> **Problem:** How much should V1 attempt?  
> **Selected approach:** A bounded local CSV workflow with durable history and recovery.  
> **Why it fits V1:** It provides a meaningful, demonstrable product without hiding key reliability problems behind a large technology stack.  
> **Trade-off:** It does not prove the economics or behavior of real cloud tiers, nor does it implement advanced data platform features.  
> **Verification:** End-to-end tests on valid, invalid, oversize and interrupted file scenarios establish the first acceptance evidence.
