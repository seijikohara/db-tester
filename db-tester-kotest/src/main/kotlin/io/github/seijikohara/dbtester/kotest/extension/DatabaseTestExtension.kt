package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.annotation.AnnotationUtils
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExpectationVerifier
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExportExecutor
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestPreparationExecutor
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
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
public class DatabaseTestExtension public constructor(
    private val registryProvider: (() -> DataSourceRegistry)? = null,
    private val configurationProvider: (() -> Configuration)? = null,
) : TestCaseExtension {
    // All primary-constructor parameters carry defaults so Kotlin synthesizes a
    // no-argument constructor. Kotest instantiates this extension reflectively
    // through @DatabaseTest / @ApplyExtension, which requires that no-arg constructor.

    /** Executor for the preparation phase. */
    private var preparationExecutor: KotestPreparationExecutor = KotestPreparationExecutor()

    /** Verifier for the expectation phase. */
    private var expectationVerifier: KotestExpectationVerifier = KotestExpectationVerifier()

    /** Executor for the export phase. */
    private var exportExecutor: KotestExportExecutor = KotestExportExecutor()

    /**
     * Creates an extension with injected lifecycle collaborators.
     *
     * <p>This constructor exists to supply test doubles for the preparation, verification, and
     * export phases. Production code uses the primary constructor.
     *
     * @param registryProvider optional provider for the [DataSourceRegistry], or null
     * @param configurationProvider optional provider for the [Configuration], or null
     * @param preparationExecutor the preparation executor
     * @param expectationVerifier the expectation verifier
     * @param exportExecutor the export executor
     */
    internal constructor(
        registryProvider: (() -> DataSourceRegistry)?,
        configurationProvider: (() -> Configuration)?,
        preparationExecutor: KotestPreparationExecutor,
        expectationVerifier: KotestExpectationVerifier,
        exportExecutor: KotestExportExecutor,
    ) : this(registryProvider, configurationProvider) {
        this.preparationExecutor = preparationExecutor
        this.expectationVerifier = expectationVerifier
        this.exportExecutor = exportExecutor
    }

    /** Companion object containing class-level constants and logger. */
    private companion object {
        private val logger = LoggerFactory.getLogger(DatabaseTestExtension::class.java)
    }

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
        requireMethod(testCase).let { method ->
            val dataSet = findDataSet(testCase, method)
            val expectedDataSet = findExpectedDataSet(testCase, method)
            val exportDataSet = findExportDataSet(testCase, method)
            when {
                dataSet != null || expectedDataSet != null || exportDataSet != null -> {
                    executeWithAnnotations(testCase, method, execute, dataSet, expectedDataSet, exportDataSet)
                }

                else -> {
                    execute(testCase)
                }
            }
        }

    /**
     * Executes the test case with dataset, expected dataset, and/or export dataset handling.
     *
     * @param testCase the test case being executed
     * @param method the resolved test method
     * @param execute the function to execute the test case
     * @param dataSet the DataSet annotation, or null
     * @param expectedDataSet the ExpectedDataSet annotation, or null
     * @param exportDataSet the ExportDataSet annotation, or null
     * @return the test result
     */
    private suspend fun executeWithAnnotations(
        testCase: TestCase,
        method: Method,
        execute: suspend (TestCase) -> TestResult,
        dataSet: DataSet?,
        expectedDataSet: ExpectedDataSet?,
        exportDataSet: ExportDataSet?,
    ): TestResult =
        createTestContext(testCase, method).let { testContext ->
            dataSet?.also {
                logger.debug(
                    "Executing preparation for {}.{}()",
                    testContext.testClass().simpleName,
                    testContext.testMethod().name,
                )
                preparationExecutor.execute(testContext, it)
            }
            execute(testCase).also { result ->
                var testFailed = result !is TestResult.Success
                try {
                    if (result is TestResult.Success && expectedDataSet != null) {
                        logger.debug(
                            "Verifying expectation for {}.{}()",
                            testContext.testClass().simpleName,
                            testContext.testMethod().name,
                        )
                        expectationVerifier.verify(testContext, expectedDataSet)
                    }
                } catch (e: Throwable) {
                    testFailed = true
                    throw e
                } finally {
                    handleExportDataSet(testContext, exportDataSet, testFailed)
                }
            }
        }

    /**
     * Handles [ExportDataSet] execution in a finally-equivalent block.
     *
     * Export errors are caught and logged to prevent masking test or verification failures.
     *
     * @param testContext the test context
     * @param exportDataSet the ExportDataSet annotation, or null
     * @param testFailed whether the test execution or verification failed
     */
    private fun handleExportDataSet(
        testContext: TestContext,
        exportDataSet: ExportDataSet?,
        testFailed: Boolean,
    ) {
        if (exportDataSet == null) return
        if (exportDataSet.onFailureOnly && !testFailed) {
            logger.debug(
                "Skipping @ExportDataSet for {}.{}() because the test passed and onFailureOnly=true",
                testContext.testClass().simpleName,
                testContext.testMethod().name,
            )
            return
        }
        try {
            exportExecutor.export(testContext, exportDataSet)
        } catch (e: Exception) {
            logger.error(
                "Failed to export dataset for {}.{}(): {}",
                testContext.testClass().simpleName,
                testContext.testMethod().name,
                e.message,
                e,
            )
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
     * Kotlin preserves backtick-escaped method names verbatim at the JVM level
     * (spaces and all), so the test case name maps directly to the reflected
     * method name.
     *
     * @param testCase the test case
     * @return the resolved method
     * @throws IllegalStateException if method resolution fails
     */
    private fun requireMethod(testCase: TestCase): Method =
        testCase.spec::class.let { specClass ->
            specClass.members
                .firstOrNull { it.name == testCase.name.name }
                ?.let { member -> (member as? kotlin.reflect.KFunction<*>)?.javaMethod }
                ?: specClass.java.declaredMethods.firstOrNull { method ->
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
     * Finds the effective [DataSet] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     * Supports meta-annotation discovery through [AnnotationUtils].
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the DataSet annotation if found, or null
     */
    private fun findDataSet(
        testCase: TestCase,
        method: Method,
    ): DataSet? =
        AnnotationUtils
            .findAnnotation(DataSet::class.java, method, testCase.spec::class.java)
            .orElse(null)

    /**
     * Finds the effective [ExpectedDataSet] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     * Supports meta-annotation discovery through [AnnotationUtils].
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the ExpectedDataSet annotation if found, or null
     */
    private fun findExpectedDataSet(
        testCase: TestCase,
        method: Method,
    ): ExpectedDataSet? =
        AnnotationUtils
            .findAnnotation(ExpectedDataSet::class.java, method, testCase.spec::class.java)
            .orElse(null)

    /**
     * Finds the effective [ExportDataSet] annotation for the current test.
     *
     * Method-level annotations take precedence over class-level annotations.
     * Supports meta-annotation discovery through [AnnotationUtils].
     *
     * @param testCase the test case
     * @param method the resolved test method
     * @return the ExportDataSet annotation if found, or null
     */
    private fun findExportDataSet(
        testCase: TestCase,
        method: Method,
    ): ExportDataSet? =
        AnnotationUtils
            .findAnnotation(ExportDataSet::class.java, method, testCase.spec::class.java)
            .orElse(null)
}
