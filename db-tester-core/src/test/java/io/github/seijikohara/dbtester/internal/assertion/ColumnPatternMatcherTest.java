package io.github.seijikohara.dbtester.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ColumnPatternMatcher}. */
@DisplayName("ColumnPatternMatcher")
class ColumnPatternMatcherTest {

  /** Tests for the ColumnPatternMatcher class. */
  ColumnPatternMatcherTest() {}

  /** Tests for the isPattern(String) method. */
  @Nested
  @DisplayName("isPattern(String) method")
  class IsPatternMethod {

    /** Tests for the isPattern method. */
    IsPatternMethod() {}

    /** Verifies that isPattern returns true when string contains asterisk. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when string contains asterisk")
    void shouldReturnTrue_whenStringContainsAsterisk() {
      // When & Then
      assertTrue(ColumnPatternMatcher.isPattern("*_AT"), "should detect asterisk pattern");
    }

    /** Verifies that isPattern returns true when string contains question mark. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when string contains question mark")
    void shouldReturnTrue_whenStringContainsQuestionMark() {
      // When & Then
      assertTrue(ColumnPatternMatcher.isPattern("VERSION?"), "should detect question mark pattern");
    }

    /** Verifies that isPattern returns false when string contains no wildcards. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when string contains no wildcards")
    void shouldReturnFalse_whenStringContainsNoWildcards() {
      // When & Then
      assertFalse(
          ColumnPatternMatcher.isPattern("CREATED_AT"), "should not detect pattern in plain name");
    }

    /** Verifies that isPattern returns false when string is empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when string is empty")
    void shouldReturnFalse_whenStringIsEmpty() {
      // When & Then
      assertFalse(ColumnPatternMatcher.isPattern(""), "should not detect pattern in empty string");
    }
  }

  /** Tests for the resolvePatterns(Collection, Collection) method. */
  @Nested
  @DisplayName("resolvePatterns(Collection, Collection) method")
  class ResolvePatternsMethod {

    /** Tests for the resolvePatterns method. */
    ResolvePatternsMethod() {}

    /** Verifies that resolvePatterns returns exact names unchanged when no patterns present. */
    @Test
    @Tag("normal")
    @DisplayName("should return exact names unchanged when no patterns present")
    void shouldReturnExactNames_whenNoPatternsPresent() {
      // Given
      final var excludeEntries = List.of("CREATED_AT", "VERSION");
      final var columnNames = List.of("ID", "NAME", "CREATED_AT", "VERSION");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertAll(
          "should contain exact names",
          () -> assertEquals(2, result.size(), "should have 2 entries"),
          () -> assertTrue(result.contains("CREATED_AT"), "should contain CREATED_AT"),
          () -> assertTrue(result.contains("VERSION"), "should contain VERSION"));
    }

    /** Verifies that resolvePatterns matches asterisk pattern against column names. */
    @Test
    @Tag("normal")
    @DisplayName("should match asterisk pattern against column names")
    void shouldMatchAsteriskPattern_whenPatternProvided() {
      // Given
      final var excludeEntries = List.of("*_AT");
      final var columnNames = List.of("ID", "NAME", "CREATED_AT", "UPDATED_AT", "DELETED_AT");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertAll(
          "should match all columns ending with _AT",
          () -> assertEquals(3, result.size(), "should have 3 matches"),
          () -> assertTrue(result.contains("CREATED_AT"), "should match CREATED_AT"),
          () -> assertTrue(result.contains("UPDATED_AT"), "should match UPDATED_AT"),
          () -> assertTrue(result.contains("DELETED_AT"), "should match DELETED_AT"));
    }

    /** Verifies that resolvePatterns matches question mark pattern against column names. */
    @Test
    @Tag("normal")
    @DisplayName("should match question mark pattern against column names")
    void shouldMatchQuestionMarkPattern_whenPatternProvided() {
      // Given
      final var excludeEntries = List.of("COL?");
      final var columnNames = List.of("COL1", "COL2", "COLA", "COLUMN");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertAll(
          "should match columns matching COL?",
          () -> assertEquals(3, result.size(), "should have 3 matches"),
          () -> assertTrue(result.contains("COL1"), "should match COL1"),
          () -> assertTrue(result.contains("COL2"), "should match COL2"),
          () -> assertTrue(result.contains("COLA"), "should match COLA"));
    }

    /** Verifies that resolvePatterns mixes exact names and patterns. */
    @Test
    @Tag("normal")
    @DisplayName("should combine exact names and pattern matches")
    void shouldCombineExactNamesAndPatternMatches() {
      // Given
      final var excludeEntries = List.of("VERSION", "*_BY");
      final var columnNames = List.of("ID", "VERSION", "CREATED_BY", "MODIFIED_BY", "NAME");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertAll(
          "should combine exact and pattern matches",
          () -> assertEquals(3, result.size(), "should have 3 entries"),
          () -> assertTrue(result.contains("VERSION"), "should contain exact name VERSION"),
          () -> assertTrue(result.contains("CREATED_BY"), "should match CREATED_BY"),
          () -> assertTrue(result.contains("MODIFIED_BY"), "should match MODIFIED_BY"));
    }

    /** Verifies that resolvePatterns performs case-insensitive matching. */
    @Test
    @Tag("edge-case")
    @DisplayName("should perform case-insensitive matching")
    void shouldPerformCaseInsensitiveMatching() {
      // Given
      final var excludeEntries = List.of("*_at");
      final var columnNames = List.of("ID", "Created_At", "UPDATED_AT");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertAll(
          "should match case-insensitively",
          () -> assertEquals(2, result.size(), "should have 2 matches"),
          () -> assertTrue(result.contains("CREATED_AT"), "should match CREATED_AT (uppercase)"),
          () -> assertTrue(result.contains("UPDATED_AT"), "should match UPDATED_AT (uppercase)"));
    }

    /** Verifies that resolvePatterns returns empty set when no entries provided. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty set when no entries provided")
    void shouldReturnEmptySet_whenNoEntriesProvided() {
      // Given
      final List<String> excludeEntries = List.of();
      final var columnNames = List.of("ID", "NAME");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertTrue(result.isEmpty(), "should return empty set");
    }

    /** Verifies that resolvePatterns returns empty set when pattern matches no columns. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return pattern with no matches as non-matching when no columns match")
    void shouldReturnNonMatchingEntries_whenPatternMatchesNoColumns() {
      // Given
      final var excludeEntries = List.of("*_XYZ");
      final var columnNames = List.of("ID", "NAME", "CREATED_AT");

      // When
      final var result = ColumnPatternMatcher.resolvePatterns(excludeEntries, columnNames);

      // Then
      assertTrue(result.isEmpty(), "should return empty set when pattern matches nothing");
    }
  }
}
