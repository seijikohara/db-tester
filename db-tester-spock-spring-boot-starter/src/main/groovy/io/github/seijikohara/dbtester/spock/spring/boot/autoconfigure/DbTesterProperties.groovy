package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for DB Tester Spring Boot integration with Spock.
 *
 * <p>These properties control how DataSource beans from the Spring application context are
 * registered with the {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry}.
 *
 * <p>Properties are prefixed with {@code db-tester}.
 */
@ConfigurationProperties(prefix = "db-tester")
class DbTesterProperties {

	/** Whether DB Tester is enabled. Defaults to true. */
	boolean enabled = true

	/** Whether to automatically register DataSource beans. Defaults to true. */
	boolean autoRegisterDataSources = true
}
