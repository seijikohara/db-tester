package example.database.oracle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.AnnotationSpec
import oracle.jdbc.pool.OracleDataSource
import org.slf4j.LoggerFactory
import org.testcontainers.oracle.OracleContainer
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Condition to check if the test is not running in CI environment.
 */
class NotInCiCondition : io.kotest.core.annotation.EnabledCondition {
    override fun evaluate(kclass: kotlin.reflect.KClass<out io.kotest.core.spec.Spec>): Boolean = System.getenv("CI") != "true"
}

/**
 * Oracle Database integration test using Testcontainers.
 *
 * This specification validates that the framework works correctly with Oracle database
 * using Testcontainers. This is a smoke test to ensure Oracle compatibility.
 *
 * The container is manually started in [setupDatabase] and stopped in [cleanupDatabase]
 * since Kotest does not support the JUnit-specific @Testcontainers/@Container annotations.
 *
 * This test is skipped in CI environments because Oracle containers require extended
 * startup time that often exceeds CI timeout limits.
 *
 * @see <a href="https://java.testcontainers.org/modules/databases/oraclefree/">Testcontainers Oracle Module</a>
 */
@EnabledIf(NotInCiCondition::class)
class OracleIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(OracleIntegrationSpec::class.java)

        private val oracle: OracleContainer =
            OracleContainer("gvenzl/oracle-free:latest")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")

        /**
         * Creates an Oracle DataSource from the Testcontainer.
         *
         * @param container the Oracle container
         * @return configured DataSource
         */
        private fun createDataSource(container: OracleContainer): DataSource =
            OracleDataSource().apply {
                url = container.jdbcUrl
                user = container.username
                setPassword(container.password)
            }

        /**
         * Executes a SQL script from classpath.
         *
         * Handles Oracle-specific error codes for DROP statements.
         *
         * @param dataSource the DataSource to use
         * @param scriptPath the classpath resource path
         */
        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                OracleIntegrationSpec::class.java.classLoader.getResource(scriptPath)
                    ?: throw IllegalStateException("Script not found: $scriptPath")
            ).readText()
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("--") }
                .let { statements ->
                    dataSource.connection.use { connection ->
                        connection.createStatement().use { statement ->
                            statements.forEach { sql ->
                                runCatching { statement.execute(sql) }
                                    .onFailure { e ->
                                        // Ignore ORA-00942 (table or view does not exist) for DROP statements
                                        val sqlException = e as? SQLException
                                        val isDropTableNotExists =
                                            sqlException?.errorCode == 942 && sql.uppercase().contains("DROP TABLE")
                                        if (isDropTableNotExists) {
                                            logger.debug("Ignoring error for DROP statement: {}", e.message)
                                        } else {
                                            throw RuntimeException("Failed to execute SQL: $sql", e)
                                        }
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
     * Sets up Oracle database connection and schema using Testcontainers.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up Oracle Testcontainer").also {
            oracle.start()
            val dataSource = createDataSource(oracle)
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/database/oracle/oracle-integration.sql")
            logger.info("Oracle database setup completed")
        }

    /**
     * Cleans up database resources after all tests complete.
     */
    @AfterAll
    fun cleanupDatabase(): Unit = oracle.stop()

    /**
     * Smoke test verifying basic framework functionality with Oracle Database.
     *
     * This test validates:
     * - Data can be loaded from CSV into Oracle
     * - Data can be verified against expected CSV
     * - Basic CRUD operations work correctly
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on Oracle`(): Unit = logger.info("Running Oracle integration smoke test").let { }
}
