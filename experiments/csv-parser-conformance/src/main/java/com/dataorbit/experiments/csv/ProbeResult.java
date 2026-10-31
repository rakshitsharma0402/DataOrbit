package com.dataorbit.experiments.csv;

import java.util.List;
import java.util.Objects;

/** CSV outcome and observations; VALID is not proof of parser conformance. */
public record ProbeResult(Outcome outcome, List<ProbeIssue> issues, boolean issuesTruncated,
                          Completion completion, long recordsEmitted, long sourceBytesRead,
                          List<Capability> capabilities) {
    public ProbeResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(completion, "completion");
        issues = List.copyOf(issues);
        capabilities = List.copyOf(capabilities);
        if (recordsEmitted < 0 || sourceBytesRead < 0) {
            throw new IllegalArgumentException("Counters must not be negative");
        }
        if (outcome == Outcome.VALID && (completion == Completion.FATAL_REJECTION
                || issues.stream().anyMatch(issue -> issue.severity() == Severity.ERROR))) {
            throw new IllegalArgumentException("A valid result cannot contain an error or fatal rejection");
        }
    }
}
