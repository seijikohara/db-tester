package example.database.neo4j

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.neo4j.jdbc.Neo4jDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.neo4j.Neo4jContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

/**
 * Neo4j Graph Database integration test using Testcontainers.
 *
 * <p>This specification validates that the framework works correctly with Neo4j graph database
 * using Testcontainers. This is a smoke test to ensure Neo4j compatibility with the DB Tester
 * framework.
 *
 * <p>Neo4j is a graph database that stores data as nodes and relationships. The JDBC driver
 * returns query results in tabular format compatible with standard JDBC operations.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
@Testcontainers
@DatabaseTest
class Neo4jIntegrationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(Neo4jIntegrationSpec)

	/** Neo4j container for integration testing. */
	@Shared
	Neo4jContainer neo4j = new Neo4jContainer('neo4j:5-community')
	.withoutAuthentication()

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
	 * Initializes the shared registry with the Neo4j DataSource.
	 */
	private void initializeRegistry() {
		sharedDataSource = createDataSource(neo4j)
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up Neo4j database connection and schema using Testcontainers.
	 */
	def setupSpec() {
		logger.info('Setting up Neo4j Testcontainer')

		if (sharedDataSource == null) {
			initializeRegistry()
		}

		sql = new Sql(sharedDataSource)
		executeScript('ddl/database/neo4j/neo4j-integration.cypher')

		logger.info('Neo4j database setup completed')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Creates a Neo4j DataSource from the Testcontainer.
	 *
	 * @param container the Neo4j container
	 * @return configured DataSource
	 */
	private static DataSource createDataSource(Neo4jContainer container) {
		def dataSource = new Neo4jDataSource()
		// Neo4j JDBC driver 6.x URL format: jdbc:neo4j://<host>:<port>
		// enableSQLTranslation: Automatically translate SQL to Cypher
		def jdbcUrl = container.boltUrl.replace('bolt://', 'jdbc:neo4j://') + '?enableSQLTranslation=true'
		dataSource.setUrl(jdbcUrl)
		return dataSource
	}

	/**
	 * Executes a Cypher script from classpath using Groovy features.
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
				.findAll { !it.empty && !it.startsWith('//') }
				.each { sql.execute(it) }
	}

	/**
	 * Smoke test verifying basic framework functionality with Neo4j.
	 *
	 * <p>This test validates:
	 * <ul>
	 *   <li>Data can be loaded from CSV into Neo4j nodes
	 *   <li>Data can be verified against expected CSV via Cypher queries
	 *   <li>Framework handles graph database results correctly
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'smokeTest'))
	def 'should execute basic database operations on Neo4j'() {
		expect: 'smoke test passes'
		logger.info('Running Neo4j integration smoke test')
		true
	}
}
