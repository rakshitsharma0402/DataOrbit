# CSV parser conformance experiment

Standalone experiment for [DataOrbit Issue #1](https://github.com/rakshitsharma0402/DataOrbit/issues/1).
Its eventual purpose is to compare uniVocity-parsers and FastCSV 4.x against the
approved CSV conformance and resource-safety requirements.

Checkpoint 1 establishes only a working Maven/JUnit 5 test harness with one smoke
test. Parser dependencies, adapters, CSV fixtures, and conformance tests are deferred.
The experiment builds independently of the production backend and frontend.

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

Expected result: `BUILD SUCCESS`, with one test run and zero failures, errors, or
skipped tests. The smoke test verifies JUnit Jupiter discovery and assertion
execution. Surefire fails the build if no tests are discovered.

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

Versions were checked against the linked official documentation. Parser selection
and the remaining Issue #1 acceptance criteria require later checkpoints.
