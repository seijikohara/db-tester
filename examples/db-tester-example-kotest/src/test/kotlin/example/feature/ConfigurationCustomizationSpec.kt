package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates customization of framework convention settings using Kotest.
 *
 * This specification demonstrates using custom [Configuration] to customize framework's
 * convention settings while keeping default operations.
 *
 * Customizations demonstrated:
 * - Custom scenario marker column name: `[TestCase]` instead of `[Scenario]`
 * - Custom expectation directory suffix: `/verify` instead of `/expected`
 *
 * Default convention settings:
 * - Scenario marker: `[Scenario]`
 * - Expectation suffix: `/expected`
 *
 * This test class uses custom conventions while keeping default database operations
 * (CLEAN_INSERT for preparation, NONE for expectation).
 */
class ConfigurationCustomizationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ConfigurationCustomizationSpec::class.java)

        /**
         * Static configuration with custom conventions.
         * Using companion object ensures the configuration is available before any test execution,
         * including interceptor invocation.
         */
        private val sharedConfiguration: Configuration =
            Configuration.withConventions(
                ConventionSettings(
                    null, // use classpath-relative resolution
                    "/verify", // custom expectation suffix
                    "[TestCase]", // custom scenario marker
                    DataFormat.CSV, // use CSV format (default)
                    TableMergeStrategy.UNION_ALL, // use UNION_ALL merge strategy (default)
                    ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                ),
            )

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ConfigurationCustomizationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ConfigurationCustomizationSpec::class.java.classLoader.getResource(scriptPath)
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

        private fun executeSql(
            dataSource: DataSource,
            sql: String,
        ): Unit =
            dataSource.connection
                .use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeUpdate(sql)
                    }
                }.let { }
    }

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    init {
        extensions(
            DatabaseTestExtension(
                registryProvider = { registry },
                configurationProvider = { sharedConfiguration },
            ),
        )
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for ConfigurationCustomizationSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ConfigurationCustomizationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates custom scenario marker usage.
     *
     * CSV files use `[TestCase]` column instead of default `[Scenario]` to filter rows
     * by test method name.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
     * - Execution: Inserts ID=2 (Bob, ACTIVE, 2024-01-15)
     * - Expectation: Verifies both records exist with correct values
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use custom scenario marker`(): Unit =
        logger.info("Running custom scenario marker test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (2, 'Bob', 'ACTIVE', '2024-01-15')",
            )
            logger.info("Record inserted successfully")
        }

    /**
     * Demonstrates custom expectation suffix usage.
     *
     * Expected data is loaded from `/verify` directory instead of default `/expected`.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
     * - Execution: Updates ID=1 status from ACTIVE to SUSPENDED
     * - Expectation: Verifies status change from `verify/TABLE1.csv`
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use custom expectation suffix`(): Unit =
        logger.info("Running custom expectation suffix test").also {
            executeSql(dataSource, "UPDATE TABLE1 SET COLUMN2 = 'SUSPENDED' WHERE ID = 1")
            logger.info("Record status updated successfully")
        }

    /**
     * Demonstrates using default configuration with standard operations.
     *
     * Although the test class customizes scenario marker and expectation suffix, operation
     * defaults remain standard (CLEAN_INSERT for preparation).
     *
     * Test flow:
     * - Preparation: Loads ID=1 (Alice) and ID=2 (Bob) with ACTIVE status
     * - Execution: Inserts ID=3 (Charlie, INACTIVE, 2024-02-01)
     * - Expectation: Verifies all three records exist
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use custom operation defaults`(): Unit =
        logger.info("Running custom operation defaults test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Charlie', 'INACTIVE', '2024-02-01')",
            )
            logger.info("Record inserted successfully")
        }
}
