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
 * Demonstrates NULL value and empty string handling in CSV files.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Using empty cells to represent SQL NULL values
 *   <li>Distinguishing between NULL and empty string in VARCHAR columns
 *   <li>Handling NOT NULL constraints
 *   <li>NULL values in numeric and timestamp columns
 * </ul>
 *
 * <p>CSV format examples and NULL representation:
 *
 * <pre>{@code
 * ID,COLUMN1,COLUMN2,COLUMN3,COLUMN4
 * 1,Required Value,,100,
 * 2,Another Value,Optional Value,200,42
 * }</pre>
 *
 * <p><strong>Important:</strong> Empty cells in CSV files are interpreted as SQL NULL for all
 * column types (VARCHAR, INTEGER, TIMESTAMP, etc.). For VARCHAR columns, to test empty strings
 * versus NULL, use quoted empty string {@code ""} for empty string and empty cell for NULL.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class NullAndEmptyValuesTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(NullAndEmptyValuesTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates NullAndEmptyValuesTest instance. */
  public NullAndEmptyValuesTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for NullAndEmptyValuesTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/NullAndEmptyValuesTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:NullAndEmptyValuesTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
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
   * Executes a SQL script from classpath.
   *
   * @param dataSource the DataSource to execute against
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(NullAndEmptyValuesTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates NULL value handling in CSV files.
   *
   * <p>Validates:
   *
   * <ul>
   *   <li>Empty cells correctly represent SQL NULL values
   *   <li>NULL values in optional (nullable) columns
   *   <li>Empty string vs NULL distinction
   *   <li>NOT NULL constraints are respected
   * </ul>
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1 with NULL COLUMN2/COLUMN4, ID=2 with values)
   *   <li>Execution: Inserts ID=3 (Third Record, NULL, 300, NULL)
   *   <li>Expectation: Verifies all three records including NULL values
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet
  @ExpectedDataSet
  void shouldHandleNullValues() throws Exception {
    logger.info("Running NULL values test");

    // Insert record with NULL values
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (3, 'Third Record', NULL, 300, NULL)
        """);

    logger.info("NULL values test completed successfully");
  }
}
