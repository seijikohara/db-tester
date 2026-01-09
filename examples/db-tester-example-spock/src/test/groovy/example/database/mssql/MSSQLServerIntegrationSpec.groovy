package example.database.mssql

import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.mssqlserver.MSSQLServerContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

/**
 * Microsoft SQL Server integration test using Testcontainers.
 *
 * <p>This specification validates that the framework works correctly with SQL Server database
 * using Testcontainers. This is a smoke test to ensure SQL Server compatibility.
 *
 * <p>The test uses {@code @Testcontainers} annotation from testcontainers-spock module
 * to automatically manage the container lifecycle. The {@code @Shared} annotation ensures
 * the container is not restarted between feature methods.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
@Testcontainers
@DatabaseTest
class MSSQLServerIntegrationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(MSSQLServerIntegrationSpec)

	/** SQL Server container for integration testing. */
	@Shared
	MSSQLServerContainer mssql = new MSSQLServerContainer('mcr.microsoft.com/mssql/server:latest')
	.acceptLicense()
	.withPassword('StrongPassword123!')

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
	 * Initializes the shared registry with the SQL Server DataSource.
	 */
	private void initializeRegistry() {
		sharedDataSource = createDataSource(mssql)
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up SQL Server database connection and schema using Testcontainers.
	 */
	def setupSpec() {
		logger.info('Setting up SQL Server Testcontainer')

		if (sharedDataSource == null) {
			initializeRegistry()
		}

		sql = new Sql(sharedDataSource)
		executeScript('ddl/database/mssql/mssql-integration.sql')

		logger.info('SQL Server database setup completed')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates a SQL Server DataSource from the Testcontainer.
	 *
	 * @param container the SQL Server container
	 * @return configured DataSource
	 */
	private static DataSource createDataSource(MSSQLServerContainer container) {
		def dataSource = new SQLServerDataSource()
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
	 * Smoke test verifying basic framework functionality with SQL Server.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into SQL Server
	 *   <li>Data can be verified against expected CSV
	 *   <li>Basic CRUD operations work correctly
	 * </ul>
	 */
	@DataSet(sources = @DataSetSource(scenarioNames = 'smokeTest'))
	@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on SQL Server'() {
		expect: 'smoke test passes'
		logger.info('Running SQL Server integration smoke test')
		true
	}
}
