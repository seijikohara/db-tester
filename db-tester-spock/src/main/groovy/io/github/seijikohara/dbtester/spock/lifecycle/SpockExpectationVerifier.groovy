package io.github.seijikohara.dbtester.spock.lifecycle

import groovy.util.logging.Slf4j
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.internal.context.TestContext
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet
import io.github.seijikohara.dbtester.internal.dbunit.DatabaseBridge

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>The verifier delegates to {@link DatabaseBridge} for all DbUnit operations. The bridge opens
 * a connection through the dataset's {@code DataSource}, applies column filtering so that only
 * declared columns participate in comparisons, and raises {@link ValidationException} when the
 * observed database state deviates from the expected dataset.
 *
 * <p>This class is stateless and thread-safe. It performs structured logging to aid debugging and
 * rewraps any {@link ValidationException} thrown by the bridge with additional test context so
 * failures remain actionable in the calling layer.
 *
 * @see SpockPreparationExecutor
 * @see DatabaseBridge
 */
@Slf4j
class SpockExpectationVerifier {

	/**
	 * Verifies the database state against expected datasets.
	 *
	 * <p>Loads the datasets specified in the {@link Expectation} annotation (or resolved via
	 * conventions) and compares them with the actual database state using DbUnit assertions.
	 *
	 * @param context the test context containing configuration and registry (must not be null)
	 * @param expectation the expectation annotation (must not be null)
	 * @throws AssertionError if the database state does not match the expected state
	 */
	void verify(TestContext context, Expectation expectation) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(expectation, 'expectation must not be null')

		log.debug('Verifying expectation for test: {}.{}',
				context.testClass().simpleName,
				context.testMethod().name)

		def dataSets = context.configuration().loader().loadExpectationDataSets(context)

		if (dataSets.empty) {
			log.debug('No expectation datasets found')
			return
		}

		dataSets.each { scenarioDataSet ->
			verifyDataSet(context, scenarioDataSet)
		}
	}

	/**
	 * Verifies a single dataset against the database.
	 *
	 * <p>Delegates to {@link DatabaseBridge#verifyExpectation} for full data comparison including
	 * column filtering and detailed assertion messages. If verification fails, wraps the exception
	 * with additional test context.
	 *
	 * @param context the test context providing access to the data source registry (must not be null)
	 * @param scenarioDataSet the expected dataset containing tables and optional data source (must not be null)
	 * @throws ValidationException if verification fails with wrapped context information
	 */
	private void verifyDataSet(TestContext context, ScenarioDataSet scenarioDataSet) {
		def dataSource = scenarioDataSet.dataSource
				.orElseGet { -> context.registry().get('') }

		def tableCount = scenarioDataSet.tables.size()
		log.info('Validating expectation dataset for {}: {} tables',
				context.testMethod().name,
				tableCount)

		try {
			DatabaseBridge.getInstance().verifyExpectation(scenarioDataSet, dataSource)

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
