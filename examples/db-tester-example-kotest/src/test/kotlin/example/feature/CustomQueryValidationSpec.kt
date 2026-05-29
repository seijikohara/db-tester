package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Date
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates database testing with SQL query result validation using Kotest.
 *
 * Each test prepares baseline data through [DataSet], runs additional SQL that exercises the
 * feature under test, then verifies the result of a SQL query (rather than the full table state)
 * using [DatabaseQueryAssertion.assertEqualsByQuery].
 *
 * This specification demonstrates four query patterns:
 * - Filtering rows via a `WHERE` clause
 * - Aggregating rows via `GROUP BY` with `SUM` and `COUNT`
 * - Joining two tables via `INNER JOIN`
 * - Restricting rows to a date range via `BETWEEN`
 */
@DatabaseTest
class CustomQueryValidationSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(CustomQueryValidationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:CustomQueryValidationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                CustomQueryValidationSpec::class.java.classLoader.getResource(scriptPath)
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

        /**
         * Builds an in-memory Table for use as an expected query result.
         *
         * @param tableName the logical table name attached to the query result
         * @param columnNames the column names in order
         * @param rowValues each inner list represents the cell values of one row, in column order
         * @return a Table representing the expected query result
         */
        private fun createTable(
            tableName: String,
            columnNames: List<String>,
            rowValues: List<List<Any?>>,
        ): Table =
            columnNames.map { ColumnName(it) }.let { columns ->
                rowValues
                    .map { values ->
                        columns
                            .mapIndexedNotNull { index, column ->
                                values.getOrNull(index)?.let { column to CellValue(it) }
                            }.toMap()
                            .let { rowMap -> Row.of(rowMap) }
                    }.let { rows -> Table.of(TableName(tableName), columns, rows) }
            }
    }

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for CustomQueryValidationSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/CustomQueryValidationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies that a WHERE filter returns only the matching rows after an insert.
     */
    @Test
    @DataSet
    fun `should return only East-region rows when WHERE filter applied`() {
        executeSql(
            dataSource,
            "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)" +
                " VALUES (4, 3, '2024-01-25', 350.00, 'East')",
        )

        val expected =
            createTable(
                "TABLE1",
                listOf("ID", "COLUMN1", "COLUMN2", "COLUMN3", "COLUMN4"),
                listOf(
                    listOf(2, 2, Date.valueOf("2024-01-15"), BigDecimal("300.00"), "East"),
                    listOf(4, 3, Date.valueOf("2024-01-25"), BigDecimal("350.00"), "East"),
                ),
            )

        DatabaseQueryAssertion.assertEqualsByQuery(
            expected,
            dataSource,
            "TABLE1",
            "SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1" +
                " WHERE COLUMN4 = 'East' ORDER BY ID",
        )
    }

    /**
     * Verifies that a GROUP BY aggregation returns the expected per-category totals.
     *
     * TableReader currently uses ResultSetMetaData#getColumnName, which ignores SQL aliases on
     * plain column references. Plain columns are projected without an alias so the expected
     * column name matches the underlying table column.
     */
    @Test
    @DataSet
    fun `should return aggregated totals per category when GROUP BY applied`() {
        executeSql(
            dataSource,
            "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)" +
                " VALUES (4, 1, '2024-01-25', 500.00, 'West')",
        )

        val expected =
            createTable(
                "CATEGORY_TOTALS",
                listOf("COLUMN1", "TOTAL_AMOUNT", "RECORD_COUNT"),
                listOf(
                    listOf(1, BigDecimal("1700.00"), 3L),
                    listOf(2, BigDecimal("300.00"), 1L),
                ),
            )

        DatabaseQueryAssertion.assertEqualsByQuery(
            expected,
            dataSource,
            "CATEGORY_TOTALS",
            "SELECT COLUMN1, SUM(COLUMN3) AS TOTAL_AMOUNT, COUNT(*) AS RECORD_COUNT" +
                " FROM TABLE1 GROUP BY COLUMN1 ORDER BY COLUMN1",
        )
    }

    /**
     * Verifies that an INNER JOIN between sales and categories returns labeled rows.
     *
     * TableReader currently uses ResultSetMetaData#getColumnName, which ignores SQL aliases on
     * plain column references. Columns are projected without aliases so the expected column
     * names mirror the underlying table columns.
     */
    @Test
    @DataSet
    fun `should return joined sale and category rows when INNER JOIN applied`() {
        executeSql(
            dataSource,
            "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)" +
                " VALUES (4, 1, '2024-02-01', 600.00, 'North')",
        )

        val expected =
            createTable(
                "SALES_WITH_CATEGORY",
                listOf("ID", "NAME", "COLUMN3"),
                listOf(
                    listOf(1, "Premium", BigDecimal("500.00")),
                    listOf(2, "Standard", BigDecimal("300.00")),
                    listOf(3, "Premium", BigDecimal("700.00")),
                    listOf(4, "Premium", BigDecimal("600.00")),
                ),
            )

        DatabaseQueryAssertion.assertEqualsByQuery(
            expected,
            dataSource,
            "SALES_WITH_CATEGORY",
            "SELECT s.ID, c.NAME, s.COLUMN3" +
                " FROM TABLE1 s INNER JOIN CATEGORIES c ON s.COLUMN1 = c.ID" +
                " ORDER BY s.ID",
        )
    }

    /**
     * Verifies that a BETWEEN date filter excludes rows outside the requested range.
     */
    @Test
    @DataSet
    fun `should return only January rows when BETWEEN date filter applied`() {
        executeSql(
            dataSource,
            "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)" +
                " VALUES (4, 2, '2024-01-25', 450.00, 'South')",
        )
        executeSql(
            dataSource,
            "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)" +
                " VALUES (5, 1, '2024-02-05', 800.00, 'North')",
        )

        val expected =
            createTable(
                "TABLE1",
                listOf("ID", "COLUMN1", "COLUMN2", "COLUMN3", "COLUMN4"),
                listOf(
                    listOf(1, 1, Date.valueOf("2024-01-10"), BigDecimal("500.00"), "West"),
                    listOf(2, 2, Date.valueOf("2024-01-15"), BigDecimal("300.00"), "East"),
                    listOf(3, 1, Date.valueOf("2024-01-20"), BigDecimal("700.00"), "North"),
                    listOf(4, 2, Date.valueOf("2024-01-25"), BigDecimal("450.00"), "South"),
                ),
            )

        DatabaseQueryAssertion.assertEqualsByQuery(
            expected,
            dataSource,
            "TABLE1",
            "SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1" +
                " WHERE COLUMN2 BETWEEN DATE '2024-01-01' AND DATE '2024-01-31'" +
                " ORDER BY ID",
        )
    }
}
