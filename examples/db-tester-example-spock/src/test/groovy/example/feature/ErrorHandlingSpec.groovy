package example.feature

import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Demonstrates assertion failure scenarios and error message quality in db-tester with Spock.
 *
 * <p>This specification shows how {@link DatabaseAssertion} handles various failure scenarios and what
 * error messages are produced. These tests serve as documentation for expected behavior when:
 *
 * <ul>
 *   <li>Row counts differ between expected and actual data
 *   <li>Cell values do not match
 *   <li>Column structures are different
 * </ul>
 *
 * <p>Understanding these error scenarios helps users diagnose and fix issues in their test setups.
 * Each test intentionally triggers an assertion failure to demonstrate the error message format.
 *
 * <p><strong>Note:</strong> These tests do not use database operations.
 * They test programmatic assertions directly.
 */
class ErrorHandlingSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingSpec)

	def "should fail when expected has more rows than actual"() {
		given: 'expected table has 3 rows, actual has 2 rows'
		logger.info("Testing scenario: expected has more rows than actual")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")

		def row1 = new SimpleRow(Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")))
		def row2 = new SimpleRow(Map.of(columnId, new CellValue(2), columnValue, new CellValue("Two")))
		def row3 = new SimpleRow(Map.of(columnId, new CellValue(3), columnValue, new CellValue("Three")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(row1, row2, row3))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(row1, row2))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for more expected rows: {}", exception.message)
		exception.message != null && !exception.message.empty
	}

	def "should fail when actual has more rows than expected"() {
		given: 'expected table has 1 row, actual has 2 rows'
		logger.info("Testing scenario: actual has more rows than expected")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")

		def row1 = new SimpleRow(Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")))
		def row2 = new SimpleRow(Map.of(columnId, new CellValue(2), columnValue, new CellValue("Two")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(row1))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(row1, row2))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for more actual rows: {}", exception.message)
	}

	def "should fail when string values differ"() {
		given: 'expected and actual have different string values'
		logger.info("Testing scenario: string value mismatch")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")

		def expectedRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, new CellValue("ExpectedValue")))

		def actualRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, new CellValue("ActualValue")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(expectedRow))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(actualRow))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for string mismatch: {}", exception.message)
	}

	def "should fail when numeric values differ"() {
		given: 'expected and actual have different numeric values'
		logger.info("Testing scenario: numeric value mismatch")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")

		def expectedRow = new SimpleRow(
				Map.of(columnId, new CellValue(100), columnValue, new CellValue("Test")))

		def actualRow = new SimpleRow(
				Map.of(columnId, new CellValue(999), columnValue, new CellValue("Test")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(expectedRow))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(actualRow))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for numeric mismatch: {}", exception.message)
	}

	def "should fail when null vs non-null values differ"() {
		given: 'expected has null, actual has value'
		logger.info("Testing scenario: null vs non-null mismatch")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")

		def expectedRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, CellValue.NULL))

		def actualRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, new CellValue("NotNull")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(expectedRow))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(actualRow))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for null mismatch: {}", exception.message)
	}

	def "should fail when expected has extra columns"() {
		given: 'expected table has more columns than actual'
		logger.info("Testing scenario: expected has extra columns")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")
		def columnExtra = new ColumnName("EXTRA")

		def expectedRow = new SimpleRow(
				Map.of(
				columnId, new CellValue(1),
				columnValue, new CellValue("One"),
				columnExtra, new CellValue("ExtraData")))

		def actualRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, new CellValue("One")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue, columnExtra),
				List.of(expectedRow))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(actualRow))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for extra columns: {}", exception.message)
	}

	def "should fail when column names differ"() {
		given: 'expected and actual have different column names'
		logger.info("Testing scenario: column names differ")

		def columnId = new ColumnName("ID")
		def columnValue = new ColumnName("NAME")
		def columnDescription = new ColumnName("DESCRIPTION")

		def expectedRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnDescription, new CellValue("Desc")))

		def actualRow = new SimpleRow(
				Map.of(columnId, new CellValue(1), columnValue, new CellValue("Desc")))

		def expectedTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnDescription),
				List.of(expectedRow))

		def actualTable = new SimpleTable(
				new TableName("TEST_TABLE"),
				List.of(columnId, columnValue),
				List.of(actualRow))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown'
		def exception = thrown(AssertionError)
		logger.info("Error message for column name mismatch: {}", exception.message)
	}

	def "should provide sufficient context in error messages"() {
		given: 'multiple rows where only the third row differs'
		logger.info("Testing error message quality")

		def columnId = new ColumnName("ID")
		def columnName = new ColumnName("NAME")
		def columnStatus = new ColumnName("STATUS")

		def row1 = new SimpleRow(
				Map.of(
				columnId, new CellValue(1),
				columnName, new CellValue("Alice"),
				columnStatus, new CellValue("ACTIVE")))

		def row2 = new SimpleRow(
				Map.of(
				columnId, new CellValue(2),
				columnName, new CellValue("Bob"),
				columnStatus, new CellValue("ACTIVE")))

		def expectedRow3 = new SimpleRow(
				Map.of(
				columnId, new CellValue(3),
				columnName, new CellValue("Charlie"),
				columnStatus, new CellValue("INACTIVE")))

		def actualRow3 = new SimpleRow(
				Map.of(
				columnId, new CellValue(3),
				columnName, new CellValue("Charlie"),
				columnStatus, new CellValue("ACTIVE")))

		def expectedTable = new SimpleTable(
				new TableName("USER_STATUS_TABLE"),
				List.of(columnId, columnName, columnStatus),
				List.of(row1, row2, expectedRow3))

		def actualTable = new SimpleTable(
				new TableName("USER_STATUS_TABLE"),
				List.of(columnId, columnName, columnStatus),
				List.of(row1, row2, actualRow3))

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion error is thrown with useful context'
		def exception = thrown(AssertionError)
		logger.info("Full error message demonstrating context:\n{}", exception.message)
		exception.message != null && exception.message.length() > 0
	}
}
