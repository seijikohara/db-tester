package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.PreparationSupport

/**
 * Executes the preparation phase of database testing for Spock specifications.
 *
 * <p>This class delegates to {@link PreparationSupport} for the actual preparation logic. It
 * provides a Spock-specific facade for backward compatibility and framework-specific customization.
 *
 * @see PreparationSupport
 */
class SpockPreparationExecutor {

	/** The preparation support. */
	private final PreparationSupport support

	/** Creates a new preparation executor. */
	SpockPreparationExecutor() {
		this.support = ServiceLoader.load(PreparationSupport).findFirst()
				.orElseThrow {
					-> new IllegalStateException(
					'No PreparationSupport implementation found. Ensure db-tester-core is on the classpath.')
				}
	}

	/**
	 * Creates a new preparation executor with the specified support.
	 *
	 * @param support the preparation support
	 */
	SpockPreparationExecutor(PreparationSupport support) {
		this.support = Objects.requireNonNull(support, 'support must not be null')
	}

	/**
	 * Executes the preparation phase.
	 *
	 * <p>Loads the datasets specified in the {@link DataSet} annotation (or resolved via conventions)
	 * and applies them to the database using the configured operation.
	 *
	 * @param context the test context containing configuration and registry
	 * @param dataSet the DataSet annotation specifying the operation to perform
	 */
	void execute(TestContext context, DataSet dataSet) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(dataSet, 'dataSet must not be null')

		support.execute(context, dataSet)
	}
}
