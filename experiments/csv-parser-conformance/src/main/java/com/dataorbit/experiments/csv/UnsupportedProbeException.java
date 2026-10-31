package com.dataorbit.experiments.csv;

import java.util.Objects;

public final class UnsupportedProbeException extends Exception {
    private final String requirement;

    public UnsupportedProbeException(String requirement) {
        super("The requested probe capability is unsupported.");
        this.requirement = Objects.requireNonNull(requirement, "requirement");
        if (requirement.isBlank()) {
            throw new IllegalArgumentException("Requirement must not be blank");
        }
    }

    public String requirement() {
        return requirement;
    }
}
