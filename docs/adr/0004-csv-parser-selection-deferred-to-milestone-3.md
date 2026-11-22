# ADR-0004: CSV parser selection deferred to Milestone 3

- Status: Accepted (decision deferred; to be completed by an update at the end of the experiment)
- Date: 2026-11-23
- Handbook references: "CSV validation as a bounded language", "Parser selection is intentionally not settled", "CSV conformance workbook".

## Context

V1 defines a strict CSV dialect: strict UTF-8 with one optional leading BOM, comma-only separator, strict quote boundaries, header equality via NFC, and four independent limits. The record limit counts **original UTF-8 source bytes**, while the field limit counts **decoded UTF-16 units**. Most parsers are lenient by default and operate on decoded characters, so strictness and byte accounting are exactly where a library can silently fail. The approved shortlist is uniVocity and FastCSV 4.x.

## Decision

- No parser is selected and none is added to production modules until Milestone 3.
- Milestone 3 begins with a time-boxed experiment in `experiments/csv-parser-conformance/`, outside the nine production Maven modules, using JUnit 5 and the fixtures in the handbook's CSV conformance workbook, stored as **raw byte arrays**.
- Each fixture records PASS, FAIL, PASS_WITH_ADAPTER or UNVERIFIED per candidate. A candidate must pass strictness and resource-safety gates (no unbounded allocation, limits enforced during tokenization) before any performance comparison. Performance runs use 10 MB, 100 MB and 250 MB files plus pathological cases, recording Java version, heap cap, library versions, fixture checksums, time and peak heap.
- Source-byte accounting is expected to need a counting layer around the input stream, independent of the parser.
- The chosen adapter's tests become the production `FileValidator` conformance suite.
- When the experiment finishes, this ADR is updated with the outcome (or superseded by a new ADR).

## If neither candidate passes the gates

Writing a small purpose-built tokenizer would depart from the approved shortlist. That is an `architecture change`: record it in a new ADR and review it before implementing.

## Consequences

Milestones 1 and 2 are unaffected. Milestone 3 has a research issue first, then implementation issues that depend on its outcome.

## Verification

Conformance suite results table committed with the experiment; all CSV-* test IDs in the verification catalog pass against the selected adapter.
