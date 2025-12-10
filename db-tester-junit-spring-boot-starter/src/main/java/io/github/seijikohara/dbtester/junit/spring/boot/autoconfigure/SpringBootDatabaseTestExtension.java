package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Spring Boot-aware database test extension that automatically registers DataSources.
 *
 * <p>This extension extends {@link DatabaseTestExtension} and adds automatic DataSource
 * registration from the Spring {@link ApplicationContext}. When the {@code
 * db-tester.auto-register-data-sources} property is set to {@code true} (the default), all
 * Spring-managed {@link javax.sql.DataSource} beans are automatically registered with the {@link
 * io.github.seijikohara.dbtester.api.config.DataSourceRegistry} before any test execution.
 *
 * <p>For custom DataSource registration (e.g., multiple DataSources with specific names), use the
 * parent class {@link DatabaseTestExtension} directly and perform manual registration in
 * {@code @BeforeAll}.
 *
 * @see DatabaseTestExtension
 * @see DataSourceRegistrar
 * @see DbTesterJUnitAutoConfiguration
 */
public class SpringBootDatabaseTestExtension extends DatabaseTestExtension
    implements BeforeAllCallback {

  /** Logger for tracking automatic DataSource registration. */
  private static final Logger logger =
      LoggerFactory.getLogger(SpringBootDatabaseTestExtension.class);

  /**
   * Creates a new Spring Boot database test extension.
   *
   * <p>This constructor is called by JUnit Jupiter when the extension is registered via
   * {@code @ExtendWith}.
   */
  public SpringBootDatabaseTestExtension() {
    super();
  }

  /**
   * Automatically registers Spring-managed DataSources and Configuration before all tests.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Retrieves the Spring {@link ApplicationContext} using {@link SpringExtension}
   *   <li>Registers the Spring-managed {@link Configuration} bean with the extension context
   *   <li>Checks if {@link DataSourceRegistrar} is available in the context
   *   <li>If available and auto-registration is enabled, registers all DataSources with the {@link
   *       io.github.seijikohara.dbtester.api.config.DataSourceRegistry}
   * </ol>
   *
   * <p>If the Spring context is not available (e.g., non-Spring tests) or if {@link
   * DataSourceRegistrar} is not configured, this method silently skips automatic registration,
   * allowing the extension to work in both Spring and non-Spring environments.
   *
   * @param context the extension context for the test class
   */
  @Override
  public void beforeAll(final ExtensionContext context) {
    try {
      final var applicationContext = SpringExtension.getApplicationContext(context);
      registerConfigurationFromContext(context, applicationContext);
      registerDataSourcesFromContext(context, applicationContext);
    } catch (final IllegalStateException e) {
      // Spring context not available - this is expected for non-Spring tests
      logger.debug(
          "Spring ApplicationContext not available, skipping automatic DataSource registration: {}",
          e.getMessage());
    }
  }

  /**
   * Registers Configuration from the Spring ApplicationContext.
   *
   * <p>If a Configuration bean is available in the Spring context, it will be used by the test
   * extension. This allows configuration to be customized via application.properties.
   *
   * @param context the extension context
   * @param applicationContext the Spring application context
   */
  private void registerConfigurationFromContext(
      final ExtensionContext context, final ApplicationContext applicationContext) {

    Optional.of(applicationContext)
        .filter(appContext -> appContext.containsBean("dbTesterConfiguration"))
        .map(appContext -> appContext.getBean("dbTesterConfiguration", Configuration.class))
        .ifPresentOrElse(
            configuration -> {
              setConfiguration(context, configuration);
              logger.debug(
                  "Registered Spring-managed Configuration with database testing framework");
            },
            () ->
                logger.debug(
                    "Configuration bean not found in ApplicationContext, using default configuration"));
  }

  /**
   * Registers DataSources from the Spring ApplicationContext.
   *
   * @param context the extension context
   * @param applicationContext the Spring application context
   */
  private void registerDataSourcesFromContext(
      final ExtensionContext context, final ApplicationContext applicationContext) {

    Optional.of(applicationContext)
        .filter(appContext -> appContext.containsBean("dataSourceRegistrar"))
        .ifPresentOrElse(
            appContext -> registerDataSources(context, appContext),
            () ->
                logger.debug(
                    "DataSourceRegistrar bean not found in ApplicationContext, skipping automatic DataSource registration"));
  }

  /**
   * Registers DataSources using the registrar bean.
   *
   * @param context the extension context
   * @param applicationContext the Spring application context
   */
  private void registerDataSources(
      final ExtensionContext context, final ApplicationContext applicationContext) {

    final var registrar = applicationContext.getBean(DataSourceRegistrar.class);
    final var registry = getRegistry(context);

    logger.info("Automatically registering Spring DataSources with database testing framework");
    registrar.registerAll(registry);
    logger.info("Automatic DataSource registration completed");
  }
}
