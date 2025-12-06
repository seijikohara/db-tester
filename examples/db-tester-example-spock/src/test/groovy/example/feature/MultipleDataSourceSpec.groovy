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
 * Demonstrates using multiple named data sources in a single test using Spock.
 *
 * <p>This specification shows:
 * <ul>
 *   <li>Registering multiple named data sources
 *   <li>Using {@code dataSourceName} in {@code @DataSet} annotations
 *   <li>Working with different databases simultaneously
 * </ul>
 *
 * <p>Use cases:
 * <ul>
 *   <li>Multi-tenant applications with separate database instances
 *   <li>Microservices with their own databases
 *   <li>Testing data synchronization between databases
 * </ul>
 */
class MultipleDataSourceSpec extends Specification {

	/** Primary database DataSource. */
	@Shared
	DataSource primaryDataSource

	/** Secondary database DataSource. */
	@Shared
	DataSource secondaryDataSource

	/** Groovy SQL helper for primary database operations. */
	@Shared
	Sql primarySql

	/** Groovy SQL helper for secondary database operations. */
	@Shared
	Sql secondarySql

	/** Static registry and DataSource shared across all tests. */
	static DataSourceRegistry sharedRegistry

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
		sharedRegistry = new DataSourceRegistry()
	}

	/**
	 * Sets up two H2 in-memory databases.
	 *
	 * <p>Creates:
	 * <ul>
	 *   <li>Default database - primary data store
	 *   <li>Named database "inventory" - secondary data store
	 * </ul>
	 */
	def setupSpec() {
		// Ensure registry is initialized
		if (sharedRegistry == null) {
			initializeSharedResources()
		}

		// Setup primary database (default)
		primaryDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:MultipleDataSourceSpec_Primary;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry.registerDefault(primaryDataSource)
		primarySql = new Sql(primaryDataSource)
		executeScript(primarySql, 'ddl/feature/MultipleDataSourceSpec-primary.sql')

		// Setup secondary database (named "inventory")
		secondaryDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:MultipleDataSourceSpec_Secondary;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry.register('inventory', secondaryDataSource)
		secondarySql = new Sql(secondaryDataSource)
		executeScript(secondarySql, 'ddl/feature/MultipleDataSourceSpec-secondary.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		primarySql?.close()
		secondarySql?.close()
	}

	/**
	 * Tests operations on the default (primary) database.
	 *
	 * <p>Uses default dataSourceName (empty string refers to the default data source).
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads default database - TABLE1(ID=1 Alice, ID=2 Bob)
	 *   <li>Execution: Inserts ID=3 (Charlie Brown, charlie@example.com) into default database
	 *   <li>Expectation: Verifies all three customers exist in default database
	 * </ul>
	 */
	@Preparation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/MultipleDataSourceSpec/default/',
	scenarioNames = 'default'
	)
	)
	@Expectation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/MultipleDataSourceSpec/default/expected/',
	scenarioNames = 'default'
	)
	)
	def 'should manage customers in default database'() {
		when: 'inserting a customer into default database'
		primarySql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
			VALUES (3, 'Charlie Brown', 'charlie@example.com')
		'''

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Tests operations on the named secondary (inventory) database.
	 *
	 * <p>Uses {@code dataSourceName = "inventory"} to specify the secondary database.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads inventory database - TABLE1(ID=1 Laptop, ID=2 Keyboard)
	 *   <li>Execution: Inserts ID=3 (Monitor, 25) into inventory database
	 *   <li>Expectation: Verifies all three products exist in inventory database
	 * </ul>
	 */
	@Preparation(
	dataSets = @DataSet(
	dataSourceName = 'inventory',
	resourceLocation = 'classpath:example/feature/MultipleDataSourceSpec/inventory/',
	scenarioNames = 'inventory'
	)
	)
	@Expectation(
	dataSets = @DataSet(
	dataSourceName = 'inventory',
	resourceLocation = 'classpath:example/feature/MultipleDataSourceSpec/inventory/expected/',
	scenarioNames = 'inventory'
	)
	)
	def 'should manage products in inventory database'() {
		when: 'inserting a product into inventory database'
		secondarySql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
			VALUES (3, 'Monitor', 25)
		'''

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Executes a SQL script from classpath using Groovy 5 features.
	 *
	 * @param sql the Groovy SQL helper
	 * @param scriptPath the classpath resource path
	 */
	private void executeScript(Sql sql, String scriptPath) {
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
