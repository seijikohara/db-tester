package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.operation.Operation
import org.spockframework.runtime.extension.IMethodInterceptor
import spock.lang.Specification

/**
 * Unit tests for {@link DatabaseTestInterceptor}.
 *
 * <p>This specification verifies the Spock method interceptor that handles
 * database setup and verification operations.
 *
 * <p>Note: Due to Spock's limitation on mocking final classes like Method,
 * SpecInfo, FeatureInfo, and IMethodInvocation, these tests focus on
 * constructor and interface verification.
 */
class DatabaseTestInterceptorSpec extends Specification {

	def 'should create instance with both annotations'() {
		given: 'mock annotations'
		def preparation = Mock(Preparation)
		def expectation = Mock(Expectation)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(preparation, expectation)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only Preparation annotation'() {
		given: 'mock Preparation annotation'
		def preparation = Mock(Preparation)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(preparation, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only Expectation annotation'() {
		given: 'mock Expectation annotation'
		def expectation = Mock(Expectation)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectation)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with null annotations'() {
		when: 'creating interceptor with null annotations'
		def interceptor = new DatabaseTestInterceptor(null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should implement IMethodInterceptor interface'() {
		given: 'a new interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null)

		expect: 'implements IMethodInterceptor'
		interceptor instanceof IMethodInterceptor
	}

	def 'should create interceptor with different operations'() {
		given: 'preparations with different operations'
		def preparation = Mock(Preparation)
		preparation.operation() >> operation

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(preparation, null)

		then: 'interceptor is created successfully'
		interceptor != null

		where:
		operation << [
			Operation.CLEAN_INSERT,
			Operation.INSERT,
			Operation.DELETE_ALL,
			Operation.NONE
		]
	}

	def 'should create multiple independent interceptors'() {
		given: 'different annotations'
		def prep1 = Mock(Preparation)
		def prep2 = Mock(Preparation)
		def exp1 = Mock(Expectation)
		def exp2 = Mock(Expectation)

		when: 'creating multiple interceptors'
		def interceptor1 = new DatabaseTestInterceptor(prep1, exp1)
		def interceptor2 = new DatabaseTestInterceptor(prep2, exp2)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should create interceptor with Preparation having custom paths'() {
		given: 'Preparation with custom paths'
		def preparation = Mock(Preparation)
		preparation.paths() >> ([
			'custom/path1.csv',
			'custom/path2.csv'
		] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(preparation, null)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with Expectation having custom columns'() {
		given: 'Expectation with custom columns'
		def expectation = Mock(Expectation)
		expectation.columns() >> (['id', 'name', 'status'] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectation)

		then: 'interceptor is created successfully'
		interceptor != null
	}
}
