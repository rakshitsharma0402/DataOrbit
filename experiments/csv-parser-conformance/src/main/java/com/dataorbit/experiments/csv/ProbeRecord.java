package com.dataorbit.experiments.csv;

import java.util.List;

public record ProbeRecord(long ordinal, List<String> fields) {
    public ProbeRecord {
        if (ordinal < 1) {
            throw new IllegalArgumentException("Ordinal must be positive");
        }
        fields = List.copyOf(fields);
    }
}
