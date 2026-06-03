package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport
import java.util.ServiceLoader

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * This class delegates to [ExpectationSupport] for the actual verification logic. It
 * provides a Kotest-specific facade for framework-specific customization.
 *
 * @property support the expectation support
 * @see ExpectationSupport
 * @see KotestPreparationExecutor
 */
class KotestExpectationVerifier internal constructor(
    private val support: ExpectationSupport,
) {
    /** Creates a new expectation verifier with a support loaded via ServiceLoader. */
    constructor() : this(loadExpectationSupport())

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
    ): Unit = support.verify(context, expectedDataSet)

    /** Companion object containing factory methods. */
    companion object {
        private fun loadExpectationSupport(): ExpectationSupport =
            ServiceLoader
                .load(ExpectationSupport::class.java)
                .findFirst()
                .orElseThrow {
                    IllegalStateException(
                        "No ExpectationSupport implementation found. Ensure db-tester-core is on the classpath.",
                    )
                }
    }
}
