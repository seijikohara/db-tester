package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import javax.sql.DataSource
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for DB Tester Spring Boot integration with Spock.
 *
 * <p>This auto-configuration is activated when:
 *
 * <ul>
 *   <li>{@link DataSourceRegistry} is on the classpath
 *   <li>{@code db-tester.enabled} property is true (default)
 * </ul>
 *
 * <p>The configuration provides {@link Configuration}, {@link DataSourceRegistry}, and {@link
 * DataSourceRegistrar} beans that enable automatic DataSource registration from the Spring context.
 *
 * <p>The {@code SpringBootDatabaseTestExtension} automatically discovers Spring-managed DataSources
 * and registers them with the testing framework.
 *
 * @see DataSourceRegistry
 * @see DataSourceRegistrar
 */
@AutoConfiguration(afterName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@ConditionalOnClass(DataSource)
@ConditionalOnProperty(prefix = "db-tester", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DbTesterProperties)
class DbTesterSpockAutoConfiguration {

	/**
	 * Creates a Configuration bean from the DB Tester properties.
	 *
	 * <p>If no custom configuration is provided, this method builds a Configuration from the
	 * properties defined in {@code application.properties} or {@code application.yml}.
	 *
	 * @param properties the DB Tester properties
	 * @return the configuration built from properties
	 */
	@Bean
	@ConditionalOnMissingBean
	Configuration dbTesterConfiguration(DbTesterProperties properties) {
		def conventionProps = properties.convention
		def operationProps = properties.operation

		def conventions = ConventionSettings.builder()
				.baseDirectory(conventionProps.baseDirectory)
				.expectationSuffix(conventionProps.expectationSuffix)
				.scenarioMarker(conventionProps.scenarioMarker)
				.dataFormat(conventionProps.dataFormat)
				.tableMergeStrategy(conventionProps.tableMergeStrategy)
				.loadOrderFileName(conventionProps.loadOrderFileName)
				.globalExcludeColumns(conventionProps.globalExcludeColumns)
				.globalColumnStrategies(Map.of())
				.build()

		def operations = OperationDefaults.builder()
				.preparation(operationProps.preparation)
				.expectation(operationProps.expectation)
				.build()

		return Configuration.builder()
				.conventions(conventions)
				.operations(operations)
				.build()
	}

	/**
	 * Creates a DataSourceRegistry bean and registers all available DataSources.
	 *
	 * <p>If a single DataSource is available, it is registered as the default. If multiple
	 * DataSources are available, they are registered by their bean names.
	 *
	 * @param dataSources provider for all DataSource beans
	 * @return the data source registry
	 */
	@Bean
	@ConditionalOnMissingBean
	DataSourceRegistry dbTesterDataSourceRegistry(ObjectProvider<DataSource> dataSources) {
		def registry = new DataSourceRegistry()
		dataSources.stream().findFirst().ifPresent { registry.registerDefault(it) }
		return registry
	}

	/**
	 * Creates a DataSourceRegistrar bean for automatic DataSource registration.
	 *
	 * <p>The registrar discovers all Spring-managed DataSource beans and registers them with the
	 * {@link DataSourceRegistry}. It is used by the {@code SpringBootDatabaseTestExtension} to
	 * populate the registry before test execution.
	 *
	 * @param properties the DB Tester properties
	 * @return the data source registrar
	 */
	@Bean
	@ConditionalOnMissingBean
	DataSourceRegistrar dataSourceRegistrar(DbTesterProperties properties) {
		return new DataSourceRegistrar(properties)
	}
}
