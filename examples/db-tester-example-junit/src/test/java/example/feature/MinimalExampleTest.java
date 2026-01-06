package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the minimal convention-based database testing approach.
 *
 * <p>This test illustrates:
 *
 * <ul>
 *   <li>Automatic CSV file resolution based on test class and method names
 *   <li>Method-level {@code @DataSet} and {@code @ExpectedDataSet} annotations
 *   <li>Single table operations with minimal configuration
 *   <li>H2 in-memory database setup
 *   <li>No scenario filtering
 * </ul>
 *
 * <p>CSV files are located at:
 *
 * <ul>
 *   <li>{@code src/test/resources/example/feature/MinimalExampleTest/TABLE1.csv}
 *   <li>{@code src/test/resources/example/feature/MinimalExampleTest/expected/TABLE1.csv}
 * </ul>
 */
@ExtendWith(DatabaseTestExtension.class)
public final class MinimalExampleTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(MinimalExampleTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates MinimalExampleTest instance. */
  public MinimalExampleTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for MinimalExampleTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/MinimalExampleTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:MinimalExampleTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(MinimalExampleTest.class.getClassLoader().getResource(scriptPath))
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
   * Executes a SQL statement against the test database.
   *
   * @param sql the SQL statement to execute
   */
  private static void executeSql(final String sql) {
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    } catch (final SQLException e) {
      throw new RuntimeException(String.format("Failed to execute SQL: %s", sql), e);
    }
  }

  /**
   * Demonstrates the minimal convention-based test.
   *
   * <p>This test uses framework conventions to automatically resolve CSV file locations based on
   * test class and method names without requiring explicit configuration.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1 Mouse, ID=2 Monitor) from {@code TABLE1.csv}
   *   <li>Execution: Inserts ID=3 (Keyboard, 79.99) into TABLE1
   *   <li>Expectation: Verifies all three products from {@code expected/TABLE1.csv}
   * </ul>
   */
  @Test
  @DataSet
  @ExpectedDataSet
  void shouldLoadAndVerifyProductData() {
    logger.info("Running minimal example test");

    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Keyboard', 79.99)");

    logger.info("Product data inserted successfully");
  }
}
