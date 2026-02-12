package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;

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
 * @see ExpectationContext
 */
public interface ExpectationProvider {

  /**
   * Logs a warning message about unsupported features.
   *
   * @param message the warning message to log
   * @param args the message arguments
   */
  private static void logWarning(final String message, final Object... args) {
    LoggerFactory.getLogger(ExpectationProvider.class).warn(message, args);
  }

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
   * Verifies that the database state matches the expected dataset using the specified context.
   *
   * <p>This method accepts an {@link ExpectationContext} parameter object that encapsulates all
   * optional verification parameters (column exclusions, column strategies, row ordering, and
   * operation defaults). This replaces the telescoping overload pattern.
   *
   * <p>The default implementation logs warnings for non-default context values and delegates to
   * {@link #verifyExpectation(TableSet, DataSource)}. Implementations should override this method
   * to support the full set of verification parameters.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param context the verification context containing optional parameters
   * @throws AssertionError if verification fails
   * @see ExpectationContext
   */
  default void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final ExpectationContext context) {
    if (!context.excludeColumns().isEmpty()) {
      logWarning(
          "Column exclusions specified but current ExpectationProvider does not support them. "
              + "Exclusions will be ignored: {}. Override verifyExpectation(TableSet, DataSource, "
              + "ExpectationContext) to support column exclusion.",
          context.excludeColumns());
    }
    if (!context.columnStrategies().isEmpty()) {
      logWarning(
          "Column strategies specified but current ExpectationProvider does not support them. "
              + "Strategies will be ignored: {}. Override verifyExpectation(TableSet, DataSource, "
              + "ExpectationContext) to support column strategies.",
          context.columnStrategies().keySet());
    }
    if (context.rowOrdering() == RowOrdering.UNORDERED) {
      logWarning(
          "Unordered row comparison requested but current ExpectationProvider does not support it. "
              + "Falling back to ordered comparison. Override verifyExpectation(TableSet, "
              + "DataSource, ExpectationContext) to support unordered comparison.");
    }
    if (context.operationDefaults().floatingPointEpsilon()
        != OperationDefaults.DEFAULT_FLOATING_POINT_EPSILON) {
      logWarning(
          "Custom floating-point epsilon specified but current ExpectationProvider does not "
              + "support it. Using default epsilon. Override verifyExpectation(TableSet, "
              + "DataSource, ExpectationContext) to support custom epsilon.");
    }
    verifyExpectation(expectedTableSet, dataSource);
  }
}
