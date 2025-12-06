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
 * Demonstrates the minimal convention-based database testing approach with Spock.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>Automatic CSV file resolution based on specification class and feature method names
 *   <li>Method-level {@code @Preparation} and {@code @Expectation} annotations
 *   <li>Single table operations with minimal configuration
 *   <li>H2 in-memory database setup using Groovy 5 features
 * </ul>
 *
 * <p>CSV files are located at:
 * <ul>
 *   <li>{@code src/test/resources/example/feature/MinimalExampleSpec/TABLE1.csv}
 *   <li>{@code src/test/resources/example/feature/MinimalExampleSpec/expected/TABLE1.csv}
 * </ul>
 */
class MinimalExampleSpec extends Specification {

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
			setURL('jdbc:h2:mem:MinimalExampleSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/MinimalExampleSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates the minimal convention-based test.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads TABLE1(ID=1 Mouse, ID=2 Monitor) from {@code TABLE1.csv}
	 *   <li>Execution: Inserts ID=3 (Keyboard, 79.99) into TABLE1
	 *   <li>Expectation: Verifies all three products from {@code expected/TABLE1.csv}
	 * </ul>
	 */
	@Preparation
	@Expectation
	def 'should load and verify product data'() {
		when: 'inserting a new product'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
			VALUES (3, 'Keyboard', 79.99)
		'''

		then: 'expectation phase verifies database state'
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
