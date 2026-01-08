package example.database.mysql;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * MySQL integration test using Testcontainers.
 *
 * <p>This test validates that the framework works correctly with MySQL database using
 * Testcontainers. This is a smoke test to ensure MySQL compatibility.
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("MySQLIntegrationTest")
final class MySQLIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(MySQLIntegrationTest.class);

  /** Creates MySQL integration test instance. */
  MySQLIntegrationTest() {}

  /** MySQL container for integration testing. */
  @Container
  static final MySQLContainer mysql =
      new MySQLContainer("mysql:latest")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  /**
   * Sets up MySQL database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up MySQL Testcontainer");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(mysql);
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/mysql/mysql-integration.sql");

    logger.info("MySQL database setup completed");
  }

  /**
   * Creates a MySQL DataSource from the Testcontainer.
   *
   * @param container the MySQL container
   * @return configured DataSource
   */
  private static DataSource createDataSource(final MySQLContainer container) {
    final var dataSource = new MysqlDataSource();
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
        Optional.ofNullable(MySQLIntegrationTest.class.getClassLoader().getResource(scriptPath))
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
   * Smoke test verifying basic framework functionality with MySQL.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into MySQL
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should execute basic database operations on MySQL")
  @DataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  @ExpectedDataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnMySQL() {
    // When & Then
    logger.info("Running MySQL integration smoke test");
  }
}
