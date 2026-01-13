package io.github.seijikohara.dbtester.kotest.annotation

import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.extensions.ApplyExtension

/**
 * Annotation that enables database testing for a Kotest spec class.
 *
 * When applied to a spec class, this annotation automatically registers
 * the [DatabaseTestExtension] for handling [DataSet][io.github.seijikohara.dbtester.api.annotation.DataSet]
 * and [ExpectedDataSet][io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet] annotations.
 *
 * **Important:** The spec class must implement [DatabaseTestSupport] to provide
 * the [DataSourceRegistry][io.github.seijikohara.dbtester.api.config.DataSourceRegistry].
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
 *     override val dbTesterConfiguration = Configuration.builder()
 *         .conventions(ConventionSettings.builder()
 *             .dataFormat(DataFormat.TSV)
 *             .build())
 *         .build()
 *
 *     @Test
 *     @DataSet
 *     fun `should load TSV data`() {
 *         // test implementation
 *     }
 * }
 * ```
 *
 * @see DatabaseTestExtension
 * @see DatabaseTestSupport
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ApplyExtension(DatabaseTestExtension::class)
annotation class DatabaseTest
