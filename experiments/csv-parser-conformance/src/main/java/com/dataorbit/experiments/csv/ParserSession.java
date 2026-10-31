package com.dataorbit.experiments.csv;

import java.io.IOException;
import java.util.Optional;

public interface ParserSession extends AutoCloseable {
    /** Empty means tokenization has finished; blank records have a separate signal. */
    Optional<TokenizedRecord> next() throws IOException, CsvRejectionException;

    @Override
    void close() throws IOException;
}
