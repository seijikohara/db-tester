package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.ExecutionSettings
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.config.VerificationSettings
import io.github.seijikohara.dbtester.spring.support.ColumnStrategyConverter
import org.springframework.beans.factory.ObjectProvider
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
        run {
            val conventions =
                ConventionSettings
                    .builder()
                    .baseDirectory(properties.convention.baseDirectory)
                    .expectationSuffix(properties.convention.expectationSuffix)
                    .scenarioMarker(properties.convention.scenarioMarker)
                    .dataFormat(properties.convention.dataFormat)
                    .tableMergeStrategy(properties.convention.tableMergeStrategy)
                    .loadOrderFileName(properties.convention.loadOrderFileName)
                    .build()
            val columnStrategies =
                properties.verification.columnStrategies
                    .filter { it.columnName != null && it.columnName!!.isNotBlank() && it.strategy != null }
                    .associate { prop ->
                        val mapping =
                            ColumnStrategyConverter.toColumnStrategyMapping(
                                prop.columnName!!,
                                prop.strategy!!,
                                prop.pattern,
                            )
                        val entry = ColumnStrategyConverter.toMapEntry(prop.columnName!!, mapping)
                        entry.key to entry.value
                    }
            val verification =
                VerificationSettings
                    .builder()
                    .globalExcludeColumns(properties.verification.globalExcludeColumns)
                    .globalColumnStrategies(columnStrategies)
                    .rowOrdering(properties.verification.rowOrdering)
                    .retryCount(properties.verification.retryCount)
                    .retryDelay(properties.verification.retryDelay)
                    .build()
            val execution =
                ExecutionSettings
                    .builder()
                    .queryTimeout(properties.execution.queryTimeout)
                    .transactionMode(properties.execution.transactionMode)
                    .build()
            val operations =
                OperationDefaults
                    .builder()
                    .preparation(properties.operation.preparation)
                    .expectation(properties.operation.expectation)
                    .build()
            Configuration
                .builder()
                .conventions(conventions)
                .verification(verification)
                .execution(execution)
                .operations(operations)
                .build()
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
    @ConditionalOnMissingBean
    fun dataSourceRegistrar(properties: DbTesterProperties): DataSourceRegistrar = DataSourceRegistrar(properties)
}
