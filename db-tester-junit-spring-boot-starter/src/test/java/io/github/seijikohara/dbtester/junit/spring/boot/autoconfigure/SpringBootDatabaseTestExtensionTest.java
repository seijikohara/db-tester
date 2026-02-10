package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Unit tests for {@link SpringBootDatabaseTestExtension}. */
@DisplayName("SpringBootDatabaseTestExtension")
class SpringBootDatabaseTestExtensionTest {

  /** Tests for the SpringBootDatabaseTestExtension class. */
  SpringBootDatabaseTestExtensionTest() {}

  /** The extension under test. */
  private SpringBootDatabaseTestExtension extension;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    extension = new SpringBootDatabaseTestExtension();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new SpringBootDatabaseTestExtension();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the beforeAll(ExtensionContext) method. */
  @Nested
  @DisplayName("beforeAll(ExtensionContext) method")
  class BeforeAllMethod {

    /** Tests for the beforeAll method. */
    BeforeAllMethod() {}

    /** Verifies that beforeAll registers Configuration and DataSources when both beans exist. */
    @Test
    @Tag("normal")
    @DisplayName("should register Configuration and DataSources when Spring context has both beans")
    void shouldRegisterBoth_whenSpringContextHasBothBeans() {
      // Given
      final var extensionContext = createMockExtensionContext();
      final var applicationContext = mock(ApplicationContext.class);
      final var configuration = Configuration.defaults();
      final var registrar = mock(DataSourceRegistrar.class);

      when(applicationContext.containsBean("dbTesterConfiguration")).thenReturn(true);
      when(applicationContext.getBean("dbTesterConfiguration", Configuration.class))
          .thenReturn(configuration);
      when(applicationContext.containsBean("dataSourceRegistrar")).thenReturn(true);
      when(applicationContext.getBean(DataSourceRegistrar.class)).thenReturn(registrar);

      try (final var springExtensionMock = mockStatic(SpringExtension.class)) {
        springExtensionMock
            .when(() -> SpringExtension.getApplicationContext(extensionContext))
            .thenReturn(applicationContext);

        // When
        extension.beforeAll(extensionContext);

        // Then
        verify(registrar).registerAll(any(DataSourceRegistry.class));
      }
    }

    /** Verifies that beforeAll skips Configuration when bean does not exist. */
    @Test
    @Tag("normal")
    @DisplayName("should skip Configuration when bean does not exist")
    void shouldSkipConfiguration_whenBeanDoesNotExist() {
      // Given
      final var extensionContext = createMockExtensionContext();
      final var applicationContext = mock(ApplicationContext.class);
      final var registrar = mock(DataSourceRegistrar.class);

      when(applicationContext.containsBean("dbTesterConfiguration")).thenReturn(false);
      when(applicationContext.containsBean("dataSourceRegistrar")).thenReturn(true);
      when(applicationContext.getBean(DataSourceRegistrar.class)).thenReturn(registrar);

      try (final var springExtensionMock = mockStatic(SpringExtension.class)) {
        springExtensionMock
            .when(() -> SpringExtension.getApplicationContext(extensionContext))
            .thenReturn(applicationContext);

        // When
        extension.beforeAll(extensionContext);

        // Then
        assertAll(
            "should skip Configuration but register DataSources",
            () ->
                verify(applicationContext, never())
                    .getBean(eq("dbTesterConfiguration"), eq(Configuration.class)),
            () -> verify(registrar).registerAll(any(DataSourceRegistry.class)));
      }
    }

    /** Verifies that beforeAll skips DataSource registration when registrar does not exist. */
    @Test
    @Tag("normal")
    @DisplayName("should skip DataSource registration when registrar does not exist")
    void shouldSkipDataSourceRegistration_whenRegistrarDoesNotExist() {
      // Given
      final var extensionContext = createMockExtensionContext();
      final var applicationContext = mock(ApplicationContext.class);
      final var configuration = Configuration.defaults();

      when(applicationContext.containsBean("dbTesterConfiguration")).thenReturn(true);
      when(applicationContext.getBean("dbTesterConfiguration", Configuration.class))
          .thenReturn(configuration);
      when(applicationContext.containsBean("dataSourceRegistrar")).thenReturn(false);

      try (final var springExtensionMock = mockStatic(SpringExtension.class)) {
        springExtensionMock
            .when(() -> SpringExtension.getApplicationContext(extensionContext))
            .thenReturn(applicationContext);

        // When
        extension.beforeAll(extensionContext);

        // Then
        verify(applicationContext, never()).getBean(DataSourceRegistrar.class);
      }
    }

    /** Verifies that beforeAll handles missing Spring context gracefully. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle missing Spring context gracefully")
    void shouldHandleGracefully_whenSpringContextNotAvailable() {
      // Given
      final var extensionContext = createMockExtensionContext();

      try (final var springExtensionMock = mockStatic(SpringExtension.class)) {
        springExtensionMock
            .when(() -> SpringExtension.getApplicationContext(extensionContext))
            .thenThrow(new IllegalStateException("No Spring context available"));

        // When & Then
        assertDoesNotThrow(
            () -> extension.beforeAll(extensionContext),
            "should not throw when Spring context is not available");
      }
    }

    /** Verifies that beforeAll handles context with no beans at all. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle context with no beans")
    void shouldHandleGracefully_whenNoBeans() {
      // Given
      final var extensionContext = createMockExtensionContext();
      final var applicationContext = mock(ApplicationContext.class);

      when(applicationContext.containsBean("dbTesterConfiguration")).thenReturn(false);
      when(applicationContext.containsBean("dataSourceRegistrar")).thenReturn(false);

      try (final var springExtensionMock = mockStatic(SpringExtension.class)) {
        springExtensionMock
            .when(() -> SpringExtension.getApplicationContext(extensionContext))
            .thenReturn(applicationContext);

        // When & Then
        assertDoesNotThrow(
            () -> extension.beforeAll(extensionContext),
            "should not throw when no beans are present");
      }
    }
  }

  /**
   * Creates a mock ExtensionContext with proper store chain for DatabaseTestExtension.
   *
   * <p>The mock configures the context hierarchy required by {@link
   * io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension#getRegistry} which
   * uses {@code extensionContext.getRoot().getStore(namespace)}.
   *
   * @return the mock ExtensionContext
   */
  private static ExtensionContext createMockExtensionContext() {
    final var store = mock(Store.class);
    final var rootContext = mock(ExtensionContext.class);
    final var extensionContext = mock(ExtensionContext.class);

    when(extensionContext.getRoot()).thenReturn(rootContext);
    when(rootContext.getStore(any(Namespace.class))).thenReturn(store);
    when(extensionContext.getRequiredTestClass())
        .thenAnswer(invocation -> SpringBootDatabaseTestExtensionTest.class);

    return extensionContext;
  }
}
