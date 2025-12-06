package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DbTesterProperties}. */
@DisplayName("DbTesterProperties")
class DbTesterPropertiesTest {

  /** Tests for the DbTesterProperties class. */
  DbTesterPropertiesTest() {}

  /** The properties instance under test. */
  private DbTesterProperties properties;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    properties = new DbTesterProperties();
  }

  /** Tests for default values. */
  @Nested
  @DisplayName("default values")
  class DefaultValues {

    /** Tests for default values. */
    DefaultValues() {}

    /** Verifies that default values are set correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given - new instance

      // When & Then
      assertAll(
          "default values should be correct",
          () -> assertTrue(properties.isEnabled(), "enabled should default to true"),
          () ->
              assertTrue(
                  properties.isAutoRegisterDataSources(),
                  "autoRegisterDataSources should default to true"));
    }
  }

  /** Tests for the enabled property. */
  @Nested
  @DisplayName("enabled property")
  class EnabledProperty {

    /** Tests for the enabled property. */
    EnabledProperty() {}

    /** Verifies that enabled can be set to false. */
    @Test
    @Tag("normal")
    @DisplayName("should allow setting enabled to false")
    void should_allow_setting_enabled_to_false() {
      // Given
      assertTrue(properties.isEnabled(), "should start as true");

      // When
      properties.setEnabled(false);

      // Then
      assertFalse(properties.isEnabled(), "should be false after setting");
    }

    /** Verifies that enabled can be toggled. */
    @Test
    @Tag("normal")
    @DisplayName("should allow toggling enabled")
    void should_allow_toggling_enabled() {
      // Given
      properties.setEnabled(false);

      // When
      properties.setEnabled(true);

      // Then
      assertTrue(properties.isEnabled(), "should be true after toggling");
    }
  }

  /** Tests for the autoRegisterDataSources property. */
  @Nested
  @DisplayName("autoRegisterDataSources property")
  class AutoRegisterDataSourcesProperty {

    /** Tests for the autoRegisterDataSources property. */
    AutoRegisterDataSourcesProperty() {}

    /** Verifies that autoRegisterDataSources can be set to false. */
    @Test
    @Tag("normal")
    @DisplayName("should allow setting autoRegisterDataSources to false")
    void should_allow_setting_auto_register_to_false() {
      // Given
      assertTrue(properties.isAutoRegisterDataSources(), "should start as true");

      // When
      properties.setAutoRegisterDataSources(false);

      // Then
      assertFalse(properties.isAutoRegisterDataSources(), "should be false after setting");
    }

    /** Verifies that autoRegisterDataSources can be toggled. */
    @Test
    @Tag("normal")
    @DisplayName("should allow toggling autoRegisterDataSources")
    void should_allow_toggling_auto_register() {
      // Given
      properties.setAutoRegisterDataSources(false);

      // When
      properties.setAutoRegisterDataSources(true);

      // Then
      assertTrue(properties.isAutoRegisterDataSources(), "should be true after toggling");
    }
  }

  /** Tests for property independence. */
  @Nested
  @DisplayName("property independence")
  class PropertyIndependence {

    /** Tests for property independence. */
    PropertyIndependence() {}

    /** Verifies that properties are independent. */
    @Test
    @Tag("normal")
    @DisplayName("should maintain independent property values")
    void should_maintain_independent_property_values() {
      // Given & When
      properties.setEnabled(false);
      properties.setAutoRegisterDataSources(true);

      // Then
      assertAll(
          "properties should be independent",
          () -> assertFalse(properties.isEnabled(), "enabled should be false"),
          () ->
              assertTrue(
                  properties.isAutoRegisterDataSources(),
                  "autoRegisterDataSources should be true"));
    }
  }
}
