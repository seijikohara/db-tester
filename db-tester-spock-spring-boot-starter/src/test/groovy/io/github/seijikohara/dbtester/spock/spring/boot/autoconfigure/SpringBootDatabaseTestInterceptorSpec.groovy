package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
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

	def 'should create instance with all annotations'() {
		given: 'mock annotations'
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)
		def exportDataSet = Mock(ExportDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet,
				exportDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with DataSet and ExpectedDataSet annotations'() {
		given: 'mock annotations'
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only DataSet annotation'() {
		given: 'mock DataSet annotation'
		def dataSet = Mock(DataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExpectedDataSet annotation'() {
		given: 'mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectedDataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExportDataSet annotation'() {
		given: 'mock ExportDataSet annotation'
		def exportDataSet = Mock(ExportDataSet)

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, exportDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with null annotations'() {
		when: 'creating interceptor with null annotations'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should implement IMethodInterceptor interface'() {
		given: 'a new interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, null)

		expect: 'implements IMethodInterceptor'
		interceptor instanceof IMethodInterceptor
	}

	def 'should handle invocation with DataSet annotation'() {
		given: 'an interceptor with DataSet annotation'
		def dataSet = createMockDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null, null)

		expect: 'interceptor is created with DataSet'
		interceptor != null
	}

	def 'should handle invocation with ExpectedDataSet annotation'() {
		given: 'an interceptor with ExpectedDataSet annotation'
		def expectedDataSet = createMockExpectedDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(null, expectedDataSet, null)

		expect: 'interceptor is created with ExpectedDataSet'
		interceptor != null
	}

	def 'should handle invocation with both annotations'() {
		given: 'an interceptor with both annotations'
		def dataSet = createMockDataSet()
		def expectedDataSet = createMockExpectedDataSet()
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet, null)

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
		def interceptor1 = new SpringBootDatabaseTestInterceptor(ds1, exp1, null)
		def interceptor2 = new SpringBootDatabaseTestInterceptor(ds2, exp2, null)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should handle different operation types'() {
		given: 'sources with different operations'
		def dataSet = createMockDataSet(operation)
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, null, null)

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

	def 'should create interceptor with ExportDataSet having onFailureOnly true'() {
		given: 'ExportDataSet with onFailureOnly enabled'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.onFailureOnly() >> true
		exportDataSet.tables() >> (['USERS', 'ORDERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExportDataSet having onFailureOnly false'() {
		given: 'ExportDataSet with onFailureOnly disabled'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.onFailureOnly() >> false
		exportDataSet.tables() >> (['USERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExportDataSet having custom dataSourceName'() {
		given: 'ExportDataSet with named DataSource'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.dataSourceName() >> 'secondary'
		exportDataSet.tables() >> (['USERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should support multi-DataSource registration via registry'() {
		given: 'a registry with multiple DataSources'
		def registry = new DataSourceRegistry()
		def defaultDs = Stub(javax.sql.DataSource)
		def secondaryDs = Stub(javax.sql.DataSource)
		registry.registerDefault(defaultDs)
		registry.register('secondary', secondaryDs)

		expect: 'both DataSources are retrievable'
		registry.hasDefault()
		registry.has('secondary')
		registry.getDefault() == defaultDs
		registry.get('secondary') == secondaryDs
	}

	def 'should create interceptor with all three annotations including ExportDataSet'() {
		given: 'all annotations with export on failure'
		def dataSet = createMockDataSet()
		def expectedDataSet = createMockExpectedDataSet()
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.onFailureOnly() >> true
		exportDataSet.tables() >> (['USERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new SpringBootDatabaseTestInterceptor(dataSet, expectedDataSet, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
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
		dataSet.sources() >> ([] as DataSetSource[])
		return dataSet
	}

	/**
	 * Creates a mock ExpectedDataSet annotation.
	 *
	 * @return the mocked annotation
	 */
	private ExpectedDataSet createMockExpectedDataSet() {
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.sources() >> ([] as DataSetSource[])
		return expectedDataSet
	}
}
