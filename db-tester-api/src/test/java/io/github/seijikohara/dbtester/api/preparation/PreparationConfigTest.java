package io.github.seijikohara.dbtester.api.preparation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PreparationConfig}. */
@DisplayName("PreparationConfig")
class PreparationConfigTest {

  /** Tests for the PreparationConfig class. */
  PreparationConfigTest() {}

  /** Tests for the standard() factory method. */
  @Nested
  @DisplayName("standard() method")
  class StandardMethod {

    /** Tests for the standard method. */
    StandardMethod() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with default values")
    void shouldReturnInstance_withDefaultValues() {
      // When
      final var config = PreparationConfig.standard();

      // Then
      assertAll(
          "should have standard defaults",
          () ->
              assertEquals(
                  TableOrderingStrategy.AUTO,
                  config.tableOrdering(),
                  "should use AUTO table ordering"),
          () ->
              assertEquals(
                  TransactionMode.SINGLE_TRANSACTION,
                  config.transactionMode(),
                  "should use SINGLE_TRANSACTION mode"),
          () -> assertNull(config.queryTimeout(), "should have no query timeout"),
          () -> assertEquals(0, config.batchSize(), "should use single-batch execution"));
    }
  }

  /** Tests for the constructor validation. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor throws exception when batchSize is negative. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when batchSize is negative")
    void shouldThrowException_whenBatchSizeIsNegative() {
      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  new PreparationConfig(
                      TableOrderingStrategy.AUTO, TransactionMode.SINGLE_TRANSACTION, null, -1));

      assertEquals(
          "batchSize must be zero or positive, but was: -1",
          exception.getMessage(),
          "should include the invalid value in the message");
    }
  }

  /** Tests for the withTableOrdering() method. */
  @Nested
  @DisplayName("withTableOrdering() method")
  class WithTableOrderingStrategyMethod {

    /** Tests for the withTableOrdering method. */
    WithTableOrderingStrategyMethod() {}

    /** Verifies that withTableOrdering returns new instance with updated strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated strategy")
    void shouldReturnNewInstance_withUpdatedStrategy() {
      // Given
      final var original = PreparationConfig.standard();

      // When
      final var result = original.withTableOrdering(TableOrderingStrategy.FOREIGN_KEY);

      // Then
      assertAll(
          "should update only table ordering strategy",
          () ->
              assertEquals(
                  TableOrderingStrategy.FOREIGN_KEY,
                  result.tableOrdering(),
                  "should use FOREIGN_KEY strategy"),
          () ->
              assertEquals(
                  original.transactionMode(),
                  result.transactionMode(),
                  "should preserve transaction mode"),
          () ->
              assertEquals(
                  original.queryTimeout(), result.queryTimeout(), "should preserve query timeout"),
          () ->
              assertEquals(original.batchSize(), result.batchSize(), "should preserve batch size"));
    }
  }

  /** Tests for the withTransactionMode() method. */
  @Nested
  @DisplayName("withTransactionMode() method")
  class WithTransactionModeMethod {

    /** Tests for the withTransactionMode method. */
    WithTransactionModeMethod() {}

    /** Verifies that withTransactionMode returns new instance with updated mode. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated mode")
    void shouldReturnNewInstance_withUpdatedMode() {
      // Given
      final var original = PreparationConfig.standard();

      // When
      final var result = original.withTransactionMode(TransactionMode.AUTO_COMMIT);

      // Then
      assertAll(
          "should update only transaction mode",
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  result.transactionMode(),
                  "should use AUTO_COMMIT mode"),
          () ->
              assertEquals(
                  original.tableOrdering(),
                  result.tableOrdering(),
                  "should preserve table ordering strategy"));
    }
  }

  /** Tests for the withQueryTimeout() method. */
  @Nested
  @DisplayName("withQueryTimeout() method")
  class WithQueryTimeoutMethod {

    /** Tests for the withQueryTimeout method. */
    WithQueryTimeoutMethod() {}

    /** Verifies that withQueryTimeout returns new instance with specified timeout. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with specified timeout")
    void shouldReturnNewInstance_withSpecifiedTimeout() {
      // Given
      final var original = PreparationConfig.standard();
      final var timeout = Duration.ofSeconds(30);

      // When
      final var result = original.withQueryTimeout(timeout);

      // Then
      assertEquals(timeout, result.queryTimeout(), "should use the specified timeout");
    }

    /** Verifies that withQueryTimeout accepts null for no timeout. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept null for no timeout")
    void shouldAcceptNull_forNoTimeout() {
      // Given
      final var config = PreparationConfig.standard().withQueryTimeout(Duration.ofSeconds(10));

      // When
      final var result = config.withQueryTimeout(null);

      // Then
      assertNull(result.queryTimeout(), "should have no timeout");
    }
  }

  /** Tests for the withBatchSize() method. */
  @Nested
  @DisplayName("withBatchSize() method")
  class WithBatchSizeMethod {

    /** Tests for the withBatchSize method. */
    WithBatchSizeMethod() {}

    /** Verifies that withBatchSize returns new instance with specified batch size. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with specified batch size")
    void shouldReturnNewInstance_withSpecifiedBatchSize() {
      // Given
      final var original = PreparationConfig.standard();

      // When
      final var result = original.withBatchSize(500);

      // Then
      assertEquals(500, result.batchSize(), "should use the specified batch size");
    }

    /** Verifies that withBatchSize throws exception when value is negative. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when value is negative")
    void shouldThrowException_whenValueIsNegative() {
      // Given
      final var config = PreparationConfig.standard();

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> config.withBatchSize(-1),
          "should reject negative batch size");
    }
  }
}
