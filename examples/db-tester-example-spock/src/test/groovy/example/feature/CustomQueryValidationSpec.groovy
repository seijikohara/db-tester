package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import java.sql.Date
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates database testing with SQL query result validation using Spock.
 *
 * <p>Each feature prepares baseline data through {@link DataSet}, runs additional SQL that
 * exercises the feature under test, then verifies the result of a SQL query (rather than the full
 * table state) using {@link DatabaseQueryAssertion#assertEqualsByQuery}.
 *
 * <p>This specification demonstrates four query patterns:
 * <ul>
 *   <li>Filtering rows via a {@code WHERE} clause
 *   <li>Aggregating rows via {@code GROUP BY} with {@code SUM} and {@code COUNT}
 *   <li>Joining two tables via {@code INNER JOIN}
 *   <li>Restricting rows to a date range via {@code BETWEEN}
 * </ul>
 */
@DatabaseTest
class CustomQueryValidationSpec extends Specification implements DatabaseTestSupport {

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
	 * Initializes shared resources (DataSource and Registry).
	 */
	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:CustomQueryValidationSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up the H2 in-memory database and schema.
	 */
	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)
		executeScript('ddl/feature/CustomQueryValidationSpec.sql')
	}

	/**
	 * Closes the SQL helper.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Builds an in-memory Table for use as an expected query result.
	 *
	 * @param tableName the logical table name attached to the query result
	 * @param columnNames the column names in order
	 * @param rowValues each inner list represents the cell values of one row, in column order
	 * @return a Table representing the expected query result
	 */
	private static Table createTable(
			String tableName, List<String> columnNames, List<List<Object>> rowValues) {
		def columns = columnNames.collect { new ColumnName(it) }
		def rows = rowValues.collect { values ->
			Map<ColumnName, CellValue> rowMap = [:]
			columns.eachWithIndex { col, i ->
				if (i < values.size()) {
					rowMap[col] = new CellValue(values[i])
				}
			}
			Row.of(rowMap)
		}
		Table.of(new TableName(tableName), columns, rows)
	}

	@DataSet
	def 'should return only East-region rows when WHERE filter applied'() {
		given: 'an additional East-region sale is inserted'
		sql.execute('''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 3, '2024-01-25', 350.00, 'East')
		''')

		and: 'expected rows are the pre-loaded East row and the newly inserted East row'
		def expected = createTable('TABLE1',
				['ID', 'COLUMN1', 'COLUMN2', 'COLUMN3', 'COLUMN4'],
				[
					[2, 2, Date.valueOf('2024-01-15'), new BigDecimal('300.00'), 'East'],
					[4, 3, Date.valueOf('2024-01-25'), new BigDecimal('350.00'), 'East']
				])

		expect: 'WHERE filter narrows the result to East rows in primary-key order'
		DatabaseQueryAssertion.assertEqualsByQuery(
				expected,
				dataSource,
				'TABLE1',
				'SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1' +
				' WHERE COLUMN4 = \'East\' ORDER BY ID')
	}

	@DataSet
	def 'should return aggregated totals per category when GROUP BY applied'() {
		given: 'an additional Premium-category sale is inserted'
		sql.execute('''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 1, '2024-01-25', 500.00, 'West')
		''')

		// The expected column names use the SQL aliases, including the alias on the plain COLUMN1
		// reference. TableReader reads column labels, so an alias on any projected column resolves
		// to that alias in the result.
		and: 'expected aggregates summarize per-category total and record count'
		def expected = createTable('CATEGORY_TOTALS',
				['CATEGORY_ID', 'TOTAL_AMOUNT', 'RECORD_COUNT'],
				[
					[1, new BigDecimal('1700.00'), 3L],
					[2, new BigDecimal('300.00'), 1L]
				])

		expect: 'GROUP BY aggregation matches the expected per-category rows'
		DatabaseQueryAssertion.assertEqualsByQuery(
				expected,
				dataSource,
				'CATEGORY_TOTALS',
				'SELECT COLUMN1 AS CATEGORY_ID, SUM(COLUMN3) AS TOTAL_AMOUNT, COUNT(*) AS RECORD_COUNT' +
				' FROM TABLE1 GROUP BY COLUMN1 ORDER BY COLUMN1')
	}

	@DataSet
	def 'should return joined sale and category rows when INNER JOIN applied'() {
		given: 'an additional Premium-category sale is inserted'
		sql.execute('''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 1, '2024-02-01', 600.00, 'North')
		''')

		// The expected column names use the SQL aliases applied to the joined columns. TableReader
		// reads column labels, so CATEGORY_NAME and SALE_AMOUNT resolve to the aliases rather than
		// the underlying table column names.
		and: 'expected rows pair each sale with the category name'
		def expected = createTable('SALES_WITH_CATEGORY',
				['ID', 'CATEGORY_NAME', 'SALE_AMOUNT'],
				[
					[1, 'Premium', new BigDecimal('500.00')],
					[2, 'Standard', new BigDecimal('300.00')],
					[3, 'Premium', new BigDecimal('700.00')],
					[4, 'Premium', new BigDecimal('600.00')]
				])

		expect: 'INNER JOIN surfaces the category label for each sale row'
		DatabaseQueryAssertion.assertEqualsByQuery(
				expected,
				dataSource,
				'SALES_WITH_CATEGORY',
				'SELECT s.ID, c.NAME AS CATEGORY_NAME, s.COLUMN3 AS SALE_AMOUNT' +
				' FROM TABLE1 s INNER JOIN CATEGORIES c ON s.COLUMN1 = c.ID' +
				' ORDER BY s.ID')
	}

	@DataSet
	def 'should return only January rows when BETWEEN date filter applied'() {
		given: 'one January row and one February row are inserted'
		sql.execute('''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 2, '2024-01-25', 450.00, 'South')
		''')
		sql.execute('''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (5, 1, '2024-02-05', 800.00, 'North')
		''')

		and: 'expected rows include all four January records and exclude the February record'
		def expected = createTable('TABLE1',
				['ID', 'COLUMN1', 'COLUMN2', 'COLUMN3', 'COLUMN4'],
				[
					[1, 1, Date.valueOf('2024-01-10'), new BigDecimal('500.00'), 'West'],
					[2, 2, Date.valueOf('2024-01-15'), new BigDecimal('300.00'), 'East'],
					[3, 1, Date.valueOf('2024-01-20'), new BigDecimal('700.00'), 'North'],
					[4, 2, Date.valueOf('2024-01-25'), new BigDecimal('450.00'), 'South']
				])

		expect: 'BETWEEN date filter excludes the February row'
		DatabaseQueryAssertion.assertEqualsByQuery(
				expected,
				dataSource,
				'TABLE1',
				'SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1' +
				' WHERE COLUMN2 BETWEEN DATE \'2024-01-01\' AND DATE \'2024-01-31\'' +
				' ORDER BY ID')
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
