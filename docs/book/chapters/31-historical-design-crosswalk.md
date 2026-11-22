<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 83. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Historical design crosswalk

*The original low-level decision identifiers remain here only, mapped to the narrative topics they govern.*

### Why these numbers are confined to the appendix

The numbered records originated during the design conversations. The engineering chapters intentionally use subject-based headings so a new reader can understand the project without knowing the chat chronology. Original high-level decision wording for the earlier planning stage is not fully recoverable and has not been invented.

| Original ID | Approved subject | Where the book explains it |
|---|---|---|
| **37** | One runnable multi-module Maven backend | The modular monolith and its boundaries |
| **38** | Nine Maven modules and dependency direction | The modular monolith and its boundaries |
| **39** | Provider-independent StorageService contract | The storage abstraction and integrity protocol |
| **40** | Streaming staging with size and checksum | The storage abstraction and integrity protocol |
| **41** | Closable streaming read contract | The storage abstraction and integrity protocol |
| **42** | Verified transfer to HOT/WARM/COLD | The storage abstraction and integrity protocol |
| **43** | Verified quarantine copy | The storage abstraction and integrity protocol |
| **44** | Native streaming download through open() | The storage abstraction and integrity protocol |
| **45** | Idempotent staging cleanup after durable retention | The storage abstraction and integrity protocol |
| **46** | Quarantine expiration removal | The storage abstraction and integrity protocol |
| **47** | Read-only storage inspection for crash reconciliation | The storage abstraction and integrity protocol |
| **48** | FileValidator boundary and streamed validation | CSV validation as a bounded language |
| **49** | ValidationResult, outcomes and diagnostic model | CSV validation as a bounded language |
| **50** | Strict analytical CSV V1 defaults | CSV validation as a bounded language |
| **51** | Header-only CSV warning and issue severity | CSV validation as a bounded language |
| **52** | Strict UTF-8 and optional initial BOM | CSV validation as a bounded language |
| **53** | Header trimming and case comparison | CSV validation as a bounded language |
| **54** | Unicode NFC header equality | CSV validation as a bounded language |
| **55** | Blank physical-line semantics | CSV validation as a bounded language |
| **56** | Line-ending acceptance | CSV validation as a bounded language |
| **57** | Strict double-quote CSV dialect | CSV validation as a bounded language |
| **58** | Independent bounded parser safety limits | CSV validation as a bounded language |
| **59** | Bounded severity-aware diagnostic collection | CSV validation as a bounded language |
| **60** | Two-candidate CSV parser selection strategy | CSV validation as a bounded language |
| **61** | Standalone parser conformance prototype | CSV validation as a bounded language |
| **62** | Physical line numbers for diagnostics | CSV validation as a bounded language |
| **63** | Ten stable validation issue codes | CSV validation as a bounded language |
| **64** | Immutable startup-injected CsvValidationPolicy | CSV validation as a bounded language |
| **65** | Field limit counts decoded UTF-16 units | CSV validation as a bounded language |
| **66** | Logical record limit counts original UTF-8 bytes | CSV validation as a bounded language |
| **67** | Column count and oversized-record precedence | CSV validation as a bounded language |
| **68** | Immutable ValidationResult invariants | CSV validation as a bounded language |
| **69** | Size-based DatasetClassifier public contract | Deterministic storage-tier classification |
| **70** | Decimal MB thresholds and exact boundaries | Deterministic storage-tier classification |
| **71** | Immutable ClassificationPolicy ownership | Deterministic storage-tier classification |
| **72** | Request/result invariants and Workflow identity check | Deterministic storage-tier classification |
| **73** | Immutable policy provenance per decision | Deterministic storage-tier classification |
| **74** | Standard Java exceptions for classifier failure | Deterministic storage-tier classification |
| **75** | Initialize and Upload IngestionService contract | Streaming ingestion and upload ownership |
| **76** | Upload states and exclusive PostgreSQL claim | Streaming ingestion and upload ownership |
| **77** | Upload attempt UUID and application-run fencing | Streaming ingestion and upload ownership |
| **78** | RECOVERY_REQUIRED and safe full-file retry | Streaming ingestion and upload ownership |
| **79** | Synchronous Execute contract and seven job states | Workflow, durable state and concurrency |
| **80** | Short transactions and crash reconciliation | Workflow, durable state and concurrency |
| **81** | Durable Status service and append-only events | Status, events and the user-visible lifecycle |
| **82** | Spring Boot properties and immutable startup policy | Startup configuration and policy snapshots |
| **83** | Five PostgreSQL core application tables | PostgreSQL as the lifecycle ledger |
| **84** | Operational persistence ownership and short atomic commands | PostgreSQL as the lifecycle ledger |
| **85** | Flyway, targeted indexes, UTC and retention | PostgreSQL as the lifecycle ledger |
| **86** | Versioned API and safe HTTP contract | REST contracts and failure semantics |
| **87** | React typed API, fixed React Flow and polling | React interaction and status polling |
| **88** | Localhost security and operational baseline | Localhost security and defensive defaults |
| **89** | End-to-end consistency audit | Architecture handoffs and integration invariants |
| **90** | Final V1 LLD sign-off and seven implementation milestones | Seven milestones to a working system |

## Source and interpretive boundaries

This publication is based on the confirmed DataOrbit V1 design baseline and the previously generated standalone Master Engineering Edition 3.0. Historical numbering is preserved only as a navigational crosswalk. The broader early project vision is described using recoverable scope themes, not claimed verbatim minutes. All SQL, REST examples, proposed physical fields, Java records and frontend mockups are illustrative until the seven implementation milestones are completed and tested.

The book describes an approved intended architecture, not a running deployment. It should be revised when real implementation evidence reveals a material design change, and that revision should record what changed and why. Library versions, final persistence technology, package names and parser selection remain deliberate implementation-time choices.
