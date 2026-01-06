package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.OperationProvider
import org.slf4j.LoggerFactory
import java.util.ServiceLoader

/**
 * Executes the preparation phase of database testing.
 *
 * This class loads datasets according to the [DataSet] annotation and applies them to
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
     * Loads the datasets specified in the [DataSet] annotation (or resolved via
     * conventions) and applies them to the database using the configured operation.
     *
     * @param context the test context containing configuration and registry
     * @param dataSet the DataSet annotation specifying the operation to perform
     */
    fun execute(
        context: TestContext,
        dataSet: DataSet,
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
                    .takeIf { tableSets -> tableSets.isNotEmpty() }
                    ?.also { tableSets ->
                        tableSets.forEach { tableSet ->
                            executeTableSet(context, tableSet, dataSet.operation, dataSet.tableOrdering)
                        }
                    }
                    ?: logger.debug("No preparation datasets found")
            }

    /**
     * Executes a single TableSet against the database.
     *
     * This method resolves the DataSource from either the TableSet itself or falls back to the
     * default registry. It then delegates to the operation executor to apply the TableSet using the
     * specified operation.
     *
     * @param context the test context providing access to the data source registry
     * @param tableSet the TableSet to execute containing tables and optional data source
     * @param operation the database operation to perform (CLEAN_INSERT, INSERT, etc.)
     * @param tableOrderingStrategy the strategy for determining table processing order
     */
    private fun executeTableSet(
        context: TestContext,
        tableSet: TableSet,
        operation: Operation,
        tableOrderingStrategy: TableOrderingStrategy,
    ): Unit =
        tableSet.dataSource
            .orElseGet { context.registry().get("") }
            .also {
                logger.debug(
                    "Applying {} operation with dataset using {} table ordering",
                    operation,
                    tableOrderingStrategy,
                )
            }.let { dataSource ->
                operationProvider.execute(operation, tableSet, dataSource, tableOrderingStrategy)
            }
}
