package io.github.seijikohara.dbtester.spock.lifecycle

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.internal.context.TestContext
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
		given: 'a mock Expectation annotation'
		def expectation = Mock(Expectation)

		when: 'verifying with null context'
		verifier.verify(null, expectation)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('context must not be null')
	}

	def 'should throw NullPointerException when expectation is null'() {
		given: 'a valid TestContext'
		def context = createTestContext()

		when: 'verifying with null expectation'
		verifier.verify(context, null)

		then: 'NullPointerException is thrown'
		def e = thrown(NullPointerException)
		e.message.contains('expectation must not be null')
	}

	def 'should handle empty datasets gracefully'() {
		given: 'a context with empty datasets'
		def context = createTestContextWithEmptyDatasets()

		and: 'a mock Expectation annotation'
		def expectation = createMockExpectation()

		when: 'verifying expectation'
		verifier.verify(context, expectation)

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
				}
		def configuration = new Configuration(
				ConventionSettings.standard(),
				OperationDefaults.standard(),
				loader
				)
		def registry = new DataSourceRegistry()
		new TestContext(testClass, testMethod, configuration, registry)
	}

	/**
	 * Creates a mock Expectation annotation.
	 *
	 * @return the mocked annotation
	 */
	private Expectation createMockExpectation() {
		def expectation = Mock(Expectation)
		expectation.paths() >> ([] as String[])
		expectation.columns() >> ([] as String[])
		return expectation
	}

	/**
	 * Sample test class for reflection.
	 */
	static class SampleTestClass {
		/** Sample test method. */
		void sampleMethod() {}
	}
}
