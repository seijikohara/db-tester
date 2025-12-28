package example.database.oracle

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import java.sql.SQLException
import javax.sql.DataSource
import oracle.jdbc.pool.OracleDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

/**
 * Oracle Database integration test using Testcontainers.
 *
 * <p>This specification validates that the framework works correctly with Oracle database
 * using Testcontainers. This is a smoke test to ensure Oracle compatibility.
 *
 * <p>The test uses {@code @Testcontainers} annotation from testcontainers-spock module
 * to automatically manage the container lifecycle. The {@code @Shared} annotation ensures
 * the container is not restarted between feature methods.
 *
 * <p>This test is skipped in CI environments because Oracle containers require extended
 * startup time that often exceeds CI timeout limits.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
@IgnoreIf({
	System.getenv('CI') == 'true'
})
@Testcontainers
@DatabaseTest
class OracleIntegrationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(OracleIntegrationSpec)

	/** Oracle container for integration testing. */
	@Shared
	OracleContainer oracle = new OracleContainer('gvenzl/oracle-free:latest')
	.withDatabaseName('testdb')
	.withUsername('testuser')
	.withPassword('testpass')

	/** Shared registry for DataSource. */
	static DataSourceRegistry sharedRegistry

	/** Shared DataSource. */
	static DataSource sharedDataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/**
	 * Gets the DataSourceRegistry (Groovy property accessor).
	 * @return the registry
	 */
	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeRegistry()
		}
		return sharedRegistry
	}

	/**
	 * Initializes the shared registry with the Oracle DataSource.
	 */
	private void initializeRegistry() {
		sharedDataSource = createDataSource(oracle)
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up Oracle database connection and schema using Testcontainers.
	 */
	def setupSpec() {
		logger.info('Setting up Oracle Testcontainer')

		if (sharedDataSource == null) {
			initializeRegistry()
		}

		sql = new Sql(sharedDataSource)
		executeScript('ddl/database/oracle/oracle-integration.sql')

		logger.info('Oracle database setup completed')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates an Oracle DataSource from the Testcontainer.
	 *
	 * @param container the Oracle container
	 * @return configured DataSource
	 * @throws SQLException if DataSource creation fails
	 */
	private static DataSource createDataSource(OracleContainer container) throws SQLException {
		def dataSource = new OracleDataSource()
		dataSource.setURL(container.jdbcUrl)
		dataSource.setUser(container.username)
		dataSource.setPassword(container.password)
		return dataSource
	}

	/**
	 * Executes a SQL script from classpath using Groovy features.
	 *
	 * <p>Handles Oracle-specific error codes for DROP statements.
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
				.findAll { !it.empty && !it.startsWith('--') }
				.each { statement ->
					try {
						sql.execute(statement)
					} catch (SQLException e) {
						// Ignore ORA-00942 (table or view does not exist) for DROP statements
						if (e.errorCode == 942 && statement.toUpperCase().contains('DROP TABLE')) {
							logger.debug('Ignoring error for DROP statement: {}', e.message)
						} else {
							throw new RuntimeException("Failed to execute SQL: $statement", e)
						}
					}
				}
	}

	/**
	 * Smoke test verifying basic framework functionality with Oracle Database.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into Oracle
	 *   <li>Data can be verified against expected CSV
	 *   <li>Basic CRUD operations work correctly
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on Oracle'() {
		expect: 'smoke test passes'
		logger.info('Running Oracle integration smoke test')
		true
	}
}
