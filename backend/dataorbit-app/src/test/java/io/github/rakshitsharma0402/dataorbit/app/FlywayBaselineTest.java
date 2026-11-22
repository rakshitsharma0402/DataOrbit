package io.github.rakshitsharma0402.dataorbit.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.util.stream.Stream;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.rakshitsharma0402.dataorbit.configuration.InvalidStartupSettingException;

/**
 * Milestone 1 evidence: Flyway applies the baseline on startup and records it, and a failed
 * migration, an unreachable database or an invalid setting stops startup. Each test uses its own
 * empty temporary schema in the dataorbit_test database.
 */
class FlywayBaselineTest {

    @Test
    void startupAppliesBaselineToEmptySchemaAndRecordsIt() throws SQLException {
        try (TemporarySchema schema = TemporarySchema.create()) {
            try (ConfigurableApplicationContext context = TestApplication.start(schema.flywaySettings())) {
                Flyway flyway = context.getBean(Flyway.class);
                assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
            }
            assertThat(schema.appliedMigrations()).containsExactly("1:true");
        }
    }

    @Test
    void restartDoesNotReapplyMigrations() throws SQLException {
        try (TemporarySchema schema = TemporarySchema.create()) {
            TestApplication.start(schema.flywaySettings()).close();
            TestApplication.start(schema.flywaySettings()).close();

            assertThat(schema.appliedMigrations()).containsExactly("1:true");
        }
    }

    @Test
    void failedMigrationStopsStartup() throws SQLException {
        try (TemporarySchema schema = TemporarySchema.create()) {
            String[] settings = with(schema.flywaySettings(),
                    "spring.flyway.locations=classpath:db/migration,classpath:db/broken");

            assertThatThrownBy(() -> TestApplication.start(settings))
                    .hasCauseInstanceOf(FlywayException.class)
                    .satisfies(e -> assertThat(e).hasStackTraceContaining("V2__deliberately_broken.sql"));

            // PostgreSQL rolls the failed migration back; only the baseline stays recorded as applied.
            assertThat(schema.appliedMigrations()).containsExactly("1:true");
        }
    }

    @Test
    void unreachableDatabaseStopsStartup() {
        assertThatThrownBy(() -> TestApplication.start(
                        "spring.datasource.url=jdbc:postgresql://127.0.0.1:1/dataorbit_test",
                        "spring.datasource.hikari.connection-timeout=1000"))
                .hasStackTraceContaining("Connection to 127.0.0.1:1 refused");
    }

    @Test
    void invalidSettingStopsStartupBeforeAnyMigration() throws SQLException {
        try (TemporarySchema schema = TemporarySchema.create()) {
            String[] settings = with(schema.flywaySettings(), "server.address=0.0.0.0");

            assertThatThrownBy(() -> TestApplication.start(settings))
                    .hasRootCauseInstanceOf(InvalidStartupSettingException.class);

            assertThat(schema.hasTable("flyway_schema_history")).isFalse();
        }
    }

    @ParameterizedTest(name = "password ''{0}'' fails startup before any migration")
    @ValueSource(strings = {"", "${DATAORBIT_VARIABLE_THAT_IS_NOT_SET}"})
    void missingDatabasePasswordStopsStartupBeforeAnyMigration(String password) throws SQLException {
        try (TemporarySchema schema = TemporarySchema.create()) {
            String[] settings = with(schema.flywaySettings(), "spring.datasource.password=" + password);

            assertThatThrownBy(() -> TestApplication.start(settings))
                    .hasRootCauseInstanceOf(InvalidStartupSettingException.class)
                    .hasStackTraceContaining("'spring.datasource.password': is required");

            assertThat(schema.hasTable("flyway_schema_history")).isFalse();
        }
    }

    private static String[] with(String[] settings, String... more) {
        return Stream.concat(Stream.of(settings), Stream.of(more)).toArray(String[]::new);
    }
}
