package io.github.rakshitsharma0402.dataorbit.configuration;

import java.net.InetAddress;
import java.util.Objects;

/**
 * The validated address the backend's HTTP server binds to.
 *
 * <p>V1 has no login, so the backend must listen only on a loopback interface (handbook chapter 17).
 * The value must be a literal loopback IP such as {@code 127.0.0.1} or {@code ::1}. A host name such
 * as {@code localhost} is rejected because resolving it depends on the machine's name configuration.
 * A missing value is rejected too: without {@code server.address}, the server would listen on every
 * network interface.
 *
 * @param address the loopback address, never {@code null}
 */
public record LoopbackBindAddress(InetAddress address) {

    /** The Spring Boot property this value comes from. */
    public static final String PROPERTY = "server.address";

    public LoopbackBindAddress {
        Objects.requireNonNull(address, "address");
        if (!address.isLoopbackAddress()) {
            throw new InvalidStartupSettingException(
                    PROPERTY, "must be a loopback address such as 127.0.0.1 or ::1");
        }
    }

    /**
     * Validates a raw setting value.
     *
     * @param value the configured value, possibly {@code null} or blank
     * @return the validated bind address
     * @throws InvalidStartupSettingException if the value is missing, not a literal IP, or not loopback
     */
    public static LoopbackBindAddress parse(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStartupSettingException(
                    PROPERTY, "is required; set it to a loopback address such as 127.0.0.1");
        }
        InetAddress address;
        try {
            // ofLiteral never performs a DNS lookup; it accepts only IPv4 or IPv6 literals.
            address = InetAddress.ofLiteral(value.strip());
        } catch (IllegalArgumentException e) {
            throw new InvalidStartupSettingException(
                    PROPERTY, "must be a literal loopback IP address such as 127.0.0.1, not a host name");
        }
        return new LoopbackBindAddress(address);
    }
}
