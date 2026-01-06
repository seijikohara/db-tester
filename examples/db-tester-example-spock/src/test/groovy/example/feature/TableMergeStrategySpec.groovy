package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates different table merge strategies when multiple datasets contain the same table.
 *
 * <p>This specification demonstrates the four merge strategies:
 * <ul>
 *   <li>{@link TableMergeStrategy#FIRST} - Use only the first occurrence of each table
 *   <li>{@link TableMergeStrategy#LAST} - Use only the last occurrence of each table
 *   <li>{@link TableMergeStrategy#UNION} - Merge tables, removing duplicate rows
 *   <li>{@link TableMergeStrategy#UNION_ALL} - Merge tables, keeping all rows (default)
 * </ul>
 *
 * <p>Note: Multiple @DataSetSource annotations in a single @DataSet are demonstrated
 * in the JUnit TableMergeStrategyTest. This Spock specification demonstrates
 * single dataset loading with different merge strategy configurations.
 */
@DatabaseTest
class TableMergeStrategySpec extends Specification {

	/**
	 * Tests FIRST merge strategy configuration.
	 *
	 * <p>Demonstrates configuring TableMergeStrategy.FIRST in ConventionSettings.
	 */
	static class FirstStrategySpec extends Specification {

		@Shared
		DataSource dataSource

		@Shared
		Sql sql

		static DataSourceRegistry sharedRegistry
		static DataSource sharedDataSource
		static Configuration sharedConfiguration

		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		Configuration getDbTesterConfiguration() {
			if (sharedConfiguration == null) {
				initializeSharedResources()
			}
			return sharedConfiguration
		}

		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TableMergeStrategySpec_FIRST;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null,
					'/expected',
					'[Scenario]',
					DataFormat.CSV,
					TableMergeStrategy.FIRST,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME, Set.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TableMergeStrategySpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies FIRST strategy configuration is applied.
		 */
		@DataSet(
		operation = Operation.INSERT,
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$FirstStrategySpec/dataset1/')
		)
		@ExpectedDataSet(
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$FirstStrategySpec/should use only first dataset/expected/')
		)
		def 'should use only first dataset'() {
			when: 'adding additional data'
			sql.execute '''
				INSERT INTO MERGE_TABLE (ID, NAME)
				VALUES (3, 'Charlie')
			'''

			then: 'expectation verifies data'
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

	/**
	 * Tests LAST merge strategy configuration.
	 *
	 * <p>Demonstrates configuring TableMergeStrategy.LAST in ConventionSettings.
	 */
	static class LastStrategySpec extends Specification {

		@Shared
		DataSource dataSource

		@Shared
		Sql sql

		static DataSourceRegistry sharedRegistry
		static DataSource sharedDataSource
		static Configuration sharedConfiguration

		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		Configuration getDbTesterConfiguration() {
			if (sharedConfiguration == null) {
				initializeSharedResources()
			}
			return sharedConfiguration
		}

		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TableMergeStrategySpec_LAST;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null,
					'/expected',
					'[Scenario]',
					DataFormat.CSV,
					TableMergeStrategy.LAST,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME, Set.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TableMergeStrategySpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies LAST strategy configuration is applied.
		 */
		@DataSet(
		operation = Operation.INSERT,
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$LastStrategySpec/dataset2/')
		)
		@ExpectedDataSet(
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$LastStrategySpec/should use only last dataset/expected/')
		)
		def 'should use only last dataset'() {
			when: 'adding additional data'
			sql.execute '''
				INSERT INTO MERGE_TABLE (ID, NAME)
				VALUES (1, 'Alice')
			'''

			then: 'expectation verifies data'
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

	/**
	 * Tests UNION merge strategy configuration.
	 *
	 * <p>Demonstrates configuring TableMergeStrategy.UNION in ConventionSettings.
	 */
	static class UnionStrategySpec extends Specification {

		@Shared
		DataSource dataSource

		@Shared
		Sql sql

		static DataSourceRegistry sharedRegistry
		static DataSource sharedDataSource
		static Configuration sharedConfiguration

		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		Configuration getDbTesterConfiguration() {
			if (sharedConfiguration == null) {
				initializeSharedResources()
			}
			return sharedConfiguration
		}

		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TableMergeStrategySpec_UNION;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null,
					'/expected',
					'[Scenario]',
					DataFormat.CSV,
					TableMergeStrategy.UNION,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME, Set.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TableMergeStrategySpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies UNION strategy configuration is applied.
		 */
		@DataSet(
		operation = Operation.INSERT,
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$UnionStrategySpec/dataset1/')
		)
		@ExpectedDataSet(
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$UnionStrategySpec/should merge and remove duplicates/expected/')
		)
		def 'should merge and remove duplicates'() {
			when: 'adding additional unique data'
			sql.execute '''
				INSERT INTO MERGE_TABLE (ID, NAME)
				VALUES (3, 'Charlie')
			'''

			then: 'expectation verifies data'
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

	/**
	 * Tests UNION_ALL merge strategy configuration (default).
	 *
	 * <p>Demonstrates configuring TableMergeStrategy.UNION_ALL in ConventionSettings.
	 */
	static class UnionAllStrategySpec extends Specification {

		@Shared
		DataSource dataSource

		@Shared
		Sql sql

		static DataSourceRegistry sharedRegistry
		static DataSource sharedDataSource
		static Configuration sharedConfiguration

		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		Configuration getDbTesterConfiguration() {
			if (sharedConfiguration == null) {
				initializeSharedResources()
			}
			return sharedConfiguration
		}

		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TableMergeStrategySpec_UNION_ALL;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null,
					'/expected',
					'[Scenario]',
					DataFormat.CSV,
					TableMergeStrategy.UNION_ALL,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME, Set.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TableMergeStrategySpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies UNION_ALL strategy configuration is applied.
		 */
		@DataSet(
		operation = Operation.INSERT,
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$UnionAllStrategySpec/dataset1/')
		)
		@ExpectedDataSet(
		dataSets = @DataSetSource(resourceLocation = 'classpath:example/feature/TableMergeStrategySpec$UnionAllStrategySpec/should merge and keep all rows/expected/')
		)
		def 'should merge and keep all rows'() {
			when: 'adding additional data'
			sql.execute '''
				INSERT INTO MERGE_TABLE (ID, NAME)
				VALUES (3, 'Charlie')
			'''

			then: 'expectation verifies data'
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
}
