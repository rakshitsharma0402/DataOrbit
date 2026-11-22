package io.github.rakshitsharma0402.dataorbit.configuration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RequiredSettingTest {

    @Test
    void acceptsAnyRealValue() {
        assertThatCode(() -> RequiredSetting.requirePresent("spring.datasource.password", "s3cret", "set X"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "${DATAORBIT_DB_PASSWORD}", "prefix-${UNSET}"})
    void rejectsMissingBlankAndUnresolvedValues(String value) {
        assertThatThrownBy(() -> RequiredSetting.requirePresent(
                        "spring.datasource.password", value, "set the DATAORBIT_DB_PASSWORD environment variable"))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessage("Invalid startup setting 'spring.datasource.password': is required;"
                        + " set the DATAORBIT_DB_PASSWORD environment variable");
    }
}
