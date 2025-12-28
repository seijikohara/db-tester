package example.feature

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.slf4j.LoggerFactory

/**
 * Demonstrates assertion failure scenarios and error message quality in db-tester with Kotest.
 *
 * This specification shows how [DatabaseAssertion] handles various failure scenarios and what
 * error messages are produced. These tests serve as documentation for expected behavior when:
 *
 * - Row counts differ between expected and actual data
 * - Cell values do not match
 * - Column structures are different
 *
 * Understanding these error scenarios helps users diagnose and fix issues in their test setups.
 * Each test intentionally triggers an assertion failure to demonstrate the error message format.
 *
 * Note: These tests do not use database operations.
 * They test programmatic assertions directly.
 */
class ErrorHandlingSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ErrorHandlingSpec::class.java)
    }

    private val registry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Verifies that an assertion error is thrown when expected has more rows than actual.
     *
     * This test creates an expected table with 3 rows and an actual table with 2 rows,
     * demonstrating the row count mismatch detection.
     */
    @Test
    fun `should fail when expected has more rows than actual`(): Unit =
        logger.info("Testing scenario: expected has more rows than actual").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")

            val row1 = SimpleRow(mapOf(columnId to CellValue(1), columnValue to CellValue("One")))
            val row2 = SimpleRow(mapOf(columnId to CellValue(2), columnValue to CellValue("Two")))
            val row3 = SimpleRow(mapOf(columnId to CellValue(3), columnValue to CellValue("Three")))

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(row1, row2, row3),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(row1, row2),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for more expected rows: {}", exception.message)
            exception.message shouldNotBe null
            exception.message!!.shouldNotBeEmpty()
        }

    /**
     * Verifies that an assertion error is thrown when actual has more rows than expected.
     *
     * This test creates an expected table with 1 row and an actual table with 2 rows,
     * demonstrating the row count mismatch detection in the opposite direction.
     */
    @Test
    fun `should fail when actual has more rows than expected`(): Unit =
        logger.info("Testing scenario: actual has more rows than expected").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")

            val row1 = SimpleRow(mapOf(columnId to CellValue(1), columnValue to CellValue("One")))
            val row2 = SimpleRow(mapOf(columnId to CellValue(2), columnValue to CellValue("Two")))

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(row1),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(row1, row2),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for more actual rows: {}", exception.message)
        }

    /**
     * Verifies that an assertion error is thrown when string values differ.
     *
     * This test creates tables with different string values in the NAME column,
     * demonstrating the string value mismatch detection.
     */
    @Test
    fun `should fail when string values differ`(): Unit =
        logger.info("Testing scenario: string value mismatch").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")

            val expectedRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue("ExpectedValue")),
                )

            val actualRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue("ActualValue")),
                )

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(expectedRow),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(actualRow),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for string mismatch: {}", exception.message)
        }

    /**
     * Verifies that an assertion error is thrown when numeric values differ.
     *
     * This test creates tables with different numeric values in the ID column,
     * demonstrating the numeric value mismatch detection.
     */
    @Test
    fun `should fail when numeric values differ`(): Unit =
        logger.info("Testing scenario: numeric value mismatch").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")

            val expectedRow =
                SimpleRow(
                    mapOf(columnId to CellValue(100), columnValue to CellValue("Test")),
                )

            val actualRow =
                SimpleRow(
                    mapOf(columnId to CellValue(999), columnValue to CellValue("Test")),
                )

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(expectedRow),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(actualRow),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for numeric mismatch: {}", exception.message)
        }

    /**
     * Verifies that an assertion error is thrown when null vs non-null values differ.
     *
     * This test creates an expected table with a NULL value and an actual table with a non-null value,
     * demonstrating the null value mismatch detection.
     */
    @Test
    fun `should fail when null vs non-null values differ`(): Unit =
        logger.info("Testing scenario: null vs non-null mismatch").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")

            val expectedRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue.NULL),
                )

            val actualRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue("NotNull")),
                )

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(expectedRow),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(actualRow),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for null mismatch: {}", exception.message)
        }

    /**
     * Verifies that an assertion error is thrown when expected has extra columns.
     *
     * This test creates an expected table with more columns than the actual table,
     * demonstrating the column structure mismatch detection.
     */
    @Test
    fun `should fail when expected has extra columns`(): Unit =
        logger.info("Testing scenario: expected has extra columns").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")
            val columnExtra = ColumnName("EXTRA")

            val expectedRow =
                SimpleRow(
                    mapOf(
                        columnId to CellValue(1),
                        columnValue to CellValue("One"),
                        columnExtra to CellValue("ExtraData"),
                    ),
                )

            val actualRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue("One")),
                )

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue, columnExtra),
                    listOf(expectedRow),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(actualRow),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for extra columns: {}", exception.message)
        }

    /**
     * Verifies that an assertion error is thrown when column names differ.
     *
     * This test creates tables with different column names (DESCRIPTION vs NAME),
     * demonstrating the column name mismatch detection.
     */
    @Test
    fun `should fail when column names differ`(): Unit =
        logger.info("Testing scenario: column names differ").also {
            val columnId = ColumnName("ID")
            val columnValue = ColumnName("NAME")
            val columnDescription = ColumnName("DESCRIPTION")

            val expectedRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnDescription to CellValue("Desc")),
                )

            val actualRow =
                SimpleRow(
                    mapOf(columnId to CellValue(1), columnValue to CellValue("Desc")),
                )

            val expectedTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnDescription),
                    listOf(expectedRow),
                )

            val actualTable =
                SimpleTable(
                    TableName("TEST_TABLE"),
                    listOf(columnId, columnValue),
                    listOf(actualRow),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Error message for column name mismatch: {}", exception.message)
        }

    /**
     * Verifies that error messages provide sufficient context for debugging.
     *
     * This test creates multiple rows where only the third row differs,
     * demonstrating that error messages include enough context to identify the specific mismatch.
     */
    @Test
    fun `should provide sufficient context in error messages`(): Unit =
        logger.info("Testing error message quality").also {
            val columnId = ColumnName("ID")
            val columnName = ColumnName("NAME")
            val columnStatus = ColumnName("STATUS")

            val row1 =
                SimpleRow(
                    mapOf(
                        columnId to CellValue(1),
                        columnName to CellValue("Alice"),
                        columnStatus to CellValue("ACTIVE"),
                    ),
                )

            val row2 =
                SimpleRow(
                    mapOf(
                        columnId to CellValue(2),
                        columnName to CellValue("Bob"),
                        columnStatus to CellValue("ACTIVE"),
                    ),
                )

            val expectedRow3 =
                SimpleRow(
                    mapOf(
                        columnId to CellValue(3),
                        columnName to CellValue("Charlie"),
                        columnStatus to CellValue("INACTIVE"),
                    ),
                )

            val actualRow3 =
                SimpleRow(
                    mapOf(
                        columnId to CellValue(3),
                        columnName to CellValue("Charlie"),
                        columnStatus to CellValue("ACTIVE"),
                    ),
                )

            val expectedTable =
                SimpleTable(
                    TableName("USER_STATUS_TABLE"),
                    listOf(columnId, columnName, columnStatus),
                    listOf(row1, row2, expectedRow3),
                )

            val actualTable =
                SimpleTable(
                    TableName("USER_STATUS_TABLE"),
                    listOf(columnId, columnName, columnStatus),
                    listOf(row1, row2, actualRow3),
                )

            val exception =
                shouldThrow<AssertionError> {
                    DatabaseAssertion.assertEquals(expectedTable, actualTable)
                }
            logger.info("Full error message demonstrating context:\n{}", exception.message)
            exception.message shouldNotBe null
            exception.message!!.isNotEmpty()
        }
}
