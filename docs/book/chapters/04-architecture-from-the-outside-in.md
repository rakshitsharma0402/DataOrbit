<!-- DataOrbit V1 Architecture & Engineering Handbook · Part II · Architecture & Technology · PDF page 13. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Architecture from the outside in

*One browser UI, one Java backend and two coordinated durable stores.*

> **FIGURE 2. System context: the Spring Boot application mediates between the browser, PostgreSQL and local storage.**  
> *(Diagram: see page 13 of the PDF.)*

## A modular monolith, not nine services

A modular monolith is one deployable program with internally defined module boundaries. DataOrbit uses Java and Spring Boot for its backend and separates the code into nine Maven modules. Each module contributes compiled Java classes to one executable application. Modules call Java interfaces directly; they do not require internal HTTP or messaging infrastructure.

This structure keeps the product comprehensible during learning: exceptions, dependency injection and transaction boundaries remain within a single process. It also creates meaningful seams for tests and future improvements. A new storage implementation can satisfy `StorageService` without changing the upload API; a new classifier can satisfy `DatasetClassifier` without taking ownership of file copying.

> **Engineering rationale**
>
> **Problem:** Microservices or modular monolith?  
> **Selected approach:** One executable Spring Boot backend with nine Maven modules.  
> **Why it fits V1:** The first release needs clear interfaces more than distributed deployment; direct Java collaboration simplifies tracing and error handling.  
> **Trade-off:** Independent scaling and deployment are not available per module.  
> **Verification:** Build the parent Maven project, enforce dependency direction and run a full integration test through the single application.  

## Why the two data stores cannot be treated as one

PostgreSQL records the truth about lifecycle intent: which dataset exists, which upload attempt or processing job is active, what the latest committed disposition is, which policy was used and what cleanup is due. The filesystem records the truth about materialized bytes: a staging object, a temporary copy, a finalized retained copy or a quarantine object may exist independently of database transactions.

PostgreSQL can atomically roll back multiple SQL updates, but it cannot roll back arbitrary filesystem writes. DataOrbit therefore uses a deliberate ordering: verify the physical destination first; then commit the application-level metadata and history; finally remove obsolete source bytes. Startup reconciliation bridges the moments when the process could have stopped between steps.

## A single identity across the lifecycle

A dataset UUID is created at initialization and survives uploading, processing retries, quarantine and history inspection. A separate upload-attempt UUID identifies one effort to transmit the bytes. A processing-job UUID identifies one effort to validate, classify and retain the data. An application-run identifier fences work started by a previous backend instance. This separation makes recovery precise: two job attempts can belong to one dataset without being interchangeable.
