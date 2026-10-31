package com.dataorbit.experiments.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SharedCsvProbeRunnerTest {
    private final CsvProbeRunner runner = new SharedCsvProbeRunner();
    private final ProbeLimits candidates = ProbeLimits.candidates();

    private ProbeResult run(SyntheticParserAdapter adapter, RecordSink sink) throws Exception {
        return runner.run(new ProbeInput("scripted", () -> new ByteArrayInputStream(new byte[0])),
                candidates, adapter, sink);
    }

    private ProbeResult run(TrackingInput input, ProbeLimits limits, SyntheticParserAdapter adapter,
                            RecordSink sink) throws Exception {
        return runner.run(new ProbeInput("tracked", () -> input), limits, adapter, sink);
    }

    private static List<DiagnosticCode> codes(ProbeResult result) {
        return result.issues().stream().map(ProbeIssue::code).toList();
    }

    @Test
    void strictDecodingStripsOnlyTheInitialBomAndPreservesLineBreaks() throws Exception {
        String text = "h\r\né\uD83D\uDE00\r\n\uFEFF";
        byte[] bytes = ("\uFEFF" + text).getBytes(StandardCharsets.UTF_8);
        TrackingInput input = new TrackingInput(bytes);
        input.maxChunk = 1;
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h").record("value");
        adapter.consumeReader = true;
        List<ProbeRecord> delivered = new ArrayList<>();
        ProbeResult result = run(input, candidates, adapter, delivered::add);
        assertEquals(Outcome.VALID, result.outcome());
        assertEquals(text, adapter.decoded.toString());
        assertEquals(bytes.length, result.sourceBytesRead());
        assertEquals(bytes.length, input.position);
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
        assertEquals(2, delivered.size());
        assertSame(candidates, adapter.receivedLimits);
    }

    static Stream<byte[]> malformedUtf8() {
        return Stream.of(new byte[]{(byte) 0xC3, 0x28},
                new byte[]{(byte) 0xEF, (byte) 0xBB},
                new byte[]{(byte) 0x80},
                new byte[]{(byte) 0xED, (byte) 0xA0, (byte) 0x80},
                new byte[]{(byte) 0xF4, (byte) 0x90, (byte) 0x80, (byte) 0x80},
                new byte[]{'h', '\n', (byte) 0xE2});
    }

    @ParameterizedTest
    @MethodSource("malformedUtf8")
    void actualReaderDecodingRejectsMalformedAndIncompleteUtf8(byte[] bytes) throws Exception {
        TrackingInput input = new TrackingInput(bytes);
        input.maxChunk = 1;
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.consumeReader = true;
        ProbeResult result = run(input, candidates, adapter, record -> fail("No record expected"));
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(List.of(DiagnosticCode.MALFORMED_UTF8), codes(result));
        assertEquals(Completion.FATAL_REJECTION, result.completion());
        assertFalse(result.issuesTruncated());
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void runnerCompletesStrictDecodingEvenWhenSyntheticSessionDoesNotConsumeReader() throws Exception {
        TrackingInput input = new TrackingInput(new byte[]{'a', (byte) 0xFF});
        input.maxChunk = 1;
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        ProbeResult result = run(input, candidates,
                adapter, record -> {});
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(List.of(DiagnosticCode.MALFORMED_UTF8), codes(result));
        assertEquals(Completion.FATAL_REJECTION, result.completion());
        assertFalse(result.issuesTruncated());
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @Test
    void prematureParserEofWithUnreadCharactersIsAnOperationalContractViolation() {
        TrackingInput input = new TrackingInput("h\nunprocessed\n".getBytes(StandardCharsets.UTF_8));
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        List<ProbeRecord> records = new ArrayList<>();
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, records::add));
        assertEquals(ProbeOperationalException.Stage.ADAPTER_READ, failure.stage());
        assertInstanceOf(IllegalStateException.class, failure.getCause());
        assertEquals(List.of(new ProbeRecord(1, List.of("h"))), records);
        assertEquals(2, adapter.nextCalls);
        assertEquals(input.bytes.length, input.position,
                "Verify the remaining source before reporting the EOF contract violation");
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @Test
    void genuineSourceFailureDuringEofVerificationHasPriorityOverUnreadCharacters() {
        TrackingInput input = new TrackingInput("h\nunprocessed\n".getBytes(StandardCharsets.UTF_8));
        input.maxChunk = 1;
        input.failAfter = 3;
        input.readFailure = new IOException("source failed while verifying EOF");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.SOURCE_READ, failure.stage());
        assertSame(input.readFailure, failure.getCause());
        assertEquals(3, input.position);
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @Test
    void headerOnlyInputIsValidWithOneWarningAndNoTruncation() throws Exception {
        List<ProbeRecord> records = new ArrayList<>();
        ProbeResult result = run(new SyntheticParserAdapter().record("h"), records::add);
        assertEquals(Outcome.VALID, result.outcome());
        assertEquals(List.of(new ProbeRecord(1, List.of("h"))), records);
        assertEquals(List.of(DiagnosticCode.NO_DATA_RECORDS), codes(result));
        assertEquals(Severity.WARNING, result.issues().getFirst().severity());
        assertFalse(result.issuesTruncated());
        assertEquals(Completion.END_OF_INPUT, result.completion());
    }

    @Test
    void warningOnlyInputStillScansToEofWithZeroDiagnosticBudget() throws Exception {
        TrackingInput input = new TrackingInput("entire byte source".getBytes(StandardCharsets.UTF_8));
        ProbeLimits limits = new ProbeLimits(100, 100, 10, 0, 100);
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h").blank().blank();
        adapter.consumeReader = true;
        ProbeResult result = run(input, limits, adapter, record -> {});
        assertEquals(Outcome.VALID, result.outcome());
        assertTrue(result.issuesTruncated());
        assertTrue(result.issues().isEmpty());
        assertEquals(Completion.END_OF_INPUT, result.completion());
        assertEquals(input.bytes.length, input.position);
        assertEquals(4, adapter.nextCalls);
    }

    @Test
    void emptyOrOnlyBlankInputHasNoHeader() throws Exception {
        ProbeResult result = run(new SyntheticParserAdapter().blank().blank(), record -> fail());
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(List.of(DiagnosticCode.MISSING_HEADER), codes(result));
        assertEquals(OptionalLong.empty(), result.issues().getFirst().recordOrdinal());
        assertFalse(result.issuesTruncated());
    }

    @Test
    void normalizedDuplicateHeaderStopsBeforeDataAndIsNotTruncation() throws Exception {
        SyntheticParserAdapter adapter = new SyntheticParserAdapter()
                .record("\u2003CAFE\u0301 ", "café").record("not", "read");
        ProbeResult result = run(adapter, record -> fail());
        assertEquals(List.of(DiagnosticCode.DUPLICATE_HEADER), codes(result));
        assertEquals(Completion.FATAL_REJECTION, result.completion());
        assertEquals(OptionalLong.of(1), result.issues().getFirst().recordOrdinal());
        assertEquals(1, adapter.nextCalls);
        assertFalse(result.issuesTruncated());
    }

    @Test
    void duplicateHeaderDetectionUsesRootCaseMapping() throws Exception {
        ProbeResult result = run(new SyntheticParserAdapter().record("İ", "i\u0307"), record -> fail());
        assertEquals(List.of(DiagnosticCode.DUPLICATE_HEADER), codes(result));
    }

    @Test
    void zeroFieldHeaderIsInvalidAndStops() throws Exception {
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record().record("unused");
        ProbeResult result = run(adapter, record -> fail());
        assertEquals(List.of(DiagnosticCode.INVALID_HEADER), codes(result));
        assertEquals(1, adapter.nextCalls);
    }

    @Test
    void originalHeadersAndEmptyValuesArePreservedWithoutAdditionalHeaderRestrictions() throws Exception {
        List<ProbeRecord> records = new ArrayList<>();
        ProbeResult result = run(new SyntheticParserAdapter()
                .record(" CAFE\u0301 ", " ").record("", ""), records::add);
        assertEquals(Outcome.VALID, result.outcome());
        assertEquals(List.of(" CAFE\u0301 ", " "), records.getFirst().fields());
        assertEquals(List.of("", ""), records.getLast().fields());
    }

    @Test
    void blanksDoNotConsumeOrdinalsButRejectedWidthsDoAndScanningContinues() throws Exception {
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().blank().record("h")
                .blank().record("bad", "width").record("good").blank()
                .record("another", "bad").record("last");
        List<ProbeRecord> records = new ArrayList<>();
        ProbeResult result = run(adapter, records::add);
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(List.of(1L, 3L, 5L), records.stream().map(ProbeRecord::ordinal).toList());
        assertEquals(List.of(OptionalLong.of(2), OptionalLong.of(4)),
                result.issues().stream().map(ProbeIssue::recordOrdinal).toList());
        assertEquals(List.of(DiagnosticCode.COLUMN_COUNT_MISMATCH,
                DiagnosticCode.COLUMN_COUNT_MISMATCH), codes(result));
        assertEquals(Completion.END_OF_INPUT, result.completion());
        assertEquals(3, result.recordsEmitted());
        assertFalse(result.issuesTruncated());
    }

    @Test
    void omittedRecoverableErrorsStillMakeFinalOutcomeInvalid() throws Exception {
        ProbeLimits limits = new ProbeLimits(100, 100, 10, 0, 100);
        ProbeResult result = run(new TrackingInput(new byte[0]), limits,
                new SyntheticParserAdapter().record("h").record("bad", "width").record("ok"),
                record -> {});
        assertEquals(Outcome.INVALID, result.outcome());
        assertTrue(result.issues().isEmpty());
        assertTrue(result.issuesTruncated());
        assertEquals(2, result.recordsEmitted());
        assertEquals(Completion.END_OF_INPUT, result.completion());
    }

    @Test
    void callbacksAreSynchronousAndDeliveredListsAreImmutable() throws Exception {
        List<ProbeRecord> records = new ArrayList<>();
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        Thread callingThread = Thread.currentThread();
        adapter.script.add(reader -> {
            assertEquals(1, records.size(), "Header callback must finish before next record");
            return Optional.of(new TokenizedRecord(false, List.of("data")));
        });
        ProbeResult result = run(adapter, record -> {
            assertSame(callingThread, Thread.currentThread());
            assertThrows(UnsupportedOperationException.class, () -> record.fields().clear());
            records.add(record);
        });
        assertEquals(Outcome.VALID, result.outcome());
        assertEquals(List.of("h"), records.getFirst().fields());
        assertEquals(List.of("data"), records.getLast().fields());
    }

    @Test
    void freshSourcesAndIdenticalLimitsAreUsedForEachRun() throws Exception {
        AtomicInteger opens = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        ProbeInput input = new ProbeInput("repeatable", () -> {
            opens.incrementAndGet();
            return new ByteArrayInputStream(new byte[]{'h'}) {
                @Override
                public void close() {
                    closes.incrementAndGet();
                }
            };
        });
        for (int i = 0; i < 2; i++) {
            SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
            adapter.consumeReader = true;
            assertEquals(1, runner.run(input, candidates, adapter, record -> {}).sourceBytesRead());
            assertSame(candidates, adapter.receivedLimits);
        }
        assertEquals(2, opens.get());
        assertEquals(2, closes.get());
    }

    @Test
    void fatalParserRejectionStopsAndDoesNotAutomaticallyTruncate() throws Exception {
        CsvRejectionException rejection = new CsvRejectionException(DiagnosticCode.MALFORMED_CSV);
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.script.add(reader -> { throw rejection; });
        adapter.record("never delivered");
        List<ProbeRecord> records = new ArrayList<>();
        ProbeResult result = run(adapter, records::add);
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(List.of(DiagnosticCode.MALFORMED_CSV), codes(result));
        assertEquals(OptionalLong.of(2), result.issues().getFirst().recordOrdinal());
        assertEquals(Completion.FATAL_REJECTION, result.completion());
        assertFalse(result.issuesTruncated());
        assertEquals(1, records.size());
        assertEquals(2, adapter.nextCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void fieldPolicyUsesParsedCodePointsButReportsEarlyEnforcementAsUnverified() throws Exception {
        ProbeLimits limits = new ProbeLimits(2, 100, 10, 5, 100);
        ProbeResult valid = run(new TrackingInput(new byte[0]), limits,
                new SyntheticParserAdapter().record("h").record("😀a"), record -> {});
        assertEquals(Outcome.VALID, valid.outcome());
        ProbeResult invalid = run(new TrackingInput(new byte[0]), limits,
                new SyntheticParserAdapter().record("h").record("😀ab"), record -> {});
        assertEquals(List.of(DiagnosticCode.FIELD_CODE_POINT_LIMIT_EXCEEDED), codes(invalid));
        assertEquals(CapabilityStatus.UNVERIFIED, invalid.capabilities().stream()
                .filter(c -> c.requirement().equals("FIELD_CODE_POINT_EARLY_ENFORCEMENT"))
                .findFirst().orElseThrow().status());
    }

    @Test
    void columnPolicyRejectsExcessWithoutClaimingPreallocationProtection() throws Exception {
        ProbeLimits limits = new ProbeLimits(100, 100, 2, 5, 100);
        ProbeResult result = run(new TrackingInput(new byte[0]), limits,
                new SyntheticParserAdapter().record("a", "b").record("1", "2", "3"),
                record -> {});
        assertEquals(List.of(DiagnosticCode.COLUMN_LIMIT_EXCEEDED), codes(result));
        assertEquals(1, result.recordsEmitted());
        assertEquals(CapabilityStatus.UNVERIFIED, result.capabilities().stream()
                .filter(c -> c.requirement().equals("COLUMN_EARLY_ENFORCEMENT"))
                .findFirst().orElseThrow().status());
    }

    @Test
    void inputBudgetIsInclusiveAndReadsAtMostOneByteOverIt() throws Exception {
        byte[] bytes = "abcdef".getBytes(StandardCharsets.UTF_8);
        ProbeLimits exact = new ProbeLimits(100, 100, 10, 5, bytes.length);
        SyntheticParserAdapter consuming = new SyntheticParserAdapter().record("h");
        consuming.consumeReader = true;
        assertEquals(Outcome.VALID, run(new TrackingInput(bytes), exact,
                consuming, record -> {}).outcome());
        TrackingInput source = new TrackingInput(bytes);
        source.maxChunk = 1;
        ProbeLimits smaller = new ProbeLimits(100, 100, 10, 5, 3);
        ProbeResult rejected = run(source, smaller,
                new SyntheticParserAdapter().record("h"), record -> {});
        assertEquals(List.of(DiagnosticCode.INPUT_BYTE_LIMIT_EXCEEDED), codes(rejected));
        assertEquals(4, rejected.sourceBytesRead());
        assertEquals(4, source.position);
        assertFalse(rejected.issuesTruncated());
        assertEquals(1, source.closeCalls);
    }

    @Test
    void bomCountsTowardWholeFileBudgetAndClosesOnBomSetupFailure() throws Exception {
        TrackingInput input = new TrackingInput(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        ProbeLimits limits = new ProbeLimits(10, 100, 10, 5, 2);
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        ProbeResult result = run(input, limits, adapter, record -> fail());
        assertEquals(List.of(DiagnosticCode.INPUT_BYTE_LIMIT_EXCEEDED), codes(result));
        assertEquals(3, result.sourceBytesRead());
        assertEquals(1, input.closeCalls);
        assertEquals(0, adapter.closeCalls);
    }

    @Test
    void capabilityEvidenceDoesNotTurnScriptedSuccessIntoParserConformance() throws Exception {
        ProbeResult result = run(new SyntheticParserAdapter().record("h"), record -> {});
        assertEquals(Outcome.VALID, result.outcome());
        assertTrue(result.capabilities().stream().anyMatch(c ->
                c.requirement().equals("STRICT_CSV_TOKENIZATION")
                        && c.status() == CapabilityStatus.UNSUPPORTED));
        assertTrue(result.capabilities().stream().anyMatch(c ->
                c.requirement().equals("RECORD_SOURCE_BYTE_EARLY_ENFORCEMENT")
                        && c.status() == CapabilityStatus.UNVERIFIED));
        assertTrue(result.capabilities().stream().anyMatch(c ->
                c.requirement().equals("SHARED_INPUT_BYTE_GUARD")
                        && c.status() == CapabilityStatus.SUPPORTED));
    }

    @Test
    void sourceOpenFailureRemainsOperationalEvenWhenItsTypeIsACodingException() {
        IOException original = new MalformedInputException(1);
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> runner.run(new ProbeInput("open-fails", () -> { throw original; }),
                        candidates, new SyntheticParserAdapter(), record -> {}));
        assertEquals(ProbeOperationalException.Stage.SOURCE_OPEN, failure.stage());
        assertSame(original, failure.getCause());
    }

    @Test
    void genuineSourceReadFailureIsNotMalformedCsvEvenWhenAdapterWrapsIt() {
        TrackingInput input = new TrackingInput(new byte[]{'a', 'b', 'c'});
        input.failAfter = 2;
        input.readFailure = new IOException("source failure");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        adapter.script.add(reader -> {
            try {
                while (reader.read() != -1) {}
                return Optional.empty();
            } catch (IOException failure) {
                throw new CsvRejectionException(DiagnosticCode.MALFORMED_CSV, failure);
            }
        });
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.SOURCE_READ, failure.stage());
        assertSame(input.readFailure, failure.getCause());
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void swallowedOrUncheckedSourceFailuresAreStillOperational() {
        for (boolean swallow : List.of(true, false)) {
            TrackingInput input = new TrackingInput(new byte[]{'a', 'b', 'c'});
            input.failAfter = 1;
            input.readFailure = new MalformedInputException(1);
            SyntheticParserAdapter adapter = new SyntheticParserAdapter();
            adapter.script.add(reader -> {
                try {
                    reader.read();
                } catch (IOException failure) {
                    if (!swallow) {
                        throw new UncheckedIOException(failure);
                    }
                }
                return Optional.empty();
            });
            ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                    () -> run(input, candidates, adapter, record -> {}));
            assertSame(input.readFailure, failure.getCause());
        }
    }

    @Test
    void sinkFailureIsOperationalAndBothResourcesClose() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        IOException original = new IOException("sink failed");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> { throw original; }));
        assertEquals(ProbeOperationalException.Stage.SINK, failure.stage());
        assertSame(original, failure.getCause());
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @Test
    void primaryOperationalFailureRetainsBothSuppressedCleanupFailures() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        input.closeFailure = new IOException("input close failed");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.closeFailure = new IOException("session close failed");
        IOException original = new IOException("sink failed first");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> { throw original; }));
        assertSame(original, failure.getCause());
        assertArrayEquals(new Throwable[]{adapter.closeFailure, input.closeFailure},
                failure.getSuppressed());
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void cleanupFailureCannotReturnSuccessfulCsvResult() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        input.closeFailure = new IOException("input close failed");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.consumeReader = true;
        adapter.closeFailure = new IOException("session close failed");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.CLEANUP, failure.stage());
        assertSame(adapter.closeFailure, failure.getCause());
        assertArrayEquals(new Throwable[]{input.closeFailure}, failure.getSuppressed());
        assertEquals(1, input.closeCalls);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void recoverableWidthMismatchDoesNotReplaceTheActualCleanupFailure(boolean sessionAlsoFails) {
        TrackingInput input = new TrackingInput(new byte[0]);
        input.closeFailure = new IOException("input close failed");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter()
                .record("h").record("bad", "width").record("ok");
        if (sessionAlsoFails) {
            adapter.closeFailure = new IOException("session close failed first");
        }
        List<ProbeRecord> records = new ArrayList<>();
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, records::add));
        assertEquals(ProbeOperationalException.Stage.CLEANUP, failure.stage());
        assertSame(sessionAlsoFails ? adapter.closeFailure : input.closeFailure, failure.getCause());
        assertArrayEquals(sessionAlsoFails ? new Throwable[]{input.closeFailure} : new Throwable[0],
                failure.getSuppressed());
        assertEquals(List.of(1L, 3L), records.stream().map(ProbeRecord::ordinal).toList());
        assertEquals(4, adapter.nextCalls, "Recoverable diagnostics must not stop scanning");
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @ParameterizedTest
    @EnumSource(value = DiagnosticCode.class,
            names = {"MALFORMED_CSV", "FIELD_CODE_POINT_LIMIT_EXCEEDED"})
    void fatalFailureAfterRecoverableMismatchRemainsPrimaryWhenBothClosesFail(DiagnosticCode code) {
        TrackingInput input = new TrackingInput(new byte[0]);
        input.closeFailure = new IOException("input close failed");
        CsvRejectionException parserFailure = new CsvRejectionException(DiagnosticCode.MALFORMED_CSV);
        SyntheticParserAdapter adapter = mismatchThenFatal(code, parserFailure);
        adapter.closeFailure = new IOException("session close failed");
        ProbeLimits limits = new ProbeLimits(1, 100, 10, 1, 100);
        List<ProbeRecord> records = new ArrayList<>();
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, limits, adapter, records::add));
        assertEquals(ProbeOperationalException.Stage.CLEANUP, failure.stage());
        CsvRejectionException fatal = assertInstanceOf(CsvRejectionException.class, failure.getCause());
        assertEquals(code, fatal.code());
        if (code == DiagnosticCode.MALFORMED_CSV) {
            assertSame(parserFailure, fatal);
        }
        assertArrayEquals(new Throwable[]{adapter.closeFailure, input.closeFailure},
                failure.getSuppressed());
        assertEquals(List.of(new ProbeRecord(1, List.of("h"))), records);
        assertEquals(3, adapter.nextCalls);
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @ParameterizedTest
    @EnumSource(value = DiagnosticCode.class,
            names = {"MALFORMED_CSV", "FIELD_CODE_POINT_LIMIT_EXCEEDED"})
    void fatalFailureAfterRecoverableMismatchPreservesValidityAndIssueTruncation(DiagnosticCode code)
            throws Exception {
        TrackingInput input = new TrackingInput(new byte[0]);
        CsvRejectionException parserFailure = new CsvRejectionException(DiagnosticCode.MALFORMED_CSV);
        SyntheticParserAdapter adapter = mismatchThenFatal(code, parserFailure);
        ProbeLimits limits = new ProbeLimits(1, 100, 10, 1, 100);
        ProbeResult result = run(input, limits, adapter, record -> {});
        assertEquals(Outcome.INVALID, result.outcome());
        assertEquals(Completion.FATAL_REJECTION, result.completion());
        assertEquals(List.of(DiagnosticCode.COLUMN_COUNT_MISMATCH), codes(result));
        assertEquals(OptionalLong.of(2), result.issues().getFirst().recordOrdinal());
        assertTrue(result.issuesTruncated(), "The later fatal diagnostic was omitted from a full buffer");
        assertEquals(1, result.recordsEmitted());
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    private static SyntheticParserAdapter mismatchThenFatal(DiagnosticCode code,
                                                            CsvRejectionException parserFailure) {
        SyntheticParserAdapter adapter = new SyntheticParserAdapter()
                .record("h").record("b", "w");
        if (code == DiagnosticCode.MALFORMED_CSV) {
            adapter.script.add(reader -> { throw parserFailure; });
        } else {
            adapter.record("xx");
        }
        return adapter.record("not reached");
    }

    @Test
    void fatalContentFailureRemainsTheCauseWhenCleanupAlsoFails() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        input.closeFailure = new IOException("input close failed");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        CsvRejectionException original = new CsvRejectionException(DiagnosticCode.MALFORMED_CSV);
        adapter.script.add(reader -> { throw original; });
        adapter.closeFailure = new IOException("session close failed");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.CLEANUP, failure.stage());
        assertSame(original, failure.getCause());
        assertArrayEquals(new Throwable[]{adapter.closeFailure, input.closeFailure},
                failure.getSuppressed());
    }

    @Test
    void adapterOpenFailureClosesTheSourceWithoutAConstructedSession() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        adapter.openFailure = new IOException("open failed");
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.ADAPTER_OPEN, failure.stage());
        assertSame(adapter.openFailure, failure.getCause());
        assertEquals(1, input.closeCalls);
        assertEquals(0, adapter.closeCalls);
    }

    @Test
    void unsupportedConfigurationDoesNotReturnValidOrInvalidAndPreservesCleanupFailures() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        input.closeFailure = new IOException("input close failed");
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        adapter.unsupported = new UnsupportedProbeException("native safety hook");
        UnsupportedProbeException failure = assertThrows(UnsupportedProbeException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertSame(adapter.unsupported, failure);
        assertArrayEquals(new Throwable[]{input.closeFailure}, failure.getSuppressed());
        assertEquals(1, input.closeCalls);
    }

    @Test
    void nativeReaderCloseDoesNotDoubleCloseRunnerOwnedSource() throws Exception {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.consumeReader = true;
        adapter.closeBorrowedReader = true;
        run(input, candidates, adapter, record -> {});
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void unexpectedRuntimeFailureIsOperationalRatherThanInvalidCsv() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        IllegalStateException original = new IllegalStateException("adapter bug");
        adapter.script.add(reader -> { throw original; });
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> run(input, candidates, adapter, record -> {}));
        assertSame(original, failure.getCause());
        assertEquals(1, input.closeCalls);
        assertEquals(1, adapter.closeCalls);
    }

    @Test
    void assertionErrorsRemainErrorsAndStillCloseEveryResource() {
        TrackingInput input = new TrackingInput(new byte[]{'h'});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        input.closeFailure = new IOException("input close failed");
        AssertionError original = new AssertionError("test assertion");
        AssertionError failure = assertThrows(AssertionError.class,
                () -> run(input, candidates, adapter, record -> { throw original; }));
        assertSame(original, failure);
        assertArrayEquals(new Throwable[]{input.closeFailure}, failure.getSuppressed());
        assertEquals(1, adapter.closeCalls);
        assertEquals(1, input.closeCalls);
    }

    @Test
    void retryingAdapterCannotReplaceTheFirstSourceFailure() {
        IOException first = new IOException("first read failure");
        IOException later = new IOException("later read failure");
        AtomicInteger calls = new AtomicInteger();
        InputStream input = new InputStream() {
            @Override
            public int read() throws IOException {
                int call = calls.getAndIncrement();
                if (call == 0) {
                    return 'h';
                }
                throw call == 1 ? first : later;
            }

            @Override
            public int read(byte[] bytes, int offset, int length) throws IOException {
                if (length == 0) {
                    return 0;
                }
                int value = read();
                if (value == -1) {
                    return -1;
                }
                bytes[offset] = (byte) value;
                return 1;
            }
        };
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        adapter.script.add(reader -> {
            for (int retry = 0; retry < 2; retry++) {
                try {
                    reader.read();
                } catch (IOException ignored) {
                    // Deliberately misbehaving test adapter: the runner must retain the first failure.
                }
            }
            return Optional.empty();
        });
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> runner.run(new ProbeInput("retry", () -> input), candidates, adapter, record -> {}));
        assertSame(first, failure.getCause());
    }

    @Test
    void eagerOpenCannotHideSourceFailureAsUnsupportedConfiguration() {
        TrackingInput input = new TrackingInput(new byte[]{'h', 'x'});
        input.failAfter = 1;
        input.readFailure = new IOException("source failure during adapter open");
        SyntheticParserAdapter metadata = new SyntheticParserAdapter();
        CsvParserAdapter adapter = new CsvParserAdapter() {
            @Override
            public ParserIdentity identity() {
                return metadata.identity();
            }

            @Override
            public List<Capability> capabilities(ProbeLimits limits) {
                return metadata.capabilities(limits);
            }

            @Override
            public ParserSession open(Reader reader, ProbeLimits limits) throws UnsupportedProbeException {
                try {
                    reader.read();
                } catch (IOException failure) {
                    throw new UnsupportedProbeException("deliberately misclassified read failure");
                }
                throw new AssertionError("Expected source failure");
            }
        };
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> runner.run(new ProbeInput("eager", () -> input), candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.SOURCE_READ, failure.stage());
        assertSame(input.readFailure, failure.getCause());
        assertEquals(1, input.closeCalls);
    }

    @Test
    void malformedDecodingWrappedAsNativeCsvRejectionStillUsesEncodingCode() throws Exception {
        TrackingInput input = new TrackingInput(new byte[]{'h', (byte) 0xFF});
        SyntheticParserAdapter adapter = new SyntheticParserAdapter();
        adapter.script.add(reader -> {
            try {
                while (reader.read() != -1) {}
            } catch (IOException failure) {
                throw new CsvRejectionException(DiagnosticCode.MALFORMED_CSV, failure);
            }
            return Optional.empty();
        });
        ProbeResult result = run(input, candidates, adapter, record -> {});
        assertEquals(List.of(DiagnosticCode.MALFORMED_UTF8), codes(result));
    }

    @Test
    void adaptersCannotOverrideSharedCapabilityEvidence() {
        SyntheticParserAdapter adapter = new SyntheticParserAdapter().record("h");
        adapter.reportedCapabilities.add(new Capability("SHARED_INPUT_BYTE_GUARD",
                CapabilityStatus.UNSUPPORTED, "Cannot override runner evidence."));
        AtomicInteger opens = new AtomicInteger();
        ProbeOperationalException failure = assertThrows(ProbeOperationalException.class,
                () -> runner.run(new ProbeInput("reserved", () -> {
                    opens.incrementAndGet();
                    return new ByteArrayInputStream(new byte[0]);
                }), candidates, adapter, record -> {}));
        assertEquals(ProbeOperationalException.Stage.INFRASTRUCTURE, failure.stage());
        assertEquals(0, opens.get());
    }

    private static final class TrackingInput extends InputStream {
        private final byte[] bytes;
        private int position;
        private int maxChunk = Integer.MAX_VALUE;
        private int failAfter = Integer.MAX_VALUE;
        private IOException readFailure;
        private IOException closeFailure;
        private int closeCalls;

        private TrackingInput(byte[] bytes) {
            this.bytes = bytes.clone();
        }

        @Override
        public int read() throws IOException {
            if (position >= failAfter && readFailure != null) {
                throw readFailure;
            }
            return position == bytes.length ? -1 : bytes[position++] & 0xFF;
        }

        @Override
        public int read(byte[] target, int offset, int length) throws IOException {
            if (position >= failAfter && readFailure != null) {
                throw readFailure;
            }
            if (position == bytes.length) {
                return -1;
            }
            int count = Math.min(length, Math.min(maxChunk,
                    Math.min(bytes.length - position, failAfter - position)));
            System.arraycopy(bytes, position, target, offset, count);
            position += count;
            return count;
        }

        @Override
        public void close() throws IOException {
            closeCalls++;
            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }
}
