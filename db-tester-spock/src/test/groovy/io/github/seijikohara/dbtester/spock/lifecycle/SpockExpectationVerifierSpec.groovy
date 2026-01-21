package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider
import javax.sql.DataSource
import spock.lang.Specification

/**
 * Unit tests for {@link SpockExpectationVerifier}.
 *
 * <p>This specification verifies the expectation verification phase that compares
 * expected datasets with the actual database state.
 */
class SpockExpectationVerifierSpec extends Specification {

	/** The verifier under test. */
	SpockExpectationVerifier verifier

	def setup() {
		verifier = new SpockExpectationVerifier()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockExpectationVerifier()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should throw NullPointerException when context is null'() {
		given: 'a mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'verifying with null context'
		verifier.verify(null, expectedDataSet)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('context must not be null')
	}

	def 'should throw NullPointerException when expectedDataSet is null'() {
		given: 'a valid TestContext'
		def context = createTestContext()

		when: 'verifying with null expectedDataSet'
		verifier.verify(context, null)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('expectedDataSet must not be null')
	}

	def 'should handle empty datasets gracefully'() {
		given: 'a context with empty datasets'
		def context = createTestContextWithEmptyDatasets()

		and: 'a mock ExpectedDataSet annotation'
		def expectedDataSet = createMockExpectedDataSet()

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should create multiple independent verifiers'() {
		when: 'creating multiple verifiers'
		def verifier1 = new SpockExpectationVerifier()
		def verifier2 = new SpockExpectationVerifier()

		then: 'verifiers are independent'
		!verifier1.is(verifier2)
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
	 * Creates a mock ExpectedDataSet annotation.
	 *
	 * @return the mocked annotation
	 */
	private ExpectedDataSet createMockExpectedDataSet() {
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.paths() >> ([] as String[])
		expectedDataSet.columns() >> ([] as String[])
		return expectedDataSet
	}

	def 'should verify expectation when datasets are found'() {
		given: 'a mock expectation provider'
		def mockExpectationProvider = Mock(ExpectationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockExpectationVerifier.getDeclaredField('expectationProvider')
		providerField.accessible = true
		providerField.set(verifier, mockExpectationProvider)

		and: 'a context with expected datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithExpectedDatasets(registry)

		and: 'a mock ExpectedDataSet annotation'
		def expectedDataSet = createMockExpectedDataSet()

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'verification is executed'
		1 * mockExpectationProvider.verifyExpectation(_, mockDataSource, _, _, _)
	}

	def 'should use annotation retry count when specified'() {
		given: 'a mock expectation provider'
		def mockExpectationProvider = Mock(ExpectationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockExpectationVerifier.getDeclaredField('expectationProvider')
		providerField.accessible = true
		providerField.set(verifier, mockExpectationProvider)

		and: 'a context with expected datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithExpectedDatasets(registry)

		and: 'a mock ExpectedDataSet annotation with retry settings'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 2
		expectedDataSet.retryDelayMillis() >> 10
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'verification is executed'
		1 * mockExpectationProvider.verifyExpectation(_, mockDataSource, _, _, RowOrdering.ORDERED)
	}

	def 'should retry and succeed on validation exception'() {
		given: 'a mock expectation provider that fails first then succeeds'
		def mockExpectationProvider = Mock(ExpectationProvider)
		def mockDataSource = Mock(DataSource)
		def callCount = 0

		and: 'inject mock provider using reflection'
		def providerField = SpockExpectationVerifier.getDeclaredField('expectationProvider')
		providerField.accessible = true
		providerField.set(verifier, mockExpectationProvider)

		and: 'a context with expected datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithExpectedDatasets(registry)

		and: 'a mock ExpectedDataSet annotation with retry settings'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 2
		expectedDataSet.retryDelayMillis() >> 10
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED

		mockExpectationProvider.verifyExpectation(_, _, _, _, _) >> {
			callCount++
			if (callCount == 1) {
				throw new ValidationException('First attempt failed')
			}
		}

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'verification succeeds after retry'
		noExceptionThrown()
		callCount == 2
	}

	def 'should throw exception after all retries exhausted'() {
		given: 'a mock expectation provider that always fails'
		def mockExpectationProvider = Mock(ExpectationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockExpectationVerifier.getDeclaredField('expectationProvider')
		providerField.accessible = true
		providerField.set(verifier, mockExpectationProvider)

		and: 'a context with expected datasets'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithExpectedDatasets(registry)

		and: 'a mock ExpectedDataSet annotation with retry settings'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 1
		expectedDataSet.retryDelayMillis() >> 10
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED

		mockExpectationProvider.verifyExpectation(_, _, _, _, _) >> {
			throw new ValidationException('Always fails')
		}

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'ValidationException is thrown'
		thrown(ValidationException)
	}

	def 'should handle exclusions and column strategies'() {
		given: 'a mock expectation provider'
		def mockExpectationProvider = Mock(ExpectationProvider)
		def mockDataSource = Mock(DataSource)

		and: 'inject mock provider using reflection'
		def providerField = SpockExpectationVerifier.getDeclaredField('expectationProvider')
		providerField.accessible = true
		providerField.set(verifier, mockExpectationProvider)

		and: 'a context with expected datasets having exclusions'
		def registry = new DataSourceRegistry()
		registry.registerDefault(mockDataSource)
		def context = createTestContextWithExclusions(registry)

		and: 'a mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 0
		expectedDataSet.retryDelayMillis() >> -1
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'verification is executed with exclusions'
		1 * mockExpectationProvider.verifyExpectation(_, mockDataSource, { it.contains('CREATED_AT') }, _, _)
	}

	/**
	 * Creates a TestContext with expected datasets.
	 *
	 * @param registry the registry to use
	 * @return the test context
	 */
	private TestContext createTestContextWithExpectedDatasets(DataSourceRegistry registry) {
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
		def expectedTableSet = new ExpectedTableSet(tableSet, [] as Set, [:])

		def loader = new DataSetLoader() {
					@Override
					List loadPreparationDataSets(TestContext ctx) {
						return []
					}

					@Override
					List loadExpectationDataSets(TestContext ctx) {
						return [tableSet]
					}

					@Override
					List loadExpectationDataSetsWithExclusions(TestContext ctx) {
						return [expectedTableSet]
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
	 * Creates a TestContext with exclusions.
	 *
	 * @param registry the registry to use
	 * @return the test context
	 */
	private TestContext createTestContextWithExclusions(DataSourceRegistry registry) {
		def testClass = SampleTestClass
		def testMethod = SampleTestClass.getMethod('sampleMethod')

		def table = Table.of(
				new TableName('users'),
				[
					new ColumnName('id'),
					new ColumnName('name'),
					new ColumnName('created_at')
				],
				[
					Row.of([(new ColumnName('id')): new CellValue('1'), (new ColumnName('name')): new CellValue('John')])
				]
				)
		def tableSet = TableSet.of(table)
		def expectedTableSet = new ExpectedTableSet(tableSet, ['CREATED_AT'] as Set, [:])

		def loader = new DataSetLoader() {
					@Override
					List loadPreparationDataSets(TestContext ctx) {
						return []
					}

					@Override
					List loadExpectationDataSets(TestContext ctx) {
						return [tableSet]
					}

					@Override
					List loadExpectationDataSetsWithExclusions(TestContext ctx) {
						return [expectedTableSet]
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
