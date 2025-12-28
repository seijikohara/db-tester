package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.extensions.SpecExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestContextManager

/**
 * Spring Boot-aware database test extension for Kotest that automatically registers DataSources.
 *
 * This extension wraps [DatabaseTestExtension] and adds automatic DataSource registration
 * from the Spring [ApplicationContext]. When the `db-tester.auto-register-data-sources`
 * property is set to `true` (the default), all Spring-managed [javax.sql.DataSource] beans
 * are automatically registered with the [DataSourceRegistry] before any test execution.
 *
 * **Usage:**
 * ```kotlin
 * @SpringBootTest
 * class MyTest : AnnotationSpec() {
 *
 *     init {
 *         extensions(SpringBootDatabaseTestExtension())
 *     }
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
 * @see DatabaseTestExtension
 * @see DataSourceRegistrar
 * @see DbTesterKotestAutoConfiguration
 */
class SpringBootDatabaseTestExtension :
    SpecExtension,
    TestCaseExtension {
    /** Companion object containing class-level logger. */
    companion object {
        private val logger = LoggerFactory.getLogger(SpringBootDatabaseTestExtension::class.java)
    }

    private var registry: DataSourceRegistry? = null
    private var configuration: Configuration? = null
    private var delegate: DatabaseTestExtension? = null
    private var applicationContext: ApplicationContext? = null

    /**
     * Called before the spec is instantiated.
     *
     * Automatically registers Spring-managed DataSources and Configuration.
     *
     * @param spec the spec instance
     */
    override suspend fun intercept(
        spec: Spec,
        execute: suspend (Spec) -> Unit,
    ): Unit = initializeFromSpec(spec).let { execute(spec) }

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
        (delegate ?: initializeFromSpec(testCase.spec).let { delegate!! })
            .intercept(testCase, execute)

    /**
     * Initializes the extension from the Spec instance.
     *
     * @param spec the spec instance
     */
    private fun initializeFromSpec(spec: Spec): Unit =
        when (delegate) {
            null ->
                getApplicationContext(spec).also { applicationContext = it }.let { context ->
                    registerConfigurationFromContext(context)
                    registerDataSourcesFromContext(context)
                    delegate =
                        DatabaseTestExtension(
                            registryProvider = { registry!! },
                            configurationProvider = { configuration ?: Configuration.defaults() },
                        )
                }
            else -> Unit
        }

    /**
     * Retrieves the ApplicationContext from the Spec using Spring's TestContextManager.
     *
     * @param spec the spec instance
     * @return the application context
     */
    private fun getApplicationContext(spec: Spec): ApplicationContext =
        TestContextManager(spec::class.java)
            .also { it.prepareTestInstance(spec) }
            .testContext
            .applicationContext

    /**
     * Registers Configuration from the Spring ApplicationContext.
     *
     * @param applicationContext the application context
     */
    private fun registerConfigurationFromContext(applicationContext: ApplicationContext): Unit =
        when {
            applicationContext.containsBean("dbTesterConfiguration") -> {
                configuration = applicationContext.getBean("dbTesterConfiguration", Configuration::class.java)
                logger.debug("Registered Spring-managed Configuration with database testing framework")
            }
            else -> logger.debug("Configuration bean not found in ApplicationContext, using default configuration")
        }

    /**
     * Registers DataSources from the Spring ApplicationContext.
     *
     * @param applicationContext the application context
     */
    private fun registerDataSourcesFromContext(applicationContext: ApplicationContext): Unit =
        when {
            applicationContext.containsBean("dbTesterDataSourceRegistry") -> {
                registry = applicationContext.getBean("dbTesterDataSourceRegistry", DataSourceRegistry::class.java)
                logger.debug("Using Spring-managed DataSourceRegistry")
            }
            applicationContext.containsBean("dataSourceRegistrar") -> {
                registry = DataSourceRegistry()
                logger.info("Automatically registering Spring DataSources with database testing framework")
                applicationContext.getBean(DataSourceRegistrar::class.java).registerAll(registry!!)
                logger.info("Automatic DataSource registration completed")
            }
            else -> {
                registry = DataSourceRegistry()
                logger.debug("DataSourceRegistrar bean not found, using empty registry")
            }
        }
}
