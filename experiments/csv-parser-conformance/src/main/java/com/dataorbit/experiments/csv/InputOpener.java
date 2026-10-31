package com.dataorbit.experiments.csv;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputOpener {
    /** Returns a fresh raw-byte stream on each invocation. */
    InputStream open() throws IOException;
}
