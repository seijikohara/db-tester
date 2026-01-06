package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Comparator for TableSet and Table objects.
 *
 * <p>This class provides methods to compare datasets and tables, collecting all differences rather
 * than failing on the first mismatch. This enables users to see all issues at once.
 *
 * <p>The comparison results are formatted in a structured, human-readable format that groups errors
 * by table and provides clear expected/actual values.
 */
public class DataSetComparator {

  /** Set of string representations for boolean true values. */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "y");

  /** Set of string representations for boolean false values. */
  private static final Set<String> FALSE_VALUES = Set.of("0", "false", "no", "n");

  /** Pattern for matching timestamps with optional trailing zeros. */
  private static final Pattern TIMESTAMP_PATTERN =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.0+)?");

  /** Pattern for matching trailing zeros in timestamps. */
  private static final Pattern TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0+$");

  /** Creates a new dataset comparator. */
  public DataSetComparator() {
    // Default constructor
  }

  /**
   * Asserts that two datasets are equal.
   *
   * <p>Compares table counts and individual tables between expected and actual datasets. Collects
   * all differences and reports them in a single structured error message.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final TableSet expected,
      final TableSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var result = new ComparisonResult();
    final var expectedTables = expected.getTables();
    final var actualTables = actual.getTables();

    if (expectedTables.size() != actualTables.size()) {
      result.addTableCountMismatch(expectedTables.size(), actualTables.size());
    }

    // Compare each expected table with actual
    expectedTables.forEach(
        expectedTable -> {
          final var tableName = expectedTable.getName();
          actual
              .getTable(tableName)
              .ifPresentOrElse(
                  actualTable -> compareTable(expectedTable, actualTable, result),
                  () -> result.addMissingTable(tableName.value()));
        });

    // Report all differences
    if (result.hasDifferences()) {
      handleFailure(result, failureHandler);
    }
  }

  /**
   * Asserts that two tables are equal.
   *
   * <p>Compares row counts and individual rows between expected and actual tables. Collects all
   * differences and reports them in a single structured error message.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var result = new ComparisonResult();
    compareTable(expected, actual, result);

    if (result.hasDifferences()) {
      handleFailure(result, failureHandler);
    }
  }

  /**
   * Compares two tables and collects differences into the result.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param result the result collector
   */
  private void compareTable(
      final Table expected, final Table actual, final ComparisonResult result) {
    final var tableName = expected.getName().value();
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    checkRowCountMismatch(tableName, expectedRows, actualRows, result);
    compareRowPairs(tableName, expectedRows, actualRows, result, this::compareRows);
  }

  /**
   * Asserts that two tables are equal, including additional columns in comparison.
   *
   * <p>This method compares all columns from the expected table plus any additional columns
   * specified. This is useful when the actual table may contain columns not present in the expected
   * table that should also be compared.
   *
   * <p>The comparison includes:
   *
   * <ul>
   *   <li>All columns present in the expected table
   *   <li>Any additional columns specified in {@code additionalColumnNames}
   * </ul>
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param additionalColumnNames additional column names to include in comparison
   * @throws AssertionError if row counts differ or if any column values differ
   */
  public void assertEqualsWithAdditionalColumns(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    final var result = new ComparisonResult();
    final var tableName = expected.getName().value();
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    checkRowCountMismatch(tableName, expectedRows, actualRows, result);

    // Combine expected columns with additional columns
    final var columnsToCompare =
        Stream.concat(
                expected.getColumns().stream(), additionalColumnNames.stream().map(ColumnName::new))
            .collect(Collectors.toSet());

    compareRowPairs(
        tableName,
        expectedRows,
        actualRows,
        result,
        (table, rowIndex, expectedRow, actualRow, res) ->
            compareRowsWithColumns(table, rowIndex, expectedRow, actualRow, columnsToCompare, res));

    result.assertNoDifferences();
  }

  /**
   * Compares two rows using only the specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param columnsToCompare the set of columns to compare
   * @param result the result collector
   */
  private void compareRowsWithColumns(
      final String tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<ColumnName> columnsToCompare,
      final ComparisonResult result) {
    compareRowColumns(tableName, rowIndex, expected, actual, columnsToCompare, result);
  }

  /**
   * Asserts that two datasets are equal for a specific table, ignoring specified columns.
   *
   * <p>Extracts the specified table from both datasets and compares them while ignoring the
   * specified columns.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param tableName the table name to compare
   * @param ignoreColumnNames columns to ignore during comparison
   * @throws AssertionError if the table is not found in either dataset or if values differ
   */
  public void assertEqualsIgnoreColumns(
      final TableSet expected,
      final TableSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var tableNameObj = new TableName(tableName);
    final var expectedTable =
        expected
            .getTable(tableNameObj)
            .orElseThrow(
                () -> new AssertionError(String.format("Expected table not found: %s", tableName)));
    final var actualTable =
        actual
            .getTable(tableNameObj)
            .orElseThrow(
                () -> new AssertionError(String.format("Actual table not found: %s", tableName)));

    assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumnNames);
  }

  /**
   * Asserts that two tables are equal, ignoring specified columns.
   *
   * <p>Compares all columns except those specified in the ignore set.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param ignoreColumnNames columns to ignore during comparison
   * @throws AssertionError if row counts differ or if any non-ignored column values differ
   */
  public void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    final var result = new ComparisonResult();
    final var ignoreSet = Set.copyOf(ignoreColumnNames);
    final var tableName = expected.getName().value();
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    checkRowCountMismatch(tableName, expectedRows, actualRows, result);

    compareRowPairs(
        tableName,
        expectedRows,
        actualRows,
        result,
        (table, rowIndex, expectedRow, actualRow, res) ->
            compareRowsIgnoreColumns(table, rowIndex, expectedRow, actualRow, ignoreSet, res));

    result.assertNoDifferences();
  }

  /**
   * Checks if expected and actual row lists have different sizes and records the mismatch.
   *
   * @param tableName the table name for error reporting
   * @param expectedRows the expected rows
   * @param actualRows the actual rows
   * @param result the result collector
   */
  private void checkRowCountMismatch(
      final String tableName,
      final List<Row> expectedRows,
      final List<Row> actualRows,
      final ComparisonResult result) {
    if (expectedRows.size() != actualRows.size()) {
      result.addRowCountMismatch(tableName, expectedRows.size(), actualRows.size());
    }
  }

  /**
   * Compares row pairs up to the minimum count using the provided comparator.
   *
   * @param tableName the table name for error reporting
   * @param expectedRows the expected rows
   * @param actualRows the actual rows
   * @param result the result collector
   * @param rowComparator the function to compare individual row pairs
   */
  private void compareRowPairs(
      final String tableName,
      final List<Row> expectedRows,
      final List<Row> actualRows,
      final ComparisonResult result,
      final RowComparator rowComparator) {
    final int rowsToCompare = Math.min(expectedRows.size(), actualRows.size());
    IntStream.range(0, rowsToCompare)
        .forEach(
            rowIndex ->
                rowComparator.compare(
                    tableName,
                    rowIndex,
                    expectedRows.get(rowIndex),
                    actualRows.get(rowIndex),
                    result));
  }

  /** Functional interface for row comparison operations. */
  @FunctionalInterface
  private interface RowComparator {
    /**
     * Compares two rows and collects mismatches into the result.
     *
     * @param tableName the table name for error reporting
     * @param rowIndex the row index for error reporting
     * @param expected the expected row
     * @param actual the actual row
     * @param result the result collector
     */
    void compare(String tableName, int rowIndex, Row expected, Row actual, ComparisonResult result);
  }

  /**
   * Compares two rows and collects mismatches into the result.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param result the result collector
   */
  private void compareRows(
      final String tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final ComparisonResult result) {
    compareRowColumns(tableName, rowIndex, expected, actual, expected.getValues().keySet(), result);
  }

  /**
   * Compares two rows while ignoring specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param ignoreSet set of column names to ignore
   * @param result the result collector
   */
  private void compareRowsIgnoreColumns(
      final String tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<String> ignoreSet,
      final ComparisonResult result) {
    final var columnsToCompare =
        expected.getValues().keySet().stream()
            .filter(columnName -> !ignoreSet.contains(columnName.value()))
            .collect(Collectors.toSet());
    compareRowColumns(tableName, rowIndex, expected, actual, columnsToCompare, result);
  }

  /**
   * Compares specified columns between two rows and collects mismatches.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param columnsToCompare the set of columns to compare
   * @param result the result collector
   */
  private void compareRowColumns(
      final String tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<ColumnName> columnsToCompare,
      final ComparisonResult result) {
    columnsToCompare.stream()
        .filter(
            columnName ->
                !valuesAreEqual(expected.getValue(columnName), actual.getValue(columnName)))
        .forEach(
            columnName ->
                result.addValueMismatch(
                    tableName,
                    rowIndex,
                    columnName.value(),
                    extractValueOrNull(expected.getValue(columnName)),
                    extractValueOrNull(actual.getValue(columnName))));
  }

  /**
   * Handles comparison failures by invoking the handler or throwing an error.
   *
   * @param result the comparison result with all differences
   * @param failureHandler optional custom failure handler
   */
  private void handleFailure(
      final ComparisonResult result, final @Nullable AssertionFailureHandler failureHandler) {
    final var message = result.formatMessage();
    Optional.ofNullable(failureHandler)
        .ifPresentOrElse(
            handler -> handler.handleFailure(message, null, null),
            () -> {
              throw new AssertionError(message);
            });
  }

  /**
   * Compares two CellValue objects with type-aware comparison.
   *
   * <p>This method handles the common case where CSV data (strings) is compared against database
   * data (typed values like Long, Integer, BigDecimal, etc.). It compares values by their string
   * representation when direct object equality fails.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return true if the values are considered equal
   */
  private boolean valuesAreEqual(
      final @Nullable CellValue expected, final @Nullable CellValue actual) {
    return compareNullable(
        expected,
        actual,
        (expectedCell, actualCell) -> compareInnerValues(expectedCell.value(), actualCell.value()));
  }

  /**
   * Compares the inner values of two CellValue objects.
   *
   * @param expectedObj the expected inner value
   * @param actualObj the actual inner value
   * @return true if the values are considered equal
   */
  private boolean compareInnerValues(
      final @Nullable Object expectedObj, final @Nullable Object actualObj) {
    return compareNullable(expectedObj, actualObj, this::compareNonNullValues);
  }

  /**
   * Compares two non-null values using various comparison strategies.
   *
   * <p>Uses pattern matching to handle type-specific comparisons including: CLOB, numeric types,
   * boolean values, and string representations.
   *
   * @param expected the expected value (non-null)
   * @param actual the actual value (non-null)
   * @return true if the values are considered equal
   */
  private boolean compareNonNullValues(final Object expected, final Object actual) {
    // Direct equality check
    if (Objects.equals(expected, actual)) {
      return true;
    }

    // Pattern matching with switch for type-specific comparison
    return switch (actual) {
      case Clob actualClob -> compareWithClob(expected, actualClob);
      case Number actualNumber when expected instanceof String expectedString ->
          compareStringToNumber(expectedString, actualNumber);
      case Number actualNumber when expected instanceof Number expectedNumber ->
          compareNumbers(expectedNumber, actualNumber);
      case String actualString when expected instanceof Number expectedNumber ->
          compareStringToNumber(actualString, expectedNumber);
      case Boolean actualBoolean -> compareToBooleanString(expected, actualBoolean);
      case String actualString when expected instanceof Boolean expectedBoolean ->
          compareToBooleanString(actualString, expectedBoolean);
      default ->
          normalizeForComparison(expected.toString())
              .equals(normalizeForComparison(actual.toString()));
    };
  }

  /**
   * Compares two nullable values using the provided comparator function.
   *
   * <p>Returns {@code true} if both values are null (both absent means equal). Returns {@code
   * false} if exactly one value is null (one absent means not equal). Otherwise, applies the
   * comparator function to the non-null values.
   *
   * @param <T> the type of values to compare
   * @param expected the expected value (nullable)
   * @param actual the actual value (nullable)
   * @param comparator the function to compare non-null values
   * @return {@code true} if the values are considered equal, {@code false} otherwise
   */
  private <T> boolean compareNullable(
      final @Nullable T expected,
      final @Nullable T actual,
      final java.util.function.BiFunction<T, T, Boolean> comparator) {
    return Optional.ofNullable(expected)
        .map(
            expectedValue ->
                Optional.ofNullable(actual)
                    .map(actualValue -> comparator.apply(expectedValue, actualValue))
                    .orElse(false))
        .orElseGet(() -> actual == null);
  }

  /**
   * Compares a value with a CLOB.
   *
   * @param expected the expected value
   * @param clob the CLOB to compare against
   * @return true if they are equal
   */
  private boolean compareWithClob(final Object expected, final Clob clob) {
    try {
      final String clobString = readClob(clob);
      return expected.toString().equals(clobString);
    } catch (final SQLException | IOException e) {
      // When CLOB reading fails, fall back to string comparison
      return expected.toString().equals(clob.toString());
    }
  }

  /**
   * Reads the content of a CLOB as a string.
   *
   * @param clob the CLOB to read
   * @return the CLOB content as a string
   * @throws SQLException if a database error occurs
   * @throws IOException if an I/O error occurs
   */
  private String readClob(final Clob clob) throws SQLException, IOException {
    try (final var reader = clob.getCharacterStream();
        final var writer = new StringWriter()) {
      reader.transferTo(writer);
      return writer.toString();
    }
  }

  /**
   * Compares a value to a boolean.
   *
   * @param value the value to compare
   * @param booleanValue the boolean to compare against
   * @return true if they represent the same boolean value
   */
  private boolean compareToBooleanString(final Object value, final Boolean booleanValue) {
    final var stringValue = value.toString().trim().toLowerCase(Locale.ROOT);
    return booleanValue ? TRUE_VALUES.contains(stringValue) : FALSE_VALUES.contains(stringValue);
  }

  /**
   * Normalizes a string for comparison by removing trailing zeros from timestamps.
   *
   * @param value the value to normalize
   * @return the normalized value
   */
  private String normalizeForComparison(final String value) {
    // Handle timestamp precision differences (e.g., "2024-01-01 10:00:00" vs "2024-01-01
    // 10:00:00.0")
    if (TIMESTAMP_PATTERN.matcher(value).matches()) {
      return TRAILING_ZEROS_PATTERN.matcher(value).replaceAll("");
    }
    return value;
  }

  /**
   * Compares a string value to a number.
   *
   * <p>For floating-point numbers (Float, Double), uses epsilon comparison to handle precision
   * differences.
   *
   * @param stringValue the string value
   * @param numberValue the number value
   * @return true if they represent the same numeric value
   */
  private boolean compareStringToNumber(final String stringValue, final Number numberValue) {
    try {
      final var trimmedString = stringValue.trim();

      // For floating-point types, use epsilon comparison
      return switch (numberValue) {
        case Float floatValue ->
            compareFloatingPoint(Double.parseDouble(trimmedString), floatValue.doubleValue());
        case Double doubleValue ->
            compareFloatingPoint(Double.parseDouble(trimmedString), doubleValue);
        default -> compareStringToIntegerOrDecimal(trimmedString, numberValue);
      };
    } catch (final NumberFormatException exception) {
      // String is not a valid number, fall back to string comparison
      return stringValue.equals(numberValue.toString());
    }
  }

  /**
   * Compares a string to an integer or decimal number.
   *
   * @param trimmedString the trimmed string value
   * @param numberValue the number value to compare against
   * @return true if the values are considered equal
   */
  private boolean compareStringToIntegerOrDecimal(
      final String trimmedString, final Number numberValue) {
    // Integer comparison when string has no decimal point
    if (!trimmedString.contains(".")) {
      return compareStringToInteger(trimmedString, numberValue);
    }

    // Decimal comparison
    final var expectedDecimal = new BigDecimal(trimmedString);
    final var actualDecimal = toBigDecimal(numberValue);
    return expectedDecimal.compareTo(actualDecimal) == 0;
  }

  /**
   * Compares a string representation of an integer to a number.
   *
   * @param trimmedString the trimmed string value (no decimal point)
   * @param numberValue the number value to compare against
   * @return true if the values are considered equal
   */
  private boolean compareStringToInteger(final String trimmedString, final Number numberValue) {
    final var expectedInteger = new BigInteger(trimmedString);
    return switch (numberValue) {
      case BigInteger bigIntegerValue -> expectedInteger.equals(bigIntegerValue);
      case BigDecimal bigDecimalValue ->
          compareIntegerStringWithBigDecimal(expectedInteger, trimmedString, bigDecimalValue);
      default -> expectedInteger.equals(BigInteger.valueOf(numberValue.longValue()));
    };
  }

  /**
   * Compares an integer string with a BigDecimal value.
   *
   * <p>First attempts exact BigInteger conversion; if the BigDecimal has a fractional part, falls
   * back to decimal comparison.
   *
   * @param expectedInteger the expected integer value
   * @param trimmedString the original string for decimal fallback comparison
   * @param bigDecimalValue the BigDecimal value to compare
   * @return true if the values are considered equal
   */
  private boolean compareIntegerStringWithBigDecimal(
      final BigInteger expectedInteger,
      final String trimmedString,
      final BigDecimal bigDecimalValue) {
    try {
      return expectedInteger.equals(bigDecimalValue.toBigIntegerExact());
    } catch (final ArithmeticException exception) {
      // Has decimal part, compare as decimal
      return new BigDecimal(trimmedString).compareTo(bigDecimalValue) == 0;
    }
  }

  /**
   * Compares two numbers of potentially different types.
   *
   * <p>For floating-point numbers (Float, Double), uses epsilon comparison to handle precision
   * differences. For other numeric types, uses exact comparison via BigDecimal.
   *
   * @param expected the expected number
   * @param actual the actual number
   * @return true if they represent the same numeric value
   */
  private boolean compareNumbers(final Number expected, final Number actual) {
    // For floating-point types, use epsilon comparison
    if (isFloatingPoint(expected) || isFloatingPoint(actual)) {
      return compareFloatingPoint(expected.doubleValue(), actual.doubleValue());
    }

    // Convert both to BigDecimal for precise comparison
    final var expectedDecimal = toBigDecimal(expected);
    final var actualDecimal = toBigDecimal(actual);
    return expectedDecimal.compareTo(actualDecimal) == 0;
  }

  /**
   * Checks if a number is a floating-point type (Float or Double).
   *
   * @param number the number to check
   * @return true if the number is Float or Double
   */
  private boolean isFloatingPoint(final Number number) {
    return number instanceof Float || number instanceof Double;
  }

  /**
   * Converts a Number to BigDecimal.
   *
   * @param number the number to convert
   * @return the BigDecimal representation
   */
  private BigDecimal toBigDecimal(final Number number) {
    return number instanceof BigDecimal bigDecimalValue
        ? bigDecimalValue
        : new BigDecimal(number.toString());
  }

  /**
   * Compares two floating-point numbers with epsilon tolerance.
   *
   * <p>Uses relative epsilon for larger numbers and absolute epsilon for smaller numbers to handle
   * floating-point precision issues.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return true if they are close enough to be considered equal
   */
  private boolean compareFloatingPoint(final double expected, final double actual) {
    // Handle special cases
    if (Double.isNaN(expected) && Double.isNaN(actual)) {
      return true;
    }
    if (Double.isInfinite(expected) && Double.isInfinite(actual)) {
      return expected == actual; // Same sign infinity
    }

    // Use relative epsilon for comparison
    final double epsilon = 1e-6;
    final double diff = Math.abs(expected - actual);
    final double maxVal = Math.max(Math.abs(expected), Math.abs(actual));

    // For small numbers, use absolute comparison
    if (maxVal < epsilon) {
      return diff < epsilon;
    }

    // Use relative comparison for larger numbers
    return diff / maxVal < epsilon;
  }

  /**
   * Extracts the underlying value from a data value, or null if the data value is null.
   *
   * @param dataValue the data value to extract
   * @return the underlying value or null
   */
  private @Nullable Object extractValueOrNull(final @Nullable CellValue dataValue) {
    return Optional.ofNullable(dataValue).map(CellValue::value).orElse(null);
  }
}
