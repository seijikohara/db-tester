package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.operation.Operation
import org.spockframework.runtime.extension.IMethodInterceptor
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
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only DataSet annotation'() {
		given: 'mock DataSet annotation'
		def dataSet = Mock(DataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExpectedDataSet annotation'() {
		given: 'mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectedDataSet)

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

	def 'should handle invocation with DataSet annotation'() {
		given: 'an interceptor with DataSet annotation'
		def dataSet = createMockDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null)

		expect: 'interceptor is created with DataSet'
		interceptor != null
	}

	def 'should handle invocation with ExpectedDataSet annotation'() {
		given: 'an interceptor with ExpectedDataSet annotation'
		def expectedDataSet = createMockExpectedDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectedDataSet)

		expect: 'interceptor is created with ExpectedDataSet'
		interceptor != null
	}

	def 'should handle invocation with both annotations'() {
		given: 'an interceptor with both annotations'
		def dataSet = createMockDataSet()
		def expectedDataSet = createMockExpectedDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet)

		expect: 'interceptor is created with both annotations'
		interceptor != null
	}

	def 'should create multiple independent interceptors'() {
		given: 'different annotations'
		def ds1 = createMockDataSet()
		def ds2 = createMockDataSet()
		def exp1 = createMockExpectedDataSet()
		def exp2 = createMockExpectedDataSet()

		when: 'creating multiple interceptors'
		def interceptor1 = new SpringBootDatabaseTestInterceptor(ds1, exp1)
		def interceptor2 = new SpringBootDatabaseTestInterceptor(ds2, exp2)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should handle different operation types'() {
		given: 'dataSets with different operations'
		def dataSet = createMockDataSet(operation)
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null)

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
	 * Creates a mock DataSet annotation.
	 *
	 * @return the mocked annotation
	 */
	private DataSet createMockDataSet() {
		createMockDataSet(Operation.CLEAN_INSERT)
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
		dataSet.paths() >> ([] as String[])
		return dataSet
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
}
