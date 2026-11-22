package io.github.rakshitsharma0402.dataorbit.app;

import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.github.rakshitsharma0402.dataorbit.configuration.DataOrbitProperties;
import io.github.rakshitsharma0402.dataorbit.configuration.LoopbackBindAddress;
import io.github.rakshitsharma0402.dataorbit.configuration.RequiredSetting;

/**
 * Database assembly. Flyway itself is configured by Spring Boot from {@code spring.flyway.*}
 * (ADR-0001); this class only adds DataOrbit's startup checks.
 */
@Configuration(proxyBeanMethods = false)
class DatabaseConfiguration {

    static final String PASSWORD_SETTING = "spring.datasource.password";

    /**
     * Runs DataOrbit's startup checks before Flyway touches the database.
     *
     * <p>Spring Boot applies every {@link FlywayConfigurationCustomizer} before it migrates. Requiring
     * the validated settings as parameters, and checking the password here, guarantees that an
     * invalid setting stops startup before any SQL runs (handbook chapter 14). Without it, the order
     * would depend on bean creation order, which nothing promises. The customizer itself changes no
     * Flyway setting.
     */
    @Bean
    FlywayConfigurationCustomizer validateSettingsBeforeMigrating(
            LoopbackBindAddress bindAddress, DataOrbitProperties properties, Environment environment) {
        RequiredSetting.requirePresent(PASSWORD_SETTING, databasePassword(environment),
                "set the DATAORBIT_DB_PASSWORD environment variable (see docs/setup.md)");
        return configuration -> {
            // Intentionally empty: the checks above are the point.
        };
    }

    /** The configured password, or {@code null} if it refers to an environment variable that is not set. */
    private static String databasePassword(Environment environment) {
        try {
            return environment.getProperty(PASSWORD_SETTING);
        } catch (IllegalArgumentException unresolvablePlaceholder) {
            return null;
        }
    }
}
