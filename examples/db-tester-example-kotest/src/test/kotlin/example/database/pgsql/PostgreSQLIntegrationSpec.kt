package example.database.pgsql

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.SQLException
import javax.sql.DataSource

/**
 * PostgreSQL integration test using Testcontainers.
 *
 * This specification validates that the framework works correctly with PostgreSQL database
 * using Testcontainers. This is a smoke test to ensure PostgreSQL compatibility.
 *
 * The container is manually started in [setupDatabase] and stopped in [cleanupDatabase]
 * since Kotest does not support the JUnit-specific @Testcontainers/@Container annotations.
 *
 * @see <a href="https://java.testcontainers.org/modules/databases/postgres/">Testcontainers PostgreSQL</a>
 */
class PostgreSQLIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(PostgreSQLIntegrationSpec::class.java)

        @Suppress("DEPRECATION")
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:latest")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")

        /**
         * Creates a PostgreSQL DataSource from the Testcontainer.
         *
         * @param container the PostgreSQL container
         * @return configured DataSource
         */
        @Suppress("DEPRECATION")
        private fun createDataSource(container: PostgreSQLContainer<*>): DataSource =
            PGSimpleDataSource().apply {
                setURL(container.jdbcUrl)
                user = container.username
                password = container.password
            }

        /**
         * Executes a SQL script from classpath.
         *
         * @param dataSource the DataSource to execute against
         * @param scriptPath the classpath resource path
         */
        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                PostgreSQLIntegrationSpec::class.java.classLoader.getResource(scriptPath)
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

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up PostgreSQL database connection and schema using Testcontainers.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up PostgreSQL Testcontainer").also {
            postgres.start()
            val dataSource = createDataSource(postgres)
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/pgsql/pgsql-integration.sql")
            logger.info("PostgreSQL database setup completed")
        }

    /**
     * Cleans up database resources after all tests complete.
     */
    @AfterAll
    fun cleanupDatabase(): Unit = postgres.stop()

    /**
     * Smoke test verifying basic framework functionality with PostgreSQL.
     *
     * This test validates:
     * - Data can be loaded from CSV into PostgreSQL
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on PostgreSQL`(): Unit = logger.info("Running PostgreSQL integration smoke test").let { }
}
