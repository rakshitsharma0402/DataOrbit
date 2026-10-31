package com.dataorbit.experiments.csv;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import static com.dataorbit.experiments.csv.ProbeOperationalException.Stage;

/** Shared policy and lifecycle only: this class does not tokenize CSV. */
public final class SharedCsvProbeRunner implements CsvProbeRunner {
    @Override
    public ProbeResult run(ProbeInput input, ProbeLimits limits, CsvParserAdapter adapter,
                           RecordSink sink)
            throws ProbeOperationalException, UnsupportedProbeException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(limits, "limits");
        Objects.requireNonNull(adapter, "adapter");
        Objects.requireNonNull(sink, "sink");
        RunState state = new RunState(limits);
        ObservedInputStream source = null;
        PushbackInputStream bomInput = null;
        TrackingReader reader = null;
        ParserSession session = null;
        Throwable primary = null;
        Stage stage = Stage.INFRASTRUCTURE;
        try {
            Objects.requireNonNull(adapter.identity(), "adapter identity");
            state.capabilities = capabilities(adapter, limits);
            stage = Stage.SOURCE_OPEN;
            source = new ObservedInputStream(
                    Objects.requireNonNull(input.opener().open(), "opened stream"),
                    limits.maxInputBytes());
            bomInput = new PushbackInputStream(source, 3);
            stage = Stage.SOURCE_READ;
            removeInitialBom(bomInput);
            reader = new TrackingReader(new InputStreamReader(bomInput,
                    StandardCharsets.UTF_8.newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT)));
            // A native parser may close its borrowed reader; only the runner closes the chain.
            Reader borrowed = new FilterReader(reader) {
                @Override
                public void close() {}
            };
            stage = Stage.ADAPTER_OPEN;
            session = Objects.requireNonNull(adapter.open(borrowed, limits), "parser session");
            checkReadFailure(source, reader);
            stage = Stage.ADAPTER_READ;
            scan(session, reader, source, sink, state);
            return state.result(source.count);
        } catch (ProbeOperationalException failure) {
            primary = failure;
            throw failure;
        } catch (IOException | CsvRejectionException | UnsupportedProbeException
                | RuntimeException failure) {
            if (source != null && source.readFailure != null) {
                ProbeOperationalException operational =
                        new ProbeOperationalException(Stage.SOURCE_READ, source.readFailure);
                primary = operational;
                throw operational;
            }
            if (source != null && source.limitFailure != null) {
                state.reject(DiagnosticCode.INPUT_BYTE_LIMIT_EXCEEDED,
                        OptionalLong.empty(), source.limitFailure);
                return state.result(source.count);
            }
            if (reader != null && reader.failure instanceof CharacterCodingException) {
                state.reject(DiagnosticCode.MALFORMED_UTF8,
                        OptionalLong.empty(), reader.failure);
                return state.result(source.count);
            }
            if (failure instanceof UnsupportedProbeException unsupported) {
                primary = unsupported;
                throw unsupported;
            }
            if (failure instanceof CsvRejectionException rejection
                    && !hasIoCause(rejection)) {
                state.reject(rejection.code(), OptionalLong.of(state.ordinal + 1), rejection);
                return state.result(source.count);
            }
            ProbeOperationalException operational = new ProbeOperationalException(stage, failure);
            primary = operational;
            throw operational;
        } catch (Error failure) {
            primary = failure;
            throw failure;
        } finally {
            AutoCloseable inputResource = reader != null ? reader
                    : bomInput != null ? bomInput : source;
            List<Throwable> failures = closeAll(session, inputResource);
            if (!failures.isEmpty()) {
                if (primary != null) {
                    suppress(primary, failures);
                } else {
                    Throwable cause = state.fatalContentFailure != null
                            ? state.fatalContentFailure : failures.removeFirst();
                    if (cause instanceof Error error) {
                        suppress(error, failures);
                        throw error;
                    }
                    ProbeOperationalException cleanup =
                            new ProbeOperationalException(Stage.CLEANUP, cause);
                    suppress(cleanup, failures);
                    throw cleanup;
                }
            }
        }
    }

    private static void scan(ParserSession session, TrackingReader reader,
                             ObservedInputStream source, RecordSink sink, RunState state)
            throws IOException, CsvRejectionException, ProbeOperationalException {
        while (true) {
            Optional<TokenizedRecord> next = Objects.requireNonNull(session.next(), "next record");
            checkReadFailure(source, reader);
            if (next.isEmpty()) {
                // Finish decoding/budget checks before reporting a premature adapter EOF.
                // Native parser-owned buffers are not visible to this reader-level check.
                char[] buffer = new char[4096];
                boolean unreadCharacters = false;
                int count;
                while ((count = reader.read(buffer, 0, buffer.length)) != -1) {
                    unreadCharacters |= count > 0;
                }
                checkReadFailure(source, reader);
                if (unreadCharacters) {
                    throw new ProbeOperationalException(Stage.ADAPTER_READ,
                            new IllegalStateException("Parser reported EOF with unread decoded input."));
                }
                if (state.headerWidth == null) {
                    state.error(DiagnosticCode.MISSING_HEADER, OptionalLong.empty());
                } else if (!state.sawData) {
                    state.issues.add(new ProbeIssue(DiagnosticCode.NO_DATA_RECORDS,
                            OptionalLong.of(1)));
                }
                return;
            }
            TokenizedRecord token = next.orElseThrow();
            if (token.ignorableBlank()) {
                continue;
            }
            long ordinal = ++state.ordinal;
            List<String> fields = token.fields();
            if (fields.size() > state.limits.maxColumns()) {
                state.reject(DiagnosticCode.COLUMN_LIMIT_EXCEEDED,
                        OptionalLong.of(ordinal), null);
                return;
            }
            for (String field : fields) {
                if (SharedCsvPolicy.exceedsCodePoints(field, state.limits.maxFieldCodePoints())) {
                    state.reject(DiagnosticCode.FIELD_CODE_POINT_LIMIT_EXCEEDED,
                            OptionalLong.of(ordinal), null);
                    return;
                }
            }
            if (state.headerWidth == null) {
                if (fields.isEmpty()) {
                    state.reject(DiagnosticCode.INVALID_HEADER, OptionalLong.of(ordinal), null);
                    return;
                }
                HashSet<String> names = new HashSet<>();
                for (String field : fields) {
                    if (!names.add(SharedCsvPolicy.normalizeHeader(field))) {
                        state.reject(DiagnosticCode.DUPLICATE_HEADER,
                                OptionalLong.of(ordinal), null);
                        return;
                    }
                }
                state.headerWidth = fields.size();
            } else {
                state.sawData = true;
                if (fields.size() != state.headerWidth) {
                    state.error(DiagnosticCode.COLUMN_COUNT_MISMATCH, OptionalLong.of(ordinal));
                    continue;
                }
            }
            try {
                sink.accept(new ProbeRecord(ordinal, fields));
            } catch (IOException | RuntimeException failure) {
                throw new ProbeOperationalException(Stage.SINK, failure);
            }
            state.emitted++;
        }
    }

    private static void removeInitialBom(PushbackInputStream input) throws IOException {
        byte[] prefix = new byte[3];
        int length = 0;
        int first = input.read();
        if (first == -1) {
            return;
        }
        prefix[length++] = (byte) first;
        if (first == 0xEF) {
            while (length < prefix.length) {
                int value = input.read();
                if (value == -1) {
                    break;
                }
                prefix[length++] = (byte) value;
            }
        }
        if (length != 3 || prefix[0] != (byte) 0xEF || prefix[1] != (byte) 0xBB
                || prefix[2] != (byte) 0xBF) {
            input.unread(prefix, 0, length);
        }
    }

    private static void checkReadFailure(ObservedInputStream source, TrackingReader reader)
            throws IOException {
        if (source.readFailure != null) {
            throw source.readFailure;
        }
        if (source.limitFailure != null) {
            throw source.limitFailure;
        }
        if (reader.failure != null) {
            throw reader.failure;
        }
    }

    private static boolean hasIoCause(Throwable failure) {
        HashSet<Throwable> seen = new HashSet<>();
        for (Throwable cause = failure.getCause(); cause != null && seen.add(cause);
                cause = cause.getCause()) {
            if (cause instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private static List<Capability> capabilities(CsvParserAdapter adapter, ProbeLimits limits) {
        List<Capability> result = new ArrayList<>(List.copyOf(adapter.capabilities(limits)));
        HashSet<String> names = new HashSet<>();
        for (Capability capability : result) {
            if (!names.add(capability.requirement())) {
                throw new IllegalArgumentException("Duplicate adapter capability");
            }
            if (capability.requirement().startsWith("SHARED_")) {
                throw new IllegalArgumentException("Shared capability names are reserved for the runner");
            }
        }
        for (String requirement : List.of("FIELD_CODE_POINT_EARLY_ENFORCEMENT",
                "RECORD_SOURCE_BYTE_EARLY_ENFORCEMENT", "COLUMN_EARLY_ENFORCEMENT")) {
            if (names.add(requirement)) {
                result.add(new Capability(requirement, CapabilityStatus.UNVERIFIED,
                        "Requires adapter-specific evidence; shared post-record checks are insufficient."));
            }
        }
        result.add(new Capability("SHARED_INPUT_BYTE_GUARD", CapabilityStatus.SUPPORTED,
                "Shared stream rejects after observing at most one byte over the input budget."));
        result.add(new Capability("SHARED_DIAGNOSTIC_RETENTION", CapabilityStatus.SUPPORTED,
                "Bounded error-priority collector with fixed diagnostic messages."));
        return List.copyOf(result);
    }

    private static List<Throwable> closeAll(AutoCloseable... resources) {
        List<Throwable> failures = new ArrayList<>();
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Throwable failure) {
                    // Cleanup only: preserve even Errors, attempt remaining closes, then propagate.
                    failures.add(failure);
                }
            }
        }
        return failures;
    }

    private static void suppress(Throwable primary, List<Throwable> failures) {
        for (Throwable failure : failures) {
            if (failure != primary) {
                primary.addSuppressed(failure);
            }
        }
    }

    private static final class RunState {
        private final ProbeLimits limits;
        private final IssueCollector issues;
        private List<Capability> capabilities = List.of();
        private Integer headerWidth;
        private boolean sawData;
        private long ordinal;
        private long emitted;
        private Completion completion = Completion.END_OF_INPUT;
        private Throwable fatalContentFailure;

        private RunState(ProbeLimits limits) {
            this.limits = limits;
            issues = new IssueCollector(limits.maxIssues());
        }

        private void error(DiagnosticCode code, OptionalLong record) {
            issues.add(new ProbeIssue(code, record));
        }

        private void reject(DiagnosticCode code, OptionalLong record, Throwable cause) {
            error(code, record);
            fatalContentFailure = cause != null ? cause : new CsvRejectionException(code);
            completion = Completion.FATAL_REJECTION;
        }

        private ProbeResult result(long bytes) {
            return new ProbeResult(issues.hasErrors() ? Outcome.INVALID : Outcome.VALID,
                    issues.snapshot(), issues.truncated(), completion, emitted, bytes, capabilities);
        }
    }

    private static final class InputBudgetExceeded extends IOException {
        private InputBudgetExceeded() {
            super(DiagnosticCode.INPUT_BYTE_LIMIT_EXCEEDED.message());
        }
    }

    private static final class ObservedInputStream extends InputStream {
        private final InputStream delegate;
        private final long limit;
        private long count;
        private IOException readFailure;
        private InputBudgetExceeded limitFailure;

        private ObservedInputStream(InputStream delegate, long limit) {
            this.delegate = delegate;
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            if (limitFailure != null) {
                throw limitFailure;
            }
            int value;
            try {
                value = delegate.read();
            } catch (IOException failure) {
                if (readFailure == null) {
                    readFailure = failure;
                }
                throw failure;
            }
            if (value != -1) {
                observed(1);
            }
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, bytes.length);
            if (length == 0) {
                return 0;
            }
            if (limitFailure != null) {
                throw limitFailure;
            }
            long remaining = limit - count;
            int permitted = remaining >= length ? length : (int) remaining + 1;
            int read;
            try {
                read = delegate.read(bytes, offset, permitted);
            } catch (IOException failure) {
                if (readFailure == null) {
                    readFailure = failure;
                }
                throw failure;
            }
            if (read > 0) {
                observed(read);
            }
            return read;
        }

        private void observed(int bytes) throws InputBudgetExceeded {
            // A stream cannot represent more than Long.MAX_VALUE observed bytes in this API.
            if (count > Long.MAX_VALUE - bytes) {
                limitFailure = new InputBudgetExceeded();
                throw limitFailure;
            }
            count += bytes;
            if (count > limit) {
                limitFailure = new InputBudgetExceeded();
                throw limitFailure;
            }
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static final class TrackingReader extends Reader {
        private final Reader delegate;
        private IOException failure;

        private TrackingReader(Reader delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read(char[] chars, int offset, int length) throws IOException {
            try {
                return delegate.read(chars, offset, length);
            } catch (IOException caught) {
                if (failure == null) {
                    failure = caught;
                }
                throw caught;
            }
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
