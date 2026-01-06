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
		def configuration = new Configuration(
				ConventionSettings.standard(),
				OperationDefaults.standard(),
				loader
				)
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

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
