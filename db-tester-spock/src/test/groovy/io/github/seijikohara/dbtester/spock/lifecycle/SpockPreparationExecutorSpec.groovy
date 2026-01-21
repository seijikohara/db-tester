package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.OperationProvider
import javax.sql.DataSource
import spock.lang.Specification

/**
 * Unit tests for {@link SpockPreparationExecutor}.
 *
 * <p>This specification verifies the preparation phase executor that loads
 * and applies datasets before test execution.
 */
class SpockPreparationExecutorSpec extends Specification {

	/** The executor under test. */
	SpockPreparationExecutor executor

	def setup() {
		executor = new SpockPreparationExecutor()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockPreparationExecutor()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should throw NullPointerException when context is null'() {
		given: 'a mock DataSet annotation'
		def dataSet = Mock(DataSet)

		when: 'executing with null context'
		executor.execute(null, dataSet)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('context must not be null')
	}

	def 'should throw NullPointerException when dataSet is null'() {
		given: 'a valid TestContext'
		def context = createTestContext()

		when: 'executing with null dataSet'
		executor.execute(context, null)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('dataSet must not be null')
	}

	def 'should handle empty datasets gracefully'() {
		given: 'a context with empty datasets'
		def context = createTestContextWithEmptyDatasets()

		and: 'a mock DataSet annotation'
		def dataSet = createMockDataSet(Operation.CLEAN_INSERT)

		when: 'executing preparation'
		executor.execute(context, dataSet)

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should create multiple independent executors'() {
		when: 'creating multiple executors'
		def executor1 = new SpockPreparationExecutor()
		def executor2 = new SpockPreparationExecutor()

		then: 'executors are independent'
		!executor1.is(executor2)
	}

	/**
	 * Creates a basic TestContext for testing.
	 *
	 * @return the test context
	 */
	private TestContext createTestContext() {
		createTestContextWithEmptyDatasets()
	}

	/**
	 * Creates a TestContext with empty datasets.
	 *
	 * @return the test context
	 */
	private TestContext createTestContextWithEmptyDatasets() {
		def testClass = SampleTestClass
		def testMethod = SampleTestClass.getMethod('sampleMethod')
		def loader = { ctx -> [] } as DataSetLoader
		def configuration = Configuration.builder()
				.conventions(ConventionSettings.standard())
				.operations(OperationDefaults.standard())
				.loader(loader)
				.build()
		def registry = new DataSourceRegistry()
		new TestContext(testClass, testMethod, configuration, registry)
	}

	/**
	 * Creates a mock DataSet annotation with the specified operation.
	 *
	 * @param operation the operation to use
	 * @return the mocked annotation
	 */
	private DataSet createMockDataSet(Operation operation) {
		def dataSet = Mock(DataSet)
		dataSet.operation() >> operation
		dataSet.tableOrdering() >> TableOrderingStrategy.AUTO
		dataSet.paths() >> ([] as String[])
		return dataSet
	}

	def 'should execute operation when datasets are found'() {
		given: 'a mock operation provider'
		def mockOperationProvider = Mock(OperationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockPreparationExecutor.getDeclaredField('operationProvider')
		providerField.accessible = true
		providerField.set(executor, mockOperationProvider)

		and: 'a context with datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithDatasets(registry)

		and: 'a mock DataSet annotation'
		def dataSet = createMockDataSet(Operation.CLEAN_INSERT)

		when: 'executing preparation'
		executor.execute(context, dataSet)

		then: 'operation is executed'
		1 * mockOperationProvider.execute(Operation.CLEAN_INSERT, _, mockDataSource, TableOrderingStrategy.AUTO, _, _)
	}

	def 'should handle datasets with different operations'() {
		given: 'a mock operation provider'
		def mockOperationProvider = Mock(OperationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockPreparationExecutor.getDeclaredField('operationProvider')
		providerField.accessible = true
		providerField.set(executor, mockOperationProvider)

		and: 'a context with datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithDatasets(registry)

		and: 'a mock DataSet annotation with specified operation'
		def dataSet = createMockDataSet(operation)

		when: 'executing preparation'
		executor.execute(context, dataSet)

		then: 'operation is executed with correct operation type'
		1 * mockOperationProvider.execute(operation, _, mockDataSource, _, _, _)

		where:
		operation << [
			Operation.INSERT,
			Operation.DELETE_ALL,
			Operation.TRUNCATE_INSERT
		]
	}

	/**
	 * Creates a TestContext with actual datasets.
	 *
	 * @param registry the registry to use
	 * @return the test context
	 */
	private TestContext createTestContextWithDatasets(DataSourceRegistry registry) {
		def testClass = SampleTestClass
		def testMethod = SampleTestClass.getMethod('sampleMethod')

		// Create a simple table set
		def table = Table.of(
				new TableName('users'),
				[
					new ColumnName('id'),
					new ColumnName('name')
				],
				[
					Row.of([(new ColumnName('id')): new CellValue('1'), (new ColumnName('name')): new CellValue('John')])
				]
				)
		def tableSet = TableSet.of(table)

		def loader = new DataSetLoader() {
					@Override
					List loadPreparationDataSets(TestContext ctx) {
						return [tableSet]
					}

					@Override
					List loadExpectationDataSets(TestContext ctx) {
						return []
					}

					@Override
					List loadExpectationDataSetsWithExclusions(TestContext ctx) {
						return []
					}
				}
		def configuration = Configuration.builder()
				.conventions(ConventionSettings.standard())
				.operations(OperationDefaults.standard())
				.loader(loader)
				.build()
		new TestContext(testClass, testMethod, configuration, registry)
	}

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
