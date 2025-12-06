package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for DB Tester Spring Boot integration with Spock Framework.
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
	 * Creates a default Configuration bean if not already present.
	 *
	 * @return the configuration, never null
	 */
	@Bean
	@ConditionalOnMissingBean
	Configuration dbTesterConfiguration() {
		return Configuration.defaults()
	}

	/**
	 * Creates a DataSourceRegistry bean.
	 *
	 * <p>The registry is populated with DataSources by the {@link DataSourceRegistrar} when tests are
	 * executed. This lazy initialization ensures proper integration with Spock's test lifecycle.
	 *
	 * @return the data source registry, never null
	 */
	@Bean
	@ConditionalOnMissingBean
	DataSourceRegistry dbTesterDataSourceRegistry() {
		return new DataSourceRegistry()
	}

	/**
	 * Creates a DataSourceRegistrar bean for automatic DataSource registration.
	 *
	 * <p>The registrar discovers all Spring-managed DataSource beans and registers them with the
	 * {@link DataSourceRegistry}. It is used by the {@code SpringBootDatabaseTestExtension} to
	 * populate the registry before test execution.
	 *
	 * @param properties the DB Tester properties (must not be null)
	 * @return the data source registrar, never null
	 */
	@Bean
	@ConditionalOnMissingBean
	DataSourceRegistrar dataSourceRegistrar(DbTesterProperties properties) {
		return new DataSourceRegistrar(properties)
	}
}
