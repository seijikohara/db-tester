package example.database.mysql

import com.mysql.cj.jdbc.MysqlDataSource
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MySQLContainer
import java.sql.SQLException
import javax.sql.DataSource

/**
 * MySQL integration test using Testcontainers.
 *
 * This specification validates that the framework works correctly with MySQL database
 * using Testcontainers. This is a smoke test to ensure MySQL compatibility.
 *
 * The container is manually started in [setupDatabase] and stopped in [cleanupDatabase]
 * since Kotest does not support the JUnit-specific @Testcontainers/@Container annotations.
 *
 * @see <a href="https://java.testcontainers.org/modules/databases/mysql/">Testcontainers MySQL</a>
 */
class MySQLIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(MySQLIntegrationSpec::class.java)

        private val mysql: MySQLContainer<*> =
            MySQLContainer("mysql:latest")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")

        /**
         * Creates a MySQL DataSource from the Testcontainer.
         *
         * @param container the MySQL container
         * @return configured DataSource
         */
        private fun createDataSource(container: MySQLContainer<*>): DataSource =
            MysqlDataSource().apply {
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
                MySQLIntegrationSpec::class.java.classLoader.getResource(scriptPath)
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
     * Sets up MySQL database connection and schema using Testcontainers.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up MySQL Testcontainer").also {
            mysql.start()
            val dataSource = createDataSource(mysql)
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/mysql/mysql-integration.sql")
            logger.info("MySQL database setup completed")
        }

    /**
     * Cleans up database resources after all tests complete.
     */
    @AfterAll
    fun cleanupDatabase(): Unit = mysql.stop()

    /**
     * Smoke test verifying basic framework functionality with MySQL.
     *
     * This test validates:
     * - Data can be loaded from CSV into MySQL
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on MySQL`(): Unit = logger.info("Running MySQL integration smoke test").let { }
}
