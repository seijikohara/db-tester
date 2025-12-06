package example.database.oracle;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

/**
 * Oracle Database integration test using Testcontainers.
 *
 * <p>This test validates that the framework works correctly with Oracle Database using
 * Testcontainers. This is a smoke test to ensure Oracle compatibility.
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
public final class OracleIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(OracleIntegrationTest.class);

  /** Creates Oracle integration test instance. */
  public OracleIntegrationTest() {}

  /** Oracle container for integration testing. */
  @Container
  static final OracleContainer oracle =
      new OracleContainer("gvenzl/oracle-free:latest")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  /**
   * Sets up Oracle database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up Oracle Testcontainer");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(oracle);
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/oracle/oracle-integration.sql");

    logger.info("Oracle database setup completed");
  }

  /**
   * Creates an Oracle DataSource from the Testcontainer.
   *
   * @param container the Oracle container
   * @return configured DataSource
   * @throws SQLException if DataSource creation fails
   */
  private static DataSource createDataSource(final OracleContainer container) throws SQLException {
    final var dataSource = new OracleDataSource();
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
        Optional.ofNullable(OracleIntegrationTest.class.getClassLoader().getResource(scriptPath))
            .orElseThrow(
                () -> new IllegalStateException(String.format("Script not found: %s", scriptPath)));

    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement();
        final var inputStream = resource.openStream()) {
      final var sql = new String(inputStream.readAllBytes(), UTF_8);
      Arrays.stream(sql.split(";"))
          .map(String::trim)
          .filter(Predicate.not(String::isEmpty))
          .filter(trimmed -> !trimmed.startsWith("--")) // Filter out comment-only lines
          .forEach(
              trimmed -> {
                try {
                  statement.execute(trimmed);
                } catch (final SQLException e) {
                  // Ignore ORA-00942 (table or view does not exist) for DROP statements
                  if (e.getErrorCode() == 942
                      && trimmed.toUpperCase(Locale.ROOT).contains("DROP TABLE")) {
                    logger.debug("Ignoring error for DROP statement: {}", e.getMessage());
                  } else {
                    throw new RuntimeException(
                        String.format("Failed to execute SQL: %s", trimmed), e);
                  }
                }
              });
    }
  }

  /**
   * Smoke test verifying basic framework functionality with Oracle Database.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into Oracle
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  @Expectation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnOracle() {
    logger.info("Running Oracle integration smoke test");
  }
}
