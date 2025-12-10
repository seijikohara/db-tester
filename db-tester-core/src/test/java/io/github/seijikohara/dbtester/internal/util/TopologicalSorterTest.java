package io.github.seijikohara.dbtester.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TopologicalSorter}. */
@DisplayName("TopologicalSorter")
class TopologicalSorterTest {

  /** Tests for the TopologicalSorter class. */
  TopologicalSorterTest() {}

  /** Tests for the sort() method. */
  @Nested
  @DisplayName("sort(List, Map) method")
  class SortMethod {

    /** Tests for the sort method. */
    SortMethod() {}

    /** Verifies that empty list returns empty list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty list when input is empty")
    void shouldReturnEmptyList_whenInputIsEmpty() {
      // Given
      final List<String> elements = List.of();
      final Map<String, Set<String>> dependencies = Map.of();

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(List.of(), result, "should return empty list");
    }

    /** Verifies that single element returns same list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return same list when only one element provided")
    void shouldReturnSameList_whenOnlyOneElementProvided() {
      // Given
      final List<String> elements = List.of("A");
      final Map<String, Set<String>> dependencies = Map.of();

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(elements, result, "should return same list for single element");
    }

    /** Verifies that elements with no dependencies return original order. */
    @Test
    @Tag("normal")
    @DisplayName("should return original order when no dependencies exist")
    void shouldReturnOriginalOrder_whenNoDependenciesExist() {
      // Given
      final List<String> elements = List.of("A", "B", "C");
      final Map<String, Set<String>> dependencies = Map.of();

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(elements, result, "should return original order when no dependencies");
    }

    /** Verifies that dependency is placed before dependent element. */
    @Test
    @Tag("normal")
    @DisplayName("should order dependency before dependent element")
    void shouldOrderDependencyBeforeDependent_whenDependencyExists() {
      // Given
      final List<String> elements = List.of("B", "A");
      final Map<String, Set<String>> dependencies = Map.of("B", Set.of("A"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(List.of("A", "B"), result, "A should come before B");
    }

    /** Verifies that multiple levels of dependencies are handled correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple levels of dependencies")
    void shouldHandleMultipleLevels_whenChainedDependenciesExist() {
      // Given
      final List<String> elements = List.of("C", "B", "A");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "B", Set.of("A"),
              "C", Set.of("B"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(List.of("A", "B", "C"), result, "order should be: A -> B -> C");
    }

    /** Verifies that diamond dependencies are handled correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should handle diamond dependencies")
    void shouldHandleDiamondDependencies_whenDiamondPatternExists() {
      // Given
      // D depends on both B and C, which both depend on A
      final List<String> elements = List.of("D", "C", "B", "A");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "B", Set.of("A"),
              "C", Set.of("A"),
              "D", Set.of("B", "C"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      // A should come first, B and C next (in original order), D last
      assertEquals(0, result.indexOf("A"), "A should be first");
      assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
      assertTrue(result.indexOf("A") < result.indexOf("C"), "A should come before C");
      assertTrue(result.indexOf("B") < result.indexOf("D"), "B should come before D");
      assertTrue(result.indexOf("C") < result.indexOf("D"), "C should come before D");
    }

    /** Verifies that circular dependencies fall back to original order. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to original order when circular dependency detected")
    void shouldFallBackToOriginalOrder_whenCircularDependencyDetected() {
      // Given
      final List<String> elements = List.of("A", "B");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "A", Set.of("B"),
              "B", Set.of("A"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(elements, result, "should preserve original order when circular dependency");
    }

    /** Verifies that partial cycles are handled correctly. */
    @Test
    @Tag("edge-case")
    @DisplayName("should process non-cyclic elements before falling back for cyclic ones")
    void shouldProcessNonCyclicFirst_whenPartialCycleExists() {
      // Given
      // A has no dependencies, B and C form a cycle
      final List<String> elements = List.of("B", "C", "A");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "B", Set.of("A", "C"),
              "C", Set.of("B"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(0, result.indexOf("A"), "A should be processed first (no dependencies)");
      // B and C should be in original order since they form a cycle
      assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
      assertTrue(result.indexOf("A") < result.indexOf("C"), "A should come before C");
    }

    /** Verifies that multiple independent chains are handled correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple independent chains")
    void shouldHandleMultipleChains_whenIndependentChainsExist() {
      // Given
      // Chain 1: A -> B, Chain 2: C -> D
      final List<String> elements = List.of("B", "D", "A", "C");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "B", Set.of("A"),
              "D", Set.of("C"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
      assertTrue(result.indexOf("C") < result.indexOf("D"), "C should come before D");
    }

    /** Verifies that original order is preserved for elements at the same level. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve original order for elements at the same dependency level")
    void shouldPreserveOriginalOrder_whenElementsAtSameLevel() {
      // Given
      // B and C both depend only on A
      final List<String> elements = List.of("C", "B", "A");
      final Map<String, Set<String>> dependencies =
          Map.of(
              "B", Set.of("A"),
              "C", Set.of("A"));

      // When
      final var result = TopologicalSorter.sort(elements, dependencies);

      // Then
      assertEquals(0, result.indexOf("A"), "A should be first");
      // B and C should maintain their original relative order (C before B)
      assertTrue(
          result.indexOf("C") < result.indexOf("B"), "C should come before B (original order)");
    }
  }
}
