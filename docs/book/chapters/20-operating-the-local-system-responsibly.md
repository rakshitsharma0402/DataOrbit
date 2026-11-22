<!-- DataOrbit V1 Architecture & Engineering Handbook · Part IV · Safety & Testing · PDF page 57. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Operating the local system responsibly

*Startup ordering, backups, observability and realistic resource limits.*

## A deliberate startup sequence

The application must load and validate configuration, apply required Flyway migrations, establish exclusive V1 runtime ownership, inspect interrupted uploads/jobs/maintenance tasks, and start its maintenance coordinator before declaring itself ready for new work. Startup errors with incomplete migrations or invalid required settings must not be hidden by silently creating a new schema or deleting storage folders.

## Logging and diagnostic boundaries

Logs should correlate dataset UUID, processing-job UUID, upload-attempt UUID and application-run ID. They should include safe operation labels, outcomes and useful elapsed durations. Failure logs can contain backend stack traces for local debugging, but REST responses must not. Avoid raw CSV rows, secrets, unnecessary original filenames and full physical paths in user-visible status text.

## Backup is not the same as a migration

Flyway describes schema evolution; it does not protect existing data. A consistent local backup covers both PostgreSQL metadata and the filesystem storage root. The documented procedure should put the app in a quiescent state, capture both stores as a coordinated set, record backup time and relevant configuration, and verify a restore into a separate safe location. Restoring only the database or only the bytes can leave provider/key references inconsistent.

## Performance goals and measurement discipline

The initial 250 MB cap motivates streaming and bounded parser tokens, but it is not a promise that every machine will process a maximum-sized file within a fixed number of seconds. Performance tests should track actual memory growth, data rate, disk-space overhead during copy/verify, connection lifetimes, events generated and user-perceived status freshness. Thresholds should be tuned with representative fixtures on the chosen target machine.

| Operational signal | How to interpret it |
|---|---|
| STAGED but no job | Upload is complete; Execute may be available if no reconciliation blocker exists |
| Job COMPLETED, cleanupPending true | Retained outcome is durable; asynchronous staging cleanup has not yet completed |
| Maintenance BLOCKED | Safety cannot be established from evidence; do not force deletion |
| Storage object unreadable | Possible integrity or permission issue; preserve and investigate |
| Flyway startup failure | Schema state needs repair; do not auto-recreate tables |
| Repeated 409 conflict | Likely lifecycle or owner mismatch; refresh status rather than retry blindly |
