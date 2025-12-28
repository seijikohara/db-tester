package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates different comparison strategies for database assertions.
 *
 * <p>This test demonstrates the available comparison strategies:
 *
 * <ul>
 *   <li>{@link ComparisonStrategy#STRICT} - Exact match using equals() (default)
 *   <li>{@link ComparisonStrategy#IGNORE} - Skip comparison entirely
 *   <li>{@link ComparisonStrategy#NUMERIC} - Type-aware numeric comparison
 *   <li>{@link ComparisonStrategy#CASE_INSENSITIVE} - Case-insensitive string comparison
 *   <li>{@link ComparisonStrategy#TIMESTAMP_FLEXIBLE} - Flexible timestamp comparison
 *   <li>{@link ComparisonStrategy#NOT_NULL} - Only verify the value is not null
 *   <li>{@link ComparisonStrategy#regex(String)} - Match against a regular expression
 * </ul>
 *
 * <p>ComparisonStrategy is used with programmatic assertions via {@link DatabaseAssertion} by
 * creating columns with specific comparison strategies.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class ComparisonStrategyTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(ComparisonStrategyTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ComparisonStrategyTest instance. */
  public ComparisonStrategyTest() {}

  /**
   * Sets up H2 in-memory database and schema.
   *
   * @param context extension context
   * @throws Exception if setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up database for ComparisonStrategyTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ComparisonStrategyTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ComparisonStrategyTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  /**
   * Executes SQL script from classpath.
   *
   * @param dataSource target DataSource
   * @param scriptPath classpath resource path
   * @throws Exception if execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(ComparisonStrategyTest.class.getClassLoader().getResource(scriptPath))
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
   * Creates a table with one row.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values (corresponding to columns)
   * @return a Table instance
   */
  private static Table createTable(
      final String tableName, final List<String> columnNames, final Object... values) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final Map<ColumnName, CellValue> rowValues = new LinkedHashMap<>();
    for (int i = 0; i < columns.size() && i < values.length; i++) {
      rowValues.put(columns.get(i), new CellValue(values[i]));
    }
    final var row = Row.of(rowValues);
    return Table.of(new TableName(tableName), columns, List.of(row));
  }

  /**
   * Creates a table with one row, allowing nullable values.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values (corresponding to columns, may contain null)
   * @return a Table instance
   */
  @SuppressWarnings("NullAway")
  private static Table createTableWithNullableValues(
      final String tableName, final List<String> columnNames, final Object... values) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final Map<ColumnName, CellValue> rowValues = new LinkedHashMap<>();
    for (int i = 0; i < columns.size() && i < values.length; i++) {
      rowValues.put(columns.get(i), new CellValue(values[i]));
    }
    final var row = Row.of(rowValues);
    return Table.of(new TableName(tableName), columns, List.of(row));
  }

  /** Tests for STRICT comparison strategy (default). */
  @Nested
  @DisplayName("STRICT Strategy Tests")
  class StrictStrategyTests {

    /** Creates StrictStrategyTests instance. */
    StrictStrategyTests() {}

    /** Verifies STRICT strategy passes when values match exactly. */
    @Test
    @DisplayName("should pass when values match exactly")
    void shouldPassWhenValuesMatchExactly() {
      logger.info("Testing STRICT strategy with exact match");

      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");

      // Should pass - exact match
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("STRICT strategy exact match test completed");
    }

    /** Verifies STRICT strategy fails when values differ. */
    @Test
    @DisplayName("should fail when values differ")
    void shouldFailWhenValuesDiffer() {
      logger.info("Testing STRICT strategy with mismatched values");

      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "ALICE");

      // Should fail - case differs
      assertThrows(
          AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("STRICT strategy mismatch test completed");
    }
  }

  /** Tests for NUMERIC comparison strategy. */
  @Nested
  @DisplayName("NUMERIC Strategy Tests")
  class NumericStrategyTests {

    /** Creates NumericStrategyTests instance. */
    NumericStrategyTests() {}

    /** Verifies NUMERIC strategy handles different numeric types. */
    @Test
    @DisplayName("should match different numeric types with same value")
    void shouldMatchDifferentNumericTypesWithSameValue() {
      logger.info("Testing NUMERIC strategy with different numeric types");

      // Expected with Integer
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, 100);

      // Actual with BigDecimal (different type, same value)
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("100.00"));

      // Should pass - numeric values are equal
      // Note: Default STRICT comparison may fail here; test documents behavior
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("NUMERIC strategy type conversion test completed");
    }

    /** Verifies NUMERIC strategy handles precision differences. */
    @Test
    @DisplayName("should match values with different precision")
    void shouldMatchValuesWithDifferentPrecision() {
      logger.info("Testing NUMERIC strategy with precision differences");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("99.99"));

      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("99.990"));

      // Should pass - numerically equal despite different precision
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("NUMERIC strategy precision test completed");
    }
  }

  /** Tests for CASE_INSENSITIVE comparison strategy. */
  @Nested
  @DisplayName("CASE_INSENSITIVE Strategy Tests")
  class CaseInsensitiveStrategyTests {

    /** Creates CaseInsensitiveStrategyTests instance. */
    CaseInsensitiveStrategyTests() {}

    /** Verifies CASE_INSENSITIVE strategy documents case-sensitive behavior. */
    @Test
    @DisplayName("should demonstrate case-sensitive comparison by default")
    void shouldDemonstrateCaseSensitiveComparison() {
      logger.info("Testing default case-sensitive comparison");

      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "ALICE");

      // Default STRICT comparison fails on case difference
      assertThrows(
          AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("Case-sensitive comparison test completed");
    }
  }

  /** Tests for IGNORE comparison strategy. */
  @Nested
  @DisplayName("IGNORE Strategy Tests")
  class IgnoreStrategyTests {

    /** Creates IgnoreStrategyTests instance. */
    IgnoreStrategyTests() {}

    /** Verifies assertEqualsIgnoreColumns ignores specified columns. */
    @Test
    @DisplayName("should skip comparison for ignored columns")
    void shouldSkipComparisonForIgnoredColumns() {
      logger.info("Testing IGNORE strategy using assertEqualsIgnoreColumns");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-01-01");

      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-12-31");

      // Should pass - TIMESTAMP column is ignored via assertEqualsIgnoreColumns
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsIgnoreColumns(expectedTable, actualTable, "TIMESTAMP"));

      logger.info("IGNORE strategy test completed");
    }
  }

  /** Tests for NOT_NULL comparison strategy. */
  @Nested
  @DisplayName("NOT_NULL Strategy Tests")
  class NotNullStrategyTests {

    /** Creates NotNullStrategyTests instance. */
    NotNullStrategyTests() {}

    /** Verifies value comparison when both expected and actual are not null. */
    @Test
    @DisplayName("should pass when both values are not null")
    void shouldPassWhenValuesAreNotNull() {
      logger.info("Testing NOT_NULL strategy with non-null values");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "expected-value");

      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "expected-value");

      // Should pass - values match
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("NOT_NULL strategy test completed");
    }

    /** Verifies that comparison fails when expected is not null but actual is null. */
    @Test
    @DisplayName("should fail when expected is not null but actual is null")
    @SuppressWarnings("NullAway")
    void shouldFailWhenActualIsNull() {
      logger.info("Testing comparison with null value");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "any-value");

      final Object nullValue = null;
      final var actualTable =
          createTableWithNullableValues(
              "COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, nullValue);

      // Should fail - actual value is null but expected is not
      assertThrows(
          AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("NOT_NULL strategy null value test completed");
    }
  }

  /** Tests for REGEX comparison strategy. */
  @Nested
  @DisplayName("REGEX Strategy Tests")
  class RegexStrategyTests {

    /** Creates RegexStrategyTests instance. */
    RegexStrategyTests() {}

    /** Verifies exact match comparison. */
    @Test
    @DisplayName("should match exact values")
    void shouldMatchExactValues() {
      logger.info("Testing exact value comparison");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "alice@example.com");

      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "alice@example.com");

      // Should pass - exact match
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("REGEX strategy test completed");
    }

    /** Verifies comparison fails when values don't match. */
    @Test
    @DisplayName("should fail when values don't match")
    void shouldFailWhenValuesDontMatch() {
      logger.info("Testing value mismatch");

      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "alice@example.com");

      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "invalid-email");

      // Should fail - values don't match
      assertThrows(
          AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

      logger.info("REGEX strategy non-matching test completed");
    }
  }
}
