package example.database.derby;

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
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache Derby integration test using in-memory database.
 *
 * <p>This test validates that the framework works correctly with Apache Derby database. This is a
 * smoke test to ensure Derby compatibility.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class DerbyIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(DerbyIntegrationTest.class);

  /** Creates Derby integration test instance. */
  public DerbyIntegrationTest() {}

  /**
   * Sets up Derby in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up Derby in-memory database");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/derby/derby-integration.sql");

    logger.info("Derby database setup completed");
  }

  /**
   * Creates a Derby embedded DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new EmbeddedDataSource();
    dataSource.setDatabaseName("memory:testdb");
    dataSource.setCreateDatabase("create");
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
        Optional.ofNullable(DerbyIntegrationTest.class.getClassLoader().getResource(scriptPath))
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
   * Smoke test verifying basic framework functionality with Derby.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into Derby
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @DataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  @ExpectedDataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnDerby() {
    logger.info("Running Derby integration smoke test");
  }
}
