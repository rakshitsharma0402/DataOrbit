package io.github.rakshitsharma0402.dataorbit.configuration;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DataOrbit's own startup settings, bound once from the {@code dataorbit.*} properties.
 *
 * <p>The record is immutable and validated in its constructor, so an instance can never hold an
 * invalid value. A missing or invalid setting fails startup (handbook chapter 14). Later milestones
 * add storage, upload, CSV and classification settings here.
 *
 * @param frontendOrigin the one browser origin the backend accepts (CORS), for example
 *     {@code http://localhost:5173}. It must use http or https, point at a loopback host, and have no
 *     path, query, fragment or user information.
 */
@ConfigurationProperties(prefix = "dataorbit")
public record DataOrbitProperties(URI frontendOrigin) {

    static final String FRONTEND_ORIGIN = "dataorbit.frontend-origin";

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");

    public DataOrbitProperties {
        validateFrontendOrigin(frontendOrigin);
    }

    private static void validateFrontendOrigin(URI origin) {
        if (origin == null) {
            throw new InvalidStartupSettingException(
                    FRONTEND_ORIGIN, "is required, for example http://localhost:5173");
        }
        String scheme = origin.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new InvalidStartupSettingException(FRONTEND_ORIGIN, "must use http or https");
        }
        String host = origin.getHost();
        if (host == null || !LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new InvalidStartupSettingException(
                    FRONTEND_ORIGIN, "must point at localhost, 127.0.0.1 or [::1]");
        }
        boolean hasPath = origin.getRawPath() != null && !origin.getRawPath().isEmpty();
        if (origin.getRawUserInfo() != null || hasPath
                || origin.getRawQuery() != null || origin.getRawFragment() != null) {
            throw new InvalidStartupSettingException(
                    FRONTEND_ORIGIN, "must be an origin only: scheme, host and optional port");
        }
    }
}
