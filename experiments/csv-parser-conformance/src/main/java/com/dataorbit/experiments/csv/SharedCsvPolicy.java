package com.dataorbit.experiments.csv;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/** Checks already parsed values. These checks do not prove early parser enforcement. */
final class SharedCsvPolicy {
    private SharedCsvPolicy() {}

    static String normalizeHeader(String value) {
        String nfc = Normalizer.normalize(Objects.requireNonNull(value, "value").strip(),
                Normalizer.Form.NFC);
        return Normalizer.normalize(nfc.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);
    }

    static boolean exceedsCodePoints(String value, long maximum) {
        Objects.requireNonNull(value, "value");
        if (maximum < 1) {
            throw new IllegalArgumentException("Maximum must be positive");
        }
        return value.codePointCount(0, value.length()) > maximum;
    }
}
