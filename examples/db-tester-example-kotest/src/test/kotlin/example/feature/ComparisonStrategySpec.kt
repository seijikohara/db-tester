package example.feature

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates different comparison strategies for database assertions with Kotest.
 *
 * This specification demonstrates the available comparison strategies:
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.STRICT] - Exact match using equals() (default)
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.IGNORE] - Skip comparison entirely
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.NUMERIC] - Type-aware numeric comparison
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.CASE_INSENSITIVE] - Case-insensitive string comparison
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.TIMESTAMP_FLEXIBLE] - Flexible timestamp comparison
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.DATE_FLEXIBLE] - Flexible date format comparison
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.JSON_EQUIVALENT] - JSON structural comparison
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.NOT_NULL] - Only verify the value is not null
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.regex] - Match against a regular expression
 */
@DatabaseTest
class ComparisonStrategySpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(ComparisonStrategySpec::class.java)

        private const val EMAIL_PATTERN = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}"

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ComparisonStrategySpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ComparisonStrategySpec::class.java.classLoader.getResource(scriptPath)
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

        /**
         * Creates a table with one row.
         *
         * @param tableName the table name
         * @param columnNames the column names
         * @param values the row values (corresponding to columns)
         * @return a Table instance
         */
        private fun createTable(
            tableName: String,
            columnNames: List<String>,
            vararg values: Any?,
        ): Table =
            columnNames
                .map { ColumnName(it) }
                .let { columns ->
                    columns
                        .mapIndexed { index, column ->
                            column to CellValue(values.getOrNull(index))
                        }.toMap()
                        .let { rowValues -> Row.of(rowValues) }
                        .let { row ->
                            Table.of(TableName(tableName), columns, listOf(row))
                        }
                }
    }

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for ComparisonStrategySpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ComparisonStrategySpec.sql")
            logger.info("Database setup completed")
        }

    // ==================== STRICT Strategy Tests ====================

    @Test
    fun `strict strategy should pass when values match exactly`(): Unit =
        DatabaseAssertion.assertEquals(
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice"),
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice"),
        )

    @Test
    fun `strict strategy should fail when values differ`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEquals(
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice"),
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "ALICE"),
            )
        }.let { }

    // ==================== NUMERIC Strategy Tests ====================

    @Test
    fun `numeric strategy should match across numeric types when applied`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, 100),
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("100.00")),
            ColumnStrategyMapping.numeric("AMOUNT"),
        )

    @Test
    fun `numeric strategy should match scaled decimals when applied`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("99.99")),
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("99.990")),
            ColumnStrategyMapping.numeric("AMOUNT"),
        )

    @Test
    fun `numeric strategy should fail when values are numerically distinct`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("99.99")),
                createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("100.00")),
                ColumnStrategyMapping.numeric("AMOUNT"),
            )
        }.let { }

    // ==================== CASE_INSENSITIVE Strategy Tests ====================

    @Test
    fun `default STRICT comparison should fail on case difference`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEquals(
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "alice"),
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "ALICE"),
            )
        }.let { }

    @Test
    fun `case-insensitive strategy should match across letter cases when applied`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "alice"),
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "ALICE"),
            ColumnStrategyMapping.caseInsensitive("NAME"),
        )

    @Test
    fun `case-insensitive strategy should fail when text differs`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "alice"),
                createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "bob"),
                ColumnStrategyMapping.caseInsensitive("NAME"),
            )
        }.let { }

    // ==================== IGNORE Strategy Tests ====================

    @Test
    fun `ignore strategy should skip comparison for ignored columns`(): Unit =
        DatabaseAssertion.assertEqualsIgnoreColumns(
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-01-01"),
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-12-31"),
            "TIMESTAMP",
        )

    // ==================== NOT_NULL Strategy Tests ====================

    @Test
    fun `not-null strategy should accept any non-null actual when applied`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "any-placeholder"),
            createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "0xFEED-CAFE-1234"),
            ColumnStrategyMapping.notNull("GENERATED_ID"),
        )

    @Test
    fun `not-null strategy should fail when actual is null`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "any-placeholder"),
                createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, null),
                ColumnStrategyMapping.notNull("GENERATED_ID"),
            )
        }.let { }

    // ==================== TIMESTAMP_FLEXIBLE Strategy Tests ====================

    @Test
    fun `timestamp-flexible strategy should match timestamps with different sub-second precision`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00.000"),
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00"),
            ColumnStrategyMapping.timestampFlexible("TIMESTAMP"),
        )

    @Test
    fun `timestamp-flexible strategy should fail when timestamps refer to different dates`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00"),
                createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-07-15T10:30:00"),
                ColumnStrategyMapping.timestampFlexible("TIMESTAMP"),
            )
        }.let { }

    @Test
    fun `timestamp-flexible strategy should match same instant across timezone offsets`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00+09:00"),
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T01:30:00Z"),
            ColumnStrategyMapping.timestampFlexible("TIMESTAMP"),
        )

    @Test
    fun `timestamp-flexible strategy should fail when offsets shift instant`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00+09:00"),
                createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-06-15T10:30:00Z"),
                ColumnStrategyMapping.timestampFlexible("TIMESTAMP"),
            )
        }.let { }

    // ==================== DATE_FLEXIBLE Strategy Tests ====================

    @Test
    fun `date-flexible strategy should match ISO and slash formats`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024-06-15"),
            createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024/06/15"),
            ColumnStrategyMapping.dateFlexible("BIRTH_DATE"),
        )

    @Test
    fun `date-flexible strategy should match dot-delimited date format`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024-06-15"),
            createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024.06.15"),
            ColumnStrategyMapping.dateFlexible("BIRTH_DATE"),
        )

    @Test
    fun `date-flexible strategy should fail when dates differ`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024-06-15"),
                createTable("COMPARISON_TEST", listOf("ID", "BIRTH_DATE"), 1, "2024-07-20"),
                ColumnStrategyMapping.dateFlexible("BIRTH_DATE"),
            )
        }.let { }

    // ==================== JSON_EQUIVALENT Strategy Tests ====================

    @Test
    fun `json-equivalent strategy should match JSON with different key order`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"name": "Alice", "age": 30}"""),
            createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"age": 30, "name": "Alice"}"""),
            ColumnStrategyMapping.jsonEquivalent("METADATA"),
        )

    @Test
    fun `json-equivalent strategy should match nested JSON when keys are reordered`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable(
                "COMPARISON_TEST",
                listOf("ID", "METADATA"),
                1,
                """{"user":{"name":"Alice","roles":["admin","user"]}}""",
            ),
            createTable(
                "COMPARISON_TEST",
                listOf("ID", "METADATA"),
                1,
                """{"user":{"roles":["admin","user"],"name":"Alice"}}""",
            ),
            ColumnStrategyMapping.jsonEquivalent("METADATA"),
        )

    @Test
    fun `json-equivalent strategy should fail when nested array order differs`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"tags":["alpha","beta","gamma"]}"""),
                createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"tags":["gamma","alpha","beta"]}"""),
                ColumnStrategyMapping.jsonEquivalent("METADATA"),
            )
        }.let { }

    @Test
    fun `json-equivalent strategy should fail when JSON values differ`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"name": "Alice", "age": 30}"""),
                createTable("COMPARISON_TEST", listOf("ID", "METADATA"), 1, """{"name": "Bob", "age": 25}"""),
                ColumnStrategyMapping.jsonEquivalent("METADATA"),
            )
        }.let { }

    // ==================== REGEX Strategy Tests ====================

    @Test
    fun `regex strategy should match actual against pattern when applied`(): Unit =
        DatabaseAssertion.assertEqualsWithStrategies(
            createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "<PATTERN_EMAIL>"),
            createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "alice@example.com"),
            ColumnStrategyMapping.regex("EMAIL", EMAIL_PATTERN),
        )

    @Test
    fun `regex strategy should fail when actual does not match pattern`(): Unit =
        shouldThrow<AssertionError> {
            DatabaseAssertion.assertEqualsWithStrategies(
                createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "<PATTERN_EMAIL>"),
                createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "invalid-email"),
                ColumnStrategyMapping.regex("EMAIL", EMAIL_PATTERN),
            )
        }.let { }
}
