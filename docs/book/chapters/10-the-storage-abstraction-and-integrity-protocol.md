<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 29. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# The storage abstraction and integrity protocol

*Provider-independent object references, stream lifetime, checksum verification and safe deletion.*

## A storage location is a reference, not a Path

DataOrbit’s Storage API represents an object as `StorageReference(String provider, String objectKey)`. A reference identifies a logical object managed by a storage provider; it is not an absolute local path. Only the local adapter resolves that key beneath its configured storage root. This design protects the user interface from host paths, keeps Workflow independent of storage directory structure and makes a future cloud adapter feasible.

A local deployment maintains configurable staging, HOT, WARM, COLD and quarantine areas. Dataset UUIDs isolate objects. The adapter must reject traversal segments, absolute paths, unsafe separators and symlinks escaping the allowed root. It creates attempt-owned temporary files and finalizes them safely within the provider’s rules. Every stream is closed even on parsing, network or copy errors.

## Seven storage operations with distinct responsibilities

| Operation | Responsibility | Required proof |
|---|---|---|
| `stage` | Write streamed upload bytes to attempt-owned staging object | Actual size within cap, checksum and finalized staging reference |
| `open` | Return a closable streaming read with trusted metadata | Caller closes resource; no whole-file buffering |
| `transfer` | Copy staged source into HOT/WARM/COLD | Destination size/checksum match before finalize |
| `quarantine` | Copy invalid accepted source into isolated quarantine | Verified destination while staged source remains safe |
| `cleanupStaging` | Remove staged source after durable retained metadata commit | Source identity and retained evidence are consistent |
| `removeQuarantinedObject` | Remove quarantine object after approved expiry claim | Correct target, checksum and expiry eligibility |
| `inspect` | Read-only inventory/evidence for reconciliation | Classify FINALIZED, TEMPORARY, MISSING or UNREADABLE |

## A copy is not committed simply because bytes arrived

The safe transfer sequence is **source** → **destination temporary file** → **checksum and size verification** → **safe finalization** → **PostgreSQL final-state commit** → **staging cleanup**. On any uncertain result, preserving the original staged bytes is more important than freeing disk space. Transfer and quarantine results must identify the verified destination reference and integrity evidence. Neither operation owns a PostgreSQL transaction.

> **FIGURE 7. The required verified-copy and durable-commit ordering; the source survives uncertain intermediate outcomes.**  
> *(Diagram: see page 30 of the PDF.)*

## Streaming reads, downloads and resource ownership

A read returns a closeable result containing the stream and metadata needed by its caller. Validation consumes the stream in a try-with-resources scope. A download controller checks PostgreSQL eligibility, opens the trusted stored reference and streams the bytes as an attachment. It uses the safe filename, not an untrusted path, and closes the stream on successful completion or error. V1 uses a full-file retry if a transfer fails; HTTP Range requests are not supported, and successful server response completion cannot prove that the browser saved the file successfully. Download is restricted to retained valid datasets in V1; quarantined and expired objects are not exposed through the ordinary endpoint.

## Cleanup is idempotent, not reckless

The staging cleanup operation returns a meaningful outcome such as REMOVED or ALREADY_ABSENT. An already absent object is acceptable only when the operation is authorized for that exact source and the retained object is durably confirmed. A stale or uncertain reference must not cause deletion of a different file. Quarantine removal follows similar identity and checksum safeguards, plus expiration eligibility. An inspection call never deletes or mutates evidence. Reconciliation may also need discovery of unrecorded attempt-owned temporary `.part` objects, so inspection coverage cannot be limited to objects already present in the database.

*Illustrative implementation:*

```java
// Provider-independent identities, not physical disk paths.
public record StorageReference(String provider, String objectKey) {}
public enum StorageTier { HOT, WARM, COLD }
public interface StorageService {
    StagingResult stage(StagingRequest request, InputStream source);
    StorageReadResult open(StorageReadRequest request);
    VerifiedTransferResult transfer(VerifiedTransferRequest request);
    QuarantineResult quarantine(QuarantineRequest request);
    CleanupResult cleanupStaging(StagingCleanupRequest request);
    CleanupResult removeQuarantinedObject(QuarantineRemovalRequest request);
    StorageInspection inspect(StorageInspectionRequest request);
}
```

*Method names reflect approved storage capabilities; exact record packages and exception details are implementation work.*

> **Engineering rationale**
>
> **Problem:** Why copy-verify-commit rather than move immediately?  
> **Selected approach:** Retain the staged source until the new object is verified and its database reference is committed.  
> **Why it fits V1:** A file move is not transactionally coupled to PostgreSQL and can destroy recoverable evidence.  
> **Trade-off:** Temporary duplicate disk use and cleanup tasks are required.  
> **Verification:** Inject copy failures, checksum mismatches, destination exists, DB commit failure and repeated cleanup attempts.
