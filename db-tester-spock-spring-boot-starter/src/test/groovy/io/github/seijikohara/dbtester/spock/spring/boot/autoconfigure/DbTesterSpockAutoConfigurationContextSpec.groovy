package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification

/**
 * Auto-configuration context tests for {@link DbTesterSpockAutoConfiguration}.
 *
 * <p>These tests use {@link ApplicationContextRunner} to verify conditional
 * auto-configuration behavior, including property-based activation, bean
 * registration, and custom bean overrides.
 */
class DbTesterSpockAutoConfigurationContextSpec extends Specification {

	/** Base context runner with the auto-configuration registered. */
	ApplicationContextRunner contextRunner = new ApplicationContextRunner()
	.withConfiguration(AutoConfigurations.of(DbTesterSpockAutoConfiguration))

	def 'should register all beans with default properties'() {
		expect: 'all beans are registered with default properties'
		contextRunner.run { context ->
			assert context.containsBean('dbTesterConfiguration')
			assert context.containsBean('dbTesterDataSourceRegistry')
			assert context.containsBean('dataSourceRegistrar')
		}
	}

	def 'should register Configuration bean with correct type'() {
		expect: 'Configuration bean is correct type'
		contextRunner.run { context ->
			assert context.getBean('dbTesterConfiguration') instanceof Configuration
		}
	}

	def 'should register DataSourceRegistry bean with correct type'() {
		expect: 'DataSourceRegistry bean is correct type'
		contextRunner.run { context ->
			assert context.getBean('dbTesterDataSourceRegistry') instanceof DataSourceRegistry
		}
	}

	def 'should register DataSourceRegistrar bean with correct type'() {
		expect: 'DataSourceRegistrar bean is correct type'
		contextRunner.run { context ->
			assert context.getBean('dataSourceRegistrar') instanceof DataSourceRegistrar
		}
	}

	def 'should not register beans when disabled'() {
		expect: 'no beans are registered when disabled'
		contextRunner
				.withPropertyValues('db-tester.enabled=false')
				.run { context ->
					assert !context.containsBean('dbTesterConfiguration')
					assert !context.containsBean('dbTesterDataSourceRegistry')
					assert !context.containsBean('dataSourceRegistrar')
				}
	}

	def 'should register beans when explicitly enabled'() {
		expect: 'beans are registered when explicitly enabled'
		contextRunner
				.withPropertyValues('db-tester.enabled=true')
				.run { context ->
					assert context.containsBean('dbTesterConfiguration')
				}
	}

	def 'should register beans when property is not set'() {
		expect: 'beans are registered by default'
		contextRunner.run { context ->
			assert context.containsBean('dbTesterConfiguration')
		}
	}

	def 'should use custom Configuration when provided'() {
		given: 'a custom Configuration bean'
		def customConfig = Configuration.defaults()

		expect: 'custom Configuration is used'
		contextRunner
				.withBean('dbTesterConfiguration', Configuration) { -> customConfig }
				.run { context ->
					assert context.containsBean('dbTesterConfiguration')
					assert context.getBean('dbTesterConfiguration').is(customConfig)
				}
	}

	def 'should use custom DataSourceRegistry when provided'() {
		given: 'a custom DataSourceRegistry bean'
		def customRegistry = new DataSourceRegistry()

		expect: 'custom DataSourceRegistry is used'
		contextRunner
				.withBean('dbTesterDataSourceRegistry', DataSourceRegistry) { -> customRegistry }
				.run { context ->
					assert context.getBean('dbTesterDataSourceRegistry').is(customRegistry)
				}
	}

	def 'should bind convention properties'() {
		expect: 'convention properties are bound'
		contextRunner
				.withPropertyValues(
				'db-tester.convention.expectation-suffix=/verify',
				'db-tester.convention.scenario-marker=[Test]')
				.run { context ->
					def config = context.getBean('dbTesterConfiguration', Configuration)
					assert config.conventions().expectationSuffix() == '/verify'
					assert config.conventions().scenarioMarker() == '[Test]'
				}
	}

	def 'should bind auto-register-data-sources property'() {
		expect: 'property is bound correctly'
		contextRunner
				.withPropertyValues('db-tester.auto-register-data-sources=false')
				.run { context ->
					def properties = context.getBean(DbTesterProperties)
					assert !properties.autoRegisterDataSources
				}
	}
}
