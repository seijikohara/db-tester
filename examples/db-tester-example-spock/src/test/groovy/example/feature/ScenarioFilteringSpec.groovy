package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates scenario-based testing with CSV row filtering using Spock.
 *
 * <p>This specification illustrates the scenario filtering feature that enables sharing
 * a single CSV file across multiple feature methods. Each test automatically loads only
 * rows matching its method name from the {@code [Scenario]} marker column.
 *
 * <p>Features demonstrated:
 * <ul>
 *   <li>Using scenario marker column for row filtering
 *   <li>Sharing a single CSV file across multiple feature methods
 *   <li>Feature method name as automatic scenario filter
 *   <li>Reducing CSV file duplication
 *   <li>Class-level {@code @Preparation} and {@code @Expectation} annotations
 * </ul>
 *
 * <p>CSV files contain scenario marker column that filters rows by feature method name:
 * <pre>
 * [Scenario],ID,COLUMN1,COLUMN2,COLUMN3
 * should create active user,1,alice,alice@example.com,ACTIVE
 * should create inactive user,1,bob,bob@example.com,INACTIVE
 * </pre>
 */
@Preparation
@Expectation
class ScenarioFilteringSpec extends Specification {

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Registry for DB Tester extension. */
	@Shared
	DataSourceRegistry dbTesterRegistry

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/**
	 * Sets up H2 in-memory database connection and schema.
	 */
	def setupSpec() {
		dataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ScenarioFilteringSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}

		dbTesterRegistry = new DataSourceRegistry()
		dbTesterRegistry.registerDefault(dataSource)

		sql = new Sql(dataSource)
		executeScript('ddl/feature/ScenarioFilteringSpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates scenario filtering for active user creation.
	 *
	 * <p>Only rows matching the feature method name are loaded from TABLE1.csv
	 * using the {@code [Scenario]} marker column.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (alice, ACTIVE) filtered by scenario name
	 *   <li>Execution: Inserts ID=2 (charlie, ACTIVE)
	 *   <li>Expectation: Verifies both records exist with ACTIVE status
	 * </ul>
	 */
	def 'should create active user'() {
		when: 'creating a new active user'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (2, 'charlie', 'charlie@example.com', 'ACTIVE')
		'''

		then: 'database state matches expected'
		noExceptionThrown()
	}

	/**
	 * Demonstrates scenario filtering for inactive user creation.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (bob, INACTIVE) filtered by scenario name
	 *   <li>Execution: Inserts ID=2 (david, INACTIVE)
	 *   <li>Expectation: Verifies both records exist with INACTIVE status
	 * </ul>
	 */
	def 'should create inactive user'() {
		when: 'creating a new inactive user'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (2, 'david', 'david@example.com', 'INACTIVE')
		'''

		then: 'database state matches expected'
		noExceptionThrown()
	}

	/**
	 * Demonstrates scenario filtering with multiple existing users.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads ID=1 (eve, ACTIVE) and ID=2 (frank, INACTIVE)
	 *   <li>Execution: Updates ID=2 status from INACTIVE to SUSPENDED
	 *   <li>Expectation: Verifies ID=1 remains ACTIVE and ID=2 is SUSPENDED
	 * </ul>
	 */
	def 'should handle multiple users'() {
		when: 'suspending an inactive user'
		sql.execute "UPDATE TABLE1 SET COLUMN3 = 'SUSPENDED' WHERE ID = 2"

		then: 'database state matches expected'
		noExceptionThrown()
	}

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
