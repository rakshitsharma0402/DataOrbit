# ADR-0003: Upload attempt attribution through the object key

- Status: Accepted
- Date: 2026-11-23
- Handbook references: "Streaming ingestion and upload ownership", "The upload-attempt attribution test", "Failure recovery and reconciliation"; the handbook requires conclusive attribution and leaves the mechanism to coding.

## Context

An interrupted attempt can leave a finalized staged file. If staged objects were named by dataset ID alone, a later attempt (or recovery code) could not tell which attempt produced a file, and might trust or overwrite the wrong bytes. Attribution must come from attempt-specific identity plus trusted storage evidence, not from "a file exists for this dataset".

## Decision

- Object keys for staging are built only from identifiers, never from the user's filename:
  - temporary: `staging/{datasetId}/{attemptId}.csv.part`
  - finalized: `staging/{datasetId}/{attemptId}.csv`
- The user-supplied filename is sanitized into `safeFilename` and stored only as database metadata.
- The `.part` file is written in the same directory as the final object, fully flushed (`FileChannel.force`), then finalized with an atomic rename. A final-named file therefore means the write completed; its SHA-256 and size are still verified before it is trusted.
- Finalization never overwrites. Attempt IDs are fresh UUIDs, so collisions are impossible by construction, and the adapter additionally refuses to finalize onto an existing object.
- `inspect` lists objects under the dataset directory, parses the attempt ID from each name and reports a condition (`FINALIZED`, `TEMPORARY`, `MISSING`, `UNREADABLE`) with observed size and checksum. It also discovers unrecorded `.part` files. It never deletes or modifies anything.
- Reconciliation compares each discovered object with the database's active attempt ID and recorded checksum. Anything it cannot attribute stays untouched and the dataset goes to `RECOVERY_REQUIRED`.

## Alternatives considered

- **Sidecar metadata file per upload:** two files must stay consistent, which adds a new failure mode.
- **Filesystem extended attributes:** not portable across operating systems and filesystems.

## Consequences

- Objects from superseded attempts are visible by name and can be cleaned up deliberately through attempt-specific reconciliation (not through `cleanupStaging`).
- The same key shape works for a future non-filesystem provider, since the key is just a provider-independent string.

## Verification

UPL-01, UPL-02, UPL-03; drills "Upload finalized before DB write", "Old upload owner resumes", "Unclear recovery evidence"; storage tests for traversal and symlink escape (STO-01 to STO-04).
