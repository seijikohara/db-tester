package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.spi.PreparationSupport;
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

  /** Mock preparation support for tests. */
  private PreparationSupport mockSupport;

  /** The executor instance under test. */
  private PreparationExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockSupport = mock(PreparationSupport.class);
    executor = new PreparationExecutor(mockSupport);
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
      final var instance = new PreparationExecutor(mockSupport);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute(TestContext, DataSet) method. */
  @Nested
  @DisplayName("execute(TestContext, DataSet) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /**
     * Verifies that execute delegates to support.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to preparation support")
    void shouldDelegateToSupport_whenCalled() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var dataSet = testMethod.getAnnotation(DataSet.class);

      // When
      executor.execute(context, dataSet);

      // Then
      verify(mockSupport).execute(context, dataSet);
    }

    /**
     * Verifies that execute completes without error.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should complete without error when called")
    void shouldCompleteWithoutError_whenCalled() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var dataSet = testMethod.getAnnotation(DataSet.class);

      // When & Then
      assertDoesNotThrow(
          () -> executor.execute(context, dataSet), "should complete without error when called");
    }
  }

  /** Test class with DataSet annotation. */
  static class TestClass {

    /** Test constructor. */
    TestClass() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void testMethod() {}
  }
}
