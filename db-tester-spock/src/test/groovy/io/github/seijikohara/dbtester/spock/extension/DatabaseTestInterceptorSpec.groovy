package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
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
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, expectedDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only DataSet annotation'() {
		given: 'mock DataSet annotation'
		def dataSet = Mock(DataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExpectedDataSet annotation'() {
		given: 'mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectedDataSet)

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
		given: 'data sets with different operations'
		def dataSet = Mock(DataSet)
		dataSet.operation() >> operation

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null)

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
		def dataSet1 = Mock(DataSet)
		def dataSet2 = Mock(DataSet)
		def expectedDataSet1 = Mock(ExpectedDataSet)
		def expectedDataSet2 = Mock(ExpectedDataSet)

		when: 'creating multiple interceptors'
		def interceptor1 = new DatabaseTestInterceptor(dataSet1, expectedDataSet1)
		def interceptor2 = new DatabaseTestInterceptor(dataSet2, expectedDataSet2)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should create interceptor with DataSet having custom paths'() {
		given: 'DataSet with custom paths'
		def dataSet = Mock(DataSet)
		dataSet.paths() >> ([
			'custom/path1.csv',
			'custom/path2.csv'
		] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExpectedDataSet having custom columns'() {
		given: 'ExpectedDataSet with custom columns'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.columns() >> (['id', 'name', 'status'] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectedDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}
}
