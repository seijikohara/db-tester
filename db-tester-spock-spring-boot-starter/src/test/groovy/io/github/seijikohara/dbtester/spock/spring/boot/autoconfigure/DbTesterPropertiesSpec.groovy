package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

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
}
