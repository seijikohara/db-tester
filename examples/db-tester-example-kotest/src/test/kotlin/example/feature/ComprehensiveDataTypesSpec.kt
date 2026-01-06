package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates comprehensive data type coverage in CSV files using Kotest.
 *
 * This specification shows all CSV-representable H2 data types:
 * - Integer types: TINYINT, SMALLINT, INTEGER, BIGINT
 * - Decimal types: DECIMAL, NUMERIC
 * - Floating point: REAL, FLOAT, DOUBLE, DOUBLE PRECISION
 * - Character types: CHAR, VARCHAR, VARCHAR_IGNORECASE, LONGVARCHAR, CLOB, TEXT
 * - Date/Time types: DATE, TIME, TIMESTAMP
 * - Boolean types: BOOLEAN, BIT
 * - Binary type: BLOB (Base64 encoded with `[BASE64]` prefix)
 * - UUID values (stored as VARCHAR for CSV compatibility)
 * - NULL value handling (empty column in CSV)
 *
 * CSV format examples:
 * ```
 * ID,TINYINT_COL,CHAR_COL,VARCHAR_COL,DATE_COL,BOOLEAN_COL,BLOB_COL,UUID_COL
 * 1,127,CHAR10,Sample Text,2024-01-15,true,[BASE64]VGVzdA==,550e8400-e29b-41d4-a716-446655440000
 * 2,,ABC,NULL Test,,false,,[null]
 * ```
 *
 * Note: CHAR columns are stored without space padding in CSV. NULL values are represented by
 * empty columns (nothing between commas).
 */
class ComprehensiveDataTypesSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ComprehensiveDataTypesSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ComprehensiveDataTypesSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ComprehensiveDataTypesSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for ComprehensiveDataTypesSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ComprehensiveDataTypesSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates handling of all H2 CSV-representable data types.
     *
     * Validates that CSV can represent all data types including integers, decimals, floating
     * points, character types, date/time, booleans, binary (BLOB with Base64), and UUIDs.
     *
     * Test flow:
     * - Preparation: Loads DATA_TYPES(ID=1,2) with comprehensive data type values
     * - Execution: Inserts ID=3 with all 24 data type columns (TINYINT to UUID)
     * - Expectation: Verifies all three records including BLOB (Base64) and CHAR (space-padded)
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should handle all data types`(): Unit =
        logger.info("Running comprehensive data types test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO DATA_TYPES (
                    ID,
                    TINYINT_COL, SMALLINT_COL, INT_COL, BIGINT_COL,
                    DECIMAL_COL, NUMERIC_COL,
                    REAL_COL, FLOAT_COL, DOUBLE_COL, DOUBLE_PRECISION_COL,
                    CHAR_COL, VARCHAR_COL, VARCHAR_IGNORECASE_COL, LONGVARCHAR_COL, CLOB_COL, TEXT_COL,
                    DATE_COL, TIME_COL, TIMESTAMP_COL,
                    BOOLEAN_COL, BIT_COL,
                    BLOB_COL,
                    UUID_COL
                ) VALUES (
                    3,
                    127, 32767, 999, 9999999999,
                    888.88, 12345.67890,
                    123.45, 456.78, 777.77, 888.88,
                    'NEWCHAR', 'New Value', 'CaseTest', 'Long variable text', 'CLOB content here', 'Text content',
                    '2024-12-31', '23:59:59', '2024-12-31 23:59:59',
                    false, false,
                    X'DEADBEEF',
                    '550e8400-e29b-41d4-a716-446655440099'
                )
                """.trimIndent(),
            )
            logger.info("All data types inserted successfully")
        }
}
