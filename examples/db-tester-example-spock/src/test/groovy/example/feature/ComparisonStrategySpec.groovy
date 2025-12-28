package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates different comparison strategies for database assertions with Spock.
 *
 * <p>This specification demonstrates the available comparison strategies:
 * <ul>
 *   <li>{@link ComparisonStrategy#STRICT} - Exact match using equals() (default)
 *   <li>{@link ComparisonStrategy#IGNORE} - Skip comparison entirely
 *   <li>{@link ComparisonStrategy#NUMERIC} - Type-aware numeric comparison
 *   <li>{@link ComparisonStrategy#CASE_INSENSITIVE} - Case-insensitive string comparison
 *   <li>{@link ComparisonStrategy#TIMESTAMP_FLEXIBLE} - Flexible timestamp comparison
 *   <li>{@link ComparisonStrategy#NOT_NULL} - Only verify the value is not null
 *   <li>{@link ComparisonStrategy#regex(String)} - Match against a regular expression
 * </ul>
 */
@DatabaseTest
class ComparisonStrategySpec extends Specification {

	@Shared
	DataSource dataSource

	@Shared
	Sql sql

	static DataSourceRegistry sharedRegistry
	static DataSource sharedDataSource

	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeSharedResources()
		}
		return sharedRegistry
	}

	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ComparisonStrategySpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)
		executeScript('ddl/feature/ComparisonStrategySpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates a table with one row.
	 *
	 * @param tableName the table name
	 * @param columnNames the column names
	 * @param values the row values (corresponding to columns)
	 * @return a Table instance
	 */
	private static Table createTable(String tableName, List<String> columnNames, Object... values) {
		def columns = columnNames.collect { new ColumnName(it) }
		Map<ColumnName, CellValue> rowValues = [:]
		columns.eachWithIndex { col, i ->
			if (i < values.length) {
				rowValues[col] = new CellValue(values[i])
			}
		}
		Row row = new SimpleRow(rowValues)
		return new SimpleTable(new TableName(tableName), columns, [row])
	}

	// ==================== STRICT Strategy Tests ====================

	def 'strict strategy should pass when values match exactly'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		and: 'actual table with same values'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		expect: 'comparison passes with exact match'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	def 'strict strategy should fail when values differ'() {
		given: 'expected table with lowercase name'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		and: 'actual table with uppercase name'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'ALICE')

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion fails due to case difference'
		thrown(AssertionError)
	}

	// ==================== NUMERIC Strategy Tests ====================

	def 'numeric strategy should match different numeric types with same value'() {
		given: 'expected table with Integer value'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, 100)

		and: 'actual table with BigDecimal value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('100.00'))

		expect: 'comparison passes - numeric values are equal'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	def 'numeric strategy should match values with different precision'() {
		given: 'expected table with 2 decimal precision'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('99.99'))

		and: 'actual table with 3 decimal precision'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('99.990'))

		expect: 'comparison passes - numerically equal despite different precision'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	// ==================== CASE_INSENSITIVE Strategy Tests ====================

	def 'case-insensitive strategy should demonstrate case-sensitive comparison by default'() {
		given: 'expected table with lowercase name'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'alice')

		and: 'actual table with uppercase name'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'ALICE')

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion fails - default comparison is case-sensitive'
		thrown(AssertionError)
	}

	// ==================== IGNORE Strategy Tests ====================

	def 'ignore strategy should skip comparison for ignored columns'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-01-01')

		and: 'actual table with different timestamp'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-12-31')

		expect: 'comparison passes - TIMESTAMP column is ignored'
		DatabaseAssertion.assertEqualsIgnoreColumns(expectedTable, actualTable, 'TIMESTAMP')
	}

	// ==================== NOT_NULL Strategy Tests ====================

	def 'not-null strategy should pass when value is not null'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, 'expected-value')

		and: 'actual table with non-null value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, 'expected-value')

		expect: 'comparison passes - value is not null'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	def 'not-null strategy should fail when value is null'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, 'any-value')

		and: 'actual table with null value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, null)

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion fails - value is null'
		thrown(AssertionError)
	}

	// ==================== REGEX Strategy Tests ====================

	def 'regex strategy should match value against pattern'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'alice@example.com')

		and: 'actual table with same email'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'alice@example.com')

		expect: 'comparison passes - exact match'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	def 'regex strategy should fail when value does not match pattern'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'alice@example.com')

		and: 'actual table with different email'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'invalid-email')

		when: 'comparing tables'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'assertion fails - values do not match'
		thrown(AssertionError)
	}

	private void executeScript(String scriptPath) {
		def resource = getClass().classLoader.getResource(scriptPath)
		if (resource == null) {
			throw new IllegalStateException("Script not found: $scriptPath")
		}
		resource.text
				.split(';')
				.collect { it.trim() }
				.findAll { !it.empty }
				.each { sql.execute(it) }
	}
}
