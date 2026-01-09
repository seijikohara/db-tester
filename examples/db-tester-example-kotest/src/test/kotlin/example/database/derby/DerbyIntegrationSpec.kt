package example.database.derby

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.apache.derby.jdbc.EmbeddedDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Apache Derby integration test using in-memory database.
 *
 * This test validates that the framework works correctly with Apache Derby database.
 * This is a smoke test to ensure Derby compatibility with Kotest.
 */
class DerbyIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(DerbyIntegrationSpec::class.java)

        private fun createDataSource(): DataSource =
            EmbeddedDataSource().apply {
                databaseName = "memory:DerbyIntegrationSpec"
                createDatabase = "create"
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                DerbyIntegrationSpec::class.java.classLoader.getResource(scriptPath)
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
     * Sets up Derby in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up Derby in-memory database").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/derby/derby-integration.sql")
            logger.info("Derby database setup completed")
        }

    /**
     * Smoke test verifying basic framework functionality with Derby.
     *
     * This test validates:
     * - Data can be loaded from CSV into Derby
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on Derby`(): Unit = logger.info("Running Derby integration smoke test").let { }
}
