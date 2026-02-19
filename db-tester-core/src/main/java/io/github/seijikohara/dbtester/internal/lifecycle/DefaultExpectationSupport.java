package io.github.seijikohara.dbtester.internal.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
import io.github.seijikohara.dbtester.internal.assertion.ColumnPatternMatcher;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ExpectationSupport}.
 *
 * <p>This class provides the common expectation verification logic used by all test framework
 * integrations (JUnit, Spock, Kotest). It delegates to {@link ExpectationProvider} for database
 * comparison operations.
 */
public final class DefaultExpectationSupport implements ExpectationSupport {

  /** Logger for tracking expectation verification. */
  private static final Logger logger = LoggerFactory.getLogger(DefaultExpectationSupport.class);

  /** The expectation provider for database verification. */
  private final ExpectationProvider expectationProvider;

  /** Creates a new instance with an expectation provider loaded via ServiceLoader. */
  public DefaultExpectationSupport() {
    this.expectationProvider =
        ServiceLoader.load(ExpectationProvider.class)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No ExpectationProvider implementation found. "
                            + "Ensure db-tester-core is on the classpath."));
  }

  /**
   * Creates a new instance with the specified expectation provider.
   *
   * @param expectationProvider the expectation provider
   */
  DefaultExpectationSupport(final ExpectationProvider expectationProvider) {
    this.expectationProvider =
        Objects.requireNonNull(expectationProvider, "expectationProvider must not be null");
  }

  @Override
  public void verify(final TestContext context, final ExpectedDataSet expectedDataSet) {
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(expectedDataSet, "expectedDataSet must not be null");

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

    final var rowOrdering = resolveRowOrdering(expectedDataSet, context);
    final var tableOrdering = expectedDataSet.tableOrdering();
    final var retryCount = resolveRetryCount(expectedDataSet, context);
    final var retryDelay = resolveRetryDelay(expectedDataSet, context);

    verifyWithRetry(context, expectedTableSets, rowOrdering, tableOrdering, retryCount, retryDelay);
  }

  /**
   * Resolves the row ordering from annotation or global settings.
   *
   * <p>If the annotation specifies a concrete value ({@link RowOrdering#ORDERED} or {@link
   * RowOrdering#UNORDERED}), that value is used directly. If the annotation value is {@link
   * RowOrdering#UNSET} (the default), the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.VerificationSettings#rowOrdering()} is used.
   *
   * @param expectedDataSet the annotation
   * @param context the test context
   * @return the resolved row ordering
   */
  private RowOrdering resolveRowOrdering(
      final ExpectedDataSet expectedDataSet, final TestContext context) {
    final var annotationValue = expectedDataSet.rowOrdering();
    if (annotationValue == RowOrdering.UNSET) {
      return context.configuration().verification().rowOrdering();
    }
    return annotationValue;
  }

  /**
   * Resolves the retry count from annotation or global settings.
   *
   * <p>If the annotation specifies a non-negative value, that value is used directly. If the
   * annotation value is {@link ExpectedDataSet#UNSET} (the default), the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.VerificationSettings#retryCount()} is used.
   *
   * @param expectedDataSet the annotation
   * @param context the test context
   * @return the resolved retry count
   * @throws IllegalArgumentException if retryCount is less than {@link ExpectedDataSet#UNSET}
   */
  private int resolveRetryCount(final ExpectedDataSet expectedDataSet, final TestContext context) {
    final var annotationValue = expectedDataSet.retryCount();
    if (annotationValue < ExpectedDataSet.UNSET) {
      throw new IllegalArgumentException(
          String.format(
              "retryCount must be %d (use global), 0, or positive. Got: %d",
              ExpectedDataSet.UNSET, annotationValue));
    }
    return annotationValue >= 0
        ? annotationValue
        : context.configuration().verification().retryCount();
  }

  /**
   * Resolves the retry delay from annotation or global settings.
   *
   * <p>If the annotation specifies a non-negative value, that value is used directly. If the
   * annotation value is {@link ExpectedDataSet#UNSET} (the default), the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.VerificationSettings#retryDelay()} is used.
   *
   * @param expectedDataSet the annotation
   * @param context the test context
   * @return the resolved retry delay
   * @throws IllegalArgumentException if retryDelayMillis is less than {@link ExpectedDataSet#UNSET}
   */
  private Duration resolveRetryDelay(
      final ExpectedDataSet expectedDataSet, final TestContext context) {
    final var annotationValue = expectedDataSet.retryDelayMillis();
    if (annotationValue < ExpectedDataSet.UNSET) {
      throw new IllegalArgumentException(
          String.format(
              "retryDelayMillis must be %d (use global), 0, or positive. Got: %d",
              ExpectedDataSet.UNSET, annotationValue));
    }
    return annotationValue >= 0
        ? Duration.ofMillis(annotationValue)
        : context.configuration().verification().retryDelay();
  }

  /**
   * Verifies expectation datasets with retry support.
   *
   * @param context the test context
   * @param expectedTableSets the expected datasets
   * @param rowOrdering the row ordering strategy
   * @param tableOrdering the table ordering strategy
   * @param retryCount the number of retries (0 = no retry)
   * @param retryDelay the delay between retries
   */
  private void verifyWithRetry(
      final TestContext context,
      final List<ExpectedTableSet> expectedTableSets,
      final RowOrdering rowOrdering,
      final io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy tableOrdering,
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

        for (final var expectedTableSet : expectedTableSets) {
          verifyExpectedTableSet(context, expectedTableSet, rowOrdering, tableOrdering);
        }

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
    throw Objects.requireNonNull(lastException, "lastException must not be null after retry loop");
  }

  /**
   * Verifies a single ExpectedTableSet against the database.
   *
   * @param context the test context
   * @param expectedTableSet the expected table set
   * @param rowOrdering the row ordering strategy
   * @param tableOrdering the table ordering strategy
   */
  private void verifyExpectedTableSet(
      final TestContext context,
      final ExpectedTableSet expectedTableSet,
      final RowOrdering rowOrdering,
      final io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy tableOrdering) {
    final var tableSet = expectedTableSet.tableSet();
    final var rawExcludeColumns = expectedTableSet.excludeColumns();
    final var excludeColumns = resolveExcludeColumnPatterns(rawExcludeColumns, tableSet);
    final var columnStrategies = expectedTableSet.columnStrategies();
    final DataSource dataSource =
        tableSet.getDataSource().orElseGet(() -> context.registry().get(""));

    final var tableCount = tableSet.getTables().size();
    logger.info(
        "Validating expectation TableSet for {}: {} tables ({}, {})",
        context.testMethod().getName(),
        tableCount,
        rowOrdering,
        tableOrdering);

    if (expectedTableSet.hasExclusions()) {
      logger.debug("Excluding columns from verification: {}", excludeColumns);
    }

    if (expectedTableSet.hasColumnStrategies()) {
      logger.debug("Using column strategies for: {}", columnStrategies.keySet());
    }

    final var operationDefaults = context.configuration().operations();
    final var expectationContext =
        ExpectationContext.of(
            excludeColumns, columnStrategies, rowOrdering, operationDefaults, tableOrdering);

    try {
      expectationProvider.verifyExpectation(tableSet, dataSource, expectationContext);

      logger.info(
          "Expectation validation completed successfully for {}: {} tables",
          context.testMethod().getName(),
          tableCount);
    } catch (final ValidationException e) {
      throw new ValidationException(
          String.format(
              "Failed to verify expectation TableSet for %s", context.testMethod().getName()),
          e);
    } catch (final AssertionError e) {
      throw new ValidationException(
          String.format(
              "Failed to verify expectation TableSet for %s", context.testMethod().getName()),
          e);
    }
  }

  /**
   * Resolves glob patterns in exclude column entries against the expected table columns.
   *
   * <p>If any entry in {@code excludeColumns} contains glob wildcards ({@code *} or {@code ?}), the
   * patterns are matched against the column names in the expected tables. Non-pattern entries are
   * passed through unchanged.
   *
   * @param excludeColumns the exclude column entries (may contain both exact names and patterns)
   * @param tableSet the expected table set providing column names for pattern resolution
   * @return resolved set of column names (uppercase, no patterns remaining)
   */
  private Set<String> resolveExcludeColumnPatterns(
      final Collection<String> excludeColumns,
      final io.github.seijikohara.dbtester.api.dataset.TableSet tableSet) {
    final var hasPatterns = excludeColumns.stream().anyMatch(ColumnPatternMatcher::isPattern);
    if (!hasPatterns) {
      return Set.copyOf(excludeColumns);
    }

    final var allColumnNames =
        tableSet.getTables().stream()
            .map(Table::getColumns)
            .flatMap(Collection::stream)
            .map(ColumnName::value)
            .collect(Collectors.toUnmodifiableSet());

    final var resolved = ColumnPatternMatcher.resolvePatterns(excludeColumns, allColumnNames);
    logger.debug("Resolved exclude column patterns: {} -> {}", excludeColumns, resolved);
    return resolved;
  }
}
