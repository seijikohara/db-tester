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

/** Unit tests for {@link RowOrdering}. */
@DisplayName("RowOrdering")
class RowOrderingTest {

  /** Tests for the RowOrdering enum. */
  RowOrderingTest() {}

  /** Tests for enum values. */
  @Nested
  @DisplayName("enum values")
  class EnumValuesTests {

    /** Tests for enum values. */
    EnumValuesTests() {}

    /** Verifies that all expected orderings exist. */
    @Test
    @Tag("normal")
    @DisplayName("should have all expected ordering values")
    void shouldHaveAllExpectedOrderingValues() {
      // Given
      final var expectedOrderings = Set.of("ORDERED", "UNORDERED");

      // When
      final var actualOrderings = Arrays.stream(RowOrdering.values()).map(Enum::name).toList();

      // Then
      assertEquals(
          expectedOrderings.size(),
          actualOrderings.size(),
          "should have expected number of orderings");
      assertTrue(
          actualOrderings.containsAll(expectedOrderings), "should contain all expected orderings");
    }

    /** Verifies that valueOf works correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct enum for valueOf")
    void shouldReturnCorrectEnumForValueOf() {
      // When & Then
      assertEquals(RowOrdering.ORDERED, RowOrdering.valueOf("ORDERED"), "should return ORDERED");
      assertEquals(
          RowOrdering.UNORDERED, RowOrdering.valueOf("UNORDERED"), "should return UNORDERED");
    }
  }

  /** Tests for values method. */
  @Nested
  @DisplayName("values method")
  class ValuesTests {

    /** Tests for values method. */
    ValuesTests() {}

    /** Verifies that values returns all orderings. */
    @Test
    @Tag("normal")
    @DisplayName("should return all orderings")
    void shouldReturnAllOrderings() {
      // When
      final var values = RowOrdering.values();

      // Then
      assertNotNull(values, "values should not be null");
      assertEquals(2, values.length, "should have 2 orderings");
    }
  }
}
