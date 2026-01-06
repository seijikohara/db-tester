package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies database state matches expected dataset.
 *
 * <p>For each table in the expected dataset:
 *
 * <ol>
 *   <li>Retrieves actual data from database
 *   <li>Filters actual data to only include columns present in expected table
 *   <li>Compares filtered actual data against expected data
 * </ol>
 *
 * <p>Only columns present in expected dataset are compared, allowing partial column validation.
 *
 * <p>This class is stateless and thread-safe. All operations can be safely called from multiple
 * threads.
 */
public final class ExpectationVerifier {

  /** Logger for tracking operations. */
  private static final Logger logger = LoggerFactory.getLogger(ExpectationVerifier.class);

  /** Table reader for retrieving database state. */
  private final TableReader tableReader;

  /** Dataset comparator for assertions. */
  private final DataSetComparator comparator;

  /** Creates a new expectation verifier with default dependencies. */
  public ExpectationVerifier() {
    this.tableReader = new TableReader();
    this.comparator = new DataSetComparator();
  }

  /**
   * Creates a new expectation verifier with specified dependencies.
   *
   * @param tableReader the table reader
   * @param comparator the dataset comparator
   */
  public ExpectationVerifier(final TableReader tableReader, final DataSetComparator comparator) {
    this.tableReader = tableReader;
    this.comparator = comparator;
  }

  /**
   * Verifies database state matches expected dataset.
   *
   * <p>For each table in the expected dataset:
   *
   * <ol>
   *   <li>Retrieves actual data from database
   *   <li>Filters actual data to only include columns present in expected table
   *   <li>Compares filtered actual data against expected data
   * </ol>
   *
   * <p>Only columns present in expected dataset are compared, allowing partial column validation.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(final TableSet expectedTableSet, final DataSource dataSource) {
    verifyExpectation(expectedTableSet, dataSource, null);
  }

  /**
   * Verifies database state matches expected dataset, excluding specified columns.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource)} with column exclusion
   * support. Excluded columns are ignored during the comparison, which is useful for auto-generated
   * columns (timestamps, version numbers, auto-increment IDs) that cannot be predicted in test
   * data.
   *
   * <p>Column name matching is case-insensitive.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison, or null/empty for no exclusions
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final @Nullable Collection<String> excludeColumns) {
    logger.debug("Verifying expectation for {} tables", expectedTableSet.getTables().size());

    // Normalize exclude columns to uppercase for case-insensitive matching
    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    expectedTableSet
        .getTables()
        .forEach(
            expectedTable -> {
              final var tableName = expectedTable.getName().value();
              final var expectedColumns = expectedTable.getColumns();

              logger.trace(
                  "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

              // Fetch actual table data with only the expected columns
              final var actualTable =
                  tableReader.fetchTable(dataSource, tableName, expectedColumns);

              logger.trace(
                  "Comparing table {}: expected {} rows, actual {} rows",
                  tableName,
                  expectedTable.getRowCount(),
                  actualTable.getRowCount());

              // Compare with or without column exclusion
              if (normalizedExcludeColumns.isEmpty()) {
                comparator.assertEquals(expectedTable, actualTable, null);
              } else {
                comparator.assertEqualsIgnoreColumns(
                    expectedTable, actualTable, normalizedExcludeColumns);
              }
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.getTables().size());
  }

  /**
   * Normalizes exclude column names for case-insensitive matching.
   *
   * @param excludeColumns the column names to normalize, may be null or empty
   * @return set of uppercase column names, empty set if input is null or empty
   */
  private Set<String> normalizeExcludeColumns(final @Nullable Collection<String> excludeColumns) {
    if (excludeColumns == null || excludeColumns.isEmpty()) {
      return Set.of();
    }
    return excludeColumns.stream()
        .map(column -> column.toUpperCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }
}
