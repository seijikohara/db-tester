package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the preparation phase of database testing.
 *
 * <p>This class loads datasets according to the {@link DataSet} annotation and applies them to the
 * database using the configured operation.
 */
public final class PreparationExecutor {

  /** Logger for tracking preparation execution. */
  private static final Logger logger = LoggerFactory.getLogger(PreparationExecutor.class);

  /** The operation provider for database operations. */
  private final OperationProvider operationProvider;

  /** Creates a new preparation executor. */
  public PreparationExecutor() {
    this.operationProvider = ServiceLoader.load(OperationProvider.class).findFirst().orElseThrow();
  }

  /**
   * Executes the preparation phase.
   *
   * <p>Loads the datasets specified in the {@link DataSet} annotation (or resolved via conventions)
   * and applies them to the database using the configured operation.
   *
   * @param context the test context containing configuration and registry
   * @param dataSet the DataSet annotation specifying the operation to perform
   */
  public void execute(final TestContext context, final DataSet dataSet) {
    logger.debug(
        "Executing preparation for test: {}.{}",
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    final var tableSets = context.configuration().loader().loadPreparationDataSets(context);

    if (tableSets.isEmpty()) {
      logger.debug("No preparation datasets found");
      return;
    }

    final var operation = dataSet.operation();
    final var tableOrderingStrategy = dataSet.tableOrdering();
    tableSets.forEach(
        tableSet -> executeTableSet(context, tableSet, operation, tableOrderingStrategy));
  }

  /**
   * Executes a single TableSet against the database.
   *
   * <p>This method resolves the DataSource from either the TableSet itself or falls back to the
   * default registry. It then delegates to the operation executor to apply the TableSet using the
   * specified operation.
   *
   * @param context the test context providing access to the data source registry
   * @param tableSet the TableSet to execute containing tables and optional data source
   * @param operation the database operation to perform (CLEAN_INSERT, INSERT, etc.)
   * @param tableOrderingStrategy the strategy for determining table processing order
   */
  private void executeTableSet(
      final TestContext context,
      final TableSet tableSet,
      final Operation operation,
      final TableOrderingStrategy tableOrderingStrategy) {
    final var dataSource = tableSet.getDataSource().orElseGet(() -> context.registry().get(""));

    logger.debug(
        "Applying {} operation with TableSet using {} table ordering",
        operation,
        tableOrderingStrategy);

    operationProvider.execute(operation, tableSet, dataSource, tableOrderingStrategy);
  }
}
