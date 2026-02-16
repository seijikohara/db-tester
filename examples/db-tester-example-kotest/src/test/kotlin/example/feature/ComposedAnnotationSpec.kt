package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Composed annotation that wraps [DataSet] to load user seed data.
 *
 * Demonstrates a meta-annotation that encapsulates a specific `@DataSet` resource
 * location, reducing repetition across tests that share the same preparation data.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataSet(
    sources = [DataSetSource(
        resourceLocation = "classpath:example/feature/ComposedAnnotationSpec/user-seed/")])
annotation class UserSeedData

/**
 * Composed annotation that wraps [ExpectedDataSet] to exclude audit columns.
 *
 * Demonstrates a meta-annotation that encapsulates a specific `@ExpectedDataSet`
 * configuration with column exclusions, allowing tests to verify data without matching
 * audit timestamps.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExpectedDataSet(
    sources = [DataSetSource(
        excludeColumns = ["CREATED_AT", "UPDATED_AT"],
        resourceLocation = "classpath:example/feature/ComposedAnnotationSpec/verify-users/expected/")])
annotation class VerifyIgnoringAuditColumns

/**
 * Deeply composed annotation combining [UserSeedData] and [VerifyIgnoringAuditColumns].
 *
 * Demonstrates two-level meta-annotation traversal: the framework discovers `@DataSet`
 * through `@UserDataTest` then `@UserSeedData` then `@DataSet`, and `@ExpectedDataSet`
 * through `@UserDataTest` then `@VerifyIgnoringAuditColumns` then `@ExpectedDataSet`.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@UserSeedData
@VerifyIgnoringAuditColumns
annotation class UserDataTest

/**
 * Demonstrates meta-annotation (composed annotation) support for [DataSet] and [ExpectedDataSet]
 * with Kotest.
 *
 * Meta-annotations allow encapsulating reusable `@DataSet` and `@ExpectedDataSet` configurations
 * into custom annotations. This reduces duplication when multiple test methods share the same
 * dataset locations or column exclusion settings.
 *
 * This specification covers three composition patterns:
 * - Composed `@DataSet`: a custom annotation wrapping `@DataSet` with a fixed resource location
 * - Composed `@ExpectedDataSet`: a custom annotation wrapping `@ExpectedDataSet` with column
 *   exclusions
 * - Two-level composition: a custom annotation that combines both composed annotations, requiring
 *   the framework to traverse two levels of meta-annotation hierarchy
 *
 * The framework's `AnnotationUtils` discovers these annotations through recursive meta-annotation
 * traversal with cycle detection.
 *
 * @see DataSet
 * @see ExpectedDataSet
 * @see UserSeedData
 * @see VerifyIgnoringAuditColumns
 * @see UserDataTest
 */
@DatabaseTest
class ComposedAnnotationSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(ComposedAnnotationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ComposedAnnotationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ComposedAnnotationSpec::class.java.classLoader.getResource(scriptPath)
                    ?: throw IllegalStateException("Script not found: $scriptPath")
            ).readText()
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .let { statements ->
                    dataSource.connection.use { connection ->
                        connection.createStatement().use { statement ->
                            statements.forEach { sql ->
                                runCatching { statement.execute(sql) }
                                    .onFailure { e ->
                                        throw RuntimeException("Failed to execute SQL: $sql", e as? SQLException ?: e)
                                    }
                            }
                        }
                    }
                }
    }

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for ComposedAnnotationSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ComposedAnnotationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies that the framework loads preparation data via composed `@DataSet` annotation.
     *
     * The [UserSeedData] meta-annotation carries `@DataSet` with a fixed resource location.
     * The framework traverses the annotation hierarchy to discover and apply it. A direct
     * `@ExpectedDataSet` verifies that the data was loaded correctly.
     */
    @Test
    @UserSeedData
    @ExpectedDataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ComposedAnnotationSpec/composed-dataset/expected/")])
    fun `should load data via composed DataSet annotation`(): Unit =
        logger.info("Running test with composed @DataSet annotation").also {
            // When - framework loads data via @UserSeedData meta-annotation
            // Then - framework verifies via direct @ExpectedDataSet
            logger.info("Composed @DataSet annotation test completed")
        }

    /**
     * Verifies that the framework applies column exclusions via composed `@ExpectedDataSet`
     * annotation.
     *
     * A direct `@DataSet` loads preparation data, and the [VerifyIgnoringAuditColumns]
     * meta-annotation carries `@ExpectedDataSet` with `CREATED_AT` and `UPDATED_AT` excluded
     * from comparison.
     */
    @Test
    @DataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ComposedAnnotationSpec/user-seed/")])
    @VerifyIgnoringAuditColumns
    fun `should exclude audit columns via composed ExpectedDataSet annotation`(): Unit =
        logger.info("Running test with composed @ExpectedDataSet annotation").also {
            // When - framework loads data via direct @DataSet
            // Then - framework verifies via @VerifyIgnoringAuditColumns meta-annotation
            logger.info("Composed @ExpectedDataSet annotation test completed")
        }

    /**
     * Verifies that the framework applies both preparation and expectation via a two-level
     * composed annotation.
     *
     * The [UserDataTest] meta-annotation carries [UserSeedData] and
     * [VerifyIgnoringAuditColumns], which in turn carry `@DataSet` and `@ExpectedDataSet`.
     * The framework traverses two levels of meta-annotation hierarchy to discover and apply
     * both annotations.
     */
    @Test
    @UserDataTest
    fun `should apply both annotations via deeply composed annotation`(): Unit =
        logger.info("Running test with deeply composed annotation").also {
            // When - framework loads data via @UserDataTest -> @UserSeedData -> @DataSet
            // Then - framework verifies via @UserDataTest -> @VerifyIgnoringAuditColumns ->
            //        @ExpectedDataSet
            logger.info("Deeply composed annotation test completed")
        }
}
