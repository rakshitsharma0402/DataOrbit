<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 69. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# Verification catalog

*A compact starting test register to expand into executable fixtures and integration suites.*

| Test ID | Scenario | Expected evidence |
|---|---|---|
| CSV-ENC-01 | Malformed UTF-8 | INVALID_ENCODING; no replacement decoding |
| CSV-BOM-02 | Single leading BOM | Accepted; BOM omitted from record-byte accounting |
| CSV-HDR-03 | Case/NFC equivalent headers | DUPLICATE_HEADER |
| CSV-HDR-04 | Header-only file | VALID with NO_DATA_RECORDS warning |
| CSV-QUO-05 | Stray/boundary quotes | MALFORMED_CSV |
| CSV-LIN-06 | CR/LF/CRLF mixed; quoted newline | Physical line numbering and logical record correct |
| CSV-LIM-07 | Field UTF-16 limit ±1 | Enforced before unbounded accumulation |
| CSV-LIM-08 | Logical raw-byte limit ±1 | Correct source-byte accounting |
| CSV-COL-09 | Trailing fields and column cap ±1 | Field count includes empties |
| CSV-DIA-10 | Error/warning issue budget | Error priority, issuesTruncated correctly set |
| CLS-01 | Tier boundaries ±1 byte | HOT/WARM/COLD exactly match policy |
| CLS-02 | Null/mismatched identity | Failure; never transfers wrong file |
| CLS-03 | Policy revision changes on restart | Historical snapshot remains exact |
| STO-01 | Streaming stage + checksum | Exact bytes and safe finalized staging |
| STO-02 | Verified transfer interrupted | Source preserved; no false retained success |
| STO-03 | Quarantine verification failure | No false QUARANTINED disposition |
| STO-04 | Repeat authorized cleanup | Idempotent removal; foreign object protected |
| UPL-01 | Concurrent PUT attempts | Only one upload claim succeeds |
| UPL-02 | Old attempt completion after restart | Fenced stale claimant rejected |
| UPL-03 | Ambiguous staging evidence | RECOVERY_REQUIRED; preserve bytes |
| JOB-01 | Concurrent Execute calls | One active job; conflict for duplicate |
| JOB-02 | Crash after verified copy | Reconcile; source preserved |
| DB-01 | Fail midway through final transaction | Atomic rollback of all SQL facts |
| DB-02 | State/event transition failure | No partial state or event commit |
| MNT-01 | Expire precisely after seven days | Persisted expiry; no clock reset |
| MNT-02 | Crash after deletion before DB update | Evidence-based EXPIRED recovery |
| API-01 | HTTP error taxonomy and 413 | Stable error code and safe message |
| API-02 | Download eligibility and stream close | Only stored valid data downloadable |
| UI-01 | Poll cleanup and lost Execute response | Refresh truth; no invented progress |
| OPS-01 | Backup/restore rehearsal | Database and bytes agree after restore |

This is an acceptance-plan index. Test IDs are provided for repository planning and are not assertions that test code already exists. Tests that intentionally destroy temporary test data must use isolated test roots, never a user’s actual DataOrbit storage root.
