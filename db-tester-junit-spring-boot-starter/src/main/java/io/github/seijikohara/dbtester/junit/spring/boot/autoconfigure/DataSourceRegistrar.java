package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrarSupport;
import io.github.seijikohara.dbtester.spring.support.PrimaryBeanResolver;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registers Spring-managed {@link DataSource} beans with a {@link DataSourceRegistry}.
 *
 * <p>This class acts as a bridge between the Spring application context and the database testing
 * framework. It discovers all {@link DataSource} beans in the context and delegates registration to
 * {@link DataSourceRegistrarSupport}.
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
 * @see DataSourceRegistrarSupport
 * @see DbTesterJUnitAutoConfiguration
 */
public final class DataSourceRegistrar implements ApplicationContextAware {

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
   *   <li>Delegates to {@link DataSourceRegistrarSupport} for registration
   * </ol>
   *
   * <p>If auto-registration is disabled in properties, this method does nothing.
   *
   * @param registry the DataSourceRegistry to populate
   * @throws IllegalStateException if application context is not set
   */
  public void registerAll(final DataSourceRegistry registry) {
    if (!properties.isAutoRegisterDataSources()) {
      logger.debug("Auto-registration disabled");
      return;
    }

    final var context = resolveApplicationContext();
    final var dataSources = context.getBeansOfType(DataSource.class);

    if (dataSources.isEmpty()) {
      logger.debug("No DataSource beans found");
      return;
    }

    DataSourceRegistrarSupport.registerDataSources(
        registry,
        dataSources,
        name -> PrimaryBeanResolver.isPrimaryBean(context, name, logger),
        logger);
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
