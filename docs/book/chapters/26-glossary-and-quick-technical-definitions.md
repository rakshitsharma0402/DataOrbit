<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 71. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Glossary and quick technical definitions

*A short reference for readers learning Java, Spring Boot, storage and durability concepts.*

| Term | Meaning |
|---|---|
| Atomic transaction | A database unit whose SQL changes commit together or roll back together. |
| Application-run owner | Identity assigned to a backend process instance for fencing stale claims. |
| BOM | Byte-order mark; V1 permits only an optional initial UTF-8 BOM. |
| CSV dialect | Specific grammar for separators, quoting, newlines and field boundaries. |
| Checksum | Digest used to detect a mismatch between expected and observed bytes; V1 uses SHA-256. |
| Classification policy | Immutable thresholds that determine HOT, WARM or COLD assignment. |
| Commit window | Interval between filesystem verification and the related metadata transaction. |
| CORS | Browser mechanism controlling cross-origin responses; not a complete access-control mechanism. |
| Dependency injection | A framework/application supplies objects to dependent components rather than each creating them. |
| Disposition | Current retained outcome of a dataset: none, stored, quarantined or expired. |
| DTO | Data transfer object: typed values exchanged across a module or REST boundary. |
| Event history | Append-only records of important processing transitions and results. |
| Fencing | Rejecting work from an owner or attempt that no longer holds a valid claim. |
| Flyway | Schema-migration tool that applies ordered, versioned SQL scripts. |
| Immutable policy | Settings held constant for a run and historically recorded by revision. |
| InputStream | Java abstraction for sequentially reading bytes without loading the whole source. |
| JDK | Java Development Kit, containing compilers and tools used to build Java applications. |
| JVM | Java Virtual Machine, the runtime that executes compiled Java bytecode. |
| Logical record | One parsed CSV row, which can span several physical lines inside quoted fields. |
| Maven module | A separately configured Java build artifact participating in a multi-module Maven project. |
| Modular monolith | One deployable application separated into internal modules with controlled dependencies. |
| NFC | Unicode Normalization Form C, used in the approved header equality pipeline. |
| ORM | Object-relational mapping; the JPA/JDBC choice remains deferred to coding. |
| Physical line | Actual input line separated by LF, CRLF or CR outside or within CSV quotation. |
| Provider-independent reference | Storage identifier composed of provider and object key, not an OS path. |
| Reconciliation | Evidence-based recovery of interrupted storage/database lifecycle operations. |
| React Flow | React graph library used for the fixed V1 processing visualization. |
| Retention | Policy governing when quarantined bytes become eligible for safe expiration. |
| Staging | Temporary verified landing area for accepted uploaded bytes before Execute. |
| Status polling | Repeated read-only HTTP requests for committed processing state. |
| TIMESTAMPTZ | PostgreSQL timestamp representing an instant with timezone semantics. |
| Tokenizer | Parser component that recognizes fields, separators, quotes and record boundaries. |
| UTF-16 code unit | A Java char-sized unit; some Unicode code points occupy two units. |
| UUID | Universally unique identifier for datasets, jobs and upload attempts. |
| Verified transfer | Copy with size/checksum evidence before finalization and metadata commit. |
