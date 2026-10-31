package com.dataorbit.experiments.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class IssueCollectorTest {
    private static ProbeIssue warning(long ordinal) {
        return new ProbeIssue(DiagnosticCode.NO_DATA_RECORDS, OptionalLong.of(ordinal));
    }

    private static ProbeIssue error(long ordinal) {
        return new ProbeIssue(DiagnosticCode.COLUMN_COUNT_MISMATCH, OptionalLong.of(ordinal));
    }

    @Test
    void errorsAreOrderedBeforeWarningsWithoutTruncatingWhenNothingIsLost() {
        IssueCollector collector = new IssueCollector(4);
        collector.add(warning(1));
        collector.add(error(2));
        collector.add(warning(3));
        collector.add(error(4));
        assertEquals(List.of(error(2), error(4), warning(1), warning(3)), collector.snapshot());
        assertFalse(collector.truncated());
        assertTrue(collector.hasErrors());
    }

    @Test
    void newErrorEvictsLatestRetainedWarning() {
        IssueCollector collector = new IssueCollector(2);
        collector.add(warning(1));
        collector.add(warning(2));
        collector.add(error(3));
        assertEquals(List.of(error(3), warning(1)), collector.snapshot());
        assertTrue(collector.truncated());
    }

    @Test
    void fullErrorBufferDoesNotEvictEarlierErrors() {
        IssueCollector collector = new IssueCollector(2);
        collector.add(error(1));
        collector.add(error(2));
        collector.add(error(3));
        collector.add(warning(4));
        assertEquals(List.of(error(1), error(2)), collector.snapshot());
        assertTrue(collector.truncated());
    }

    @Test
    void zeroBudgetTracksErrorsAndOnlyTruncatesAfterAnObservedIssue() {
        IssueCollector collector = new IssueCollector(0);
        assertFalse(collector.truncated());
        collector.add(warning(1));
        assertTrue(collector.truncated());
        assertFalse(collector.hasErrors());
        collector.add(error(2));
        assertTrue(collector.hasErrors());
        assertEquals(List.of(), collector.snapshot());
    }

    @Test
    void snapshotsAreImmutableAndDoNotChangeWhenMoreIssuesArrive() {
        IssueCollector collector = new IssueCollector(2);
        collector.add(warning(1));
        List<ProbeIssue> before = collector.snapshot();
        collector.add(error(2));
        assertEquals(List.of(warning(1)), before);
        assertThrows(UnsupportedOperationException.class, () -> before.clear());
    }

    @Test
    void candidateBudgetRetainsExactlyOneHundredIssues() {
        IssueCollector collector = new IssueCollector(ProbeLimits.candidates().maxIssues());
        for (int i = 1; i <= 100; i++) {
            collector.add(warning(i));
        }
        assertEquals(100, collector.snapshot().size());
        assertFalse(collector.truncated());
        collector.add(error(101));
        assertEquals(100, collector.snapshot().size());
        assertEquals(error(101), collector.snapshot().getFirst());
        assertTrue(collector.truncated());
    }

    @Test
    void rejectsInvalidCollectorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new IssueCollector(-1));
        assertThrows(NullPointerException.class, () -> new IssueCollector(1).add(null));
    }
}
