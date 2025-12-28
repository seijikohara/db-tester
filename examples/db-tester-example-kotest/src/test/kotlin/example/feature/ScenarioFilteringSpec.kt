package example.feature

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates scenario-based testing with CSV row filtering using Kotest.
 *
 * This specification illustrates the scenario filtering feature that enables sharing
 * a single CSV file across multiple test methods. Each test automatically loads only
 * rows matching its method name from the `[Scenario]` marker column.
 *
 * Features demonstrated:
 * - Using scenario marker column for row filtering
 * - Sharing a single CSV file across multiple test methods
 * - Test method name as automatic scenario filter
 * - Reducing CSV file duplication
 * - Class-level [Preparation] and [Expectation] annotations
 *
 * CSV files contain scenario marker column that filters rows by test method name:
 * ```
 * [Scenario],ID,COLUMN1,COLUMN2,COLUMN3
 * should create active user,1,alice,alice@example.com,ACTIVE
 * should create inactive user,1,bob,bob@example.com,INACTIVE
 * ```
 */
@Preparation
@Expectation
class ScenarioFilteringSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ScenarioFilteringSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ScenarioFilteringSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ScenarioFilteringSpec::class.java.classLoader.getResource(scriptPath)
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
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for ScenarioFilteringSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ScenarioFilteringSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates scenario filtering for active user creation.
     *
     * Only rows matching the test method name are loaded from TABLE1.csv
     * using the `[Scenario]` marker column.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (alice, ACTIVE) filtered by scenario name
     * - Execution: Inserts ID=2 (charlie, ACTIVE)
     * - Expectation: Verifies both records exist with ACTIVE status
     */
    @Test
    fun `should create active user`(): Unit =
        logger.info("Creating a new active user").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (2, 'charlie', 'charlie@example.com', 'ACTIVE')
                """.trimIndent(),
            )
            logger.info("Active user created successfully")
        }

    /**
     * Demonstrates scenario filtering for inactive user creation.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (bob, INACTIVE) filtered by scenario name
     * - Execution: Inserts ID=2 (david, INACTIVE)
     * - Expectation: Verifies both records exist with INACTIVE status
     */
    @Test
    fun `should create inactive user`(): Unit =
        logger.info("Creating a new inactive user").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (2, 'david', 'david@example.com', 'INACTIVE')
                """.trimIndent(),
            )
            logger.info("Inactive user created successfully")
        }

    /**
     * Demonstrates scenario filtering with multiple existing users.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (eve, ACTIVE) and ID=2 (frank, INACTIVE)
     * - Execution: Updates ID=2 status from INACTIVE to SUSPENDED
     * - Expectation: Verifies ID=1 remains ACTIVE and ID=2 is SUSPENDED
     */
    @Test
    fun `should handle multiple users`(): Unit =
        logger.info("Suspending an inactive user").also {
            executeSql(dataSource, "UPDATE TABLE1 SET COLUMN3 = 'SUSPENDED' WHERE ID = 2")
            logger.info("User status updated to SUSPENDED")
        }
}
