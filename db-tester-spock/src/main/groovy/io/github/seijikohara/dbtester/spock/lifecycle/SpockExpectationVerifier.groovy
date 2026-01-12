package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider
import java.time.Duration

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>The verifier delegates to {@link ExpectationProvider} for database operations. The verifier opens
 * a connection through the dataset's {@code DataSource}, applies column filtering so that only
 * declared columns participate in comparisons, and raises {@link ValidationException} when the
 * observed database state deviates from the expected dataset.
 *
 * <p>This class is stateless and thread-safe. It performs structured logging to aid debugging and
 * rewraps any {@link ValidationException} thrown by the verifier with additional test context so
 * failures remain actionable in the calling layer.
 *
 * @see SpockPreparationExecutor
 * @see ExpectationProvider
 */
@Slf4j
class SpockExpectationVerifier {

	/** Provider for expectation verification operations. */
	private final ExpectationProvider expectationProvider = ServiceLoader.load(ExpectationProvider).findFirst().orElseThrow()

	/**
	 * Verifies the database state against expected datasets.
	 *
	 * <p>Loads the datasets specified in the {@link ExpectedDataSet} annotation (or resolved via
	 * conventions) and compares them with the actual database state. Supports retry with configurable
	 * count and delay for eventual consistency scenarios.
	 *
	 * @param context the test context containing configuration and registry
	 * @param expectedDataSet the expected data set annotation containing row ordering and retry settings
	 * @throws AssertionError if the database state does not match the expected state after retries
	 */
	void verify(TestContext context, ExpectedDataSet expectedDataSet) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(expectedDataSet, 'expectedDataSet must not be null')

		log.debug('Verifying expectation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		def expectedTableSets = context.configuration().loader().loadExpectationDataSetsWithExclusions(context)

		if (expectedTableSets.empty) {
			log.debug('No expectation datasets found')
			return
		}

		def rowOrdering = expectedDataSet.rowOrdering()
		def retryCount = resolveRetryCount(expectedDataSet, context)
		def retryDelay = resolveRetryDelay(expectedDataSet, context)

		verifyWithRetry(context, expectedTableSets, rowOrdering, retryCount, retryDelay)
	}

	/**
	 * Resolves the retry count from annotation or global settings.
	 *
	 * @param expectedDataSet the annotation
	 * @param context the test context
	 * @return the resolved retry count
	 */
	private int resolveRetryCount(ExpectedDataSet expectedDataSet, TestContext context) {
		def annotationValue = expectedDataSet.retryCount()
		annotationValue >= 0 ? annotationValue : context.configuration().conventions().retryCount()
	}

	/**
	 * Resolves the retry delay from annotation or global settings.
	 *
	 * @param expectedDataSet the annotation
	 * @param context the test context
	 * @return the resolved retry delay
	 */
	private Duration resolveRetryDelay(ExpectedDataSet expectedDataSet, TestContext context) {
		def annotationValue = expectedDataSet.retryDelayMillis()
		annotationValue >= 0 ? Duration.ofMillis(annotationValue) : context.configuration().conventions().retryDelay()
	}

	/**
	 * Verifies expectation datasets with retry support.
	 *
	 * @param context the test context
	 * @param expectedTableSets the expected datasets
	 * @param rowOrdering the row ordering strategy
	 * @param retryCount the number of retries (0 = no retry)
	 * @param retryDelay the delay between retries
	 */
	private void verifyWithRetry(
			TestContext context,
			List<ExpectedTableSet> expectedTableSets,
			RowOrdering rowOrdering,
			int retryCount,
			Duration retryDelay) {
		ValidationException lastException = null

		for (int attempt = 0; attempt <= retryCount; attempt++) {
			try {
				if (attempt > 0) {
					log.debug('Retry attempt {} of {} after {} ms delay', attempt, retryCount, retryDelay.toMillis())
					Thread.sleep(retryDelay.toMillis())
				}

				expectedTableSets.each { expectedTableSet ->
					verifyExpectedTableSet(context, expectedTableSet, rowOrdering)
				}

				// Success - exit the retry loop
				return
			} catch (ValidationException e) {
				lastException = e
				if (attempt < retryCount) {
					log.debug('Verification failed, will retry: {}', e.message)
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt()
				throw new ValidationException('Verification interrupted during retry delay', e)
			}
		}

		// All retries exhausted, throw the last exception
		throw lastException
	}

	/**
	 * Verifies a single expected table set against the database.
	 *
	 * <p>Delegates to {@link ExpectationProvider#verifyExpectation} for full data comparison including
	 * column filtering, column comparison strategies, row ordering, and detailed assertion messages.
	 * If verification fails, wraps the exception with additional test context.
	 *
	 * @param context the test context providing access to the data source registry
	 * @param expectedTableSet the expected table set with exclusion and strategy metadata
	 * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
	 * @throws ValidationException if verification fails with wrapped context information
	 */
	private void verifyExpectedTableSet(TestContext context, ExpectedTableSet expectedTableSet, RowOrdering rowOrdering) {
		def tableSet = expectedTableSet.tableSet()
		def excludeColumns = expectedTableSet.excludeColumns()
		def columnStrategies = expectedTableSet.columnStrategies()
		def dataSource = tableSet.dataSource
				.orElseGet { -> context.registry().get('') }

		def tableCount = tableSet.tables.size()
		log.info('Validating expectation dataset for {}: {} tables ({})',
				context.testMethod().name,
				tableCount,
				rowOrdering)

		if (expectedTableSet.hasExclusions()) {
			log.debug('Excluding columns from verification: {}', excludeColumns)
		}

		if (expectedTableSet.hasColumnStrategies()) {
			log.debug('Using column strategies for: {}', columnStrategies.keySet())
		}

		try {
			expectationProvider.verifyExpectation(tableSet, dataSource, excludeColumns, columnStrategies, rowOrdering)

			log.info('Expectation validation completed successfully for {}: {} tables',
					context.testMethod().name,
					tableCount)
		} catch (ValidationException e) {
			throw new ValidationException(
			"Failed to verify expectation dataset for ${context.testMethod().name}",
			e)
		}
	}
}
