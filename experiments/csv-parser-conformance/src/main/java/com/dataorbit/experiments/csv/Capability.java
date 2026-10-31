package com.dataorbit.experiments.csv;

import java.util.Objects;

public record Capability(String requirement, CapabilityStatus status, String evidence) {
    public Capability {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(evidence, "evidence");
        if (requirement.isBlank() || evidence.isBlank()) {
            throw new IllegalArgumentException("Requirement and evidence must not be blank");
        }
    }
}
