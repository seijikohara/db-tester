package example.database.derby

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.apache.derby.jdbc.EmbeddedDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * Apache Derby integration test using in-memory database.
 *
 * <p>This test validates that the framework works correctly with Apache Derby database.
 * This is a smoke test to ensure Derby compatibility with Spock.
 */
@DatabaseTest
class DerbyIntegrationSpec extends Specification {

	/** Logger instance for test execution logging. */
	private static final Logger logger = LoggerFactory.getLogger(DerbyIntegrationSpec)

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
		logger.info('Setting up Derby in-memory database')

		sharedDataSource = new EmbeddedDataSource().tap {
			setDatabaseName('memory:DerbyIntegrationSpec')
			setCreateDatabase('create')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)

		logger.info('Derby database setup completed')
	}

	/**
	 * Sets up Derby in-memory database connection and schema.
	 */
	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)

		executeScript('ddl/database/derby/derby-integration.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Smoke test verifying basic framework functionality with Derby.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into Derby
	 *   <li>Data can be verified against expected CSV
	 *   <li>Basic CRUD operations work correctly
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on Derby'() {
		when: 'running Derby integration smoke test'
		logger.info('Running Derby integration smoke test')

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

		resource.text
				.split(';')
				.collect { it.trim() }
				.findAll { !it.empty }
				.each { sql.execute(it) }
	}
}
