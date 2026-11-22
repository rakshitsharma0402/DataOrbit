<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 40. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Startup configuration and policy snapshots

*Keeping runtime settings validated, stable and explainable across a process lifetime.*

## Properties are inputs, not mutable global state

Spring Boot configuration properties and environment variables supply the V1 effective settings: storage root, upload maximum, CSV validation ceilings, classification thresholds, PostgreSQL connection, quarantine retention and localhost networking. Configuration validates required values before the app begins accepting work. Missing required paths, impossible threshold order or invalid limits must fail startup rather than degrade into unsafe defaults.

## Immutable policy injection

Application assembly constructs or obtains an immutable `CsvValidationPolicy` and `ClassificationPolicy`. These are injected into their respective components once at startup. Validation and Classification do not read configuration on each dataset or fetch policy changes halfway through a job. This matters because the result and its recorded provenance must derive from a coherent policy snapshot.

*Illustrative implementation:*

```java
// Illustrative configuration objects, not final Spring annotations.
public record CsvValidationPolicy(
    long maxFieldUtf16CodeUnits,
    long maxRecordBytes,
    int maxColumns,
    int maxReportedIssues) {}
public record ClassificationPolicy(
    long hotMaxBytes,
    long warmMaxBytes) {}
```

*Production constructors must reject nonpositive resource caps and invalid tier thresholds.*

## Configuration change is a restart event in V1

A user cannot modify classification thresholds live from the V1 UI. A configuration change takes effect after restart and should result in an immutable policy revision in PostgreSQL when the effective classification policy differs. Historical classifications remain associated with their prior revision and are not automatically reclassified. The seven-day retention clock recorded for existing quarantine objects is likewise not recomputed from later configuration changes.

| Setting | Illustrative effective V1 value | Validation requirement |
|---|---|---|
| Upload maximum | 250 MB (configurable) | Positive; count actual streamed bytes |
| HOT maximum | 10,000,000 bytes | Nonnegative |
| WARM maximum | 100,000,000 bytes | Strictly greater than HOT maximum |
| Field units | 1,000,000 UTF-16 units | Positive bounded integer |
| Record size | 4,194,304 source bytes | Positive bounded integer |
| Column maximum | 1,000 parsed fields | Positive integer |
| Diagnostic maximum | 100 issues | Positive integer |
| Quarantine retention | Seven days | Duration applied once when quarantined |
| Storage root | Configured local directory | Safe and writable for the trusted process |
