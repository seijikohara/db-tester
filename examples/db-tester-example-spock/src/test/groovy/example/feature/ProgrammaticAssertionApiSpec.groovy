package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates both annotation-based and programmatic database validation approaches with Spock.
 *
 * <p>This specification illustrates two complementary validation strategies:
 * <ul>
 *   <li><strong>Annotation-based validation</strong> using {@code @ExpectedDataSet} - suitable for
 *       standard table comparisons with convention-based expected data
 *   <li><strong>Programmatic validation</strong> using custom SQL queries - provides flexibility
 *       for complex scenarios where annotation-based testing is insufficient
 * </ul>
 *
 * <p>Key programmatic API features available in {@link DatabaseAssertion}:
 * <ul>
 *   <li>{@link DatabaseAssertion#assertEqualsByQuery} - Compare expected data against SQL query results
 *   <li>{@link DatabaseAssertion#assertEquals} - Compare two datasets or tables directly
 *   <li>{@link DatabaseAssertion#assertEqualsIgnoreColumns} - Compare datasets ignoring specific columns
 * </ul>
 *
 * <p>Programmatic assertions are useful for custom SQL queries, dynamic column filtering, mid-test
 * state verification, or comparing multiple dataset sources.
 */
@DatabaseTest
class ProgrammaticAssertionApiSpec extends Specification {

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/** Static registry and DataSource shared across all tests. */
	static DataSourceRegistry sharedRegistry
	static DataSource sharedDataSource

	/**
	 * Gets the DataSourceRegistry (Groovy property accessor).
	 * @return the registry
	 */
	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeSharedResources()
		}
		return sharedRegistry
	}

	/**
	 * Initializes shared resources (DataSource, Registry, SQL helper).
	 */
	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ProgrammaticAssertionApiSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database connection and schema.
	 * Uses Groovy compact syntax and extension methods.
	 */
	def setupSpec() {
		// Ensure resources are initialized
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource

		// Create Groovy SQL helper
		sql = new Sql(dataSource)

		// Execute DDL script using Groovy's resource handling
		executeScript('ddl/feature/ProgrammaticAssertionApiSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates basic programmatic assertion without annotations.
	 *
	 * <p>Shows direct use of {@link DatabaseAssertion} assertion APIs for custom validation scenarios
	 * where annotation-based testing is insufficient.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
	 *   <li>Execution: Inserts (3,Value3,300,NULL)
	 *   <li>Expectation: Verifies all three records including NULL COLUMN3
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should demonstrate basic programmatic API'() {
		when: 'inserting new record with NULL column'
		sql.execute "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, NULL)"

		then: 'expectation phase verifies database state using standard annotation'
		noExceptionThrown()
	}

	/**
	 * Demonstrates programmatic custom SQL query validation.
	 *
	 * <p>This test shows validation using direct SQL queries instead of relying on
	 * {@code @ExpectedDataSet} annotation. Programmatic assertions provide flexibility for custom
	 * validation scenarios.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
	 *   <li>Execution: Inserts (3,Value3,300,NULL) and (4,Value4,400,NULL)
	 *   <li>Expectation: Validates using SQL queries to verify row count and specific records
	 * </ul>
	 */
	@DataSet
	def 'should validate using multiple queries'() {
		when: 'inserting two new records'
		sql.execute "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Value3', 300)"
		sql.execute "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Value4', 400)"

		then: 'verify total row count is 4'
		def rowCount = 0
		sql.eachRow('SELECT COUNT(*) as cnt FROM TABLE1') { row ->
			rowCount = row.cnt
		}
		rowCount == 4

		and: 'verify newly inserted records have correct values'
		def records = []
		sql.eachRow('SELECT COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (3, 4) ORDER BY ID') { row ->
			records << [column1: row.COLUMN1, column2: row.COLUMN2]
		}
		records.size() == 2
		records[0].column1 == 'Value3'
		records[0].column2 == 300
		records[1].column1 == 'Value4'
		records[1].column2 == 400
	}

	/**
	 * Executes a SQL script from classpath using Groovy features.
	 *
	 * @param scriptPath the classpath resource path
	 */
	private void executeScript(String scriptPath) {
		def resource = getClass().classLoader.getResource(scriptPath)
		if (resource == null) {
			throw new IllegalStateException("Script not found: $scriptPath")
		}

		// Use Groovy's text property and split with filter
		resource.text
				.split(';')
				.collect { it.trim() }
				.findAll { !it.empty }
				.each { sql.execute(it) }
	}
}
