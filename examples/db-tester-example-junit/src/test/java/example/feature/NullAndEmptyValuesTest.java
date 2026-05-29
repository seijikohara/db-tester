package example.feature;

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
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates NULL value and empty string handling across data formats.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Using empty cells to represent SQL NULL values
 *   <li>Handling NOT NULL constraints
 *   <li>NULL values in numeric columns
 *   <li>The NULL versus empty-string distinction available in JSON and YAML
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
 * <p><strong>Important:</strong> An empty CSV or TSV cell is interpreted as SQL NULL for all column
 * types, and a quoted {@code ""} also materializes as NULL. The delimited formats cannot express a
 * non-null empty string. To distinguish an empty string from NULL, use JSON or YAML, which provide
 * distinct syntax for {@code null} and {@code ""}.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("NullAndEmptyValuesTest")
final class NullAndEmptyValuesTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(NullAndEmptyValuesTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates NullAndEmptyValuesTest instance. */
  NullAndEmptyValuesTest() {}

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
  @Tag("edge-case")
  @DisplayName("should handle NULL values in CSV files correctly")
  @DataSet
  @ExpectedDataSet
  void shouldHandleNullValues() throws Exception {
    // Given
    logger.info("Running NULL values test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (3, 'Third Record', NULL, 300, NULL)
        """);

    // Then
    logger.info("NULL values test completed successfully");
  }

  /**
   * Documents that the CSV loader materializes both a bare empty cell ({@code ,,}) and a quoted
   * empty string ({@code ,"",}) as SQL {@code NULL}.
   *
   * <p>The fixture supplies two rows in the {@code shouldDistinguishEmptyStringFromNullValues}
   * scenario:
   *
   * <ul>
   *   <li>ID=1 - COLUMN2 is left empty
   *   <li>ID=2 - COLUMN2 is quoted as {@code ""}
   * </ul>
   *
   * <p>SQL probes confirm that both forms materialize as {@code NULL}. The delimited parser maps an
   * empty field to NULL, so a delimited file cannot express a non-null empty string. Use JSON or
   * YAML when the empty-string versus NULL distinction matters.
   *
   * @throws SQLException if the probe queries fail
   */
  @Test
  @Tag("edge-case")
  @DisplayName("should normalize quoted empty string to NULL during CSV preparation")
  @DataSet
  @ExpectedDataSet
  void shouldDistinguishEmptyStringFromNullValues() throws SQLException {
    logger.info("Confirming empty cells and quoted empty strings both materialize as NULL");
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {
      try (final var resultSet =
          statement.executeQuery(
              "SELECT COUNT(*) FROM TABLE1 WHERE COLUMN2 IS NULL AND ID IN (1, 2)")) {
        resultSet.next();
        if (resultSet.getInt(1) != 2) {
          throw new AssertionError(
              "Expected COLUMN2 to be NULL for both ID=1 and ID=2 (empty cell and quoted empty)");
        }
      }
      try (final var resultSet =
          statement.executeQuery("SELECT COUNT(*) FROM TABLE1 WHERE COLUMN2 = ''")) {
        resultSet.next();
        if (resultSet.getInt(1) != 0) {
          throw new AssertionError(
              "Loader should not materialize quoted empty cells as empty strings");
        }
      }
    }
    logger.info("Empty-string normalization to NULL confirmed");
  }

  /**
   * Verifies that JSON preserves the distinction between {@code null} and an empty string.
   *
   * <p>The JSON fixture loads two rows into {@code JSON_VALUES}:
   *
   * <ul>
   *   <li>ID=1 - COLUMN2 is JSON {@code null}, which materializes as SQL NULL
   *   <li>ID=2 - COLUMN2 is JSON {@code ""}, which materializes as a non-null empty string
   * </ul>
   *
   * <p>SQL probes confirm the row counts for each form. This demonstrates that JSON and YAML express
   * an empty string that the delimited formats cannot.
   *
   * @throws SQLException if the probe queries fail
   */
  @Test
  @Tag("edge-case")
  @DisplayName("should preserve empty string distinct from NULL when loading JSON")
  @DataSet(
      sources =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/NullAndEmptyValuesTest/jsonEmptyString/"))
  void shouldPreserveEmptyStringDistinctFromNull_whenLoadingJson() throws SQLException {
    logger.info("Confirming JSON null and empty string materialize distinctly");
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {
      try (final var resultSet =
          statement.executeQuery("SELECT COUNT(*) FROM JSON_VALUES WHERE COLUMN2 IS NULL")) {
        resultSet.next();
        if (resultSet.getInt(1) != 1) {
          throw new AssertionError("Expected exactly one NULL COLUMN2 (the JSON null row)");
        }
      }
      try (final var resultSet =
          statement.executeQuery("SELECT COUNT(*) FROM JSON_VALUES WHERE COLUMN2 = ''")) {
        resultSet.next();
        if (resultSet.getInt(1) != 1) {
          throw new AssertionError("Expected exactly one empty-string COLUMN2 (the JSON \"\" row)");
        }
      }
    }
    logger.info("JSON empty-string preservation confirmed");
  }
}
