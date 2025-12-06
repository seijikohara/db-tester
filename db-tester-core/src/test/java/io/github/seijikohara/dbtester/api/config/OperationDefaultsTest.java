package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

  /** Tests for the standard factory method. */
  @Nested
  @DisplayName("standard() factory method")
  class StandardMethod {

    /** Tests for the standard method. */
    StandardMethod() {}

    /** Verifies that standard returns instance with default operations. */
    @Test
    @Tag("normal")
    @DisplayName(
        "should return instance with CLEAN_INSERT for preparation and NONE for expectation")
    void should_return_instance_with_default_operations() {
      // Given & When
      final var defaults = OperationDefaults.standard();

      // Then
      assertAll(
          "should have default operations",
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT,
                  defaults.preparation(),
                  "preparation should be CLEAN_INSERT"),
          () -> assertEquals(Operation.NONE, defaults.expectation(), "expectation should be NONE"));
    }

    /** Verifies that standard returns equal instances. */
    @Test
    @Tag("normal")
    @DisplayName("should return equal instances on multiple calls")
    void should_return_equal_instances_on_multiple_calls() {
      // Given & When
      final var defaults1 = OperationDefaults.standard();
      final var defaults2 = OperationDefaults.standard();

      // Then
      assertEquals(defaults1, defaults2, "standard instances should be equal");
    }
  }

  /** Tests for the record constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor accepts custom operations. */
    @Test
    @Tag("normal")
    @DisplayName("should accept custom operations when provided")
    void should_accept_custom_operations_when_provided() {
      // Given
      final var prepOp = Operation.INSERT;
      final var expectOp = Operation.DELETE_ALL;

      // When
      final var defaults = new OperationDefaults(prepOp, expectOp);

      // Then
      assertAll(
          "should have custom operations",
          () -> assertEquals(prepOp, defaults.preparation(), "preparation should match"),
          () -> assertEquals(expectOp, defaults.expectation(), "expectation should match"));
    }

    /** Verifies that constructor accepts NONE for both operations. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept NONE for both operations")
    void should_accept_none_for_both_operations() {
      // Given & When
      final var defaults = new OperationDefaults(Operation.NONE, Operation.NONE);

      // Then
      assertAll(
          "should have NONE operations",
          () -> assertEquals(Operation.NONE, defaults.preparation(), "preparation should be NONE"),
          () -> assertEquals(Operation.NONE, defaults.expectation(), "expectation should be NONE"));
    }

    /** Verifies that all operation types are accepted. */
    @Test
    @Tag("normal")
    @DisplayName("should accept all operation types")
    void should_accept_all_operation_types() {
      // Given & When & Then - verify no exceptions for various combinations
      assertAll(
          "should accept all operation types",
          () -> new OperationDefaults(Operation.UPDATE, Operation.NONE),
          () -> new OperationDefaults(Operation.INSERT, Operation.DELETE),
          () -> new OperationDefaults(Operation.REFRESH, Operation.DELETE_ALL),
          () -> new OperationDefaults(Operation.DELETE, Operation.TRUNCATE_TABLE),
          () -> new OperationDefaults(Operation.DELETE_ALL, Operation.CLEAN_INSERT),
          () -> new OperationDefaults(Operation.TRUNCATE_TABLE, Operation.TRUNCATE_INSERT),
          () -> new OperationDefaults(Operation.CLEAN_INSERT, Operation.UPDATE),
          () -> new OperationDefaults(Operation.TRUNCATE_INSERT, Operation.INSERT),
          () -> new OperationDefaults(Operation.NONE, Operation.REFRESH));
    }
  }

  /** Tests for record equality. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /** Verifies that records with same values are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when operations are the same")
    void should_be_equal_when_operations_are_the_same() {
      // Given
      final var defaults1 = new OperationDefaults(Operation.INSERT, Operation.DELETE);
      final var defaults2 = new OperationDefaults(Operation.INSERT, Operation.DELETE);

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(defaults1, defaults2, "should be equal"),
          () -> assertEquals(defaults1.hashCode(), defaults2.hashCode(), "hashCodes should match"));
    }
  }
}
