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

/** Unit tests for {@link Operation}. */
@DisplayName("Operation")
class OperationTest {

  /** Tests for the Operation enum. */
  OperationTest() {}

  /** Tests for enum values. */
  @Nested
  @DisplayName("enum values")
  class EnumValuesTests {

    /** Tests for enum values. */
    EnumValuesTests() {}

    /** Verifies that all expected operations exist. */
    @Test
    @Tag("normal")
    @DisplayName("should have all expected operation values")
    void shouldHaveAllExpectedOperationValues() {
      // Given
      final var expectedOperations =
          Set.of(
              "NONE",
              "UPDATE",
              "INSERT",
              "UPSERT",
              "DELETE",
              "DELETE_ALL",
              "TRUNCATE_TABLE",
              "CLEAN_INSERT",
              "TRUNCATE_INSERT");

      // When
      final var actualOperations = Arrays.stream(Operation.values()).map(Enum::name).toList();

      // Then
      assertEquals(
          expectedOperations.size(),
          actualOperations.size(),
          "should have expected number of operations");
      assertTrue(
          actualOperations.containsAll(expectedOperations),
          "should contain all expected operations");
    }

    /** Verifies that valueOf works correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct enum for valueOf")
    void shouldReturnCorrectEnumForValueOf() {
      // When & Then
      assertEquals(Operation.NONE, Operation.valueOf("NONE"), "should return NONE");
      assertEquals(Operation.INSERT, Operation.valueOf("INSERT"), "should return INSERT");
      assertEquals(Operation.UPDATE, Operation.valueOf("UPDATE"), "should return UPDATE");
      assertEquals(Operation.UPSERT, Operation.valueOf("UPSERT"), "should return UPSERT");
      assertEquals(Operation.DELETE, Operation.valueOf("DELETE"), "should return DELETE");
      assertEquals(
          Operation.DELETE_ALL, Operation.valueOf("DELETE_ALL"), "should return DELETE_ALL");
      assertEquals(
          Operation.TRUNCATE_TABLE,
          Operation.valueOf("TRUNCATE_TABLE"),
          "should return TRUNCATE_TABLE");
      assertEquals(
          Operation.CLEAN_INSERT, Operation.valueOf("CLEAN_INSERT"), "should return CLEAN_INSERT");
      assertEquals(
          Operation.TRUNCATE_INSERT,
          Operation.valueOf("TRUNCATE_INSERT"),
          "should return TRUNCATE_INSERT");
    }
  }

  /** Tests for name method. */
  @Nested
  @DisplayName("name method")
  class NameTests {

    /** Tests for name method. */
    NameTests() {}

    /** Verifies that name returns correct string. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct name")
    void shouldReturnCorrectName() {
      // When & Then
      assertEquals("NONE", Operation.NONE.name(), "should return NONE");
      assertEquals("CLEAN_INSERT", Operation.CLEAN_INSERT.name(), "should return CLEAN_INSERT");
    }
  }

  /** Tests for values method. */
  @Nested
  @DisplayName("values method")
  class ValuesTests {

    /** Tests for values method. */
    ValuesTests() {}

    /** Verifies that values returns all operations. */
    @Test
    @Tag("normal")
    @DisplayName("should return all operations")
    void shouldReturnAllOperations() {
      // When
      final var values = Operation.values();

      // Then
      assertNotNull(values, "values should not be null");
      assertEquals(9, values.length, "should have 9 operations");
    }
  }
}
