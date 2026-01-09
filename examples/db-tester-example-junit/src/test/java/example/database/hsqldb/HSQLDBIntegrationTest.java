package example.database.hsqldb;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HSQLDB integration test using in-memory database.
 *
 * <p>This test validates that the framework works correctly with HSQLDB (HyperSQL Database). This
 * is a smoke test to ensure HSQLDB compatibility.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("HSQLDBIntegrationTest")
final class HSQLDBIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(HSQLDBIntegrationTest.class);

  /** Creates HSQLDB integration test instance. */
  HSQLDBIntegrationTest() {}

  /**
   * Sets up HSQLDB in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up HSQLDB in-memory database");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/hsqldb/hsqldb-integration.sql");

    logger.info("HSQLDB database setup completed");
  }

  /**
   * Creates an HSQLDB in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JDBCDataSource();
    dataSource.setURL("jdbc:hsqldb:mem:testdb");
    dataSource.setUser("sa");
    dataSource.setPassword("");
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
        Optional.ofNullable(HSQLDBIntegrationTest.class.getClassLoader().getResource(scriptPath))
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
   * Smoke test verifying basic framework functionality with HSQLDB.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into HSQLDB
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should execute basic database operations on HSQLDB")
  @DataSet(sources = @DataSetSource(scenarioNames = "smokeTest"))
  @ExpectedDataSet(sources = @DataSetSource(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnHSQLDB() {
    // When & Then
    logger.info("Running HSQLDB integration smoke test");
  }
}
