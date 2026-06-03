package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.kotest.core.extensions.ApplyExtension
import java.lang.annotation.Inherited

/**
 * Enables Spring Boot-integrated database testing for a Kotest spec class.
 *
 * Apply this annotation to a Spring Boot test spec to process
 * [DataSet][io.github.seijikohara.dbtester.api.annotation.DataSet] and
 * [ExpectedDataSet][io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet] annotations, with
 * automatic DataSource discovery from the Spring ApplicationContext.
 *
 * **Usage:**
 * ```kotlin
 * @SpringBootTest
 * @SpringBootDatabaseTest
 * class UserRepositorySpec : AnnotationSpec() {
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
 * Unlike [DatabaseTest][io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest], this
 * annotation discovers and registers DataSources from the Spring context, so no manual
 * [DataSourceRegistry][io.github.seijikohara.dbtester.api.config.DataSourceRegistry] configuration is
 * required.
 *
 * @see SpringBootDatabaseTestExtension
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ApplyExtension(SpringBootDatabaseTestExtension::class)
public annotation class SpringBootDatabaseTest
