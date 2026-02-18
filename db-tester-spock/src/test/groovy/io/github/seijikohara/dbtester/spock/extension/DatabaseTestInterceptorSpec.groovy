package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.operation.Operation
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import spock.lang.Specification

/**
 * Unit tests for {@link DatabaseTestInterceptor}.
 *
 * <p>This specification verifies the Spock method interceptor that handles
 * database setup, verification, and export operations.
 *
 * <p>Note: Due to Spock's limitation on mocking final classes like Method,
 * SpecInfo, FeatureInfo, and IMethodInvocation, these tests focus on
 * constructor and interface verification.
 */
class DatabaseTestInterceptorSpec extends Specification {

	def 'should create instance with all annotations'() {
		given: 'mock annotations'
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)
		def exportDataSet = Mock(ExportDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, expectedDataSet, exportDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with DataSet and ExpectedDataSet annotations'() {
		given: 'mock annotations'
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, expectedDataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only DataSet annotation'() {
		given: 'mock DataSet annotation'
		def dataSet = Mock(DataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExpectedDataSet annotation'() {
		given: 'mock ExpectedDataSet annotation'
		def expectedDataSet = Mock(ExpectedDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectedDataSet, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with only ExportDataSet annotation'() {
		given: 'mock ExportDataSet annotation'
		def exportDataSet = Mock(ExportDataSet)

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, exportDataSet)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should create instance with null annotations'() {
		when: 'creating interceptor with null annotations'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		then: 'instance is created successfully'
		interceptor != null
	}

	def 'should implement IMethodInterceptor interface'() {
		given: 'a new interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		expect: 'implements IMethodInterceptor'
		interceptor instanceof IMethodInterceptor
	}

	def 'should create interceptor with different operations'() {
		given: 'data sets with different operations'
		def dataSet = Mock(DataSet)
		dataSet.operation() >> operation

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null, null)

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
		def interceptor1 = new DatabaseTestInterceptor(dataSet1, expectedDataSet1, null)
		def interceptor2 = new DatabaseTestInterceptor(dataSet2, expectedDataSet2, null)

		then: 'interceptors are independent'
		!interceptor1.is(interceptor2)
	}

	def 'should create interceptor with DataSet having custom sources'() {
		given: 'DataSet with custom sources'
		def dataSet = Mock(DataSet)
		dataSet.sources() >> ([] as DataSetSource[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(dataSet, null, null)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExpectedDataSet having custom sources'() {
		given: 'ExpectedDataSet with custom sources'
		def expectedDataSet = Mock(ExpectedDataSet)
		expectedDataSet.sources() >> ([] as DataSetSource[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, expectedDataSet, null)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExportDataSet having onFailureOnly true'() {
		given: 'ExportDataSet with onFailureOnly enabled'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.onFailureOnly() >> true
		exportDataSet.tables() >> (['USERS', 'ORDERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExportDataSet having onFailureOnly false'() {
		given: 'ExportDataSet with onFailureOnly disabled'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.onFailureOnly() >> false
		exportDataSet.tables() >> (['USERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	def 'should create interceptor with ExportDataSet having custom dataSourceName'() {
		given: 'ExportDataSet with named DataSource'
		def exportDataSet = Mock(ExportDataSet)
		exportDataSet.dataSourceName() >> 'secondary'
		exportDataSet.tables() >> (['USERS'] as String[])

		when: 'creating interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, exportDataSet)

		then: 'interceptor is created successfully'
		interceptor != null
	}

	// Note: Tests for intercept() method are not included because IMethodInvocation
	// and related Spock internal classes cannot be mocked with Spock's mocking framework.
	// The DatabaseTestInterceptor functionality is tested through integration tests
	// in the examples module.

	def 'should get default Configuration when spec does not implement DatabaseTestSupport'() {
		given: 'a test interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		and: 'a stub invocation with non-DatabaseTestSupport instance'
		def invocation = Stub(IMethodInvocation) {
			instance >> new Object()
		}

		when: 'getConfiguration is called'
		def configuration = interceptor.getConfiguration(invocation)

		then: 'default configuration is returned'
		configuration != null
		configuration == Configuration.defaults()
	}

	def 'should get Configuration from DatabaseTestSupport when spec implements trait'() {
		given: 'a test interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		and: 'a custom configuration'
		def customConfig = Configuration.defaults()

		and: 'a stub invocation with DatabaseTestSupport instance'
		def support = Stub(DatabaseTestSupport) {
			getDbTesterConfiguration() >> customConfig
		}
		def invocation = Stub(IMethodInvocation) {
			instance >> support
		}

		when: 'getConfiguration is called'
		def configuration = interceptor.getConfiguration(invocation)

		then: 'custom configuration is returned'
		configuration == customConfig
	}

	def 'should get registry from DatabaseTestSupport when spec implements trait'() {
		given: 'a test interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		and: 'a registry with a default DataSource'
		def expectedRegistry = new DataSourceRegistry()

		and: 'a stub invocation with DatabaseTestSupport instance'
		def support = Stub(DatabaseTestSupport) {
			getDbTesterRegistry() >> expectedRegistry
		}
		def invocation = Stub(IMethodInvocation) {
			instance >> support
		}

		when: 'getRegistry is called'
		def registry = interceptor.getRegistry(invocation)

		then: 'expected registry is returned'
		registry == expectedRegistry
	}

	def 'should throw IllegalStateException when spec does not implement DatabaseTestSupport for registry'() {
		given: 'a test interceptor'
		def interceptor = new DatabaseTestInterceptor(null, null, null)

		and: 'a stub invocation with non-DatabaseTestSupport instance'
		def invocation = Stub(IMethodInvocation) {
			instance >> new Object()
		}

		when: 'getRegistry is called'
		interceptor.getRegistry(invocation)

		then: 'IllegalStateException is thrown'
		def e = thrown(IllegalStateException)
		e.message.contains('must implement DatabaseTestSupport')
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
}
