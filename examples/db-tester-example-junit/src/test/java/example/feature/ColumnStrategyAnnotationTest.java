package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.annotation.Strategy;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates annotation-based column comparison strategies using {@code @ColumnStrategy}.
 *
 * <p>This test demonstrates how to configure column-level comparison strategies via annotations:
 *
 * <ul>
 *   <li>{@link Strategy#IGNORE} - Skip comparison for auto-generated columns
 *   <li>{@link Strategy#CASE_INSENSITIVE} - Compare strings ignoring case differences
 *   <li>{@link Strategy#NUMERIC} - Type-aware numeric comparison
 *   <li>{@link Strategy#NOT_NULL} - Only verify value is not null
 *   <li>{@link Strategy#REGEX} - Match against regular expression pattern
 *   <li>{@link Strategy#TIMESTAMP_FLEXIBLE} - Flexible timestamp comparison
 * </ul>
 *
 * <p>Directory structure:
 *
 * <pre>
 * example/feature/ColumnStrategyAnnotationTest/
 *   USERS.csv
 *   expected/
 *     USERS.csv
 * </pre>
 *
 * @see ColumnStrategy
 * @see Strategy
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ColumnStrategyAnnotationTest")
final class ColumnStrategyAnnotationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(ColumnStrategyAnnotationTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates ColumnStrategyAnnotationTest instance. */
  ColumnStrategyAnnotationTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for ColumnStrategyAnnotationTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ColumnStrategyAnnotationTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ColumnStrategyAnnotationTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(
                ColumnStrategyAnnotationTest.class.getClassLoader().getResource(scriptPath))
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

  /** Tests for IGNORE strategy. */
  @Nested
  @DisplayName("IGNORE Strategy")
  class IgnoreStrategyTests {

    /** Creates IgnoreStrategyTests instance. */
    IgnoreStrategyTests() {}

    /**
     * Demonstrates using IGNORE strategy for auto-generated timestamp columns.
     *
     * <p>The CREATED_AT column is auto-generated by the database and cannot be predicted in test
     * data. Using {@link Strategy#IGNORE} skips comparison for this column.
     */
    @Test
    @Tag("normal")
    @DisplayName("should ignore auto-generated timestamp column")
    @DataSet
    @ExpectedDataSet(
        dataSets =
            @DataSetSource(
                columnStrategies =
                    @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE)))
    void shouldIgnoreTimestampColumn() {
      // Given
      logger.info("Testing IGNORE strategy for CREATED_AT column");

      // When - insert with auto-generated timestamp
      executeSql(
          """
          INSERT INTO USERS (ID, NAME, EMAIL, CREATED_AT)
          VALUES (2, 'Bob', 'bob@example.com', CURRENT_TIMESTAMP)
          """);

      // Then - CREATED_AT is ignored, only ID, NAME, EMAIL are compared
      logger.info("IGNORE strategy test completed");
    }
  }

  /** Tests for CASE_INSENSITIVE strategy. */
  @Nested
  @DisplayName("CASE_INSENSITIVE Strategy")
  class CaseInsensitiveStrategyTests {

    /** Creates CaseInsensitiveStrategyTests instance. */
    CaseInsensitiveStrategyTests() {}

    /**
     * Demonstrates using CASE_INSENSITIVE strategy for email comparison.
     *
     * <p>Email addresses are often normalized to lowercase by the application. Using {@link
     * Strategy#CASE_INSENSITIVE} allows comparing expected values regardless of case.
     */
    @Test
    @Tag("normal")
    @DisplayName("should compare email case-insensitively")
    @DataSet
    @ExpectedDataSet(
        dataSets =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
                  @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE)
                }))
    void shouldCompareCaseInsensitively() {
      // Given
      logger.info("Testing CASE_INSENSITIVE strategy for EMAIL column");

      // When - insert with lowercase email (application normalizes)
      executeSql(
          """
          INSERT INTO USERS (ID, NAME, EMAIL, CREATED_AT)
          VALUES (2, 'Charlie', 'charlie@example.com', CURRENT_TIMESTAMP)
          """);

      // Then - expected has 'CHARLIE@EXAMPLE.COM' but passes due to case-insensitive comparison
      logger.info("CASE_INSENSITIVE strategy test completed");
    }
  }

  /** Tests for REGEX strategy. */
  @Nested
  @DisplayName("REGEX Strategy")
  class RegexStrategyTests {

    /** Creates RegexStrategyTests instance. */
    RegexStrategyTests() {}

    /**
     * Demonstrates using REGEX strategy for UUID validation.
     *
     * <p>UUIDs are auto-generated and cannot be predicted. Using {@link Strategy#REGEX} with a UUID
     * pattern validates the format without requiring exact value match.
     */
    @Test
    @Tag("normal")
    @DisplayName("should validate UUID format using regex pattern")
    @DataSet
    @ExpectedDataSet(
        dataSets =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(
                      name = "TOKEN",
                      strategy = Strategy.REGEX,
                      pattern = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"),
                  @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE)
                }))
    void shouldValidateUuidFormat() {
      // Given
      logger.info("Testing REGEX strategy for TOKEN column");

      // When - insert with generated UUID
      executeSql(
          """
          INSERT INTO USERS (ID, NAME, EMAIL, TOKEN, CREATED_AT)
          VALUES (2, 'Diana', 'diana@example.com', RANDOM_UUID(), CURRENT_TIMESTAMP)
          """);

      // Then - TOKEN is validated against UUID pattern
      logger.info("REGEX strategy test completed");
    }
  }

  /** Tests for multiple column strategies. */
  @Nested
  @DisplayName("Multiple Column Strategies")
  class MultipleStrategiesTests {

    /** Creates MultipleStrategiesTests instance. */
    MultipleStrategiesTests() {}

    /**
     * Demonstrates combining multiple column strategies in a single test.
     *
     * <p>Real-world scenarios often require different comparison strategies for different columns.
     * This test shows how to combine IGNORE, CASE_INSENSITIVE, and REGEX strategies.
     */
    @Test
    @Tag("normal")
    @DisplayName("should apply multiple column strategies")
    @DataSet
    @ExpectedDataSet(
        dataSets =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
                  @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
                  @ColumnStrategy(
                      name = "TOKEN",
                      strategy = Strategy.REGEX,
                      pattern = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
                }))
    void shouldApplyMultipleStrategies() {
      // Given
      logger.info("Testing multiple column strategies");

      // When
      executeSql(
          """
          INSERT INTO USERS (ID, NAME, EMAIL, TOKEN, CREATED_AT)
          VALUES (2, 'Eve', 'eve@example.com', RANDOM_UUID(), CURRENT_TIMESTAMP)
          """);

      // Then - each column uses its configured strategy
      logger.info("Multiple strategies test completed");
    }
  }
}
