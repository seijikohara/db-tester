package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.internal.context.TestContext;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet;
import io.github.seijikohara.dbtester.internal.dbunit.DatabaseBridge;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>The verifier delegates to {@link DatabaseBridge} for all DbUnit operations. The bridge opens a
 * connection through the dataset's {@link DataSource}, applies column filtering so that only
 * declared columns participate in comparisons, and raises {@link ValidationException} when the
 * observed database state deviates from the expected dataset.
 *
 * <p>Like {@link PreparationExecutor}, this class is stateless and thread-safe. It performs
 * structured logging to aid debugging and rewraps any {@link ValidationException} thrown by the
 * bridge with additional test context so failures remain actionable in the calling layer.
 *
 * @see PreparationExecutor
 * @see DatabaseBridge
 */
public final class ExpectationVerifier {

  /** Logger for tracking expectation verification. */
  private static final Logger logger = LoggerFactory.getLogger(ExpectationVerifier.class);

  /** Creates a new expectation verifier. */
  public ExpectationVerifier() {
    // Default constructor
  }

  /**
   * Verifies the database state against expected datasets.
   *
   * <p>Loads the datasets specified in the {@link Expectation} annotation (or resolved via
   * conventions) and compares them with the actual database state using DbUnit assertions.
   *
   * @param context the test context containing configuration and registry
   * @param expectation the expectation annotation (currently unused but reserved for future
   *     options)
   * @throws AssertionError if the database state does not match the expected state
   */
  public void verify(final TestContext context, final Expectation expectation) {
    logger.debug(
        "Verifying expectation for test: {}.{}",
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    final var dataSets = context.configuration().loader().loadExpectationDataSets(context);

    if (dataSets.isEmpty()) {
      logger.debug("No expectation datasets found");
      return;
    }

    dataSets.forEach(scenarioDataSet -> verifyDataSet(context, scenarioDataSet));
  }

  /**
   * Verifies a single dataset against the database.
   *
   * <p>Delegates to {@link
   * DatabaseBridge#verifyExpectation(io.github.seijikohara.dbtester.internal.dataset.DataSet,
   * DataSource)} for full data comparison including column filtering and detailed assertion
   * messages. If verification fails, wraps the exception with additional test context.
   *
   * @param context the test context providing access to the data source registry
   * @param scenarioDataSet the expected dataset containing tables and optional data source
   * @throws ValidationException if verification fails with wrapped context information
   */
  private void verifyDataSet(final TestContext context, final ScenarioDataSet scenarioDataSet) {
    final var dataSource =
        scenarioDataSet.getDataSource().orElseGet(() -> context.registry().get(""));

    final var tableCount = scenarioDataSet.getTables().size();
    logger.info(
        "Validating expectation dataset for {}: {} tables",
        context.testMethod().getName(),
        tableCount);

    try {
      DatabaseBridge.getInstance().verifyExpectation(scenarioDataSet, dataSource);

      logger.info(
          "Expectation validation completed successfully for {}: {} tables",
          context.testMethod().getName(),
          tableCount);
    } catch (final ValidationException e) {
      throw new ValidationException(
          String.format(
              "Failed to verify expectation dataset for %s", context.testMethod().getName()),
          e);
    }
  }
}
