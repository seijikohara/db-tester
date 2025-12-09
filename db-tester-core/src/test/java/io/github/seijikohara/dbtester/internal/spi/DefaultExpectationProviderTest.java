package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.assertion.ExpectationVerifier;
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

  /** Tests for the verifyExpectation() method. */
  @Nested
  @DisplayName("verifyExpectation(DataSet, DataSource) method")
  class VerifyExpectationMethod {

    /** Tests for the verifyExpectation method. */
    VerifyExpectationMethod() {}

    /** Verifies that verifyExpectation delegates to expectation verifier. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to expectation verifier when called")
    void shouldDelegateToExpectationVerifier_whenCalled() {
      // Given
      final var expectedDataSet = mock(DataSet.class);
      final var dataSource = mock(DataSource.class);
      doNothing()
          .when(mockExpectationVerifier)
          .verifyExpectation(any(DataSet.class), any(DataSource.class));

      // When
      provider.verifyExpectation(expectedDataSet, dataSource);

      // Then
      verify(mockExpectationVerifier).verifyExpectation(expectedDataSet, dataSource);
    }
  }
}
