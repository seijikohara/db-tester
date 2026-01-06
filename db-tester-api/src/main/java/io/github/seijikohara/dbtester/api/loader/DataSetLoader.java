package io.github.seijikohara.dbtester.api.loader;

import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy SPI that resolves {@link TableSet} instances for each test phase.
 *
 * <p>Implementations of this interface define how datasets are loaded for database preparation and
 * validation. The framework invokes these methods at specific points in the test lifecycle to set
 * up initial database state and verify expected outcomes.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} or configured directly in
 * test annotations.
 *
 * @see TableSet
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
  List<TableSet> loadPreparationDataSets(final TestContext context);

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
  List<TableSet> loadExpectationDataSets(final TestContext context);

  /**
   * Loads datasets for validating the database state with column exclusion metadata.
   *
   * <p>This method extends {@link #loadExpectationDataSets(TestContext)} to include column
   * exclusion information from annotations ({@link
   * io.github.seijikohara.dbtester.api.annotation.DataSetSource#excludeColumns()}) and global
   * settings ({@link
   * io.github.seijikohara.dbtester.api.config.ConventionSettings#globalExcludeColumns()}).
   *
   * <p>The default implementation wraps results from {@link #loadExpectationDataSets(TestContext)}
   * with global exclude columns only. Implementations should override this method to also include
   * annotation-level exclusions.
   *
   * @param context the test execution context containing test metadata and configuration
   * @return immutable list of expected table sets with column exclusion metadata
   */
  default List<ExpectedTableSet> loadExpectationDataSetsWithExclusions(final TestContext context) {
    final var globalExcludeColumns = context.configuration().conventions().globalExcludeColumns();
    return loadExpectationDataSets(context).stream()
        .map(tableSet -> ExpectedTableSet.of(tableSet, globalExcludeColumns))
        .collect(Collectors.toUnmodifiableList());
  }
}
