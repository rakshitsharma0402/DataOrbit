package com.dataorbit.experiments.csv;

/** Requested limits; their presence does not establish adapter enforcement. */
public record ProbeLimits(long maxFieldCodePoints, long maxRecordSourceBytes,
                          int maxColumns, int maxIssues, long maxInputBytes) {
    public ProbeLimits {
        if (maxFieldCodePoints < 1 || maxRecordSourceBytes < 1 || maxColumns < 1
                || maxIssues < 0 || maxInputBytes < 1) {
            throw new IllegalArgumentException("Limits must be positive; maxIssues may be zero");
        }
    }

    public static ProbeLimits candidates() {
        return new ProbeLimits(1_000_000, 4_194_304, 1_000, 100, 250_000_000);
    }
}
