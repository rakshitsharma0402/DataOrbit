<!-- DataOrbit V1 Architecture & Engineering Handbook · Part I · Product & Purpose · PDF page 10. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# A walkthrough before the implementation

*Four stories introduce the same states and guarantees that later technical chapters specify precisely.*

## A valid eight-megabyte CSV

### SCENARIO · Ordinary HOT-tier upload

1. The browser initializes a dataset; PostgreSQL records the new UUID and an INITIALIZED upload state.
2. The browser streams roughly 8,000,000 bytes; the staging adapter computes SHA-256 and commits the verified size/reference before reporting STAGED.
3. Execute atomically claims a processing job. Validation accepts the analytical CSV structure and reports any nonblocking warnings.
4. Classification uses the exact actual file size and immutable startup policy to select HOT.
5. Storage creates and verifies a HOT copy. A short PostgreSQL transaction commits the retained reference, classification provenance, completed job, event and cleanup intent.
6. The UI shows STORED/HOT, and a later maintenance task removes the old staged source.

**Required outcome:** The retained file is downloadable, no invented processing percentage appears, and the staging copy is removed only after safe commit.

## An invalid accepted CSV

### SCENARIO · Malformed quoting

1. A file passes the stream-size cap and is durably staged.
2. The validator detects an invalid quote boundary and returns INVALID with an ERROR diagnostic.
3. Workflow copies the original staged bytes to isolated quarantine and verifies the checksum.
4. PostgreSQL atomically records QUARANTINED, the seven-day expiry, a completed job and a cleanup task.
5. A maintenance task later removes quarantine bytes and records EXPIRED while preserving historical metadata.

**Required outcome:** Invalid content is not misreported as operational failure; a successfully quarantined invalid file can have a COMPLETED job.

## A valid 150 MB CSV

### SCENARIO · COLD placement with bounded memory

1. The actual received bytes remain below the configured 250 MB ceiling.
2. Validation streams the CSV rather than loading it completely and enforces per-field, per-record and per-row limits.
3. The classifier compares the exact size to 100,000,000 bytes and selects COLD.
4. The storage adapter verifies the destination copy, and only afterward does Workflow commit the final durable reference.

**Required outcome:** The entire 150 MB file is not buffered in Java heap; file-size classification is independent of the frontend display unit.

## A crash at the worst moment

### SCENARIO · Copy completed; database commit did not

1. The storage adapter finalizes a checksum-matching destination object while staging still exists.
2. The application process stops before PostgreSQL records the new durable reference.
3. At startup, the exclusive runtime owner inspects the database checkpoint and storage evidence.
4. Only a provably attributable, integrity-verified result may be safely finished; uncertain evidence remains blocked for investigation.

**Required outcome:** The source is not deleted, no double-processing job is created, and the app does not guess from object age alone.
