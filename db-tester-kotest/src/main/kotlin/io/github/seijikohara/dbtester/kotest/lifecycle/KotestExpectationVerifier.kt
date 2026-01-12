package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider
import org.slf4j.LoggerFactory
import java.time.Duration
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
     * conventions) and compares them with the actual database state. Supports retry with
     * configurable count and delay for eventual consistency scenarios.
     *
     * @param context the test context containing configuration and registry
     * @param expectedDataSet the ExpectedDataSet annotation containing row ordering and retry settings
     * @throws AssertionError if the database state does not match the expected state after retries
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
                    verifyWithRetry(
                        context,
                        expectedTableSets,
                        methodName,
                        expectedDataSet.rowOrdering,
                        resolveRetryCount(expectedDataSet, context),
                        resolveRetryDelay(expectedDataSet, context),
                    )
                }
                ?: logger.debug("No expectation datasets found")
        }

    /**
     * Resolves the retry count from annotation or global settings.
     *
     * @param expectedDataSet the annotation
     * @param context the test context
     * @return the resolved retry count
     */
    private fun resolveRetryCount(
        expectedDataSet: ExpectedDataSet,
        context: TestContext,
    ): Int =
        expectedDataSet.retryCount
            .takeIf { it >= 0 }
            ?: context.configuration().conventions().retryCount()

    /**
     * Resolves the retry delay from annotation or global settings.
     *
     * @param expectedDataSet the annotation
     * @param context the test context
     * @return the resolved retry delay
     */
    private fun resolveRetryDelay(
        expectedDataSet: ExpectedDataSet,
        context: TestContext,
    ): Duration =
        expectedDataSet.retryDelayMillis
            .takeIf { it >= 0 }
            ?.let { Duration.ofMillis(it) }
            ?: context.configuration().conventions().retryDelay()

    /**
     * Verifies expectation datasets with retry support.
     *
     * @param context the test context
     * @param expectedTableSets the expected datasets
     * @param methodName the test method name for logging
     * @param rowOrdering the row ordering strategy
     * @param retryCount the number of retries (0 = no retry)
     * @param retryDelay the delay between retries
     */
    private fun verifyWithRetry(
        context: TestContext,
        expectedTableSets: List<ExpectedTableSet>,
        methodName: String,
        rowOrdering: RowOrdering,
        retryCount: Int,
        retryDelay: Duration,
    ) {
        var lastException: ValidationException? = null

        for (attempt in 0..retryCount) {
            try {
                if (attempt > 0) {
                    logger.debug(
                        "Retry attempt {} of {} after {} ms delay",
                        attempt,
                        retryCount,
                        retryDelay.toMillis(),
                    )
                    Thread.sleep(retryDelay.toMillis())
                }

                expectedTableSets.forEach { expectedTableSet ->
                    verifyExpectedTableSet(context, expectedTableSet, methodName, rowOrdering)
                }

                // Success - exit the retry loop
                return
            } catch (e: ValidationException) {
                lastException = e
                if (attempt < retryCount) {
                    logger.debug("Verification failed, will retry: {}", e.message)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw ValidationException("Verification interrupted during retry delay", e)
            }
        }

        // All retries exhausted, throw the last exception
        throw lastException!!
    }

    /**
     * Verifies a single ExpectedTableSet against the database.
     *
     * Delegates to [ExpectationProvider.verifyExpectation] for full data comparison
     * including column filtering, column comparison strategies, row ordering, and detailed
     * assertion messages. If verification fails, wraps the exception with additional test context.
     *
     * @param context the test context providing access to the data source registry
     * @param expectedTableSet the expected TableSet with exclusion and strategy metadata
     * @param methodName the test method name for logging
     * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
     * @throws ValidationException if verification fails with wrapped context information
     */
    private fun verifyExpectedTableSet(
        context: TestContext,
        expectedTableSet: ExpectedTableSet,
        methodName: String,
        rowOrdering: RowOrdering,
    ): Unit =
        expectedTableSet.tableSet().let { tableSet ->
            val excludeColumns = expectedTableSet.excludeColumns()
            val columnStrategies = expectedTableSet.columnStrategies()
            val dataSource = tableSet.dataSource.orElseGet { context.registry().get("") }
            val tableCount = tableSet.tables.size

            logger.info(
                "Validating expectation dataset for {}: {} tables ({})",
                methodName,
                tableCount,
                rowOrdering,
            )

            if (expectedTableSet.hasExclusions()) {
                logger.debug("Excluding columns from verification: {}", excludeColumns)
            }

            if (expectedTableSet.hasColumnStrategies()) {
                logger.debug("Using column strategies for: {}", columnStrategies.keys)
            }

            runCatching {
                expectationProvider.verifyExpectation(
                    tableSet,
                    dataSource,
                    excludeColumns,
                    columnStrategies,
                    rowOrdering,
                )
            }.onSuccess {
                logger.info(
                    "Expectation validation completed successfully for {}: {} tables",
                    methodName,
                    tableCount,
                )
            }.onFailure { e ->
                when (e) {
                    is ValidationException ->
                        throw ValidationException(
                            "Failed to verify expectation dataset for $methodName",
                            e,
                        )
                    else -> throw e
                }
            }
        }
}
