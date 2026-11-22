package io.github.rakshitsharma0402.dataorbit.app;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.Ordered;

import io.github.rakshitsharma0402.dataorbit.configuration.InvalidStartupSettingException;

/**
 * Turns an invalid startup setting into Spring Boot's short "APPLICATION FAILED TO START" report,
 * naming the setting and how to fix it, instead of a long stack trace.
 *
 * <p>Registered in {@code META-INF/spring.factories}. It runs before Spring Boot's own analyzers, so
 * a settings error found while binding {@code dataorbit.*} properties is reported the same way.
 */
class InvalidStartupSettingFailureAnalyzer extends AbstractFailureAnalyzer<InvalidStartupSettingException>
        implements Ordered {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, InvalidStartupSettingException cause) {
        return new FailureAnalysis(
                cause.getMessage(),
                "Fix '" + cause.setting() + "' in application.yml, an environment variable or a"
                        + " command-line argument, then start DataOrbit again.",
                cause);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
