package io.github.rakshitsharma0402.dataorbit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.github.rakshitsharma0402.dataorbit.configuration.DataOrbitProperties;

/**
 * The DataOrbit backend's entry point. This module only assembles the application: it wires the
 * other modules together and hosts the REST adapters. Business rules live in their own modules.
 */
@SpringBootApplication
@EnableConfigurationProperties(DataOrbitProperties.class)
public class DataOrbitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataOrbitApplication.class, args);
    }
}
