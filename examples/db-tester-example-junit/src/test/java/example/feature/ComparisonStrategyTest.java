package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
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
import java.util.stream.IntStream;
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
 *   <li>{@link ComparisonStrategy#DATE_FLEXIBLE} - Flexible date format comparison
 *   <li>{@link ComparisonStrategy#JSON_EQUIVALENT} - JSON structural comparison
 *   <li>{@link ComparisonStrategy#NOT_NULL} - Only verify the value is not null
 *   <li>{@link ComparisonStrategy#regex(String)} - Match against a regular expression
 * </ul>
 *
 * <p>ComparisonStrategy is used with programmatic assertions via {@link DatabaseAssertion} by
 * creating columns with specific comparison strategies.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ComparisonStrategyTest")
final class ComparisonStrategyTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(ComparisonStrategyTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ComparisonStrategyTest instance. */
  ComparisonStrategyTest() {}

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
    IntStream.range(0, Math.min(columns.size(), values.length))
        .forEach(i -> rowValues.put(columns.get(i), new CellValue(values[i])));
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
    IntStream.range(0, Math.min(columns.size(), values.length))
        .forEach(i -> rowValues.put(columns.get(i), new CellValue(values[i])));
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
    @Tag("normal")
    @DisplayName("should pass when values match exactly")
    void shouldPassWhenValuesMatchExactly() {
      // Given
      logger.info("Testing STRICT strategy with exact match");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");

      // When & Then
      assertDoesNotThrow(() -> DatabaseAssertion.assertEquals(expectedTable, actualTable));
      logger.info("STRICT strategy exact match test completed");
    }

    /** Verifies STRICT strategy fails when values differ. */
    @Test
    @Tag("error")
    @DisplayName("should fail when values differ")
    void shouldFailWhenValuesDiffer() {
      // Given
      logger.info("Testing STRICT strategy with mismatched values");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "Alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "ALICE");

      // When & Then
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

    /** Verifies NUMERIC strategy matches integer and decimal representations of the same value. */
    @Test
    @Tag("normal")
    @DisplayName("should match across numeric types when NUMERIC strategy applied")
    void shouldMatchAcrossNumericTypes_whenNumericStrategyApplied() {
      // Given
      logger.info("Testing NUMERIC strategy across Integer and BigDecimal");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, 100);
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("100.00"));

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.numeric("AMOUNT")));
      logger.info("NUMERIC strategy type coercion test completed");
    }

    /** Verifies NUMERIC strategy ignores trailing zero scale differences. */
    @Test
    @Tag("edge-case")
    @DisplayName("should match scaled decimals when NUMERIC strategy applied")
    void shouldMatchScaledDecimals_whenNumericStrategyApplied() {
      // Given
      logger.info("Testing NUMERIC strategy with trailing zero scale");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("99.99"));
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("99.990"));

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.numeric("AMOUNT")));
      logger.info("NUMERIC strategy scale test completed");
    }

    /** Verifies NUMERIC strategy fails when values are numerically distinct. */
    @Test
    @Tag("error")
    @DisplayName("should fail when values are numerically distinct under NUMERIC strategy")
    void shouldFail_whenValuesAreNumericallyDistinctUnderNumericStrategy() {
      // Given
      logger.info("Testing NUMERIC strategy rejection of distinct values");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("99.99"));
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "AMOUNT"), 1, new BigDecimal("100.00"));

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.numeric("AMOUNT")));
      logger.info("NUMERIC strategy rejection test completed");
    }
  }

  /** Tests for CASE_INSENSITIVE comparison strategy. */
  @Nested
  @DisplayName("CASE_INSENSITIVE Strategy Tests")
  class CaseInsensitiveStrategyTests {

    /** Creates CaseInsensitiveStrategyTests instance. */
    CaseInsensitiveStrategyTests() {}

    /** Verifies default STRICT comparison is case-sensitive. */
    @Test
    @Tag("error")
    @DisplayName("should fail with case difference under default STRICT comparison")
    void shouldFail_withCaseDifferenceUnderDefaultStrictComparison() {
      // Given
      logger.info("Verifying STRICT default is case-sensitive");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "ALICE");

      // When & Then
      assertThrows(
          AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));
      logger.info("STRICT default case-sensitivity test completed");
    }

    /** Verifies CASE_INSENSITIVE strategy ignores letter case. */
    @Test
    @Tag("normal")
    @DisplayName("should match across letter cases when CASE_INSENSITIVE strategy applied")
    void shouldMatchAcrossLetterCases_whenCaseInsensitiveStrategyApplied() {
      // Given
      logger.info("Testing CASE_INSENSITIVE strategy across letter cases");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "ALICE");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.caseInsensitive("NAME")));
      logger.info("CASE_INSENSITIVE strategy match test completed");
    }

    /** Verifies CASE_INSENSITIVE strategy still fails when text content differs. */
    @Test
    @Tag("error")
    @DisplayName("should fail when text differs under CASE_INSENSITIVE strategy")
    void shouldFail_whenTextDiffersUnderCaseInsensitiveStrategy() {
      // Given
      logger.info("Testing CASE_INSENSITIVE strategy rejection of distinct text");
      final var expectedTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "alice");
      final var actualTable = createTable("COMPARISON_TEST", List.of("ID", "NAME"), 1, "bob");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.caseInsensitive("NAME")));
      logger.info("CASE_INSENSITIVE strategy rejection test completed");
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
    @Tag("normal")
    @DisplayName("should skip comparison for ignored columns")
    void shouldSkipComparisonForIgnoredColumns() {
      // Given
      logger.info("Testing IGNORE strategy using assertEqualsIgnoreColumns");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-01-01");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-12-31");

      // When & Then
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

    /**
     * Verifies NOT_NULL strategy tolerates an actual value that differs from the expected
     * placeholder so long as the actual value is not null.
     */
    @Test
    @Tag("normal")
    @DisplayName("should match any non-null actual when NOT_NULL strategy applied")
    void shouldMatchAnyNonNullActual_whenNotNullStrategyApplied() {
      // Given
      logger.info("Testing NOT_NULL strategy with differing non-null values");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "any-placeholder");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "0xFEED-CAFE-1234");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.notNull("GENERATED_ID")));
      logger.info("NOT_NULL strategy non-null match test completed");
    }

    /** Verifies NOT_NULL strategy fails when the actual value is null. */
    @Test
    @Tag("error")
    @DisplayName("should fail when actual is null under NOT_NULL strategy")
    @SuppressWarnings("NullAway")
    void shouldFail_whenActualIsNullUnderNotNullStrategy() {
      // Given
      logger.info("Testing NOT_NULL strategy rejection of null actual value");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, "any-placeholder");
      final Object nullValue = null;
      final var actualTable =
          createTableWithNullableValues(
              "COMPARISON_TEST", List.of("ID", "GENERATED_ID"), 1, nullValue);

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.notNull("GENERATED_ID")));
      logger.info("NOT_NULL strategy null rejection test completed");
    }
  }

  /** Tests for TIMESTAMP_FLEXIBLE comparison strategy. */
  @Nested
  @DisplayName("TIMESTAMP_FLEXIBLE Strategy Tests")
  class TimestampFlexibleStrategyTests {

    /** Creates TimestampFlexibleStrategyTests instance. */
    TimestampFlexibleStrategyTests() {}

    /** Verifies TIMESTAMP_FLEXIBLE strategy matches timestamps with different precision. */
    @Test
    @Tag("normal")
    @DisplayName("should match timestamps with different sub-second precision")
    void shouldMatchTimestampsWithDifferentPrecision() {
      // Given
      logger.info("Testing TIMESTAMP_FLEXIBLE strategy with different precision");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00.000");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.timestampFlexible("TIMESTAMP")));
      logger.info("TIMESTAMP_FLEXIBLE precision test completed");
    }

    /** Verifies TIMESTAMP_FLEXIBLE strategy fails when dates differ. */
    @Test
    @Tag("error")
    @DisplayName("should fail when timestamp dates differ")
    void shouldFailWhenTimestampDatesDiffer() {
      // Given
      logger.info("Testing TIMESTAMP_FLEXIBLE strategy with different dates");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-07-15T10:30:00");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.timestampFlexible("TIMESTAMP")));
      logger.info("TIMESTAMP_FLEXIBLE date mismatch test completed");
    }

    /** Verifies TIMESTAMP_FLEXIBLE strategy treats the same instant across offsets as equal. */
    @Test
    @Tag("normal")
    @DisplayName("should match same instant across timezone offsets")
    void shouldMatchSameInstant_acrossTimezoneOffsets() {
      // Given
      logger.info("Testing TIMESTAMP_FLEXIBLE strategy with same instant across offsets");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00+09:00");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T01:30:00Z");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.timestampFlexible("TIMESTAMP")));
      logger.info("TIMESTAMP_FLEXIBLE timezone equivalence test completed");
    }

    /** Verifies TIMESTAMP_FLEXIBLE strategy distinguishes different instants across offsets. */
    @Test
    @Tag("error")
    @DisplayName("should fail when offsets shift instant under TIMESTAMP_FLEXIBLE")
    void shouldFail_whenOffsetsShiftInstantUnderTimestampFlexible() {
      // Given
      logger.info("Testing TIMESTAMP_FLEXIBLE strategy with distinct instants across offsets");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00+09:00");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00Z");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.timestampFlexible("TIMESTAMP")));
      logger.info("TIMESTAMP_FLEXIBLE distinct instant test completed");
    }
  }

  /** Tests for DATE_FLEXIBLE comparison strategy. */
  @Nested
  @DisplayName("DATE_FLEXIBLE Strategy Tests")
  class DateFlexibleStrategyTests {

    /** Creates DateFlexibleStrategyTests instance. */
    DateFlexibleStrategyTests() {}

    /** Verifies DATE_FLEXIBLE strategy matches dates in different formats. */
    @Test
    @Tag("normal")
    @DisplayName("should match dates in different formats")
    void shouldMatchDatesInDifferentFormats() {
      // Given
      logger.info("Testing DATE_FLEXIBLE strategy with different formats");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024-06-15");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024/06/15");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.dateFlexible("BIRTH_DATE")));
      logger.info("DATE_FLEXIBLE format test completed");
    }

    /** Verifies DATE_FLEXIBLE strategy fails when dates differ. */
    @Test
    @Tag("error")
    @DisplayName("should fail when dates differ")
    void shouldFailWhenDatesDiffer() {
      // Given
      logger.info("Testing DATE_FLEXIBLE strategy with different dates");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024-06-15");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024-07-20");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.dateFlexible("BIRTH_DATE")));
      logger.info("DATE_FLEXIBLE mismatch test completed");
    }

    /** Verifies DATE_FLEXIBLE strategy supports the dot-delimited date format. */
    @Test
    @Tag("normal")
    @DisplayName("should match dot-delimited date format")
    void shouldMatchDotDelimitedDateFormat() {
      // Given
      logger.info("Testing DATE_FLEXIBLE strategy with dot-delimited format");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024-06-15");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "BIRTH_DATE"), 1, "2024.06.15");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.dateFlexible("BIRTH_DATE")));
      logger.info("DATE_FLEXIBLE dot format test completed");
    }
  }

  /** Tests for JSON_EQUIVALENT comparison strategy. */
  @Nested
  @DisplayName("JSON_EQUIVALENT Strategy Tests")
  class JsonEquivalentStrategyTests {

    /** Creates JsonEquivalentStrategyTests instance. */
    JsonEquivalentStrategyTests() {}

    /** Verifies JSON_EQUIVALENT strategy ignores key order and whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should match JSON with different key order")
    void shouldMatchJsonWithDifferentKeyOrder() {
      // Given
      logger.info("Testing JSON_EQUIVALENT strategy with different key order");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"name\": \"Alice\", \"age\": 30}");
      final var actualTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"age\": 30, \"name\": \"Alice\"}");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent("METADATA")));
      logger.info("JSON_EQUIVALENT key order test completed");
    }

    /** Verifies JSON_EQUIVALENT strategy fails when JSON values differ. */
    @Test
    @Tag("error")
    @DisplayName("should fail when JSON values differ")
    void shouldFailWhenJsonValuesDiffer() {
      // Given
      logger.info("Testing JSON_EQUIVALENT strategy with different values");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"name\": \"Alice\", \"age\": 30}");
      final var actualTable =
          createTable(
              "COMPARISON_TEST", List.of("ID", "METADATA"), 1, "{\"name\": \"Bob\", \"age\": 25}");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent("METADATA")));
      logger.info("JSON_EQUIVALENT mismatch test completed");
    }

    /** Verifies JSON_EQUIVALENT strategy normalizes key order within nested objects. */
    @Test
    @Tag("normal")
    @DisplayName("should match nested JSON when keys are reordered")
    void shouldMatchNestedJson_whenKeysAreReordered() {
      // Given
      logger.info("Testing JSON_EQUIVALENT strategy with nested object key reordering");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"user\":{\"name\":\"Alice\",\"roles\":[\"admin\",\"user\"]}}");
      final var actualTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"user\":{\"roles\":[\"admin\",\"user\"],\"name\":\"Alice\"}}");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent("METADATA")));
      logger.info("JSON_EQUIVALENT nested key reorder test completed");
    }

    /** Verifies JSON_EQUIVALENT strategy preserves array element order. */
    @Test
    @Tag("error")
    @DisplayName("should fail when nested array order differs")
    void shouldFail_whenNestedArrayOrderDiffers() {
      // Given
      logger.info("Testing JSON_EQUIVALENT strategy preserves array ordering");
      final var expectedTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"tags\":[\"alpha\",\"beta\",\"gamma\"]}");
      final var actualTable =
          createTable(
              "COMPARISON_TEST",
              List.of("ID", "METADATA"),
              1,
              "{\"tags\":[\"gamma\",\"alpha\",\"beta\"]}");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent("METADATA")));
      logger.info("JSON_EQUIVALENT array order rejection test completed");
    }
  }

  /** Tests for REGEX comparison strategy. */
  @Nested
  @DisplayName("REGEX Strategy Tests")
  class RegexStrategyTests {

    /** Email validation pattern used by the REGEX strategy tests. */
    private static final String EMAIL_PATTERN = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}";

    /** Creates RegexStrategyTests instance. */
    RegexStrategyTests() {}

    /**
     * Verifies REGEX strategy matches an actual value against the configured pattern, ignoring the
     * placeholder expected value.
     */
    @Test
    @Tag("normal")
    @DisplayName("should match actual against pattern when REGEX strategy applied")
    void shouldMatchActualAgainstPattern_whenRegexStrategyApplied() {
      // Given
      logger.info("Testing REGEX strategy with conforming actual value");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "<PATTERN_EMAIL>");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "alice@example.com");

      // When & Then
      assertDoesNotThrow(
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.regex("EMAIL", EMAIL_PATTERN)));
      logger.info("REGEX strategy match test completed");
    }

    /** Verifies REGEX strategy fails when the actual value does not satisfy the pattern. */
    @Test
    @Tag("error")
    @DisplayName("should fail when actual does not match pattern under REGEX strategy")
    void shouldFail_whenActualDoesNotMatchPatternUnderRegexStrategy() {
      // Given
      logger.info("Testing REGEX strategy rejection of non-conforming actual value");
      final var expectedTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "<PATTERN_EMAIL>");
      final var actualTable =
          createTable("COMPARISON_TEST", List.of("ID", "EMAIL"), 1, "invalid-email");

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              DatabaseAssertion.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  ColumnStrategyMapping.regex("EMAIL", EMAIL_PATTERN)));
      logger.info("REGEX strategy rejection test completed");
    }
  }
}
