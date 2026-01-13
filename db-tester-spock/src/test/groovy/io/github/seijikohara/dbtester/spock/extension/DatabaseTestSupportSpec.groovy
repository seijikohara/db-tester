package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import spock.lang.Specification

/**
 * Unit tests for {@link DatabaseTestSupport} trait.
 *
 * <p>This specification verifies the trait that provides database testing support
 * contract for Spock specifications.
 */
class DatabaseTestSupportSpec extends Specification {

	def 'should provide default configuration when not overridden'() {
		given: 'a class implementing DatabaseTestSupport with only required method'
		def support = new MinimalDatabaseTestSupport()

		expect: 'default configuration is returned'
		support.dbTesterConfiguration == Configuration.defaults()
	}

	def 'should allow custom configuration override'() {
		given: 'a class implementing DatabaseTestSupport with custom configuration'
		def customConfig = Configuration.defaults()
		def support = new CustomConfigDatabaseTestSupport(customConfig)

		expect: 'custom configuration is returned'
		support.dbTesterConfiguration == customConfig
	}

	def 'should require registry implementation'() {
		given: 'a class implementing DatabaseTestSupport'
		def customRegistry = new DataSourceRegistry()
		def support = new CustomRegistryDatabaseTestSupport(customRegistry)

		expect: 'custom registry is returned'
		support.dbTesterRegistry == customRegistry
	}

	/**
	 * Minimal implementation of DatabaseTestSupport for testing.
	 */
	static class MinimalDatabaseTestSupport implements DatabaseTestSupport {

		private final DataSourceRegistry registry = new DataSourceRegistry()

		@Override
		DataSourceRegistry getDbTesterRegistry() {
			registry
		}
	}

	/**
	 * DatabaseTestSupport implementation with custom configuration.
	 */
	static class CustomConfigDatabaseTestSupport implements DatabaseTestSupport {

		private final DataSourceRegistry registry = new DataSourceRegistry()
		private final Configuration configuration

		CustomConfigDatabaseTestSupport(Configuration configuration) {
			this.configuration = configuration
		}

		@Override
		DataSourceRegistry getDbTesterRegistry() {
			registry
		}

		@Override
		Configuration getDbTesterConfiguration() {
			configuration
		}
	}

	/**
	 * DatabaseTestSupport implementation with custom registry.
	 */
	static class CustomRegistryDatabaseTestSupport implements DatabaseTestSupport {

		private final DataSourceRegistry registry

		CustomRegistryDatabaseTestSupport(DataSourceRegistry registry) {
			this.registry = registry
		}

		@Override
		DataSourceRegistry getDbTesterRegistry() {
			registry
		}
	}
}
