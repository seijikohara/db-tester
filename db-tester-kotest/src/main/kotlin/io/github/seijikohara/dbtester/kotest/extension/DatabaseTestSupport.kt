package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry

/**
 * Interface for Kotest specs that require database testing support.
 *
 * Implement this interface in your spec class to provide the [DataSourceRegistry]
 * and optionally a custom [Configuration] for database testing.
 *
 * **Usage:**
 * ```kotlin
 * @DatabaseTest
 * class MySpec : AnnotationSpec(), DatabaseTestSupport {
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
 * **With custom configuration:**
 * ```kotlin
 * @DatabaseTest
 * class MySpec : AnnotationSpec(), DatabaseTestSupport {
 *
 *     override val dbTesterRegistry = DataSourceRegistry()
 *
 *     override val dbTesterConfiguration: Configuration = Configuration.builder()
 *         .conventions(ConventionSettings.builder()
 *             .dataFormat(DataFormat.TSV)
 *             .build())
 *         .build()
 * }
 * ```
 *
 * @see io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
 * @see DatabaseTestExtension
 */
interface DatabaseTestSupport {
    /**
     * The data source registry for database connections.
     *
     * This property must be implemented by the spec class. Register your
     * [javax.sql.DataSource] instances with this registry before tests run.
     */
    val dbTesterRegistry: DataSourceRegistry

    /**
     * Optional custom configuration for database testing.
     *
     * Override this property to provide custom settings such as data format,
     * scenario marker, or operation defaults. When not overridden, returns
     * the default configuration.
     */
    val dbTesterConfiguration: Configuration
        get() = Configuration.defaults()
}
