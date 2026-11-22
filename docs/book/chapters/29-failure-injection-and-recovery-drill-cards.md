<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 79. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Failure-injection and recovery drill cards

*Twelve explicit experiments for proving that DataOrbit preserves bytes and durable truth under interruption.*

## How to structure each drill

A drill names its initial database state, its physical source/destination evidence, the precise failure injection point, the permissible post-restart transition and the destructive actions that must not happen. Use disposable PostgreSQL and filesystem test environments. Never perform crash or deletion drills against actual user data.

### Upload stream ends early

- **Initial facts:** UPLOADING with active attempt; temporary .part exists
- **Injected fault:** Stop the source InputStream mid-chunk
- **Allowed recovery:** Inspect attempt evidence; RECOVERY_REQUIRED until safe isolation
- **Forbidden action:** Do not mark STAGED or reuse the attempt blindly

### Oversize stream exceeds cap

- **Initial facts:** UPLOADING; actual count reaches cap+1
- **Injected fault:** Send additional byte after declared in-range size
- **Allowed recovery:** Reject upload, preserve only evidence necessary for safe cleanup
- **Forbidden action:** Do not quarantine as malformed CSV

### Upload finalized before DB write

- **Initial facts:** UPLOADING; attributable finalized staged object
- **Injected fault:** Crash before STAGED commit
- **Allowed recovery:** Verify attempt identity/checksum before safe STAGED recovery
- **Forbidden action:** Do not issue duplicate staging overwrite

### Old upload owner resumes

- **Initial facts:** Replacement attempt has new owner/attempt
- **Injected fault:** Resume prior request’s completion SQL
- **Allowed recovery:** Fence stale update; preserve current owner
- **Forbidden action:** Do not accept dataset-only matching

### Validation read fails

- **Initial facts:** VALIDATING; staging exists
- **Injected fault:** Throw I/O error from Storage.open stream
- **Allowed recovery:** Operational failure and retained staging evidence
- **Forbidden action:** Do not convert to INVALID_ENCODING or quarantine

### Classifier fails

- **Initial facts:** CLASSIFYING; staged valid file
- **Injected fault:** Throw constructor/logic exception
- **Allowed recovery:** FAILED; record safe diagnostic and preserve bytes
- **Forbidden action:** Do not assign fallback tier

### Destination copy fails

- **Initial facts:** STORING; staging remains
- **Injected fault:** Throw write failure mid-copy
- **Allowed recovery:** Preserve source and isolate partial destination
- **Forbidden action:** Do not commit STORED

### Destination checksum mismatches

- **Initial facts:** STORING; final copy not verified
- **Injected fault:** Corrupt destination bytes before verify
- **Allowed recovery:** Reject finalization and retain staging
- **Forbidden action:** Do not remove source

### DB final transaction fails

- **Initial facts:** Verified destination and staged source coexist
- **Injected fault:** Rollback one required SQL update
- **Allowed recovery:** No partial dataset/job/event/task commit; reconcile later
- **Forbidden action:** Do not delete staging

### Crash after DB commit

- **Initial facts:** COMPLETED / STORED; cleanup task pending
- **Injected fault:** Crash before cleanup runs
- **Allowed recovery:** Maintenance safely cleans verified old staging
- **Forbidden action:** Do not mark the job FAILED

### Quarantine deletion interrupted

- **Initial facts:** QUARANTINED and expiry eligible
- **Injected fault:** Crash after object removal before EXPIRED update
- **Allowed recovery:** Inspect exact deletion evidence, then finish metadata if provable
- **Forbidden action:** Do not infer EXPIRED from unrelated missing object

### Unclear recovery evidence

- **Initial facts:** Old owner claim; object unreadable or mismatched
- **Injected fault:** Restart with ambiguous storage inspection
- **Allowed recovery:** Block and preserve all uncertain objects
- **Forbidden action:** Do not reclaim by file age or erase evidence

## Evidence review after a drill

The test must inspect the uploaded bytes and their SHA-256, all dataset and job states, ownership identity, events and outstanding maintenance tasks. A single assertion on an HTTP status code is insufficient. For a recoverable case, verify the next safe state transition. For a blocked case, verify that no full automatic reprocessing or unauthorized deletion occurs.

Test suites should also confirm that stale application-run claims cannot commit after restart, that a previously completed job remains completed when cleanup fails, and that a dataset can have EXPIRED disposition while its historical successful quarantine job remains in the event history.
