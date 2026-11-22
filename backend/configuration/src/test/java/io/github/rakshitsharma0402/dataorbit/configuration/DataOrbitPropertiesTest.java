package io.github.rakshitsharma0402.dataorbit.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DataOrbitPropertiesTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://[::1]:5173",
        "https://localhost",
        "HTTP://LOCALHOST:3000"
    })
    void acceptsLoopbackOrigins(String origin) {
        assertThat(new DataOrbitProperties(URI.create(origin)).frontendOrigin()).isEqualTo(URI.create(origin));
    }

    @Test
    void rejectsMissingOrigin() {
        assertThatThrownBy(() -> new DataOrbitProperties(null))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("'dataorbit.frontend-origin'")
                .hasMessageContaining("is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://localhost", "file:///tmp/x", "localhost:5173", "//localhost:5173"})
    void rejectsNonHttpSchemes(String origin) {
        assertThatThrownBy(() -> new DataOrbitProperties(URI.create(origin)))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("must use http or https");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://example.com",
        "http://192.168.1.10:5173",
        "http://0.0.0.0:5173",
        "http://localhost.example.com"
    })
    void rejectsNonLoopbackHosts(String origin) {
        assertThatThrownBy(() -> new DataOrbitProperties(URI.create(origin)))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("must point at localhost");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:5173/",
        "http://localhost:5173/app",
        "http://localhost:5173?x=1",
        "http://localhost:5173#top",
        "http://user@localhost:5173"
    })
    void rejectsAnythingBeyondAnOrigin(String origin) {
        assertThatThrownBy(() -> new DataOrbitProperties(URI.create(origin)))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("must be an origin only");
    }
}
