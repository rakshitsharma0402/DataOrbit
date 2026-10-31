package com.dataorbit.experiments.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface CsvParserAdapter {
    ParserIdentity identity();

    List<Capability> capabilities(ProbeLimits limits);

    /** Borrows the reader; the runner retains ownership of the input. */
    ParserSession open(Reader decodedInput, ProbeLimits limits)
            throws IOException, UnsupportedProbeException;
}
