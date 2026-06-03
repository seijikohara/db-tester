package example.database.pgsql;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL integration test for the {@code ARRAY} {@link
 * io.github.seijikohara.dbtester.api.spi.TypeHandler}.
 *
 * <p>This test validates that the framework reads and writes native PostgreSQL {@code TEXT[]}
 * columns through the registered array type handler: the dataset stores the value as a PostgreSQL
 * array literal, the preparation phase parses it into a JDBC array, and the expectation phase formats
 * the database array back into the same literal.
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("PostgreSQLArrayTypeIntegrationTest")
final class PostgreSQLArrayTypeIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger =
      LoggerFactory.getLogger(PostgreSQLArrayTypeIntegrationTest.class);

  /** Creates the integration test instance. */
  PostgreSQLArrayTypeIntegrationTest() {}

  /** PostgreSQL container for integration testing. */
  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:latest")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  /**
   * Sets up the PostgreSQL database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(postgres);
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/pgsql/pgsql-array.sql");
    logger.info("PostgreSQL array type test setup completed");
  }

  /**
   * Creates a PostgreSQL DataSource from the Testcontainer.
   *
   * @param container the PostgreSQL container
   * @return configured DataSource
   */
  private static DataSource createDataSource(final PostgreSQLContainer container) {
    final var dataSource = new PGSimpleDataSource();
    dataSource.setURL(container.getJdbcUrl());
    dataSource.setUser(container.getUsername());
    dataSource.setPassword(container.getPassword());
    return dataSource;
  }

  /**
   * Executes a SQL script from classpath.
   *
   * @param dataSource the DataSource to execute against
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(
                PostgreSQLArrayTypeIntegrationTest.class.getClassLoader().getResource(scriptPath))
            .orElseThrow(
                () -> new IllegalStateException(String.format("Script not found: %s", scriptPath)));

    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement();
        final var inputStream = resource.openStream()) {
      final var sql = new String(inputStream.readAllBytes(), UTF_8);
      Arrays.stream(sql.split(";"))
          .map(String::trim)
          .filter(Predicate.not(String::isEmpty))
          .forEach(
              trimmed -> {
                try {
                  statement.execute(trimmed);
                } catch (final SQLException e) {
                  throw new RuntimeException(
                      String.format("Failed to execute SQL: %s", trimmed), e);
                }
              });
    }
  }

  /**
   * Verifies that a native PostgreSQL {@code TEXT[]} column round-trips through the array type
   * handler during preparation and expectation.
   */
  @Test
  @Tag("normal")
  @DisplayName("should round-trip TEXT[] arrays through the array type handler")
  @DataSet
  @ExpectedDataSet
  void shouldRoundTripTextArrays_whenUsingArrayTypeHandler() {
    // When & Then
    logger.info("Running PostgreSQL array type handler round-trip test");
  }
}
