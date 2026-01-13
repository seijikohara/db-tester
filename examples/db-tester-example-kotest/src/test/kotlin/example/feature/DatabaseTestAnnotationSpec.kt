package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
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
 * Demonstrates the simplified `@DatabaseTest` annotation approach with Kotest.
 *
 * This test illustrates:
 * - Using `@DatabaseTest` annotation for automatic extension registration
 * - Convention-based registry discovery via `dbTesterRegistry` property
 * - Cleaner test setup without explicit `init { extensions(...) }` block
 *
 * The `@DatabaseTest` annotation automatically registers [DatabaseTestExtension]
 * and discovers the [DataSourceRegistry] by looking for a property named `dbTesterRegistry`.
 *
 * CSV files are located at:
 * - `src/test/resources/example/feature/DatabaseTestAnnotationSpec/TABLE1.csv`
 * - `src/test/resources/example/feature/DatabaseTestAnnotationSpec/expected/TABLE1.csv`
 */
@DatabaseTest
class DatabaseTestAnnotationSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseTestAnnotationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:DatabaseTestAnnotationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                DatabaseTestAnnotationSpec::class.java.classLoader.getResource(scriptPath)
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

    /**
     * Convention-based registry discovery: the extension finds this property by name.
     *
     * The property must be named `dbTesterRegistry` for automatic discovery.
     */
    override val dbTesterRegistry = DataSourceRegistry()

    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for DatabaseTestAnnotationSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/DatabaseTestAnnotationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates the `@DatabaseTest` annotation-based approach.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1 Mouse, ID=2 Monitor) from `TABLE1.csv`
     * - Execution: Inserts ID=3 (Keyboard, 79.99) into TABLE1
     * - Expectation: Verifies all three products from `expected/TABLE1.csv`
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should load and verify product data with @DatabaseTest annotation`(): Unit =
        logger.info("Running @DatabaseTest annotation example test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Keyboard', 79.99)")
            logger.info("Product data inserted successfully")
        }
}
