<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 73. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Service contract atlas and Java design sketches

*Expanded typed operation vocabulary for the seven Storage API capabilities and the surrounding application handoffs.*

## Why full request and result types matter

Interfaces become safer when the arguments carry all evidence needed by an operation rather than relying on global mutable state. For DataOrbit, dataset identity travels with storage references and integrity expectations. A caller cannot request deletion merely by giving a filename; it must supply the exact authorized source, retained destination and expected SHA-256. The tables below show approved record shapes or illustrative fields explicitly identified as such.

| Operation | Approved input shape | Approved output shape |
|---|---|---|
| stage | StagingRequest(datasetId, originalFilename, maximumAllowedBytes), InputStream | StagingResult(datasetId, reference, safeFilename, sizeBytes, sha256Checksum) |
| open | StorageReadRequest for a trusted StorageReference | AutoCloseable StorageReadResult with InputStream, sizeBytes and filename |
| transfer | VerifiedTransferRequest(datasetId, sourceReference, destinationTier, expectedSha256) | VerifiedTransferResult(destinationReference, tier, sizeBytes, sha256Checksum) |
| quarantine | QuarantineRequest for a staged source and integrity evidence | QuarantineResult(reference, sizeBytes, sha256Checksum) |
| cleanupStaging | StagingCleanupRequest(datasetId, stagedReference, retainedReference, expectedSha256) | REMOVED or ALREADY_ABSENT |
| removeQuarantinedObject | QuarantineRemovalRequest(datasetId, quarantineReference, expectedSha256) | REMOVED or ALREADY_ABSENT |
| inspect | StorageInspectionRequest(datasetId, expectedObjects) | ObjectEvidence(reference, condition, observedSizeBytes, observedSha256) |

Some complete request record fields and returned type names beyond the approved shapes will be finalized when coding. No example may substitute an absolute `Path` for a public `StorageReference` or omit evidence required to authorize destructive cleanup.

## A closeable streaming result in practice

*Illustrative implementation:*

```java
// Illustrative ownership pattern; exact record layout is deferred.
try (StorageReadResult file = storage.open(request)) {
    InputStream source = file.stream();
    // Consume in bounded chunks; do not call readAllBytes() on a 250 MB file.
    // Validation runs a tokenizer with explicit source-byte and field-unit caps.
}
// The stream closes even if validation throws.
```

*The approved read result is AutoCloseable and must be closed by Validation and the download streaming callback.*

## The smallest safe transactional command

A backend service may decide that validation succeeded and storage returned a verified HOT reference, but it does not become the owner of SQL. Instead, Workflow submits a typed finalization command to operational persistence. The database adapter checks the expected job owner and state, updates the dataset, finalizes the job, appends the event and creates staging cleanup intent within one transaction. A successful return means those database facts committed, not merely that four independent repository save methods were invoked.

*Illustrative implementation:*

```java
// Illustrative application-service structure; no long SQL transaction.
VerifiedTransferResult copy = storage.transfer(request);
// Verify same dataset, expected tier and checksum evidence.
processingPersistence.commitStoredDataset(
    new StoredDatasetCommit(datasetId, jobId, ownerRunId,
        copy.destinationReference(), copy.sizeBytes(),
        copy.sha256Checksum(), policySnapshot)
);
// Only after successful commit: schedule or run guarded cleanup.
```

*The exact command record and persistence adapter API are implementation-time details; atomicity and ownership fencing are approved rules.*

## A conditional claim in SQL

*Illustrative implementation:*

```sql
-- Illustrative sketch; column names and owner representation may vary.
UPDATE datasets
SET upload_state = 'UPLOADING',
    active_upload_attempt_id = :attemptId,
    upload_owner_run_id = :runId
WHERE dataset_id = :datasetId
  AND upload_state = 'INITIALIZED'
  AND active_upload_attempt_id IS NULL;
-- Require exactly one updated row. A zero-row result is not success.
```

*Condition-based updates must verify affected row count. Never accept a stale attempt completion based solely on dataset ID.*

## Boundary-specific test responsibilities

| Port or component | Positive test | Negative test |
|---|---|---|
| IngestionService | Initialize → stage → commit STAGED | Concurrent claim, oversized actual stream, DB commit failure |
| FileValidator | VALID report and warning-only outcome | Malformed byte/quote, immutable List mutation |
| DatasetClassifier | Boundary-accurate tier with exact ID | Negative size, policy invalid, mismatch returned ID |
| StorageService | Checksum-matching copy and closeable read | Path escape, unreadable object, mismatch, safe cleanup refusal |
| WorkflowService | Valid/invalid route and job completion | Duplicate Execute, stale owner, crash after copy |
| Status/persistence | Atomic state/event and history query | Transaction rollback and pagination limit |
| Configuration | Validated immutable policy injection | Bad limits fail startup; wrong provenance rejected |
