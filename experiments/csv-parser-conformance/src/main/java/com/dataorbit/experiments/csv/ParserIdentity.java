package com.dataorbit.experiments.csv;

import java.util.Objects;

public record ParserIdentity(String library, String version) {
    public ParserIdentity {
        Objects.requireNonNull(library, "library");
        Objects.requireNonNull(version, "version");
        if (library.isBlank() || version.isBlank()) {
            throw new IllegalArgumentException("Library and version must not be blank");
        }
    }
}
