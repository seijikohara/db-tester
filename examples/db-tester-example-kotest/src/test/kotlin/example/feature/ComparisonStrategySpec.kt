package example.feature

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
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
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.NOT_NULL] - Only verify the value is not null
 * - [io.github.seijikohara.dbtester.api.domain.ComparisonStrategy.regex] - Match against a regular expression
 */
class ComparisonStrategySpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ComparisonStrategySpec::class.java)

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
                        .let { rowValues -> SimpleRow(rowValues) as Row }
                        .let { row ->
                            SimpleTable(TableName(tableName), columns, listOf(row))
                        }
                }
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
        logger.info("Setting up H2 in-memory database for ComparisonStrategySpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ComparisonStrategySpec.sql")
            logger.info("Database setup completed")
        }

    // ==================== STRICT Strategy Tests ====================

    /**
     * Verifies that strict strategy passes when values match exactly.
     */
    @Test
    fun `strict strategy should pass when values match exactly`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice").let { actualTable ->
                DatabaseAssertion.assertEquals(expectedTable, actualTable)
            }
        }

    /**
     * Verifies that strict strategy fails when values differ.
     */
    @Test
    fun `strict strategy should fail when values differ`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "Alice").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "ALICE").let { actualTable ->
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            }
        }

    // ==================== NUMERIC Strategy Tests ====================

    /**
     * Verifies that numeric strategy matches different numeric types with same value.
     */
    @Test
    fun `numeric strategy should match different numeric types with same value`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, 100).let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("100.00")).let { actualTable ->
                DatabaseAssertion.assertEquals(expectedTable, actualTable)
            }
        }

    /**
     * Verifies that numeric strategy matches values with different precision.
     */
    @Test
    fun `numeric strategy should match values with different precision`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("99.99")).let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "AMOUNT"), 1, BigDecimal("99.990")).let { actualTable ->
                DatabaseAssertion.assertEquals(expectedTable, actualTable)
            }
        }

    // ==================== CASE_INSENSITIVE Strategy Tests ====================

    /**
     * Verifies that case-insensitive strategy demonstrates case-sensitive comparison by default.
     */
    @Test
    fun `case-insensitive strategy should demonstrate case-sensitive comparison by default`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "alice").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "NAME"), 1, "ALICE").let { actualTable ->
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            }
        }

    // ==================== IGNORE Strategy Tests ====================

    /**
     * Verifies that ignore strategy skips comparison for ignored columns.
     */
    @Test
    fun `ignore strategy should skip comparison for ignored columns`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-01-01").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "TIMESTAMP"), 1, "2024-12-31").let { actualTable ->
                DatabaseAssertion.assertEqualsIgnoreColumns(expectedTable, actualTable, "TIMESTAMP")
            }
        }

    // ==================== NOT_NULL Strategy Tests ====================

    /**
     * Verifies that not-null strategy passes when value is not null.
     */
    @Test
    fun `not-null strategy should pass when value is not null`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "expected-value").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "expected-value").let { actualTable ->
                DatabaseAssertion.assertEquals(expectedTable, actualTable)
            }
        }

    /**
     * Verifies that not-null strategy fails when value is null.
     */
    @Test
    fun `not-null strategy should fail when value is null`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, "any-value").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "GENERATED_ID"), 1, null).let { actualTable ->
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            }
        }

    // ==================== REGEX Strategy Tests ====================

    /**
     * Verifies that regex strategy matches value against pattern.
     */
    @Test
    fun `regex strategy should match value against pattern`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "alice@example.com").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "alice@example.com").let { actualTable ->
                DatabaseAssertion.assertEquals(expectedTable, actualTable)
            }
        }

    /**
     * Verifies that regex strategy fails when value does not match pattern.
     */
    @Test
    fun `regex strategy should fail when value does not match pattern`(): Unit =
        createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "alice@example.com").let { expectedTable ->
            createTable("COMPARISON_TEST", listOf("ID", "EMAIL"), 1, "invalid-email").let { actualTable ->
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            }
        }
}
