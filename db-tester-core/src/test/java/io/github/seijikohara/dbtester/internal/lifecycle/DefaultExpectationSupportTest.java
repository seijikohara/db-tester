package io.github.seijikohara.dbtester.internal.lifecycle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.config.VerificationSettings;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Unit tests for {@link DefaultExpectationSupport}. */
@DisplayName("DefaultExpectationSupport")
class DefaultExpectationSupportTest {

  /** Tests for the DefaultExpectationSupport class. */
  DefaultExpectationSupportTest() {}

  /** The instance under test. */
  private DefaultExpectationSupport support;

  /** Mock expectation provider. */
  private ExpectationProvider expectationProvider;

  /** Mock test context. */
  private TestContext context;

  /** Mock expected dataset annotation. */
  private ExpectedDataSet expectedDataSet;

  /** Mock configuration. */
  private Configuration configuration;

  /** Mock data set loader. */
  private DataSetLoader loader;

  /** Mock verification settings. */
  private VerificationSettings verification;

  /** Mock operation defaults. */
  private OperationDefaults operationDefaults;

  /** Mock data source registry. */
  private DataSourceRegistry registry;

  /**
   * Sets up test fixtures before each test.
   *
   * @throws NoSuchMethodException if the test method cannot be found via reflection
   */
  @BeforeEach
  void setUp() throws NoSuchMethodException {
    expectationProvider = mock(ExpectationProvider.class);
    support = new DefaultExpectationSupport(expectationProvider);

    loader = mock(DataSetLoader.class);
    verification = mock(VerificationSettings.class);
    operationDefaults = mock(OperationDefaults.class);
    registry = mock(DataSourceRegistry.class);

    configuration = mock(Configuration.class);
    when(configuration.loader()).thenReturn(loader);
    when(configuration.verification()).thenReturn(verification);
    when(configuration.operations()).thenReturn(operationDefaults);

    final Method testMethod = DefaultExpectationSupportTest.class.getDeclaredMethod("setUp");
    context =
        new TestContext(DefaultExpectationSupportTest.class, testMethod, configuration, registry);

    expectedDataSet = mock(ExpectedDataSet.class);
    when(expectedDataSet.rowOrdering()).thenReturn(RowOrdering.UNSET);
    when(expectedDataSet.tableOrdering()).thenReturn(TableOrderingStrategy.AUTO);
    when(expectedDataSet.retryCount()).thenReturn(-1);
    when(expectedDataSet.retryDelayMillis()).thenReturn(-1L);

    when(verification.retryCount()).thenReturn(0);
    when(verification.retryDelay()).thenReturn(Duration.ZERO);
    when(verification.rowOrdering()).thenReturn(RowOrdering.ORDERED);
  }

  /** Tests for the verify method with retry logic. */
  @Nested
  @DisplayName("verify method - retry logic")
  class VerifyRetryLogic {

    /** Tests for verify retry logic. */
    VerifyRetryLogic() {}

    /** Verifies that verification succeeds without retry when first attempt passes. */
    @Test
    @Tag("normal")
    @DisplayName("should succeed on first attempt when verification passes")
    void shouldSucceedOnFirstAttempt_whenVerificationPasses() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      assertDoesNotThrow(
          () -> support.verify(context, expectedDataSet),
          "should not throw when verification succeeds");

      verify(expectationProvider, times(1))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that verification throws on first failure when retryCount is 0. */
    @Test
    @Tag("normal")
    @DisplayName("should throw immediately when retryCount is zero and verification fails")
    void shouldThrowImmediately_whenRetryCountIsZeroAndVerificationFails() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(verification.retryCount()).thenReturn(0);

      doThrow(new ValidationException("Mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      final var exception =
          assertThrows(
              ValidationException.class,
              () -> support.verify(context, expectedDataSet),
              "should throw ValidationException when verification fails");

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("Failed to verify"),
          "exception message should indicate verification failure");

      verify(expectationProvider, times(1))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that verification succeeds on a retry attempt. */
    @Test
    @Tag("normal")
    @DisplayName("should succeed on retry when later attempt passes")
    void shouldSucceedOnRetry_whenLaterAttemptPasses() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.retryCount()).thenReturn(2);
      when(expectedDataSet.retryDelayMillis()).thenReturn(0L);

      // First call fails, second call succeeds
      doThrow(new ValidationException("Mismatch"))
          .doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      assertDoesNotThrow(
          () -> support.verify(context, expectedDataSet), "should not throw when retry succeeds");

      verify(expectationProvider, times(2))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that all retries are exhausted before throwing. */
    @Test
    @Tag("error")
    @DisplayName("should throw after all retries are exhausted")
    void shouldThrow_whenAllRetriesExhausted() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.retryCount()).thenReturn(2);
      when(expectedDataSet.retryDelayMillis()).thenReturn(0L);

      doThrow(new ValidationException("Persistent mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      final var exception =
          assertThrows(
              ValidationException.class,
              () -> support.verify(context, expectedDataSet),
              "should throw after all retries exhausted");

      final var message = exception.getMessage();
      assertAll(
          "retry exhaustion behavior",
          () ->
              assertTrue(
                  message != null && message.contains("Failed to verify"),
                  "exception message should indicate verification failure"),
          () ->
              verify(expectationProvider, times(3))
                  .verifyExpectation(any(), any(), any(ExpectationContext.class)));
    }

    /** Verifies that AssertionError is retried when retryCount is positive. */
    @Test
    @Tag("normal")
    @DisplayName("should retry on AssertionError when retryCount is positive")
    void shouldRetryOnAssertionError_whenRetryCountIsPositive() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.retryCount()).thenReturn(2);
      when(expectedDataSet.retryDelayMillis()).thenReturn(0L);

      // First call throws AssertionError, second call succeeds
      doThrow(new AssertionError("Data mismatch"))
          .doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      assertDoesNotThrow(
          () -> support.verify(context, expectedDataSet),
          "should not throw when retry succeeds after AssertionError");

      verify(expectationProvider, times(2))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that AssertionError exhausting retries throws ValidationException with cause. */
    @Test
    @Tag("error")
    @DisplayName("should throw ValidationException when AssertionError exhausts retries")
    void shouldThrowValidationException_whenAssertionErrorExhaustsRetries() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.retryCount()).thenReturn(1);
      when(expectedDataSet.retryDelayMillis()).thenReturn(0L);

      doThrow(new AssertionError("Persistent data mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      final var exception =
          assertThrows(
              ValidationException.class,
              () -> support.verify(context, expectedDataSet),
              "should throw ValidationException wrapping AssertionError");

      assertAll(
          "AssertionError retry exhaustion",
          () ->
              assertTrue(
                  exception.getMessage() != null
                      && exception.getMessage().contains("Failed to verify"),
                  "exception message should indicate verification failure"),
          () ->
              assertInstanceOf(
                  AssertionError.class, exception.getCause(), "cause should be AssertionError"),
          () ->
              verify(expectationProvider, times(2))
                  .verifyExpectation(any(), any(), any(ExpectationContext.class)));
    }

    /** Verifies that AssertionError with zero retries throws immediately as ValidationException. */
    @Test
    @Tag("error")
    @DisplayName("should throw immediately when retryCount is zero and AssertionError thrown")
    void shouldThrowImmediately_whenRetryCountIsZeroAndAssertionErrorThrown() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(verification.retryCount()).thenReturn(0);

      doThrow(new AssertionError("Data mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      final var exception =
          assertThrows(
              ValidationException.class,
              () -> support.verify(context, expectedDataSet),
              "should throw ValidationException wrapping AssertionError");

      assertAll(
          "immediate AssertionError wrapping",
          () ->
              assertInstanceOf(
                  AssertionError.class, exception.getCause(), "cause should be AssertionError"),
          () ->
              verify(expectationProvider, times(1))
                  .verifyExpectation(any(), any(), any(ExpectationContext.class)));
    }

    /** Verifies that no datasets results in early return without verification. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return early when no expectation datasets found")
    void shouldReturnEarly_whenNoExpectationDatasetsFound() {
      // Given
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(List.of());

      // When & Then
      assertDoesNotThrow(
          () -> support.verify(context, expectedDataSet),
          "should not throw when no datasets found");

      verify(expectationProvider, times(0))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that annotation retryCount overrides global setting. */
    @Test
    @Tag("normal")
    @DisplayName("should use annotation retryCount when non-negative")
    void shouldUseAnnotationRetryCount_whenNonNegative() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // Annotation sets retryCount=1, global sets retryCount=5
      when(expectedDataSet.retryCount()).thenReturn(1);
      when(expectedDataSet.retryDelayMillis()).thenReturn(0L);
      when(verification.retryCount()).thenReturn(5);

      doThrow(new ValidationException("Mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      assertThrows(
          ValidationException.class,
          () -> support.verify(context, expectedDataSet),
          "should throw after annotation retryCount is exhausted");

      // Annotation retryCount=1 means 2 total attempts (initial + 1 retry)
      verify(expectationProvider, times(2))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }

    /** Verifies that global retryCount is used when annotation value is -1. */
    @Test
    @Tag("normal")
    @DisplayName("should use global retryCount when annotation value is -1")
    void shouldUseGlobalRetryCount_whenAnnotationValueIsNegative() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // Annotation returns -1 (use global), global sets retryCount=1
      when(expectedDataSet.retryCount()).thenReturn(-1);
      when(expectedDataSet.retryDelayMillis()).thenReturn(-1L);
      when(verification.retryCount()).thenReturn(1);
      when(verification.retryDelay()).thenReturn(Duration.ZERO);

      doThrow(new ValidationException("Mismatch"))
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When & Then
      assertThrows(
          ValidationException.class,
          () -> support.verify(context, expectedDataSet),
          "should throw after global retryCount is exhausted");

      // Global retryCount=1 means 2 total attempts (initial + 1 retry)
      verify(expectationProvider, times(2))
          .verifyExpectation(any(), any(), any(ExpectationContext.class));
    }
  }

  /** Tests for rowOrdering resolution logic. */
  @Nested
  @DisplayName("verify method - rowOrdering resolution")
  class VerifyRowOrderingResolution {

    /** Tests for rowOrdering resolution. */
    VerifyRowOrderingResolution() {}

    /** Verifies that global rowOrdering is used when annotation value is UNSET. */
    @Test
    @Tag("normal")
    @DisplayName("should use global rowOrdering when annotation value is UNSET")
    void shouldUseGlobalRowOrdering_whenAnnotationValueIsUnset() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // Annotation returns UNSET, global sets UNORDERED
      when(expectedDataSet.rowOrdering()).thenReturn(RowOrdering.UNSET);
      when(verification.rowOrdering()).thenReturn(RowOrdering.UNORDERED);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          RowOrdering.UNORDERED,
          captor.getValue().rowOrdering(),
          "should use global UNORDERED when annotation is UNSET");
    }

    /** Verifies that annotation rowOrdering overrides global setting when explicitly set. */
    @Test
    @Tag("normal")
    @DisplayName("should use annotation rowOrdering when explicitly set to ORDERED")
    void shouldUseAnnotationRowOrdering_whenExplicitlySetToOrdered() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // Annotation explicitly sets ORDERED, global sets UNORDERED
      when(expectedDataSet.rowOrdering()).thenReturn(RowOrdering.ORDERED);
      when(verification.rowOrdering()).thenReturn(RowOrdering.UNORDERED);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          RowOrdering.ORDERED,
          captor.getValue().rowOrdering(),
          "should use annotation ORDERED overriding global UNORDERED");
    }

    /** Verifies that annotation rowOrdering overrides global setting when set to UNORDERED. */
    @Test
    @Tag("normal")
    @DisplayName("should use annotation rowOrdering when explicitly set to UNORDERED")
    void shouldUseAnnotationRowOrdering_whenExplicitlySetToUnordered() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // Annotation explicitly sets UNORDERED, global sets ORDERED
      when(expectedDataSet.rowOrdering()).thenReturn(RowOrdering.UNORDERED);
      when(verification.rowOrdering()).thenReturn(RowOrdering.ORDERED);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          RowOrdering.UNORDERED,
          captor.getValue().rowOrdering(),
          "should use annotation UNORDERED overriding global ORDERED");
    }
  }

  /** Tests for tableOrdering pass-through. */
  @Nested
  @DisplayName("verify method - tableOrdering")
  class VerifyTableOrdering {

    /** Tests for tableOrdering pass-through. */
    VerifyTableOrdering() {}

    /** Verifies that tableOrdering from annotation is passed to ExpectationContext. */
    @Test
    @Tag("normal")
    @DisplayName("should pass tableOrdering from annotation to ExpectationContext")
    void shouldPassTableOrdering_whenAnnotationSpecifiesStrategy() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.tableOrdering()).thenReturn(TableOrderingStrategy.ALPHABETICAL);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          TableOrderingStrategy.ALPHABETICAL,
          captor.getValue().tableOrdering(),
          "should pass ALPHABETICAL table ordering to ExpectationContext");
    }

    /** Verifies that default tableOrdering AUTO is passed when not overridden. */
    @Test
    @Tag("normal")
    @DisplayName("should pass AUTO tableOrdering when annotation uses default")
    void shouldPassAutoTableOrdering_whenAnnotationUsesDefault() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      // tableOrdering defaults to AUTO in setUp
      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          TableOrderingStrategy.AUTO,
          captor.getValue().tableOrdering(),
          "should pass AUTO table ordering to ExpectationContext");
    }

    /** Verifies that FOREIGN_KEY tableOrdering is passed through correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should pass FOREIGN_KEY tableOrdering to ExpectationContext")
    void shouldPassForeignKeyTableOrdering_whenAnnotationSpecifiesForeignKey() {
      // Given
      final var tableSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      when(tableSet.dataSource()).thenReturn(Optional.of(dataSource));
      when(tableSet.tables()).thenReturn(List.of());

      final var expectedTableSets = List.of(ExpectedTableSet.of(tableSet));
      when(loader.loadExpectationDataSetsWithExclusions(context)).thenReturn(expectedTableSets);

      when(expectedDataSet.tableOrdering()).thenReturn(TableOrderingStrategy.FOREIGN_KEY);

      doNothing()
          .when(expectationProvider)
          .verifyExpectation(any(), any(), any(ExpectationContext.class));

      // When
      support.verify(context, expectedDataSet);

      // Then
      final var captor = ArgumentCaptor.forClass(ExpectationContext.class);
      verify(expectationProvider).verifyExpectation(any(), any(), captor.capture());
      assertEquals(
          TableOrderingStrategy.FOREIGN_KEY,
          captor.getValue().tableOrdering(),
          "should pass FOREIGN_KEY table ordering to ExpectationContext");
    }
  }

  /** Tests for the verify method - null argument handling. */
  @Nested
  @DisplayName("verify method - argument validation")
  class VerifyArgumentValidation {

    /** Tests for argument validation. */
    VerifyArgumentValidation() {}

    /** Verifies that null context throws NullPointerException. */
    @Test
    @Tag("error")
    @DisplayName("should throw NullPointerException when context is null")
    @SuppressWarnings("NullAway")
    void shouldThrowNullPointerException_whenContextIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> support.verify(null, expectedDataSet),
          "should throw NullPointerException for null context");
    }

    /** Verifies that null expectedDataSet throws NullPointerException. */
    @Test
    @Tag("error")
    @DisplayName("should throw NullPointerException when expectedDataSet is null")
    @SuppressWarnings("NullAway")
    void shouldThrowNullPointerException_whenExpectedDataSetIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> support.verify(context, null),
          "should throw NullPointerException for null expectedDataSet");
    }
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that package-private constructor rejects null provider. */
    @Test
    @Tag("error")
    @DisplayName("should throw NullPointerException when expectationProvider is null")
    @SuppressWarnings("NullAway")
    void shouldThrowNullPointerException_whenExpectationProviderIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new DefaultExpectationSupport(null),
          "should throw NullPointerException for null expectationProvider");
    }
  }
}
