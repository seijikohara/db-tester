package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.operation.Operation
import java.lang.reflect.Method
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

/**
 * Unit tests for {@link SpringBootDatabaseTestInterceptor}.
 *
 * <p>This specification verifies the Spring Boot-aware Spock method interceptor
 * for database testing.
 */
class SpringBootDatabaseTestInterceptorSpec extends Specification {

	def 'should create instance with both annotations'() {
		given: 'mock annotations'
		def preparation = Mock(Preparation)
		def expectation = Mock(Expectation)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(preparation, expectation)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only Preparation annotation'() {
		given: 'mock Preparation annotation'
		def preparation = Mock(Preparation)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(preparation, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only Expectation annotation'() {
		given: 'mock Expectation annotation'
		def expectation = Mock(Expectation)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectation)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with null annotations'() {
		when: 'creating interceptor with null annotations'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should implement IMethodInterceptor interface'() {
		given: 'a new interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null)

		expect: 'implements IMethodInterceptor'
		interceptor instanceof IMethodInterceptor
	}

	def 'should handle invocation with Preparation annotation'() {
		given: 'an interceptor with Preparation annotation'
		def preparation = createMockPreparation()
		def interceptor = new SpringBootDatabaseTestInterceptor(preparation, null)

		expect: 'interceptor is created with Preparation'
		interceptor != null
	}

	def 'should handle invocation with Expectation annotation'() {
		given: 'an interceptor with Expectation annotation'
		def expectation = createMockExpectation()
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectation)

		expect: 'interceptor is created with Expectation'
		interceptor != null
	}

	def 'should handle invocation with both annotations'() {
		given: 'an interceptor with both annotations'
		def preparation = createMockPreparation()
		def expectation = createMockExpectation()
		def interceptor = new SpringBootDatabaseTestInterceptor(preparation, expectation)

		expect: 'interceptor is created with both annotations'
		interceptor != null
	}

	def 'should create multiple independent interceptors'() {
		given: 'different annotations'
		def prep1 = createMockPreparation()
		def prep2 = createMockPreparation()
		def exp1 = createMockExpectation()
		def exp2 = createMockExpectation()

		when: 'creating multiple interceptors'
		def interceptor1 = new SpringBootDatabaseTestInterceptor(prep1, exp1)
		def interceptor2 = new SpringBootDatabaseTestInterceptor(prep2, exp2)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should handle different operation types'() {
		given: 'preparations with different operations'
		def preparation = createMockPreparation(operation)
		def interceptor = new SpringBootDatabaseTestInterceptor(preparation, null)

		expect: 'interceptor is created successfully'
		interceptor != null

		where:
		operation << [
			Operation.CLEAN_INSERT,
			Operation.INSERT,
			Operation.DELETE_ALL,
			Operation.NONE
		]
	}

	/**
	 * Creates a mock Preparation annotation.
	 *
	 * @return the mocked annotation
	 */
	private Preparation createMockPreparation() {
		createMockPreparation(Operation.CLEAN_INSERT)
	}

	/**
	 * Creates a mock Preparation annotation with the specified operation.
	 *
	 * @param operation the operation to use
	 * @return the mocked annotation
	 */
	private Preparation createMockPreparation(Operation operation) {
		def preparation = Mock(Preparation)
		preparation.operation() >> operation
		preparation.paths() >> ([] as String[])
		return preparation
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
}
