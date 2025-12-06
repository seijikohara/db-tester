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
 * Demonstrates comprehensive data type coverage in CSV files using Spock.
 *
 * <p>This specification shows all CSV-representable H2 data types:
 * <ul>
 *   <li>Integer types: TINYINT, SMALLINT, INTEGER, BIGINT
 *   <li>Decimal types: DECIMAL, NUMERIC
 *   <li>Floating point: REAL, FLOAT, DOUBLE, DOUBLE PRECISION
 *   <li>Character types: CHAR, VARCHAR, VARCHAR_IGNORECASE, LONGVARCHAR, CLOB, TEXT
 *   <li>Date/Time types: DATE, TIME, TIMESTAMP
 *   <li>Boolean types: BOOLEAN, BIT
 *   <li>Binary type: BLOB (Base64 encoded with {@code [BASE64]} prefix)
 *   <li>UUID values (stored as VARCHAR for CSV compatibility)
 *   <li>NULL value handling (empty column in CSV)
 * </ul>
 *
 * <p>CSV format examples:
 * <pre>{@code
 * ID,TINYINT_COL,CHAR_COL,VARCHAR_COL,DATE_COL,BOOLEAN_COL,BLOB_COL,UUID_COL
 * 1,127,CHAR10,Sample Text,2024-01-15,true,[BASE64]VGVzdA==,550e8400-e29b-41d4-a716-446655440000
 * 2,,ABC,NULL Test,,false,,[null]
 * }</pre>
 *
 * <p>Note: CHAR columns are stored without space padding in CSV. NULL values are represented by
 * empty columns (nothing between commas).
 */
class ComprehensiveDataTypesSpec extends Specification {

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
			setURL('jdbc:h2:mem:ComprehensiveDataTypesSpec;DB_CLOSE_DELAY=-1')
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
		executeScript('ddl/feature/ComprehensiveDataTypesSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates handling of all H2 CSV-representable data types.
	 *
	 * <p>Validates that CSV can represent all data types including integers, decimals, floating
	 * points, character types, date/time, booleans, binary (BLOB with Base64), and UUIDs.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Loads DATA_TYPES(ID=1,2) with comprehensive data type values
	 *   <li>Execution: Inserts ID=3 with all 24 data type columns (TINYINT to UUID)
	 *   <li>Expectation: Verifies all three records including BLOB (Base64) and CHAR (space-padded)
	 * </ul>
	 */
	@Preparation
	@Expectation
	def 'should handle all data types'() {
		when: 'inserting a record with all data types'
		sql.execute '''
			INSERT INTO DATA_TYPES (
				ID,
				TINYINT_COL, SMALLINT_COL, INT_COL, BIGINT_COL,
				DECIMAL_COL, NUMERIC_COL,
				REAL_COL, FLOAT_COL, DOUBLE_COL, DOUBLE_PRECISION_COL,
				CHAR_COL, VARCHAR_COL, VARCHAR_IGNORECASE_COL, LONGVARCHAR_COL, CLOB_COL, TEXT_COL,
				DATE_COL, TIME_COL, TIMESTAMP_COL,
				BOOLEAN_COL, BIT_COL,
				BLOB_COL,
				UUID_COL
			) VALUES (
				3,
				127, 32767, 999, 9999999999,
				888.88, 12345.67890,
				123.45, 456.78, 777.77, 888.88,
				'NEWCHAR', 'New Value', 'CaseTest', 'Long variable text', 'CLOB content here', 'Text content',
				'2024-12-31', '23:59:59', '2024-12-31 23:59:59',
				false, false,
				X'DEADBEEF',
				'550e8400-e29b-41d4-a716-446655440099'
			)
		'''

		then: 'all data types are properly stored and verified'
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
