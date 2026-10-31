# CSV parser conformance experiment

Standalone experiment for [DataOrbit Issue #1](https://github.com/rakshitsharma0402/DataOrbit/issues/1).
Its eventual purpose is to compare uniVocity-parsers and FastCSV 4.x against the
approved CSV conformance and resource-safety requirements.

Checkpoint 1 established the Maven/JUnit 5 smoke harness. Checkpoint 2A adds shared
probe contracts, a streaming runner, and focused shared-infrastructure tests.
The experiment builds independently of the production backend and frontend.

## Checkpoint 2A scope

- `CsvProbeRunner`, `CsvParserAdapter`, and `ParserSession` separate shared policy
  from parser-specific tokenization. Supporting records make immutable snapshots;
  operational and unsupported failures use typed exceptions.
- `SharedCsvProbeRunner` owns raw input/session cleanup, strict UTF-8 and initial-BOM
  handling, original-byte counting, header normalization/validation, column-count
  policy, logical ordinals, synchronous record delivery, and final validity.
- A bounded issue collector uses fixed messages, prioritizes errors, and records
  truncation only when observed diagnostics are omitted or evicted.
- A streaming whole-file byte guard rejects after observing at most one byte over
  the inclusive budget. `INPUT_BYTE_LIMIT_EXCEEDED` was separately approved by the
  owner for this guard. Candidate input limit: 250,000,000 bytes (decimal 250 MB).
- The test-only `SyntheticParserAdapter` scripts records, blanks, and failures.
  It can consume the actual supplied `Reader` to exercise strict decoding, but it
  is not a CSV tokenizer and does not prove either real parser's conformance.

See the [reconstructed requirements and subsequent owner approvals](../../docs/decisions/csv-validation-and-parser-experiment.md).

## Prerequisites

- JDK 21 or a newer JDK that supports `--release 21`.
- Maven 3.9 or newer.
- Internet access on the first run to download artifacts from Maven Central.

The POM sets `maven.compiler.release` to `21`, constraining Java language features,
standard-library APIs, and bytecode to Java 21 even when Maven runs on a newer JDK.
This release target applies only to this experiment.
Use `mvn --version` to check the JDK Maven actually uses.

## Run tests

From the repository root:

```sh
cd experiments/csv-parser-conformance
mvn clean test
```

Expected result: `BUILD SUCCESS`, with zero failures, errors, or skipped tests.
The tests cover strict decoding/BOM, headers, widths/ordinals, immutable synchronous
delivery, diagnostic retention, whole-file byte budgets, capabilities, and lifecycle
failures including suppressed cleanup errors. Surefire fails if no tests are discovered.

Generated classes and test reports are written to `target/`, which is already
ignored by the repository. Surefire reports are under `target/surefire-reports/`.

## Maven configuration

This POM has no parent or production-module dependencies. Its only dependency is
JUnit Jupiter, scoped to tests. The plugins used by `clean test` have explicit versions:

| Component | Version | Purpose |
| --- | --- | --- |
| [JUnit Jupiter](https://docs.junit.org/5.13.4/user-guide/index.html) | 5.13.4 | JUnit 5 API and test engine |
| [Maven Clean Plugin](https://maven.apache.org/plugins/maven-clean-plugin/plugin-info.html) | 3.5.0 | Remove generated build output |
| [Maven Resources Plugin](https://maven.apache.org/plugins-archives/maven-resources-plugin-3.3.1/plugin-info.html) | 3.3.1 | Standard Maven resource lifecycle |
| [Maven Compiler Plugin](https://maven.apache.org/plugins-archives/maven-compiler-plugin-3.14.1/examples/set-compiler-release.html) | 3.14.1 | Compile with Java release 21 |
| [Maven Surefire Plugin](https://maven.apache.org/surefire-archives/surefire-3.5.4/maven-surefire-plugin/examples/junit-platform.html) | 3.5.4 | Discover and run JUnit 5 tests |

Versions were checked against the linked official documentation. The POM and its
dependencies are unchanged by Checkpoint 2A. Verification uses OpenJDK 25 with
`--release 21`; execution on a JDK 21 runtime has not been separately verified.

## Safety evidence and remaining work

| Requirement | Current implementation/evidence |
| --- | --- |
| Whole-file bytes | Shared streaming guard, tested with small exact/over-limit inputs; original BOM counts toward this budget. |
| Diagnostic issues | Bounded retention, candidate 100; zero retention is also supported. Fixed catalog messages never interpolate uploaded fields or native exception text. |
| Field code points | Candidate 1,000,000 after quote-unescaping. Shared checks on already-parsed values are policy tests only; safe early enforcement remains UNVERIFIED. |
| Logical-record source bytes | Candidate 4,194,304 inclusive, including original quote/delimiter/embedded-newline bytes and excluding record terminators, initial BOM and ignored blanks. Early enforcement/accounting remains UNVERIFIED and adapter-specific. |
| Columns | Candidate 1,000. Shared post-record policy check exists; it does not prove safe pre-allocation enforcement. |
| CSV syntax and tokenization | UNSUPPORTED by the synthetic adapter; real-library quoting, newline/blank recognition and source-byte boundaries are deferred. |

Capabilities do not have a PASS status. A synthetic VALID result and a green shared
test suite do not establish real-parser conformance or complete Issue #1. A future
case ledger/acceptance gate must report unsupported/unverified protection without
crediting it as passing. Timeouts and heap/crash experiments also remain future work.

After a session reports EOF, the runner finishes strict decoding and whole-file
byte-budget verification. If decoded characters remained unread, it reports an
operational adapter-contract violation at ADAPTER_READ, not VALID or INVALID CSV.
Genuine source I/O failures, malformed UTF-8 and byte-budget violations encountered
during verification take priority over that contract violation. The runner does
not tokenize the tail. Characters already read into a parser-owned internal buffer
are invisible to this check; real-adapter conformance tests must still prove complete
tokenization. Fatal content errors stop without draining; byte counts can include
read-ahead and are not logical-record sizes or precise error positions.

Adapters borrow a close-shielded reader; the runner closes sessions and the input
chain. The caller owns the sink. Source/sink/infrastructure failures remain operational;
malformed decoding is content rejection. Recoverable width diagnostics affect final
validity but are not fatal exceptions. With no primary operational or fatal content
failure, a cleanup exception is the cause of Stage.CLEANUP and later cleanup failures
are suppressed. After fatal content rejection, the fatal failure remains the cause
and all cleanup failures are suppressed. An existing operational failure remains
primary. All appropriate closes are attempted; cleanup cannot return a false success.

No uniVocity-parsers or FastCSV dependencies/adapters, shared CSV tokenizer, generic
source-position map, production code, tier routing, or large test artifacts are added.
The planned comparison of uniVocity-parsers and FastCSV 4.x remains open; no production
parser is selected. No CSV byte-normalizing transformation is performed.
