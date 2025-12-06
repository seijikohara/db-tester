package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Registers Spring-managed {@link DataSource} beans with a {@link DataSourceRegistry}.
 *
 * <p>This class acts as a bridge between the Spring application context and the database testing
 * framework. It discovers all {@link DataSource} beans in the context and provides methods to
 * register them with a {@link DataSourceRegistry}.
 *
 * <p>The registrar discovers DataSource beans using the following rules:
 *
 * <ul>
 *   <li>If a single DataSource is found, it becomes the default
 *   <li>If multiple DataSources are found, the one marked with {@code @Primary} becomes the default
 *   <li>All DataSources are registered by their bean names for named access
 * </ul>
 *
 * @see DataSourceRegistry
 * @see DbTesterJUnitAutoConfiguration
 */
public final class DataSourceRegistrar implements ApplicationContextAware {

  /** Default bean name used as fallback when no primary DataSource is found. */
  private static final String DEFAULT_DATASOURCE_BEAN_NAME = "dataSource";

  /** Logger for tracking DataSource registration activity. */
  private static final Logger logger = LoggerFactory.getLogger(DataSourceRegistrar.class);

  /** Configuration properties for the registrar. */
  private final DbTesterProperties properties;

  /** The Spring application context, set by {@link #setApplicationContext}. */
  private @Nullable ApplicationContext applicationContext;

  /**
   * Creates a new registrar with the specified properties.
   *
   * @param properties the configuration properties
   */
  public DataSourceRegistrar(final DbTesterProperties properties) {
    this.properties = properties;
  }

  /**
   * Sets the application context.
   *
   * <p>This method is called by Spring during bean initialization.
   *
   * @param applicationContext the Spring application context
   * @throws BeansException if context setting fails
   */
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  /**
   * Registers all DataSource beans from the Spring context with the specified registry.
   *
   * <p>This method performs the following:
   *
   * <ol>
   *   <li>Discovers all DataSource beans in the application context
   *   <li>Registers the primary DataSource as the default
   *   <li>Registers all DataSources by their bean names
   * </ol>
   *
   * <p>If auto-registration is disabled in properties, this method does nothing.
   *
   * @param registry the DataSourceRegistry to populate
   * @throws IllegalStateException if application context is not set
   */
  public void registerAll(final DataSourceRegistry registry) {

    Optional.of(properties)
        .filter(DbTesterProperties::isAutoRegisterDataSources)
        .map(props -> resolveApplicationContext())
        .map(context -> context.getBeansOfType(DataSource.class))
        .filter(Predicate.not(Map::isEmpty))
        .ifPresentOrElse(
            dataSources -> registerDataSources(registry, dataSources),
            () -> logger.debug("Auto-registration disabled or no DataSource beans found"));
  }

  /**
   * Registers the discovered DataSources with the registry.
   *
   * @param registry the registry to populate
   * @param dataSources the map of bean names to DataSource instances
   */
  private void registerDataSources(
      final DataSourceRegistry registry, final Map<String, DataSource> dataSources) {

    logger.info("Registering {} DataSource(s) with DataSourceRegistry", dataSources.size());

    // Register each DataSource by name using functional forEach
    dataSources.entrySet().forEach(createRegistrationConsumer(registry));

    // Register default DataSource
    resolveDefaultDataSource(dataSources).ifPresent(createDefaultRegistrationConsumer(registry));
  }

  /**
   * Creates a consumer that registers a DataSource with the given registry.
   *
   * @param registry the registry to register with
   * @return a consumer that registers name-DataSource pairs
   */
  private Consumer<Map.Entry<String, DataSource>> createRegistrationConsumer(
      final DataSourceRegistry registry) {
    return entry -> {
      registry.register(entry.getKey(), entry.getValue());
      logger.debug("Registered DataSource '{}' with registry", entry.getKey());
    };
  }

  /**
   * Creates a consumer that registers a DataSource as the default.
   *
   * @param registry the registry to register with
   * @return a consumer that registers the default DataSource
   */
  private Consumer<Map.Entry<String, DataSource>> createDefaultRegistrationConsumer(
      final DataSourceRegistry registry) {
    return entry -> {
      registry.registerDefault(entry.getValue());
      logger.info("Registered DataSource '{}' as default", entry.getKey());
    };
  }

  /**
   * Resolves the default DataSource from the discovered DataSources.
   *
   * <p>Resolution priority:
   *
   * <ol>
   *   <li>Single DataSource (automatic default)
   *   <li>Primary-annotated DataSource
   *   <li>DataSource named "dataSource"
   * </ol>
   *
   * @param dataSources the map of discovered DataSources
   * @return an Optional containing the default DataSource entry
   */
  private Optional<Map.Entry<String, DataSource>> resolveDefaultDataSource(
      final Map<String, DataSource> dataSources) {

    return Optional.of(dataSources)
        .filter(ds -> ds.size() == 1)
        .map(ds -> ds.entrySet().iterator().next())
        .or(() -> findPrimaryDataSource(dataSources))
        .or(() -> findDataSourceByName(dataSources, DEFAULT_DATASOURCE_BEAN_NAME));
  }

  /**
   * Finds the primary DataSource bean from the context.
   *
   * @param dataSources the map of discovered DataSources
   * @return an Optional containing the primary DataSource entry, or empty if none found
   */
  private Optional<Map.Entry<String, DataSource>> findPrimaryDataSource(
      final Map<String, DataSource> dataSources) {

    final var context = resolveApplicationContext();

    return dataSources.entrySet().stream()
        .filter(entry -> context.containsBeanDefinition(entry.getKey()))
        .filter(entry -> isPrimaryBean(context, entry.getKey()))
        .findFirst();
  }

  /**
   * Finds a DataSource by its bean name.
   *
   * @param dataSources the map of discovered DataSources
   * @param beanName the bean name to search for
   * @return an Optional containing the matching DataSource entry
   */
  private Optional<Map.Entry<String, DataSource>> findDataSourceByName(
      final Map<String, DataSource> dataSources, final String beanName) {

    return dataSources.entrySet().stream()
        .filter(entry -> entry.getKey().equals(beanName))
        .findFirst();
  }

  /**
   * Checks if a bean is marked as primary.
   *
   * @param context the application context
   * @param beanName the bean name to check
   * @return true if the bean is primary
   */
  private boolean isPrimaryBean(final ApplicationContext context, final String beanName) {
    return Optional.of(context)
        .filter(ConfigurableApplicationContext.class::isInstance)
        .map(ConfigurableApplicationContext.class::cast)
        .map(ConfigurableApplicationContext::getBeanFactory)
        .filter(factory -> factory.containsBeanDefinition(beanName))
        .map(factory -> factory.getBeanDefinition(beanName).isPrimary())
        .orElseGet(() -> logAndReturnFalse(beanName));
  }

  /**
   * Logs a debug message and returns false.
   *
   * @param beanName the bean name for logging
   * @return always false
   */
  private boolean logAndReturnFalse(final String beanName) {
    logger.debug("Unable to determine if bean '{}' is primary", beanName);
    return false;
  }

  /**
   * Resolves the application context, throwing if not set.
   *
   * @return the application context
   * @throws IllegalStateException if the context is not set
   */
  private ApplicationContext resolveApplicationContext() {
    return Optional.ofNullable(applicationContext)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "ApplicationContext not set. "
                        + "Ensure this bean is managed by Spring and properly initialized."));
  }
}
