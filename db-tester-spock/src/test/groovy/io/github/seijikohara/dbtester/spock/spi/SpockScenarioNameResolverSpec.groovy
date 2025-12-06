package io.github.seijikohara.dbtester.spock.spi

import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolver
import spock.lang.Specification

/**
 * Unit tests for {@link SpockScenarioNameResolver}.
 *
 * <p>This specification verifies the Spock-specific scenario name resolution
 * which extracts feature names from {@code @FeatureMetadata} annotation.
 */
class SpockScenarioNameResolverSpec extends Specification {

	/** The resolver under test. */
	SpockScenarioNameResolver resolver

	def setup() {
		resolver = new SpockScenarioNameResolver()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpockScenarioNameResolver()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should implement ScenarioNameResolver interface'() {
		expect: 'resolver implements ScenarioNameResolver'
		resolver instanceof ScenarioNameResolver
	}

	def 'should return priority 100'() {
		when: 'getting priority'
		def priority = resolver.priority()

		then: 'priority is 100 (higher than default)'
		priority == 100
	}

	def 'should return consistent priority'() {
		expect: 'priority is consistent across calls'
		resolver.priority() == resolver.priority()
	}

	def 'should return false for canResolve when FeatureMetadata is absent'() {
		given: 'a method without FeatureMetadata annotation'
		def method = SampleClass.getMethod('regularMethod')

		when: 'checking if resolver can resolve'
		def canResolve = resolver.canResolve(method)

		then: 'returns false'
		!canResolve
	}

	def 'should fall back to method name when FeatureMetadata is absent'() {
		given: 'a method without FeatureMetadata annotation'
		def method = SampleClass.getMethod('testMethod')

		when: 'resolving scenario name'
		def scenarioName = resolver.resolve(method)

		then: 'returns the method name'
		scenarioName.value() == 'testMethod'
	}

	def 'should have higher priority than JUnit resolver'() {
		given: 'Spock resolver priority'
		def spockPriority = resolver.priority()

		and: 'JUnit resolver default priority'
		def junitDefaultPriority = ScenarioNameResolver.DEFAULT_PRIORITY

		expect: 'Spock priority is higher'
		spockPriority > junitDefaultPriority
	}

	def 'should return non-null ScenarioName for regular method'() {
		given: 'a regular method'
		def method = SampleClass.getMethod('testMethod')

		when: 'resolving scenario name'
		def scenarioName = resolver.resolve(method)

		then: 'returns non-null ScenarioName'
		scenarioName != null
	}

	def 'should handle various method names'() {
		given: 'methods with various names'
		def method = SampleClass.getMethod(methodName)

		when: 'resolving scenario name'
		def scenarioName = resolver.resolve(method)

		then: 'returns the method name'
		scenarioName.value() == methodName

		where:
		methodName << [
			'testMethod',
			'regularMethod',
			'anotherMethod'
		]
	}

	/**
	 * Sample class for testing regular methods without FeatureMetadata.
	 */
	static class SampleClass {
		/** Sample test method without annotation. */
		void testMethod() {}

		/** Sample regular method. */
		void regularMethod() {}

		/** Another sample method. */
		void anotherMethod() {}
	}
}
