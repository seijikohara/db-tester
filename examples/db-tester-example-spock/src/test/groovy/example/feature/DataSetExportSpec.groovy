package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.export.DataSetExporter
import io.github.seijikohara.dbtester.api.export.ExportConfiguration
import io.github.seijikohara.dbtester.api.export.LobHandling
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Demonstrates the {@link DataSetExporter} facade for exporting database state to files.
 *
 * <p>This specification covers:
 * <ul>
 *   <li>{@link DataSetExporter#csv} - Export to CSV format
 *   <li>{@link DataSetExporter#json} - Export to JSON format
 *   <li>{@link DataSetExporter#yaml} - Export to YAML format
 *   <li>{@link ExportConfiguration} - Custom export settings including {@link LobHandling}
 * </ul>
 *
 * <p>The export API generates dataset files from existing database state. Exported files follow the
 * same format as input dataset files and can be used as expected datasets.
 */
class DataSetExportSpec extends Specification {

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/** Static DataSource shared across all features. */
	static DataSource sharedDataSource

	/**
	 * Sets up the H2 in-memory database, schema, and seed data.
	 */
	def setupSpec() {
		if (sharedDataSource == null) {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:DataSetExportSpec;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
		}
		dataSource = sharedDataSource
		sql = new Sql(dataSource)
		executeScript('ddl/feature/DataSetExportSpec.sql')
		sql.execute "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (1, 'Alice', 99.99)"
		sql.execute "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (2, 'Bob', 149.50)"
		sql.execute "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (3, 'Charlie', 75.00)"
	}

	/**
	 * Closes the SQL helper.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Verifies that database state exports to CSV format.
	 *
	 * @param tempDir temporary directory for export output
	 */
	def 'should export database state to CSV file'(@TempDir Path tempDir) {
		when: 'the database state is exported to CSV'
		DataSetExporter.csv(dataSource, ['EXPORT_DATA'], tempDir)

		then: 'the exported CSV file contains every row'
		def exportedFile = tempDir.resolve('EXPORT_DATA.csv')
		Files.exists(exportedFile)
		def content = Files.readString(exportedFile, StandardCharsets.UTF_8)
		content.contains('Alice')
		content.contains('Bob')
		content.contains('Charlie')
	}

	/**
	 * Verifies that database state exports to JSON format.
	 *
	 * @param tempDir temporary directory for export output
	 */
	def 'should export database state to JSON file'(@TempDir Path tempDir) {
		when: 'the database state is exported to JSON'
		DataSetExporter.json(dataSource, ['EXPORT_DATA'], tempDir)

		then: 'the exported JSON file contains the expected rows'
		def exportedFile = tempDir.resolve('EXPORT_DATA.json')
		Files.exists(exportedFile)
		def content = Files.readString(exportedFile, StandardCharsets.UTF_8)
		content.contains('Alice')
		content.contains('Bob')
	}

	/**
	 * Verifies that database state exports to YAML format.
	 *
	 * @param tempDir temporary directory for export output
	 */
	def 'should export database state to YAML file'(@TempDir Path tempDir) {
		when: 'the database state is exported to YAML'
		DataSetExporter.yaml(dataSource, ['EXPORT_DATA'], tempDir)

		then: 'the exported YAML file contains the expected rows'
		def exportedFile = tempDir.resolve('EXPORT_DATA.yaml')
		Files.exists(exportedFile)
		def content = Files.readString(exportedFile, StandardCharsets.UTF_8)
		content.contains('Alice')
		content.contains('Bob')
	}

	/**
	 * Verifies that export configuration customizes output behavior.
	 *
	 * <p>Demonstrates using {@link ExportConfiguration.Builder} to customize null value
	 * representation and {@link LobHandling} strategy.
	 *
	 * @param tempDir temporary directory for export output
	 */
	def 'should apply custom export configuration'(@TempDir Path tempDir) {
		given: 'a custom export configuration'
		def config = ExportConfiguration.builder()
				.lobHandling(LobHandling.OMIT)
				.nullValue('[NULL]')
				.build()

		when: 'the database state is exported with the custom configuration'
		DataSetExporter.export(dataSource, ['EXPORT_DATA'], tempDir, DataFormat.CSV, config)

		then: 'the exported file is created'
		Files.exists(tempDir.resolve('EXPORT_DATA.csv'))
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
