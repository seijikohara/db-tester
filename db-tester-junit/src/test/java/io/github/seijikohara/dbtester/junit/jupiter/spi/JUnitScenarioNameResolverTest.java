package io.github.seijikohara.dbtester.junit.jupiter.spi;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JUnitScenarioNameResolver}. */
@DisplayName("JUnitScenarioNameResolver")
class JUnitScenarioNameResolverTest {

  /** Tests for the JUnitScenarioNameResolver class. */
  JUnitScenarioNameResolverTest() {}

  /** The resolver under test. */
  private JUnitScenarioNameResolver resolver;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    resolver = new JUnitScenarioNameResolver();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates a valid instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance")
    void should_create_instance() {
      // Given & When
      final var instance = new JUnitScenarioNameResolver();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the resolve method. */
  @Nested
  @DisplayName("resolve(Method) method")
  class ResolveMethod {

    /** Tests for the resolve method. */
    ResolveMethod() {}

    /**
     * Verifies that resolve returns method name as scenario name.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return method name as scenario name")
    void should_return_method_name_as_scenario_name() throws NoSuchMethodException {
      // Given
      final var method = SampleTestClass.class.getMethod("shouldCreateUser");

      // When
      final var result = resolver.resolve(method);

      // Then
      assertEquals("shouldCreateUser", result.value(), "should return method name");
    }

    /**
     * Verifies that resolve handles various method naming patterns.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle various method naming patterns")
    void should_handle_various_method_naming_patterns() throws NoSuchMethodException {
      // Given
      final var camelCase = SampleTestClass.class.getMethod("shouldCreateUser");
      final var snakeCase = SampleTestClass.class.getMethod("should_create_user");
      final var withNumbers = SampleTestClass.class.getMethod("testCase123");

      // When & Then
      assertAll(
          "should handle various patterns",
          () ->
              assertEquals(
                  "shouldCreateUser", resolver.resolve(camelCase).value(), "camelCase method"),
          () ->
              assertEquals(
                  "should_create_user", resolver.resolve(snakeCase).value(), "snake_case method"),
          () ->
              assertEquals(
                  "testCase123", resolver.resolve(withNumbers).value(), "method with numbers"));
    }

    /**
     * Verifies that resolve returns non-null ScenarioName.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return non-null ScenarioName")
    void should_return_non_null_scenario_name() throws NoSuchMethodException {
      // Given
      final var method = SampleTestClass.class.getMethod("shouldCreateUser");

      // When
      final var result = resolver.resolve(method);

      // Then
      assertNotNull(result, "ScenarioName should not be null");
    }
  }

  /** Tests for the canResolve method. */
  @Nested
  @DisplayName("canResolve(Method) method")
  class CanResolveMethod {

    /** Tests for the canResolve method. */
    CanResolveMethod() {}

    /**
     * Verifies that canResolve returns true for any method.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return true for any method")
    void should_return_true_for_any_method() throws NoSuchMethodException {
      // Given
      final var method = SampleTestClass.class.getMethod("shouldCreateUser");

      // When
      final var result = resolver.canResolve(method);

      // Then
      assertTrue(result, "should return true for any method");
    }

    /**
     * Verifies that canResolve returns true regardless of method characteristics.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return true for methods without @Test annotation")
    void should_return_true_for_methods_without_test_annotation() throws NoSuchMethodException {
      // Given
      final var helperMethod = SampleTestClass.class.getMethod("helperMethod");

      // When
      final var result = resolver.canResolve(helperMethod);

      // Then
      assertTrue(result, "should return true for helper methods");
    }

    /**
     * Verifies that canResolve returns true for static methods.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return true for static methods")
    void should_return_true_for_static_methods() throws NoSuchMethodException {
      // Given
      final var staticMethod = SampleTestClass.class.getMethod("staticMethod");

      // When
      final var result = resolver.canResolve(staticMethod);

      // Then
      assertTrue(result, "should return true for static methods");
    }
  }

  /** Tests for the priority method. */
  @Nested
  @DisplayName("priority() method")
  class PriorityMethod {

    /** Tests for the priority method. */
    PriorityMethod() {}

    /** Verifies that priority returns DEFAULT_PRIORITY. */
    @Test
    @Tag("normal")
    @DisplayName("should return DEFAULT_PRIORITY")
    void should_return_default_priority() {
      // Given & When
      final var result = resolver.priority();

      // Then
      assertEquals(
          ScenarioNameResolver.DEFAULT_PRIORITY, result, "should return DEFAULT_PRIORITY (0)");
    }

    /** Verifies that priority is consistent. */
    @Test
    @Tag("normal")
    @DisplayName("should return consistent priority")
    void should_return_consistent_priority() {
      // Given & When
      final var priority1 = resolver.priority();
      final var priority2 = resolver.priority();

      // Then
      assertEquals(priority1, priority2, "priority should be consistent");
    }
  }

  /** Tests for the ScenarioNameResolver interface contract. */
  @Nested
  @DisplayName("ScenarioNameResolver interface contract")
  class InterfaceContract {

    /** Tests for interface contract. */
    InterfaceContract() {}

    /** Verifies that resolver implements ScenarioNameResolver. */
    @Test
    @Tag("normal")
    @DisplayName("should implement ScenarioNameResolver")
    void should_implement_scenario_name_resolver() {
      // Given & When & Then
      assertTrue(
          ScenarioNameResolver.class.isAssignableFrom(resolver.getClass()),
          "should implement ScenarioNameResolver");
    }

    /**
     * Verifies that resolver acts as fallback with default priority.
     *
     * @throws NoSuchMethodException if the method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should act as fallback resolver with default priority")
    void should_act_as_fallback_resolver() throws NoSuchMethodException {
      // Given
      final var method = SampleTestClass.class.getMethod("shouldCreateUser");

      // When
      final var canResolve = resolver.canResolve(method);
      final var priority = resolver.priority();

      // Then
      assertAll(
          "should be fallback resolver",
          () -> assertTrue(canResolve, "should handle any method"),
          () -> assertEquals(0, priority, "should have default priority"));
    }
  }

  /** Sample test class for reflection-based testing. */
  public static class SampleTestClass {

    /** Creates a SampleTestClass for testing. */
    public SampleTestClass() {}

    /** Sample test method with camelCase naming. */
    public void shouldCreateUser() {
      // Empty method for reflection testing
    }

    /** Sample test method with snake_case naming. */
    public void should_create_user() {
      // Empty method for reflection testing
    }

    /** Sample test method with numbers in name. */
    public void testCase123() {
      // Empty method for reflection testing
    }

    /** Sample helper method without @Test annotation. */
    public void helperMethod() {
      // Empty method for reflection testing
    }

    /** Sample static method. */
    public static void staticMethod() {
      // Empty method for reflection testing
    }
  }
}
