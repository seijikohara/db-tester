package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
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
 *   <li>Distinguishing between NULL and empty string in VARCHAR columns
 *   <li>Handling NOT NULL constraints
 *   <li>NULL values in numeric and timestamp columns
 * </ul>
 *
 * <p>CSV format examples and NULL representation:
 * <pre>{@code
 * ID,COLUMN1,COLUMN2,COLUMN3,COLUMN4
 * 1,Required Value,,100,
 * 2,Another Value,Optional Value,200,42
 * }</pre>
 *
 * <p><strong>Important:</strong> Empty cells in CSV files are interpreted as SQL NULL
 * for all column types.
 */
class NullAndEmptyValuesSpec extends Specification {

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
	@Preparation
	@Expectation
	def 'should handle null values'() {
		when: 'inserting record with NULL values'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (3, 'Third Record', NULL, 300, NULL)
		'''

		then: 'all records including NULL values are verified'
		noExceptionThrown()
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
