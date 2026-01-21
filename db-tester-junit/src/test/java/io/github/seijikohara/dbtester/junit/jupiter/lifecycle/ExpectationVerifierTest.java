package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpectationVerifier}. */
@DisplayName("ExpectationVerifier")
@SuppressWarnings("unchecked")
class ExpectationVerifierTest {

  /** Tests for the ExpectationVerifier class. */
  ExpectationVerifierTest() {}

  /** The verifier instance under test. */
  private ExpectationVerifier verifier;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    verifier = new ExpectationVerifier();
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
      final var instance = new ExpectationVerifier();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the verify(TestContext, Expectation) method. */
  @Nested
  @DisplayName("verify(TestContext, Expectation) method")
  class VerifyMethod {

    /** Tests for the verify method. */
    VerifyMethod() {}

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
     * Verifies that verify processes datasets when found.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should verify expectation when datasets found")
    void shouldVerifyExpectation_whenDatasetsFound() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockExpectationProvider = mock(ExpectationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      // Create a simple TableSet
      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("name")),
              List.of(
                  Row.of(
                      Map.of(
                          new ColumnName("id"),
                          new CellValue("1"),
                          new ColumnName("name"),
                          new CellValue("John")))));
      final var tableSet = TableSet.of(table);
      final var expectedTableSet =
          new ExpectedTableSet(tableSet, Collections.emptySet(), Collections.emptyMap());

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(List.of(expectedTableSet));
      when(mockConventions.retryCount()).thenReturn(0);
      when(mockConventions.retryDelay()).thenReturn(Duration.ofMillis(100));

      // Inject mock ExpectationProvider using reflection
      final Field providerField = ExpectationVerifier.class.getDeclaredField("expectationProvider");
      providerField.setAccessible(true);
      providerField.set(verifier, mockExpectationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When
      verifier.verify(context, expectedDataSet);

      // Then
      verify(mockExpectationProvider)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));
    }

    /**
     * Verifies that verify uses annotation retry count when specified.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should use annotation retry count when specified")
    void shouldUseAnnotationRetryCount_whenSpecified() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockExpectationProvider = mock(ExpectationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id")),
              List.of(Row.of(Map.of(new ColumnName("id"), new CellValue("1")))));
      final var tableSet = TableSet.of(table);
      final var expectedTableSet =
          new ExpectedTableSet(tableSet, Collections.emptySet(), Collections.emptyMap());

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(List.of(expectedTableSet));
      when(mockConventions.retryCount()).thenReturn(5);
      when(mockConventions.retryDelay()).thenReturn(Duration.ofMillis(100));

      final Field providerField = ExpectationVerifier.class.getDeclaredField("expectationProvider");
      providerField.setAccessible(true);
      providerField.set(verifier, mockExpectationProvider);

      final var testClass = TestClassWithRetrySettings.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When
      verifier.verify(context, expectedDataSet);

      // Then - verify was called (using annotation's retryCount=2)
      verify(mockExpectationProvider)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));
    }

    /**
     * Verifies that verify retries on validation exception.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("exceptional")
    @DisplayName("should retry and succeed on validation exception")
    void shouldRetryAndSucceed_onValidationException() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockExpectationProvider = mock(ExpectationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id")),
              List.of(Row.of(Map.of(new ColumnName("id"), new CellValue("1")))));
      final var tableSet = TableSet.of(table);
      final var expectedTableSet =
          new ExpectedTableSet(tableSet, Collections.emptySet(), Collections.emptyMap());

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(List.of(expectedTableSet));
      when(mockConventions.retryCount()).thenReturn(2);
      when(mockConventions.retryDelay()).thenReturn(Duration.ofMillis(10));

      // Fail first time, succeed second time
      doThrow(new ValidationException("First attempt failed"))
          .doNothing()
          .when(mockExpectationProvider)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));

      final Field providerField = ExpectationVerifier.class.getDeclaredField("expectationProvider");
      providerField.setAccessible(true);
      providerField.set(verifier, mockExpectationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When & Then
      assertDoesNotThrow(
          () -> verifier.verify(context, expectedDataSet), "should succeed after retry");

      verify(mockExpectationProvider, times(2))
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));
    }

    /**
     * Verifies that verify throws exception after all retries exhausted.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("exceptional")
    @DisplayName("should throw exception after all retries exhausted")
    void shouldThrowException_afterAllRetriesExhausted() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockExpectationProvider = mock(ExpectationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id")),
              List.of(Row.of(Map.of(new ColumnName("id"), new CellValue("1")))));
      final var tableSet = TableSet.of(table);
      final var expectedTableSet =
          new ExpectedTableSet(tableSet, Collections.emptySet(), Collections.emptyMap());

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(List.of(expectedTableSet));
      when(mockConventions.retryCount()).thenReturn(1);
      when(mockConventions.retryDelay()).thenReturn(Duration.ofMillis(10));

      // Always fail
      doThrow(new ValidationException("Persistent failure"))
          .when(mockExpectationProvider)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));

      final Field providerField = ExpectationVerifier.class.getDeclaredField("expectationProvider");
      providerField.setAccessible(true);
      providerField.set(verifier, mockExpectationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When & Then
      assertThrows(
          ValidationException.class,
          () -> verifier.verify(context, expectedDataSet),
          "should throw exception after retries exhausted");

      // Should have tried 2 times (initial + 1 retry)
      verify(mockExpectationProvider, times(2))
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));
    }

    /**
     * Verifies that verify handles exclusions and column strategies.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle exclusions and column strategies")
    void shouldHandleExclusionsAndColumnStrategies() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockExpectationProvider = mock(ExpectationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("created_at")),
              List.of(
                  Row.of(
                      Map.of(
                          new ColumnName("id"),
                          new CellValue("1"),
                          new ColumnName("created_at"),
                          new CellValue("2024-01-01")))));
      final var tableSet = TableSet.of(table);
      final var columnStrategy = ColumnStrategyMapping.ignore("CREATED_AT");
      final var expectedTableSet =
          new ExpectedTableSet(
              tableSet, java.util.Set.of("CREATED_AT"), Map.of("CREATED_AT", columnStrategy));

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any(TestContext.class)))
          .thenReturn(List.of(expectedTableSet));
      when(mockConventions.retryCount()).thenReturn(0);
      when(mockConventions.retryDelay()).thenReturn(Duration.ofMillis(100));

      final Field providerField = ExpectationVerifier.class.getDeclaredField("expectationProvider");
      providerField.setAccessible(true);
      providerField.set(verifier, mockExpectationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var expectedDataSet = testMethod.getAnnotation(ExpectedDataSet.class);

      // When
      verifier.verify(context, expectedDataSet);

      // Then
      verify(mockExpectationProvider)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(Collection.class),
              any(Map.class),
              any(RowOrdering.class));
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
