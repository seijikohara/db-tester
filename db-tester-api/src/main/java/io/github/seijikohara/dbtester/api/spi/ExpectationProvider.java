package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
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
 * @see ExpectationContext
 */
public interface ExpectationProvider {

  /**
   * Verifies that the database state matches the expected dataset using the specified context.
   *
   * <p>For each table in the expected dataset, fetches actual data from the database and compares
   * it with the expected data. Only columns present in the expected table are included in the
   * comparison. The {@link ExpectationContext} parameter object encapsulates all optional
   * verification parameters (column exclusions, column strategies, row ordering, and operation
   * defaults).
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param context the verification context containing optional parameters
   * @throws AssertionError if verification fails (row count mismatch, column value mismatch, or
   *     table structure mismatch)
   * @see ExpectationContext
   */
  void verifyExpectation(
      TableSet expectedTableSet, DataSource dataSource, ExpectationContext context);
}
