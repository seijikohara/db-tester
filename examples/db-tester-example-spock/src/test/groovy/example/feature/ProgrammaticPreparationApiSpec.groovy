package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.preparation.DatabasePreparation
import io.github.seijikohara.dbtester.api.preparation.PreparationConfig
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates the {@link DatabasePreparation} programmatic API for preparing test data without
 * {@code @DataSet} annotations with Spock.
 *
 * <p>This specification illustrates how to use the programmatic preparation API for scenarios where
 * annotation-based dataset loading is insufficient or impractical:
 * <ul>
 *   <li>{@link DatabasePreparation#cleanInsert(DataSource, TableSet)} - Clean insert with default
 *       configuration
 *   <li>{@link DatabasePreparation#cleanInsert(DataSource, TableSet, PreparationConfig)} - Clean
 *       insert with custom batch size and transaction mode
 *   <li>{@link DatabasePreparation#execute(DataSource, TableSet, Operation)} - Execute a specific
 *       operation (INSERT, UPDATE, DELETE)
 *   <li>{@link DatabasePreparation#execute(DataSource, TableSet, Operation, PreparationConfig)} -
 *       Execute with full configuration control
 * </ul>
 *
 * <p>Programmatic preparation supports dynamic test data generation, computed values, and mid-test
 * data manipulation that cannot be expressed in static CSV files.
 *
 * @see DatabasePreparation
 * @see PreparationConfig
 * @see Operation
 */
@DatabaseTest
class ProgrammaticPreparationApiSpec extends Specification implements DatabaseTestSupport {

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
			setURL('jdbc:h2:mem:ProgrammaticPreparationApiSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database connection and schema.
	 * Uses Groovy compact syntax and extension methods.
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
		executeScript('ddl/feature/ProgrammaticPreparationApiSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Verifies that {@link DatabasePreparation#cleanInsert(DataSource, TableSet)} inserts data with
	 * default configuration.
	 *
	 * <p>This test demonstrates programmatic preparation by building tables using
	 * {@link Table#ofValues} and inserting them via {@code cleanInsert} with standard defaults.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Programmatically constructs USERS and ORDERS tables
	 *   <li>Execution: Calls {@code DatabasePreparation.cleanInsert(dataSource, tableSet)}
	 *   <li>Expectation: Verifies database state via {@code @ExpectedDataSet}
	 * </ul>
	 */
	@ExpectedDataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ProgrammaticPreparationApiSpec/clean-insert-default/expected/'))
	def 'should insert data with default settings'() {
		given: 'programmatically constructed USERS and ORDERS tables'
		def usersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(List.of(1, 'Alice', 'alice@example.com'), List.of(2, 'Bob', 'bob@example.com')))

		def ordersTable = Table.ofValues('ORDERS',
				List.of('ID', 'USER_ID', 'PRODUCT', 'AMOUNT'),
				List.of(List.of(101, 1, 'Laptop', 999.99), List.of(102, 2, 'Mouse', 29.99)))

		def tableSet = TableSet.of(usersTable, ordersTable)

		when: 'executing cleanInsert with default configuration'
		DatabasePreparation.cleanInsert(dataSource, tableSet)

		then: 'framework verifies database state via @ExpectedDataSet'
		noExceptionThrown()
	}

	/**
	 * Verifies that {@link DatabasePreparation#cleanInsert(DataSource, TableSet, PreparationConfig)}
	 * inserts data with custom batch size and transaction mode.
	 *
	 * <p>This test demonstrates customizing preparation behavior via {@link PreparationConfig}
	 * method chaining, including batch size, transaction mode, and table ordering strategy.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Constructs USERS (3 rows) and ORDERS (3 rows) with custom config
	 *   <li>Execution: Calls {@code cleanInsert} with batch size 2, AUTO_COMMIT, FOREIGN_KEY ordering
	 *   <li>Expectation: Verifies all six records via {@code @ExpectedDataSet}
	 * </ul>
	 */
	@ExpectedDataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ProgrammaticPreparationApiSpec/clean-insert-custom-batch/expected/'))
	def 'should insert data with custom batch size'() {
		given: 'programmatically constructed USERS and ORDERS tables with three rows each'
		def usersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(
				List.of(1, 'Alice', 'alice@example.com'),
				List.of(2, 'Bob', 'bob@example.com'),
				List.of(3, 'Charlie', 'charlie@example.com')))

		def ordersTable = Table.ofValues('ORDERS',
				List.of('ID', 'USER_ID', 'PRODUCT', 'AMOUNT'),
				List.of(
				List.of(101, 1, 'Laptop', 999.99),
				List.of(102, 2, 'Mouse', 29.99),
				List.of(103, 3, 'Keyboard', 79.99)))

		def tableSet = TableSet.of(usersTable, ordersTable)

		def config = PreparationConfig.standard()
				.withBatchSize(2)
				.withTransactionMode(TransactionMode.AUTO_COMMIT)
				.withTableOrdering(TableOrderingStrategy.FOREIGN_KEY)

		when: 'executing cleanInsert with custom configuration'
		DatabasePreparation.cleanInsert(dataSource, tableSet, config)

		then: 'framework verifies database state via @ExpectedDataSet'
		noExceptionThrown()
	}

	/**
	 * Verifies that {@link DatabasePreparation#execute(DataSource, TableSet, Operation)} executes
	 * an INSERT operation.
	 *
	 * <p>This test demonstrates using a specific {@link Operation#INSERT} instead of the default
	 * CLEAN_INSERT. The tables are manually cleared before insertion to ensure a clean state.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Clears ORDERS then USERS tables via SQL DELETE
	 *   <li>Execution: Calls {@code DatabasePreparation.execute} with {@link Operation#INSERT}
	 *   <li>Expectation: Verifies single USERS record via {@code @ExpectedDataSet}
	 * </ul>
	 */
	@ExpectedDataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ProgrammaticPreparationApiSpec/execute-insert/expected/'))
	def 'should execute insert operation'() {
		given: 'empty tables'
		sql.execute 'DELETE FROM ORDERS'
		sql.execute 'DELETE FROM USERS'

		def usersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(List.of(1, 'Alice', 'alice@example.com')))

		def tableSet = TableSet.of(usersTable)

		when: 'executing INSERT operation'
		DatabasePreparation.execute(dataSource, tableSet, Operation.INSERT)

		then: 'framework verifies database state via @ExpectedDataSet'
		noExceptionThrown()
	}

	/**
	 * Verifies that {@link DatabasePreparation#execute(DataSource, TableSet, Operation,
	 * PreparationConfig)} executes with explicit operation and configuration.
	 *
	 * <p>This test demonstrates combining a specific operation with custom configuration settings
	 * for transaction mode and table ordering strategy.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Constructs USERS and ORDERS tables with custom config
	 *   <li>Execution: Calls {@code execute} with CLEAN_INSERT, FOREIGN_KEY ordering, and
	 *       AUTO_COMMIT transaction mode
	 *   <li>Expectation: Verifies all records via {@code @ExpectedDataSet}
	 * </ul>
	 */
	@ExpectedDataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ProgrammaticPreparationApiSpec/execute-full-config/expected/'))
	def 'should execute with explicit operation and configuration'() {
		given: 'programmatically constructed USERS and ORDERS tables with custom config'
		def usersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(
				List.of(1, 'Alice', 'alice@example.com'), List.of(2, 'Bob', 'bob@example.com')))

		def ordersTable = Table.ofValues('ORDERS',
				List.of('ID', 'USER_ID', 'PRODUCT', 'AMOUNT'),
				List.of(List.of(101, 1, 'Laptop', 999.99), List.of(102, 2, 'Mouse', 29.99)))

		def tableSet = TableSet.of(usersTable, ordersTable)

		def config = PreparationConfig.standard()
				.withTransactionMode(TransactionMode.AUTO_COMMIT)
				.withTableOrdering(TableOrderingStrategy.FOREIGN_KEY)

		when: 'executing CLEAN_INSERT with explicit configuration'
		DatabasePreparation.execute(dataSource, tableSet, Operation.CLEAN_INSERT, config)

		then: 'framework verifies database state via @ExpectedDataSet'
		noExceptionThrown()
	}

	/**
	 * Verifies that test data can be prepared and verified without any annotations.
	 *
	 * <p>This test demonstrates a fully programmatic approach using {@link DatabasePreparation} for
	 * data setup and {@link DatabaseQueryAssertion#assertEqualsByQuery} for verification, without
	 * relying on {@code @DataSet} or {@code @ExpectedDataSet} annotations.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Constructs and inserts USERS and ORDERS data via {@code cleanInsert}
	 *   <li>Execution: No additional operation (preparation is the action under test)
	 *   <li>Expectation: Builds expected data programmatically and verifies via
	 *       {@code assertEqualsByQuery}
	 * </ul>
	 */
	def 'should prepare and verify without annotations'() {
		given: 'programmatically constructed USERS and ORDERS tables'
		def usersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(
				List.of(1, 'Alice', 'alice@example.com'), List.of(2, 'Bob', 'bob@example.com')))

		def ordersTable = Table.ofValues('ORDERS',
				List.of('ID', 'USER_ID', 'PRODUCT', 'AMOUNT'),
				List.of(List.of(101, 1, 'Laptop', 999.99), List.of(102, 2, 'Mouse', 29.99)))

		def tableSet = TableSet.of(usersTable, ordersTable)

		when: 'executing cleanInsert'
		DatabasePreparation.cleanInsert(dataSource, tableSet)

		then: 'programmatic assertions verify database state'
		def expectedUsersTable = Table.ofValues('USERS',
				List.of('ID', 'NAME', 'EMAIL'),
				List.of(
				List.of(1, 'Alice', 'alice@example.com'), List.of(2, 'Bob', 'bob@example.com')))

		DatabaseQueryAssertion.assertEqualsByQuery(
				expectedUsersTable, dataSource, 'USERS', 'SELECT ID, NAME, EMAIL FROM USERS ORDER BY ID')

		def expectedOrdersTable = Table.ofValues('ORDERS',
				List.of('ID', 'USER_ID', 'PRODUCT', 'AMOUNT'),
				List.of(List.of(101, 1, 'Laptop', 999.99), List.of(102, 2, 'Mouse', 29.99)))

		DatabaseQueryAssertion.assertEqualsByQuery(
				expectedOrdersTable, dataSource, 'ORDERS',
				'SELECT ID, USER_ID, PRODUCT, AMOUNT FROM ORDERS ORDER BY ID')
	}

	/**
	 * Executes a SQL script from classpath using Groovy features.
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
