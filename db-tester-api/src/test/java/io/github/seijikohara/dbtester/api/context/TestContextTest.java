package io.github.seijikohara.dbtester.api.context;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TestContext}. */
@DisplayName("TestContext")
class TestContextTest {

  /** Tests for the TestContext class. */
  TestContextTest() {}

  /** Tests for the record constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** The test class for fixture setup. */
    private Class<?> testClass;

    /** The test method for fixture setup. */
    private Method testMethod;

    /** The configuration for fixture setup. */
    private Configuration configuration;

    /** The registry for fixture setup. */
    private DataSourceRegistry registry;

    /**
     * Sets up test fixtures.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @BeforeEach
    void setUp() throws NoSuchMethodException {
      testClass = SampleTestClass.class;
      testMethod = SampleTestClass.class.getMethod("sampleTestMethod");
      configuration =
          new Configuration(
              ConventionSettings.standard(), OperationDefaults.standard(), createMockLoader());
      registry = new DataSourceRegistry();
    }

    /** Verifies that constructor stores all components. */
    @Test
    @Tag("normal")
    @DisplayName("should store all components when created")
    void should_store_all_components_when_created() {
      // Given & When
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // Then
      assertAll(
          "should store all components",
          () -> assertSame(testClass, context.testClass(), "testClass should match"),
          () -> assertSame(testMethod, context.testMethod(), "testMethod should match"),
          () -> assertSame(configuration, context.configuration(), "configuration should match"),
          () -> assertSame(registry, context.registry(), "registry should match"));
    }
  }

  /** Tests for the testClass accessor. */
  @Nested
  @DisplayName("testClass() accessor")
  class TestClassAccessor {

    /** Tests for the testClass accessor. */
    TestClassAccessor() {}

    /**
     * Verifies that testClass returns the test class.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return the test class")
    void should_return_the_test_class() throws NoSuchMethodException {
      // Given
      final var expectedClass = SampleTestClass.class;
      final var context =
          new TestContext(
              expectedClass,
              SampleTestClass.class.getMethod("sampleTestMethod"),
              createConfiguration(),
              new DataSourceRegistry());

      // When
      final var result = context.testClass();

      // Then
      assertSame(expectedClass, result, "should return the test class");
    }
  }

  /** Tests for the testMethod accessor. */
  @Nested
  @DisplayName("testMethod() accessor")
  class TestMethodAccessor {

    /** Tests for the testMethod accessor. */
    TestMethodAccessor() {}

    /**
     * Verifies that testMethod returns the test method.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return the test method")
    void should_return_the_test_method() throws NoSuchMethodException {
      // Given
      final var expectedMethod = SampleTestClass.class.getMethod("sampleTestMethod");
      final var context =
          new TestContext(
              SampleTestClass.class,
              expectedMethod,
              createConfiguration(),
              new DataSourceRegistry());

      // When
      final var result = context.testMethod();

      // Then
      assertSame(expectedMethod, result, "should return the test method");
    }

    /**
     * Verifies that testMethod name can be extracted.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should allow extracting method name")
    void should_allow_extracting_method_name() throws NoSuchMethodException {
      // Given
      final var context =
          new TestContext(
              SampleTestClass.class,
              SampleTestClass.class.getMethod("sampleTestMethod"),
              createConfiguration(),
              new DataSourceRegistry());

      // When
      final var methodName = context.testMethod().getName();

      // Then
      assertEquals("sampleTestMethod", methodName, "should extract method name");
    }
  }

  /** Tests for the configuration accessor. */
  @Nested
  @DisplayName("configuration() accessor")
  class ConfigurationAccessor {

    /** Tests for the configuration accessor. */
    ConfigurationAccessor() {}

    /**
     * Verifies that configuration returns the configuration.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return the configuration")
    void should_return_the_configuration() throws NoSuchMethodException {
      // Given
      final var expectedConfig = createConfiguration();
      final var context =
          new TestContext(
              SampleTestClass.class,
              SampleTestClass.class.getMethod("sampleTestMethod"),
              expectedConfig,
              new DataSourceRegistry());

      // When
      final var result = context.configuration();

      // Then
      assertSame(expectedConfig, result, "should return the configuration");
    }

    /**
     * Verifies that configuration components are accessible.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should allow accessing configuration components")
    void should_allow_accessing_configuration_components() throws NoSuchMethodException {
      // Given
      final var context =
          new TestContext(
              SampleTestClass.class,
              SampleTestClass.class.getMethod("sampleTestMethod"),
              createConfiguration(),
              new DataSourceRegistry());

      // When
      final var conventions = context.configuration().conventions();
      final var operations = context.configuration().operations();

      // Then
      assertAll(
          "should access configuration components",
          () ->
              assertEquals("/expected", conventions.expectationSuffix(), "conventions accessible"),
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT, operations.preparation(), "operations accessible"));
    }
  }

  /** Tests for the registry accessor. */
  @Nested
  @DisplayName("registry() accessor")
  class RegistryAccessor {

    /** Tests for the registry accessor. */
    RegistryAccessor() {}

    /**
     * Verifies that registry returns the registry.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return the registry")
    void should_return_the_registry() throws NoSuchMethodException {
      // Given
      final var expectedRegistry = new DataSourceRegistry();
      final var context =
          new TestContext(
              SampleTestClass.class,
              SampleTestClass.class.getMethod("sampleTestMethod"),
              createConfiguration(),
              expectedRegistry);

      // When
      final var result = context.registry();

      // Then
      assertSame(expectedRegistry, result, "should return the registry");
    }
  }

  /** Tests for record equality. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /**
     * Verifies that contexts with same components are equal.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when all components are the same")
    void should_be_equal_when_all_components_are_the_same() throws NoSuchMethodException {
      // Given
      final var testClass = SampleTestClass.class;
      final var testMethod = SampleTestClass.class.getMethod("sampleTestMethod");
      final var configuration = createConfiguration();
      final var registry = new DataSourceRegistry();

      final var context1 = new TestContext(testClass, testMethod, configuration, registry);
      final var context2 = new TestContext(testClass, testMethod, configuration, registry);

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(context1, context2, "should be equal"),
          () -> assertEquals(context1.hashCode(), context2.hashCode(), "hashCodes should match"));
    }
  }

  /**
   * Creates a mock DataSetLoader for testing.
   *
   * @return a mock DataSetLoader
   */
  private static DataSetLoader createMockLoader() {
    return new DataSetLoader() {
      @Override
      public java.util.List<io.github.seijikohara.dbtester.api.dataset.TableSet>
          loadPreparationDataSets(final TestContext context) {
        return java.util.List.of();
      }

      @Override
      public java.util.List<io.github.seijikohara.dbtester.api.dataset.TableSet>
          loadExpectationDataSets(final TestContext context) {
        return java.util.List.of();
      }
    };
  }

  /**
   * Creates a Configuration for testing.
   *
   * @return a Configuration instance
   */
  private static Configuration createConfiguration() {
    return new Configuration(
        ConventionSettings.standard(), OperationDefaults.standard(), createMockLoader());
  }

  /** Sample test class used for testing. */
  public static class SampleTestClass {

    /** Creates a SampleTestClass for testing. */
    public SampleTestClass() {}

    /** Sample test method. */
    public void sampleTestMethod() {
      // Empty test method for reflection
    }
  }
}
