package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport
import spock.lang.Specification

/**
 * Unit tests for {@link SpockExpectationVerifier}.
 *
 * <p>This specification verifies the expectation verification phase that compares
 * expected datasets with the actual database state.
 */
class SpockExpectationVerifierSpec extends Specification {

	/** Mock expectation support for tests. */
	ExpectationSupport mockSupport

	/** The verifier under test. */
	SpockExpectationVerifier verifier

	def setup() {
		mockSupport = Mock(ExpectationSupport)
		verifier = new SpockExpectationVerifier(mockSupport)
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockExpectationVerifier(mockSupport)

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
		def verifier1 = new SpockExpectationVerifier(mockSupport)
		def verifier2 = new SpockExpectationVerifier(mockSupport)

		then: 'verifiers are independent'
		!verifier1.is(verifier2)
	}

	def 'should delegate to expectation support'() {
		given: 'a context and expected data set'
		def context = createTestContextWithEmptyDatasets()
		def expectedDataSet = createMockExpectedDataSet()

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'support is called'
		1 * mockSupport.verify(context, expectedDataSet)
	}

	def 'should propagate validation exception from support'() {
		given: 'a context and expected data set'
		def context = createTestContextWithEmptyDatasets()
		def expectedDataSet = createMockExpectedDataSet()

		mockSupport.verify(_, _) >> {
			throw new ValidationException('Verification failed')
		}

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'ValidationException is thrown'
		thrown(ValidationException)
	}

	def 'should handle ordered row verification'() {
		given: 'a context and expected data set with ordered rows'
		def context = createTestContextWithEmptyDatasets()
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 0
		expectedDataSet.retryDelayMillis() >> -1
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED
		expectedDataSet.sources() >> ([] as String[])
		expectedDataSet.tableOrdering() >> TableOrderingStrategy.AUTO

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'support is called with the expected data set'
		1 * mockSupport.verify(context, expectedDataSet)
	}

	def 'should handle unordered row verification'() {
		given: 'a context and expected data set with unordered rows'
		def context = createTestContextWithEmptyDatasets()
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 0
		expectedDataSet.retryDelayMillis() >> -1
		expectedDataSet.rowOrdering() >> RowOrdering.UNORDERED
		expectedDataSet.sources() >> ([] as String[])
		expectedDataSet.tableOrdering() >> TableOrderingStrategy.AUTO

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'support is called with the expected data set'
		1 * mockSupport.verify(context, expectedDataSet)
	}

	def 'should handle retry count settings'() {
		given: 'a context and expected data set with retry settings'
		def context = createTestContextWithEmptyDatasets()
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.retryCount() >> 3
		expectedDataSet.retryDelayMillis() >> 100
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED
		expectedDataSet.sources() >> ([] as String[])
		expectedDataSet.tableOrdering() >> TableOrderingStrategy.AUTO

		when: 'verifying expectation'
		verifier.verify(context, expectedDataSet)

		then: 'support is called with the expected data set (retry logic is handled by support)'
		1 * mockSupport.verify(context, expectedDataSet)
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
		expectedDataSet.sources() >> ([] as String[])
		expectedDataSet.tableOrdering() >> TableOrderingStrategy.AUTO
		expectedDataSet.retryCount() >> 0
		expectedDataSet.retryDelayMillis() >> -1
		expectedDataSet.rowOrdering() >> RowOrdering.ORDERED
		return expectedDataSet
	}

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
