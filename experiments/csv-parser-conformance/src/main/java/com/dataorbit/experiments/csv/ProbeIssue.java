package com.dataorbit.experiments.csv;

import java.util.Objects;
import java.util.OptionalLong;

public record ProbeIssue(DiagnosticCode code, OptionalLong recordOrdinal) {
    public ProbeIssue {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(recordOrdinal, "recordOrdinal");
        if (recordOrdinal.isPresent() && recordOrdinal.getAsLong() < 1) {
            throw new IllegalArgumentException("Record ordinal must be positive when present");
        }
    }

    public Severity severity() {
        return code.severity();
    }

    public String message() {
        return code.message();
    }
}
