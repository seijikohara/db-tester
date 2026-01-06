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
 * Demonstrates using multiple named data sources in a single test using Kotest.
 *
 * This specification shows:
 * - Registering multiple named data sources
 * - Using [DataSetSource.dataSourceName] in annotations
 * - Working with different databases simultaneously
 *
 * Use cases:
 * - Multi-tenant applications with separate database instances
 * - Microservices with their own databases
 * - Testing data synchronization between databases
 */
class MultipleDataSourceSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(MultipleDataSourceSpec::class.java)

        private fun createPrimaryDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:MultipleDataSourceSpec_Primary;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun createSecondaryDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:MultipleDataSourceSpec_Secondary;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                MultipleDataSourceSpec::class.java.classLoader.getResource(scriptPath)
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
    private lateinit var primaryDataSource: DataSource
    private lateinit var secondaryDataSource: DataSource

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up two H2 in-memory databases.
     *
     * Creates:
     * - Default database - primary data store
     * - Named database "inventory" - secondary data store
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory databases for MultipleDataSourceSpec").also {
            primaryDataSource = createPrimaryDataSource()
            registry.registerDefault(primaryDataSource)
            executeScript(primaryDataSource, "ddl/feature/MultipleDataSourceSpec-primary.sql")
            logger.info("Primary database setup completed")

            secondaryDataSource = createSecondaryDataSource()
            registry.register("inventory", secondaryDataSource)
            executeScript(secondaryDataSource, "ddl/feature/MultipleDataSourceSpec-secondary.sql")
            logger.info("Secondary database setup completed")
        }

    /**
     * Tests operations on the default (primary) database.
     *
     * Uses default dataSourceName (empty string refers to the default data source).
     *
     * Test flow:
     * - Preparation: Loads default database - TABLE1(ID=1 Alice, ID=2 Bob)
     * - Execution: Inserts ID=3 (Charlie Brown, charlie@example.com) into default database
     * - Expectation: Verifies all three customers exist in default database
     */
    @Test
    @DataSet(
        dataSets = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/MultipleDataSourceSpec/default/",
                scenarioNames = ["default"],
            ),
        ],
    )
    @ExpectedDataSet(
        dataSets = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/MultipleDataSourceSpec/default/expected/",
                scenarioNames = ["default"],
            ),
        ],
    )
    fun `should manage customers in default database`(): Unit =
        logger.info("Running default database test").also {
            executeSql(
                primaryDataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Charlie Brown', 'charlie@example.com')",
            )
            logger.info("Customer data inserted into default database")
        }

    /**
     * Tests operations on the named secondary (inventory) database.
     *
     * Uses [DataSetSource.dataSourceName] = "inventory" to specify the secondary database.
     *
     * Test flow:
     * - Preparation: Loads inventory database - TABLE1(ID=1 Laptop, ID=2 Keyboard)
     * - Execution: Inserts ID=3 (Monitor, 25) into inventory database
     * - Expectation: Verifies all three products exist in inventory database
     */
    @Test
    @DataSet(
        dataSets = [
            DataSetSource(
                dataSourceName = "inventory",
                resourceLocation = "classpath:example/feature/MultipleDataSourceSpec/inventory/",
                scenarioNames = ["inventory"],
            ),
        ],
    )
    @ExpectedDataSet(
        dataSets = [
            DataSetSource(
                dataSourceName = "inventory",
                resourceLocation = "classpath:example/feature/MultipleDataSourceSpec/inventory/expected/",
                scenarioNames = ["inventory"],
            ),
        ],
    )
    fun `should manage products in inventory database`(): Unit =
        logger.info("Running inventory database test").also {
            executeSql(
                secondaryDataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Monitor', 25)",
            )
            logger.info("Product data inserted into inventory database")
        }
}
