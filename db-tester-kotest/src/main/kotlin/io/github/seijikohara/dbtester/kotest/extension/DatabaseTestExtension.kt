package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExpectationVerifier
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestPreparationExecutor
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

/**
 * Kotest extension for database testing.
 *
 * This extension processes [DataSet] and [ExpectedDataSet] annotations to set up
 * test data before each test and verify database state after each test.
 *
 * The extension performs two responsibilities:
 * 1. Before each test, resolves [DataSet] declarations and executes the resulting datasets.
 * 2. After each test, resolves [ExpectedDataSet] declarations and validates the database contents.
 *
 * **Usage with `@DatabaseTest` annotation and `DatabaseTestSupport` interface (recommended):**
 * ```kotlin
 * @DatabaseTest
 * class MyTest : AnnotationSpec(), DatabaseTestSupport {
 *
 *     override val dbTesterRegistry = DataSourceRegistry()
 *
 *     @BeforeAll
 *     fun setup() {
 *         dbTesterRegistry.registerDefault(createDataSource())
 *     }
 *
 *     @Test
 *     @DataSet
 *     @ExpectedDataSet
 *     fun `should verify database state`() {
 *         // test implementation
 *     }
 * }
 * ```
 *
 * **Usage with explicit extension registration:**
 * ```kotlin
 * class MyTest : AnnotationSpec() {
 *     private val registry = DataSourceRegistry()
 *
 *     init {
 *         extensions(DatabaseTestExtension(registryProvider = { registry }))
 *     }
 *
 *     @Test
 *     @DataSet
 *     @ExpectedDataSet
 *     fun `should verify database state`() {
 *         // test implementation
 *     }
 * }
 * ```
 *
 * When using the `@DatabaseTest` annotation, the spec class must implement
 * [DatabaseTestSupport] to provide the registry and optional configuration.
 *
 * @property registryProvider optional provider function that returns the [DataSourceRegistry].
 *     When null, the extension requires the spec to implement [DatabaseTestSupport].
 * @property configurationProvider optional provider function for custom [Configuration].
 *     When null, the extension uses [DatabaseTestSupport.dbTesterConfiguration] if available,
 *     falling back to [Configuration.defaults] if not found.
 * @see io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
 * @see DatabaseTestSupport
 * @see DataSet
 * @see ExpectedDataSet
 */
class DatabaseTestExtension(
    private val registryProvider: (() -> DataSourceRegistry)? = null,
    private val configurationProvider: (() -> Configuration)? = null,
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
                findDataSet(testCase, method) to findExpectedDataSet(testCase, method)
            }.let { (dataSet, expectedDataSet) ->
                when {
                    dataSet != null || expectedDataSet != null ->
                        executeWithAnnotations(testCase, execute, dataSet, expectedDataSet)
                    else -> execute(testCase)
                }
            }

    /**
     * Executes the test case with dataset and/or expected dataset handling.
     *
     * @param testCase the test case being executed
     * @param execute the function to execute the test case
     * @param dataSet the DataSet annotation, or null
     * @param expectedDataSet the ExpectedDataSet annotation, or null
     * @return the test result
     */
    private suspend fun executeWithAnnotations(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult,
        dataSet: DataSet?,
        expectedDataSet: ExpectedDataSet?,
    ): TestResult =
        createTestContext(testCase, requireMethod(testCase)).let { testContext ->
            dataSet?.also {
                logger.debug(
                    "Executing preparation for {}.{}()",
                    testContext.testClass().simpleName,
                    testContext.testMethod().name,
                )
                preparationExecutor.execute(testContext, it)
            }
            execute(testCase).also { result ->
                if (result is TestResult.Success && expectedDataSet != null) {
                    logger.debug(
                        "Verifying expectation for {}.{}()",
                        testContext.testClass().simpleName,
                        testContext.testMethod().name,
                    )
                    expectationVerifier.verify(testContext, expectedDataSet)
                }
            }
        }

    /**
     * Creates a [TestContext] from the Kotest [TestCase].
     *
     * Resolves the [DataSourceRegistry] and [Configuration] using the following priority:
     * 1. Explicit provider functions passed to the constructor
     * 2. [DatabaseTestSupport] interface implementation
     * 3. Default values (empty registry causes error, default configuration)
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
            resolveConfiguration(testCase.spec),
            resolveRegistry(testCase.spec),
        )

    /**
     * Resolves the [DataSourceRegistry] for the test.
     *
     * Resolution order:
     * 1. Explicit provider function (if provided)
     * 2. [DatabaseTestSupport.dbTesterRegistry] property
     * 3. Error (spec must implement DatabaseTestSupport)
     *
     * @param spec the spec instance
     * @return the resolved registry
     * @throws IllegalStateException if no registry is available
     */
    private fun resolveRegistry(spec: Spec): DataSourceRegistry =
        registryProvider?.invoke()
            ?: (spec as? DatabaseTestSupport)?.dbTesterRegistry
            ?: throw IllegalStateException(
                "Spec class '${spec::class.simpleName}' must implement DatabaseTestSupport " +
                    "to provide dbTesterRegistry, or use explicit registryProvider in " +
                    "DatabaseTestExtension constructor.",
            )

    /**
     * Resolves the [Configuration] for the test.
     *
     * Resolution order:
     * 1. Explicit provider function (if provided)
     * 2. [DatabaseTestSupport.dbTesterConfiguration] property
     * 3. Default configuration via [Configuration.defaults]
     *
     * @param spec the spec instance
     * @return the resolved configuration
     */
    private fun resolveConfiguration(spec: Spec): Configuration =
        configurationProvider?.invoke()
            ?: (spec as? DatabaseTestSupport)?.dbTesterConfiguration
            ?: Configuration.defaults()

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
     * Finds the effective [DataSet] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the DataSet annotation if found, or null
     */
    private fun findDataSet(
        testCase: TestCase,
        method: Method,
    ): DataSet? =
        method.getAnnotation(DataSet::class.java)
            ?: testCase.spec::class.findAnnotation<DataSet>()
            ?: testCase.spec::class.java.getAnnotation(DataSet::class.java)

    /**
     * Finds the effective [ExpectedDataSet] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the ExpectedDataSet annotation if found, or null
     */
    private fun findExpectedDataSet(
        testCase: TestCase,
        method: Method,
    ): ExpectedDataSet? =
        method.getAnnotation(ExpectedDataSet::class.java)
            ?: testCase.spec::class.findAnnotation<ExpectedDataSet>()
            ?: testCase.spec::class.java.getAnnotation(ExpectedDataSet::class.java)
}
