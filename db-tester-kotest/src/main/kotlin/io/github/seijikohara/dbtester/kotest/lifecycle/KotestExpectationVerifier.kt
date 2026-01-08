package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider
import org.slf4j.LoggerFactory
import java.util.ServiceLoader

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * The verifier delegates to [ExpectationProvider] for database operations. The verifier
 * opens a connection through the dataset's DataSource, applies column filtering so that
 * only declared columns participate in comparisons, and raises [ValidationException] when the
 * observed database state deviates from the expected dataset.
 *
 * Like [KotestPreparationExecutor], this class is stateless and thread-safe. It performs
 * structured logging to aid debugging and rewraps any [ValidationException] thrown by the
 * verifier with additional test context so failures remain actionable in the calling layer.
 *
 * @see KotestPreparationExecutor
 * @see ExpectationProvider
 */
class KotestExpectationVerifier {
    /** Companion object containing class-level logger. */
    companion object {
        private val logger = LoggerFactory.getLogger(KotestExpectationVerifier::class.java)
    }

    private val expectationProvider: ExpectationProvider =
        ServiceLoader.load(ExpectationProvider::class.java).findFirst().orElseThrow()

    /**
     * Verifies the database state against expected datasets.
     *
     * Loads the datasets specified in the [ExpectedDataSet] annotation (or resolved via
     * conventions) and compares them with the actual database state.
     *
     * @param context the test context containing configuration and registry
     * @param expectedDataSet the ExpectedDataSet annotation (currently unused but reserved for future options)
     * @throws AssertionError if the database state does not match the expected state
     */
    fun verify(
        context: TestContext,
        expectedDataSet: ExpectedDataSet,
    ): Unit =
        context.testMethod().name.let { methodName ->
            logger.debug(
                "Verifying expectation for test: {}.{}",
                context.testClass().simpleName,
                methodName,
            )
            context
                .configuration()
                .loader()
                .loadExpectationDataSetsWithExclusions(context)
                .takeIf { expectedTableSets -> expectedTableSets.isNotEmpty() }
                ?.also { expectedTableSets ->
                    expectedTableSets.forEach { expectedTableSet ->
                        verifyExpectedTableSet(context, expectedTableSet, methodName)
                    }
                }
                ?: logger.debug("No expectation datasets found")
        }

    /**
     * Verifies a single ExpectedTableSet against the database.
     *
     * Delegates to [ExpectationProvider.verifyExpectation] for full data comparison
     * including column filtering and detailed assertion messages. If verification fails, wraps the
     * exception with additional test context.
     *
     * @param context the test context providing access to the data source registry
     * @param expectedTableSet the expected TableSet with exclusion metadata
     * @param methodName the test method name for logging
     * @throws ValidationException if verification fails with wrapped context information
     */
    private fun verifyExpectedTableSet(
        context: TestContext,
        expectedTableSet: ExpectedTableSet,
        methodName: String,
    ): Unit =
        expectedTableSet.tableSet().let { tableSet ->
            val excludeColumns = expectedTableSet.excludeColumns()
            val dataSource = tableSet.dataSource.orElseGet { context.registry().get("") }
            val tableCount = tableSet.tables.size

            logger.info(
                "Validating expectation dataset for {}: {} tables",
                methodName,
                tableCount,
            )

            if (expectedTableSet.hasExclusions()) {
                logger.debug("Excluding columns from verification: {}", excludeColumns)
            }

            runCatching { expectationProvider.verifyExpectation(tableSet, dataSource, excludeColumns) }
                .onSuccess {
                    logger.info(
                        "Expectation validation completed successfully for {}: {} tables",
                        methodName,
                        tableCount,
                    )
                }.onFailure { e ->
                    when (e) {
                        is ValidationException -> throw ValidationException(
                            "Failed to verify expectation dataset for $methodName",
                            e,
                        )
                        else -> throw e
                    }
                }
        }
}
