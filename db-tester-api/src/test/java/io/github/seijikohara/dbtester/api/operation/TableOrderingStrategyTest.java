package io.github.seijikohara.dbtester.api.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableOrderingStrategy}. */
@DisplayName("TableOrderingStrategy")
class TableOrderingStrategyTest {

  /** Tests for the TableOrderingStrategy enum. */
  TableOrderingStrategyTest() {}

  /** Tests for enum values. */
  @Nested
  @DisplayName("enum values")
  class EnumValuesTests {

    /** Tests for enum values. */
    EnumValuesTests() {}

    /** Verifies that all expected strategies exist. */
    @Test
    @Tag("normal")
    @DisplayName("should have all expected strategy values")
    void shouldHaveAllExpectedStrategyValues() {
      // Given
      final var expectedStrategies =
          Set.of("AUTO", "LOAD_ORDER_FILE", "FOREIGN_KEY", "ALPHABETICAL");

      // When
      final var actualStrategies =
          Arrays.stream(TableOrderingStrategy.values()).map(Enum::name).toList();

      // Then
      assertEquals(
          expectedStrategies.size(),
          actualStrategies.size(),
          "should have expected number of strategies");
      assertTrue(
          actualStrategies.containsAll(expectedStrategies),
          "should contain all expected strategies");
    }

    /** Verifies that valueOf works correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct enum for valueOf")
    void shouldReturnCorrectEnumForValueOf() {
      // When & Then
      assertEquals(
          TableOrderingStrategy.AUTO, TableOrderingStrategy.valueOf("AUTO"), "should return AUTO");
      assertEquals(
          TableOrderingStrategy.LOAD_ORDER_FILE,
          TableOrderingStrategy.valueOf("LOAD_ORDER_FILE"),
          "should return LOAD_ORDER_FILE");
      assertEquals(
          TableOrderingStrategy.FOREIGN_KEY,
          TableOrderingStrategy.valueOf("FOREIGN_KEY"),
          "should return FOREIGN_KEY");
      assertEquals(
          TableOrderingStrategy.ALPHABETICAL,
          TableOrderingStrategy.valueOf("ALPHABETICAL"),
          "should return ALPHABETICAL");
    }
  }

  /** Tests for values method. */
  @Nested
  @DisplayName("values method")
  class ValuesTests {

    /** Tests for values method. */
    ValuesTests() {}

    /** Verifies that values returns all strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return all strategies")
    void shouldReturnAllStrategies() {
      // When
      final var values = TableOrderingStrategy.values();

      // Then
      assertNotNull(values, "values should not be null");
      assertEquals(4, values.length, "should have 4 strategies");
    }
  }
}
