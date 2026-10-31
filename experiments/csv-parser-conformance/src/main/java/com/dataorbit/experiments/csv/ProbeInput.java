package com.dataorbit.experiments.csv;

import java.util.Objects;

public record ProbeInput(String fixtureId, InputOpener opener) {
    public ProbeInput {
        Objects.requireNonNull(fixtureId, "fixtureId");
        if (fixtureId.isBlank()) {
            throw new IllegalArgumentException("fixtureId must not be blank");
        }
        Objects.requireNonNull(opener, "opener");
    }
}
