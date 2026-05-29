package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates convention-based data loading grouped by logical specification.
 *
 * Kotest [AnnotationSpec] requires top-level specification classes, so each logical group is a
 * dedicated specification rather than a nested class. The JUnit equivalent uses `@Nested` classes
 * whose runtime names embed the enclosing class with a dollar sign. Each Kotest specification here
 * resolves convention-based CSV files from a directory named after the specification simple name.
 *
 * This specification illustrates:
 * - Convention-based CSV resolution from `classpath:example/feature/<SimpleClassName>/`
 * - Scenario filtering by feature method name within each group
 */
object NestedConventionSpec

/**
 * Specification for user-related operations.
 *
 * Loads convention-based CSV files from `classpath:example/feature/NestedConventionUserSpec/`.
 */
@DatabaseTest
class NestedConventionUserSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(NestedConventionUserSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:NestedConventionSpec_User;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                NestedConventionUserSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for NestedConventionUserSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/NestedConventionSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies convention-based loading and the createUser scenario.
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["createUser"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["createUser"])])
    fun `should create new user with convention-based data loading`(): Unit =
        executeSql(
            dataSource,
            """
            INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
            VALUES (2, 'jane_doe', 'jane@example.com', true)
            """.trimIndent(),
        )

    /**
     * Verifies convention-based loading and the updateStatus scenario.
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["updateStatus"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["updateStatus"])])
    fun `should update user status with convention-based data loading`(): Unit =
        executeSql(dataSource, "UPDATE TABLE1 SET COLUMN3 = false WHERE ID = 1")
}

/**
 * Specification for product-related operations.
 *
 * Loads convention-based CSV files from `classpath:example/feature/NestedConventionProductSpec/`.
 */
@DatabaseTest
class NestedConventionProductSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(NestedConventionProductSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:NestedConventionSpec_Product;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                NestedConventionProductSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for NestedConventionProductSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/NestedConventionSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies convention-based loading and the addProduct scenario.
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["addProduct"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["addProduct"])])
    fun `should add new product with convention-based data loading`(): Unit =
        executeSql(
            dataSource,
            """
            INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3)
            VALUES (2, 'Tablet', 299.99, 15)
            """.trimIndent(),
        )

    /**
     * Verifies convention-based loading and the updatePrice scenario.
     */
    @Test
    @DataSet(sources = [DataSetSource(scenarioNames = ["updatePrice"])])
    @ExpectedDataSet(sources = [DataSetSource(scenarioNames = ["updatePrice"])])
    fun `should update product price with convention-based data loading`(): Unit =
        executeSql(dataSource, "UPDATE TABLE2 SET COLUMN2 = 899.99 WHERE ID = 1")
}
