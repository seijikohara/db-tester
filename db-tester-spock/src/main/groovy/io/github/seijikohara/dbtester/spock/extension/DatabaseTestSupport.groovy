package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry

/**
 * Trait for Spock specifications that require database testing support.
 *
 * <p>Implement this trait in your specification class to provide the {@link DataSourceRegistry}
 * and optionally a custom {@link Configuration} for database testing.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * @DatabaseTest
 * class MySpec extends Specification implements DatabaseTestSupport {
 *
 *     DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()
 *
 *     def setupSpec() {
 *         dbTesterRegistry.registerDefault(createDataSource())
 *     }
 *
 *     @DataSet
 *     @ExpectedDataSet
 *     def "should verify database state"() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * <p><b>With custom configuration:</b>
 * <pre>{@code
 * @DatabaseTest
 * class MySpec extends Specification implements DatabaseTestSupport {
 *
 *     DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()
 *
 *     Configuration dbTesterConfiguration = Configuration.builder()
 *         .conventions(ConventionSettings.builder()
 *             .dataFormat(DataFormat.TSV)
 *             .build())
 *         .build()
 *
 *     // test methods...
 * }
 * }</pre>
 *
 * @see DatabaseTest
 * @see DatabaseTestExtension
 * @see DataSourceRegistry
 * @see Configuration
 */
trait DatabaseTestSupport {

	/**
	 * The data source registry for database connections.
	 *
	 * <p>This property must be implemented by the specification class. Register your
	 * {@link javax.sql.DataSource} instances with this registry before tests run.
	 */
	abstract DataSourceRegistry getDbTesterRegistry()

	/**
	 * Optional custom configuration for database testing.
	 *
	 * <p>Override this property to provide custom settings such as data format,
	 * scenario marker, or operation defaults. When not overridden, returns
	 * the default configuration.
	 *
	 * @return the configuration, defaults to {@link Configuration#defaults()}
	 */
	Configuration getDbTesterConfiguration() {
		Configuration.defaults()
	}
}
