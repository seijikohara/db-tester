package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import spock.lang.Specification

/**
 * Unit tests for {@link DbTesterProperties}.
 *
 * <p>This specification verifies the configuration properties for
 * DB Tester Spring Boot integration with Spock.
 */
class DbTesterPropertiesSpec extends Specification {

	/** The properties under test. */
	DbTesterProperties properties

	def setup() {
		properties = new DbTesterProperties()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new DbTesterProperties()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should have enabled set to true by default'() {
		expect: 'enabled defaults to true'
		properties.enabled
	}

	def 'should have autoRegisterDataSources set to true by default'() {
		expect: 'autoRegisterDataSources defaults to true'
		properties.autoRegisterDataSources
	}

	def 'should allow setting enabled to false'() {
		when: 'setting enabled to false'
		properties.enabled = false

		then: 'enabled is false'
		!properties.enabled
	}

	def 'should allow setting autoRegisterDataSources to false'() {
		when: 'setting autoRegisterDataSources to false'
		properties.autoRegisterDataSources = false

		then: 'autoRegisterDataSources is false'
		!properties.autoRegisterDataSources
	}

	def 'should allow toggling enabled'() {
		given: 'enabled is initially true'
		properties.enabled = false

		when: 'toggling to true'
		properties.enabled = true

		then: 'enabled is true'
		properties.enabled
	}

	def 'should allow toggling autoRegisterDataSources'() {
		given: 'autoRegisterDataSources is set to false'
		properties.autoRegisterDataSources = false

		when: 'toggling to true'
		properties.autoRegisterDataSources = true

		then: 'autoRegisterDataSources is true'
		properties.autoRegisterDataSources
	}

	def 'should maintain independent property values'() {
		when: 'setting enabled to false and autoRegisterDataSources to true'
		properties.enabled = false
		properties.autoRegisterDataSources = true

		then: 'properties are independent'
		!properties.enabled
		properties.autoRegisterDataSources
	}

	def 'should maintain property values across multiple changes'() {
		when: 'making multiple changes'
		properties.enabled = false
		properties.autoRegisterDataSources = false
		properties.enabled = true

		then: 'only enabled is changed back'
		properties.enabled
		!properties.autoRegisterDataSources
	}

	def 'should have convention property with correct defaults'() {
		expect: 'convention defaults are correct'
		properties.convention != null
		properties.convention.baseDirectory == null
		properties.convention.expectationSuffix == '/expected'
		properties.convention.scenarioMarker == '[Scenario]'
		properties.convention.dataFormat == DataFormat.CSV
		properties.convention.tableMergeStrategy == TableMergeStrategy.UNION_ALL
	}

	def 'should allow modifying convention properties'() {
		when: 'modifying convention properties'
		properties.convention.baseDirectory = '/custom/base'
		properties.convention.expectationSuffix = '/verify'
		properties.convention.scenarioMarker = '[TestCase]'
		properties.convention.dataFormat = DataFormat.TSV
		properties.convention.tableMergeStrategy = TableMergeStrategy.FIRST

		then: 'convention properties are modified'
		properties.convention.baseDirectory == '/custom/base'
		properties.convention.expectationSuffix == '/verify'
		properties.convention.scenarioMarker == '[TestCase]'
		properties.convention.dataFormat == DataFormat.TSV
		properties.convention.tableMergeStrategy == TableMergeStrategy.FIRST
	}

	def 'should allow replacing convention'() {
		given: 'a new convention instance'
		def newConvention = new DbTesterProperties.ConventionProperties()
		newConvention.dataFormat = DataFormat.TSV

		when: 'replacing convention'
		properties.convention = newConvention

		then: 'convention is replaced'
		properties.convention.dataFormat == DataFormat.TSV
	}

	def 'should have operation property with correct defaults'() {
		expect: 'operation defaults are correct'
		properties.operation != null
		properties.operation.preparation == Operation.CLEAN_INSERT
		properties.operation.expectation == Operation.NONE
	}

	def 'should allow modifying operation properties'() {
		when: 'modifying operation properties'
		properties.operation.preparation = Operation.INSERT
		properties.operation.expectation = Operation.DELETE_ALL

		then: 'operation properties are modified'
		properties.operation.preparation == Operation.INSERT
		properties.operation.expectation == Operation.DELETE_ALL
	}

	def 'should allow replacing operation'() {
		given: 'a new operation instance'
		def newOperation = new DbTesterProperties.OperationProperties()
		newOperation.preparation = Operation.TRUNCATE_INSERT

		when: 'replacing operation'
		properties.operation = newOperation

		then: 'operation is replaced'
		properties.operation.preparation == Operation.TRUNCATE_INSERT
	}
}
