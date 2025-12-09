package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

/**
 * Comparator for DataSet and Table objects.
 *
 * <p>This class provides methods to compare datasets and tables, optionally ignoring specific
 * columns or using custom failure handlers.
 */
public class DataSetComparator {

  /** Creates a new dataset comparator. */
  public DataSetComparator() {
    // Default constructor
  }

  /**
   * Asserts that two datasets are equal.
   *
   * <p>Compares table counts and individual tables between expected and actual datasets.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final DataSet expected,
      final DataSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var expectedTables = expected.getTables();
    final var actualTables = actual.getTables();

    if (expectedTables.size() != actualTables.size()) {
      fail(
          String.format(
              "Table count mismatch: expected %d tables, but got %d",
              expectedTables.size(), actualTables.size()),
          expectedTables.size(),
          actualTables.size(),
          failureHandler);
      return;
    }

    expectedTables.forEach(
        expectedTable -> {
          final var tableName = expectedTable.getName();
          actual
              .getTable(tableName)
              .ifPresentOrElse(
                  actualTable -> assertEquals(expectedTable, actualTable, failureHandler),
                  () ->
                      fail(
                          "Table not found: " + tableName.value(),
                          tableName.value(),
                          null,
                          failureHandler));
        });
  }

  /**
   * Asserts that two tables are equal.
   *
   * <p>Compares row counts and individual rows between expected and actual tables.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      fail(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()),
          expectedRows.size(),
          actualRows.size(),
          failureHandler);
      return;
    }

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i -> compareRows(tableName, i, expectedRows.get(i), actualRows.get(i), failureHandler));
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
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      throw new AssertionError(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()));
    }

    // Combine expected columns with additional columns
    final Set<ColumnName> columnsToCompare = new HashSet<>(expected.getColumns());
    additionalColumnNames.stream().map(ColumnName::new).forEach(columnsToCompare::add);

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i ->
                compareRowsWithColumns(
                    tableName, i, expectedRows.get(i), actualRows.get(i), columnsToCompare));
  }

  /**
   * Compares two rows using only the specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param columnsToCompare the set of columns to compare
   * @throws AssertionError if any column values differ
   */
  private void compareRowsWithColumns(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<ColumnName> columnsToCompare) {
    columnsToCompare.forEach(
        columnName -> {
          final var expectedValue = expected.getValue(columnName);
          final var actualValue = actual.getValue(columnName);

          if (!valuesAreEqual(expectedValue, actualValue)) {
            throw new AssertionError(
                String.format(
                    "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                    tableName.value(),
                    rowIndex,
                    columnName.value(),
                    extractValueString(expectedValue),
                    extractValueString(actualValue)));
          }
        });
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
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var tableNameObj = new TableName(tableName);
    final var expectedTable =
        expected
            .getTable(tableNameObj)
            .orElseThrow(() -> new AssertionError("Expected table not found: " + tableName));
    final var actualTable =
        actual
            .getTable(tableNameObj)
            .orElseThrow(() -> new AssertionError("Actual table not found: " + tableName));

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
    final var ignoreSet = Set.copyOf(ignoreColumnNames);
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      throw new AssertionError(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()));
    }

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i ->
                compareRowsIgnoreColumns(
                    tableName, i, expectedRows.get(i), actualRows.get(i), ignoreSet));
  }

  /**
   * Compares two rows and reports mismatches via failure handler.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param failureHandler optional custom failure handler
   */
  private void compareRows(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    expected
        .getValues()
        .keySet()
        .forEach(
            columnName -> {
              final var expectedValue = expected.getValue(columnName);
              final var actualValue = actual.getValue(columnName);

              if (!valuesAreEqual(expectedValue, actualValue)) {
                fail(
                    String.format(
                        "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                        tableName.value(),
                        rowIndex,
                        columnName.value(),
                        extractValueString(expectedValue),
                        extractValueString(actualValue)),
                    extractValueOrNull(expectedValue),
                    extractValueOrNull(actualValue),
                    failureHandler);
              }
            });
  }

  /**
   * Compares two rows while ignoring specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param ignoreSet set of column names to ignore
   * @throws AssertionError if any non-ignored column values differ
   */
  private void compareRowsIgnoreColumns(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<String> ignoreSet) {
    expected.getValues().keySet().stream()
        .filter(columnName -> !ignoreSet.contains(columnName.value()))
        .forEach(
            columnName -> {
              final var expectedValue = expected.getValue(columnName);
              final var actualValue = actual.getValue(columnName);

              if (!valuesAreEqual(expectedValue, actualValue)) {
                throw new AssertionError(
                    String.format(
                        "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                        tableName.value(),
                        rowIndex,
                        columnName.value(),
                        extractValueString(expectedValue),
                        extractValueString(actualValue)));
              }
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
        expected, actual, (exp, act) -> compareInnerValues(exp.value(), act.value()));
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
   * @param exp the expected value (non-null)
   * @param act the actual value (non-null)
   * @return true if the values are considered equal
   */
  private boolean compareNonNullValues(final Object exp, final Object act) {
    // Direct equality check
    if (Objects.equals(exp, act)) {
      return true;
    }

    // Handle CLOB comparison (actual is CLOB, expected is String)
    if (act instanceof Clob actualClob) {
      return compareWithClob(exp, actualClob);
    }

    // Try numeric comparison for CSV string vs database number comparison
    if (exp instanceof String expectedStr && act instanceof Number actualNum) {
      return compareStringToNumber(expectedStr, actualNum);
    }
    if (exp instanceof Number expectedNum && act instanceof String actualStr) {
      return compareStringToNumber(actualStr, expectedNum);
    }

    // Both are numbers but different types (e.g., Integer vs Long)
    if (exp instanceof Number expectedNum && act instanceof Number actualNum) {
      return compareNumbers(expectedNum, actualNum);
    }

    // Boolean comparison (CSV "1"/"0" vs database true/false)
    if (act instanceof Boolean actualBool) {
      return compareToBooleanString(exp, actualBool);
    }
    if (exp instanceof Boolean expectedBool && act instanceof String actualStr) {
      return compareToBooleanString(actualStr, expectedBool);
    }

    // Fall back to normalized string comparison (handles timestamp precision differences)
    return normalizeForComparison(exp.toString()).equals(normalizeForComparison(act.toString()));
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
      final BiFunction<T, T, Boolean> comparator) {
    return Optional.ofNullable(expected)
        .map(
            exp -> Optional.ofNullable(actual).map(act -> comparator.apply(exp, act)).orElse(false))
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
      // If we can't read the CLOB, fall back to string comparison
      return expected.toString().equals(clob.toString());
    }
  }

  /**
   * Reads the content of a CLOB as a string.
   *
   * <p>This method uses an imperative loop because I/O operations require sequential buffered
   * reading. Stream-based alternatives would not improve readability or correctness for this
   * character stream processing pattern.
   *
   * @param clob the CLOB to read
   * @return the CLOB content as a string
   * @throws SQLException if a database error occurs
   * @throws IOException if an I/O error occurs
   */
  private String readClob(final Clob clob) throws SQLException, IOException {
    try (final Reader reader = clob.getCharacterStream()) {
      final var sb = new StringBuilder();
      final var buffer = new char[1024];
      int length;
      while ((length = reader.read(buffer)) != -1) {
        sb.append(buffer, 0, length);
      }
      return sb.toString();
    }
  }

  /**
   * Compares a value to a boolean.
   *
   * @param value the value to compare
   * @param bool the boolean to compare against
   * @return true if they represent the same boolean value
   */
  private boolean compareToBooleanString(final Object value, final Boolean bool) {
    final var str = value.toString().trim().toLowerCase(java.util.Locale.ROOT);
    if (bool) {
      return "1".equals(str) || "true".equals(str) || "yes".equals(str) || "y".equals(str);
    } else {
      return "0".equals(str) || "false".equals(str) || "no".equals(str) || "n".equals(str);
    }
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
    if (value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.0+)?")) {
      return value.replaceAll("\\.0+$", "");
    }
    return value;
  }

  /**
   * Compares a string value to a number.
   *
   * <p>For floating-point numbers (Float, Double), uses epsilon comparison to handle precision
   * differences.
   *
   * @param str the string value
   * @param num the number value
   * @return true if they represent the same numeric value
   */
  private boolean compareStringToNumber(final String str, final Number num) {
    try {
      final var strTrimmed = str.trim();

      // For floating-point types, use epsilon comparison
      if (num instanceof Float || num instanceof Double) {
        final var expected = Double.parseDouble(strTrimmed);
        return compareFloatingPoint(expected, num.doubleValue());
      }

      // Try integer comparison first
      if (!strTrimmed.contains(".")) {
        final var expected = new BigInteger(strTrimmed);
        final BigInteger actual;
        if (num instanceof BigInteger bi) {
          actual = bi;
        } else if (num instanceof BigDecimal bd) {
          try {
            actual = bd.toBigIntegerExact();
          } catch (final ArithmeticException e) {
            // Has decimal part, compare as decimal
            return new BigDecimal(strTrimmed).compareTo(bd) == 0;
          }
        } else {
          actual = BigInteger.valueOf(num.longValue());
        }
        return expected.equals(actual);
      }

      // Decimal comparison
      final var expected = new BigDecimal(strTrimmed);
      final var actual = num instanceof BigDecimal bd ? bd : new BigDecimal(num.toString());
      return expected.compareTo(actual) == 0;
    } catch (final NumberFormatException e) {
      // String is not a valid number, fall back to string comparison
      return str.equals(num.toString());
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
    if (expected instanceof Float
        || expected instanceof Double
        || actual instanceof Float
        || actual instanceof Double) {
      return compareFloatingPoint(expected.doubleValue(), actual.doubleValue());
    }

    // Convert both to BigDecimal for precise comparison
    final var expectedDecimal =
        expected instanceof BigDecimal bd ? bd : new BigDecimal(expected.toString());
    final var actualDecimal =
        actual instanceof BigDecimal bd ? bd : new BigDecimal(actual.toString());
    return expectedDecimal.compareTo(actualDecimal) == 0;
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

    // For very small numbers, use absolute comparison
    if (maxVal < epsilon) {
      return diff < epsilon;
    }

    // Use relative comparison for larger numbers
    return diff / maxVal < epsilon;
  }

  /**
   * Extracts the string representation of a data value, or "null" if null.
   *
   * @param dataValue the data value to extract
   * @return the value string or "null"
   */
  private String extractValueString(final @Nullable CellValue dataValue) {
    return Optional.ofNullable(dataValue)
        .map(CellValue::value)
        .map(Object::toString)
        .orElse("null");
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

  /**
   * Reports an assertion failure via the handler or throws an AssertionError.
   *
   * @param message the failure message
   * @param expected the expected value
   * @param actual the actual value
   * @param failureHandler optional custom failure handler
   * @throws AssertionError if no failure handler is provided
   */
  private void fail(
      final String message,
      final @Nullable Object expected,
      final @Nullable Object actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    Optional.ofNullable(failureHandler)
        .ifPresentOrElse(
            handler -> handler.handleFailure(message, expected, actual),
            () -> {
              throw new AssertionError(message);
            });
  }
}
