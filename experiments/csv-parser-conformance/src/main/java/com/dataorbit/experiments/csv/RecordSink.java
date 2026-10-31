package com.dataorbit.experiments.csv;

import java.io.IOException;

@FunctionalInterface
public interface RecordSink {
    /** Synchronous observation; the caller owns the sink, not the input resources. */
    void accept(ProbeRecord record) throws IOException;
}
