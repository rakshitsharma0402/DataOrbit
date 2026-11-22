package io.github.rakshitsharma0402.dataorbit.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/** Starts the real application in a test, on a random port, with extra settings. */
final class TestApplication {

    private TestApplication() {}

    /**
     * Starts DataOrbit with the given {@code name=value} settings as command-line arguments. Those
     * outrank application.yml, as they would for a real user. ({@code SpringApplicationBuilder
     * .properties} would set defaults instead, which application.yml silently overrides.)
     */
    static ConfigurableApplicationContext start(String... settings) {
        List<String> args = new ArrayList<>();
        args.add("--server.port=0");
        for (String setting : settings) {
            args.add("--" + setting);
        }
        return new SpringApplicationBuilder(DataOrbitApplication.class).run(args.toArray(String[]::new));
    }
}
