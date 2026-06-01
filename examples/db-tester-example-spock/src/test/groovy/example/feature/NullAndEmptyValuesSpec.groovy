package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates NULL value and empty string handling in CSV files using Spock.
 *
 * <p>This specification shows:
 * <ul>
 *   <li>Using empty cells to represent SQL NULL values
 *   <li>Handling NOT NULL constraints
 *   <li>NULL values in numeric and timestamp columns
 *   <li>The NULL versus empty-string distinction available in JSON and YAML
 * </ul>
 *
 * <p>CSV format examples and NULL representation:
 * <pre>{@code
 * ID,COLUMN1,COLUMN2,COLUMN3,COLUMN4
 * 1,Required Value,,100,
 * 2,Another Value,Optional Value,200,42
 * }</pre>
 *
 * <p><strong>Important:</strong> An empty CSV or TSV cell is interpreted as SQL NULL for all
 * column types, and a quoted {@code ""} also materializes as NULL. The delimited formats cannot
 * express a non-null empty string. To distinguish an empty string from NULL, use JSON or YAML,
 * which provide distinct syntax for {@code null} and {@code ""}.
 */
@DatabaseTest
class NullAndEmptyValuesSpec extends Specification implements DatabaseTestSupport {

	@Shared
	DataSource dataSource

	@Shared
	DataSourceRegistry dbTesterRegistry

	@Shared
	Sql sql

	def setupSpec() {
		dataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:NullAndEmptyValuesSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}

		dbTesterRegistry = new DataSourceRegistry()
		dbTesterRegistry.registerDefault(dataSource)

		sql = new Sql(dataSource)
		executeScript('ddl/feature/NullAndEmptyValuesSpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates NULL value handling in CSV files.
	 *
	 * <p>Validates:
	 * <ul>
	 *   <li>Empty cells correctly represent SQL NULL values
	 *   <li>NULL values in optional (nullable) columns
	 *   <li>Empty string vs NULL distinction
	 *   <li>NOT NULL constraints are respected
	 * </ul>
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1 with NULL COLUMN2/COLUMN4, ID=2 with values)
	 *   <li>Execution: Inserts ID=3 (Third Record, NULL, 300, NULL)
	 *   <li>Expectation: Verifies all three records including NULL values
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should handle null values'() {
		when: 'inserting record with NULL values'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (3, 'Third Record', NULL, 300, NULL)
		'''

		then: 'all records including NULL values are verified'
		noExceptionThrown()
	}

	/**
	 * Documents that the CSV loader normalizes both empty cells and quoted empty strings to SQL
	 * NULL for database compatibility.
	 */
	@DataSet
	@ExpectedDataSet
	def 'should normalize quoted empty string to null'() {
		when: 'probing the database after preparation'
		def nullCount = sql.firstRow("SELECT COUNT(*) AS C FROM TABLE1 WHERE COLUMN2 IS NULL AND ID IN (1, 2)").C
		def emptyCount = sql.firstRow("SELECT COUNT(*) AS C FROM TABLE1 WHERE COLUMN2 = ''").C

		then: 'both forms of empty value are stored as NULL'
		nullCount == 2
		emptyCount == 0
	}

	/**
	 * Demonstrates that JSON preserves an empty string as distinct from NULL.
	 *
	 * <p>A JSON {@code null} materializes as SQL NULL, while a JSON {@code ""} materializes as a
	 * non-null empty string. This distinction is unavailable in CSV and TSV.
	 */
	@DataSet(sources = @DataSetSource(resourceLocation = 'classpath:example/feature/NullAndEmptyValuesSpec/jsonEmptyString/'))
	def 'should preserve empty string distinct from null when loading json'() {
		when: 'probing JSON_VALUES after preparation'
		def nullCount = sql.firstRow("SELECT COUNT(*) AS C FROM JSON_VALUES WHERE COLUMN2 IS NULL").C
		def emptyCount = sql.firstRow("SELECT COUNT(*) AS C FROM JSON_VALUES WHERE COLUMN2 = ''").C

		then: 'JSON null becomes NULL and JSON empty string is preserved'
		nullCount == 1
		emptyCount == 1
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
