package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.Strategy
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates annotation-based column comparison strategies using {@code @ColumnStrategy} with Spock.
 *
 * <p>This specification demonstrates how to configure column-level comparison strategies via annotations:
 * <ul>
 *   <li>{@link Strategy#IGNORE} - Skip comparison for auto-generated columns
 *   <li>{@link Strategy#CASE_INSENSITIVE} - Compare strings ignoring case differences
 *   <li>{@link Strategy#REGEX} - Match against regular expression pattern
 * </ul>
 *
 * <p>Directory structure:
 * <pre>
 * example/feature/ColumnStrategyAnnotationSpec/
 *   USERS.csv
 *   expected/
 *     USERS.csv
 * </pre>
 *
 * @see ColumnStrategy
 * @see Strategy
 */
@DatabaseTest
class ColumnStrategyAnnotationSpec extends Specification {

	@Shared
	DataSource dataSource

	@Shared
	Sql sql

	static DataSourceRegistry sharedRegistry
	static DataSource sharedDataSource

	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeSharedResources()
		}
		return sharedRegistry
	}

	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ColumnStrategyAnnotationSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	def setupSpec() {
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)
		executeScript('ddl/feature/ColumnStrategyAnnotationSpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	// ==================== IGNORE Strategy Tests ====================

	@DataSet
	@ExpectedDataSet(sources = @DataSetSource(
	columnStrategies = @ColumnStrategy(name = 'CREATED_AT', strategy = Strategy.IGNORE)
	))
	def 'ignore strategy should skip auto-generated timestamp column'() {
		when: 'inserting user with auto-generated timestamp'
		sql.execute('''
			INSERT INTO USERS (ID, NAME, EMAIL, CREATED_AT)
			VALUES (2, 'Bob', 'bob@example.com', CURRENT_TIMESTAMP)
		''')

		then: 'comparison passes - CREATED_AT is ignored'
		noExceptionThrown()
	}

	// ==================== CASE_INSENSITIVE Strategy Tests ====================

	@DataSet
	@ExpectedDataSet(sources = @DataSetSource(
	columnStrategies = [
		@ColumnStrategy(name = 'EMAIL', strategy = Strategy.CASE_INSENSITIVE),
		@ColumnStrategy(name = 'CREATED_AT', strategy = Strategy.IGNORE)
	]
	))
	def 'case-insensitive strategy should compare email ignoring case'() {
		when: 'inserting user with lowercase email (normalized by application)'
		sql.execute('''
			INSERT INTO USERS (ID, NAME, EMAIL, CREATED_AT)
			VALUES (2, 'Charlie', 'charlie@example.com', CURRENT_TIMESTAMP)
		''')

		then: 'comparison passes - expected has uppercase, actual has lowercase'
		noExceptionThrown()
	}

	// ==================== REGEX Strategy Tests ====================

	@DataSet
	@ExpectedDataSet(sources = @DataSetSource(
	columnStrategies = [
		@ColumnStrategy(
		name = 'TOKEN',
		strategy = Strategy.REGEX,
		pattern = '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}'
		),
		@ColumnStrategy(name = 'CREATED_AT', strategy = Strategy.IGNORE)
	]
	))
	def 'regex strategy should validate UUID format using pattern'() {
		when: 'inserting user with auto-generated UUID token'
		sql.execute('''
			INSERT INTO USERS (ID, NAME, EMAIL, TOKEN, CREATED_AT)
			VALUES (2, 'Diana', 'diana@example.com', RANDOM_UUID(), CURRENT_TIMESTAMP)
		''')

		then: 'comparison passes - TOKEN matches UUID pattern'
		noExceptionThrown()
	}

	// ==================== Multiple Strategies Tests ====================

	@DataSet
	@ExpectedDataSet(sources = @DataSetSource(
	columnStrategies = [
		@ColumnStrategy(name = 'CREATED_AT', strategy = Strategy.IGNORE),
		@ColumnStrategy(name = 'EMAIL', strategy = Strategy.CASE_INSENSITIVE),
		@ColumnStrategy(
		name = 'TOKEN',
		strategy = Strategy.REGEX,
		pattern = '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}'
		)
	]
	))
	def 'multiple column strategies should be applied correctly'() {
		when: 'inserting user with multiple strategy-applicable columns'
		sql.execute('''
			INSERT INTO USERS (ID, NAME, EMAIL, TOKEN, CREATED_AT)
			VALUES (2, 'Eve', 'eve@example.com', RANDOM_UUID(), CURRENT_TIMESTAMP)
		''')

		then: 'comparison passes - each column uses its configured strategy'
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
