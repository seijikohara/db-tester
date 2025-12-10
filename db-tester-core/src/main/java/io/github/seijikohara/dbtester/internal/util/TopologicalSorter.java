package io.github.seijikohara.dbtester.internal.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs topological sorting on a directed acyclic graph (DAG).
 *
 * <p>This class implements Kahn's algorithm for topological sorting. Elements with no dependencies
 * are placed first, followed by elements that only depend on already-placed elements.
 *
 * <p>If a cycle is detected, the remaining elements are appended in their original order and a
 * warning is logged.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * List<String> elements = List.of("C", "B", "A");
 * Map<String, Set<String>> dependencies = Map.of("B", Set.of("A"), "C", Set.of("B"));
 * List<String> sorted = TopologicalSorter.sort(elements, dependencies);
 * // Result: ["A", "B", "C"]
 * }</pre>
 *
 * @param <T> the type of elements to sort
 */
public final class TopologicalSorter<T> {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TopologicalSorter.class);

  /** The original elements to sort (order is preserved for elements at the same level). */
  private final List<T> elements;

  /** The dependency graph mapping each element to its dependencies. */
  private final Map<T, Set<T>> dependencies;

  /** The sorted result list. */
  private final List<T> result;

  /** The set of already visited (processed) elements. */
  private final Set<T> visited;

  /** The set of remaining (unprocessed) elements. */
  private final Set<T> remaining;

  /**
   * Creates a new topological sorter for the given elements and dependencies.
   *
   * @param elements the elements to sort (order is preserved for elements at the same level)
   * @param dependencies a map from element to the set of elements it depends on
   */
  private TopologicalSorter(final List<T> elements, final Map<T, Set<T>> dependencies) {
    this.elements = elements;
    this.dependencies = dependencies;
    this.result = new ArrayList<>();
    this.visited = new HashSet<>();
    this.remaining = new LinkedHashSet<>(elements);
  }

  /**
   * Sorts elements based on their dependencies using topological ordering.
   *
   * <p>Elements with no dependencies come first, followed by elements whose dependencies have
   * already been placed. The original order is preserved among elements at the same dependency
   * level.
   *
   * <p>If a cycle is detected, the remaining elements are appended in their original order.
   *
   * @param <T> the type of elements to sort
   * @param elements the elements to sort (order is preserved for elements at the same level)
   * @param dependencies a map from element to the set of elements it depends on
   * @return the topologically sorted list of elements
   */
  public static <T> List<T> sort(final List<T> elements, final Map<T, Set<T>> dependencies) {
    return new TopologicalSorter<>(elements, dependencies).execute();
  }

  /**
   * Executes the topological sort algorithm.
   *
   * @return the topologically sorted list of elements
   */
  private List<T> execute() {
    if (elements.size() <= 1) {
      return elements;
    }

    // Kahn's algorithm - while loop required for iterative dependency resolution
    while (!remaining.isEmpty()) {
      final var ready = findReadyElements();

      if (ready.isEmpty()) {
        handleCycleDetected();
        break;
      }

      processReadyElements(ready);
    }

    return List.copyOf(result);
  }

  /**
   * Finds elements that are ready to be processed (all dependencies visited).
   *
   * @return the set of ready elements
   */
  private Set<T> findReadyElements() {
    return remaining.stream().filter(this::isReady).collect(Collectors.toSet());
  }

  /**
   * Checks if an element is ready to be processed (all dependencies are visited).
   *
   * @param element the element to check
   * @return true if the element has no unvisited dependencies
   */
  private boolean isReady(final T element) {
    return Optional.ofNullable(dependencies.get(element)).map(visited::containsAll).orElse(true);
  }

  /** Handles cycle detection by adding remaining elements in original order. */
  private void handleCycleDetected() {
    logger.warn(
        "Circular dependency detected among elements: {}. Using original order for these elements.",
        remaining);
    elements.stream().filter(remaining::contains).forEach(result::add);
  }

  /**
   * Processes ready elements by adding them to result and updating state.
   *
   * @param ready the set of ready elements
   */
  private void processReadyElements(final Set<T> ready) {
    elements.stream()
        .filter(element -> ready.contains(element) && !visited.contains(element))
        .forEach(
            element -> {
              result.add(element);
              visited.add(element);
              remaining.remove(element);
            });
  }
}
