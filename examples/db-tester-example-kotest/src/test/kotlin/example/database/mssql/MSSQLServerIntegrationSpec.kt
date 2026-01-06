package example.database.mssql

import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MSSQLServerContainer
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Microsoft SQL Server integration test using Testcontainers.
 *
 * This specification validates that the framework works correctly with SQL Server database
 * using Testcontainers. This is a smoke test to ensure SQL Server compatibility.
 *
 * The container is manually started in [setupDatabase] and stopped in [cleanupDatabase]
 * since Kotest does not support the JUnit-specific @Testcontainers/@Container annotations.
 *
 * @see <a href="https://java.testcontainers.org/modules/databases/mssqlserver/">Testcontainers MSSQL</a>
 */
class MSSQLServerIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(MSSQLServerIntegrationSpec::class.java)

        private val mssql: MSSQLServerContainer<*> =
            MSSQLServerContainer("mcr.microsoft.com/mssql/server:latest")
                .acceptLicense()
                .withPassword("StrongPassword123!")

        /**
         * Creates a SQL Server DataSource from the Testcontainer.
         *
         * @param container the SQL Server container
         * @return configured DataSource
         */
        private fun createDataSource(container: MSSQLServerContainer<*>): DataSource =
            SQLServerDataSource().apply {
                url = container.jdbcUrl
                user = container.username
                setPassword(container.password)
            }

        /**
         * Executes a SQL script from classpath.
         *
         * SQL Server uses 'GO' as batch separator, but for this integration test
         * we use semicolons as statement separators.
         *
         * @param dataSource the data source to execute against
         * @param scriptPath the classpath resource path
         */
        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                MSSQLServerIntegrationSpec::class.java.classLoader.getResource(scriptPath)
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
     * Sets up SQL Server database connection and schema using Testcontainers.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up SQL Server Testcontainer").also {
            mssql.start()
            val dataSource = createDataSource(mssql)
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/mssql/mssql-integration.sql")
            logger.info("SQL Server database setup completed")
        }

    /**
     * Cleans up database resources after all tests complete.
     */
    @AfterAll
    fun cleanupDatabase(): Unit = mssql.stop()

    /**
     * Smoke test verifying basic framework functionality with SQL Server.
     *
     * This test validates:
     * - Data can be loaded from CSV into SQL Server
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(dataSets = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on SQL Server`(): Unit = logger.info("Running SQL Server integration smoke test").let { }
}
