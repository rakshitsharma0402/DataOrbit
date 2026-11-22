<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 35. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Status, events and the user-visible lifecycle

*How the frontend sees truthful progress without replaying logs or inventing completion percentages.*

## Three related states that must not be collapsed

**Upload state** tracks transmission and staging: INITIALIZED, UPLOADING, STAGED or RECOVERY_REQUIRED. **Processing-job state** tracks one Execute attempt through the seven job states. **Dataset** **disposition** records whether the dataset has no retained final object (NONE), is stored (STORED), is quarantined (QUARANTINED), or has had quarantine bytes removed after expiry (EXPIRED). They are distinct dimensions.

| Upload | Job | Disposition | Meaning to UI |
|---|---|---|---|
| INITIALIZED | none | NONE | Dataset identity reserved; bytes not yet uploaded |
| UPLOADING | none | NONE | Streaming in progress or claim needs reconciliation |
| STAGED | PENDING / VALIDATING | NONE | Accepted bytes exist; processing underway |
| STAGED | CLASSIFYING / STORING | NONE | Valid-path processing active |
| STAGED | QUARANTINING | NONE | Invalid-path verified quarantine copy underway |
| STAGED | COMPLETED | STORED | Valid dataset retained and eligible for download |
| STAGED | COMPLETED | QUARANTINED | Invalid file safely quarantined |
| STAGED | COMPLETED | EXPIRED | Previously quarantined bytes removed; history retained |
| STAGED | FAILED | NONE | Operational failure requiring investigation or controlled retry |

## Current state and append-only events

The Status service reads committed PostgreSQL state. It exposes upload state, current job, tier, current step, a safe last-error summary and whether cleanup is pending. Processing events form an append-only history describing transitions and important outcomes. The current status is stored explicitly rather than reconstructed by replaying every historical event on each frontend poll.

An event accompanying a state transition commits in the same transaction as the state change. This prevents the UI from observing a new state with no corresponding event or an event announcing a state that never committed. Events and old jobs survive processing retries and are fetched through bounded pagination.

## What a status payload should communicate

*Illustrative implementation:*

```json
{
  "datasetId": "6e0c9db2-39f9-4acd-86d6-3ce521c29af0",
  "uploadState": "STAGED",
  "jobState": "CLASSIFYING",
  "storageTier": null,
  "currentStep": "Evaluating storage tier",
  "lastError": null,
  "cleanupPending": false
}
```

*Illustrative status shape; the backend schema and typed DTOs must agree on nullable fields and final disposition.*

The browser may compute a real upload-transfer percentage from transmitted bytes, but it must not claim that a processing job is 68% done without a measured progress metric. Showing “VALIDATING” or “STORING” is honest and sufficient for V1. If the polling endpoint fails, React reports a connectivity or observation problem, not a newly inferred backend processing failure.
