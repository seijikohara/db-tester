package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpectationContext}. */
@DisplayName("ExpectationContext")
class ExpectationContextTest {

  /** Tests for the ExpectationContext class. */
  ExpectationContextTest() {}

  /** Tests for the defaults() factory method. */
  @Nested
  @DisplayName("defaults() method")
  class DefaultsMethod {

    /** Tests for the defaults method. */
    DefaultsMethod() {}

    /** Verifies that defaults creates instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with default values")
    void shouldCreateInstanceWithDefaultValues() {
      // When
      final var context = ExpectationContext.defaults();

      // Then
      assertAll(
          "default context values",
          () -> assertNotNull(context, "context should not be null"),
          () -> assertTrue(context.excludeColumns().isEmpty(), "excludeColumns should be empty"),
          () ->
              assertTrue(context.columnStrategies().isEmpty(), "columnStrategies should be empty"),
          () ->
              assertEquals(
                  RowOrdering.ORDERED, context.rowOrdering(), "rowOrdering should be ORDERED"),
          () -> assertNotNull(context.operationDefaults(), "operationDefaults should not be null"),
          () ->
              assertEquals(
                  TableOrderingStrategy.AUTO,
                  context.tableOrdering(),
                  "tableOrdering should be AUTO"));
    }
  }

  /** Tests for the of() factory method. */
  @Nested
  @DisplayName("of(Collection, Map, RowOrdering, OperationDefaults) method")
  class OfMethod {

    /** Tests for the of method. */
    OfMethod() {}

    /** Verifies that of creates instance with provided values. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with provided values")
    void shouldCreateInstanceWithProvidedValues() {
      // Given
      final var excludeColumns = List.of("CREATED_AT", "UPDATED_AT");
      final var columnStrategies = Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));
      final var rowOrdering = RowOrdering.UNORDERED;
      final var operationDefaults = OperationDefaults.standard();

      // When
      final var context =
          ExpectationContext.of(excludeColumns, columnStrategies, rowOrdering, operationDefaults);

      // Then
      assertAll(
          "context values",
          () ->
              assertEquals(
                  Set.of("CREATED_AT", "UPDATED_AT"),
                  context.excludeColumns(),
                  "excludeColumns should match"),
          () ->
              assertEquals(1, context.columnStrategies().size(), "should have one column strategy"),
          () ->
              assertEquals(
                  RowOrdering.UNORDERED, context.rowOrdering(), "rowOrdering should be UNORDERED"),
          () ->
              assertEquals(
                  operationDefaults,
                  context.operationDefaults(),
                  "operationDefaults should match"));
    }
  }

  /** Tests for defensive copy behavior. */
  @Nested
  @DisplayName("defensive copies")
  class DefensiveCopies {

    /** Tests for defensive copy behavior. */
    DefensiveCopies() {}

    /** Verifies that excludeColumns creates a defensive copy. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy of excludeColumns")
    void shouldCreateDefensiveCopy_whenExcludeColumnsModified() {
      // Given
      final var mutableList = new ArrayList<>(List.of("COL1", "COL2"));
      final var context =
          ExpectationContext.of(
              mutableList, Map.of(), RowOrdering.ORDERED, OperationDefaults.standard());

      // When
      mutableList.add("COL3");

      // Then
      assertEquals(2, context.excludeColumns().size(), "excludeColumns should not be affected");
    }

    /** Verifies that columnStrategies creates a defensive copy. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy of columnStrategies")
    void shouldCreateDefensiveCopy_whenColumnStrategiesModified() {
      // Given
      final var mutableMap =
          new HashMap<>(Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")));
      final var context =
          ExpectationContext.of(
              List.of(), mutableMap, RowOrdering.ORDERED, OperationDefaults.standard());

      // When
      mutableMap.put("NAME", ColumnStrategyMapping.strict("NAME"));

      // Then
      assertEquals(1, context.columnStrategies().size(), "columnStrategies should not be affected");
    }

    /** Verifies that returned excludeColumns is immutable. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return immutable excludeColumns")
    void shouldReturnImmutableExcludeColumns() {
      // Given
      final var context =
          ExpectationContext.of(
              List.of("COL1"), Map.of(), RowOrdering.ORDERED, OperationDefaults.standard());

      // When & Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> context.excludeColumns().add("COL2"),
          "excludeColumns should be immutable");
    }

    /** Verifies that returned columnStrategies is immutable. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return immutable columnStrategies")
    void shouldReturnImmutableColumnStrategies() {
      // Given
      final var context =
          ExpectationContext.of(
              List.of(),
              Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")),
              RowOrdering.ORDERED,
              OperationDefaults.standard());

      // When & Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> context.columnStrategies().put("NAME", ColumnStrategyMapping.strict("NAME")),
          "columnStrategies should be immutable");
    }
  }

  /** Tests for with*() copy methods. */
  @Nested
  @DisplayName("with*() copy methods")
  class WithMethods {

    /** Tests for with methods. */
    WithMethods() {}

    /** Verifies that withExcludeColumns returns new instance with updated columns. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated excludeColumns")
    void shouldReturnNewInstance_whenWithExcludeColumnsCalled() {
      // Given
      final var original = ExpectationContext.defaults();

      // When
      final var updated = original.withExcludeColumns(List.of("CREATED_AT"));

      // Then
      assertAll(
          "updated context",
          () ->
              assertEquals(
                  Set.of("CREATED_AT"),
                  updated.excludeColumns(),
                  "excludeColumns should be updated"),
          () ->
              assertTrue(
                  original.excludeColumns().isEmpty(),
                  "original excludeColumns should remain empty"));
    }

    /** Verifies that withColumnStrategies returns new instance with updated strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated columnStrategies")
    void shouldReturnNewInstance_whenWithColumnStrategiesCalled() {
      // Given
      final var original = ExpectationContext.defaults();
      final var strategies = Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));

      // When
      final var updated = original.withColumnStrategies(strategies);

      // Then
      assertAll(
          "updated context",
          () ->
              assertEquals(1, updated.columnStrategies().size(), "should have one column strategy"),
          () ->
              assertTrue(
                  original.columnStrategies().isEmpty(),
                  "original columnStrategies should remain empty"));
    }

    /** Verifies that withRowOrdering returns new instance with updated ordering. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated rowOrdering")
    void shouldReturnNewInstance_whenWithRowOrderingCalled() {
      // Given
      final var original = ExpectationContext.defaults();

      // When
      final var updated = original.withRowOrdering(RowOrdering.UNORDERED);

      // Then
      assertAll(
          "updated context",
          () ->
              assertEquals(
                  RowOrdering.UNORDERED, updated.rowOrdering(), "rowOrdering should be UNORDERED"),
          () ->
              assertEquals(
                  RowOrdering.ORDERED,
                  original.rowOrdering(),
                  "original rowOrdering should remain ORDERED"));
    }

    /** Verifies that withOperationDefaults returns new instance with updated defaults. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated operationDefaults")
    void shouldReturnNewInstance_whenWithOperationDefaultsCalled() {
      // Given
      final var original = ExpectationContext.defaults();
      final var customDefaults = OperationDefaults.builder().floatingPointEpsilon(1e-9).build();

      // When
      final var updated = original.withOperationDefaults(customDefaults);

      // Then
      assertAll(
          "updated context",
          () ->
              assertEquals(
                  customDefaults,
                  updated.operationDefaults(),
                  "operationDefaults should be updated"),
          () ->
              assertEquals(
                  OperationDefaults.standard(),
                  original.operationDefaults(),
                  "original operationDefaults should remain standard"));
    }

    /** Verifies that withTableOrdering returns new instance with updated strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with updated tableOrdering")
    void shouldReturnNewInstance_whenWithTableOrderingCalled() {
      // Given
      final var original = ExpectationContext.defaults();

      // When
      final var updated = original.withTableOrdering(TableOrderingStrategy.ALPHABETICAL);

      // Then
      assertAll(
          "updated context",
          () ->
              assertEquals(
                  TableOrderingStrategy.ALPHABETICAL,
                  updated.tableOrdering(),
                  "tableOrdering should be ALPHABETICAL"),
          () ->
              assertEquals(
                  TableOrderingStrategy.AUTO,
                  original.tableOrdering(),
                  "original tableOrdering should remain AUTO"));
    }
  }
}
