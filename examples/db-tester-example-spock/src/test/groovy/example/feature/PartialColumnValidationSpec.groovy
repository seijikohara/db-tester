package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates partial column validation techniques using CSV files with Spock.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>Validating only specific columns via partial CSV files
 *   <li>Excluding auto-generated columns (ID, timestamps) from CSV expectations
 *   <li>Testing business logic without worrying about database-generated values
 *   <li>Using custom expectation paths for different validation scenarios
 * </ul>
 *
 * <p>Use partial column validation when:
 * <ul>
 *   <li>Testing tables with auto-increment IDs
 *   <li>Ignoring timestamp columns (CREATED_AT, UPDATED_AT)
 *   <li>Focusing on business-relevant columns only
 *   <li>Dealing with database-generated values (UUIDs, sequences)
 * </ul>
 *
 * <p>Note: For programmatic column exclusion using {@code
 * DatabaseAssertion.assertEqualsIgnoreColumns}, you would need to manually create datasets using
 * DbUnit APIs.
 */
class PartialColumnValidationSpec extends Specification {

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
			setURL('jdbc:h2:mem:PartialColumnValidationSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/PartialColumnValidationSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates partial column validation using CSV with subset of columns.
	 *
	 * <p>CSV contains only business-relevant columns, ignoring auto-generated ID and timestamp.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
	 *   <li>Execution: Inserts (DELETE,User,789) - ID and COLUMN4/5 auto-generated
	 *   <li>Expectation: Verifies all three records exist with expected COLUMN1/2/3 values
	 * </ul>
	 */
	@Preparation
	@Expectation
	def 'should validate partial columns via CSV'() {
		when: 'inserting new record with business columns only'
		sql.execute '''
			INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
			VALUES ('DELETE', 'User', 789)
		'''

		then: 'expectation phase verifies partial columns via CSV'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation with partial CSV (ignoring auto-generated columns).
	 *
	 * <p>CSV file contains only business columns, excluding ID, COLUMN4, and COLUMN5.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
	 *   <li>Execution: Inserts (UPDATE,Product,456) - same values but different auto-generated ID
	 *   <li>Expectation: Verifies three records with matching COLUMN1/2/3, ignoring ID differences
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/PartialColumnValidationSpec/expected-ignore-columns/'
	))
	def 'should ignore auto generated columns'() {
		when: 'inserting duplicate business values with different auto-generated ID'
		sql.execute '''
			INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
			VALUES ('UPDATE', 'Product', 456)
		'''

		then: 'expectation phase verifies ignoring auto-generated columns'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation with minimal CSV columns including default value verification.
	 *
	 * <p>CSV contains essential business columns plus COLUMN5 to verify DEFAULT 'SYSTEM' value.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
	 *   <li>Execution: Inserts (CREATE,Order,999) - COLUMN5 defaults to 'SYSTEM'
	 *   <li>Expectation: Verifies COLUMN1/2/3/5 values including default COLUMN5='SYSTEM'
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/PartialColumnValidationSpec/expected-combined/'
	))
	def 'should validate with minimal columns'() {
		when: 'inserting record with minimal columns'
		sql.execute '''
			INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
			VALUES ('CREATE', 'Order', 999)
		'''

		then: 'expectation phase verifies minimal columns including defaults'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validation after UPDATE operation.
	 *
	 * <p>Note: This test validates the complete table state after an update operation. True partial
	 * column validation (validating only specific columns while ignoring others) requires
	 * programmatic assertions using {@code DatabaseAssertion.assertEqualsIgnoreColumns}, which is
	 * beyond the scope of annotation-based testing.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
	 *   <li>Execution: Updates ID=1 COLUMN3 from 123 to 555
	 *   <li>Expectation: Verifies (CREATE,User,555), (UPDATE,Product,456) with updated value
	 * </ul>
	 */
	@Preparation
	@Expectation(dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/PartialColumnValidationSpec/expected-after-update/'
	))
	def 'should validate after update'() {
		when: 'updating existing record column value'
		sql.executeUpdate "UPDATE TABLE1 SET COLUMN3 = 555 WHERE COLUMN1 = 'CREATE'"

		then: 'expectation phase verifies updated state'
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
