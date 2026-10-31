package com.dataorbit.experiments.csv;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Bounded retention, independent of whether any observed error was retained. */
final class IssueCollector {
    private final int limit;
    private final ArrayDeque<ProbeIssue> errors = new ArrayDeque<>();
    private final ArrayDeque<ProbeIssue> warnings = new ArrayDeque<>();
    private boolean hasErrors;
    private boolean truncated;

    IssueCollector(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Issue limit must not be negative");
        }
        this.limit = limit;
    }

    void add(ProbeIssue issue) {
        Objects.requireNonNull(issue, "issue");
        boolean error = issue.severity() == Severity.ERROR;
        hasErrors |= error;
        if (errors.size() + warnings.size() == limit) {
            truncated = true;
            if (!error || warnings.isEmpty()) {
                return;
            }
            warnings.removeLast();
        }
        (error ? errors : warnings).addLast(issue);
    }

    List<ProbeIssue> snapshot() {
        List<ProbeIssue> result = new ArrayList<>();
        result.addAll(errors);
        result.addAll(warnings);
        return List.copyOf(result);
    }

    boolean hasErrors() {
        return hasErrors;
    }

    boolean truncated() {
        return truncated;
    }
}
