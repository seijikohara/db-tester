package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.spi.ExportSupport
import spock.lang.Specification

/**
 * Unit tests for {@link SpockExportExecutor}.
 *
 * <p>This specification verifies the export phase executor that captures
 * database state after test execution.
 */
class SpockExportExecutorSpec extends Specification {

	/** Mock export support for tests. */
	ExportSupport mockSupport

	/** The executor under test. */
	SpockExportExecutor executor

	def setup() {
		mockSupport = Mock(ExportSupport)
		executor = new SpockExportExecutor(mockSupport)
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockExportExecutor(mockSupport)

		then: 'instance is created successfully'
		instance != null
	}

	def 'should throw NullPointerException when support is null'() {
		when: 'creating with null support'
		new SpockExportExecutor(null)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('support must not be null')
	}

	def 'should throw NullPointerException when context is null'() {
		given: 'a mock ExportDataSet annotation'
		def exportDataSet = Mock(ExportDataSet)

		when: 'exporting with null context'
		executor.export(null, exportDataSet)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('context must not be null')
	}

	def 'should throw NullPointerException when exportDataSet is null'() {
		given: 'a valid TestContext'
		def context = createTestContext()

		when: 'exporting with null exportDataSet'
		executor.export(context, null)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('exportDataSet must not be null')
	}

	def 'should delegate to export support'() {
		given: 'a context and export data set'
		def context = createTestContext()
		def exportDataSet = createMockExportDataSet()

		when: 'exporting'
		executor.export(context, exportDataSet)

		then: 'support is called'
		1 * mockSupport.export(context, exportDataSet)
	}

	def 'should create multiple independent executors'() {
		when: 'creating multiple executors'
		def executor1 = new SpockExportExecutor(mockSupport)
		def executor2 = new SpockExportExecutor(mockSupport)

		then: 'executors are independent'
		!executor1.is(executor2)
	}

	def 'should propagate exception from support'() {
		given: 'a context and export data set'
		def context = createTestContext()
		def exportDataSet = createMockExportDataSet()

		mockSupport.export(_, _) >> {
			throw new DatabaseTesterException('Export failed')
		}

		when: 'exporting'
		executor.export(context, exportDataSet)

		then: 'DatabaseTesterException is thrown'
		thrown(DatabaseTesterException)
	}

	def 'should handle export with different formats'() {
		given: 'a context and export data set with specified format'
		def context = createTestContext()
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.format() >> format
		exportDataSet.outputDirectory() >> 'build/db-tester-export'
		exportDataSet.tables() >> ([] as String[])
		exportDataSet.dataSourceName() >> ''
		exportDataSet.onFailureOnly() >> false

		when: 'exporting'
		executor.export(context, exportDataSet)

		then: 'support is called with correct export data set'
		1 * mockSupport.export(context, exportDataSet)

		where:
		format << [
			DataFormat.CSV,
			DataFormat.JSON,
			DataFormat.YAML
		]
	}

	def 'should handle export with onFailureOnly flag'() {
		given: 'a context and export data set with onFailureOnly'
		def context = createTestContext()
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.format() >> DataFormat.CSV
		exportDataSet.outputDirectory() >> 'build/db-tester-export'
		exportDataSet.tables() >> ([] as String[])
		exportDataSet.dataSourceName() >> ''
		exportDataSet.onFailureOnly() >> true

		when: 'exporting'
		executor.export(context, exportDataSet)

		then: 'support is called'
		1 * mockSupport.export(context, exportDataSet)
	}

	/**
	 * Creates a basic TestContext for testing.
	 *
	 * @return the test context
	 */
	private TestContext createTestContext() {
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
	 * Creates a mock ExportDataSet annotation.
	 *
	 * @return the mocked annotation
	 */
	private ExportDataSet createMockExportDataSet() {
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.format() >> DataFormat.CSV
		exportDataSet.outputDirectory() >> 'build/db-tester-export'
		exportDataSet.tables() >> ([] as String[])
		exportDataSet.dataSourceName() >> ''
		exportDataSet.onFailureOnly() >> false
		return exportDataSet
	}

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
