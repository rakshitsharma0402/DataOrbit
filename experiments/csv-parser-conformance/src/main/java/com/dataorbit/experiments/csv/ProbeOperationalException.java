package com.dataorbit.experiments.csv;

import java.util.Objects;

public final class ProbeOperationalException extends Exception {
    public enum Stage {
        SOURCE_OPEN, SOURCE_READ, ADAPTER_OPEN, ADAPTER_READ, SINK, CLEANUP, INFRASTRUCTURE
    }

    private final Stage stage;

    public ProbeOperationalException(Stage stage, Throwable cause) {
        super("Probe execution failed during " + Objects.requireNonNull(stage, "stage"),
                Objects.requireNonNull(cause, "cause"));
        this.stage = stage;
    }

    public Stage stage() {
        return stage;
    }
}
