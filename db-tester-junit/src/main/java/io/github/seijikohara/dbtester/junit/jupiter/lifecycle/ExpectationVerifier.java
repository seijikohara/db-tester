package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>The verifier delegates to {@link ExpectationProvider} for database operations. The verifier
 * opens a connection through the dataset's {@link DataSource}, applies column filtering so that
 * only declared columns participate in comparisons, and raises {@link ValidationException} when the
 * observed database state deviates from the expected dataset.
 *
 * <p>Like {@link PreparationExecutor}, this class is stateless and thread-safe. It performs
 * structured logging to aid debugging and rewraps any {@link ValidationException} thrown by the
 * verifier with additional test context so failures remain actionable in the calling layer.
 *
 * @see PreparationExecutor
 * @see ExpectationProvider
 */
public final class ExpectationVerifier {

  /** Logger for tracking expectation verification. */
  private static final Logger logger = LoggerFactory.getLogger(ExpectationVerifier.class);

  /** The expectation provider. */
  private final ExpectationProvider expectationProvider;

  /** Creates a new expectation verifier. */
  public ExpectationVerifier() {
    this.expectationProvider =
        ServiceLoader.load(ExpectationProvider.class).findFirst().orElseThrow();
  }

  /**
   * Verifies the database state against expected datasets.
   *
   * <p>Loads the datasets specified in the {@link ExpectedDataSet} annotation (or resolved via
   * conventions) and compares them with the actual database state.
   *
   * @param context the test context containing configuration and registry
   * @param expectedDataSet the ExpectedDataSet annotation (currently unused but reserved for future
   *     options)
   * @throws AssertionError if the database state does not match the expected state
   */
  public void verify(final TestContext context, final ExpectedDataSet expectedDataSet) {
    logger.debug(
        "Verifying expectation for test: {}.{}",
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    final var expectedTableSets =
        context.configuration().loader().loadExpectationDataSetsWithExclusions(context);

    if (expectedTableSets.isEmpty()) {
      logger.debug("No expectation datasets found");
      return;
    }

    expectedTableSets.forEach(
        expectedTableSet -> verifyExpectedTableSet(context, expectedTableSet));
  }

  /**
   * Verifies a single ExpectedTableSet against the database.
   *
   * <p>Delegates to {@link ExpectationProvider#verifyExpectation} for full data comparison
   * including column filtering, column comparison strategies, and detailed assertion messages. If
   * verification fails, wraps the exception with additional test context.
   *
   * @param context the test context providing access to the data source registry
   * @param expectedTableSet the expected TableSet with exclusion and strategy metadata
   * @throws ValidationException if verification fails with wrapped context information
   */
  private void verifyExpectedTableSet(
      final TestContext context, final ExpectedTableSet expectedTableSet) {
    final var tableSet = expectedTableSet.tableSet();
    final var excludeColumns = expectedTableSet.excludeColumns();
    final var columnStrategies = expectedTableSet.columnStrategies();
    final var dataSource = tableSet.getDataSource().orElseGet(() -> context.registry().get(""));

    final var tableCount = tableSet.getTables().size();
    logger.info(
        "Validating expectation TableSet for {}: {} tables",
        context.testMethod().getName(),
        tableCount);

    if (expectedTableSet.hasExclusions()) {
      logger.debug("Excluding columns from verification: {}", excludeColumns);
    }

    if (expectedTableSet.hasColumnStrategies()) {
      logger.debug("Using column strategies for: {}", columnStrategies.keySet());
    }

    try {
      expectationProvider.verifyExpectation(tableSet, dataSource, excludeColumns, columnStrategies);

      logger.info(
          "Expectation validation completed successfully for {}: {} tables",
          context.testMethod().getName(),
          tableCount);
    } catch (final ValidationException e) {
      throw new ValidationException(
          String.format(
              "Failed to verify expectation TableSet for %s", context.testMethod().getName()),
          e);
    }
  }
}
