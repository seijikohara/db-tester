package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport
import io.github.seijikohara.dbtester.api.spi.ExportSupport
import io.github.seijikohara.dbtester.api.spi.PreparationSupport
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExpectationVerifier
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExportExecutor
import io.github.seijikohara.dbtester.spock.lifecycle.SpockPreparationExecutor
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import spock.lang.Specification

/**
 * Unit tests for {@link DatabaseTestInterceptor}.
 *
 * <p>This specification verifies the Spock method interceptor that handles
 * database setup, verification, and export operations.
 *
 * <p>The {@code intercept} tests inject mock SPI support behind real lifecycle
 * executors and override {@code createTestContext} to bypass Spock invocation
 * reflection, isolating the orchestration branches under test.
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

	def 'intercept runs preparation before the test and verifies on success'() {
		given: 'mock SPI support and a passing test'
		def prepSupport = Mock(PreparationSupport)
		def expectSupport = Mock(ExpectationSupport)
		def exportSupport = Mock(ExportSupport)
		def dataSet = Mock(DataSet)
		def expectedDataSet = Mock(ExpectedDataSet)
		def context = newContext()
		def interceptor = newInterceptor(dataSet, expectedDataSet, null,
				prepSupport, expectSupport, exportSupport, context)
		def invocation = Mock(IMethodInvocation)

		when: 'intercepting'
		interceptor.intercept(invocation)

		then: 'preparation runs, the test proceeds, then verification runs'
		1 * prepSupport.execute(context, dataSet)
		1 * invocation.proceed()
		1 * expectSupport.verify(context, expectedDataSet)
		0 * exportSupport.export(_, _)
	}

	def 'intercept skips verification and rethrows when the test fails'() {
		given:
		def expectSupport = Mock(ExpectationSupport)
		def expectedDataSet = Mock(ExpectedDataSet)
		def context = newContext()
		def interceptor = newInterceptor(null, expectedDataSet, null,
				Mock(PreparationSupport), expectSupport, Mock(ExportSupport), context)
		def invocation = Mock(IMethodInvocation)
		def failure = new RuntimeException('boom')

		when:
		interceptor.intercept(invocation)

		then: 'the failure propagates and verification is skipped'
		1 * invocation.proceed() >> { throw failure }
		0 * expectSupport.verify(_, _)
		def e = thrown(RuntimeException)
		e.is(failure)
	}

	def 'intercept exports on success when onFailureOnly is false'() {
		given:
		def exportSupport = Mock(ExportSupport)
		def exportDataSet = Mock(ExportDataSet) { onFailureOnly() >> false }
		def context = newContext()
		def interceptor = newInterceptor(null, null, exportDataSet,
				Mock(PreparationSupport), Mock(ExpectationSupport), exportSupport, context)

		when:
		interceptor.intercept(Mock(IMethodInvocation))

		then:
		1 * exportSupport.export(context, exportDataSet)
	}

	def 'intercept skips export on success when onFailureOnly is true'() {
		given:
		def exportSupport = Mock(ExportSupport)
		def exportDataSet = Mock(ExportDataSet) { onFailureOnly() >> true }
		def context = newContext()
		def interceptor = newInterceptor(null, null, exportDataSet,
				Mock(PreparationSupport), Mock(ExpectationSupport), exportSupport, context)

		when:
		interceptor.intercept(Mock(IMethodInvocation))

		then:
		0 * exportSupport.export(_, _)
	}

	def 'intercept exports on failure when onFailureOnly is true'() {
		given:
		def exportSupport = Mock(ExportSupport)
		def exportDataSet = Mock(ExportDataSet) { onFailureOnly() >> true }
		def context = newContext()
		def interceptor = newInterceptor(null, null, exportDataSet,
				Mock(PreparationSupport), Mock(ExpectationSupport), exportSupport, context)
		def invocation = Mock(IMethodInvocation)

		when:
		interceptor.intercept(invocation)

		then: 'export runs because the test failed, and the failure propagates'
		1 * invocation.proceed() >> { throw new RuntimeException('boom') }
		1 * exportSupport.export(context, exportDataSet)
		thrown(RuntimeException)
	}

	def 'intercept swallows export errors so they do not mask the result'() {
		given:
		def exportSupport = Mock(ExportSupport)
		def exportDataSet = Mock(ExportDataSet) { onFailureOnly() >> false }
		def context = newContext()
		def interceptor = newInterceptor(null, null, exportDataSet,
				Mock(PreparationSupport), Mock(ExpectationSupport), exportSupport, context)

		when:
		interceptor.intercept(Mock(IMethodInvocation))

		then: 'the export error is caught and not rethrown'
		1 * exportSupport.export(context, exportDataSet) >> { throw new RuntimeException('export failed') }
		noExceptionThrown()
	}

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

	/**
	 * Creates a TestContext backed by arbitrary class and method metadata.
	 *
	 * <p>The class and method only feed diagnostic log messages, so any reflective
	 * method serves the purpose.
	 *
	 * @return a test context
	 */
	private static TestContext newContext() {
		new TestContext(String, String.getMethod('toString'),
				Configuration.defaults(), new DataSourceRegistry())
	}

	/**
	 * Creates an interceptor with real executors backed by mock SPI support.
	 *
	 * <p>The executors are concrete facade classes that the JDK-proxy mock maker cannot
	 * mock, so this wraps each one around its mockable SPI interface. The anonymous
	 * subclass overrides {@code createTestContext} to bypass Spock invocation reflection,
	 * isolating the orchestration logic under test.
	 *
	 * @param dataSet the data set annotation, or null
	 * @param expectedDataSet the expected data set annotation, or null
	 * @param exportDataSet the export data set annotation, or null
	 * @param prepSupport the preparation SPI support
	 * @param expectSupport the expectation SPI support
	 * @param exportSupport the export SPI support
	 * @param context the test context returned from createTestContext
	 * @return the interceptor under test
	 */
	private static DatabaseTestInterceptor newInterceptor(DataSet dataSet,
			ExpectedDataSet expectedDataSet, ExportDataSet exportDataSet,
			PreparationSupport prepSupport, ExpectationSupport expectSupport,
			ExportSupport exportSupport, TestContext context) {
		def prep = new SpockPreparationExecutor(prepSupport)
		def verify = new SpockExpectationVerifier(expectSupport)
		def export = new SpockExportExecutor(exportSupport)
		new DatabaseTestInterceptor(dataSet, expectedDataSet, exportDataSet, prep, verify, export) {
					@Override
					protected TestContext createTestContext(IMethodInvocation invocation) {
						context
					}
				}
	}
}
