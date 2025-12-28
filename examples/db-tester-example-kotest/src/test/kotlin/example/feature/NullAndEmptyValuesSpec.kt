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
 * Demonstrates NULL value and empty string handling in CSV files using Kotest.
 *
 * This specification shows:
 * - Using empty cells to represent SQL NULL values
 * - Distinguishing between NULL and empty string in VARCHAR columns
 * - Handling NOT NULL constraints
 * - NULL values in numeric and timestamp columns
 *
 * CSV format examples and NULL representation:
 * ```
 * ID,COLUMN1,COLUMN2,COLUMN3,COLUMN4
 * 1,Required Value,,100,
 * 2,Another Value,Optional Value,200,42
 * ```
 *
 * **Important:** Empty cells in CSV files are interpreted as SQL NULL
 * for all column types.
 */
class NullAndEmptyValuesSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(NullAndEmptyValuesSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:NullAndEmptyValuesSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                NullAndEmptyValuesSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for NullAndEmptyValuesSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/NullAndEmptyValuesSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates NULL value handling in CSV files.
     *
     * Validates:
     * - Empty cells correctly represent SQL NULL values
     * - NULL values in optional (nullable) columns
     * - Empty string vs NULL distinction
     * - NOT NULL constraints are respected
     *
     * Test flow:
     * - Preparation: Loads TABLE1(ID=1 with NULL COLUMN2/COLUMN4, ID=2 with values)
     * - Execution: Inserts ID=3 (Third Record, NULL, 300, NULL)
     * - Expectation: Verifies all three records including NULL values
     */
    @Test
    @Preparation
    @Expectation
    fun `should handle null values`(): Unit =
        logger.info("Running null values test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4) VALUES (3, 'Third Record', NULL, 300, NULL)",
            )
            logger.info("Record with NULL values inserted successfully")
        }
}
