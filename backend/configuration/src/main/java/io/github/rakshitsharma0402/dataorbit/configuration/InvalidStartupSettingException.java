package io.github.rakshitsharma0402.dataorbit.configuration;

import java.util.Objects;

/**
 * Thrown when a startup setting is missing or invalid. It stops the application before it accepts
 * any work, as handbook chapter 14 requires, instead of falling back to an unsafe default.
 *
 * <p>The message names the setting and the rule it broke. It never echoes the rejected value, so a
 * mistyped secret or a full local path cannot end up in logs.
 */
public final class InvalidStartupSettingException extends IllegalArgumentException {

    private final String setting;

    public InvalidStartupSettingException(String setting, String problem) {
        super("Invalid startup setting '" + Objects.requireNonNull(setting, "setting") + "': "
                + Objects.requireNonNull(problem, "problem"));
        this.setting = setting;
    }

    /** The name of the offending setting, for example {@code server.address}. */
    public String setting() {
        return setting;
    }
}
