package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates customization of framework convention settings using Spock.
 *
 * <p>This specification demonstrates using custom {@link Configuration} to customize framework's
 * convention settings while keeping default operations.
 *
 * <p>Customizations demonstrated:
 * <ul>
 *   <li>Custom scenario marker column name: {@code [TestCase]} instead of {@code [Scenario]}
 *   <li>Custom expectation directory suffix: {@code /verify} instead of {@code /expected}
 * </ul>
 *
 * <p>Default convention settings:
 * <ul>
 *   <li>Scenario marker: {@code [Scenario]}
 *   <li>Expectation suffix: {@code /expected}
 * </ul>
 *
 * <p>This test class uses custom conventions while keeping default database operations
 * (CLEAN_INSERT for preparation, NONE for expectation).
 */
@DatabaseTest
class ConfigurationCustomizationSpec extends Specification {

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/** Static registry, DataSource, and Configuration shared across all tests. */
	static DataSourceRegistry sharedRegistry
	static DataSource sharedDataSource

	/**
	 * Static configuration with custom conventions.
	 * Using static initialization ensures the configuration is available before any test execution,
	 * including interceptor invocation.
	 */
	static Configuration sharedConfiguration = Configuration.withConventions(
	new ConventionSettings(
	null,                       // use classpath-relative resolution
	'/verify',                  // custom expectation suffix
	'[TestCase]',               // custom scenario marker
	DataFormat.CSV,             // use CSV format (default)
	TableMergeStrategy.UNION_ALL, // use UNION_ALL merge strategy (default)
	ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
	Set.of()
	)
	)

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
	 * Gets the custom Configuration (Groovy property accessor).
	 * Returns the static shared configuration to ensure it's available before field initialization.
	 * @return the configuration
	 */
	Configuration getDbTesterConfiguration() {
		return sharedConfiguration
	}

	/**
	 * Initializes shared resources (DataSource, Registry, SQL helper).
	 */
	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ConfigurationCustomizationSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database connection and schema.
	 * Uses Groovy 5 compact syntax and extension methods.
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
		executeScript('ddl/feature/ConfigurationCustomizationSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates custom scenario marker usage.
	 *
	 * <p>CSV files use {@code [TestCase]} column instead of default {@code [Scenario]} to filter rows
	 * by test method name.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
	 *   <li>Execution: Inserts ID=2 (Bob, ACTIVE, 2024-01-15)
	 *   <li>Expectation: Verifies both records exist with correct values
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use custom scenario marker'() {
		when: 'inserting a new record'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (2, 'Bob', 'ACTIVE', '2024-01-15')
		'''

		then: 'both records are verified'
		noExceptionThrown()
	}

	/**
	 * Demonstrates custom expectation suffix usage.
	 *
	 * <p>Expected data is loaded from {@code /verify} directory instead of default {@code /expected}.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
	 *   <li>Execution: Updates ID=1 status from ACTIVE to SUSPENDED
	 *   <li>Expectation: Verifies status change from {@code verify/TABLE1.csv}
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use custom expectation suffix'() {
		when: 'updating record status'
		sql.executeUpdate "UPDATE TABLE1 SET COLUMN2 = 'SUSPENDED' WHERE ID = 1"

		then: 'status is updated and verified'
		noExceptionThrown()
	}

	/**
	 * Demonstrates using default configuration with standard operations.
	 *
	 * <p>Although the test class customizes scenario marker and expectation suffix, operation
	 * defaults remain standard (CLEAN_INSERT for preparation).
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (Alice) and ID=2 (Bob) with ACTIVE status
	 *   <li>Execution: Inserts ID=3 (Charlie, INACTIVE, 2024-02-01)
	 *   <li>Expectation: Verifies all three records exist
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use custom operation defaults'() {
		when: 'inserting a new record'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (3, 'Charlie', 'INACTIVE', '2024-02-01')
		'''

		then: 'all three records exist'
		noExceptionThrown()
	}

	/**
	 * Executes a SQL script from classpath using Groovy 5 features.
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
