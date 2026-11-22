package io.github.rakshitsharma0402.dataorbit.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A uniquely named, empty schema in the test database, dropped on close. Tests that need a
 * database "from empty" use one, so they never depend on, or damage, any other schema.
 *
 * <p>Connection settings match src/test/resources/config/application.yml.
 */
final class TemporarySchema implements AutoCloseable {

    private static final String URL = env("DATAORBIT_TEST_DB_URL", "jdbc:postgresql://127.0.0.1:5432/dataorbit_test");
    private static final String USER = "dataorbit";
    private static final String PASSWORD = env("DATAORBIT_DB_PASSWORD", "unset-test-password");

    private final String name;

    private TemporarySchema(String name) {
        this.name = name;
    }

    /** Creates a new empty schema named {@code it_<random hex>}. */
    static TemporarySchema create() throws SQLException {
        byte[] random = new byte[6];
        ThreadLocalRandom.current().nextBytes(random);
        TemporarySchema schema = new TemporarySchema("it_" + HexFormat.of().formatHex(random));
        schema.execute("CREATE SCHEMA " + schema.name);
        return schema;
    }

    String name() {
        return name;
    }

    /** Settings that point Flyway, and the app's default schema, at this schema. */
    String[] flywaySettings() {
        return new String[] {"spring.flyway.schemas=" + name, "spring.flyway.default-schema=" + name};
    }

    boolean hasTable(String table) throws SQLException {
        return !query("SELECT 1 FROM information_schema.tables WHERE table_schema = '" + name
                + "' AND table_name = '" + Objects.requireNonNull(table) + "'").isEmpty();
    }

    /** Rows of {@code flyway_schema_history} for versioned migrations, as "version:success". */
    List<String> appliedMigrations() throws SQLException {
        return query("SELECT version || ':' || success FROM " + name
                + ".flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank");
    }

    @Override
    public void close() throws SQLException {
        execute("DROP SCHEMA IF EXISTS " + name + " CASCADE");
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private List<String> query(String sql) throws SQLException {
        try (Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            List<String> values = new ArrayList<>();
            while (rows.next()) {
                values.add(rows.getString(1));
            }
            return values;
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
