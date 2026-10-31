package com.dataorbit.experiments.csv;

public interface CsvProbeRunner {
    ProbeResult run(ProbeInput input, ProbeLimits limits, CsvParserAdapter adapter, RecordSink sink)
            throws ProbeOperationalException, UnsupportedProbeException;
}
