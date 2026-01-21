package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TransactionMode}. */
@DisplayName("TransactionMode")
class TransactionModeTest {

  /** Tests for the TransactionMode enum. */
  TransactionModeTest() {}

  /** Tests for enum values. */
  @Nested
  @DisplayName("enum values")
  class EnumValuesTests {

    /** Tests for enum values. */
    EnumValuesTests() {}

    /** Verifies that all expected modes exist. */
    @Test
    @Tag("normal")
    @DisplayName("should have all expected mode values")
    void shouldHaveAllExpectedModeValues() {
      // Given
      final var expectedModes = Set.of("AUTO_COMMIT", "SINGLE_TRANSACTION", "NONE");

      // When
      final var actualModes = Arrays.stream(TransactionMode.values()).map(Enum::name).toList();

      // Then
      assertEquals(
          expectedModes.size(), actualModes.size(), "should have expected number of modes");
      assertTrue(actualModes.containsAll(expectedModes), "should contain all expected modes");
    }

    /** Verifies that valueOf works correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct enum for valueOf")
    void shouldReturnCorrectEnumForValueOf() {
      // When & Then
      assertEquals(
          TransactionMode.AUTO_COMMIT,
          TransactionMode.valueOf("AUTO_COMMIT"),
          "should return AUTO_COMMIT");
      assertEquals(
          TransactionMode.SINGLE_TRANSACTION,
          TransactionMode.valueOf("SINGLE_TRANSACTION"),
          "should return SINGLE_TRANSACTION");
      assertEquals(TransactionMode.NONE, TransactionMode.valueOf("NONE"), "should return NONE");
    }
  }

  /** Tests for values method. */
  @Nested
  @DisplayName("values method")
  class ValuesTests {

    /** Tests for values method. */
    ValuesTests() {}

    /** Verifies that values returns all modes. */
    @Test
    @Tag("normal")
    @DisplayName("should return all modes")
    void shouldReturnAllModes() {
      // When
      final var values = TransactionMode.values();

      // Then
      assertNotNull(values, "values should not be null");
      assertEquals(3, values.length, "should have 3 modes");
    }
  }
}
