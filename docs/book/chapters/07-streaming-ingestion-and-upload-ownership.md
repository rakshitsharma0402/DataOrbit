<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 20. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Streaming ingestion and upload ownership

*Why upload is its own lifecycle, and how an interrupted transfer can be retried without corrupting identity.*

## Three public phases, two Ingestion methods

The public interaction consists of **Initialize** → **Upload** → **Execute**. Initialize reserves a dataset identity and stores its pending metadata. Upload accepts the raw HTTP request body as a stream and creates a verified staging object. Execute belongs to Workflow and begins only after the staged bytes have been recorded durably. Separating the phases avoids hiding a long file transfer inside an opaque all-in-one processing request.

*Illustrative implementation:*

```java
public interface IngestionService {
    UploadInitializationResult initialize(UploadInitializationRequest request);
    UploadResult upload(UploadRequest request, InputStream content);
}
public record UploadInitializationRequest(String originalFilename,
                                          long declaredSizeBytes) {}
public record UploadInitializationResult(UUID datasetId) {}
public record UploadRequest(UUID datasetId) {}
public record UploadResult(UUID datasetId, StorageReference stagedReference,
                           String safeFilename, long sizeBytes,
                           String sha256Checksum) {}
```

*The public Ingestion contract remains small even though ownership and storage recovery use additional internal records.*

## The database claim happens before streaming

When the upload begins, Ingestion must atomically change the dataset from `INITIALIZED` to `UPLOADING`. The claim records an upload-attempt UUID and an application-run owner. A second request cannot claim the same dataset concurrently; it receives a lifecycle conflict. The storage adapter then streams into an attempt-owned temporary file, counts actual bytes and computes SHA-256 as bytes arrive. No CSV content validation is performed at this phase.

On successful transfer, the storage adapter finalizes its staged object and reports the actual size, safe filename, checksum and provider-independent reference. Ingestion then conditionally commits `UPLOADING → STAGED` using the expected attempt and owner. If that metadata commit fails, the API must not report a successful upload. The finalized bytes are retained as evidence for recovery.

> **FIGURE 4. Upload state machine, including the guarded recovery branch after interrupted attempts.**  
> *(Diagram: see page 21 of the PDF.)*

## What makes an upload claim safe

- **Dataset identity:** stable across upload retries and processing jobs.
- **Attempt identity:** a new UUID for each distinct full-file upload attempt.
- **Run identity:** identifies the backend instance that owns the attempt.
- **Conditional transition:** the SQL update must match the expected dataset, state, attempt and run owner, and exactly one row must change.
- **Attempt evidence:** any finalized staging object must be attributable to the correct upload attempt, not merely to a matching dataset UUID.
- **Authoritative byte limit:** a declared size is an early screen; the streaming count is the final gate.

## Recovery required is a real state, not a timeout heuristic

Interrupted uploads may leave a temporary object, a finalized object, missing bytes or an uncertain combination. The state `RECOVERY_REQUIRED` prevents a new upload from racing with evidence that has not yet been inspected. On startup, reconciliation uses the database claim and Storage inspection to decide whether STAGED can be proven, a failed attempt can be safely isolated, or manual investigation is required. Age alone is not sufficient proof that an owner died.

Once a prior attempt is conclusively inactive and its incomplete evidence is safely handled, a fresh attempt may begin for the same dataset UUID with a new attempt UUID. The replacement upload starts at byte zero. The post-retention `cleanupStaging` operation is not a general-purpose cleanup mechanism for abandoned `.part` files; such partial files are handled through attempt-specific reconciliation.

### SCENARIO · Upload response lost after STAGED commit

1. The server commits verified upload metadata and the HTTP response fails to reach React.
2. The browser checks the dataset Status API rather than assuming the upload failed.
3. If uploadState is STAGED, the UI offers Execute; it must not overwrite the staged object with an immediate duplicate PUT.

**Required outcome:** A network error is interpreted as uncertainty about the response, not proof of a failed server operation.

> **Engineering rationale**
>
> **Problem:** Why is there no automatic blind retry?  
> **Selected approach:** Recovery requires explicit state and attempt-specific storage evidence.  
> **Why it fits V1:** An old request could still be committing and a finalized file could already exist; new writes must not overwrite it.  
> **Trade-off:** Recovery code is more intricate than treating every error as retryable.  
> **Verification:** Simulate disconnects, process restarts, stale claim owners, duplicate PUT and ambiguous temporary/final object combinations.
