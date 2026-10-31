# CSV validation and parser experiment — reconstructed requirements

**Status:** Reviewed reconstructed requirements; proposed parser probe contract remains UNAPPROVED.
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
The sections below distinguish recovered approved requirements, known open
questions, details missing from the reconstruction, and new proposals.

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

Checkpoint 1 provides the standalone Java release 21 Maven/JUnit 5 smoke harness.
See the [experiment README](../../experiments/csv-parser-conformance/README.md).
The probe contract, adapters, and CSV fixtures have not been implemented.

## Known unresolved architectural questions

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

No proposal below resolves these questions. Assertions that depend on them remain
pending a reviewed definition; library defaults do not establish DataOrbit policy.

## Details requiring confirmation because the original text is unavailable

- The complete original decision text and precise requirement-to-decision
  attributions for LLD #48–#59.
- The complete diagnostic code inventory beyond the recovered `NO_DATA_RECORDS`
  warning, and any unspecified exact message or row-number conventions.

These are gaps in the reconstruction, not claims that the original discussions
left all of these topics undecided.

## Proposed minimal experiment contract — UNAPPROVED and not implemented

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

## Review gate

Review this reconstruction, the missing-policy questions, and the proposed contract
before implementation. Do not add dependencies, implement the probe/adapters,
choose a parser, or treat pending questions as settled through this document.
