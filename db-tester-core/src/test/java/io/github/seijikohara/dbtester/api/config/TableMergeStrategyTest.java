package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableMergeStrategy}. */
@DisplayName("TableMergeStrategy")
class TableMergeStrategyTest {

  /** Tests for the TableMergeStrategy enum. */
  TableMergeStrategyTest() {}

  /** Tests for the FIRST strategy. */
  @Nested
  @DisplayName("FIRST strategy")
  class FirstStrategy {

    /** Tests for the FIRST strategy. */
    FirstStrategy() {}

    /** Verifies that FIRST has correct name. */
    @Test
    @Tag("normal")
    @DisplayName("should have name FIRST")
    void shouldHaveNameFirst() {
      // When
      final var name = TableMergeStrategy.FIRST.name();

      // Then
      assertEquals("FIRST", name, "name should be FIRST");
    }
  }

  /** Tests for the LAST strategy. */
  @Nested
  @DisplayName("LAST strategy")
  class LastStrategy {

    /** Tests for the LAST strategy. */
    LastStrategy() {}

    /** Verifies that LAST has correct name. */
    @Test
    @Tag("normal")
    @DisplayName("should have name LAST")
    void shouldHaveNameLast() {
      // When
      final var name = TableMergeStrategy.LAST.name();

      // Then
      assertEquals("LAST", name, "name should be LAST");
    }
  }

  /** Tests for the UNION strategy. */
  @Nested
  @DisplayName("UNION strategy")
  class UnionStrategy {

    /** Tests for the UNION strategy. */
    UnionStrategy() {}

    /** Verifies that UNION has correct name. */
    @Test
    @Tag("normal")
    @DisplayName("should have name UNION")
    void shouldHaveNameUnion() {
      // When
      final var name = TableMergeStrategy.UNION.name();

      // Then
      assertEquals("UNION", name, "name should be UNION");
    }
  }

  /** Tests for the UNION_ALL strategy. */
  @Nested
  @DisplayName("UNION_ALL strategy")
  class UnionAllStrategy {

    /** Tests for the UNION_ALL strategy. */
    UnionAllStrategy() {}

    /** Verifies that UNION_ALL has correct name. */
    @Test
    @Tag("normal")
    @DisplayName("should have name UNION_ALL")
    void shouldHaveNameUnionAll() {
      // When
      final var name = TableMergeStrategy.UNION_ALL.name();

      // Then
      assertEquals("UNION_ALL", name, "name should be UNION_ALL");
    }
  }

  /** Tests for enum values. */
  @Nested
  @DisplayName("values() method")
  class ValuesMethod {

    /** Tests for the values method. */
    ValuesMethod() {}

    /** Verifies that values returns all strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return all strategies")
    void shouldReturnAllStrategies() {
      // When
      final var values = TableMergeStrategy.values();

      // Then
      assertAll(
          "should have all strategies",
          () -> assertNotNull(values, "values should not be null"),
          () -> assertEquals(4, values.length, "should have four strategies"),
          () -> assertEquals(TableMergeStrategy.FIRST, values[0], "first should be FIRST"),
          () -> assertEquals(TableMergeStrategy.LAST, values[1], "second should be LAST"),
          () -> assertEquals(TableMergeStrategy.UNION, values[2], "third should be UNION"),
          () ->
              assertEquals(TableMergeStrategy.UNION_ALL, values[3], "fourth should be UNION_ALL"));
    }
  }

  /** Tests for valueOf method. */
  @Nested
  @DisplayName("valueOf() method")
  class ValueOfMethod {

    /** Tests for the valueOf method. */
    ValueOfMethod() {}

    /** Verifies that valueOf returns correct strategy for FIRST. */
    @Test
    @Tag("normal")
    @DisplayName("should return FIRST for valueOf(\"FIRST\")")
    void shouldReturnFirstForValueOf() {
      // When
      final var strategy = TableMergeStrategy.valueOf("FIRST");

      // Then
      assertEquals(TableMergeStrategy.FIRST, strategy, "should return FIRST");
    }

    /** Verifies that valueOf returns correct strategy for LAST. */
    @Test
    @Tag("normal")
    @DisplayName("should return LAST for valueOf(\"LAST\")")
    void shouldReturnLastForValueOf() {
      // When
      final var strategy = TableMergeStrategy.valueOf("LAST");

      // Then
      assertEquals(TableMergeStrategy.LAST, strategy, "should return LAST");
    }

    /** Verifies that valueOf returns correct strategy for UNION. */
    @Test
    @Tag("normal")
    @DisplayName("should return UNION for valueOf(\"UNION\")")
    void shouldReturnUnionForValueOf() {
      // When
      final var strategy = TableMergeStrategy.valueOf("UNION");

      // Then
      assertEquals(TableMergeStrategy.UNION, strategy, "should return UNION");
    }

    /** Verifies that valueOf returns correct strategy for UNION_ALL. */
    @Test
    @Tag("normal")
    @DisplayName("should return UNION_ALL for valueOf(\"UNION_ALL\")")
    void shouldReturnUnionAllForValueOf() {
      // When
      final var strategy = TableMergeStrategy.valueOf("UNION_ALL");

      // Then
      assertEquals(TableMergeStrategy.UNION_ALL, strategy, "should return UNION_ALL");
    }
  }
}
