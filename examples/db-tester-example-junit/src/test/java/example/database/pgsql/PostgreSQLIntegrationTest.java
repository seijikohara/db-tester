package example.database.pgsql;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
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
 * PostgreSQL integration test using Testcontainers.
 *
 * <p>This test validates that the framework works correctly with PostgreSQL database using
 * Testcontainers. This is a smoke test to ensure PostgreSQL compatibility.
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
public final class PostgreSQLIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(PostgreSQLIntegrationTest.class);

  /** Creates PostgreSQL integration test instance. */
  public PostgreSQLIntegrationTest() {}

  /** PostgreSQL container for integration testing. */
  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:latest")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  /**
   * Sets up PostgreSQL database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up PostgreSQL Testcontainer");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(postgres);
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/pgsql/pgsql-integration.sql");

    logger.info("PostgreSQL database setup completed");
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
                PostgreSQLIntegrationTest.class.getClassLoader().getResource(scriptPath))
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
   * Smoke test verifying basic framework functionality with PostgreSQL.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into PostgreSQL
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  @Expectation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnPostgreSQL() {
    logger.info("Running PostgreSQL integration smoke test");
  }
}
