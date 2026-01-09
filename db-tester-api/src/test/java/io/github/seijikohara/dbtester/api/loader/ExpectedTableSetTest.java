package io.github.seijikohara.dbtester.api.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpectedTableSet}. */
@DisplayName("ExpectedTableSet")
class ExpectedTableSetTest {

  /** Tests for the ExpectedTableSet class. */
  ExpectedTableSetTest() {}

  /** Mock TableSet for tests. */
  private TableSet mockTableSet;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockTableSet = mock(TableSet.class);
  }

  /** Tests for the primary constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates defensive copies of collections. */
    @Test
    @Tag("normal")
    @DisplayName("should create defensive copies when collections provided")
    void shouldCreateDefensiveCopies_whenCollectionsProvided() {
      // Given
      final var excludeColumns = new HashSet<String>();
      excludeColumns.add("COLUMN1");
      final var columnStrategies = new HashMap<String, ColumnStrategyMapping>();
      columnStrategies.put("COLUMN2", ColumnStrategyMapping.ignore("COLUMN2"));

      // When
      final var result = new ExpectedTableSet(mockTableSet, excludeColumns, columnStrategies);

      // Then - modify original collections
      excludeColumns.add("COLUMN3");
      columnStrategies.put("COLUMN4", ColumnStrategyMapping.strict("COLUMN4"));

      assertAll(
          "should be immutable and independent of original collections",
          () ->
              assertEquals(
                  1, result.excludeColumns().size(), "excludeColumns should have original size"),
          () ->
              assertEquals(
                  1,
                  result.columnStrategies().size(),
                  "columnStrategies should have original size"),
          () ->
              assertTrue(
                  result.excludeColumns().contains("COLUMN1"), "should contain original column"));
    }

    /** Verifies that constructor returns immutable excludeColumns set. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return immutable excludeColumns when accessed")
    void shouldReturnImmutableExcludeColumns_whenAccessed() {
      // Given
      final var result = new ExpectedTableSet(mockTableSet, Set.of("COLUMN1"), Map.of());

      // When & Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.excludeColumns().add("NEW_COLUMN"),
          "excludeColumns should be immutable");
    }

    /** Verifies that constructor returns immutable columnStrategies map. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return immutable columnStrategies when accessed")
    void shouldReturnImmutableColumnStrategies_whenAccessed() {
      // Given
      final var result = new ExpectedTableSet(mockTableSet, Set.of(), Map.of());

      // When & Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.columnStrategies().put("NEW", ColumnStrategyMapping.strict("NEW")),
          "columnStrategies should be immutable");
    }
  }

  /** Tests for the of(TableSet) factory method. */
  @Nested
  @DisplayName("of(TableSet) method")
  class OfTableSetMethod {

    /** Tests for the of(TableSet) method. */
    OfTableSetMethod() {}

    /** Verifies that of returns instance with empty exclusions and strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with empty exclusions when only tableSet provided")
    void shouldReturnInstanceWithEmptyExclusions_whenOnlyTableSetProvided() {
      // When
      final var result = ExpectedTableSet.of(mockTableSet);

      // Then
      assertAll(
          "should have empty exclusions and strategies",
          () -> assertEquals(mockTableSet, result.tableSet(), "should have the provided tableSet"),
          () -> assertTrue(result.excludeColumns().isEmpty(), "excludeColumns should be empty"),
          () -> assertTrue(result.columnStrategies().isEmpty(), "columnStrategies should be empty"),
          () -> assertFalse(result.hasExclusions(), "hasExclusions should be false"),
          () -> assertFalse(result.hasColumnStrategies(), "hasColumnStrategies should be false"));
    }
  }

  /** Tests for the of(TableSet, Set) factory method. */
  @Nested
  @DisplayName("of(TableSet, Set) method")
  class OfTableSetSetMethod {

    /** Tests for the of(TableSet, Set) method. */
    OfTableSetSetMethod() {}

    /** Verifies that of returns instance with specified exclusions. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with exclusions when excludeColumns provided")
    void shouldReturnInstanceWithExclusions_whenExcludeColumnsProvided() {
      // Given
      final var excludeColumns = Set.of("COLUMN1", "COLUMN2");

      // When
      final var result = ExpectedTableSet.of(mockTableSet, excludeColumns);

      // Then
      assertAll(
          "should have the specified exclusions",
          () -> assertEquals(2, result.excludeColumns().size(), "should have 2 excluded columns"),
          () -> assertTrue(result.hasExclusions(), "hasExclusions should be true"),
          () -> assertFalse(result.hasColumnStrategies(), "hasColumnStrategies should be false"));
    }

    /** Verifies that of creates defensive copy of excludeColumns. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy when excludeColumns modified after creation")
    void shouldCreateDefensiveCopy_whenExcludeColumnsModifiedAfterCreation() {
      // Given
      final var excludeColumns = new HashSet<String>();
      excludeColumns.add("COLUMN1");

      // When
      final var result = ExpectedTableSet.of(mockTableSet, excludeColumns);
      excludeColumns.add("COLUMN2");

      // Then
      assertEquals(
          1,
          result.excludeColumns().size(),
          "excludeColumns should not be affected by original modification");
    }
  }

  /** Tests for the of(TableSet, Set, Map) factory method. */
  @Nested
  @DisplayName("of(TableSet, Set, Map) method")
  class OfTableSetSetMapMethod {

    /** Tests for the of(TableSet, Set, Map) method. */
    OfTableSetSetMapMethod() {}

    /** Verifies that of returns instance with specified exclusions and strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with exclusions and strategies when all provided")
    void shouldReturnInstanceWithExclusionsAndStrategies_whenAllProvided() {
      // Given
      final var excludeColumns = Set.of("EXCLUDED");
      final var columnStrategies = Map.of("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));

      // When
      final var result = ExpectedTableSet.of(mockTableSet, excludeColumns, columnStrategies);

      // Then
      assertAll(
          "should have the specified exclusions and strategies",
          () -> assertEquals(1, result.excludeColumns().size(), "should have 1 excluded column"),
          () -> assertEquals(1, result.columnStrategies().size(), "should have 1 strategy"),
          () -> assertTrue(result.hasExclusions(), "hasExclusions should be true"),
          () -> assertTrue(result.hasColumnStrategies(), "hasColumnStrategies should be true"));
    }

    /** Verifies that of creates defensive copies of both collections. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copies when collections modified after creation")
    void shouldCreateDefensiveCopies_whenCollectionsModifiedAfterCreation() {
      // Given
      final var excludeColumns = new HashSet<String>();
      excludeColumns.add("COLUMN1");
      final var columnStrategies = new HashMap<String, ColumnStrategyMapping>();
      columnStrategies.put("COLUMN2", ColumnStrategyMapping.ignore("COLUMN2"));

      // When
      final var result = ExpectedTableSet.of(mockTableSet, excludeColumns, columnStrategies);
      excludeColumns.add("COLUMN3");
      columnStrategies.put("COLUMN4", ColumnStrategyMapping.strict("COLUMN4"));

      // Then
      assertAll(
          "should not be affected by original collection modifications",
          () ->
              assertEquals(
                  1, result.excludeColumns().size(), "excludeColumns should have original size"),
          () ->
              assertEquals(
                  1,
                  result.columnStrategies().size(),
                  "columnStrategies should have original size"));
    }

    /** Verifies that returned collections are independent copies. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return independent collections when accessed multiple times")
    void shouldReturnIndependentCollections_whenAccessedMultipleTimes() {
      // Given
      final var excludeColumns = Set.of("COLUMN1");
      final var columnStrategies = Map.of("COLUMN2", ColumnStrategyMapping.strict("COLUMN2"));
      final var result = ExpectedTableSet.of(mockTableSet, excludeColumns, columnStrategies);

      // When
      final var excludeColumns1 = result.excludeColumns();
      final var excludeColumns2 = result.excludeColumns();
      final var strategies1 = result.columnStrategies();
      final var strategies2 = result.columnStrategies();

      // Then - should be the same instance (record accessor returns the field directly)
      assertAll(
          "should return same collection instances",
          () -> assertEquals(excludeColumns1, excludeColumns2, "excludeColumns should be equal"),
          () -> assertEquals(strategies1, strategies2, "columnStrategies should be equal"));
    }
  }

  /** Tests for the hasExclusions() method. */
  @Nested
  @DisplayName("hasExclusions() method")
  class HasExclusionsMethod {

    /** Tests for the hasExclusions method. */
    HasExclusionsMethod() {}

    /** Verifies that hasExclusions returns false when excludeColumns is empty. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when excludeColumns is empty")
    void shouldReturnFalse_whenExcludeColumnsIsEmpty() {
      // Given
      final var result = ExpectedTableSet.of(mockTableSet);

      // When & Then
      assertFalse(result.hasExclusions(), "should return false for empty excludeColumns");
    }

    /** Verifies that hasExclusions returns true when excludeColumns has elements. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when excludeColumns has elements")
    void shouldReturnTrue_whenExcludeColumnsHasElements() {
      // Given
      final var result = ExpectedTableSet.of(mockTableSet, Set.of("COLUMN1"));

      // When & Then
      assertTrue(result.hasExclusions(), "should return true for non-empty excludeColumns");
    }
  }

  /** Tests for the hasColumnStrategies() method. */
  @Nested
  @DisplayName("hasColumnStrategies() method")
  class HasColumnStrategiesMethod {

    /** Tests for the hasColumnStrategies method. */
    HasColumnStrategiesMethod() {}

    /** Verifies that hasColumnStrategies returns false when columnStrategies is empty. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when columnStrategies is empty")
    void shouldReturnFalse_whenColumnStrategiesIsEmpty() {
      // Given
      final var result = ExpectedTableSet.of(mockTableSet);

      // When & Then
      assertFalse(result.hasColumnStrategies(), "should return false for empty columnStrategies");
    }

    /** Verifies that hasColumnStrategies returns true when columnStrategies has elements. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when columnStrategies has elements")
    void shouldReturnTrue_whenColumnStrategiesHasElements() {
      // Given
      final var strategies = Map.of("COLUMN1", ColumnStrategyMapping.ignore("COLUMN1"));
      final var result = ExpectedTableSet.of(mockTableSet, Set.of(), strategies);

      // When & Then
      assertTrue(result.hasColumnStrategies(), "should return true for non-empty columnStrategies");
    }
  }
}
