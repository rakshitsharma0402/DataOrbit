package com.dataorbit.experiments.csv;

/** Fixed messages never interpolate uploaded fields or native exception text. */
public enum DiagnosticCode {
    MISSING_HEADER("A CSV header is required."),
    INVALID_HEADER("The parser returned a header with no fields."),
    DUPLICATE_HEADER("Normalized header names must be unique."),
    COLUMN_COUNT_MISMATCH("Record field count differs from the header."),
    MALFORMED_UTF8("Input is not well-formed UTF-8."),
    MALFORMED_CSV("The parser rejected malformed CSV."),
    FIELD_CODE_POINT_LIMIT_EXCEEDED("A parsed field exceeds the code-point limit."),
    RECORD_SOURCE_BYTE_LIMIT_EXCEEDED("A logical record exceeds the source-byte limit."),
    COLUMN_LIMIT_EXCEEDED("A record exceeds the column limit."),
    NO_DATA_RECORDS("The CSV contains a header but no data records."),
    // Additionally approved by the owner during Checkpoint 2A implementation.
    INPUT_BYTE_LIMIT_EXCEEDED("The source exceeds the whole-file byte limit.");

    private final String message;

    DiagnosticCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    public Severity severity() {
        return this == NO_DATA_RECORDS ? Severity.WARNING : Severity.ERROR;
    }
}
