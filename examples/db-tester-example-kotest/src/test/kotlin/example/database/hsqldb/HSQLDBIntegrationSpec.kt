package example.database.hsqldb

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.hsqldb.jdbc.JDBCDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * HSQLDB integration test using in-memory database.
 *
 * This test validates that the framework works correctly with HSQLDB (HyperSQL Database).
 * This is a smoke test to ensure HSQLDB compatibility with Kotest.
 */
class HSQLDBIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(HSQLDBIntegrationSpec::class.java)

        private fun createDataSource(): DataSource =
            JDBCDataSource().apply {
                setURL("jdbc:hsqldb:mem:HSQLDBIntegrationSpec")
                user = "sa"
                setPassword("")
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                HSQLDBIntegrationSpec::class.java.classLoader.getResource(scriptPath)
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

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up HSQLDB in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up HSQLDB in-memory database").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/hsqldb/hsqldb-integration.sql")
            logger.info("HSQLDB database setup completed")
        }

    /**
     * Smoke test verifying basic framework functionality with HSQLDB.
     *
     * This test validates:
     * - Data can be loaded from CSV into HSQLDB
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on HSQLDB`(): Unit = logger.info("Running HSQLDB integration smoke test").let { }
}
