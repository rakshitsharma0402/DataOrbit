<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 42. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# REST contracts and failure semantics

*A versioned HTTP boundary between the browser and the Java backend.*

## The browser talks to one API

The backend exposes endpoints under `/api/v1`. JSON carries ordinary request and response DTOs; raw CSV bytes and downloads are streamed rather than encoded as JSON. Public identifiers are dataset and job UUIDs. The REST contract does not expose `StorageReference`, host directories, internal Java exception types or database schema details. Controllers validate request shapes, delegate to the owning service and translate outcomes into stable HTTP responses.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/uploads/initialize` | Create dataset identity and initialized upload metadata |
| PUT | `/api/v1/uploads/{datasetId}/content` | Stream actual CSV bytes and record verified staging |
| POST | `/api/v1/datasets/{datasetId}/execute` | Start synchronous processing with atomic job claim |
| GET | `/api/v1/datasets` | Paginated dataset inventory |
| GET | `/api/v1/datasets/{datasetId}` | Dataset details and disposition |
| GET | `/api/v1/datasets/{datasetId}/status` | Read current committed upload/job state |
| GET | `/api/v1/datasets/{datasetId}/jobs` | Paginated job attempt history |
| GET | `/api/v1/datasets/{datasetId}/events` | Paginated processing events |
| GET | `/api/v1/datasets/{datasetId}/download` | Stream retained valid dataset bytes |
| POST | `/api/v1/datasets/{datasetId}/retry` | Controlled manual processing retry after reconciliation |

## The three request/response conversations

*Illustrative implementation:*

```http
POST /api/v1/uploads/initialize
Content-Type: application/json
{"originalFilename":"sales.csv","declaredSizeBytes":15000000}
HTTP/1.1 201 Created
{"datasetId":"6e0c9db2-39f9-4acd-86d6-3ce521c29af0"}
```

*Declared size is an early filter. The later stream measurement controls actual upload acceptance.*

*Illustrative implementation:*

```http
PUT /api/v1/uploads/{datasetId}/content
Content-Type: text/csv
<raw UTF-8 CSV bytes; no JSON wrapper>
HTTP/1.1 200 OK
{"datasetId":"...","safeFilename":"sales.csv",
 "sizeBytes":15000000,"sha256Checksum":"..."}
```

*The server returns upload success only after staged bytes and verified metadata are durably recorded.*

*Illustrative implementation:*

```http
POST /api/v1/datasets/{datasetId}/execute
HTTP/1.1 200 OK
{"jobId":"9e2c2c96-b720-4ef5-8d2e-d5b66b4aa872",
 "datasetId":"6e0c9db2-39f9-4acd-86d6-3ce521c29af0",
 "state":"COMPLETED"}
```

*COMPLETED describes successful processing, including successful quarantine. Dataset disposition gives the outcome.*

## HTTP status codes are part of the interface

| Code | Meaning | Frontend reaction |
|---|---|---|
| 200 | Successful operation or read | Update data from returned DTO |
| 201 | Dataset initialized | Remember dataset UUID |
| 400 | Malformed request or invalid request fields | Correct input; do not spam retries |
| 404 | Resource does not exist | Show not found and return to inventory |
| 409 | State/ownership conflict or retry not eligible | Refresh status to learn server truth |
| 413 | Actual or declared upload limit exceeded | Explain cap; do not quarantine rejected bytes |
| 415 | Unsupported upload content type | Explain expected input |
| 500 | Unexpected internal failure | Show safe error; inspect logs for diagnosis |
| 503 | Dependency unavailable | Allow deliberate later refresh/retry |

## Stable API errors rather than guessed messages

*Illustrative implementation:*

```json
{
  "code": "DATASET_NOT_READY",
  "message": "The dataset upload has not completed.",
  "datasetId": "6e0c9db2-39f9-4acd-86d6-3ce521c29af0"
}
```

*Example safe error envelope. Stable machine codes guide the UI; raw stack traces and storage paths are excluded.*

Paged reads accept bounded `page`/`size` parameters and return explicit pagination metadata. Download uses a trusted stored reference, streams to the response body, and applies a safe attachment filename. Normal users cannot download quarantined or expired objects via this route.

## What if Execute returns no response?

An HTTP connection can close while a synchronous Workflow thread continues executing. The browser must not infer cancellation from a lost response. It first calls Status and checks current job state. A duplicate Execute cannot create a competing active job; it should return `409 Conflict` when the dataset is not eligible, rather than silently beginning a second attempt.

> **FIGURE 11. Execute and read-only status polling are separate HTTP conversations sharing committed PostgreSQL state.**  
> *(Diagram: see page 44 of the PDF.)*

> **Engineering rationale**
>
> **Problem:** Why REST and explicit errors?  
> **Selected approach:** A versioned, JSON-oriented HTTP boundary plus streaming bodies and stable machine-readable errors.  
> **Why it fits V1:** It is simple to integrate with React and keeps internal Java/storage details out of the browser.  
> **Trade-off:** V1 does not provide push progress or a resumable upload protocol.  
> **Verification:** Contract tests cover DTO types, state conflicts, large uploads, pagination, download eligibility and lost responses.
