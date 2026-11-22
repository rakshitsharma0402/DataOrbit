package io.github.rakshitsharma0402.dataorbit.app.api;

import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports which DataOrbit backend is running. A successful response also shows the backend is up.
 *
 * <p>The version comes from {@code META-INF/build-info.properties}, which the Maven build generates
 * from the project's {@code ${revision}}, so it always matches the build.
 */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
class VersionController {

    static final String PRODUCT_NAME = "DataOrbit";

    private final VersionResponse response;

    VersionController(BuildProperties buildProperties) {
        this.response = new VersionResponse(PRODUCT_NAME, buildProperties.getVersion());
    }

    @GetMapping("/version")
    VersionResponse version() {
        return response;
    }

    /** Response body for {@code GET /api/v1/version}. */
    record VersionResponse(String name, String version) {}
}
