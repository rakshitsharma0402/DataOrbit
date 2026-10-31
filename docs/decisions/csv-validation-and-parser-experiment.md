# CSV validation and parser experiment — reconstructed requirements

**Status:** Reviewed reconstructed requirements, supplemented by subsequent owner-approved
Checkpoint 2A decisions. ChatGPT and the owner have reviewed the revised implementation
ZIP and approved the Checkpoint 2A implementation for commit and push.
This is not a verbatim LLD transcript or a complete recovery of the original decisions.
The original discussions were approved, but their full text was never published
as canonical documents. Approval of those discussions does not imply approval of
the experiment-specific proposals below.

## Provenance and decision references

This document uses the recovered requirements in the project conversation,
[Issue #1](https://github.com/rakshitsharma0402/DataOrbit/issues/1), the
[project README](../../README.md), and the
[architecture overview](../architecture/overview.md).
The owner explicitly identified these two decisions:

| Decision | Recovered subject |
| --- | --- |
| LLD #60 | Evaluate uniVocity-parsers and FastCSV 4.x before selecting the production parser; neither was selected. |
| LLD #61 | Establish a standalone Maven/JUnit 5 conformance experiment. |

No exact requirement-to-decision mappings are assigned for LLD #48–#59.
The historical sections below retain the reconstructed reference and earlier proposal.
The subsequent owner-approved decisions and current implementation notes at the end
supersede the earlier unresolved questions and proposed API where explicitly stated.

## Recovered approved requirements

- V1 processes CSV datasets through Initialize → Upload to staging → Execute
  processing, with streaming handling and a configurable maximum of 250 MB.
  Successfully staged invalid CSV files are quarantined for seven days.

### Validation interface and outcomes

- `FileValidator.validate(ValidationRequest(dataset UUID, StorageReference))`.
- `ValidationResult(dataset UUID, VALID/INVALID outcome, List<ValidationIssue>, issuesTruncated)`.
- `ValidationIssue(code, ERROR/WARNING severity, message, optional row number)`.
- Operational failures use typed exceptions; they are not reported as `INVALID`.

### CSV structure, encoding, and byte preservation

- A CSV header is mandatory. Header names are normalized using
  `strip -> NFC -> lowercase(Locale.ROOT) -> NFC`; normalized duplicates are rejected.
- Header-only CSV is `VALID` with a `NO_DATA_RECORDS` `WARNING`.
- Decode strict UTF-8, permitting an optional initial BOM. Require consistent
  field counts, ignore blank records outside quotes, and allow empty field values.
- Accept LF, CRLF, CR, and mixed record endings. Preserve line breaks inside
  quoted fields. The experiment also covers files without a final newline.
- Double quotes delimit quoted fields; doubled quotes escape a quote. Reject
  stray or unclosed quotes, whitespace immediately before an opening quote or
  after a closing quote, and attempts to use backslash as an alternative quote escape.
- Preserve the original uploaded bytes and checksum. Do not normalize or rewrite
  CSV bytes, including during header-name normalization.

### Configurable safety candidates and diagnostics

Correctness and resource safety must both be evaluated. Record these candidate
configurable defaults for testing; they are not final production settings:

| Limit | Candidate default |
| --- | --- |
| Decoded characters per field | 1,000,000; Unicode counting unit remains unresolved. |
| Source bytes per logical record | 4 MiB; exact source-byte boundaries remain unresolved. |
| Columns | 1,000 |
| Issues | 100 |

Enforce each limit independently before dangerous allocation. Fatal malformed
errors stop processing. Warning-only input must be fully scanned, including when
the diagnostic budget is full. When diagnostics are bounded, prioritize errors
over warnings. `issuesTruncated` indicates incomplete diagnostics.

### Parser evaluation and standalone experiment

- Compare uniVocity-parsers with FastCSV 4.x using shared synthetic CSV fixtures,
  common expected outcomes, a common parser probe contract, and separate adapters.
  Neither library has been selected for production.
- Keep the Java/Maven/JUnit 5 experiment under
  `experiments/csv-parser-conformance/`, independent of production backend modules.
  Document actual test commands, results, and limitations.
- Include malformed-input and boundary/resource experiments at the approved
  10 MB, 100 MB, and 250 MB boundaries. Large generated inputs and runtime data
  remain excluded from Git. Small synthetic fixtures must preserve their exact bytes.
- Production validation, REST APIs, frontend work, and database integration are
  outside this experiment. Existing production architecture remains unchanged.

Checkpoint 1 provided the standalone Java release 21 Maven/JUnit 5 smoke harness.
See the [experiment README](../../experiments/csv-parser-conformance/README.md).
The earlier reconstruction preceded the shared runner; current implementation
scope is recorded below. Neither real parser adapter has been implemented.

## Historical unresolved architectural questions

These three questions describe the earlier reconstruction. Subsequent owner
approvals below have now resolved them; they are not current open policy questions.

1. **Unicode counting:** whether a character limit counts UTF-16 code units,
   Unicode code points, or another explicitly defined measure. The candidate field
   limit applies to decoded characters; their counting unit remains unresolved.
2. **Logical-record bytes:** exactly which source bytes belong to a logical record,
   including quotes, escape syntax, delimiters, embedded newlines, record
   terminators, and BOM treatment. Bytes read ahead by a decoder/parser are not
   automatically the record's byte length.
3. **MB convention:** the exact byte unit behind the approved size boundaries.
   Do not silently substitute MiB or assign exact byte values to the MB-labelled
   file-size boundaries. The explicitly specified 4 MiB logical-record candidate
   does not settle that separate convention.

The definitions in the subsequent approvals govern the experiment. Library defaults
do not establish DataOrbit policy, and implementation support still requires evidence.

## Details requiring confirmation because the original text is unavailable

- The complete original decision text and precise requirement-to-decision
  attributions for LLD #48–#59.
- The complete original production diagnostic code inventory and any original
  message/row-number conventions. The subsequently approved experiment-only code
  inventory below does not claim to reconstruct the entire production inventory.

These are gaps in the reconstruction, not claims that the original discussions
left all of these topics undecided.

## Historical minimal experiment proposal — superseded for Checkpoint 2A

This section records the earlier unapproved proposal, not a claim that every detail
was subsequently approved. The current Reader-based SPI and scope are specified below.

Conceptual operation: `probe(inputSource, options, recordSink) -> ProbeResult`.
All types would belong only to the standalone experiment.
This proposal does not replace the recovered `FileValidator` interface. Any eventual
mapping must preserve its outcomes, issue severities, truncation semantics, and
typed operational failures.

| Element | Proposed responsibility |
| --- | --- |
| Input | A source identifier and opener returning a fresh byte stream for each run. The probe owns and closes the opened stream on success and failure. Raw-byte input permits malformed UTF-8, BOM, and injected I/O-failure tests. |
| Options | An explicit shared CSV rule profile and requested limits. Both adapters receive the same settings; no dialect detection, silent trimming, skipped records, or library defaults may silently decide policy. |
| Record sink | Receive ordered logical records with ordered decoded fields, one record at a time. Include the header as input to shared header validation rather than silently consuming it in one adapter. Preserve blank-record distinctions where required; record inability to expose them. |
| Result | Accepted or rejected, completed-record count (including any emitted header), observed input bytes read, and bounded diagnostics with a truncation indicator. Return no complete dataset. Small correctness tests may collect records in their sink; large tests use a counting sink. |
| Diagnostic | A normalized category, optional logical-record/field/physical-line location, and bounded context. Exact library exception wording and unavailable offsets are not common correctness assertions. Partial counts/read-ahead may differ after rejection. |
| Execution failure | A separate exception preserving the underlying cause for source-open/read/close failures, sink failures, unsupported configuration, or unexpected adapter failures. Malformed UTF-8 is a content rejection even when Java represents it as an I/O exception. Never turn every exception into an invalid-CSV result. |

Proposed rejection categories are encoding, CSV syntax, header/shape, and configured
limit violations. Names and detailed mappings remain UNAPPROVED and require review.
The recovered stopping/scanning rules above still apply: fatal malformed errors
stop processing, while a full diagnostic budget alone must not stop a warning-only scan.

### Limits and streaming safety

Request field size, record size, maximum columns, and diagnostic count/context
bounds explicitly. Document each measurement's unit. Do not claim equivalence
between a native character limit and an unresolved byte limit. ASCII cases can
establish unambiguous initial boundaries; dependent Unicode/byte cases stay pending.

Process incrementally with bounded buffers, one bounded record, necessary header
state, and bounded diagnostics. Release parser resources on every exit. A check
after constructing an oversized field/record cannot demonstrate safe allocation
limits. Mark missing pre-allocation protection as a limitation rather than adding
a second CSV tokenizer to hide it.

Keep elapsed-time and heap budgets in the test harness, using isolated runs where
needed. A timeout, exhausted heap, or process crash is a failed/aborted experiment,
not a successful CSV rejection. Track source closure and read behavior separately.

### Shared fixture plan

Use one manifest containing fixture ID, exact bytes/source generator, options, and
reviewed expected records or rejection categories. Run it unchanged for both adapters.
Cover the recovered correctness inventory, limit-minus-one/limit/limit-plus-one
cases, long or unterminated fields, excessive columns, injected read failures,
and closure on all exits. Distinguish physical blank lines from records of empty fields.
Record unresolved expectations as pending rather than deriving them from whichever
parser happens to accept the input. Generate large cases under ignored output paths.

### Recording library-specific limitations

Maintain a results table keyed by library/version, fixture, and requirement, showing
native support, shared validation/wrapper support, unsupported behavior, or an
unresolved expectation. Record effective settings, enforcement timing, observed
behavior, and a minimal reproducer. Unsupported or pending checks never count as passes.
Shared post-parse header/shape checks must remain distinguishable from native parser
behavior and resource protection.

Adapter mappings need verification against the exact later-selected versions;
[FastCSV's 4.x migration guide](https://fastcsv.org/guides/upgrading/) documents
changes to defaults and limit configuration. These library behaviors are evidence
to record, not new DataOrbit architectural decisions.

## Subsequent owner-approved Checkpoint 2 decisions

The following ten rows consolidate the subsequent owner approvals and the
Checkpoint 2A implementation instruction. A1–A10 are editorial labels for this
document, not recovered LLD numbers or invented mappings for #48–#59.

| Label | Approved decision |
| --- | --- |
| A1 | Shared strict UTF-8/initial-BOM decoding and header/shape validation; library-specific CSV tokenization, quote-unescaping, blank-record identification, and capability reporting. |
| A2 | The shared runner owns source/session cleanup. Header is emitted as ordinal 1; ignored blanks consume no ordinals; delivered records are immutable and callbacks synchronous. Final result determines validity. |
| A3 | Configurable diagnostic retention, candidate 100, prioritizes errors over warnings. Truncation means an observed issue was omitted or evicted, not merely that parsing stopped. Warning-only input scans to EOF. Messages are predefined and bounded. |
| A4 | Content rejection is distinct from typed operational failures. Preserve the original failure and suppressed cleanup failures; cleanup cannot yield a false successful result. Unexpected infrastructure exceptions are not INVALID CSV. |
| A5 | A header is mandatory; normalize strip → NFC → lowercase(Locale.ROOT) → NFC for duplicate detection without modifying emitted values. Invalid/duplicate headers stop. Invalid-width data records consume ordinals, report COLUMN_COUNT_MISMATCH, are not emitted, and scanning continues. Header-only CSV is VALID with NO_DATA_RECORDS WARNING. |
| A6 | Count Unicode code points in parsed field values after quote-unescaping. Candidate maximum: 1,000,000. Safe early enforcement needs adapter-specific evidence. |
| A7 | Count original UTF-8 source bytes per logical CSV record, including delimiters, quotes, doubled quotes, and embedded newlines. Exclude the terminating record separator, initial BOM, and ignored blank lines. Candidate maximum: 4,194,304 bytes, inclusive. |
| A8 | Whole-file MB is decimal: 1 MB = 1,000,000 bytes. For otherwise valid files: ≤10,000,000 HOT; >10,000,000–100,000,000 WARM; >100,000,000–250,000,000 COLD; larger files rejected. The separate record limit is binary 4 MiB. No storage-tier routing is added here. |
| A9 | Use the approved CsvProbeRunner, CsvParserAdapter, and ParserSession interfaces. Adapters borrow a Reader; there is no shared UTF-16-to-source-byte mapping or second CSV tokenizer. Unsupported/unverified capabilities cannot be credited as passing protection. |
| A10 | Checkpoint 2A implements only shared contracts/runner, a test-only scripted adapter, focused small JUnit tests, and documentation. Real adapters, parser dependencies, real-parser conformance evidence, and large generated experiments remain deferred. |

These approvals settle policy, not the final production tuning or the libraries'
ability to enforce it. The source-byte and code-point safety candidates remain
subject to experiments rather than being declared proven production defaults.

### Current interfaces and experiment-only issue codes

`CsvProbeRunner.run(ProbeInput, ProbeLimits, CsvParserAdapter, RecordSink)` returns
`ProbeResult` or throws typed operational/unsupported exceptions. An adapter exposes
identity/capabilities and opens a `ParserSession` over a borrowed `Reader`.
`ParserSession.next()` returns a tokenized record, a blank signal, or end-of-input,
and can report malformed input through `CsvRejectionException`.

The approved codes are `MISSING_HEADER`, `INVALID_HEADER`, `DUPLICATE_HEADER`,
`COLUMN_COUNT_MISMATCH`, `MALFORMED_UTF8`, `MALFORMED_CSV`,
`FIELD_CODE_POINT_LIMIT_EXCEEDED`, `RECORD_SOURCE_BYTE_LIMIT_EXCEEDED`,
`COLUMN_LIMIT_EXCEEDED`, and `NO_DATA_RECORDS`.
During Checkpoint 2A implementation the owner additionally approved
`INPUT_BYTE_LIMIT_EXCEEDED` for the shared whole-file byte-budget guard.
These codes are experiment-only; no additional production code inventory is inferred.

### Implemented behavior and limitations, reviewed for Checkpoint 2A

- `SharedCsvProbeRunner` opens a fresh source, counts original bytes (including BOM,
  separators and blanks), uses a reporting UTF-8 decoder, and strips only an initial
  UTF-8 BOM from the reader's character view. Original source bytes are never rewritten.
- A close-shield protects the runner-owned reader from adapter closes. Session
  cleanup precedes closing the reader/source chain, or the partially constructed
  input chain if opening fails. All closes are attempted. Operational failures
  remain primary with cleanup failures suppressed. Recoverable content diagnostics
  do not become fatal exceptions: absent a primary operational or fatal content
  failure, Stage.CLEANUP identifies the actual first cleanup exception as its cause,
  with later cleanup exceptions suppressed. Cleanup after fatal content rejection
  retains the fatal content failure as its cause and suppresses all cleanup failures.
- Shared normalization, duplicate detection, width checks and ordinal assignment
  are implemented. Empty normalized header names are not newly forbidden. An
  impossible nonblank zero-field header signal is INVALID_HEADER. Further header
  restrictions require an explicit approved policy.
- Diagnostic count and fixed message storage are bounded. Errors precede retained
  warnings; earlier errors are retained and a new error can evict the latest retained
  warning. A zero issue budget is supported: outcome still reflects observed errors,
  and observing an omitted issue sets truncation. Fatal rejection alone does not.
- The whole-file byte guard uses bounded reads, observing at most one byte beyond
  the configured inclusive maximum before rejecting. Small custom limits test the
  guard; no 10 MB, 100 MB, or 250 MB artifacts are generated. Configurable experiment
  limits are not a production upload configuration or tier router.
- Parsed-field code-point and column checks operate on already-materialized records.
  They verify semantics only. Early field, column and logical-record byte protection
  remain UNVERIFIED unless an adapter supplies evidence. There is no shared logical
  record source-byte accounting, guessed offset, or tokenizer.
- On session end-of-input the runner finishes strict decoding and whole-file budget
  verification. Unread decoded characters cause an ADAPTER_READ operational contract
  violation, not VALID/INVALID CSV. Source I/O, malformed UTF-8 and byte-budget
  failures discovered during verification take priority. No tail is tokenized.
  Parser-owned internal buffering remains invisible to this reader-level check and
  requires real-adapter conformance tests. Fatal content rejection does not drain.
- `SyntheticParserAdapter` exists only under test sources. It scripts events and can
  consume the supplied reader without parsing it. Its successful VALID results prove
  shared behavior only, never real quoting, quote-unescaping, newline interpretation,
  blank-line recognition, or native allocation safety.
- CapabilityStatus is SUPPORTED, UNSUPPORTED or UNVERIFIED; it has no PASS value.
  SUPPORTED describes a declared mechanism/evidence, not an executed conformance test.
  Future case reporting distinguishes PASS, FAIL, UNSUPPORTED, DEFERRED and ABORTED;
  a full conformance ledger, worker-JVM budgets and real-adapter acceptance gate are
  not implemented in this checkpoint. Missing protection cannot count as PASS.

## Current review gate

The revised Checkpoint 2A implementation is approved for commit and push. Verify the
pushed commit on GitHub before creating a PR; merging is not authorized by this approval.
Keep the experiment independent of production modules. Do not add uniVocity/FastCSV
dependencies or adapters, choose a production parser, or claim the experiment is complete.
Remaining questions are original-record completeness, unspecified production diagnostic
conventions, additional header-name restrictions, and real-adapter compatibility and
early-enforcement evidence; the three measurement/unit policies above are resolved.
