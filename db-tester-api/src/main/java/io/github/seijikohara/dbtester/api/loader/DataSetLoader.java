package io.github.seijikohara.dbtester.api.loader;

import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import java.util.List;

/**
 * Strategy SPI that resolves {@link DataSet} instances for each test phase.
 *
 * <p>Implementations of this interface define how datasets are loaded for database preparation and
 * validation. The framework invokes these methods at specific points in the test lifecycle to set
 * up initial database state and verify expected outcomes.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} or configured directly in
 * test annotations.
 *
 * @see DataSet
 * @see TestContext
 */
public interface DataSetLoader {

  /**
   * Loads datasets for preparing the database before test execution.
   *
   * <p>This method is called by framework-specific extensions (JUnit Jupiter, Spock) before each
   * test method runs. The returned datasets are applied to the database using the configured
   * operations (typically {@link
   * io.github.seijikohara.dbtester.api.operation.Operation#CLEAN_INSERT}).
   *
   * @param context the test execution context containing test metadata and configuration
   * @return immutable list of datasets to load into the database
   */
  List<DataSet> loadPreparationDataSets(final TestContext context);

  /**
   * Loads datasets for validating the database state after test execution.
   *
   * <p>This method is called by framework-specific extensions (JUnit Jupiter, Spock) after each
   * test method completes. The returned datasets are compared with the actual database state to
   * verify test results.
   *
   * @param context the test execution context containing test metadata and configuration
   * @return immutable list of datasets to validate against the database
   */
  List<DataSet> loadExpectationDataSets(final TestContext context);
}
