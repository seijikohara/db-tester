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
 * Demonstrates different data format configurations (CSV and TSV) with Spock.
 *
 * <p>This specification demonstrates:
 * <ul>
 *   <li>Using CSV format (default) with {@link DataFormat#CSV}
 *   <li>Using TSV format with {@link DataFormat#TSV}
 *   <li>Configuring data format via {@link ConventionSettings}
 * </ul>
 *
 * <p>CSV files use comma (,) as delimiter, TSV files use tab character as delimiter.
 */
@DatabaseTest
class DataFormatSpec extends Specification {

	/**
	 * Tests CSV format (default configuration).
	 *
	 * <p>CSV files use comma as field delimiter:
	 * <pre>
	 * ID,NAME,DATA_VALUE
	 * 1,Alice,100
	 * 2,Bob,200
	 * </pre>
	 */
	static class CsvFormatSpec extends Specification {

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
				setURL('jdbc:h2:mem:DataFormatSpec_CSV;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			// CSV is the default format, but we explicitly configure it for clarity
			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null, // classpath-relative
					'/expected', // default expectation suffix
					'[Scenario]', // default scenario marker
					DataFormat.CSV, // CSV format
					TableMergeStrategy.UNION_ALL,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
					Set.of(),
					Map.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/DataFormatSpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that CSV format files are loaded correctly.
		 *
		 * <p>Test flow:
		 * <ul>
		 *   <li>Preparation: Loads data from CSV file (comma-separated)
		 *   <li>Execution: Inserts additional record
		 *   <li>Expectation: Verifies data from expected CSV file
		 * </ul>
		 */
		@DataSet(
		operation = Operation.INSERT,
		sources = [
			@DataSetSource(
			resourceLocation = 'classpath:example/feature/DataFormatSpec$CsvFormatSpec/should load CSV format data/'
			)
		]
		)
		@ExpectedDataSet(
		sources = [
			@DataSetSource(
			resourceLocation = 'classpath:example/feature/DataFormatSpec$CsvFormatSpec/should load CSV format data/expected/'
			)
		]
		)
		def 'should load CSV format data'() {
			when: 'inserting a new record'
			sql.execute '''
				INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE)
				VALUES (3, 'Charlie', 300)
			'''

			then: 'expectation phase verifies database state'
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
	 * Tests TSV format configuration.
	 *
	 * <p>TSV files use tab as field delimiter:
	 * <pre>
	 * ID	NAME	DATA_VALUE
	 * 1	Alice	100
	 * 2	Bob	200
	 * </pre>
	 */
	static class TsvFormatSpec extends Specification {

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
				setURL('jdbc:h2:mem:DataFormatSpec_TSV;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)

			// Configure TSV format
			sharedConfiguration = Configuration.withConventions(
					new ConventionSettings(
					null, // classpath-relative
					'/expected', // default expectation suffix
					'[Scenario]', // default scenario marker
					DataFormat.TSV, // TSV format
					TableMergeStrategy.UNION_ALL,
					ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
					Set.of(),
					Map.of()
					)
					)
		}

		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/DataFormatSpec.sql')
		}

		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that TSV format files are loaded correctly.
		 *
		 * <p>Test flow:
		 * <ul>
		 *   <li>Preparation: Loads data from TSV file (tab-separated)
		 *   <li>Execution: Inserts additional record
		 *   <li>Expectation: Verifies data from expected TSV file
		 * </ul>
		 */
		@DataSet(
		operation = Operation.INSERT,
		sources = [
			@DataSetSource(
			resourceLocation = 'classpath:example/feature/DataFormatSpec$TsvFormatSpec/should load TSV format data/'
			)
		]
		)
		@ExpectedDataSet(
		sources = [
			@DataSetSource(
			resourceLocation = 'classpath:example/feature/DataFormatSpec$TsvFormatSpec/should load TSV format data/expected/'
			)
		]
		)
		def 'should load TSV format data'() {
			when: 'inserting a new record'
			sql.execute '''
				INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE)
				VALUES (3, 'Charlie', 300)
			'''

			then: 'expectation phase verifies database state'
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
