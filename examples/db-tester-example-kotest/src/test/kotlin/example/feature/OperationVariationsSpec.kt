package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseOrder
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates all database operations for test data preparation using Kotest.
 *
 * This specification provides comprehensive coverage of all [Operation] enum values:
 * - [Operation.CLEAN_INSERT] - Delete all rows, then insert (default, most common)
 * - [Operation.INSERT] - Insert new rows (fails if primary key already exists)
 * - [Operation.UPDATE] - Update existing rows only (fails if row not exists)
 * - [Operation.REFRESH] - Update if exists, insert if not (upsert)
 * - [Operation.DELETE] - Delete only specified rows by primary key
 * - [Operation.DELETE_ALL] - Delete all rows from tables
 * - [Operation.TRUNCATE_TABLE] - Truncate tables, resetting auto-increment sequences
 * - [Operation.TRUNCATE_INSERT] - Truncate then insert (predictable IDs)
 *
 * **Note on Partial Column Validation:** The expectation CSV files in this test
 * omit COLUMN3 (TIMESTAMP) to demonstrate that specifying all table columns in CSV files is not
 * required.
 */
class OperationVariationsSpec : AnnotationSpec() {
    /**
     * Ensures tests run in sequential (source code) order to match Spock behavior.
     *
     * This is important because some tests (e.g., REFRESH operation) depend on the
     * database state from previous tests not containing unexpected data.
     */
    override fun testCaseOrder(): TestCaseOrder = TestCaseOrder.Sequential

    companion object {
        private val logger = LoggerFactory.getLogger(OperationVariationsSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:OperationVariationsSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                OperationVariationsSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for OperationVariationsSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/OperationVariationsSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates CLEAN_INSERT operation (default).
     *
     * Deletes all existing rows, then inserts test data. Most common operation for test setup.
     */
    @Test
    @DataSet(operation = Operation.CLEAN_INSERT)
    @ExpectedDataSet
    fun `should use clean insert operation`(): Unit =
        logger.info("Running clean insert operation test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (3, 'Tablet', 25, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            logger.info("Clean insert operation test completed")
        }

    /**
     * Demonstrates INSERT operation behavior.
     *
     * Uses DELETE_ALL preparation to ensure clean state, then demonstrates INSERT behavior.
     */
    @Test
    @DataSet(operation = Operation.DELETE_ALL)
    @ExpectedDataSet
    fun `should use insert operation`(): Unit =
        logger.info("Running insert operation test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (1, 'Keyboard', 20, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (2, 'Monitor', 15, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (3, 'Smartwatch', 30, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            logger.info("Insert operation test completed")
        }

    /**
     * Demonstrates UPDATE operation.
     *
     * Updates existing rows only. The UPDATE operation requires rows to already exist.
     */
    @Test
    @DataSet(operation = Operation.CLEAN_INSERT)
    @ExpectedDataSet
    fun `should use update operation`(): Unit =
        logger.info("Running update operation test").also {
            executeSql(dataSource, "UPDATE TABLE1 SET COLUMN2 = 8 WHERE ID = 2")
            logger.info("Update operation test completed")
        }

    /**
     * Demonstrates REFRESH operation (upsert).
     *
     * Updates row if exists, inserts if not exists. REFRESH does not delete rows not in the CSV,
     * so we first ensure ID=3 does not exist to make this test independent of execution order.
     */
    @Test
    @DataSet(operation = Operation.REFRESH)
    @ExpectedDataSet
    fun `should use refresh operation`(): Unit =
        logger.info("Running refresh operation test").also {
            // Ensure ID=3 doesn't exist (REFRESH doesn't delete rows not in CSV)
            executeSql(dataSource, "DELETE FROM TABLE1 WHERE ID = 3")
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (3, 'Headphones', 40, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            logger.info("Refresh operation test completed")
        }

    /**
     * Demonstrates DELETE_ALL followed by INSERT.
     *
     * Clears table completely before inserting test data.
     */
    @Test
    @DataSet(operation = Operation.DELETE_ALL)
    @ExpectedDataSet
    fun `should use delete all operation`(): Unit =
        logger.info("Running delete all operation test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (1, 'Camera', 15, CURRENT_TIMESTAMP)
                """.trimIndent(),
            )
            logger.info("Delete all operation test completed")
        }

    /**
     * Demonstrates testing deletion scenarios with database validation.
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use delete operation`(): Unit =
        logger.info("Running delete operation test").also {
            executeSql(dataSource, "DELETE FROM TABLE1 WHERE ID = 2")
            logger.info("Delete operation test completed")
        }

    /**
     * Demonstrates TRUNCATE_INSERT operation.
     *
     * Truncates tables then inserts test data for predictable ID values.
     */
    @Test
    @DataSet(operation = Operation.TRUNCATE_INSERT)
    @ExpectedDataSet
    fun `should use truncate insert operation`(): Unit =
        logger.info("Running truncate insert operation test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (3, 'Monitor', 12, NULL)
                """.trimIndent(),
            )
            logger.info("Truncate insert operation test completed")
        }

    /**
     * Demonstrates TRUNCATE_TABLE operation.
     *
     * Truncates tables, removing all data and resetting auto-increment sequences.
     */
    @Test
    @DataSet(operation = Operation.TRUNCATE_TABLE)
    @ExpectedDataSet
    fun `should use truncate table operation`(): Unit =
        logger.info("Running truncate table operation test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (1, 'Keyboard', 25, NULL)
                """.trimIndent(),
            )
            logger.info("Truncate table operation test completed")
        }
}
