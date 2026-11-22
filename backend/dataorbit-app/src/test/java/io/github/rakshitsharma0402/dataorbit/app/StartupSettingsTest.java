package io.github.rakshitsharma0402.dataorbit.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.rakshitsharma0402.dataorbit.configuration.InvalidStartupSettingException;

/** Starts the full application with bad settings and checks that startup fails before serving. */
class StartupSettingsTest {

    @ParameterizedTest(name = "{0}={1} fails startup")
    @CsvSource(delimiter = '|', value = {
        "server.address            | 0.0.0.0",
        "server.address            | ''",
        "server.address            | localhost",
        "server.address            | 192.168.1.10",
        "dataorbit.frontend-origin | ''",
        "dataorbit.frontend-origin | http://example.com",
        "dataorbit.frontend-origin | http://localhost:5173/app"
    })
    void invalidOrMissingSettingFailsStartup(String setting, String value) {
        assertThatThrownBy(() -> start(setting + "=" + value))
                .hasRootCauseInstanceOf(InvalidStartupSettingException.class)
                .rootCause()
                .satisfies(cause -> assertThat(((InvalidStartupSettingException) cause).setting())
                        .isEqualTo(setting));
    }

    @Test
    void defaultSettingsStartAndCanBeClosed() {
        try (ConfigurableApplicationContext context = start()) {
            assertThat(context.isActive()).isTrue();
        }
    }

    @Test
    void failureReportNamesTheSettingAndTheFix() {
        InvalidStartupSettingException cause =
                new InvalidStartupSettingException("server.address", "must be a loopback address");

        FailureAnalysis analysis = new InvalidStartupSettingFailureAnalyzer()
                .analyze(new IllegalStateException("context refresh failed", cause));

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription())
                .isEqualTo("Invalid startup setting 'server.address': must be a loopback address");
        assertThat(analysis.getAction()).contains("Fix 'server.address'");
    }

    private static ConfigurableApplicationContext start(String... settings) {
        return TestApplication.start(settings);
    }
}
