package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.ExportSupport

/**
 * Executes database export after test execution.
 *
 * <p>This class delegates to {@link ExportSupport} for the actual export logic. It provides a
 * Spock-specific facade for framework-specific customization.
 *
 * @see ExportSupport
 * @see SpockPreparationExecutor
 * @see SpockExpectationVerifier
 */
class SpockExportExecutor {

	/** The export support. */
	private final ExportSupport support

	/** Creates a new export executor. */
	SpockExportExecutor() {
		this.support = ServiceLoader.load(ExportSupport).findFirst()
				.orElseThrow {
					-> new IllegalStateException(
					'No ExportSupport implementation found. Ensure db-tester-core is on the classpath.')
				}
	}

	/**
	 * Creates a new export executor with the specified support.
	 *
	 * @param support the export support
	 */
	SpockExportExecutor(ExportSupport support) {
		this.support = Objects.requireNonNull(support, 'support must not be null')
	}

	/**
	 * Exports the current database state to files.
	 *
	 * @param context the test context containing configuration and registry
	 * @param exportDataSet the export data set annotation containing export settings
	 */
	void export(TestContext context, ExportDataSet exportDataSet) {
		Objects.requireNonNull(context, 'context must not be null')
		Objects.requireNonNull(exportDataSet, 'exportDataSet must not be null')

		support.export(context, exportDataSet)
	}
}
