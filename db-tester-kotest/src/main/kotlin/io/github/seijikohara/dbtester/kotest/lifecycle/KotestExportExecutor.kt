package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.ExportSupport
import java.util.ServiceLoader

/**
 * Executes database export after test execution.
 *
 * This class delegates to [ExportSupport] for the actual export logic. It provides a
 * Kotest-specific facade for framework-specific customization.
 *
 * @property support the export support
 * @see ExportSupport
 * @see KotestPreparationExecutor
 * @see KotestExpectationVerifier
 */
public class KotestExportExecutor internal constructor(
    private val support: ExportSupport,
) {
    /** Creates a new export executor with a support loaded via ServiceLoader. */
    public constructor() : this(loadExportSupport())

    /**
     * Exports the current database state to files.
     *
     * @param context the test context containing configuration and registry
     * @param exportDataSet the ExportDataSet annotation containing export settings
     */
    public fun export(
        context: TestContext,
        exportDataSet: ExportDataSet,
    ): Unit = support.export(context, exportDataSet)

    /** Companion object containing factory methods. */
    private companion object {
        private fun loadExportSupport(): ExportSupport =
            ServiceLoader
                .load(ExportSupport::class.java)
                .findFirst()
                .orElseThrow {
                    IllegalStateException(
                        "No ExportSupport implementation found. Ensure db-tester-core is on the classpath.",
                    )
                }
    }
}
