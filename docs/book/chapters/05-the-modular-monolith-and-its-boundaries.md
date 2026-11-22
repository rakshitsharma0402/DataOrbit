<!-- DataOrbit V1 Architecture & Engineering Handbook · Part II · Architecture & Technology · PDF page 15. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# The modular monolith and its boundaries

*Module responsibilities, dependency direction and Java interfaces that prevent accidental coupling.*

> **FIGURE 3. The nine approved modules assembled by one Spring Boot application; arrows are conceptual caller-to-contract links.**  
> *(Diagram: see page 15 of the PDF.)*

## The nine-module map

| Maven module | What it owns | What it must not own |
|---|---|---|
| `dataorbit-app` | Executable Spring Boot entry point, REST adapters, dependency composition and concrete database adapters | CSV business rules or direct low-level storage policy |
| `workflow` | Processing sequence, jobs, controlled retry, recovery decisions | Parser-specific syntax and direct filesystem path construction |
| `ingestion` | Initialize and Upload, attempt claims, verified STAGED metadata | Execute-time CSV validation or tier selection |
| `validation` | Strict streaming CSV interpretation, issue reporting | PostgreSQL writes or REST controllers |
| `classification` | Pure tier choice for size and immutable policy | I/O, SQL or mutable application configuration |
| `storage-api` | Reference, tier and storage operation contracts | Knowledge of local disk layout |
| `storage-local` | Byte streaming, safe paths, checksum-verified copies, inspection, deletion | Database lifecycle state and SQL transactions |
| `status` | Operational persistence contracts, job/events/tasks, read-only status | The business decision of whether data is valid or where it belongs |
| `configuration` | Startup settings and immutable classification-policy persistence | Live policy editing or runtime CSV parsing |

## Dependency direction is a design constraint

The application module performs assembly. Workflow depends on interfaces from Validation, Classification, Storage API and Status. Ingestion uses Storage API and operational persistence. Storage Local depends on Storage API, and Validation uses Storage API to open the staged file. Classification may import the `StorageTier` enum from Storage API but does not call Configuration during classification. Configuration and Status expose foundational contracts. Concrete adapters are wired in the application module.

Some early sketches described Classification as directly depending on Configuration. The finalized design refined that relationship: **immutable policy objects are supplied at startup through application assembly**. This avoids a runtime lookup from Classification to Configuration, helps preserve an acyclic graph and ensures the recorded policy is the same policy actually used.

## Ports and adapters in beginner-friendly terms

An interface such as `StorageService` is a *port*: it describes what higher-level code needs without stating how the work is performed. `LocalStorageService` is an *adapter*: it performs the promised operation using files and directories. Workflow receives the interface and does not need to know the implementation class. Spring dependency injection supplies the implementation during application startup.

*Illustrative implementation:*

```java
public interface DatasetClassifier {
    ClassificationResult classify(ClassificationRequest request);
}
public record ClassificationRequest(UUID datasetId, long sizeBytes) {}
public record ClassificationResult(UUID datasetId, StorageTier tier) {}
```

*The contract belongs to the Classification module. The enum StorageTier is shared from Storage API.*

### What a Java record actually guarantees

A record provides concise constructor, accessor and value-semantics syntax. It is not a deep immutability mechanism: a record containing `List<ValidationIssue>` still needs `List.copyOf(...)` and validation to prevent mutable or contradictory reports.
