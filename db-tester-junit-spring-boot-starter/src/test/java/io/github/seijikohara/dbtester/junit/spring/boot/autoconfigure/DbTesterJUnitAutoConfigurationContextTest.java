package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Auto-configuration context tests for {@link DbTesterJUnitAutoConfiguration}.
 *
 * <p>These tests use {@link ApplicationContextRunner} to verify conditional auto-configuration
 * behavior, including property-based activation, bean registration, and custom bean overrides.
 */
@DisplayName("DbTesterJUnitAutoConfiguration (context)")
class DbTesterJUnitAutoConfigurationContextTest {

  /** Tests for the auto-configuration context behavior. */
  DbTesterJUnitAutoConfigurationContextTest() {}

  /** Base context runner with the auto-configuration registered. */
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(DbTesterJUnitAutoConfiguration.class));

  /** Tests for bean registration when auto-configuration is enabled. */
  @Nested
  @DisplayName("bean registration")
  class BeanRegistration {

    /** Tests for the bean registration behavior. */
    BeanRegistration() {}

    /** Verifies that all beans are registered with default properties. */
    @Test
    @Tag("normal")
    @DisplayName("should register all beans with default properties")
    void shouldRegisterAllBeans_whenDefaultProperties() {
      contextRunner.run(
          context ->
              assertAll(
                  "should register all expected beans",
                  () ->
                      assertTrue(
                          context.containsBean("dbTesterConfiguration"),
                          "should have dbTesterConfiguration bean"),
                  () ->
                      assertTrue(
                          context.containsBean("dbTesterDataSourceRegistry"),
                          "should have dbTesterDataSourceRegistry bean"),
                  () ->
                      assertTrue(
                          context.containsBean("dataSourceRegistrar"),
                          "should have dataSourceRegistrar bean")));
    }

    /** Verifies that Configuration bean has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should register Configuration bean with correct type")
    void shouldRegisterConfigurationBean_withCorrectType() {
      contextRunner.run(
          context ->
              assertTrue(
                  context.getBean("dbTesterConfiguration") instanceof Configuration,
                  "dbTesterConfiguration should be Configuration instance"));
    }

    /** Verifies that DataSourceRegistry bean has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should register DataSourceRegistry bean with correct type")
    void shouldRegisterDataSourceRegistryBean_withCorrectType() {
      contextRunner.run(
          context ->
              assertTrue(
                  context.getBean("dbTesterDataSourceRegistry") instanceof DataSourceRegistry,
                  "dbTesterDataSourceRegistry should be DataSourceRegistry instance"));
    }

    /** Verifies that DataSourceRegistrar bean has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should register DataSourceRegistrar bean with correct type")
    void shouldRegisterDataSourceRegistrarBean_withCorrectType() {
      contextRunner.run(
          context ->
              assertTrue(
                  context.getBean("dataSourceRegistrar") instanceof DataSourceRegistrar,
                  "dataSourceRegistrar should be DataSourceRegistrar instance"));
    }
  }

  /** Tests for the db-tester.enabled property. */
  @Nested
  @DisplayName("db-tester.enabled property")
  class EnabledProperty {

    /** Tests for the enabled property behavior. */
    EnabledProperty() {}

    /** Verifies that auto-configuration is disabled when enabled is false. */
    @Test
    @Tag("normal")
    @DisplayName("should not register beans when disabled")
    void shouldNotRegisterBeans_whenDisabled() {
      contextRunner
          .withPropertyValues("db-tester.enabled=false")
          .run(
              context ->
                  assertAll(
                      "should not register any beans",
                      () ->
                          assertFalse(
                              context.containsBean("dbTesterConfiguration"),
                              "should not have dbTesterConfiguration bean"),
                      () ->
                          assertFalse(
                              context.containsBean("dbTesterDataSourceRegistry"),
                              "should not have dbTesterDataSourceRegistry bean"),
                      () ->
                          assertFalse(
                              context.containsBean("dataSourceRegistrar"),
                              "should not have dataSourceRegistrar bean")));
    }

    /** Verifies that auto-configuration is enabled when enabled is true. */
    @Test
    @Tag("normal")
    @DisplayName("should register beans when explicitly enabled")
    void shouldRegisterBeans_whenExplicitlyEnabled() {
      contextRunner
          .withPropertyValues("db-tester.enabled=true")
          .run(
              context ->
                  assertTrue(
                      context.containsBean("dbTesterConfiguration"),
                      "should have dbTesterConfiguration bean"));
    }

    /** Verifies that auto-configuration is enabled by default (matchIfMissing=true). */
    @Test
    @Tag("normal")
    @DisplayName("should register beans when property is not set (default enabled)")
    void shouldRegisterBeans_whenPropertyNotSet() {
      contextRunner.run(
          context ->
              assertTrue(
                  context.containsBean("dbTesterConfiguration"),
                  "should have dbTesterConfiguration bean by default"));
    }
  }

  /** Tests for the @ConditionalOnMissingBean behavior. */
  @Nested
  @DisplayName("@ConditionalOnMissingBean behavior")
  class ConditionalOnMissingBean {

    /** Tests for the conditional bean registration behavior. */
    ConditionalOnMissingBean() {}

    /** Verifies that custom Configuration bean takes precedence. */
    @Test
    @Tag("normal")
    @DisplayName("should use custom Configuration when provided")
    void shouldUseCustomConfiguration_whenProvided() {
      final var customConfig = Configuration.defaults();
      contextRunner
          .withBean("dbTesterConfiguration", Configuration.class, () -> customConfig)
          .run(
              context ->
                  assertAll(
                      "should use custom Configuration",
                      () ->
                          assertTrue(
                              context.containsBean("dbTesterConfiguration"),
                              "should have dbTesterConfiguration bean"),
                      () ->
                          assertTrue(
                              context.getBean("dbTesterConfiguration") == customConfig,
                              "should be the custom Configuration instance")));
    }

    /** Verifies that custom DataSourceRegistry bean takes precedence. */
    @Test
    @Tag("normal")
    @DisplayName("should use custom DataSourceRegistry when provided")
    void shouldUseCustomDataSourceRegistry_whenProvided() {
      final var customRegistry = new DataSourceRegistry();
      contextRunner
          .withBean("dbTesterDataSourceRegistry", DataSourceRegistry.class, () -> customRegistry)
          .run(
              context ->
                  assertTrue(
                      context.getBean("dbTesterDataSourceRegistry") == customRegistry,
                      "should be the custom DataSourceRegistry instance"));
    }
  }

  /** Tests for property binding. */
  @Nested
  @DisplayName("property binding")
  class PropertyBinding {

    /** Tests for property binding behavior. */
    PropertyBinding() {}

    /** Verifies that convention properties are bound correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should bind convention properties")
    void shouldBindConventionProperties() {
      contextRunner
          .withPropertyValues(
              "db-tester.convention.expectation-suffix=/verify",
              "db-tester.convention.scenario-marker=[Test]")
          .run(
              context -> {
                final var config = context.getBean("dbTesterConfiguration", Configuration.class);
                assertAll(
                    "convention properties should be bound",
                    () ->
                        assertTrue(
                            config.conventions().expectationSuffix().equals("/verify"),
                            "expectation suffix should be /verify"),
                    () ->
                        assertTrue(
                            config.conventions().scenarioMarker().equals("[Test]"),
                            "scenario marker should be [Test]"));
              });
    }

    /** Verifies that auto-register-data-sources property is bound correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should bind auto-register-data-sources property")
    void shouldBindAutoRegisterProperty() {
      contextRunner
          .withPropertyValues("db-tester.auto-register-data-sources=false")
          .run(
              context -> {
                final var properties = context.getBean(DbTesterProperties.class);
                assertFalse(
                    properties.isAutoRegisterDataSources(),
                    "auto-register-data-sources should be false");
              });
    }
  }
}
