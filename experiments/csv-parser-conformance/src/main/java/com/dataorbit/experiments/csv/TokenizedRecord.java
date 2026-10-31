package com.dataorbit.experiments.csv;

import java.util.List;

/** Adapter-produced parsed values, not raw CSV syntax or source positions. */
public record TokenizedRecord(boolean ignorableBlank, List<String> fields) {
    public TokenizedRecord {
        fields = List.copyOf(fields);
        if (ignorableBlank && !fields.isEmpty()) {
            throw new IllegalArgumentException("A blank signal must have no fields");
        }
    }
}
