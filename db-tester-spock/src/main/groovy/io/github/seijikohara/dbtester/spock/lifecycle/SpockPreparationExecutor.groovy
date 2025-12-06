package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.internal.context.TestContext
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet
import io.github.seijikohara.dbtester.internal.dbunit.DbUnitOperations

/**
 * Executes the preparation phase of database testing for Spock specifications.
 *
 * <p>This class loads datasets according to the {@link Preparation} annotation and applies them
 * to the database using the configured operation.
 */
@Slf4j
class SpockPreparationExecutor {

	private final DbUnitOperations dbUnitOperations = new DbUnitOperations()

	/**
	 * Executes the preparation phase.
	 *
	 * @param context the test context (must not be null)
	 * @param preparation the preparation annotation (must not be null)
	 */
	void execute(TestContext context, Preparation preparation) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(preparation, 'preparation must not be null')

		log.debug('Executing preparation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		List<ScenarioDataSet> dataSets = context.configuration().loader().loadPreparationDataSets(context)

		if (dataSets.empty) {
			log.debug('No preparation datasets found')
			return
		}

		def operation = preparation.operation()

		dataSets.each { scenarioDataSet ->
			executeDataSet(context, scenarioDataSet, operation)
		}
	}

	/**
	 * Executes a single dataset against the database.
	 *
	 * <p>This method resolves the DataSource from either the scenario dataset itself or falls back
	 * to the default registry. It then delegates to DbUnit operations to apply the dataset using
	 * the specified operation.
	 *
	 * @param context the test context providing access to the data source registry (must not be null)
	 * @param scenarioDataSet the dataset to execute containing tables and optional data source (must not be null)
	 * @param operation the database operation to perform (must not be null)
	 */
	private void executeDataSet(TestContext context, ScenarioDataSet scenarioDataSet, Operation operation) {
		def dataSource = scenarioDataSet.dataSource
				.orElseGet { -> context.registry().get('') }

		log.debug('Applying {} operation with dataset', operation)

		dbUnitOperations.execute(operation, scenarioDataSet, dataSource)
	}
}
