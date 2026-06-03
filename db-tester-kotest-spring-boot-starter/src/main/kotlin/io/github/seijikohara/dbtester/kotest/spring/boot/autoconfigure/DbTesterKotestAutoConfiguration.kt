package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrar
import io.github.seijikohara.dbtester.spring.support.DbTesterConfigurationFactory
import io.github.seijikohara.dbtester.spring.support.DbTesterProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

/**
 * Auto-configuration for DB Tester Spring Boot integration with Kotest.
 *
 * This auto-configuration is activated when:
 * - [DataSourceRegistry] is on the classpath
 * - `db-tester.enabled` property is true (default)
 *
 * The configuration provides a [DataSourceRegistrar] bean that automatically registers
 * Spring-managed [DataSource] beans with the [DataSourceRegistry] used by the database
 * testing framework.
 *
 * @see DataSourceRegistry
 * @see DataSourceRegistrar
 */
@AutoConfiguration(
    afterName = ["org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"],
)
@ConditionalOnClass(DataSource::class)
@ConditionalOnProperty(
    prefix = "db-tester",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(DbTesterProperties::class)
public class DbTesterKotestAutoConfiguration {
    /**
     * Creates a Configuration bean from the DB Tester properties.
     *
     * If no custom configuration is provided, this method builds a Configuration from the
     * properties defined in `application.properties` or `application.yml`.
     *
     * @param properties the DB Tester properties
     * @return the configuration built from properties
     */
    @Bean
    @ConditionalOnMissingBean
    public fun dbTesterConfiguration(properties: DbTesterProperties): Configuration =
        DbTesterConfigurationFactory.toConfiguration(properties)

    /**
     * Creates a [DataSourceRegistrar] bean.
     *
     * The registrar is responsible for registering Spring-managed [DataSource] beans with
     * the [DataSourceRegistry]. It provides a bridge between the Spring application context and
     * the database testing framework.
     *
     * @param properties the database tester configuration properties
     * @return a new DataSourceRegistrar instance
     */
    @Bean
    @ConditionalOnMissingBean
    public fun dataSourceRegistrar(properties: DbTesterProperties): DataSourceRegistrar = DataSourceRegistrar(properties)
}
