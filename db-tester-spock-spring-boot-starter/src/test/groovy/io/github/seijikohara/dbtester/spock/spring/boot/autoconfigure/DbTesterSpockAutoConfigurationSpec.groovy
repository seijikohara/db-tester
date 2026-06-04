package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrar
import io.github.seijikohara.dbtester.spring.support.DbTesterProperties
import spock.lang.Specification

/**
 * Unit tests for {@link DbTesterSpockAutoConfiguration}.
 *
 * <p>This specification verifies the Spring Boot auto-configuration
 * for DB Tester with Spock.
 */
class DbTesterSpockAutoConfigurationSpec extends Specification {

	/** The auto-configuration under test. */
	DbTesterSpockAutoConfiguration autoConfiguration

	/** Test properties. */
	DbTesterProperties properties

	def setup() {
		autoConfiguration = new DbTesterSpockAutoConfiguration()
		properties = new DbTesterProperties()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new DbTesterSpockAutoConfiguration()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should build Configuration from properties'() {
		given: 'a custom operation property'
		properties.operation.preparation = Operation.INSERT

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration is built and the property is mapped'
		config instanceof Configuration
		config.operations().preparation() == Operation.INSERT
	}

	def 'should return DataSourceRegistrar'() {
		when: 'getting dataSourceRegistrar'
		def registrar = autoConfiguration.dataSourceRegistrar(properties)

		then: 'registrar is not null'
		registrar instanceof DataSourceRegistrar
	}

	def 'should return new DataSourceRegistrar on each call'() {
		when: 'getting dataSourceRegistrar twice'
		def registrar1 = autoConfiguration.dataSourceRegistrar(properties)
		def registrar2 = autoConfiguration.dataSourceRegistrar(properties)

		then: 'different instances are returned'
		!registrar1.is(registrar2)
	}
}
