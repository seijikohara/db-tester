package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.DataSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.spi.OperationProvider

/**
 * Executes the preparation phase of database testing for Spock specifications.
 *
 * <p>This class loads datasets according to the {@link Preparation} annotation and applies them
 * to the database using the configured operation.
 */
@Slf4j
class SpockPreparationExecutor {

	private final OperationProvider operationProvider = ServiceLoader.load(OperationProvider).findFirst().orElseThrow()

	/**
	 * Executes the preparation phase.
	 *
	 * @param context the test context
	 * @param preparation the preparation annotation
	 */
	void execute(TestContext context, Preparation preparation) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(preparation, 'preparation must not be null')

		log.debug('Executing preparation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		List<DataSet> dataSets = context.configuration().loader().loadPreparationDataSets(context)

		if (dataSets.empty) {
			log.debug('No preparation datasets found')
			return
		}

		def operation = preparation.operation()

		dataSets.each { dataSet ->
			executeDataSet(context, dataSet, operation)
		}
	}

	/**
	 * Executes a single dataset against the database.
	 *
	 * <p>This method resolves the DataSource from either the dataset itself or falls back
	 * to the default registry. It then delegates to the operation executor to apply the dataset
	 * using the specified operation.
	 *
	 * @param context the test context providing access to the data source registry
	 * @param dataSet the dataset to execute containing tables and optional data source
	 * @param operation the database operation to perform
	 */
	private void executeDataSet(TestContext context, DataSet dataSet, Operation operation) {
		def dataSource = dataSet.dataSource
				.orElseGet { -> context.registry().get('') }

		log.debug('Applying {} operation with dataset', operation)

		operationProvider.execute(operation, dataSet, dataSource)
	}
}
