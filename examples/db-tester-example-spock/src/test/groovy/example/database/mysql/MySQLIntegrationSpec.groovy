package example.database.mysql

import com.mysql.cj.jdbc.MysqlDataSource
import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

/**
 * MySQL integration test using Testcontainers.
 *
 * <p>This specification validates that the framework works correctly with MySQL database
 * using Testcontainers. This is a smoke test to ensure MySQL compatibility.
 *
 * <p>The test uses {@code @Testcontainers} annotation from testcontainers-spock module
 * to automatically manage the container lifecycle. The {@code @Shared} annotation ensures
 * the container is not restarted between feature methods.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
@Testcontainers
@DatabaseTest
class MySQLIntegrationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(MySQLIntegrationSpec)

	/** MySQL container for integration testing. */
	@Shared
	MySQLContainer mysql = new MySQLContainer('mysql:latest')
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
	 * Initializes the shared registry with the MySQL DataSource.
	 */
	private void initializeRegistry() {
		sharedDataSource = createDataSource(mysql)
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up MySQL database connection and schema using Testcontainers.
	 */
	def setupSpec() {
		logger.info('Setting up MySQL Testcontainer')

		if (sharedDataSource == null) {
			initializeRegistry()
		}

		sql = new Sql(sharedDataSource)
		executeScript('ddl/database/mysql/mysql-integration.sql')

		logger.info('MySQL database setup completed')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates a MySQL DataSource from the Testcontainer.
	 *
	 * @param container the MySQL container
	 * @return configured DataSource
	 */
	private static DataSource createDataSource(MySQLContainer container) {
		def dataSource = new MysqlDataSource()
		dataSource.setURL(container.jdbcUrl)
		dataSource.setUser(container.username)
		dataSource.setPassword(container.password)
		return dataSource
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

		resource.text
				.split(';')
				.collect { it.trim() }
				.findAll { !it.empty }
				.each { sql.execute(it) }
	}

	/**
	 * Smoke test verifying basic framework functionality with MySQL.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into MySQL
	 *   <li>Data can be verified against expected CSV
	 *   <li>Basic CRUD operations work correctly
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on MySQL'() {
		expect: 'smoke test passes'
		logger.info('Running MySQL integration smoke test')
		true
	}
}
