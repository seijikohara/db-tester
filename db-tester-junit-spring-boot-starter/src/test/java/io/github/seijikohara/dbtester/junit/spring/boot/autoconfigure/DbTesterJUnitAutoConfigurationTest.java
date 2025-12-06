package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/** Unit tests for {@link DbTesterJUnitAutoConfiguration}. */
@DisplayName("DbTesterJUnitAutoConfiguration")
class DbTesterJUnitAutoConfigurationTest {

  /** Tests for the DbTesterJUnitAutoConfiguration class. */
  DbTesterJUnitAutoConfigurationTest() {}

  /** The auto-configuration instance under test. */
  private DbTesterJUnitAutoConfiguration autoConfiguration;

  /** The properties instance for testing. */
  private DbTesterProperties properties;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    autoConfiguration = new DbTesterJUnitAutoConfiguration();
    properties = new DbTesterProperties();
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
    void should_create_instance() {
      // Given & When
      final var instance = new DbTesterJUnitAutoConfiguration();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the dbTesterConfiguration bean. */
  @Nested
  @DisplayName("dbTesterConfiguration(DbTesterProperties) method")
  class DbTesterConfigurationMethod {

    /** Tests for the dbTesterConfiguration method. */
    DbTesterConfigurationMethod() {}

    /** Verifies that dbTesterConfiguration returns Configuration.defaults(). */
    @Test
    @Tag("normal")
    @DisplayName("should return default Configuration")
    void should_return_default_configuration() {
      // Given & When
      final var config = autoConfiguration.dbTesterConfiguration(properties);

      // Then
      assertNotNull(config, "configuration should not be null");
    }

    /** Verifies that Configuration has conventions. */
    @Test
    @Tag("normal")
    @DisplayName("should return Configuration with conventions")
    void should_return_configuration_with_conventions() {
      // Given & When
      final var config = autoConfiguration.dbTesterConfiguration(properties);

      // Then
      assertNotNull(config.conventions(), "conventions should not be null");
    }

    /** Verifies that Configuration has operations. */
    @Test
    @Tag("normal")
    @DisplayName("should return Configuration with operations")
    void should_return_configuration_with_operations() {
      // Given & When
      final var config = autoConfiguration.dbTesterConfiguration(properties);

      // Then
      assertNotNull(config.operations(), "operations should not be null");
    }
  }

  /** Tests for the dbTesterDataSourceRegistry bean. */
  @Nested
  @DisplayName("dbTesterDataSourceRegistry(ObjectProvider) method")
  class DbTesterDataSourceRegistryMethod {

    /** Tests for the dbTesterDataSourceRegistry method. */
    DbTesterDataSourceRegistryMethod() {}

    /** Verifies that dbTesterDataSourceRegistry returns registry. */
    @Test
    @Tag("normal")
    @DisplayName("should return DataSourceRegistry")
    void should_return_data_source_registry() {
      // Given
      final var dataSourceProvider = createEmptyDataSourceProvider();

      // When
      final var registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider);

      // Then
      assertNotNull(registry, "registry should not be null");
    }

    /** Verifies that registry has default when DataSource provided. */
    @Test
    @Tag("normal")
    @DisplayName("should register DataSource as default when provided")
    void should_register_data_source_as_default_when_provided() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var dataSourceProvider = createDataSourceProvider(dataSource);

      // When
      final var registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider);

      // Then
      assertTrue(registry.hasDefault(), "should have default DataSource");
    }

    /** Verifies that registry has no default when no DataSource provided. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not have default when no DataSource provided")
    void should_not_have_default_when_no_data_source_provided() {
      // Given
      final var dataSourceProvider = createEmptyDataSourceProvider();

      // When
      final var registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider);

      // Then
      assertFalse(registry.hasDefault(), "should not have default DataSource");
    }
  }

  /** Tests for the dataSourceRegistrar bean. */
  @Nested
  @DisplayName("dataSourceRegistrar(DbTesterProperties) method")
  class DataSourceRegistrarMethod {

    /** Tests for the dataSourceRegistrar method. */
    DataSourceRegistrarMethod() {}

    /** Verifies that dataSourceRegistrar returns registrar. */
    @Test
    @Tag("normal")
    @DisplayName("should return DataSourceRegistrar")
    void should_return_data_source_registrar() {
      // Given & When
      final var registrar = autoConfiguration.dataSourceRegistrar(properties);

      // Then
      assertNotNull(registrar, "registrar should not be null");
    }
  }

  /**
   * Creates an empty ObjectProvider for DataSource using Mockito.
   *
   * @return the mock provider
   */
  @SuppressWarnings("unchecked")
  private ObjectProvider<DataSource> createEmptyDataSourceProvider() {
    final var provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(null);
    when(provider.getIfUnique()).thenReturn(null);
    when(provider.stream()).thenReturn(Stream.empty());
    return provider;
  }

  /**
   * Creates an ObjectProvider with a single DataSource using Mockito.
   *
   * @param dataSource the DataSource to provide
   * @return the mock provider
   */
  @SuppressWarnings("unchecked")
  private ObjectProvider<DataSource> createDataSourceProvider(final DataSource dataSource) {
    final var provider = mock(ObjectProvider.class);
    when(provider.getObject()).thenReturn(dataSource);
    when(provider.getIfAvailable()).thenReturn(dataSource);
    when(provider.getIfUnique()).thenReturn(dataSource);
    when(provider.stream()).thenReturn(Stream.of(dataSource));
    return provider;
  }
}
