package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.util.Map;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for DB Tester Spring Boot integration with JUnit 5.
 *
 * <p>This auto-configuration is activated when:
 *
 * <ul>
 *   <li>{@link DataSourceRegistry} is on the classpath
 *   <li>{@code db-tester.enabled} property is true (default)
 * </ul>
 *
 * <p>The configuration provides a {@link DataSourceRegistrar} bean that automatically registers
 * Spring-managed {@link DataSource} beans with the {@link DataSourceRegistry} used by the database
 * testing framework.
 *
 * @see DataSourceRegistry
 * @see DatabaseTestExtension
 * @see DataSourceRegistrar
 */
@AutoConfiguration(
    afterName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(
    prefix = "db-tester",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(DbTesterProperties.class)
public class DbTesterJUnitAutoConfiguration {

  /**
   * Creates a new auto-configuration instance.
   *
   * <p>This constructor is called by Spring Boot's auto-configuration mechanism.
   */
  public DbTesterJUnitAutoConfiguration() {
    // Default constructor for Spring auto-configuration
  }

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
  public Configuration dbTesterConfiguration(final DbTesterProperties properties) {
    final DbTesterProperties.ConventionProperties conventionProps = properties.getConvention();
    final DbTesterProperties.OperationProperties operationProps = properties.getOperation();

    final Map<String, ColumnStrategyMapping> globalColumnStrategies = Map.of();

    final ConventionSettings conventions =
        new ConventionSettings(
            conventionProps.getBaseDirectory(),
            conventionProps.getExpectationSuffix(),
            conventionProps.getScenarioMarker(),
            conventionProps.getDataFormat(),
            conventionProps.getTableMergeStrategy(),
            conventionProps.getLoadOrderFileName(),
            conventionProps.getGlobalExcludeColumns(),
            globalColumnStrategies);

    final OperationDefaults operations =
        new OperationDefaults(operationProps.getPreparation(), operationProps.getExpectation());

    final DataSetLoader loader = loadDataSetLoader();

    return new Configuration(conventions, operations, loader);
  }

  /**
   * Loads the DataSetLoader implementation via ServiceLoader.
   *
   * @return the DataSetLoader instance
   */
  private DataSetLoader loadDataSetLoader() {
    return ServiceLoader.load(DataSetLoaderProvider.class)
        .findFirst()
        .map(DataSetLoaderProvider::getLoader)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No DataSetLoaderProvider implementation found. Add db-tester-core to your classpath."));
  }

  /**
   * Creates a DataSourceRegistry bean and registers all available DataSources.
   *
   * <p>If a single DataSource is available, it will be registered as the default. If multiple
   * DataSources are available, they will be registered by their bean names.
   *
   * @param dataSources provider for all DataSource beans
   * @return the data source registry
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSourceRegistry dbTesterDataSourceRegistry(
      final ObjectProvider<DataSource> dataSources) {
    final DataSourceRegistry registry = new DataSourceRegistry();

    // Register the first DataSource as default
    dataSources.stream().findFirst().ifPresent(registry::registerDefault);

    return registry;
  }

  /**
   * Creates a {@link DataSourceRegistrar} bean.
   *
   * <p>The registrar is responsible for registering Spring-managed {@link DataSource} beans with
   * the {@link DataSourceRegistry}. It provides a bridge between the Spring application context and
   * the database testing framework.
   *
   * @param properties the database tester configuration properties
   * @return a new DataSourceRegistrar instance
   */
  @Bean
  public DataSourceRegistrar dataSourceRegistrar(final DbTesterProperties properties) {
    return new DataSourceRegistrar(properties);
  }
}
