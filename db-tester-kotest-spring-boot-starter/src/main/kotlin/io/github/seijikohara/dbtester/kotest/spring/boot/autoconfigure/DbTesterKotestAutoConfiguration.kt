package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.util.ServiceLoader
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
class DbTesterKotestAutoConfiguration {
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
    fun dbTesterConfiguration(properties: DbTesterProperties): Configuration =
        properties.convention
            .let { conventionProps ->
                ConventionSettings(
                    conventionProps.baseDirectory,
                    conventionProps.expectationSuffix,
                    conventionProps.scenarioMarker,
                    conventionProps.dataFormat,
                    conventionProps.tableMergeStrategy,
                    conventionProps.loadOrderFileName,
                )
            }.let { conventions ->
                OperationDefaults(
                    properties.operation.preparation,
                    properties.operation.expectation,
                ).let { operations ->
                    ServiceLoader
                        .load(DataSetLoaderProvider::class.java)
                        .findFirst()
                        .map { it.loader }
                        .orElseThrow {
                            IllegalStateException(
                                "No DataSetLoaderProvider implementation found. Add db-tester-core to your classpath.",
                            )
                        }.let { loader -> Configuration(conventions, operations, loader) }
                }
            }

    /**
     * Creates a DataSourceRegistry bean and registers all available DataSources.
     *
     * If a single DataSource is available, it will be registered as the default. If multiple
     * DataSources are available, they will be registered by their bean names.
     *
     * @param dataSources provider for all DataSource beans
     * @return the data source registry
     */
    @Bean
    @ConditionalOnMissingBean
    fun dbTesterDataSourceRegistry(dataSources: ObjectProvider<DataSource>): DataSourceRegistry =
        DataSourceRegistry().also { registry ->
            dataSources.stream().findFirst().ifPresent { registry.registerDefault(it) }
        }

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
    fun dataSourceRegistrar(properties: DbTesterProperties): DataSourceRegistrar = DataSourceRegistrar(properties)
}
