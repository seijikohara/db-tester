package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.OperationProvider

/**
 * Executes the preparation phase of database testing for Spock specifications.
 *
 * <p>This class loads datasets according to the {@link DataSet} annotation and applies them
 * to the database using the configured operation.
 */
@Slf4j
class SpockPreparationExecutor {

	private final OperationProvider operationProvider = ServiceLoader.load(OperationProvider).findFirst().orElseThrow()

	/**
	 * Executes the preparation phase.
	 *
	 * @param context the test context
	 * @param dataSet the data set annotation
	 */
	void execute(TestContext context, DataSet dataSet) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(dataSet, 'dataSet must not be null')

		log.debug('Executing preparation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		List<TableSet> tableSets = context.configuration().loader().loadPreparationDataSets(context)

		if (tableSets.empty) {
			log.debug('No preparation datasets found')
			return
		}

		def operation = dataSet.operation()
		def tableOrderingStrategy = dataSet.tableOrdering()

		tableSets.each { tableSet ->
			executeTableSet(context, tableSet, operation, tableOrderingStrategy)
		}
	}

	/**
	 * Executes a single table set against the database.
	 *
	 * <p>This method resolves the DataSource from either the table set itself or falls back
	 * to the default registry. It then delegates to the operation executor to apply the table set
	 * using the specified operation.
	 *
	 * @param context the test context providing access to the data source registry
	 * @param tableSet the table set to execute containing tables and optional data source
	 * @param operation the database operation to perform
	 * @param tableOrderingStrategy the strategy for determining table processing order
	 */
	private void executeTableSet(TestContext context, TableSet tableSet, Operation operation, TableOrderingStrategy tableOrderingStrategy) {
		def dataSource = tableSet.dataSource
				.orElseGet { -> context.registry().get('') }

		log.debug('Applying {} operation with dataset using {} table ordering', operation, tableOrderingStrategy)

		operationProvider.execute(operation, tableSet, dataSource, tableOrderingStrategy)
	}
}
