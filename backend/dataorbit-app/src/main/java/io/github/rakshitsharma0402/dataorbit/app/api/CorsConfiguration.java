package io.github.rakshitsharma0402.dataorbit.app.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.rakshitsharma0402.dataorbit.configuration.DataOrbitProperties;

/**
 * Lets exactly one browser origin, {@code dataorbit.frontend-origin}, call the REST API.
 *
 * <p>Spring answers a request to {@code /api/**} that carries any other {@code Origin} header with
 * 403 Forbidden, so a page on another site cannot use the API from a browser. CORS is not
 * authentication or CSRF protection (handbook chapter 17); requests without an {@code Origin}
 * header, such as curl, are not affected.
 */
@Configuration(proxyBeanMethods = false)
class CorsConfiguration implements WebMvcConfigurer {

    /** The methods the V1 REST contract uses (handbook chapter 15). */
    static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT"};

    private final String allowedOrigin;

    CorsConfiguration(DataOrbitProperties properties) {
        this.allowedOrigin = properties.frontendOrigin().toString();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
