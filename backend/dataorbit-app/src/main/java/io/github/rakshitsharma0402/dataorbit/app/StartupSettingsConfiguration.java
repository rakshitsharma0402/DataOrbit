package io.github.rakshitsharma0402.dataorbit.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.github.rakshitsharma0402.dataorbit.configuration.LoopbackBindAddress;

/**
 * Validates startup settings that belong to Spring Boot rather than to {@code dataorbit.*}.
 *
 * <p>Spring creates this bean while the application context refreshes. That happens before the
 * embedded web server starts accepting connections, so an invalid bind address stops the
 * application before it listens on any port.
 */
@Configuration(proxyBeanMethods = false)
class StartupSettingsConfiguration {

    @Bean
    LoopbackBindAddress loopbackBindAddress(Environment environment) {
        return LoopbackBindAddress.parse(environment.getProperty(LoopbackBindAddress.PROPERTY));
    }
}
