package io.github.rakshitsharma0402.dataorbit.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoopbackBindAddressTest {

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "127.0.0.2", "::1", " 127.0.0.1 "})
    void acceptsLiteralLoopbackAddresses(String value) {
        assertThat(LoopbackBindAddress.parse(value).address().isLoopbackAddress()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingAddress(String value) {
        assertThatThrownBy(() -> LoopbackBindAddress.parse(value))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("'server.address'")
                .hasMessageContaining("is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0.0", "::", "192.168.1.10", "10.0.0.1", "8.8.8.8"})
    void rejectsNonLoopbackAddresses(String value) {
        assertThatThrownBy(() -> LoopbackBindAddress.parse(value))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("must be a loopback address");
    }

    @ParameterizedTest
    @ValueSource(strings = {"localhost", "example.com", "127.0.0.1:8080", "not an address"})
    void rejectsHostNamesAndMalformedValues(String value) {
        assertThatThrownBy(() -> LoopbackBindAddress.parse(value))
                .isInstanceOf(InvalidStartupSettingException.class)
                .hasMessageContaining("not a host name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0.0", "my-secret-host"})
    void errorMessageNeverEchoesTheRejectedValue(String value) {
        assertThatThrownBy(() -> LoopbackBindAddress.parse(value))
                .isInstanceOf(InvalidStartupSettingException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(value));
    }
}
