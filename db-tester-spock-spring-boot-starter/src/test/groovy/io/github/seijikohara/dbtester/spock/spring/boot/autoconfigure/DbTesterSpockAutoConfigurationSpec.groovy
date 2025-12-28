package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
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

	def 'should return default Configuration'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration is not null'
		config != null
		config instanceof Configuration
	}

	def 'should return Configuration with conventions'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has conventions'
		config.conventions() != null
	}

	def 'should return Configuration with operations'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has operations'
		config.operations() != null
	}

	def 'should return Configuration with loader'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has loader'
		config.loader() != null
	}

	def 'should return DataSourceRegistry'() {
		when: 'getting dbTesterDataSourceRegistry'
		def registry = autoConfiguration.dbTesterDataSourceRegistry()

		then: 'registry is not null'
		registry != null
		registry instanceof DataSourceRegistry
	}

	def 'should return empty DataSourceRegistry initially'() {
		when: 'getting dbTesterDataSourceRegistry'
		def registry = autoConfiguration.dbTesterDataSourceRegistry()

		then: 'registry has no default'
		!registry.hasDefault()
	}

	def 'should return new DataSourceRegistry on each call'() {
		when: 'getting dbTesterDataSourceRegistry twice'
		def registry1 = autoConfiguration.dbTesterDataSourceRegistry()
		def registry2 = autoConfiguration.dbTesterDataSourceRegistry()

		then: 'different instances are returned'
		!registry1.is(registry2)
	}

	def 'should return DataSourceRegistrar'() {
		when: 'getting dataSourceRegistrar'
		def registrar = autoConfiguration.dataSourceRegistrar(properties)

		then: 'registrar is not null'
		registrar != null
		registrar instanceof DataSourceRegistrar
	}

	def 'should return DataSourceRegistrar with provided properties'() {
		given: 'custom properties'
		def customProperties = new DbTesterProperties()
		customProperties.autoRegisterDataSources = false

		when: 'getting dataSourceRegistrar'
		def registrar = autoConfiguration.dataSourceRegistrar(customProperties)

		then: 'registrar is created with properties'
		registrar != null
	}

	def 'should return new DataSourceRegistrar on each call'() {
		when: 'getting dataSourceRegistrar twice'
		def registrar1 = autoConfiguration.dataSourceRegistrar(properties)
		def registrar2 = autoConfiguration.dataSourceRegistrar(properties)

		then: 'different instances are returned'
		!registrar1.is(registrar2)
	}

	def 'should create all beans successfully'() {
		when: 'creating all beans'
		def config = autoConfiguration.dbTesterConfiguration(properties)
		def registry = autoConfiguration.dbTesterDataSourceRegistry()
		def registrar = autoConfiguration.dataSourceRegistrar(properties)

		then: 'all beans are created'
		config != null
		registry != null
		registrar != null
	}
}
