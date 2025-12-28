package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExpectationVerifier
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestPreparationExecutor
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

/**
 * Kotest extension for database testing.
 *
 * This extension processes [Preparation] and [Expectation] annotations to set up
 * test data before each test and verify database state after each test.
 *
 * The extension performs two responsibilities:
 * 1. Before each test, resolves [Preparation] declarations and executes the resulting datasets.
 * 2. After each test, resolves [Expectation] declarations and validates the database contents.
 *
 * **Usage with AnnotationSpec:**
 * ```kotlin
 * class MyTest : AnnotationSpec() {
 *     override fun extensions(): List<Extension> =
 *         listOf(DatabaseTestExtension(::getDbTesterRegistry))
 *
 *     @Test
 *     @Preparation
 *     @Expectation
 *     fun `should verify database state`() {
 *         // test implementation
 *     }
 * }
 * ```
 *
 * @property registryProvider provider function that returns the [DataSourceRegistry]
 * @property configurationProvider optional provider function for custom [Configuration]
 * @see Preparation
 * @see Expectation
 */
class DatabaseTestExtension(
    private val registryProvider: () -> DataSourceRegistry,
    private val configurationProvider: () -> Configuration = { Configuration.defaults() },
) : TestCaseExtension {
    /** Companion object containing class-level constants and logger. */
    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseTestExtension::class.java)
    }

    private val preparationExecutor = KotestPreparationExecutor()
    private val expectationVerifier = KotestExpectationVerifier()

    /**
     * Intercepts test case execution to handle preparation and expectation phases.
     *
     * @param testCase the test case being executed
     * @param execute the function to execute the test case
     * @return the test result
     */
    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult,
    ): TestResult =
        requireMethod(testCase)
            .let { method ->
                findPreparation(testCase, method) to findExpectation(testCase, method)
            }.let { (preparation, expectation) ->
                when {
                    preparation != null || expectation != null ->
                        executeWithAnnotations(testCase, execute, preparation, expectation)
                    else -> execute(testCase)
                }
            }

    /**
     * Executes the test case with preparation and/or expectation handling.
     *
     * @param testCase the test case being executed
     * @param execute the function to execute the test case
     * @param preparation the preparation annotation, or null
     * @param expectation the expectation annotation, or null
     * @return the test result
     */
    private suspend fun executeWithAnnotations(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult,
        preparation: Preparation?,
        expectation: Expectation?,
    ): TestResult =
        createTestContext(testCase, requireMethod(testCase)).let { testContext ->
            preparation?.also {
                logger.debug(
                    "Executing preparation for {}.{}()",
                    testContext.testClass().simpleName,
                    testContext.testMethod().name,
                )
                preparationExecutor.execute(testContext, it)
            }
            execute(testCase).also { result ->
                if (result is TestResult.Success && expectation != null) {
                    logger.debug(
                        "Verifying expectation for {}.{}()",
                        testContext.testClass().simpleName,
                        testContext.testMethod().name,
                    )
                    expectationVerifier.verify(testContext, expectation)
                }
            }
        }

    /**
     * Creates a [TestContext] from the Kotest [TestCase].
     *
     * @param testCase the Kotest test case
     * @param method the resolved test method
     * @return the test context
     */
    private fun createTestContext(
        testCase: TestCase,
        method: Method,
    ): TestContext =
        TestContext(
            testCase.spec::class.java,
            method,
            configurationProvider(),
            registryProvider(),
        )

    /**
     * Requires and returns the test method from the test case.
     *
     * For AnnotationSpec, the method name is derived from the test case name.
     * Handles both regular method names and backtick-escaped names.
     *
     * @param testCase the test case
     * @return the resolved method
     * @throws IllegalStateException if method resolution fails
     */
    private fun requireMethod(testCase: TestCase): Method =
        testCase.spec::class.let { specClass ->
            specClass.members
                .firstOrNull { it.name == testCase.name.name }
                ?.let { member ->
                    @Suppress("UNCHECKED_CAST")
                    (member as? kotlin.reflect.KFunction<*>)?.javaMethod
                }
                ?: specClass.java.declaredMethods.firstOrNull { method ->
                    method.name == sanitizeMethodName(testCase.name.name) ||
                        method.name == testCase.name.name
                }
        } ?: throw IllegalStateException(
            String.format(
                "Cannot resolve test method '%s' in class '%s'. " +
                    "DatabaseTestExtension requires AnnotationSpec style tests.",
                testCase.name.name,
                testCase.spec::class.java.name,
            ),
        )

    /**
     * Sanitizes a method name by replacing spaces and removing backticks.
     *
     * @param name the original method name
     * @return the sanitized method name
     */
    private fun sanitizeMethodName(name: String): String = name.replace("`", "").replace(" ", "\$")

    /**
     * Finds the effective [Preparation] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the preparation annotation if found, or null
     */
    private fun findPreparation(
        testCase: TestCase,
        method: Method,
    ): Preparation? =
        method.getAnnotation(Preparation::class.java)
            ?: testCase.spec::class.findAnnotation<Preparation>()
            ?: testCase.spec::class.java.getAnnotation(Preparation::class.java)

    /**
     * Finds the effective [Expectation] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the expectation annotation if found, or null
     */
    private fun findExpectation(
        testCase: TestCase,
        method: Method,
    ): Expectation? =
        method.getAnnotation(Expectation::class.java)
            ?: testCase.spec::class.findAnnotation<Expectation>()
            ?: testCase.spec::class.java.getAnnotation(Expectation::class.java)
}
