package example.database.cassandra

import com.ing.data.cassandra.jdbc.CassandraDataSource
import com.ing.data.cassandra.jdbc.utils.ContactPoint
import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.cassandra.CassandraContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

/**
 * Apache Cassandra integration test using Testcontainers.
 *
 * <p>This specification validates that the framework works correctly with Apache Cassandra
 * using Testcontainers. This is a smoke test to ensure Cassandra compatibility with the
 * DB Tester framework.
 *
 * <p>Cassandra is a distributed wide-column NoSQL database that uses CQL (Cassandra Query
 * Language). The JDBC wrapper provides SQL-like access to Cassandra data.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
@Testcontainers
@DatabaseTest
class CassandraIntegrationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(CassandraIntegrationSpec)

	/** Keyspace name for test data. */
	private static final String KEYSPACE = 'test_keyspace'

	/** Cassandra container for integration testing. */
	@Shared
	CassandraContainer cassandra = new CassandraContainer('cassandra:4.1')

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
	 * Initializes the shared registry with the Cassandra DataSource.
	 */
	private void initializeRegistry() {
		createKeyspace()
		sharedDataSource = createDataSource(cassandra)
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Creates the keyspace for testing.
	 */
	private void createKeyspace() {
		def jdbcUrl = "jdbc:cassandra://${cassandra.host}:${cassandra.getMappedPort(9042)}/system_schema"
		def systemSql = Sql.newInstance(jdbcUrl)
		try {
			systemSql.execute """
				CREATE KEYSPACE IF NOT EXISTS $KEYSPACE
				WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
			"""
		} finally {
			systemSql.close()
		}
	}

	/**
	 * Sets up Cassandra database connection and schema using Testcontainers.
	 */
	def setupSpec() {
		logger.info('Setting up Cassandra Testcontainer')

		if (sharedDataSource == null) {
			initializeRegistry()
		}

		sql = new Sql(sharedDataSource)
		executeScript('ddl/database/cassandra/cassandra-integration.cql')

		logger.info('Cassandra database setup completed')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates a Cassandra DataSource from the Testcontainer.
	 *
	 * @param container the Cassandra container
	 * @return configured DataSource
	 */
	private static DataSource createDataSource(CassandraContainer container) {
		def contactPoints = [
			ContactPoint.of(container.host, container.getMappedPort(9042))
		]
		def dataSource = new CassandraDataSource(contactPoints, KEYSPACE)
		dataSource.localDataCenter = 'datacenter1'
		return dataSource
	}

	/**
	 * Executes a CQL script from classpath using Groovy features.
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
				.each { sql.execute(it) }
	}

	/**
	 * Smoke test verifying basic framework functionality with Cassandra.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into Cassandra tables
	 *   <li>Data can be verified against expected CSV via CQL queries
	 *   <li>Framework handles wide-column database results correctly
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on Cassandra'() {
		expect: 'smoke test passes'
		logger.info('Running Cassandra integration smoke test')
		true
	}
}
