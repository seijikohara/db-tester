package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.assertion.ExpectationVerifier;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultExpectationProvider}. */
@DisplayName("DefaultExpectationProvider")
class DefaultExpectationProviderTest {

  /** Tests for the DefaultExpectationProvider class. */
  DefaultExpectationProviderTest() {}

  /** Mock expectation verifier. */
  private ExpectationVerifier mockExpectationVerifier;

  /** The provider instance under test. */
  private DefaultExpectationProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockExpectationVerifier = mock(ExpectationVerifier.class);
    provider = new DefaultExpectationProvider(mockExpectationVerifier);
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that default constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when default constructor called")
    void shouldCreateInstance_whenDefaultConstructorCalled() {
      // When
      final var instance = new DefaultExpectationProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependencies creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when dependencies provided")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance = new DefaultExpectationProvider(mockExpectationVerifier);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the verifyExpectation(TableSet, DataSource) method. */
  @Nested
  @DisplayName("verifyExpectation(TableSet, DataSource) method")
  class VerifyExpectationMethod {

    /** Tests for the verifyExpectation method. */
    VerifyExpectationMethod() {}

    /** Verifies that verifyExpectation delegates to expectation verifier. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to expectation verifier when called")
    void shouldDelegateToExpectationVerifier_whenCalled() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      doNothing()
          .when(mockExpectationVerifier)
          .verifyExpectation(any(TableSet.class), any(DataSource.class));

      // When
      provider.verifyExpectation(expectedDataSet, dataSource);

      // Then
      verify(mockExpectationVerifier).verifyExpectation(expectedDataSet, dataSource);
    }
  }

  /** Tests for the verifyExpectation(TableSet, DataSource, ExpectationContext) method. */
  @Nested
  @DisplayName("verifyExpectation(TableSet, DataSource, ExpectationContext) method")
  class VerifyExpectationWithContextMethod {

    /** Tests for the verifyExpectation method with ExpectationContext. */
    VerifyExpectationWithContextMethod() {}

    /** Verifies that verifyExpectation delegates to expectation verifier with context. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to expectation verifier when called with context")
    void shouldDelegateToExpectationVerifier_whenCalledWithContext() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var context = ExpectationContext.defaults();
      doNothing()
          .when(mockExpectationVerifier)
          .verifyExpectation(
              any(TableSet.class), any(DataSource.class), any(ExpectationContext.class));

      // When
      provider.verifyExpectation(expectedDataSet, dataSource, context);

      // Then
      verify(mockExpectationVerifier).verifyExpectation(expectedDataSet, dataSource, context);
    }

    /** Verifies that verifyExpectation passes all context parameters to verifier. */
    @Test
    @Tag("normal")
    @DisplayName("should pass context with all parameters to expectation verifier")
    void shouldPassContextWithAllParameters_whenCalledWithCustomContext() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var context =
          ExpectationContext.of(
              List.of("CREATED_AT"),
              Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")),
              RowOrdering.UNORDERED,
              OperationDefaults.standard());
      doNothing()
          .when(mockExpectationVerifier)
          .verifyExpectation(
              any(TableSet.class), any(DataSource.class), any(ExpectationContext.class));

      // When
      provider.verifyExpectation(expectedDataSet, dataSource, context);

      // Then
      verify(mockExpectationVerifier).verifyExpectation(expectedDataSet, dataSource, context);
    }
  }

  /**
   * Tests for the verifyExpectation(TableSet, DataSource, Collection, Map, RowOrdering,
   * OperationDefaults) method.
   */
  @Nested
  @DisplayName(
      "verifyExpectation(TableSet, DataSource, Collection, Map, RowOrdering, OperationDefaults)"
          + " method")
  @SuppressWarnings("removal")
  class VerifyExpectationWithOperationDefaultsMethod {

    /** Tests for the verifyExpectation method with OperationDefaults. */
    VerifyExpectationWithOperationDefaultsMethod() {}

    /** Verifies that verifyExpectation delegates to expectation verifier with all arguments. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to expectation verifier when called with operation defaults")
    void shouldDelegateToExpectationVerifier_whenCalledWithOperationDefaults() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var excludeColumns = List.of("CREATED_AT");
      final Map<String, ColumnStrategyMapping> columnStrategies = Map.of();
      final var rowOrdering = RowOrdering.ORDERED;
      final var operationDefaults = OperationDefaults.standard();
      doNothing()
          .when(mockExpectationVerifier)
          .verifyExpectation(
              any(TableSet.class),
              any(DataSource.class),
              any(),
              any(),
              any(RowOrdering.class),
              any(OperationDefaults.class));

      // When
      provider.verifyExpectation(
          expectedDataSet,
          dataSource,
          excludeColumns,
          columnStrategies,
          rowOrdering,
          operationDefaults);

      // Then
      verify(mockExpectationVerifier)
          .verifyExpectation(
              expectedDataSet,
              dataSource,
              excludeColumns,
              columnStrategies,
              rowOrdering,
              operationDefaults);
    }
  }
}
