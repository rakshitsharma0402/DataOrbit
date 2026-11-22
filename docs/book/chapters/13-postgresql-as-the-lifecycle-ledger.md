<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 37. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# PostgreSQL as the lifecycle ledger

*Five core application tables, their invariants, transaction boundaries and durable maintenance intent.*

## The five-table logical schema

> **FIGURE 10. Conceptual entity relationships for the five approved PostgreSQL application tables.**  
> *(Diagram: see page 37 of the PDF.)*

| Table | Primary role | Representative fields (illustrative) |
|---|---|---|
| `datasets` | Current dataset/upload disposition and references | dataset_id, filename, upload_state, disposition, stage_provider/key, retained_provider/key, bytes, checksum, upload_attempt_id, run_owner, expiry |
| `processing_jobs` | One row per processing attempt | job_id, dataset_id, state, owner_run_id, policy_id, timestamps, safe diagnostic |
| `processing_events` | Append-only transition and observation history | event_id, job_id, dataset_id, event_type, occurred_at, safe payload |
| `classification_policies` | Immutable policy revision and thresholds | policy_id, revision, hot_max_bytes, warm_max_bytes, created_at |
| `maintenance_tasks` | Durable cleanup, expiry and reconciliation work | task_id, dataset_id, task_type, state, due_at, owner, attempt count |

### Logical fields versus final migrations

The five table names, responsibilities, key constraints and provider-independent references are approved. Exact SQL column names, data types beyond agreed timestamp semantics, migration splitting and ORM technology are implementation details. Example fields here are a blueprint, not a claim that migrations have already been executed.

## Database constraints provide a second line of defense

UUID primary keys identify datasets, jobs, events and tasks; foreign keys relate children to their parent dataset or job. NOT NULL and CHECK constraints reject structurally invalid states and thresholds. Targeted indexes support dataset lookup, chronological job history, paginated events and due maintenance work. A partial unique index enforces no more than one active processing job per dataset.

*Illustrative implementation:*

```sql
-- Illustrative invariant, to be implemented in a Flyway migration.
CREATE UNIQUE INDEX one_active_job_per_dataset
ON processing_jobs (dataset_id)
WHERE state IN (
    'PENDING', 'VALIDATING', 'CLASSIFYING',
    'STORING', 'QUARANTINING'
);
```

*The exact migration depends on the finalized physical schema. The uniqueness invariant itself is approved.*

## Transactional commands, not unrelated save calls

Operational persistence interfaces live behind Status, while Configuration owns immutable classification-policy persistence. Ingestion and Workflow issue transactional commands such as claim upload, claim job, transition state/event, and commit stored or quarantined outcome. The concrete PostgreSQL adapters are assembled by the application module. Status does not decide whether a file is valid; Workflow still owns business orchestration.

The final stored or quarantined result is one short transaction. It verifies current ownership and expected state, writes dataset disposition and provider/key reference, finalizes the job, appends an event and creates cleanup intent. Any SQL failure rolls back all those database changes. It does not erase a physical file copy that may already have completed, which is why the staged source and verified destination must remain available for reconciliation.

## Flyway and UTC timestamps

Flyway applies versioned SQL migrations on startup and records their versions in its own schema-history metadata. A failed required migration prevents application readiness. Hibernate, if selected, may validate mappings but must not silently generate or change production tables. All instants use PostgreSQL `TIMESTAMPTZ` and Java `Instant`; the application treats time values as UTC instants rather than assuming local wall-clock zones.

## Retention, history and maintenance tasks

When the invalid file is durably quarantined, its expiration timestamp is calculated and persisted once, seven days after quarantine. Restarting the application must not reset the retention clock. After expiration, maintenance claims an expiration operation, confirms staging-remnant reconciliation and verifies that the reference and checksum still match the authorized target. The cleanup lifecycle may use an internal EXPIRING claim before it removes bytes safely and commits EXPIRED disposition; EXPIRING is not one of the four public dataset dispositions. If deletion fails or object evidence is ambiguous, the dataset is not falsely marked EXPIRED.

Maintenance states are PENDING, RUNNING, RETRY_PENDING, BLOCKED and COMPLETED. Claims are atomic; a RUNNING task from a previous app run is examined before being reclaimed. V1 does not automatically delete old dataset, job, event or policy history. Processing success remains intact if a subsequent cleanup task is pending or blocked.

> **Engineering rationale**
>
> **Problem:** Why durable maintenance instead of deleting immediately?  
> **Selected approach:** A PostgreSQL-backed maintenance-task lifecycle.  
> **Why it fits V1:** Cleanup can be retried, audited and recovered without rewriting completed processing results.  
> **Trade-off:** Background maintenance needs careful ownership and failure handling.  
> **Verification:** Exercise task claims, duplicate schedulers, expiry boundary, checksum mismatch, crash after delete and database failure after delete.
