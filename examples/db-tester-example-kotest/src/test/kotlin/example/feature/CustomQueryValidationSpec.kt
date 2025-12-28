package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
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
 * Demonstrates database testing with custom query validation scenarios using Kotest.
 *
 * This specification illustrates:
 * - Testing INSERT operations with data
 * - Using custom expectation paths for different scenarios
 * - Validating filtered data
 * - Testing aggregation scenarios
 * - Validating date-range queries
 *
 * Note: For actual SQL query result validation using
 * [io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion.assertEqualsByQuery],
 * you would need to programmatically create expected datasets using DbUnit APIs.
 */
class CustomQueryValidationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(CustomQueryValidationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:CustomQueryValidationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                CustomQueryValidationSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for CustomQueryValidationSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/CustomQueryValidationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates validation with filtered data.
     *
     * Validates data after adding new record with specific filter criteria.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1,2,3) with sales data
     * - Execution: Inserts ID=4 (COLUMN1=3, East region, 2024-01-25, 350.00)
     * - Expectation: Verifies all four records from `expected-filtered/`
     */
    @Test
    @Preparation
    @Expectation(
        dataSets = [
            DataSet(
                resourceLocation = "classpath:example/feature/CustomQueryValidationSpec/expected-filtered/",
            ),
        ],
    )
    fun `should validate regional sales`(): Unit =
        logger.info("Inserting new regional sales record").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (4, 3, '2024-01-25', 350.00, 'East')
                """.trimIndent(),
            )
            logger.info("Regional sales record inserted successfully")
        }

    /**
     * Demonstrates validation with aggregated data.
     *
     * Validates aggregated data after adding new record.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1,2,3) with sales data
     * - Execution: Inserts ID=4 (COLUMN1=1, West region, 2024-01-25, 500.00)
     * - Expectation: Verifies all four records from `expected-aggregation/`
     */
    @Test
    @Preparation
    @Expectation(
        dataSets = [
            DataSet(
                resourceLocation = "classpath:example/feature/CustomQueryValidationSpec/expected-aggregation/",
            ),
        ],
    )
    fun `should validate sales summary`(): Unit =
        logger.info("Inserting new sales record for aggregation").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (4, 1, '2024-01-25', 500.00, 'West')
                """.trimIndent(),
            )
            logger.info("Sales record for aggregation inserted successfully")
        }

    /**
     * Demonstrates validation with high-value records.
     *
     * Validates data after adding a high-value record.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1,2,3) with sales data (January)
     * - Execution: Inserts ID=4 (COLUMN1=1, North region, 2024-02-01, 600.00)
     * - Expectation: Verifies all four records including February data from `expected-join/`
     */
    @Test
    @Preparation
    @Expectation(
        dataSets = [
            DataSet(
                resourceLocation = "classpath:example/feature/CustomQueryValidationSpec/expected-join/",
            ),
        ],
    )
    fun `should validate high value sales`(): Unit =
        logger.info("Inserting high-value sales record").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (4, 1, '2024-02-01', 600.00, 'North')
                """.trimIndent(),
            )
            logger.info("High-value sales record inserted successfully")
        }

    /**
     * Demonstrates validation with date range filtering for January sales.
     *
     * Validates that only January sales data is present in the database by adding a January record
     * and verifying the final state contains only January data.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1,2,3) with January sales data
     * - Execution: Inserts ID=4 (COLUMN1=2, South region, 2024-01-25, 450.00)
     * - Expectation: Verifies all four January records from `expected-daterange/`
     */
    @Test
    @Preparation
    @Expectation(
        dataSets = [
            DataSet(
                resourceLocation = "classpath:example/feature/CustomQueryValidationSpec/expected-daterange/",
            ),
        ],
    )
    fun `should validate january sales`(): Unit =
        logger.info("Inserting January sales record").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (4, 2, '2024-01-25', 450.00, 'South')
                """.trimIndent(),
            )
            logger.info("January sales record inserted successfully")
        }
}
