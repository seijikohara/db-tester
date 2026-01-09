package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Base specification demonstrating annotation inheritance for database tests with Spock.
 *
 * <p>This base specification provides:
 * <ul>
 *   <li>Class-level {@code @DataSet} annotation inherited by subclasses
 *   <li>Common database setup and utility methods
 *   <li>Reusable test infrastructure
 * </ul>
 *
 * <p>Child specifications inherit:
 * <ul>
 *   <li>The class-level {@code @DataSet} annotation
 *   <li>Database setup and helper methods
 * </ul>
 *
 * @see InheritedAnnotationSpec
 */
@DatabaseTest
@DataSet(
sources = @DataSetSource(
resourceLocation = 'classpath:example/feature/InheritanceSpecBase/',
scenarioNames = 'baseSetup'
)
)
abstract class InheritanceSpecBase extends Specification {

	/** Shared DataSource for all feature methods. */
	@Shared
	protected static DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	protected Sql sql

	/** Static registry and DataSource shared across all tests. */
	protected static DataSourceRegistry sharedRegistry
	protected static DataSource sharedDataSource

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
	protected static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:InheritanceSpecBase;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database connection and schema.
	 *
	 * <p>This method is inherited by subclasses and provides shared database initialization.
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
		executeScript('ddl/feature/InheritanceSpecBase.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Gets the count of records in a table.
	 *
	 * @param tableName the table name
	 * @return the record count
	 */
	protected int getRecordCount(String tableName) {
		sql.firstRow("SELECT COUNT(*) as cnt FROM $tableName".toString()).cnt as int
	}

	/**
	 * Executes a SQL script from classpath using Groovy 5 features.
	 *
	 * @param scriptPath the classpath resource path
	 */
	protected void executeScript(String scriptPath) {
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
