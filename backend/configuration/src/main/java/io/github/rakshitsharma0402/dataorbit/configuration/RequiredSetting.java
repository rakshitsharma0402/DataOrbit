package io.github.rakshitsharma0402.dataorbit.configuration;

/**
 * Checks that a required text setting, typically a secret, is actually present.
 *
 * <p>Spring Boot keeps an unresolved placeholder such as {@code ${DATAORBIT_DB_PASSWORD}} as literal
 * text when the environment variable is missing, so "the setting has a value" is not enough. A
 * value that is blank or still contains {@code ${} counts as missing.
 */
public final class RequiredSetting {

    private RequiredSetting() {}

    /**
     * @param setting the setting's name, used in the error message
     * @param value the resolved value, possibly {@code null}
     * @param howToSet a short hint for the error message, for example which environment variable to set
     * @throws InvalidStartupSettingException if the value is missing, blank or an unresolved placeholder
     */
    public static void requirePresent(String setting, String value, String howToSet) {
        if (value == null || value.isBlank() || value.contains("${")) {
            throw new InvalidStartupSettingException(setting, "is required; " + howToSet);
        }
    }
}
