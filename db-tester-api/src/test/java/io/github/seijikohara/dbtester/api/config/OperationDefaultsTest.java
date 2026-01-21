package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.operation.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link OperationDefaults}. */
@DisplayName("OperationDefaults")
class OperationDefaultsTest {

  /** Tests for the OperationDefaults class. */
  OperationDefaultsTest() {}

  /** Tests for standard() factory method. */
  @Nested
  @DisplayName("standard()")
  class StandardTest {

    /** Tests for standard() factory method. */
    StandardTest() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("returns instance with default values")
    void returnsInstanceWithDefaultValues() {
      final var defaults = OperationDefaults.standard();

      assertNotNull(defaults);
      assertEquals(Operation.CLEAN_INSERT, defaults.preparation());
      assertEquals(Operation.NONE, defaults.expectation());
    }
  }

  /** Tests for builder() factory method. */
  @Nested
  @DisplayName("builder()")
  class BuilderTest {

    /** Tests for builder() factory method. */
    BuilderTest() {}

    /** Verifies that builder creates builder with default values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with default values")
    void createsBuilderWithDefaultValues() {
      final var builder = OperationDefaults.builder();

      assertNotNull(builder);
    }

    /** Verifies that builder builds with custom preparation operation. */
    @Test
    @Tag("normal")
    @DisplayName("builds with custom preparation operation")
    void buildsWithCustomPreparationOperation() {
      final var defaults =
          OperationDefaults.builder().preparation(Operation.TRUNCATE_INSERT).build();

      assertEquals(Operation.TRUNCATE_INSERT, defaults.preparation());
    }

    /** Verifies that builder builds with custom expectation operation. */
    @Test
    @Tag("normal")
    @DisplayName("builds with custom expectation operation")
    void buildsWithCustomExpectationOperation() {
      final var defaults = OperationDefaults.builder().expectation(Operation.DELETE_ALL).build();

      assertEquals(Operation.DELETE_ALL, defaults.expectation());
    }

    /** Verifies that builder builds with both custom operations. */
    @Test
    @Tag("normal")
    @DisplayName("builds with both custom operations")
    void buildsWithBothCustomOperations() {
      final var defaults =
          OperationDefaults.builder()
              .preparation(Operation.INSERT)
              .expectation(Operation.DELETE)
              .build();

      assertEquals(Operation.INSERT, defaults.preparation());
      assertEquals(Operation.DELETE, defaults.expectation());
    }
  }

  /** Tests for withPreparation() method. */
  @Nested
  @DisplayName("withPreparation()")
  class WithPreparationTest {

    /** Tests for withPreparation() method. */
    WithPreparationTest() {}

    /** Verifies that withPreparation creates new instance with different preparation. */
    @Test
    @Tag("normal")
    @DisplayName("creates new instance with different preparation")
    void createsNewInstanceWithDifferentPreparation() {
      final var original = OperationDefaults.standard();
      final var modified = original.withPreparation(Operation.INSERT);

      assertNotEquals(original, modified);
      assertEquals(Operation.CLEAN_INSERT, original.preparation());
      assertEquals(Operation.INSERT, modified.preparation());
    }

    /** Verifies that withPreparation preserves expectation when changing preparation. */
    @Test
    @Tag("normal")
    @DisplayName("preserves expectation when changing preparation")
    void preservesExpectationWhenChangingPreparation() {
      final var original = OperationDefaults.builder().expectation(Operation.DELETE_ALL).build();
      final var modified = original.withPreparation(Operation.TRUNCATE_INSERT);

      assertEquals(Operation.DELETE_ALL, modified.expectation());
    }
  }

  /** Tests for withExpectation() method. */
  @Nested
  @DisplayName("withExpectation()")
  class WithExpectationTest {

    /** Tests for withExpectation() method. */
    WithExpectationTest() {}

    /** Verifies that withExpectation creates new instance with different expectation. */
    @Test
    @Tag("normal")
    @DisplayName("creates new instance with different expectation")
    void createsNewInstanceWithDifferentExpectation() {
      final var original = OperationDefaults.standard();
      final var modified = original.withExpectation(Operation.DELETE);

      assertNotEquals(original, modified);
      assertEquals(Operation.NONE, original.expectation());
      assertEquals(Operation.DELETE, modified.expectation());
    }

    /** Verifies that withExpectation preserves preparation when changing expectation. */
    @Test
    @Tag("normal")
    @DisplayName("preserves preparation when changing expectation")
    void preservesPreparationWhenChangingExpectation() {
      final var original = OperationDefaults.builder().preparation(Operation.INSERT).build();
      final var modified = original.withExpectation(Operation.DELETE_ALL);

      assertEquals(Operation.INSERT, modified.preparation());
    }
  }

  /** Tests for toBuilder() method. */
  @Nested
  @DisplayName("toBuilder()")
  class ToBuilderTest {

    /** Tests for toBuilder() method. */
    ToBuilderTest() {}

    /** Verifies that toBuilder creates builder with current values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with current values")
    void createsBuilderWithCurrentValues() {
      final var original =
          OperationDefaults.builder()
              .preparation(Operation.INSERT)
              .expectation(Operation.DELETE)
              .build();

      final var rebuilt = original.toBuilder().build();

      assertEquals(original, rebuilt);
    }

    /** Verifies that toBuilder allows modification of copied values. */
    @Test
    @Tag("normal")
    @DisplayName("allows modification of copied values")
    void allowsModificationOfCopiedValues() {
      final var original = OperationDefaults.builder().preparation(Operation.INSERT).build();

      final var modified = original.toBuilder().preparation(Operation.TRUNCATE_INSERT).build();

      assertEquals(Operation.INSERT, original.preparation());
      assertEquals(Operation.TRUNCATE_INSERT, modified.preparation());
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsHashCodeTest {

    /** Tests for equals and hashCode. */
    EqualsHashCodeTest() {}

    /** Verifies that equals returns true for same values. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same values")
    void equalsReturnsTrueForSameValues() {
      final var defaults1 = OperationDefaults.standard();
      final var defaults2 = OperationDefaults.standard();

      assertEquals(defaults1, defaults2);
    }

    /** Verifies that equals returns false for different preparation. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different preparation")
    void equalsReturnsFalseForDifferentPreparation() {
      final var defaults1 = OperationDefaults.standard();
      final var defaults2 = OperationDefaults.builder().preparation(Operation.INSERT).build();

      assertNotEquals(defaults1, defaults2);
    }

    /** Verifies that equals returns false for different expectation. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different expectation")
    void equalsReturnsFalseForDifferentExpectation() {
      final var defaults1 = OperationDefaults.standard();
      final var defaults2 = OperationDefaults.builder().expectation(Operation.DELETE).build();

      assertNotEquals(defaults1, defaults2);
    }

    /** Verifies that equals returns true for same instance. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same instance")
    void equalsReturnsTrueForSameInstance() {
      final var defaults = OperationDefaults.standard();

      assertEquals(defaults, defaults);
    }

    /** Verifies that equals returns false for null. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for null")
    void equalsReturnsFalseForNull() {
      final var defaults = OperationDefaults.standard();

      assertNotEquals(null, defaults);
    }

    /** Verifies that equals returns false for different type. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different type")
    void equalsReturnsFalseForDifferentType() {
      final var defaults = OperationDefaults.standard();

      assertNotEquals("string", defaults);
    }

    /** Verifies that hashCode is consistent for equal objects. */
    @Test
    @Tag("normal")
    @DisplayName("hashCode is consistent for equal objects")
    void hashCodeIsConsistentForEqualObjects() {
      final var defaults1 = OperationDefaults.standard();
      final var defaults2 = OperationDefaults.standard();

      assertEquals(defaults1.hashCode(), defaults2.hashCode());
    }
  }

  /** Tests for toString() method. */
  @Nested
  @DisplayName("toString()")
  class ToStringTest {

    /** Tests for toString() method. */
    ToStringTest() {}

    /** Verifies that toString returns string representation. */
    @Test
    @Tag("normal")
    @DisplayName("returns string representation")
    void returnsStringRepresentation() {
      final var defaults = OperationDefaults.standard();

      final var result = defaults.toString();

      assertNotNull(result);
      assertTrue(result.contains("OperationDefaults"));
      assertTrue(result.contains("preparation"));
      assertTrue(result.contains("CLEAN_INSERT"));
    }
  }
}
