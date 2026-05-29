package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.preparation.DatabasePreparation
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates all database operations for test data preparation using Spock.
 *
 * <p>This specification provides comprehensive coverage of all {@link Operation} enum values:
 * <ul>
 *   <li>{@link Operation#CLEAN_INSERT} - Delete all rows, then insert (default, most common)
 *   <li>{@link Operation#INSERT} - Insert new rows (fails if primary key already exists)
 *   <li>{@link Operation#UPDATE} - Update existing rows only (fails if row not exists)
 *   <li>{@link Operation#UPSERT} - Update if exists, insert if not (upsert)
 *   <li>{@link Operation#DELETE} - Delete only specified rows by primary key
 *   <li>{@link Operation#DELETE_ALL} - Delete all rows from tables
 *   <li>{@link Operation#TRUNCATE_TABLE} - Truncate tables, resetting auto-increment sequences
 *   <li>{@link Operation#TRUNCATE_INSERT} - Truncate then insert (predictable IDs)
 * </ul>
 *
 * <p><strong>Note on Partial Column Validation:</strong> The expectation CSV files in this test
 * omit COLUMN3 (TIMESTAMP) to demonstrate that specifying all table columns in CSV files is not
 * required.
 */
@DatabaseTest
class OperationVariationsSpec extends Specification implements DatabaseTestSupport {

	@Shared
	DataSource dataSource

	@Shared
	DataSourceRegistry dbTesterRegistry

	@Shared
	Sql sql

	def setupSpec() {
		dataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:OperationVariationsSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}

		dbTesterRegistry = new DataSourceRegistry()
		dbTesterRegistry.registerDefault(dataSource)

		sql = new Sql(dataSource)
		executeScript('ddl/feature/OperationVariationsSpec.sql')
	}

	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Demonstrates CLEAN_INSERT operation (default).
	 *
	 * <p>Deletes all existing rows, then inserts test data. Most common operation for test setup.
	 */
	@DataSet(operation = Operation.CLEAN_INSERT)
	@ExpectedDataSet
	def 'should use clean insert operation'() {
		when: 'inserting a new product after clean insert'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (3, 'Tablet', 25, CURRENT_TIMESTAMP)
		'''

		then: 'all three products exist'
		noExceptionThrown()
	}

	/**
	 * Demonstrates {@link Operation#INSERT} via the programmatic preparation API.
	 *
	 * <p>The annotation-driven preparation empties the table via DELETE_ALL, then
	 * {@link DatabasePreparation#execute} appends rows using {@link Operation#INSERT}.
	 */
	@DataSet(operation = Operation.DELETE_ALL)
	@ExpectedDataSet
	def 'should use insert operation'() {
		given: 'the rows to insert via Operation.INSERT'
		def rows = Table.ofValues(
				'TABLE1',
				['ID', 'COLUMN1', 'COLUMN2'],
				[
					[1, 'Keyboard', 20],
					[2, 'Monitor', 15],
					[3, 'Smartwatch', 30]
				])

		when: 'invoking DatabasePreparation.execute with Operation.INSERT'
		DatabasePreparation.execute(dataSource, TableSet.of(rows), Operation.INSERT)

		then: 'INSERT executes without exception'
		noExceptionThrown()
	}

	/**
	 * Demonstrates {@link Operation#UPDATE} via the programmatic preparation API.
	 *
	 * <p>The annotation-driven CLEAN_INSERT seeds the rows, and {@link DatabasePreparation#execute}
	 * applies UPDATE to modify the targeted row.
	 */
	@DataSet(operation = Operation.CLEAN_INSERT)
	@ExpectedDataSet
	def 'should use update operation'() {
		given: 'the row to update via Operation.UPDATE'
		def updateRows = Table.ofValues(
				'TABLE1',
				['ID', 'COLUMN1', 'COLUMN2'],
				[[2, 'Monitor', 8]])

		when: 'invoking DatabasePreparation.execute with Operation.UPDATE'
		DatabasePreparation.execute(dataSource, TableSet.of(updateRows), Operation.UPDATE)

		then: 'UPDATE executes without exception'
		noExceptionThrown()
	}

	/**
	 * Demonstrates UPSERT operation.
	 *
	 * <p>Updates row if exists, inserts if not exists.
	 */
	@DataSet(operation = Operation.UPSERT)
	@ExpectedDataSet
	def 'should use upsert operation'() {
		when: 'inserting a new product after upsert'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (3, 'Headphones', 40, CURRENT_TIMESTAMP)
		'''

		then: 'all three products exist'
		noExceptionThrown()
	}

	/**
	 * Demonstrates DELETE_ALL followed by INSERT.
	 *
	 * <p>Clears table completely before inserting test data.
	 */
	@DataSet(operation = Operation.DELETE_ALL)
	@ExpectedDataSet
	def 'should use delete all operation'() {
		when: 'inserting a product into empty table'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (1, 'Camera', 15, CURRENT_TIMESTAMP)
		'''

		then: 'only one product exists'
		noExceptionThrown()
	}

	/**
	 * Demonstrates {@link Operation#DELETE} via the programmatic preparation API.
	 */
	@DataSet
	@ExpectedDataSet
	def 'should use delete operation'() {
		given: 'the row to remove via Operation.DELETE'
		def deletionTargets = Table.ofValues(
				'TABLE1',
				['ID', 'COLUMN1', 'COLUMN2'],
				[[2, 'Mouse', 5]])

		when: 'invoking DatabasePreparation.execute with Operation.DELETE'
		DatabasePreparation.execute(dataSource, TableSet.of(deletionTargets), Operation.DELETE)

		then: 'DELETE executes without exception'
		noExceptionThrown()
	}

	/**
	 * Demonstrates TRUNCATE_INSERT operation.
	 *
	 * <p>Truncates tables then inserts test data for predictable ID values.
	 */
	@DataSet(operation = Operation.TRUNCATE_INSERT)
	@ExpectedDataSet
	def 'should use truncate insert operation'() {
		when: 'inserting after truncate'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (3, 'Monitor', 12, NULL)
		'''

		then: 'all three products exist with predictable IDs'
		noExceptionThrown()
	}

	/**
	 * Demonstrates TRUNCATE_TABLE operation.
	 *
	 * <p>Truncates tables, removing all data and resetting auto-increment sequences.
	 */
	@DataSet(operation = Operation.TRUNCATE_TABLE)
	@ExpectedDataSet
	def 'should use truncate table operation'() {
		when: 'inserting after truncate'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (1, 'Keyboard', 25, NULL)
		'''

		then: 'only one product exists'
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
