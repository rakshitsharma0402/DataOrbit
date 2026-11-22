<!-- DataOrbit V1 Architecture & Engineering Handbook · Part III · Backend & Frontend Specification · PDF page 23. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# CSV validation as a bounded language

*A precise definition of what V1 considers a structurally valid analytical CSV.*

## The validator is independent of ingestion and persistence

Validation is performed only after a staged object is available for Execute. The `FileValidator` receives a dataset UUID and provider-independent staged reference, opens the bytes through Storage API, and closes the stream in every outcome. It neither changes PostgreSQL state nor moves the file. It returns a completed immutable report or raises an operational exception.

*Illustrative implementation:*

```java
public interface FileValidator {
    ValidationResult validate(ValidationRequest request);
}
public record ValidationRequest(UUID datasetId,
                                StorageReference stagedReference) {}
public enum ValidationOutcome { VALID, INVALID }
public enum ValidationSeverity { ERROR, WARNING }
public record ValidationIssue(ValidationIssueCode code,
                              ValidationSeverity severity,
                              String message, Long lineNumber) {}
public record ValidationResult(UUID datasetId, ValidationOutcome outcome,
                               List<ValidationIssue> issues,
                               boolean issuesTruncated) {}
```

*ValidationResult must defensively copy issues and reject inconsistent outcome/severity combinations.*

## Encoding, separator and quote grammar

V1 accepts strict UTF-8 with one optional leading byte-order mark `EF BB BF`. Invalid byte sequences are reported as `INVALID_ENCODING` instead of being silently replaced. Comma is the only field separator. A quoted field starts at a valid field boundary with a double quote; doubled double quotes represent a literal quote, and embedded commas or line breaks inside the quoted value are data.

The dialect is intentionally strict at quote boundaries. A bare quote inside an unquoted field, whitespace between a delimiter and an opening quote, whitespace between a closing quote and the next delimiter, and backslash-style quote escaping are not accepted. This removes parser ambiguity, even though some permissive spreadsheet exporters might allow such forms. The conformance prototype must prove that the selected library can implement this policy without unbounded intermediate allocation. V1 validates structure, not domain-specific business values or numerical plausibility of arbitrary CSV cells.

| Input form | V1 interpretation | Reason |
|---|---|---|
| `a,b,c` | Three unquoted fields | Comma-delimited record |
| `"a,b",c` | Two fields; embedded comma is data | Quoted comma is not a delimiter |
| `"a""b",c` | First field contains `a"b` | Doubled quote escape |
| `a,"line one line two"` | One logical record spanning two physical lines | Quoted newline remains in the field |
| `a, "b"` | Rejected as malformed | Whitespace adjacent to opening quote boundary |
| `"a" ,b` | Rejected as malformed | Whitespace after closing quote before delimiter |
| `a,\"b\"` | Rejected as malformed | Backslash is not a quote escape |

## Physical lines are not logical CSV records

The validator accepts LF, CRLF and CR as separators outside quoted fields, including mixed styles and files without a final newline. An empty or horizontal-whitespace-only physical line outside quoted fields is ignored. The same newline characters inside a quoted field remain part of that field. Thus one logical record may span several physical lines. Issue locations refer to the *one-based physical line where the offending logical record begins*, including the first line of an unclosed quoted record. Ignored physical blank lines and newlines within quotes still contribute to physical line positions. The line number is nullable when a trustworthy position cannot be established.

## Header equality is defined, not guessed

A structurally useful analytical CSV needs a nonempty header record with nonempty, unique names. Uniqueness is evaluated on a comparison key, not by rewriting the original header bytes. The normalization pipeline is: Java `strip()` → Unicode NFC → `toLowerCase(Locale.ROOT)` → NFC again. This catches differences in case and canonically equivalent Unicode sequences without using broader NFKC compatibility folding, accent removal or confusable-character matching.

If the file contains a valid header but no data rows, the result is `VALID` with the nonblocking warning `NO_DATA_RECORDS`. An empty input or a file made only of ignorable physical lines is instead invalid with `EMPTY_FILE`. Empty field values within data records are allowed; column-count consistency is still enforced against the header.

> **FIGURE 5. The validator operates simultaneously in source-byte, decoded-character and parsed-record units.**  
> *(Diagram: see page 24 of the PDF.)*

## Four independent resource limits

| Limit | Candidate startup setting | What exactly counts |
|---|---|---|
| Field length | 1,000,000 UTF-16 code units | Decoded Java char units in an individual parsed field |
| Logical record length | 4,194,304 original UTF-8 bytes | Fields, commas, quotes, doubled quotes and embedded quoted line endings; not final external separator or file BOM |
| Fields per record | 1,000 fields | All parsed fields including empty and trailing empty fields, for headers and data |
| Reported issues | 100 issues | Bounded ERROR/WARNING collector with error priority |

These are configurable candidate limits, not four interchangeable measures. A field containing supplementary Unicode characters may use more UTF-16 units than its number of displayed symbols. Likewise, source-byte record limits cannot be reconstructed accurately from decoded string lengths, especially when quotes and embedded newlines are present. The counters must advance during tokenization before dangerous string or list allocations occur. Field length counts the decoded value without trimming or normalization; an excessive field count yields TOO_MANY_COLUMNS before an ordinary INCONSISTENT_COLUMNS comparison. Ignored outside-quote blank lines do not consume a logical record byte budget.

## Diagnostics and early termination

The result contains a stable outcome, a list of issues and `issuesTruncated`. Warnings alone do not end scanning: a file with a header-only warning can still be valid. A proven fatal encoding, syntax or size condition may stop immediately. Once the file is demonstrably invalid and the reporting budget is full, validation may stop early and mark the issue list truncated when additional diagnostics may be missing. If any ERROR appears, outcome cannot be VALID; if outcome is INVALID, at least one ERROR must appear.

A single error report is useful only if the scanner remains safe. Reporting all possible issues is not a higher priority than bounding CPU and memory. The parser prototype should therefore measure both diagnostic fidelity and pathological-input behavior.

## The ten diagnostic codes

| Code | Severity | Meaning |
|---|---|---|
| EMPTY_FILE | ERROR | No header or meaningful CSV content |
| INVALID_ENCODING | ERROR | Malformed UTF-8 input |
| MALFORMED_CSV | ERROR | Invalid quote boundaries or broken CSV syntax |
| EMPTY_HEADER | ERROR | Header comparison key is empty |
| DUPLICATE_HEADER | ERROR | Normalized header name is repeated |
| INCONSISTENT_COLUMNS | ERROR | Data field count differs from the header |
| FIELD_TOO_LARGE | ERROR | Decoded field exceeds configured UTF-16 unit limit |
| RECORD_TOO_LARGE | ERROR | Logical record exceeds source-byte limit |
| TOO_MANY_COLUMNS | ERROR | A parsed record exceeds maximum field count |
| NO_DATA_RECORDS | WARNING | Valid headers but no data rows |

### Parser selection is intentionally not settled

The approved shortlist is **uniVocity** and **FastCSV 4.x**. A separate Maven/JUnit 5 conformance prototype will compare both using identical fixtures, strict dialect and memory-safety gates. No parser is selected or implemented merely by writing this book.

> **Engineering rationale**
>
> **Problem:** Permissive parsing or strict analytical CSV?  
> **Selected approach:** A fixed UTF-8, comma, double-quote dialect with explicit header and length invariants.  
> **Why it fits V1:** A stable language produces deterministic issues and supports repeatable integration tests.  
> **Trade-off:** Some CSVs accepted by permissive spreadsheet readers will be rejected.  
> **Verification:** Run a shared conformance suite against the parser candidates, including source-byte boundary and Unicode fixtures.
