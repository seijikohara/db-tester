package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates table ordering strategies for foreign key constraints using Spock.
 *
 * <p>This specification shows:
 * <ul>
 *   <li>Automatic alphabetical table ordering (default)
 *   <li>Manual ordering via {@code load-order.txt} file
 *   <li>Programmatic ordering via {@code load-order.txt} in custom directories
 *   <li>Handling foreign key constraints
 *   <li>Complex table dependencies (many-to-many relationships)
 * </ul>
 *
 * <p>Table ordering is critical when:
 * <ul>
 *   <li>Tables have foreign key relationships
 *   <li>Parent tables must be loaded before child tables
 *   <li>Junction tables require both parent tables
 *   <li>Deletion order must be reverse of insertion order
 * </ul>
 *
 * <p>Schema:
 * <pre>
 * TABLE1 (parent)
 *   ↓
 * TABLE2 (child of TABLE1)
 *   ↓
 * TABLE3 (independent)
 *   ↓
 * TABLE4 (junction: TABLE2 + TABLE3)
 * </pre>
 */
@DatabaseTest
class TableOrderingStrategiesSpec extends Specification {

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
			setURL('jdbc:h2:mem:TableOrderingStrategiesSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/TableOrderingStrategiesSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates automatic alphabetical table ordering.
	 *
	 * <p>Framework orders tables alphabetically: TABLE1, TABLE2, TABLE3, TABLE4. This works well when
	 * foreign keys follow alphabetical order.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(1,2), TABLE2(1,2,3), TABLE3(1,2,3), TABLE4(3 rows)
	 *   <li>Execution: Inserts TABLE1(3,'Services'), TABLE2(4,3,'Consulting')
	 *   <li>Expectation: Verifies TABLE1 has 3 rows, TABLE2 has 4 rows
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use alphabetical ordering'() {
		when: 'inserting new records following alphabetical order'
		sql.execute "INSERT INTO TABLE1 (ID, COLUMN1) VALUES (3, 'Services')"
		sql.execute "INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (4, 3, 'Consulting')"

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Demonstrates manual table ordering via load-order.txt file.
	 *
	 * <p>Uses {@code load-order.txt} to specify correct insertion order for foreign keys.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2,3), TABLE4(2 rows)
	 *   <li>Execution: Inserts TABLE3(4,'Featured'), TABLE4(1,4)
	 *   <li>Expectation: Verifies TABLE3 has 4 rows, TABLE4 has 3 rows with new association
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use manual ordering'() {
		when: 'inserting records with manual ordering'
		sql.execute "INSERT INTO TABLE3 (ID, COLUMN1) VALUES (4, 'Featured')"
		sql.execute 'INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (1, 4)'

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Demonstrates custom resource location for table ordering.
	 *
	 * <p>Uses {@code load-order.txt} in a custom directory to explicitly control table insertion
	 * order.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE2(1,1,'Widget') from programmatic/ directory
	 *   <li>Execution: Updates TABLE2 COLUMN2 from 'Widget' to 'Updated Widget' WHERE ID=1
	 *   <li>Expectation: Verifies TABLE2(1,1,'Updated Widget')
	 * </ul>
	 */
	@DataSet(
	dataSets = @DataSetSource(
	resourceLocation = 'classpath:example/feature/TableOrderingStrategiesSpec/programmatic/'
	)
	)
	@ExpectedDataSet(
	dataSets = @DataSetSource(
	resourceLocation = 'classpath:example/feature/TableOrderingStrategiesSpec/programmatic/expected/'
	)
	)
	def 'should use programmatic ordering'() {
		when: 'updating a record with programmatic ordering'
		sql.executeUpdate "UPDATE TABLE2 SET COLUMN2 = 'Updated Widget' WHERE ID = 1"

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Demonstrates handling complex many-to-many relationships.
	 *
	 * <p>Shows proper ordering for junction tables with multiple foreign keys.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2), TABLE4(2 rows)
	 *   <li>Execution: Adds TABLE1(4,'Accessories'), TABLE2(5,4,'Cable'), TABLE3(5,'Essential'),
	 *       TABLE4(5,1), TABLE4(5,5)
	 *   <li>Expectation: Verifies all 4 tables have new records with proper foreign key relationships
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def 'should handle many to many relationships'() {
		when: 'inserting records with many-to-many relationships'
		sql.execute "INSERT INTO TABLE1 (ID, COLUMN1) VALUES (4, 'Accessories')"
		sql.execute "INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (5, 4, 'Cable')"
		sql.execute "INSERT INTO TABLE3 (ID, COLUMN1) VALUES (5, 'Essential')"
		sql.execute 'INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 1)'
		sql.execute 'INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 5)'

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
