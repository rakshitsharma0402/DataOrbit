<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 27. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Deterministic storage-tier classification

*How an immutable policy maps exact byte sizes to a tier and preserves explainability.*

## A pure decision component

Classification starts only when Validation returns VALID. It does not open CSV bytes, modify files or write SQL. Workflow passes the verified actual size to a pure `DatasetClassifier`, which returns a `StorageTier`. The classifier checks that its request contains a non-null dataset UUID and a nonnegative size; its result must contain the same UUID and a non-null HOT, WARM or COLD tier.

> **FIGURE 6. Exact inclusive decimal byte thresholds for the default DataOrbit V1 storage policy.**  
> *(Diagram: see page 27 of the PDF.)*

*Illustrative implementation:*

```java
public record ClassificationPolicy(long hotMaxBytes,
                                   long warmMaxBytes) {
    public ClassificationPolicy {
        if (hotMaxBytes < 0 || warmMaxBytes <= hotMaxBytes)
            throw new IllegalArgumentException("Invalid thresholds");
    }
}
// Given the approved default policy:
// HOT : size <= 10_000_000
// WARM : 10_000_000 < size <= 100_000_000
// COLD : size > 100_000_000
```

*The classifier does not enforce the 250 MB ingestion cap; upload eligibility belongs to Ingestion.*

## Boundary conditions are part of the specification

| Actual verified size | Selected tier | Boundary reason |
|---|---|---|
| 0 bytes (mathematical input) | HOT | The classifier is size-only; actual empty CSV validation prevents ordinary retention |
| 9,999,999 bytes | HOT | Below the inclusive HOT boundary |
| 10,000,000 bytes | HOT | Exactly at the inclusive HOT boundary |
| 10,000,001 bytes | WARM | First byte beyond HOT |
| 100,000,000 bytes | WARM | Exactly at the inclusive WARM boundary |
| 100,000,001 bytes | COLD | First byte beyond WARM |
| 150,000,000 bytes | COLD | Accepted size under the initial 250 MB cap if structurally valid |

## Policy provenance is not an optional log line

The policy object is immutable and injected at startup. Configuration validates the required thresholds and associates the effective settings with an immutable policy revision in PostgreSQL. When Workflow records classification, it preserves the exact dataset size, tier, policy identity/version, threshold values and timestamp used for that decision. The provenance path must receive the same immutable snapshot that the classifier used; it must not fetch a new “current policy” independently.

Policy changes require application restart and new policy history. Previously classified datasets are not automatically moved or reclassified. If the classifier throws an exception or returns a mismatched dataset ID, Workflow reports an operational failure and preserves staging. It must not invent a fallback tier or confuse the failure with an invalid CSV.

> **Engineering rationale**
>
> **Problem:** Why not machine-learning placement yet?  
> **Selected approach:** Simple byte thresholds held in an immutable revision.  
> **Why it fits V1:** Exact integer comparisons are deterministic, explainable and sufficient to demonstrate the tiered-storage architecture.  
> **Trade-off:** The choice ignores future access frequency, network costs and predictive value.  
> **Verification:** Test thresholds ±1 byte, invalid policy construction, ID mismatch and immutable provenance consistency.
