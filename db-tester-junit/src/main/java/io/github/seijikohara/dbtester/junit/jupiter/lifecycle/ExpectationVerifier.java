package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import java.time.Duration;
import java.util.List;
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
   * conventions) and compares them with the actual database state. Supports retry with configurable
   * count and delay for eventual consistency scenarios.
   *
   * @param context the test context containing configuration and registry
   * @param expectedDataSet the ExpectedDataSet annotation containing row ordering and retry
   *     settings
   * @throws AssertionError if the database state does not match the expected state after retries
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

    final var rowOrdering = expectedDataSet.rowOrdering();
    final var retryCount = resolveRetryCount(expectedDataSet, context);
    final var retryDelay = resolveRetryDelay(expectedDataSet, context);

    verifyWithRetry(context, expectedTableSets, rowOrdering, retryCount, retryDelay);
  }

  /**
   * Resolves the retry count from annotation or global settings.
   *
   * @param expectedDataSet the annotation
   * @param context the test context
   * @return the resolved retry count
   */
  private int resolveRetryCount(final ExpectedDataSet expectedDataSet, final TestContext context) {
    final var annotationValue = expectedDataSet.retryCount();
    return annotationValue >= 0
        ? annotationValue
        : context.configuration().conventions().retryCount();
  }

  /**
   * Resolves the retry delay from annotation or global settings.
   *
   * @param expectedDataSet the annotation
   * @param context the test context
   * @return the resolved retry delay
   */
  private Duration resolveRetryDelay(
      final ExpectedDataSet expectedDataSet, final TestContext context) {
    final var annotationValue = expectedDataSet.retryDelayMillis();
    return annotationValue >= 0
        ? Duration.ofMillis(annotationValue)
        : context.configuration().conventions().retryDelay();
  }

  /**
   * Verifies expectation datasets with retry support.
   *
   * @param context the test context
   * @param expectedTableSets the expected datasets
   * @param rowOrdering the row ordering strategy
   * @param retryCount the number of retries (0 = no retry)
   * @param retryDelay the delay between retries
   */
  private void verifyWithRetry(
      final TestContext context,
      final List<ExpectedTableSet> expectedTableSets,
      final RowOrdering rowOrdering,
      final int retryCount,
      final Duration retryDelay) {
    ValidationException lastException = null;

    for (int attempt = 0; attempt <= retryCount; attempt++) {
      try {
        if (attempt > 0) {
          logger.debug(
              "Retry attempt {} of {} after {} ms delay",
              attempt,
              retryCount,
              retryDelay.toMillis());
          Thread.sleep(retryDelay.toMillis());
        }

        expectedTableSets.forEach(
            expectedTableSet -> verifyExpectedTableSet(context, expectedTableSet, rowOrdering));

        // Success - exit the retry loop
        return;
      } catch (final ValidationException e) {
        lastException = e;
        if (attempt < retryCount) {
          logger.debug("Verification failed, will retry: {}", e.getMessage());
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ValidationException("Verification interrupted during retry delay", e);
      }
    }

    // All retries exhausted, throw the last exception
    throw lastException;
  }

  /**
   * Verifies a single ExpectedTableSet against the database.
   *
   * <p>Delegates to {@link ExpectationProvider#verifyExpectation} for full data comparison
   * including column filtering, column comparison strategies, row ordering, and detailed assertion
   * messages. If verification fails, wraps the exception with additional test context.
   *
   * @param context the test context providing access to the data source registry
   * @param expectedTableSet the expected TableSet with exclusion and strategy metadata
   * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
   * @throws ValidationException if verification fails with wrapped context information
   */
  private void verifyExpectedTableSet(
      final TestContext context,
      final ExpectedTableSet expectedTableSet,
      final RowOrdering rowOrdering) {
    final var tableSet = expectedTableSet.tableSet();
    final var excludeColumns = expectedTableSet.excludeColumns();
    final var columnStrategies = expectedTableSet.columnStrategies();
    final var dataSource = tableSet.getDataSource().orElseGet(() -> context.registry().get(""));

    final var tableCount = tableSet.getTables().size();
    logger.info(
        "Validating expectation TableSet for {}: {} tables ({})",
        context.testMethod().getName(),
        tableCount,
        rowOrdering);

    if (expectedTableSet.hasExclusions()) {
      logger.debug("Excluding columns from verification: {}", excludeColumns);
    }

    if (expectedTableSet.hasColumnStrategies()) {
      logger.debug("Using column strategies for: {}", columnStrategies.keySet());
    }

    try {
      expectationProvider.verifyExpectation(
          tableSet, dataSource, excludeColumns, columnStrategies, rowOrdering);

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
