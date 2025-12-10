package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import javax.sql.DataSource;
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
   * @param expectedDataSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(final DataSet expectedDataSet, final DataSource dataSource) {
    logger.debug("Verifying expectation for {} tables", expectedDataSet.getTables().size());

    expectedDataSet
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

              // Compare
              comparator.assertEquals(expectedTable, actualTable, null);
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedDataSet.getTables().size());
  }
}
