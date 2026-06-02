package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>This class delegates to {@link ExpectationSupport} for the actual verification logic. It
 * provides a Spock-specific facade for framework-specific customization.
 *
 * @see ExpectationSupport
 * @see SpockPreparationExecutor
 */
class SpockExpectationVerifier {

	/** The expectation support. */
	private final ExpectationSupport support

	/** Creates a new expectation verifier. */
	SpockExpectationVerifier() {
		this.support = ServiceLoader.load(ExpectationSupport).findFirst()
				.orElseThrow {
					-> new IllegalStateException(
					'No ExpectationSupport implementation found. Ensure db-tester-core is on the classpath.')
				}
	}

	/**
	 * Creates a new expectation verifier with the specified support.
	 *
	 * @param support the expectation support
	 */
	SpockExpectationVerifier(ExpectationSupport support) {
		this.support = Objects.requireNonNull(support, 'support must not be null')
	}

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

		support.verify(context, expectedDataSet)
	}
}
