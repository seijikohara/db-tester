package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.Collection;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Service Provider Interface for verifying database state against expected datasets.
 *
 * <p>This SPI abstracts expectation verification, allowing test framework modules (JUnit, Spock) to
 * depend only on the API module. The actual implementation is provided by the core module and
 * loaded via {@link java.util.ServiceLoader}.
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
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 */
public interface ExpectationProvider {

  /**
   * Verifies that the database state matches the expected dataset.
   *
   * <p>For each table in the expected dataset, fetches actual data from the database and compares
   * it with the expected data. Only columns present in the expected table are included in the
   * comparison.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @throws AssertionError if verification fails (row count mismatch, column value mismatch, or
   *     table structure mismatch)
   */
  void verifyExpectation(TableSet expectedTableSet, DataSource dataSource);

  /**
   * Verifies that the database state matches the expected dataset, excluding specified columns.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource)} with column exclusion
   * support. Excluded columns are ignored during the comparison, which is useful for auto-generated
   * columns (timestamps, version numbers, auto-increment IDs) that cannot be predicted in test
   * data.
   *
   * <p>Column name matching is case-insensitive. For example, excluding {@code "CREATED_AT"} will
   * ignore columns named {@code "created_at"}, {@code "CREATED_AT"}, or {@code "Created_At"}.
   *
   * <p>The default implementation delegates to {@link #verifyExpectation(TableSet, DataSource)}
   * when no columns are excluded. Implementations should override this method to support column
   * exclusion.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison (case-insensitive matching)
   * @throws AssertionError if verification fails (row count mismatch, column value mismatch, or
   *     table structure mismatch)
   */
  default void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final Collection<String> excludeColumns) {
    // Default implementation delegates to the basic verifyExpectation method.
    // Implementations should override this method to support column exclusion.
    // Note: excludeColumns is intentionally ignored in the default implementation
    // for backward compatibility with existing providers.
    verifyExpectation(expectedTableSet, dataSource);
  }

  /**
   * Verifies that the database state matches the expected dataset with column comparison
   * strategies.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource, Collection)} with
   * column-specific comparison strategy support. Each column can have its own comparison strategy:
   *
   * <ul>
   *   <li>{@code STRICT} - Exact match (default)
   *   <li>{@code IGNORE} - Skip comparison entirely
   *   <li>{@code NUMERIC} - Type-aware numeric comparison
   *   <li>{@code CASE_INSENSITIVE} - Case-insensitive string comparison
   *   <li>{@code TIMESTAMP_FLEXIBLE} - Ignores sub-second precision and handles timezone
   *   <li>{@code NOT_NULL} - Only verify the value is not null
   *   <li>{@code REGEX} - Match against a regular expression pattern
   * </ul>
   *
   * <p>Column exclusion takes precedence: columns in {@code excludeColumns} are skipped entirely
   * before column strategies are applied.
   *
   * <p>The default implementation delegates to {@link #verifyExpectation(TableSet, DataSource,
   * Collection)} when no column strategies are provided. Implementations should override this
   * method to support column strategies.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison (case-insensitive matching)
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @throws AssertionError if verification fails
   * @see ColumnStrategyMapping
   * @see io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
   */
  default void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final Collection<String> excludeColumns,
      final Map<String, ColumnStrategyMapping> columnStrategies) {
    // Default implementation ignores column strategies for backward compatibility.
    // Implementations should override this method to support column strategies.
    verifyExpectation(expectedTableSet, dataSource, excludeColumns);
  }
}
