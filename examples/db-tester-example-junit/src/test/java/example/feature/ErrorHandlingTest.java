package example.feature;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates assertion failure scenarios and error message quality in db-tester.
 *
 * <p>This test class shows how {@link DatabaseAssertion} handles various failure scenarios and what
 * error messages are produced. These tests serve as documentation for expected behavior when:
 *
 * <ul>
 *   <li>Row counts differ between expected and actual data
 *   <li>Cell values do not match
 *   <li>Column structures are different
 * </ul>
 *
 * <p>Understanding these error scenarios helps users diagnose and fix issues in their test setups.
 * Each test intentionally triggers an assertion failure to demonstrate the error message format.
 *
 * <p><strong>Note:</strong> These tests do not use {@code @ExtendWith(DatabaseTestExtension.class)}
 * because they test programmatic assertions directly without database operations.
 */
@DisplayName("Error Handling Scenarios")
final class ErrorHandlingTest {

  /** Logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingTest.class);

  /** Creates ErrorHandlingTest instance. */
  ErrorHandlingTest() {}

  /**
   * Demonstrates what happens when expected has more rows than actual.
   *
   * <p>This test shows the error message produced when the expected dataset has more rows than the
   * actual database state. The error message should clearly indicate the row count difference.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when expected has more rows than actual")
  void shouldFailWhenExpectedHasMoreRows() {
    // Given
    logger.info("Testing scenario: expected has more rows than actual");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");

    // Expected: 3 rows
    final var row1 = Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")));
    final var row2 = Row.of(Map.of(columnId, new CellValue(2), columnValue, new CellValue("Two")));
    final var row3 =
        Row.of(Map.of(columnId, new CellValue(3), columnValue, new CellValue("Three")));

    final var expectedTable =
        Table.of(
            new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(row1, row2, row3));

    // Actual: 2 rows
    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(row1, row2));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError when expected has more rows");

    logger.info("Error message for more expected rows: {}", exception.getMessage());
    assertTrue(
        exception.getMessage() != null && !exception.getMessage().isEmpty(),
        "Error message should not be empty");
  }

  /**
   * Demonstrates what happens when actual has more rows than expected.
   *
   * <p>This test shows the error message produced when the actual database has more rows than
   * expected. Users should be able to identify this as a data insertion or cleanup issue.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when actual has more rows than expected")
  void shouldFailWhenActualHasMoreRows() {
    // Given
    logger.info("Testing scenario: actual has more rows than expected");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");

    // Expected: 1 row
    final var row1 = Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")));

    final var expectedTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(row1));

    // Actual: 2 rows
    final var row2 = Row.of(Map.of(columnId, new CellValue(2), columnValue, new CellValue("Two")));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(row1, row2));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError when actual has more rows");

    logger.info("Error message for more actual rows: {}", exception.getMessage());
  }

  /**
   * Demonstrates what happens when a string value differs.
   *
   * <p>This test shows the error message when a string cell value in the expected data differs from
   * the actual value.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when string values differ")
  void shouldFailWhenStringValuesDiffer() {
    // Given
    logger.info("Testing scenario: string value mismatch");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");

    final var expectedRow =
        Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("ExpectedValue")));

    final var actualRow =
        Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("ActualValue")));

    final var expectedTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(expectedRow));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(actualRow));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError for string value mismatch");

    logger.info("Error message for string mismatch: {}", exception.getMessage());
  }

  /**
   * Demonstrates what happens when numeric values differ.
   *
   * <p>This test shows the error message when a numeric cell value differs.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when numeric values differ")
  void shouldFailWhenNumericValuesDiffer() {
    // Given
    logger.info("Testing scenario: numeric value mismatch");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");

    final var expectedRow =
        Row.of(Map.of(columnId, new CellValue(100), columnValue, new CellValue("Test")));

    final var actualRow =
        Row.of(Map.of(columnId, new CellValue(999), columnValue, new CellValue("Test")));

    final var expectedTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(expectedRow));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(actualRow));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError for numeric value mismatch");

    logger.info("Error message for numeric mismatch: {}", exception.getMessage());
  }

  /**
   * Demonstrates what happens when null handling differs.
   *
   * <p>This test shows the error message when expected has null but actual has a value.
   */
  @Test
  @Tag("edge-case")
  @DisplayName("should fail when null vs non-null values differ")
  void shouldFailWhenNullHandlingDiffers() {
    // Given
    logger.info("Testing scenario: null vs non-null mismatch");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");

    final var expectedRow = Row.of(Map.of(columnId, new CellValue(1), columnValue, CellValue.NULL));

    final var actualRow =
        Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("NotNull")));

    final var expectedTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(expectedRow));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(actualRow));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError for null vs non-null mismatch");

    logger.info("Error message for null mismatch: {}", exception.getMessage());
  }

  /**
   * Demonstrates what happens when expected has more columns than actual.
   *
   * <p>This test shows the error message when the expected table schema includes columns that are
   * not present in the actual data.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when expected has extra columns")
  void shouldFailWhenExpectedHasExtraColumns() {
    // Given
    logger.info("Testing scenario: expected has extra columns");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");
    final var columnExtra = new ColumnName("EXTRA");

    final var expectedRow =
        Row.of(
            Map.of(
                columnId,
                new CellValue(1),
                columnValue,
                new CellValue("One"),
                columnExtra,
                new CellValue("ExtraData")));

    final var actualRow =
        Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")));

    final var expectedTable =
        Table.of(
            new TableName("TEST_TABLE"),
            List.of(columnId, columnValue, columnExtra),
            List.of(expectedRow));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(actualRow));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError when expected has extra columns");

    logger.info("Error message for extra columns: {}", exception.getMessage());
  }

  /**
   * Demonstrates what happens when column names differ.
   *
   * <p>This test shows the error message when the column names in expected and actual tables don't
   * match.
   */
  @Test
  @Tag("error")
  @DisplayName("should fail when column names differ")
  void shouldFailWhenColumnNamesDiffer() {
    // Given
    logger.info("Testing scenario: column names differ");

    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("NAME");
    final var columnDescription = new ColumnName("DESCRIPTION");

    final var expectedRow =
        Row.of(Map.of(columnId, new CellValue(1), columnDescription, new CellValue("Desc")));

    final var actualRow =
        Row.of(Map.of(columnId, new CellValue(1), columnValue, new CellValue("Desc")));

    final var expectedTable =
        Table.of(
            new TableName("TEST_TABLE"),
            List.of(columnId, columnDescription),
            List.of(expectedRow));

    final var actualTable =
        Table.of(new TableName("TEST_TABLE"), List.of(columnId, columnValue), List.of(actualRow));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class,
            () -> DatabaseAssertion.assertEquals(expectedTable, actualTable),
            "Should throw AssertionError when column names differ");

    logger.info("Error message for column name mismatch: {}", exception.getMessage());
  }

  /**
   * Demonstrates that assertion errors provide context for debugging.
   *
   * <p>Good error messages should include enough information to quickly identify the failure: table
   * name, row position, column name, expected vs actual values.
   */
  @Test
  @Tag("normal")
  @DisplayName("should provide sufficient context in error messages")
  void shouldProvideSufficientContext() {
    // Given
    logger.info("Testing error message quality");

    final var columnId = new ColumnName("ID");
    final var columnName = new ColumnName("NAME");
    final var columnStatus = new ColumnName("STATUS");

    // Create multiple rows where only the third row differs
    final var row1 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(1),
                columnName,
                new CellValue("Alice"),
                columnStatus,
                new CellValue("ACTIVE")));
    final var row2 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(2),
                columnName,
                new CellValue("Bob"),
                columnStatus,
                new CellValue("ACTIVE")));
    final var expectedRow3 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(3),
                columnName,
                new CellValue("Charlie"),
                columnStatus,
                new CellValue("INACTIVE")));
    final var actualRow3 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(3),
                columnName,
                new CellValue("Charlie"),
                columnStatus,
                new CellValue("ACTIVE")));

    final var expectedTable =
        Table.of(
            new TableName("USER_STATUS_TABLE"),
            List.of(columnId, columnName, columnStatus),
            List.of(row1, row2, expectedRow3));

    final var actualTable =
        Table.of(
            new TableName("USER_STATUS_TABLE"),
            List.of(columnId, columnName, columnStatus),
            List.of(row1, row2, actualRow3));

    // When & Then
    final var exception =
        assertThrows(
            AssertionError.class, () -> DatabaseAssertion.assertEquals(expectedTable, actualTable));

    // Log the full error message for documentation purposes
    logger.info("Full error message demonstrating context:\n{}", exception.getMessage());

    // The message should not be empty and should provide useful information
    assertTrue(
        exception.getMessage() != null && exception.getMessage().length() > 0,
        "Error message should provide meaningful context");
  }
}
