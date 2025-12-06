package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.internal.context.TestContext;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet;
import io.github.seijikohara.dbtester.internal.dbunit.DbUnitOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the preparation phase of database testing.
 *
 * <p>This class loads datasets according to the {@link Preparation} annotation and applies them to
 * the database using the configured operation.
 */
public final class PreparationExecutor {

  /** Logger for tracking preparation execution. */
  private static final Logger logger = LoggerFactory.getLogger(PreparationExecutor.class);

  /** The DbUnit operations bridge. */
  private final DbUnitOperations dbUnitOperations = new DbUnitOperations();

  /** Creates a new preparation executor. */
  public PreparationExecutor() {
    // Default constructor
  }

  /**
   * Executes the preparation phase.
   *
   * <p>Loads the datasets specified in the {@link Preparation} annotation (or resolved via
   * conventions) and applies them to the database using the configured operation.
   *
   * @param context the test context containing configuration and registry
   * @param preparation the preparation annotation specifying the operation to perform
   */
  public void execute(final TestContext context, final Preparation preparation) {
    logger.debug(
        "Executing preparation for test: {}.{}",
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    final var dataSets = context.configuration().loader().loadPreparationDataSets(context);

    if (dataSets.isEmpty()) {
      logger.debug("No preparation datasets found");
      return;
    }

    final var operation = preparation.operation();
    dataSets.forEach(scenarioDataSet -> executeDataSet(context, scenarioDataSet, operation));
  }

  /**
   * Executes a single dataset against the database.
   *
   * <p>This method resolves the DataSource from either the scenario dataset itself or falls back to
   * the default registry. It then delegates to DbUnit operations to apply the dataset using the
   * specified operation.
   *
   * @param context the test context providing access to the data source registry
   * @param scenarioDataSet the dataset to execute containing tables and optional data source
   * @param operation the database operation to perform (CLEAN_INSERT, INSERT, etc.)
   */
  private void executeDataSet(
      final TestContext context, final ScenarioDataSet scenarioDataSet, final Operation operation) {
    final var dataSource =
        scenarioDataSet.getDataSource().orElseGet(() -> context.registry().get(""));

    logger.debug("Applying {} operation with dataset", operation);

    dbUnitOperations.execute(operation, scenarioDataSet, dataSource);
  }
}
