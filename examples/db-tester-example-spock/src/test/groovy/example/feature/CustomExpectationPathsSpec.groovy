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
 * Demonstrates custom expectation paths for flexible test data organization using Spock.
 *
 * <p>This specification demonstrates using {@link DataSet#resourceLocation()} to specify custom paths
 * for expectation data, enabling flexible test data organization beyond convention-based defaults.
 *
 * <p>Key features demonstrated:
 * <ul>
 *   <li>Custom expectation paths using {@link DataSet} annotation
 *   <li>Organizing multiple expectation scenarios in subdirectories
 *   <li>Multi-stage testing with different expected states
 *   <li>Complex business logic validation with database state changes
 * </ul>
 *
 * <p>This approach is useful when tests require multiple expectation variants or when
 * convention-based paths are insufficient for complex test scenarios.
 */
@DatabaseTest
class CustomExpectationPathsSpec extends Specification {

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
			setURL('jdbc:h2:mem:CustomExpectationPathsSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/CustomExpectationPathsSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates custom expectation paths with basic INSERT operation.
	 *
	 * <p>This test uses {@link DataSet#resourceLocation()} to specify a custom path for expectation
	 * data, demonstrating how to organize test data in non-default directories.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 from default location
	 *   <li>Execution: Inserts ID=2 (customer_id=1, amount=299.99, PENDING)
	 *   <li>Expectation: Verifies both records from {@code expected-basic/} directory
	 * </ul>
	 */
	@Preparation
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomExpectationPathsSpec/expected-basic/'
	)
	)
	def 'should insert new order'() {
		when: 'inserting a new order'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (2, 1, 299.99, '2024-02-15', 'PENDING')
		'''

		then: 'both orders exist'
		noExceptionThrown()
	}

	/**
	 * Demonstrates partial column validation using custom expectation paths.
	 *
	 * <p>CSV files in the custom path contain only the columns to validate, allowing partial
	 * validation without programmatic assertions.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 from default location
	 *   <li>Execution: Inserts ID=2 (customer_id=2, amount=599.99, SHIPPED)
	 *   <li>Expectation: Validates selected columns from {@code expected-ignore-columns/} directory
	 * </ul>
	 */
	@Preparation
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomExpectationPathsSpec/expected-ignore-columns/'
	)
	)
	def 'should validate with partial columns'() {
		when: 'inserting a shipped order'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (2, 2, 599.99, '2024-03-20', 'SHIPPED')
		'''

		then: 'partial columns are validated'
		noExceptionThrown()
	}

	/**
	 * Demonstrates validating related tables with custom expectation paths.
	 *
	 * <p>This test inserts data into TABLE2 and validates the relationship with TABLE1 using a custom
	 * expectation directory.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads existing orders and items
	 *   <li>Execution: Inserts ID=3 (order_id=1, product=Headphones)
	 *   <li>Expectation: Verifies order-item relationship from {@code expected-query/} directory
	 * </ul>
	 */
	@Preparation
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomExpectationPathsSpec/expected-query/'
	)
	)
	def 'should validate order items'() {
		when: 'inserting a new order item'
		sql.execute '''
			INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (3, 1, 'Headphones', 1, 79.99)
		'''

		then: 'order items are validated'
		noExceptionThrown()
	}

	/**
	 * Demonstrates multi-stage workflow testing with custom expectation paths (stage 1).
	 *
	 * <p>This test represents the first stage of an order lifecycle, validating the initial PENDING
	 * state using a stage-specific expectation directory.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (existing order)
	 *   <li>Execution: Inserts ID=2 (customer_id=1, amount=150.00, PENDING)
	 *   <li>Expectation: Verifies PENDING status from {@code expected-stage1/} directory
	 * </ul>
	 */
	@Preparation
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomExpectationPathsSpec/expected-stage1/'
	)
	)
	def 'should create order'() {
		when: 'creating a new order'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
		'''

		then: 'order is created with PENDING status'
		noExceptionThrown()
	}

	/**
	 * Demonstrates multi-stage workflow testing with custom expectation paths (stage 2).
	 *
	 * <p>This test represents order status transition from PENDING to SHIPPED, demonstrating how
	 * different expectation directories can validate different workflow stages.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (existing order)
	 *   <li>Execution: Creates ID=2 as PENDING, then updates to SHIPPED
	 *   <li>Expectation: Verifies SHIPPED status from {@code expected-stage2/} directory
	 * </ul>
	 */
	@Preparation
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/CustomExpectationPathsSpec/expected-stage2/'
	)
	)
	def 'should ship order'() {
		when: 'creating and shipping an order'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
			VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
		'''

		sql.executeUpdate "UPDATE TABLE1 SET COLUMN4 = 'SHIPPED' WHERE ID = 2"

		then: 'order status is SHIPPED'
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
