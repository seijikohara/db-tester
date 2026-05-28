package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping
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
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
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
 *   <li>{@link ComparisonStrategy#DATE_FLEXIBLE} - Flexible date format comparison
 *   <li>{@link ComparisonStrategy#JSON_EQUIVALENT} - JSON structural comparison
 *   <li>{@link ComparisonStrategy#NOT_NULL} - Only verify the value is not null
 *   <li>{@link ComparisonStrategy#regex(String)} - Match against a regular expression
 * </ul>
 */
@DatabaseTest
class ComparisonStrategySpec extends Specification implements DatabaseTestSupport {

	/** Email validation pattern used by the REGEX strategy tests. */
	private static final String EMAIL_PATTERN = '[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}'

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/** Static registry shared across all features. */
	static DataSourceRegistry sharedRegistry

	/** Static DataSource shared across all features. */
	static DataSource sharedDataSource

	/**
	 * Gets the DataSourceRegistry (Groovy property accessor).
	 *
	 * @return the registry
	 */
	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeSharedResources()
		}
		return sharedRegistry
	}

	/**
	 * Initializes shared resources.
	 */
	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ComparisonStrategySpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database and schema.
	 */
	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)
		executeScript('ddl/feature/ComparisonStrategySpec.sql')
	}

	/**
	 * Closes the SQL helper.
	 */
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
		given: 'expected table with name Alice'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		and: 'actual table with the same values'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		expect: 'STRICT comparison passes on exact match'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)
	}

	def 'strict strategy should fail when values differ'() {
		given: 'expected table with lowercase name'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'Alice')

		and: 'actual table with uppercase name'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'ALICE')

		when: 'comparing tables with STRICT'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'STRICT rejects the case difference'
		thrown(AssertionError)
	}

	// ==================== NUMERIC Strategy Tests ====================

	def 'numeric strategy should match across numeric types when applied'() {
		given: 'expected table with Integer value'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, 100)

		and: 'actual table with BigDecimal value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('100.00'))

		expect: 'NUMERIC strategy treats integer and decimal as equal'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.numeric('AMOUNT'))
	}

	def 'numeric strategy should match scaled decimals when applied'() {
		given: 'expected table with two-digit precision'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('99.99'))

		and: 'actual table with three-digit precision'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('99.990'))

		expect: 'NUMERIC strategy ignores trailing zero scale'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.numeric('AMOUNT'))
	}

	def 'numeric strategy should fail when values are numerically distinct'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('99.99'))

		and: 'actual table with a different numeric value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'AMOUNT'], 1, new BigDecimal('100.00'))

		when: 'comparing with NUMERIC strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.numeric('AMOUNT'))

		then: 'NUMERIC strategy rejects distinct values'
		thrown(AssertionError)
	}

	// ==================== CASE_INSENSITIVE Strategy Tests ====================

	def 'default STRICT comparison should fail on case difference'() {
		given: 'expected table with lowercase name'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'alice')

		and: 'actual table with uppercase name'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'ALICE')

		when: 'comparing with default STRICT'
		DatabaseAssertion.assertEquals(expectedTable, actualTable)

		then: 'STRICT default rejects case difference'
		thrown(AssertionError)
	}

	def 'case-insensitive strategy should match across letter cases when applied'() {
		given: 'expected table with lowercase name'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'alice')

		and: 'actual table with uppercase name'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'ALICE')

		expect: 'CASE_INSENSITIVE strategy ignores letter case'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.caseInsensitive('NAME'))
	}

	def 'case-insensitive strategy should fail when text differs'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'alice')

		and: 'actual table with different text'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'NAME'], 1, 'bob')

		when: 'comparing with CASE_INSENSITIVE strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.caseInsensitive('NAME'))

		then: 'CASE_INSENSITIVE strategy rejects distinct text'
		thrown(AssertionError)
	}

	// ==================== IGNORE Strategy Tests ====================

	def 'ignore strategy should skip comparison for ignored columns'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-01-01')

		and: 'actual table with different timestamp'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-12-31')

		expect: 'IGNORE strategy bypasses the TIMESTAMP column'
		DatabaseAssertion.assertEqualsIgnoreColumns(expectedTable, actualTable, 'TIMESTAMP')
	}

	// ==================== NOT_NULL Strategy Tests ====================

	def 'not-null strategy should accept any non-null actual when applied'() {
		given: 'expected table with placeholder value'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, 'any-placeholder')

		and: 'actual table with a different non-null value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, '0xFEED-CAFE-1234')

		expect: 'NOT_NULL strategy tolerates value differences when the value exists'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.notNull('GENERATED_ID'))
	}

	def 'not-null strategy should fail when actual is null'() {
		given: 'expected table'
		def expectedTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, 'any-placeholder')

		and: 'actual table with a null value'
		def actualTable = createTable('COMPARISON_TEST', ['ID', 'GENERATED_ID'], 1, null)

		when: 'comparing with NOT_NULL strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.notNull('GENERATED_ID'))

		then: 'NOT_NULL strategy rejects null actual value'
		thrown(AssertionError)
	}

	// ==================== TIMESTAMP_FLEXIBLE Strategy Tests ====================

	def 'timestamp-flexible strategy should match timestamps with different sub-second precision'() {
		given: 'expected table with millisecond precision'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00.000')

		and: 'actual table without sub-second precision'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00')

		expect: 'TIMESTAMP_FLEXIBLE strategy ignores sub-second precision'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.timestampFlexible('TIMESTAMP'))
	}

	def 'timestamp-flexible strategy should fail when timestamps refer to different dates'() {
		given: 'expected table'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00')

		and: 'actual table with a different month'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-07-15T10:30:00')

		when: 'comparing with TIMESTAMP_FLEXIBLE strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.timestampFlexible('TIMESTAMP'))

		then: 'TIMESTAMP_FLEXIBLE strategy rejects different instants'
		thrown(AssertionError)
	}

	def 'timestamp-flexible strategy should match same instant across timezone offsets'() {
		given: 'expected table with +09:00 offset'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00+09:00')

		and: 'actual table with equivalent UTC instant'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T01:30:00Z')

		expect: 'TIMESTAMP_FLEXIBLE strategy normalizes to instant'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.timestampFlexible('TIMESTAMP'))
	}

	def 'timestamp-flexible strategy should fail when offsets shift instant'() {
		given: 'expected table with +09:00 offset'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00+09:00')

		and: 'actual table with the same local time but UTC'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'TIMESTAMP'], 1, '2024-06-15T10:30:00Z')

		when: 'comparing with TIMESTAMP_FLEXIBLE strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.timestampFlexible('TIMESTAMP'))

		then: 'TIMESTAMP_FLEXIBLE strategy rejects shifted instant'
		thrown(AssertionError)
	}

	// ==================== DATE_FLEXIBLE Strategy Tests ====================

	def 'date-flexible strategy should match ISO and slash formats'() {
		given: 'expected table with ISO format'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024-06-15')

		and: 'actual table with slash format'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024/06/15')

		expect: 'DATE_FLEXIBLE strategy treats ISO and slash formats as equal'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.dateFlexible('BIRTH_DATE'))
	}

	def 'date-flexible strategy should match dot-delimited date format'() {
		given: 'expected table with ISO format'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024-06-15')

		and: 'actual table with dot-delimited format'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024.06.15')

		expect: 'DATE_FLEXIBLE strategy supports the dot-delimited format'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.dateFlexible('BIRTH_DATE'))
	}

	def 'date-flexible strategy should fail when dates differ'() {
		given: 'expected table'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024-06-15')

		and: 'actual table with a different date'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'BIRTH_DATE'], 1, '2024-07-20')

		when: 'comparing with DATE_FLEXIBLE strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.dateFlexible('BIRTH_DATE'))

		then: 'DATE_FLEXIBLE strategy rejects different dates'
		thrown(AssertionError)
	}

	// ==================== JSON_EQUIVALENT Strategy Tests ====================

	def 'json-equivalent strategy should match JSON with different key order'() {
		given: 'expected table with one key order'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1, '{"name": "Alice", "age": 30}')

		and: 'actual table with a different key order'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1, '{"age": 30, "name": "Alice"}')

		expect: 'JSON_EQUIVALENT strategy ignores key order'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent('METADATA'))
	}

	def 'json-equivalent strategy should match nested JSON when keys are reordered'() {
		given: 'expected nested JSON'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1,
				'{"user":{"name":"Alice","roles":["admin","user"]}}')

		and: 'actual nested JSON with reordered keys'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1,
				'{"user":{"roles":["admin","user"],"name":"Alice"}}')

		expect: 'JSON_EQUIVALENT strategy normalizes nested keys'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent('METADATA'))
	}

	def 'json-equivalent strategy should fail when nested array order differs'() {
		given: 'expected table'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1,
				'{"tags":["alpha","beta","gamma"]}')

		and: 'actual table with a different array order'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1,
				'{"tags":["gamma","alpha","beta"]}')

		when: 'comparing with JSON_EQUIVALENT strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent('METADATA'))

		then: 'JSON_EQUIVALENT strategy preserves array order'
		thrown(AssertionError)
	}

	def 'json-equivalent strategy should fail when JSON values differ'() {
		given: 'expected table'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1, '{"name": "Alice", "age": 30}')

		and: 'actual table with different values'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'METADATA'], 1, '{"name": "Bob", "age": 25}')

		when: 'comparing with JSON_EQUIVALENT strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.jsonEquivalent('METADATA'))

		then: 'JSON_EQUIVALENT strategy rejects different values'
		thrown(AssertionError)
	}

	// ==================== REGEX Strategy Tests ====================

	def 'regex strategy should match actual against pattern when applied'() {
		given: 'expected table with placeholder value'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'EMAIL'], 1, '<PATTERN_EMAIL>')

		and: 'actual table with a value that matches the pattern'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'alice@example.com')

		expect: 'REGEX strategy matches actual against the configured pattern'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.regex('EMAIL', EMAIL_PATTERN))
	}

	def 'regex strategy should fail when actual does not match pattern'() {
		given: 'expected table with placeholder value'
		def expectedTable = createTable(
				'COMPARISON_TEST', ['ID', 'EMAIL'], 1, '<PATTERN_EMAIL>')

		and: 'actual table with a value that violates the pattern'
		def actualTable = createTable(
				'COMPARISON_TEST', ['ID', 'EMAIL'], 1, 'invalid-email')

		when: 'comparing with REGEX strategy'
		DatabaseAssertion.assertEqualsWithStrategies(
				expectedTable, actualTable, ColumnStrategyMapping.regex('EMAIL', EMAIL_PATTERN))

		then: 'REGEX strategy rejects values that do not match'
		thrown(AssertionError)
	}

	/**
	 * Executes a SQL script from the classpath.
	 *
	 * @param scriptPath the classpath resource path
	 * @throws IllegalStateException if the script is not found
	 */
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
