package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates database testing with custom query validation scenarios using Spock.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>Testing INSERT operations with data
 *   <li>Using custom expectation paths for different scenarios
 *   <li>Validating filtered data
 *   <li>Testing aggregation scenarios
 *   <li>Validating date-range queries
 * </ul>
 *
 * <p>Note: For actual SQL query result validation using {@code
 * DatabaseAssertion.assertEqualsByQuery}, you would need to programmatically create expected
 * datasets using DbUnit APIs.
 */
@DatabaseTest
class CustomQueryValidationSpec extends Specification {

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
			setURL('jdbc:h2:mem:CustomQueryValidationSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/CustomQueryValidationSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates validation with filtered data.
	 *
	 * <p>Validates data after adding new record with specific filter criteria.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data
	 *   <li>Execution: Inserts ID=4 (COLUMN1=3, East region, 2024-01-25, 350.00)
	 *   <li>Expectation: Verifies all four records from {@code expected-filtered/}
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomQueryValidationSpec/expected-filtered/'
	))
	def 'should validate regional sales'() {
		when: 'inserting new regional sales record'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 3, '2024-01-25', 350.00, 'East')
		'''

		then: 'expectation phase verifies filtered regional sales data'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation with aggregated data.
	 *
	 * <p>Validates aggregated data after adding new record.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data
	 *   <li>Execution: Inserts ID=4 (COLUMN1=1, West region, 2024-01-25, 500.00)
	 *   <li>Expectation: Verifies all four records from {@code expected-aggregation/}
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomQueryValidationSpec/expected-aggregation/'
	))
	def 'should validate sales summary'() {
		when: 'inserting new sales record for aggregation'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 1, '2024-01-25', 500.00, 'West')
		'''

		then: 'expectation phase verifies aggregated sales data'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation with high-value records.
	 *
	 * <p>Validates data after adding a high-value record.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data (January)
	 *   <li>Execution: Inserts ID=4 (COLUMN1=1, North region, 2024-02-01, 600.00)
	 *   <li>Expectation: Verifies all four records including February data from {@code expected-join/}
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomQueryValidationSpec/expected-join/'
	))
	def 'should validate high value sales'() {
		when: 'inserting high-value sales record'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 1, '2024-02-01', 600.00, 'North')
		'''

		then: 'expectation phase verifies high-value sales with join scenario'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation with date range filtering for January sales.
	 *
	 * <p>Validates that only January sales data is present in the database by adding a January record
	 * and verifying the final state contains only January data.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1,2,3) with January sales data
	 *   <li>Execution: Inserts ID=4 (COLUMN1=2, South region, 2024-01-25, 450.00)
	 *   <li>Expectation: Verifies all four January records from {@code expected-daterange/}
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomQueryValidationSpec/expected-daterange/'
	))
	def 'should validate january sales'() {
		when: 'inserting January sales record'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (4, 2, '2024-01-25', 450.00, 'South')
		'''

		then: 'expectation phase verifies date-range filtered January sales'
		noExceptionThrown()
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
