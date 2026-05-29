package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates nested specifications with convention-based data loading.
 *
 * <p>This specification shows:
 * <ul>
 *   <li>Using nested {@code static} specifications for logical test grouping
 *   <li>Convention-based CSV resolution for nested specifications
 *   <li>Scenario filtering by feature method name within nested groups
 * </ul>
 *
 * <p>The convention resolver derives the resource directory from the runtime class name. A nested
 * specification compiles to {@code NestedConventionSpec$UserTests}, which maps to the directory
 * {@code classpath:example/feature/NestedConventionSpec$UserTests/}.
 */
class NestedConventionSpec extends Specification {

	/**
	 * Nested specification for user-related operations.
	 *
	 * <p>Loads convention-based CSV files from {@code
	 * classpath:example/feature/NestedConventionSpec$UserTests/}.
	 */
	@DatabaseTest
	static class UserTests extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:NestedConventionSpec_User;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/NestedConventionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies convention-based loading and the createUser scenario.
		 */
		@DataSet(sources = @DataSetSource(scenarioNames = 'createUser'))
		@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'createUser'))
		def 'should create new user with convention-based data loading'() {
			when: 'a new user is inserted'
			sql.execute '''
				INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
				VALUES (2, 'jane_doe', 'jane@example.com', true)
			'''

			then: 'expectation verifies both users exist'
			noExceptionThrown()
		}

		/**
		 * Verifies convention-based loading and the updateStatus scenario.
		 */
		@DataSet(sources = @DataSetSource(scenarioNames = 'updateStatus'))
		@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'updateStatus'))
		def 'should update user status with convention-based data loading'() {
			when: 'the user status is updated'
			sql.execute('UPDATE TABLE1 SET COLUMN3 = false WHERE ID = 1')

			then: 'expectation verifies the updated status'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
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

	/**
	 * Nested specification for product-related operations.
	 *
	 * <p>Loads convention-based CSV files from {@code
	 * classpath:example/feature/NestedConventionSpec$ProductTests/}.
	 */
	@DatabaseTest
	static class ProductTests extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:NestedConventionSpec_Product;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/NestedConventionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies convention-based loading and the addProduct scenario.
		 */
		@DataSet(sources = @DataSetSource(scenarioNames = 'addProduct'))
		@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'addProduct'))
		def 'should add new product with convention-based data loading'() {
			when: 'a new product is inserted'
			sql.execute '''
				INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3)
				VALUES (2, 'Tablet', 299.99, 15)
			'''

			then: 'expectation verifies both products exist'
			noExceptionThrown()
		}

		/**
		 * Verifies convention-based loading and the updatePrice scenario.
		 */
		@DataSet(sources = @DataSetSource(scenarioNames = 'updatePrice'))
		@ExpectedDataSet(sources = @DataSetSource(scenarioNames = 'updatePrice'))
		def 'should update product price with convention-based data loading'() {
			when: 'the product price is updated'
			sql.execute('UPDATE TABLE2 SET COLUMN2 = 899.99 WHERE ID = 1')

			then: 'expectation verifies the updated price'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
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
}
