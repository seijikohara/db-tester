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
 * Demonstrates table ordering strategies for foreign key constraints using Kotest.
 *
 * This specification shows:
 * - Automatic alphabetical table ordering (default)
 * - Manual ordering via `load-order.txt` file
 * - Programmatic ordering via `load-order.txt` in custom directories
 * - Handling foreign key constraints
 * - Complex table dependencies (many-to-many relationships)
 *
 * Table ordering is critical when:
 * - Tables have foreign key relationships
 * - Parent tables must be loaded before child tables
 * - Junction tables require both parent tables
 * - Deletion order must be reverse of insertion order
 *
 * Schema:
 * ```
 * TABLE1 (parent)
 *   |
 * TABLE2 (child of TABLE1)
 *   |
 * TABLE3 (independent)
 *   |
 * TABLE4 (junction: TABLE2 + TABLE3)
 * ```
 */
class TableOrderingStrategiesSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(TableOrderingStrategiesSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TableOrderingStrategiesSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TableOrderingStrategiesSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for TableOrderingStrategiesSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TableOrderingStrategiesSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates automatic alphabetical table ordering.
     *
     * Framework orders tables alphabetically: TABLE1, TABLE2, TABLE3, TABLE4. This works well when
     * foreign keys follow alphabetical order.
     *
     * Test flow:
     * - Preparation: TABLE1(1,2), TABLE2(1,2,3), TABLE3(1,2,3), TABLE4(3 rows)
     * - Execution: Inserts TABLE1(3,'Services'), TABLE2(4,3,'Consulting')
     * - Expectation: Verifies TABLE1 has 3 rows, TABLE2 has 4 rows
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use alphabetical ordering`(): Unit =
        logger.info("Running should use alphabetical ordering test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1) VALUES (3, 'Services')")
            executeSql(dataSource, "INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (4, 3, 'Consulting')")
            logger.info("Alphabetical ordering test completed")
        }

    /**
     * Demonstrates manual table ordering via load-order.txt file.
     *
     * Uses `load-order.txt` to specify correct insertion order for foreign keys.
     *
     * Test flow:
     * - Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2,3), TABLE4(2 rows)
     * - Execution: Inserts TABLE3(4,'Featured'), TABLE4(1,4)
     * - Expectation: Verifies TABLE3 has 4 rows, TABLE4 has 3 rows with new association
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should use manual ordering`(): Unit =
        logger.info("Running should use manual ordering test").also {
            executeSql(dataSource, "INSERT INTO TABLE3 (ID, COLUMN1) VALUES (4, 'Featured')")
            executeSql(dataSource, "INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (1, 4)")
            logger.info("Manual ordering test completed")
        }

    /**
     * Demonstrates custom resource location for table ordering.
     *
     * Uses `load-order.txt` in a custom directory to explicitly control table insertion order.
     *
     * Test flow:
     * - Preparation: TABLE2(1,1,'Widget') from programmatic/ directory
     * - Execution: Updates TABLE2 COLUMN2 from 'Widget' to 'Updated Widget' WHERE ID=1
     * - Expectation: Verifies TABLE2(1,1,'Updated Widget')
     */
    @Test
    @DataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TableOrderingStrategiesSpec/programmatic/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TableOrderingStrategiesSpec/programmatic/expected/",
            ),
        ],
    )
    fun `should use programmatic ordering`(): Unit =
        logger.info("Running should use programmatic ordering test").also {
            executeSql(dataSource, "UPDATE TABLE2 SET COLUMN2 = 'Updated Widget' WHERE ID = 1")
            logger.info("Programmatic ordering test completed")
        }

    /**
     * Demonstrates handling complex many-to-many relationships.
     *
     * Shows proper ordering for junction tables with multiple foreign keys.
     *
     * Test flow:
     * - Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2), TABLE4(2 rows)
     * - Execution: Adds TABLE1(4,'Accessories'), TABLE2(5,4,'Cable'), TABLE3(5,'Essential'),
     *   TABLE4(5,1), TABLE4(5,5)
     * - Expectation: Verifies all 4 tables have new records with proper foreign key relationships
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should handle many to many relationships`(): Unit =
        logger.info("Running should handle many to many relationships test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1) VALUES (4, 'Accessories')")
            executeSql(dataSource, "INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (5, 4, 'Cable')")
            executeSql(dataSource, "INSERT INTO TABLE3 (ID, COLUMN1) VALUES (5, 'Essential')")
            executeSql(dataSource, "INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 1)")
            executeSql(dataSource, "INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 5)")
            logger.info("Many to many relationships test completed")
        }
}
