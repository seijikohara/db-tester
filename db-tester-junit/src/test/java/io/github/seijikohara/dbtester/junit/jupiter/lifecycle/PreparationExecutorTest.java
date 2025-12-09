package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PreparationExecutor}. */
@DisplayName("PreparationExecutor")
class PreparationExecutorTest {

  /** Tests for the PreparationExecutor class. */
  PreparationExecutorTest() {}

  /** The executor instance under test. */
  private PreparationExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    executor = new PreparationExecutor();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new PreparationExecutor();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute(TestContext, Preparation) method. */
  @Nested
  @DisplayName("execute(TestContext, Preparation) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /**
     * Verifies that execute completes without error when no datasets found.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should complete without error when no datasets found")
    void shouldCompleteWithoutError_whenNoDatasetsFound() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(org.mockito.ArgumentMatchers.any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var preparation = testMethod.getAnnotation(Preparation.class);

      // When & Then
      assertDoesNotThrow(
          () -> executor.execute(context, preparation),
          "should complete without error when no datasets found");
    }
  }

  /** Test class with Preparation annotation. */
  static class TestClass {

    /** Test constructor. */
    TestClass() {}

    /** Test method with Preparation annotation. */
    @Preparation
    void testMethod() {}
  }
}
