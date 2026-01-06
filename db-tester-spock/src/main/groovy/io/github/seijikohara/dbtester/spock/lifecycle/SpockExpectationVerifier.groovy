package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider

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

	private final ExpectationProvider expectationProvider = ServiceLoader.load(ExpectationProvider).findFirst().orElseThrow()

	/**
	 * Verifies the database state against expected datasets.
	 *
	 * <p>Loads the datasets specified in the {@link ExpectedDataSet} annotation (or resolved via
	 * conventions) and compares them with the actual database state.
	 *
	 * @param context the test context containing configuration and registry
	 * @param expectedDataSet the expected data set annotation
	 * @throws AssertionError if the database state does not match the expected state
	 */
	void verify(TestContext context, ExpectedDataSet expectedDataSet) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(expectedDataSet, 'expectedDataSet must not be null')

		log.debug('Verifying expectation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		def tableSets = context.configuration().loader().loadExpectationDataSets(context)

		if (tableSets.empty) {
			log.debug('No expectation datasets found')
			return
		}

		tableSets.each { tableSet ->
			verifyTableSet(context, tableSet)
		}
	}

	/**
	 * Verifies a single table set against the database.
	 *
	 * <p>Delegates to {@link ExpectationProvider#verifyExpectation} for full data comparison including
	 * column filtering and detailed assertion messages. If verification fails, wraps the exception
	 * with additional test context.
	 *
	 * @param context the test context providing access to the data source registry
	 * @param tableSet the expected table set containing tables and optional data source
	 * @throws ValidationException if verification fails with wrapped context information
	 */
	private void verifyTableSet(TestContext context, TableSet tableSet) {
		def dataSource = tableSet.dataSource
				.orElseGet { -> context.registry().get('') }

		def tableCount = tableSet.tables.size()
		log.info('Validating expectation dataset for {}: {} tables',
				context.testMethod().name,
				tableCount)

		try {
			expectationProvider.verifyExpectation(tableSet, dataSource)

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
