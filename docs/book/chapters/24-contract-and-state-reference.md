<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 67. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Contract and state reference

*A concise implementation lookup for names, variants and invariants without turning the main book into a meeting log.*

## The vocabulary that must not drift

| Concept | Approved form / values | Key invariant |
|---|---|---|
| Dataset identity | UUID datasetId | Stable across upload, jobs and retention |
| Storage object | StorageReference(provider, objectKey) | No absolute disk path in business contracts |
| Upload states | INITIALIZED, UPLOADING, STAGED, RECOVERY_REQUIRED | One active upload attempt with owner fencing |
| Processing-job states | PENDING, VALIDATING, CLASSIFYING, STORING, QUARANTINING, COMPLETED, FAILED | One active job per dataset |
| Dataset disposition | NONE, STORED, QUARANTINED, EXPIRED | Distinct from job success and upload state |
| Validation outcome | VALID or INVALID | VALID has no ERROR issue; INVALID has ERROR |
| Issue severity | ERROR, WARNING | NO_DATA_RECORDS is WARNING |
| Tiers | HOT, WARM, COLD | Exact decimal byte comparisons |
| Maintenance states | PENDING, RUNNING, RETRY_PENDING, BLOCKED, COMPLETED | Claims are durable and fenced |
| Timestamps | Instant / TIMESTAMPTZ | UTC instants, including persisted expiry |
| Processing policy | Immutable startup policy snapshot | Provenance records the same policy used |

## Interface ownership at a glance

| Boundary | Primary contract | Owner |
|---|---|---|
| Ingestion | initialize(request), upload(request, InputStream) | ingestion |
| Validation | validate(datasetId, stagedReference) | validation |
| Classification | classify(datasetId, sizeBytes) | classification |
| Storage | stage, open, transfer, quarantine, cleanupStaging, removeQuarantinedObject, inspect | storage-api and storage-local |
| Processing | execute(datasetId) | workflow |
| Status | recordEvent, getStatus, paginated history, atomic persistence commands | status |
| Policies | validated startup properties, immutable policy revisions | configuration |

## Source-fidelity convention

The book reports the design confirmed during DataOrbit V1 planning and final low-level sign-off. Earlier high-level decisions were available as consolidated scope themes rather than complete original numbered minutes; their exact historical wording is not reconstructed. Pseudocode, proposed columns, illustrative DTO payloads, UI concept images and future options are expressly not presented as implemented or independently approved changes.
