package com.dataorbit.experiments.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProbeContractsTest {
    static Stream<Arguments> badLimits() {
        return Stream.of(
                Arguments.of(0L, 1L, 1, 1, 1L), Arguments.of(1L, 0L, 1, 1, 1L),
                Arguments.of(1L, 1L, 0, 1, 1L), Arguments.of(1L, 1L, 1, -1, 1L),
                Arguments.of(1L, 1L, 1, 1, 0L));
    }

    @ParameterizedTest
    @MethodSource("badLimits")
    void rejectsInvalidLimits(long field, long record, int columns, int issues, long input) {
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeLimits(field, record, columns, issues, input));
    }

    @Test
    void preservesExactCandidateUnitsAndValues() {
        assertEquals(new ProbeLimits(1_000_000, 4_194_304, 1_000, 100, 250_000_000),
                ProbeLimits.candidates());
    }

    @Test
    void recordsAreDefensiveSnapshots() {
        List<String> values = new ArrayList<>(List.of("original"));
        TokenizedRecord token = new TokenizedRecord(false, values);
        ProbeRecord record = new ProbeRecord(1, values);
        values.set(0, "changed");
        assertEquals(List.of("original"), token.fields());
        assertEquals(List.of("original"), record.fields());
        assertThrows(UnsupportedOperationException.class, () -> record.fields().clear());
        assertThrows(UnsupportedOperationException.class, () -> token.fields().clear());
    }

    @Test
    void resultCopiesItsCollections() {
        List<ProbeIssue> issues = new ArrayList<>(List.of(
                new ProbeIssue(DiagnosticCode.NO_DATA_RECORDS, OptionalLong.of(1))));
        List<Capability> capabilities = new ArrayList<>(List.of(
                new Capability("test", CapabilityStatus.UNVERIFIED, "No evidence yet.")));
        ProbeResult result = new ProbeResult(Outcome.VALID, issues, false,
                Completion.END_OF_INPUT, 1, 0, capabilities);
        issues.clear();
        capabilities.clear();
        assertEquals(1, result.issues().size());
        assertEquals(1, result.capabilities().size());
        assertThrows(UnsupportedOperationException.class, () -> result.issues().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.capabilities().clear());
    }

    @Test
    void issueMessagesAreOnlyFixedCatalogMessages() {
        for (DiagnosticCode code : DiagnosticCode.values()) {
            ProbeIssue issue = new ProbeIssue(code, OptionalLong.empty());
            assertEquals(code.message(), issue.message());
            assertTrue(issue.message().length() < 100);
            assertEquals(code == DiagnosticCode.NO_DATA_RECORDS ? Severity.WARNING : Severity.ERROR,
                    issue.severity());
        }
    }

    @Test
    void rejectsInvalidOrNullContractValues() {
        assertThrows(NullPointerException.class, () -> new ProbeInput("case", null));
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeInput(" ", () -> new ByteArrayInputStream(new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> new ParserIdentity("", "1"));
        assertThrows(NullPointerException.class, () -> new ParserIdentity("library", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Capability("x", CapabilityStatus.SUPPORTED, ""));
        assertThrows(NullPointerException.class,
                () -> new Capability("x", null, "evidence"));
        assertThrows(IllegalArgumentException.class,
                () -> new TokenizedRecord(true, List.of("value")));
        assertThrows(NullPointerException.class,
                () -> new TokenizedRecord(false, java.util.Arrays.asList("x", null)));
        assertThrows(IllegalArgumentException.class, () -> new ProbeRecord(0, List.of("x")));
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeIssue(DiagnosticCode.MISSING_HEADER, OptionalLong.of(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new CsvRejectionException(DiagnosticCode.NO_DATA_RECORDS));
        assertThrows(IllegalArgumentException.class, () -> new UnsupportedProbeException(""));
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeResult(Outcome.VALID, List.of(), false,
                        Completion.END_OF_INPUT, -1, 0, List.of()));
    }

    @Test
    void validResultsCannotContainErrorsOrClaimFatalRejection() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeResult(Outcome.VALID, List.of(), false,
                        Completion.FATAL_REJECTION, 0, 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new ProbeResult(Outcome.VALID,
                        List.of(new ProbeIssue(DiagnosticCode.MALFORMED_CSV, OptionalLong.empty())),
                        false, Completion.END_OF_INPUT, 0, 0, List.of()));
    }
}
