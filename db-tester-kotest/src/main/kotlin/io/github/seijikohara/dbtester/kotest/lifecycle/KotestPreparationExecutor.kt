package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.DataSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.OperationProvider
import org.slf4j.LoggerFactory
import java.util.ServiceLoader

/**
 * Executes the preparation phase of database testing.
 *
 * This class loads datasets according to the [Preparation] annotation and applies them to
 * the database using the configured operation.
 */
class KotestPreparationExecutor {
    /** Companion object containing class-level logger. */
    companion object {
        private val logger = LoggerFactory.getLogger(KotestPreparationExecutor::class.java)
    }

    private val operationProvider: OperationProvider =
        ServiceLoader.load(OperationProvider::class.java).findFirst().orElseThrow()

    /**
     * Executes the preparation phase.
     *
     * Loads the datasets specified in the [Preparation] annotation (or resolved via
     * conventions) and applies them to the database using the configured operation.
     *
     * @param context the test context containing configuration and registry
     * @param preparation the preparation annotation specifying the operation to perform
     */
    fun execute(
        context: TestContext,
        preparation: Preparation,
    ): Unit =
        logger
            .debug(
                "Executing preparation for test: {}.{}",
                context.testClass().simpleName,
                context.testMethod().name,
            ).let {
                context
                    .configuration()
                    .loader()
                    .loadPreparationDataSets(context)
                    .takeIf { dataSets -> dataSets.isNotEmpty() }
                    ?.also { dataSets ->
                        dataSets.forEach { dataSet ->
                            executeDataSet(context, dataSet, preparation.operation, preparation.tableOrdering)
                        }
                    }
                    ?: logger.debug("No preparation datasets found")
            }

    /**
     * Executes a single dataset against the database.
     *
     * This method resolves the DataSource from either the dataset itself or falls back to the
     * default registry. It then delegates to the operation executor to apply the dataset using the
     * specified operation.
     *
     * @param context the test context providing access to the data source registry
     * @param dataSet the dataset to execute containing tables and optional data source
     * @param operation the database operation to perform (CLEAN_INSERT, INSERT, etc.)
     * @param tableOrderingStrategy the strategy for determining table processing order
     */
    private fun executeDataSet(
        context: TestContext,
        dataSet: DataSet,
        operation: Operation,
        tableOrderingStrategy: TableOrderingStrategy,
    ): Unit =
        dataSet.dataSource
            .orElseGet { context.registry().get("") }
            .also {
                logger.debug(
                    "Applying {} operation with dataset using {} table ordering",
                    operation,
                    tableOrderingStrategy,
                )
            }.let { dataSource ->
                operationProvider.execute(operation, dataSet, dataSource, tableOrderingStrategy)
            }
}
