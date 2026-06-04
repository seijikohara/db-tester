package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.spring.support.DbTesterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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

    /** Verifies that dbTesterConfiguration delegates to the factory. */
    @Test
    @Tag("normal")
    @DisplayName("should build Configuration from properties")
    void should_build_configuration_from_properties() {
      // Given
      properties.getOperation().setPreparation(Operation.INSERT);

      // When
      final var config = autoConfiguration.dbTesterConfiguration(properties);

      // Then
      assertNotNull(config, "configuration should not be null");
      assertEquals(
          Operation.INSERT,
          config.operations().preparation(),
          "preparation should be mapped from properties");
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
}
