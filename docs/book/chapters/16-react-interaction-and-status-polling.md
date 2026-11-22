<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 46. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# React interaction and status polling

*A frontend that explains what the server did rather than simulating backend state.*

## Feature-oriented frontend modules

The frontend is a React + TypeScript application with a small typed API client and feature-oriented UI components: Upload Panel, Pipeline Graph, Dataset Table, Status Panel and supporting hooks. The exact files can evolve during coding, but responsibilities remain clear. A dedicated upload client may use `XMLHttpRequest` or an HTTP library with upload-progress events; ordinary `fetch()` does not reliably expose client upload-progress events in every environment.

*Illustrative implementation:*

```text
dataorbit-frontend/src/
  api/ # typed REST client and endpoint wrappers
  components/ # upload, graph, inventory, status views
  hooks/ # data fetching and status polling
  types/ # API DTO types shared in frontend code
  App.tsx # application composition
```

*Suggested feature-oriented layout; exact file names may change during implementation.*

## React Flow is a fixed, explainable pipeline

The processing graph contains fixed logical nodes for upload, validation, classification and storage. A successful invalid-content branch shows quarantine rather than pretending to classify the file. Users may click nodes to inspect details, but V1 does not permit rearranging the pipeline to change backend execution. Node presentation is derived from durable status and disposition, not inferred from animations.

| Backend state | UI behavior |
|---|---|
| INITIALIZED | Offer upload controls; no job progress |
| UPLOADING | Show measured browser transmission progress |
| STAGED | Offer Execute; stage is durably ready |
| VALIDATING | Highlight validation node and current-step label |
| CLASSIFYING | Highlight tier decision; do not claim percentages |
| STORING | Show verified retention activity |
| QUARANTINING | Show invalid-content branch and safe outcome |
| COMPLETED / STORED | Show selected tier and download action |
| COMPLETED / QUARANTINED | Show validation issues and quarantine retention |
| FAILED or RECOVERY_REQUIRED | Show distinct failure/recovery messaging |

## Polling is observation, not control

A status hook polls every one to two seconds while processing is active, with cleanup when the selected dataset changes or the component unmounts. It avoids overlapping requests and stops polling after terminal state unless a user explicitly refreshes. Failures in fetching Status are displayed as observation failures; they are not written back as processing failures. Job history and events are fetched separately with pagination.

## Safe client-side state

React owns the selected browser File, current form inputs and display-only state. It may cache the dataset UUID and the latest successful status response, but PostgreSQL remains authoritative for durable states and policy history. Frontend data types must preserve nullable fields: a storage tier may be null while validation is running or when a dataset is quarantined. The user should never need to infer a valid storage location from a UI component name.

## Visual direction and accessibility

The approved editorial design principles for the engineering book do not impose a final frontend color scheme. The application’s future UI should nevertheless use clear semantic labels, keyboard-accessible controls, high text contrast, progress descriptions readable by assistive technologies and explicit error/retry actions. Pipeline graphics should complement, not replace, text status. The existing visual mockups are concept references, not screenshots of working software.

> **CONCEPT STUDY 1. Concept artwork: dark operational dashboard composition; exploratory design, not implemented UI.**  
> *(Image: see page 47 of the PDF.)*

> **CONCEPT STUDY 2. Concept artwork: soft-color operations dashboard; exploratory design, not implemented UI.**  
> *(Image: see page 48 of the PDF.)*

> **CONCEPT STUDY 3. Concept artwork: light-theme dataset dashboard; exploratory design, not implemented UI.**  
> *(Image: see page 48 of the PDF.)*

> **CONCEPT STUDY 4. Concept artwork: file ingestion and dataset inventory composition; implementation not yet complete.**  
> *(Image: see page 49 of the PDF.)*

> **CONCEPT STUDY 5. Concept artwork: process visualization and WARM-tier placement; not evidence of executed code.**  
> *(Image: see page 49 of the PDF.)*

## The important distinction between loading and progress

During file transfer, browser network APIs can report how many bytes have been sent, so a percentage may be meaningful. During backend validation and copy, DataOrbit V1 exposes durable states and events but no calibrated percent-complete measurement. The UI must not use an arbitrary timer to increase a progress bar from 0% to 100% or claim success before the server has committed it.
