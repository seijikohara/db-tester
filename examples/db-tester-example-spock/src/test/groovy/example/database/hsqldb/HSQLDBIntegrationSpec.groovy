package example.database.hsqldb

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.hsqldb.jdbc.JDBCDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * HSQLDB integration test using in-memory database.
 *
 * <p>This test validates that the framework works correctly with HSQLDB (HyperSQL Database).
 * This is a smoke test to ensure HSQLDB compatibility with Spock.
 */
@DatabaseTest
class HSQLDBIntegrationSpec extends Specification {

	/** Logger instance for test execution logging. */
	private static final Logger logger = LoggerFactory.getLogger(HSQLDBIntegrationSpec)

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
		logger.info('Setting up HSQLDB in-memory database')

		sharedDataSource = new JDBCDataSource().tap {
			setURL('jdbc:hsqldb:mem:HSQLDBIntegrationSpec')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)

		logger.info('HSQLDB database setup completed')
	}

	/**
	 * Sets up HSQLDB in-memory database connection and schema.
	 */
	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)

		executeScript('ddl/database/hsqldb/hsqldb-integration.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Smoke test verifying basic framework functionality with HSQLDB.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into HSQLDB
	 *   <li>Data can be verified against expected CSV
	 *   <li>Basic CRUD operations work correctly
	 * </ul>
	 */
	@DataSet(sources = @DataSetSource(scenarioNames = 'smokeTest'))
	@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on HSQLDB'() {
		when: 'running HSQLDB integration smoke test'
		logger.info('Running HSQLDB integration smoke test')

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
