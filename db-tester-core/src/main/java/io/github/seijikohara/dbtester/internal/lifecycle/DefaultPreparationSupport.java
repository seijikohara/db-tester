package io.github.seijikohara.dbtester.internal.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import io.github.seijikohara.dbtester.api.spi.PreparationSupport;
import java.util.Objects;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link PreparationSupport}.
 *
 * <p>This class provides the common preparation logic used by all test framework integrations
 * (JUnit, Spock, Kotest). It delegates to {@link OperationProvider} for database operations.
 */
public final class DefaultPreparationSupport implements PreparationSupport {

  /** Logger for tracking preparation execution. */
  private static final Logger logger = LoggerFactory.getLogger(DefaultPreparationSupport.class);

  /** The operation provider for database operations. */
  private final OperationProvider operationProvider;

  /** Creates a new instance with an operation provider loaded via ServiceLoader. */
  public DefaultPreparationSupport() {
    this.operationProvider =
        ServiceLoader.load(OperationProvider.class)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No OperationProvider implementation found. "
                            + "Ensure db-tester-core is on the classpath."));
  }

  /**
   * Creates a new instance with the specified operation provider.
   *
   * @param operationProvider the operation provider
   */
  DefaultPreparationSupport(final OperationProvider operationProvider) {
    this.operationProvider =
        Objects.requireNonNull(operationProvider, "operationProvider must not be null");
  }

  @Override
  public void execute(final TestContext context, final DataSet dataSet) {
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(dataSet, "dataSet must not be null");

    logger.debug(
        "Executing preparation for test: {}.{}",
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    final var tableSets = context.configuration().loader().loadPreparationDataSets(context);

    if (tableSets.isEmpty()) {
      logger.debug("No preparation datasets found");
      return;
    }

    tableSets.forEach(tableSet -> executeTableSet(context, tableSet, dataSet));
  }

  /**
   * Executes a single TableSet against the database.
   *
   * @param context the test context
   * @param tableSet the table set to execute
   * @param dataSet the DataSet annotation containing operation settings
   */
  private void executeTableSet(
      final TestContext context, final TableSet tableSet, final DataSet dataSet) {
    final var dataSource = tableSet.dataSource().orElseGet(() -> context.registry().get(""));
    final var tableCount = tableSet.tables().size();

    logger.info(
        "Executing preparation TableSet for {}: {} tables",
        context.testMethod().getName(),
        tableCount);

    executeOperationOnDataSource(context, tableSet, dataSet, dataSource);

    logger.info(
        "Preparation completed successfully for {}: {} tables",
        context.testMethod().getName(),
        tableCount);
  }

  /**
   * Executes the operation on the data source.
   *
   * @param context the test context
   * @param tableSet the table set
   * @param dataSet the data set annotation
   * @param dataSource the data source
   */
  private void executeOperationOnDataSource(
      final TestContext context,
      final TableSet tableSet,
      final DataSet dataSet,
      final DataSource dataSource) {
    final var operation = dataSet.operation();
    final var tableOrdering = dataSet.tableOrdering();
    final var execution = context.configuration().execution();
    final var transactionMode = execution.transactionMode();
    final var queryTimeout = execution.queryTimeout();
    final var batchSize = resolveBatchSize(dataSet, context);

    operationProvider.execute(
        operation, tableSet, dataSource, tableOrdering, transactionMode, queryTimeout, batchSize);
  }

  /**
   * Resolves the effective batch size from the annotation and global defaults.
   *
   * <p>If the annotation specifies a non-negative value, that value is used directly. If the
   * annotation value is {@link DataSet#UNSET} (the default), the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.OperationDefaults#batchSize()} is used.
   *
   * @param dataSet the DataSet annotation
   * @param context the test context
   * @return the effective batch size (zero or positive)
   * @throws IllegalArgumentException if batchSize is less than {@link DataSet#UNSET}
   */
  private int resolveBatchSize(final DataSet dataSet, final TestContext context) {
    final var annotationBatchSize = dataSet.batchSize();
    if (annotationBatchSize < DataSet.UNSET) {
      throw new IllegalArgumentException(
          String.format(
              "batchSize must be %d (use global), 0 (single batch), or positive. Got: %d",
              DataSet.UNSET, annotationBatchSize));
    }
    if (annotationBatchSize >= 0) {
      return annotationBatchSize;
    }
    return context.configuration().operations().batchSize();
  }
}
