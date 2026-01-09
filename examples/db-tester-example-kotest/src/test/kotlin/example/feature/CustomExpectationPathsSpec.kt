package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates custom expectation paths for flexible test data organization using Kotest.
 *
 * This specification demonstrates using [DataSetSource.resourceLocation] to specify custom paths
 * for expectation data, enabling flexible test data organization beyond convention-based defaults.
 *
 * Key features demonstrated:
 * - Custom expectation paths using [DataSetSource] annotation
 * - Organizing multiple expectation scenarios in subdirectories
 * - Multi-stage testing with different expected states
 * - Complex business logic validation with database state changes
 *
 * This approach is useful when tests require multiple expectation variants or when
 * convention-based paths are insufficient for complex test scenarios.
 */
class CustomExpectationPathsSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(CustomExpectationPathsSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:CustomExpectationPathsSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                CustomExpectationPathsSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for CustomExpectationPathsSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/CustomExpectationPathsSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates custom expectation paths with basic INSERT operation.
     *
     * This test uses [DataSetSource.resourceLocation] to specify a custom path for expectation
     * data, demonstrating how to organize test data in non-default directories.
     *
     * Test flow:
     * - Preparation: Loads ID=1 from default location
     * - Execution: Inserts ID=2 (customer_id=1, amount=299.99, PENDING)
     * - Expectation: Verifies both records from `expected-basic/` directory
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/CustomExpectationPathsSpec/expected-basic/",
            ),
        ],
    )
    fun `should insert new order`(): Unit =
        logger.info("Running should insert new order test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (2, 1, 299.99, '2024-02-15', 'PENDING')
                """.trimIndent(),
            )
            logger.info("New order inserted successfully")
        }

    /**
     * Demonstrates partial column validation using custom expectation paths.
     *
     * CSV files in the custom path contain only the columns to validate, allowing partial
     * validation without programmatic assertions.
     *
     * Test flow:
     * - Preparation: Loads ID=1 from default location
     * - Execution: Inserts ID=2 (customer_id=2, amount=599.99, SHIPPED)
     * - Expectation: Validates selected columns from `expected-ignore-columns/` directory
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/CustomExpectationPathsSpec/expected-ignore-columns/",
            ),
        ],
    )
    fun `should validate with partial columns`(): Unit =
        logger.info("Running should validate with partial columns test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (2, 2, 599.99, '2024-03-20', 'SHIPPED')
                """.trimIndent(),
            )
            logger.info("Shipped order inserted successfully")
        }

    /**
     * Demonstrates validating related tables with custom expectation paths.
     *
     * This test inserts data into TABLE2 and validates the relationship with TABLE1 using a custom
     * expectation directory.
     *
     * Test flow:
     * - Preparation: Loads existing orders and items
     * - Execution: Inserts ID=3 (order_id=1, product=Headphones)
     * - Expectation: Verifies order-item relationship from `expected-query/` directory
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/CustomExpectationPathsSpec/expected-query/",
            ),
        ],
    )
    fun `should validate order items`(): Unit =
        logger.info("Running should validate order items test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (3, 1, 'Headphones', 1, 79.99)
                """.trimIndent(),
            )
            logger.info("Order item inserted successfully")
        }

    /**
     * Demonstrates multi-stage workflow testing with custom expectation paths (stage 1).
     *
     * This test represents the first stage of an order lifecycle, validating the initial PENDING
     * state using a stage-specific expectation directory.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (existing order)
     * - Execution: Inserts ID=2 (customer_id=1, amount=150.00, PENDING)
     * - Expectation: Verifies PENDING status from `expected-stage1/` directory
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/CustomExpectationPathsSpec/expected-stage1/",
            ),
        ],
    )
    fun `should create order`(): Unit =
        logger.info("Running should create order test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
                """.trimIndent(),
            )
            logger.info("Order created with PENDING status")
        }

    /**
     * Demonstrates multi-stage workflow testing with custom expectation paths (stage 2).
     *
     * This test represents order status transition from PENDING to SHIPPED, demonstrating how
     * different expectation directories can validate different workflow stages.
     *
     * Test flow:
     * - Preparation: Loads ID=1 (existing order)
     * - Execution: Creates ID=2 as PENDING, then updates to SHIPPED
     * - Expectation: Verifies SHIPPED status from `expected-stage2/` directory
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/CustomExpectationPathsSpec/expected-stage2/",
            ),
        ],
    )
    fun `should ship order`(): Unit =
        logger.info("Running should ship order test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
                """.trimIndent(),
            )
            executeSql(
                dataSource,
                "UPDATE TABLE1 SET COLUMN4 = 'SHIPPED' WHERE ID = 2",
            )
            logger.info("Order shipped successfully")
        }
}
