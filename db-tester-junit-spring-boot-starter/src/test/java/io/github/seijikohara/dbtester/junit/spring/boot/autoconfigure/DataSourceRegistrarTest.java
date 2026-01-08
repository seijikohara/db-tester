package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/** Unit tests for {@link DataSourceRegistrar}. */
@DisplayName("DataSourceRegistrar")
class DataSourceRegistrarTest {

  /** Tests for the DataSourceRegistrar class. */
  DataSourceRegistrarTest() {}

  /** The properties instance for testing. */
  private DbTesterProperties properties;

  /** The registrar instance under test. */
  private DataSourceRegistrar registrar;

  /** The registry instance for testing. */
  private DataSourceRegistry registry;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    properties = new DbTesterProperties();
    registrar = new DataSourceRegistrar(properties);
    registry = new DataSourceRegistry();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates instance with properties. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with properties")
    void should_create_instance_with_properties() {
      // Given
      final var customProperties = new DbTesterProperties();
      customProperties.setEnabled(false);

      // When
      final var customRegistrar = new DataSourceRegistrar(customProperties);

      // Then
      assertNotNull(customRegistrar, "registrar should be created");
    }
  }

  /** Tests for the setApplicationContext method. */
  @Nested
  @DisplayName("setApplicationContext(ApplicationContext) method")
  class SetApplicationContextMethod {

    /** Tests for the setApplicationContext method. */
    SetApplicationContextMethod() {}

    /** Verifies that setApplicationContext accepts a context. */
    @Test
    @Tag("normal")
    @DisplayName("should accept application context")
    void should_accept_application_context() {
      // Given
      final var context = mock(ConfigurableApplicationContext.class);

      // When & Then (no exception)
      registrar.setApplicationContext(context);
    }
  }

  /** Tests for the registerAll method. */
  @Nested
  @DisplayName("registerAll(DataSourceRegistry) method")
  class RegisterAllMethod {

    /** Tests for the registerAll method. */
    RegisterAllMethod() {}

    /** Verifies that registerAll throws when context is not set. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalStateException when context not set")
    void should_throw_when_context_not_set() {
      // Given - no context set

      // When & Then
      assertThrows(
          IllegalStateException.class,
          () -> registrar.registerAll(registry),
          "should throw when context not set");
    }

    /** Verifies that registerAll does nothing when auto-register is disabled. */
    @Test
    @Tag("normal")
    @DisplayName("should do nothing when auto-register is disabled")
    void should_do_nothing_when_auto_register_disabled() {
      // Given
      properties.setAutoRegisterDataSources(false);
      final var context = mock(ConfigurableApplicationContext.class);
      registrar.setApplicationContext(context);

      // When
      registrar.registerAll(registry);

      // Then
      assertFalse(registry.hasDefault(), "should not register any DataSources");
    }

    /** Verifies that registerAll registers single DataSource as default. */
    @Test
    @Tag("normal")
    @DisplayName("should register single DataSource as default")
    void should_register_single_data_source_as_default() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var context = createContextWithDataSources(Map.of("dataSource", dataSource));
      registrar.setApplicationContext(context);

      // When
      registrar.registerAll(registry);

      // Then
      assertAll(
          "should register as default",
          () -> assertTrue(registry.hasDefault(), "should have default"),
          () ->
              assertSame(dataSource, registry.getDefault(), "should be the registered DataSource"),
          () -> assertTrue(registry.has("dataSource"), "should be registered by name"));
    }

    /** Verifies that registerAll registers multiple DataSources by name. */
    @Test
    @Tag("normal")
    @DisplayName("should register multiple DataSources by name")
    void should_register_multiple_data_sources_by_name() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final var context = createContextWithDataSources(Map.of("ds1", ds1, "ds2", ds2));
      registrar.setApplicationContext(context);

      // When
      registrar.registerAll(registry);

      // Then
      assertAll(
          "should register all DataSources by name",
          () -> assertTrue(registry.has("ds1"), "should have ds1"),
          () -> assertTrue(registry.has("ds2"), "should have ds2"),
          () -> assertSame(ds1, registry.get("ds1"), "ds1 should match"),
          () -> assertSame(ds2, registry.get("ds2"), "ds2 should match"));
    }

    /** Verifies that registerAll registers primary DataSource as default. */
    @Test
    @Tag("normal")
    @DisplayName("should register primary DataSource as default")
    void should_register_primary_data_source_as_default() {
      // Given
      final var primaryDs = mock(DataSource.class);
      final var secondaryDs = mock(DataSource.class);
      final var context =
          createContextWithPrimaryDataSource("primaryDs", primaryDs, "secondaryDs", secondaryDs);
      registrar.setApplicationContext(context);

      // When
      registrar.registerAll(registry);

      // Then
      assertAll(
          "should register primary as default",
          () -> assertTrue(registry.hasDefault(), "should have default"),
          () ->
              assertSame(primaryDs, registry.getDefault(), "default should be primary DataSource"));
    }

    /** Verifies that registerAll falls back to 'dataSource' bean name. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to 'dataSource' bean name when no primary")
    void should_fall_back_to_data_source_bean_name() {
      // Given
      final var defaultDs = mock(DataSource.class);
      final var otherDs = mock(DataSource.class);
      final var context =
          createContextWithDataSources(Map.of("dataSource", defaultDs, "otherDs", otherDs));
      registrar.setApplicationContext(context);

      // When
      registrar.registerAll(registry);

      // Then
      assertAll(
          "should use 'dataSource' as default",
          () -> assertTrue(registry.hasDefault(), "should have default"),
          () ->
              assertSame(defaultDs, registry.getDefault(), "default should be 'dataSource' bean"));
    }
  }

  /**
   * Creates a mock ConfigurableApplicationContext with the given DataSources.
   *
   * @param dataSources map of bean names to DataSources
   * @return the mock context
   */
  private ConfigurableApplicationContext createContextWithDataSources(
      final Map<String, DataSource> dataSources) {
    final var context = mock(ConfigurableApplicationContext.class);
    final var beanFactory = mock(ConfigurableListableBeanFactory.class);

    when(context.getBeansOfType(DataSource.class)).thenReturn(dataSources);
    when(context.getBeanFactory()).thenReturn(beanFactory);

    dataSources
        .keySet()
        .forEach(
            beanName -> {
              when(context.containsBeanDefinition(beanName)).thenReturn(true);
              when(beanFactory.containsBeanDefinition(beanName)).thenReturn(true);
              final var beanDef = mock(BeanDefinition.class);
              when(beanDef.isPrimary()).thenReturn(false);
              when(beanFactory.getBeanDefinition(beanName)).thenReturn(beanDef);
            });

    return context;
  }

  /**
   * Creates a mock ConfigurableApplicationContext with a primary DataSource.
   *
   * @param primaryName the primary DataSource bean name
   * @param primaryDs the primary DataSource
   * @param secondaryName the secondary DataSource bean name
   * @param secondaryDs the secondary DataSource
   * @return the mock context
   */
  private ConfigurableApplicationContext createContextWithPrimaryDataSource(
      final String primaryName,
      final DataSource primaryDs,
      final String secondaryName,
      final DataSource secondaryDs) {
    final var context = mock(ConfigurableApplicationContext.class);
    final var beanFactory = mock(ConfigurableListableBeanFactory.class);

    when(context.getBeansOfType(DataSource.class))
        .thenReturn(Map.of(primaryName, primaryDs, secondaryName, secondaryDs));
    when(context.getBeanFactory()).thenReturn(beanFactory);

    // Primary DataSource
    when(context.containsBeanDefinition(primaryName)).thenReturn(true);
    when(beanFactory.containsBeanDefinition(primaryName)).thenReturn(true);
    final var primaryBeanDef = mock(BeanDefinition.class);
    when(primaryBeanDef.isPrimary()).thenReturn(true);
    when(beanFactory.getBeanDefinition(primaryName)).thenReturn(primaryBeanDef);

    // Secondary DataSource
    when(context.containsBeanDefinition(secondaryName)).thenReturn(true);
    when(beanFactory.containsBeanDefinition(secondaryName)).thenReturn(true);
    final var secondaryBeanDef = mock(BeanDefinition.class);
    when(secondaryBeanDef.isPrimary()).thenReturn(false);
    when(beanFactory.getBeanDefinition(secondaryName)).thenReturn(secondaryBeanDef);

    return context;
  }
}
