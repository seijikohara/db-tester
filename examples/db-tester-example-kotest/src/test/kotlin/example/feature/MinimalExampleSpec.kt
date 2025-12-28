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
 * Demonstrates the minimal convention-based database testing approach with Kotest.
 *
 * This test illustrates:
 * - Automatic CSV file resolution based on test class and method names
 * - Method-level `@Preparation` and `@Expectation` annotations
 * - Single table operations with minimal configuration
 * - H2 in-memory database setup
 *
 * CSV files are located at:
 * - `src/test/resources/example/feature/MinimalExampleSpec/TABLE1.csv`
 * - `src/test/resources/example/feature/MinimalExampleSpec/expected/TABLE1.csv`
 */
class MinimalExampleSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(MinimalExampleSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:MinimalExampleSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                MinimalExampleSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for MinimalExampleSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/MinimalExampleSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates the minimal convention-based test.
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1 Mouse, ID=2 Monitor) from `TABLE1.csv`
     * - Execution: Inserts ID=3 (Keyboard, 79.99) into TABLE1
     * - Expectation: Verifies all three products from `expected/TABLE1.csv`
     */
    @Test
    @Preparation
    @Expectation
    fun `should load and verify product data`(): Unit =
        logger.info("Running minimal example test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Keyboard', 79.99)")
            logger.info("Product data inserted successfully")
        }
}
