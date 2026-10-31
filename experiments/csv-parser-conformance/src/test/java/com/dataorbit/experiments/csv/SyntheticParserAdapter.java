package com.dataorbit.experiments.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Scripts events and optionally consumes characters; it never tokenizes CSV. */
final class SyntheticParserAdapter implements CsvParserAdapter {
    @FunctionalInterface
    interface Step {
        Optional<TokenizedRecord> perform(Reader reader) throws IOException, CsvRejectionException;
    }

    final List<Step> script = new ArrayList<>();
    final List<Capability> reportedCapabilities = new ArrayList<>(List.of(
            new Capability("STRICT_CSV_TOKENIZATION", CapabilityStatus.UNSUPPORTED,
                    "Synthetic adapter scripts records; it is not a CSV parser."),
            new Capability("FIELD_CODE_POINT_EARLY_ENFORCEMENT", CapabilityStatus.UNVERIFIED,
                    "Scripted fields are already materialized."),
            new Capability("RECORD_SOURCE_BYTE_EARLY_ENFORCEMENT", CapabilityStatus.UNVERIFIED,
                    "No raw record source positions are available.")));
    final StringBuilder decoded = new StringBuilder();
    boolean consumeReader;
    boolean closeBorrowedReader;
    IOException openFailure;
    IOException closeFailure;
    UnsupportedProbeException unsupported;
    int nextCalls;
    int closeCalls;
    ProbeLimits receivedLimits;

    SyntheticParserAdapter record(String... fields) {
        TokenizedRecord record = new TokenizedRecord(false, List.of(fields));
        script.add(reader -> Optional.of(record));
        return this;
    }

    SyntheticParserAdapter blank() {
        script.add(reader -> Optional.of(new TokenizedRecord(true, List.of())));
        return this;
    }

    @Override
    public ParserIdentity identity() {
        return new ParserIdentity("synthetic-test-only", "script-v1");
    }

    @Override
    public List<Capability> capabilities(ProbeLimits limits) {
        return reportedCapabilities;
    }

    @Override
    public ParserSession open(Reader input, ProbeLimits limits)
            throws IOException, UnsupportedProbeException {
        receivedLimits = limits;
        if (unsupported != null) {
            throw unsupported;
        }
        if (openFailure != null) {
            throw openFailure;
        }
        return new ParserSession() {
            private int position;
            private boolean consumed;

            @Override
            public Optional<TokenizedRecord> next() throws IOException, CsvRejectionException {
                nextCalls++;
                if (consumeReader && !consumed) {
                    consumed = true;
                    char[] buffer = new char[7];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        decoded.append(buffer, 0, count);
                    }
                }
                if (position == script.size()) {
                    return Optional.empty();
                }
                return script.get(position++).perform(input);
            }

            @Override
            public void close() throws IOException {
                closeCalls++;
                if (closeBorrowedReader) {
                    input.close();
                }
                if (closeFailure != null) {
                    throw closeFailure;
                }
            }
        };
    }
}
