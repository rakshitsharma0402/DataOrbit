<!-- DataOrbit V1 Architecture & Engineering Handbook · Reference material · PDF page 76. Converted from ../DataOrbit-V1-Handbook.pdf, which remains authoritative. Diagrams appear only in the PDF. -->

# CSV conformance workbook

*An extended, implementation-ready collection of strict-language fixtures and parser evaluation gates.*

## How to run the parser experiment fairly

Keep the parser comparison outside the production reactor, using identical raw-byte fixtures and a stable JUnit 5 harness. Implement adapters for uniVocity and FastCSV 4.x that expose the exact same behavior to the tests. Track four explicit outcomes per fixture: **PASS**, **FAIL**, **PASS_WITH_ADAPTER** and **UNVERIFIED**. A candidate is unsuitable if it cannot safely enforce the required strict syntax or limits; speed alone cannot compensate for semantic mismatch.

Reproducible performance runs should use representative 10 MB, 100 MB and 250 MB files, alongside pathological quote, width and encoding cases. Record Java version, heap cap, parser and adapter versions, fixture checksums, elapsed time, max observed heap and failure status. The test corpus should include raw byte arrays, not only Java Strings, because source-byte accounting and invalid UTF-8 cannot be proven with decoded strings alone.

## Encoding and source-position fixtures

| Fixture | Constructed input | Expected proof |
|---|---|---|
| ENC-A | ASCII header and one data record | VALID baseline |
| ENC-B | One leading EF BB BF before header | VALID, no BOM warning |
| ENC-C | BOM bytes in middle of content | Ordinary data or strict context handling; never silently strip as initial BOM |
| ENC-D | Invalid UTF-8 continuation byte | INVALID_ENCODING |
| ENC-E | Truncated multi-byte UTF-8 sequence | INVALID_ENCODING |
| ENC-F | Supplementary Unicode in field | Count two UTF-16 units per code point |
| LIN-A | LF-only file | VALID with correct physical lines |
| LIN-B | CRLF-only file | VALID with correct physical lines |
| LIN-C | CR-only file | VALID with correct physical lines |
| LIN-D | Mixed line endings | VALID without style warning |
| LIN-E | No final line separator | Final record parsed correctly |
| LIN-F | Quoted newline and blank physical lines | One logical record, accurate starting line |

## Header and dialect fixtures

| Fixture | Constructed input | Expected proof |
|---|---|---|
| HDR-A | Empty file | EMPTY_FILE |
| HDR-B | Only ignorable spaces, tabs and physical blank lines | EMPTY_FILE |
| HDR-C | One valid header line; no data rows | VALID + NO_DATA_RECORDS |
| HDR-D | Empty normalized header cell | EMPTY_HEADER |
| HDR-E | Case-insensitive duplicate headers | DUPLICATE_HEADER |
| HDR-F | NFC-equivalent composed/decomposed header | DUPLICATE_HEADER |
| HDR-G | Different accents not canonically equivalent | Do not collapse into false duplicate |
| QUO-A | Quoted comma | Field count does not increase |
| QUO-B | Doubled embedded double quote | Single literal quote in decoded field |
| QUO-C | Unclosed quoted field | MALFORMED_CSV at record starting line |
| QUO-D | Stray double quote in unquoted field | MALFORMED_CSV |
| QUO-E | Space before opening quote | MALFORMED_CSV |
| QUO-F | Space after closing quote | MALFORMED_CSV |
| QUO-G | Backslash quote escape | Rejected; not treated as CSV quote escape |
| QUO-H | Single quotes in unquoted text | Accepted as ordinary characters |

## Limits, precedence and diagnostic fixtures

| Fixture | Constructed input | Expected proof |
|---|---|---|
| LIM-A | Exactly 1,000,000 decoded UTF-16 units in one field | Within field ceiling |
| LIM-B | One extra UTF-16 unit | FIELD_TOO_LARGE before unsafe allocation |
| LIM-C | Exactly 4,194,304 original source bytes in one record | Within record ceiling |
| LIM-D | One extra original source byte | RECORD_TOO_LARGE |
| LIM-E | Quoted CRLF inside field | Both embedded bytes count toward record |
| LIM-F | External CRLF terminator | Terminator excluded from record bytes |
| LIM-G | Ignored external blank line | Ignored line not counted as a logical record |
| LIM-H | Exactly 1,000 fields including trailing empties | Within field-count ceiling |
| LIM-I | 1,001 fields | TOO_MANY_COLUMNS takes precedence over mismatch |
| LIM-J | Wrong row width but within maximum | INCONSISTENT_COLUMNS |
| DIA-A | 100 warning candidates followed by a real error | Continue scanning and retain later ERROR by priority |
| DIA-B | Invalid established with full report budget | issuesTruncated where further findings may exist |
| DIA-C | Fatal parse error before report budget | Stop safely without invented downstream issues |
| DIA-D | Injected storage read exception | Operational exception, not an INVALID CSV report |

## A precise byte-counting thought experiment

Consider the source record `"a,b",c` followed by CRLF. The comma inside the quotes is content, while the comma outside is a separator. The original source record includes both double quotes, the embedded comma, the external comma and every field byte. The final CRLF terminator is excluded from that record’s raw-byte total. If the quoted field instead contains an embedded CRLF, those two bytes count because they occur within the logical record. The optional initial file BOM is excluded from the first record’s count.

Field length is measured after decoding: doubled quote syntax contributes one decoded quote character, while the source-byte record counter counts both source quote bytes. This is why DataOrbit cannot use `String.length()` as a substitute for the original-byte limit. A strict tokenizer or surrounding byte-accounting layer must make both units available safely.

### No parser has been selected by this workbook

These fixtures are a specification for work to perform during implementation. A PASS row is not assigned until the candidate parser adapter actually passes the JUnit 5 tests under the documented environment.
