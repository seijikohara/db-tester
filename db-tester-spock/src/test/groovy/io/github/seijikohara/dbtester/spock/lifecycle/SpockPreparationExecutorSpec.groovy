package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.PreparationSupport
import spock.lang.Specification

/**
 * Unit tests for {@link SpockPreparationExecutor}.
 *
 * <p>This specification verifies the preparation phase executor that loads
 * and applies datasets before test execution.
 */
class SpockPreparationExecutorSpec extends Specification {

	/** Mock preparation support for tests. */
	PreparationSupport mockSupport

	/** The executor under test. */
	SpockPreparationExecutor executor

	def setup() {
		mockSupport = Mock(PreparationSupport)
		executor = new SpockPreparationExecutor(mockSupport)
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockPreparationExecutor(mockSupport)

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
		def executor1 = new SpockPreparationExecutor(mockSupport)
		def executor2 = new SpockPreparationExecutor(mockSupport)

		then: 'executors are independent'
		!executor1.is(executor2)
	}

	def 'should delegate to preparation support'() {
		given: 'a context and data set'
		def context = createTestContextWithEmptyDatasets()
		def dataSet = createMockDataSet(Operation.CLEAN_INSERT)

		when: 'executing preparation'
		executor.execute(context, dataSet)

		then: 'support is called'
		1 * mockSupport.execute(context, dataSet)
	}

	def 'should handle datasets with different operations'() {
		given: 'a context with datasets'
		def context = createTestContextWithEmptyDatasets()

		and: 'a mock DataSet annotation with specified operation'
		def dataSet = createMockDataSet(operation)

		when: 'executing preparation'
		executor.execute(context, dataSet)

		then: 'operation is executed with correct operation type'
		1 * mockSupport.execute(context, dataSet)

		where:
		operation << [
			Operation.INSERT,
			Operation.DELETE_ALL,
			Operation.TRUNCATE_INSERT
		]
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
		def loader = new DataSetLoader() {
					@Override
					List loadPreparationDataSets(TestContext ctx) {
						return []
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
		dataSet.sources() >> ([] as String[])
		return dataSet
	}

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
