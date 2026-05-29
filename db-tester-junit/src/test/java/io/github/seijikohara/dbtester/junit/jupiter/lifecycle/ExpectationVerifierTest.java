package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpectationVerifier}. */
@DisplayName("ExpectationVerifier")
class ExpectationVerifierTest {

  /** Tests for the ExpectationVerifier class. */
  ExpectationVerifierTest() {}

  /** Mock expectation support for tests. */
  private ExpectationSupport mockSupport;

  /** The verifier instance under test. */
  private ExpectationVerifier verifier;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockSupport = mock(ExpectationSupport.class);
    verifier = new ExpectationVerifier(mockSupport);
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
      final var instance = new ExpectationVerifier(mockSupport);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the verify(TestContext, ExpectedDataSet) method. */
  @Nested
  @DisplayName("verify(TestContext, ExpectedDataSet) method")
  class VerifyMethod {

    /** Tests for the verify method. */
    VerifyMethod() {}

    /**
     * Verifies that verify delegates to expectation support.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to expectation support")
    void shouldDelegateToSupport_whenCalled() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When
      verifier.verify(context, expectedDataSet);

      // Then
      verify(mockSupport).verify(context, expectedDataSet);
    }

    /**
     * Verifies that verify completes without error when no datasets found.
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
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When & Then
      assertDoesNotThrow(
          () -> verifier.verify(context, expectedDataSet),
          "should complete without error when no datasets found");
    }

    /**
     * Verifies that verify propagates validation exception from support.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("error")
    @DisplayName("should propagate validation exception from support")
    void shouldPropagateValidationException_fromSupport() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      doThrow(new ValidationException("Verification failed"))
          .when(mockSupport)
          .verify(any(TestContext.class), any(ExpectedDataSet.class));

      // When & Then
      assertThrows(
          ValidationException.class,
          () -> verifier.verify(context, expectedDataSet),
          "should propagate validation exception from support");
    }
  }

  /** Test class with ExpectedDataSet annotation. */
  static class TestClass {

    /** Test constructor. */
    TestClass() {}

    /** Test method with ExpectedDataSet annotation. */
    @ExpectedDataSet
    void testMethod() {}
  }

  /** Test class with retry settings in annotation. */
  static class TestClassWithRetrySettings {

    /** Test constructor. */
    TestClassWithRetrySettings() {}

    /** Test method with retry settings. */
    @ExpectedDataSet(retryCount = 2, retryDelayMillis = 50)
    void testMethod() {}
  }
}
