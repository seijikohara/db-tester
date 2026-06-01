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
 * Demonstrates NULL value and empty string handling in CSV files using Kotest.
 *
 * This specification shows:
 * - Using empty cells to represent SQL NULL values
 * - Handling NOT NULL constraints
 * - NULL values in numeric and timestamp columns
 * - The NULL versus empty-string distinction available in JSON and YAML
 *
 * CSV format examples and NULL representation:
 * ```
 * ID,COLUMN1,COLUMN2,COLUMN3,COLUMN4
 * 1,Required Value,,100,
 * 2,Another Value,Optional Value,200,42
 * ```
 *
 * **Important:** An empty CSV or TSV cell is interpreted as SQL NULL for all
 * column types, and a quoted `""` also materializes as NULL. The delimited
 * formats cannot express a non-null empty string. To distinguish an empty
 * string from NULL, use JSON or YAML, which provide distinct syntax for
 * `null` and `""`.
 */
@DatabaseTest
class NullAndEmptyValuesSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for NullAndEmptyValuesSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
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
    @DataSet
    @ExpectedDataSet
    fun `should handle null values`(): Unit =
        logger.info("Running null values test").also {
            executeSql(
                dataSource,
                "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4) VALUES (3, 'Third Record', NULL, 300, NULL)",
            )
            logger.info("Record with NULL values inserted successfully")
        }

    /**
     * Documents that the CSV loader normalizes both empty cells and quoted empty strings to SQL
     * NULL for database compatibility.
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should normalize quoted empty string to null`() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement
                    .executeQuery("SELECT COUNT(*) FROM TABLE1 WHERE COLUMN2 IS NULL AND ID IN (1, 2)")
                    .use { rs ->
                        rs.next()
                        check(rs.getInt(1) == 2) {
                            "Expected COLUMN2 to be NULL for both ID=1 and ID=2 (empty cell and quoted empty)"
                        }
                    }
                statement.executeQuery("SELECT COUNT(*) FROM TABLE1 WHERE COLUMN2 = ''").use { rs ->
                    rs.next()
                    check(rs.getInt(1) == 0) {
                        "Loader should not materialize quoted empty cells as empty strings"
                    }
                }
            }
        }
    }

    /**
     * Demonstrates that JSON preserves an empty string as distinct from NULL.
     *
     * A JSON `null` materializes as SQL NULL, while a JSON `""` materializes as a
     * non-null empty string. This distinction is unavailable in CSV and TSV.
     */
    @Test
    @DataSet(
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/NullAndEmptyValuesSpec/jsonEmptyString/")],
    )
    fun `should preserve empty string distinct from null when loading json`() {
        logger.info("Confirming JSON null and empty string materialize distinctly")
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM JSON_VALUES WHERE COLUMN2 IS NULL").use { rs ->
                    rs.next()
                    check(rs.getInt(1) == 1) { "Expected exactly one NULL COLUMN2 (the JSON null row)" }
                }
                statement.executeQuery("SELECT COUNT(*) FROM JSON_VALUES WHERE COLUMN2 = ''").use { rs ->
                    rs.next()
                    check(rs.getInt(1) == 1) { "Expected exactly one empty-string COLUMN2 (the JSON \"\" row)" }
                }
            }
        }
        logger.info("JSON empty-string preservation confirmed")
    }
}
