<!-- DataOrbit V1 Architecture & Engineering Handbook · Part I · Product & Purpose · PDF page 6. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# A system for trustworthy CSV handling

*A local application that accepts analytical datasets, verifies their structure and makes durable, explainable storage decisions.*

DataOrbit V1 is an intelligent data-management prototype with a deliberately narrow first responsibility: ingest analytical CSV files, validate them rigorously, classify valid datasets into simulated storage tiers and retain an auditable history of what happened.

A user interacts with DataOrbit through a browser, not a command line or direct database console. The system assigns each dataset a UUID, accepts its bytes through a streaming upload, records its actual byte size and SHA-256 checksum, and later processes the staged file. Valid analytical CSV datasets are retained in one of three logical storage tiers. Invalid but accepted files are placed in quarantine for a limited period. Operational errors are tracked separately from content invalidity.

The word *intelligent* describes an explainable pipeline that applies explicit rules and preserves the evidence needed to explain outcomes. In V1, classification is deterministic and size-based. DataOrbit does not yet use predictive models, real cloud storage tiers, advanced query engines or an ETL worker. Its architectural foundations leave room for such developments without making them prerequisites for the first working product.

> **FIGURE 1. The user journey separates dataset initialization, raw file transfer and deliberate processing.**  
> *(Diagram: see page 6 of the PDF.)*

## The problem DataOrbit is designed to solve

A raw CSV file is easy to upload and difficult to manage responsibly. Its contents may be malformed, truncated, non-UTF-8, internally inconsistent or deliberately hostile to a parser. Merely copying it into a folder does not make it trustworthy. Likewise, placing a file into a directory named “hot” does not explain why it belongs there, whether the copy was verified, or what would happen if the application crashed halfway through the operation.

DataOrbit treats these as engineering problems: **admission** of untrusted bytes, **validation** of a strict data language, **classification** according to an immutable policy, **verified retention** with checksum evidence, and **durable observation** of the full lifecycle. These responsibilities are kept distinct so a future change in CSV parsing or a new storage provider does not require rewriting the entire system.

## What the first usable release delivers

- **Local browser experience.** Upload a CSV, observe a fixed processing diagram, inspect dataset metadata and history, and download retained valid files.
- **Streaming ingestion.** Enforce the configured upload cap against actual received bytes without holding the entire upload in JVM memory.
- **Strict structural validation.** Produce stable, actionable issue codes rather than vague parser exceptions.
- **Transparent placement.** Assign HOT, WARM or COLD based on exact integer byte boundaries and preserve the policy used.
- **Safe retention.** Verify destination copies before committing new durable references, and clean staging only after successful commit.
- **Recovery-first operations.** Preserve uncertain objects after crashes and make interrupted upload or job attempts reconcilable.

## A promise and a non-promise

### What “architecture approved” means

The design has been reviewed and accepted as the V1 implementation baseline. It does *not* mean the Java application exists, that tests have passed, or that example code in this book has been executed. Implementation and verification are subsequent milestones.

The document distinguishes design commitments from educational discussion. Trade-offs and alternatives are explained for engineering learning, but those explanations do not imply that every alternative received a formal benchmark or design vote. Where original early planning details are unavailable, only recoverable architectural themes are presented as fact.
