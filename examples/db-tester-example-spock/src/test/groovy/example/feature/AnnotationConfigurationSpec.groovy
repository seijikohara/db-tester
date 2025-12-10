package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates advanced annotation configuration features using Spock.
 *
 * <p>This specification shows:
 * <ul>
 *   <li>Explicit {@code resourceLocation} specification
 *   <li>Multiple {@code scenarioNames} in a single DataSet
 *   <li>Class-level vs method-level annotation precedence
 *   <li>Custom directory structure
 *   <li>Multiple tables with foreign key relationships
 * </ul>
 *
 * <p>Directory structure:
 * <pre>
 * example/feature/AnnotationConfigurationSpec/
 *   custom-location/
 *     TABLE1.csv
 *     TABLE2.csv
 *   expected/
 *     TABLE1.csv
 *     TABLE2.csv
 * </pre>
 */
@Preparation(
dataSets = @DataSet(
resourceLocation = 'classpath:example/feature/AnnotationConfigurationSpec/',
scenarioNames = 'classLevel'
)
)
@DatabaseTest
class AnnotationConfigurationSpec extends Specification {

	@Shared
	DataSource dataSource

	@Shared
	DataSourceRegistry dbTesterRegistry

	@Shared
	Sql sql

	def setupSpec() {
		dataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:AnnotationConfigurationSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}

		dbTesterRegistry = new DataSourceRegistry()
		dbTesterRegistry.registerDefault(dataSource)

		sql = new Sql(dataSource)
		executeScript('ddl/feature/AnnotationConfigurationSpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates explicit resource location specification.
	 *
	 * <p>Uses custom directory path instead of convention-based resolution.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads from {@code custom-location/} - TABLE1(ID=1,2), TABLE2(ID=1,2)
	 *   <li>Execution: Inserts ID=3 (Marketing, Tokyo) into TABLE1
	 *   <li>Expectation: Verifies all three departments and two employees exist
	 * </ul>
	 */
	@Preparation(
	dataSets = @DataSet(
	resourceLocation = 'classpath:example/feature/AnnotationConfigurationSpec/custom-location/'
	)
	)
	@Expectation
	def 'should use custom resource location'() {
		when: 'inserting a new department'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
			VALUES (3, 'Marketing', 'Tokyo')
		'''

		then: 'all three departments exist'
		noExceptionThrown()
	}

	/**
	 * Demonstrates multiple scenario names in a single test.
	 *
	 * <p>Loads rows matching either scenario name from the same CSV files.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads scenario1 and scenario2 - TABLE1(ID=1,2), TABLE2(ID=1,2)
	 *   <li>Execution: Updates Bob Smith's salary from 60000.00 to 65000.00
	 *   <li>Expectation: Verifies both departments and updated employee salary
	 * </ul>
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = ['scenario1', 'scenario2']))
	@Expectation
	def 'should handle multiple scenarios'() {
		when: 'updating employee salary'
		sql.executeUpdate 'UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2'

		then: 'salary is updated'
		noExceptionThrown()
	}

	/**
	 * Demonstrates multiple scenario names for preparation and expectation.
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = ['scenario1', 'scenario2']))
	@Expectation(dataSets = @DataSet(scenarioNames = 'should merge multiple data sets'))
	def 'should merge multiple data sets'() {
		when: 'updating employee salary'
		sql.executeUpdate 'UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2'

		then: 'salary is updated'
		noExceptionThrown()
	}

	/**
	 * Demonstrates class-level annotation inheritance.
	 *
	 * <p>This test uses the class-level {@code @Preparation} annotation defined at the class level.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Uses class-level @Preparation with scenario "classLevel"
	 *   <li>Execution: Inserts new employee (ID=100, New Employee, 45000.00) into TABLE2
	 *   <li>Expectation: Verifies HR department and two employees
	 * </ul>
	 */
	@Expectation
	def 'should use class level annotation'() {
		when: 'inserting a new employee'
		sql.execute '''
			INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (100, 'New Employee', 1, 45000.00)
		'''

		then: 'new employee exists'
		noExceptionThrown()
	}

	/**
	 * Demonstrates using different scenarios for preparation and expectation.
	 */
	@Preparation(dataSets = @DataSet(scenarioNames = 'multiDataSet1'))
	@Expectation(dataSets = @DataSet(scenarioNames = 'multiDataSet'))
	def 'should handle multiple data sets'() {
		when: 'inserting a new department'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
			VALUES (4, 'Research', 'Osaka')
		'''

		then: 'new department exists'
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
