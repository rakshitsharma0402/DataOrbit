package com.dataorbit.experiments.csv;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SharedCsvPolicyTest {
    @Test
    void headerNormalizationUsesStripNfcRootLowercaseAndNfc() {
        assertEquals("café", SharedCsvPolicy.normalizeHeader("\u2003CAFE\u0301  "));
        assertEquals("i\u0307", SharedCsvPolicy.normalizeHeader("\u0130"));
        assertEquals("", SharedCsvPolicy.normalizeHeader(" \t"));
    }

    @Test
    void countsCodePointsRatherThanUtf16UnitsOrUtf8Bytes() {
        String parsedValue = "\uD83D\uDE00a";
        assertEquals(3, parsedValue.length());
        assertFalse(SharedCsvPolicy.exceedsCodePoints(parsedValue, 2));
        assertTrue(SharedCsvPolicy.exceedsCodePoints(parsedValue, 1));
    }

    @Test
    void doesNotNormalizeParsedFieldValuesBeforeCounting() {
        assertTrue(SharedCsvPolicy.exceedsCodePoints("e\u0301", 1));
        assertFalse(SharedCsvPolicy.exceedsCodePoints("é", 1));
        assertFalse(SharedCsvPolicy.exceedsCodePoints("", 1));
    }

    @Test
    void codePointPolicyDoesNotClaimEarlyParserEnforcement() {
        assertFalse(SharedCsvPolicy.exceedsCodePoints("\"", 1));
        // The value represents an already-unescaped quote; no raw CSV was parsed here.
        assertThrows(IllegalArgumentException.class,
                () -> SharedCsvPolicy.exceedsCodePoints("a", 0));
        assertThrows(NullPointerException.class, () -> SharedCsvPolicy.normalizeHeader(null));
    }
}
