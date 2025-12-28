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
 * Demonstrates advanced annotation configuration features using Kotest.
 *
 * This specification shows:
 * - Explicit `resourceLocation` specification
 * - Multiple `scenarioNames` in a single DataSet
 * - Class-level vs method-level annotation precedence
 * - Custom directory structure
 * - Multiple tables with foreign key relationships
 *
 * Directory structure:
 * ```
 * example/feature/AnnotationConfigurationSpec/
 *   custom-location/
 *     TABLE1.csv
 *     TABLE2.csv
 *   expected/
 *     TABLE1.csv
 *     TABLE2.csv
 * ```
 */
@Preparation(
    dataSets = [
        DataSet(
            resourceLocation = "classpath:example/feature/AnnotationConfigurationSpec/",
            scenarioNames = ["classLevel"],
        ),
    ],
)
class AnnotationConfigurationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(AnnotationConfigurationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:AnnotationConfigurationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                AnnotationConfigurationSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for AnnotationConfigurationSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/AnnotationConfigurationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates explicit resource location specification.
     *
     * Uses custom directory path instead of convention-based resolution.
     *
     * Test flow:
     * - Preparation: Loads from `custom-location/` - TABLE1(ID=1,2), TABLE2(ID=1,2)
     * - Execution: Inserts ID=3 (Marketing, Tokyo) into TABLE1
     * - Expectation: Verifies all three departments and two employees exist
     */
    @Test
    @Preparation(
        dataSets = [
            DataSet(
                resourceLocation = "classpath:example/feature/AnnotationConfigurationSpec/custom-location/",
            ),
        ],
    )
    @Expectation
    fun `should use custom resource location`(): Unit =
        logger.info("Running custom resource location test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Marketing', 'Tokyo')",
            )
            logger.info("Department data inserted successfully")
        }

    /**
     * Demonstrates multiple scenario names in a single test.
     *
     * Loads rows matching either scenario name from the same CSV files.
     *
     * Test flow:
     * - Preparation: Loads scenario1 and scenario2 - TABLE1(ID=1,2), TABLE2(ID=1,2)
     * - Execution: Updates Bob Smith's salary from 60000.00 to 65000.00
     * - Expectation: Verifies both departments and updated employee salary
     */
    @Test
    @Preparation(dataSets = [DataSet(scenarioNames = ["scenario1", "scenario2"])])
    @Expectation
    fun `should handle multiple scenarios`(): Unit =
        logger.info("Running multiple scenarios test").also {
            executeSql(dataSource, "UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2")
            logger.info("Employee salary updated successfully")
        }

    /**
     * Demonstrates multiple scenario names for preparation and expectation.
     */
    @Test
    @Preparation(dataSets = [DataSet(scenarioNames = ["scenario1", "scenario2"])])
    @Expectation(dataSets = [DataSet(scenarioNames = ["should merge multiple data sets"])])
    fun `should merge multiple data sets`(): Unit =
        logger.info("Running merge multiple data sets test").also {
            executeSql(dataSource, "UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2")
            logger.info("Employee salary updated successfully")
        }

    /**
     * Demonstrates class-level annotation inheritance.
     *
     * This test uses the class-level `@Preparation` annotation defined at the class level.
     *
     * Test flow:
     * - Preparation: Uses class-level @Preparation with scenario "classLevel"
     * - Execution: Inserts new employee (ID=100, New Employee, 45000.00) into TABLE2
     * - Expectation: Verifies HR department and two employees
     */
    @Test
    @Expectation
    fun `should use class level annotation`(): Unit =
        logger.info("Running class level annotation test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (100, 'New Employee', 1, 45000.00)",
            )
            logger.info("New employee data inserted successfully")
        }

    /**
     * Demonstrates using different scenarios for preparation and expectation.
     */
    @Test
    @Preparation(dataSets = [DataSet(scenarioNames = ["multiDataSet1"])])
    @Expectation(dataSets = [DataSet(scenarioNames = ["multiDataSet"])])
    fun `should handle multiple data sets`(): Unit =
        logger.info("Running multiple data sets test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Research', 'Osaka')",
            )
            logger.info("New department data inserted successfully")
        }
}
