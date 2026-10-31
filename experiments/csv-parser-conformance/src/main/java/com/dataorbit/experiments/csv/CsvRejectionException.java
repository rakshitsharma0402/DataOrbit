package com.dataorbit.experiments.csv;

import java.util.Objects;

public final class CsvRejectionException extends Exception {
    private final DiagnosticCode code;

    public CsvRejectionException(DiagnosticCode code) {
        this(code, null);
    }

    public CsvRejectionException(DiagnosticCode code, Throwable cause) {
        super(Objects.requireNonNull(code, "code").message(), cause);
        if (code.severity() != Severity.ERROR) {
            throw new IllegalArgumentException("A rejection must be an error");
        }
        this.code = code;
    }

    public DiagnosticCode code() {
        return code;
    }
}
