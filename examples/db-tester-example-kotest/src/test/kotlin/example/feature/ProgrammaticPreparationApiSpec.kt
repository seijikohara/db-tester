package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.preparation.DatabasePreparation
import io.github.seijikohara.dbtester.api.preparation.PreparationConfig
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates the [DatabasePreparation] programmatic API for preparing test data without
 * `@DataSet` annotations with Kotest.
 *
 * This specification illustrates how to use the programmatic preparation API for scenarios where
 * annotation-based dataset loading is insufficient or impractical:
 * - [DatabasePreparation.cleanInsert] - Clean insert with default configuration
 * - [DatabasePreparation.cleanInsert] with [PreparationConfig] - Clean insert with custom batch
 *   size and transaction mode
 * - [DatabasePreparation.execute] with [Operation] - Execute a specific operation (INSERT, UPDATE,
 *   DELETE)
 * - [DatabasePreparation.execute] with [Operation] and [PreparationConfig] - Execute with full
 *   configuration control
 *
 * Programmatic preparation supports dynamic test data generation, computed values, and mid-test
 * data manipulation that cannot be expressed in static CSV files.
 *
 * @see DatabasePreparation
 * @see PreparationConfig
 * @see Operation
 */
@DatabaseTest
class ProgrammaticPreparationApiSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammaticPreparationApiSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ProgrammaticPreparationApiSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ProgrammaticPreparationApiSpec::class.java.classLoader.getResource(scriptPath)
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for ProgrammaticPreparationApiSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ProgrammaticPreparationApiSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies that [DatabasePreparation.cleanInsert] inserts data with default configuration.
     *
     * This test demonstrates programmatic preparation by building tables using [Table.ofValues]
     * and inserting them via `cleanInsert` with standard defaults.
     *
     * Test flow:
     * - Preparation: Programmatically constructs USERS and ORDERS tables
     * - Execution: Calls `DatabasePreparation.cleanInsert(dataSource, tableSet)`
     * - Expectation: Verifies database state via `@ExpectedDataSet`
     */
    @Test
    @ExpectedDataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ProgrammaticPreparationApiSpec/clean-insert-default/expected/")])
    fun `should insert data with default settings`(): Unit {
        // Given
        logger.info("Running cleanInsert with default settings")

        val usersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(
                listOf(1, "Alice", "alice@example.com"),
                listOf(2, "Bob", "bob@example.com")))

        val ordersTable = Table.ofValues(
            "ORDERS",
            listOf("ID", "USER_ID", "PRODUCT", "AMOUNT"),
            listOf(
                listOf(101, 1, "Laptop", 999.99),
                listOf(102, 2, "Mouse", 29.99)))

        val tableSet = TableSet.of(usersTable, ordersTable)

        // When
        DatabasePreparation.cleanInsert(dataSource, tableSet)

        // Then
        logger.info("cleanInsert with default settings completed")
    }

    /**
     * Verifies that [DatabasePreparation.cleanInsert] with [PreparationConfig] inserts data with
     * custom batch size and transaction mode.
     *
     * This test demonstrates customizing preparation behavior via [PreparationConfig] method
     * chaining, including batch size, transaction mode, and table ordering strategy.
     *
     * Test flow:
     * - Preparation: Constructs USERS (3 rows) and ORDERS (3 rows) with custom config
     * - Execution: Calls `cleanInsert` with batch size 2, AUTO_COMMIT, FOREIGN_KEY ordering
     * - Expectation: Verifies all six records via `@ExpectedDataSet`
     */
    @Test
    @ExpectedDataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ProgrammaticPreparationApiSpec/clean-insert-custom-batch/expected/")])
    fun `should insert data with custom batch size`(): Unit {
        // Given
        logger.info("Running cleanInsert with custom batch size")

        val usersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(
                listOf(1, "Alice", "alice@example.com"),
                listOf(2, "Bob", "bob@example.com"),
                listOf(3, "Charlie", "charlie@example.com")))

        val ordersTable = Table.ofValues(
            "ORDERS",
            listOf("ID", "USER_ID", "PRODUCT", "AMOUNT"),
            listOf(
                listOf(101, 1, "Laptop", 999.99),
                listOf(102, 2, "Mouse", 29.99),
                listOf(103, 3, "Keyboard", 79.99)))

        val tableSet = TableSet.of(usersTable, ordersTable)

        val config = PreparationConfig.standard()
            .withBatchSize(2)
            .withTransactionMode(TransactionMode.AUTO_COMMIT)
            .withTableOrderingStrategy(TableOrderingStrategy.FOREIGN_KEY)

        // When
        DatabasePreparation.cleanInsert(dataSource, tableSet, config)

        // Then
        logger.info("cleanInsert with custom batch size completed")
    }

    /**
     * Verifies that [DatabasePreparation.execute] executes an INSERT operation.
     *
     * This test demonstrates using a specific [Operation.INSERT] instead of the default
     * CLEAN_INSERT. The tables are manually cleared before insertion to ensure a clean state.
     *
     * Test flow:
     * - Preparation: Clears ORDERS then USERS tables via SQL DELETE
     * - Execution: Calls `DatabasePreparation.execute` with [Operation.INSERT]
     * - Expectation: Verifies single USERS record via `@ExpectedDataSet`
     */
    @Test
    @ExpectedDataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ProgrammaticPreparationApiSpec/execute-insert/expected/")])
    fun `should execute insert operation`(): Unit {
        // Given
        logger.info("Running execute with INSERT operation")

        executeSql(dataSource, "DELETE FROM ORDERS")
        executeSql(dataSource, "DELETE FROM USERS")

        val usersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(listOf(1, "Alice", "alice@example.com")))

        val tableSet = TableSet.of(usersTable)

        // When
        DatabasePreparation.execute(dataSource, tableSet, Operation.INSERT)

        // Then
        logger.info("execute with INSERT operation completed")
    }

    /**
     * Verifies that [DatabasePreparation.execute] with [Operation] and [PreparationConfig] executes
     * with explicit operation and configuration.
     *
     * This test demonstrates combining a specific operation with custom configuration settings
     * for transaction mode and table ordering strategy.
     *
     * Test flow:
     * - Preparation: Constructs USERS and ORDERS tables with custom config
     * - Execution: Calls `execute` with CLEAN_INSERT, FOREIGN_KEY ordering, and AUTO_COMMIT
     *   transaction mode
     * - Expectation: Verifies all records via `@ExpectedDataSet`
     */
    @Test
    @ExpectedDataSet(
        sources = [DataSetSource(
            resourceLocation = "classpath:example/feature/ProgrammaticPreparationApiSpec/execute-full-config/expected/")])
    fun `should execute with explicit operation and configuration`(): Unit {
        // Given
        logger.info("Running execute with explicit operation and configuration")

        val usersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(
                listOf(1, "Alice", "alice@example.com"),
                listOf(2, "Bob", "bob@example.com")))

        val ordersTable = Table.ofValues(
            "ORDERS",
            listOf("ID", "USER_ID", "PRODUCT", "AMOUNT"),
            listOf(
                listOf(101, 1, "Laptop", 999.99),
                listOf(102, 2, "Mouse", 29.99)))

        val tableSet = TableSet.of(usersTable, ordersTable)

        val config = PreparationConfig.standard()
            .withTransactionMode(TransactionMode.AUTO_COMMIT)
            .withTableOrderingStrategy(TableOrderingStrategy.FOREIGN_KEY)

        // When
        DatabasePreparation.execute(dataSource, tableSet, Operation.CLEAN_INSERT, config)

        // Then
        logger.info("execute with explicit operation and configuration completed")
    }

    /**
     * Verifies that test data can be prepared and verified without any annotations.
     *
     * This test demonstrates a fully programmatic approach using [DatabasePreparation] for data
     * setup and [DatabaseQueryAssertion.assertEqualsByQuery] for verification, without relying on
     * `@DataSet` or `@ExpectedDataSet` annotations.
     *
     * Test flow:
     * - Preparation: Constructs and inserts USERS and ORDERS data via `cleanInsert`
     * - Execution: No additional operation (preparation is the action under test)
     * - Expectation: Builds expected data programmatically and verifies via `assertEqualsByQuery`
     */
    @Test
    fun `should prepare and verify without annotations`(): Unit {
        // Given
        logger.info("Running full programmatic flow without annotations")

        val usersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(
                listOf(1, "Alice", "alice@example.com"),
                listOf(2, "Bob", "bob@example.com")))

        val ordersTable = Table.ofValues(
            "ORDERS",
            listOf("ID", "USER_ID", "PRODUCT", "AMOUNT"),
            listOf(
                listOf(101, 1, "Laptop", 999.99),
                listOf(102, 2, "Mouse", 29.99)))

        val tableSet = TableSet.of(usersTable, ordersTable)

        // When
        DatabasePreparation.cleanInsert(dataSource, tableSet)

        // Then
        val expectedUsersTable = Table.ofValues(
            "USERS",
            listOf("ID", "NAME", "EMAIL"),
            listOf(
                listOf(1, "Alice", "alice@example.com"),
                listOf(2, "Bob", "bob@example.com")))

        DatabaseQueryAssertion.assertEqualsByQuery(
            expectedUsersTable, dataSource, "USERS", "SELECT ID, NAME, EMAIL FROM USERS ORDER BY ID")

        val expectedOrdersTable = Table.ofValues(
            "ORDERS",
            listOf("ID", "USER_ID", "PRODUCT", "AMOUNT"),
            listOf(
                listOf(101, 1, "Laptop", 999.99),
                listOf(102, 2, "Mouse", 29.99)))

        DatabaseQueryAssertion.assertEqualsByQuery(
            expectedOrdersTable, dataSource, "ORDERS",
            "SELECT ID, USER_ID, PRODUCT, AMOUNT FROM ORDERS ORDER BY ID")

        logger.info("Full programmatic flow completed")
    }
}
