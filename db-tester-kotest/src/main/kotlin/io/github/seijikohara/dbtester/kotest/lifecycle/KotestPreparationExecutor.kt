package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.PreparationSupport
import java.util.ServiceLoader

/**
 * Executes the preparation phase of database testing.
 *
 * This class delegates to [PreparationSupport] for the actual preparation logic. It
 * provides a Kotest-specific facade for framework-specific customization.
 *
 * @property support the preparation support
 * @see PreparationSupport
 */
public class KotestPreparationExecutor internal constructor(
    private val support: PreparationSupport,
) {
    /** Creates a new preparation executor with a support loaded via ServiceLoader. */
    public constructor() : this(loadPreparationSupport())

    /**
     * Executes the preparation phase.
     *
     * Loads the datasets specified in the [DataSet] annotation (or resolved via
     * conventions) and applies them to the database using the configured operation.
     *
     * @param context the test context containing configuration and registry
     * @param dataSet the DataSet annotation specifying the operation to perform
     */
    public fun execute(
        context: TestContext,
        dataSet: DataSet,
    ): Unit = support.execute(context, dataSet)

    /** Companion object containing factory methods. */
    private companion object {
        private fun loadPreparationSupport(): PreparationSupport =
            ServiceLoader
                .load(PreparationSupport::class.java)
                .findFirst()
                .orElseThrow {
                    IllegalStateException(
                        "No PreparationSupport implementation found. Ensure db-tester-core is on the classpath.",
                    )
                }
    }
}
