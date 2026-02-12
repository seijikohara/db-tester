package io.github.seijikohara.dbtester.api.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Strategy}. */
@DisplayName("Strategy")
class StrategyTest {

  /** Tests for Strategy enum. */
  StrategyTest() {}

  /** Tests for toComparisonStrategy(String) with non-REGEX strategies (pattern is ignored). */
  @Nested
  @DisplayName("toComparisonStrategy(String) for non-REGEX strategies")
  class ToComparisonStrategyNonRegexTests {

    /** Tests for toComparisonStrategy with non-REGEX strategies. */
    ToComparisonStrategyNonRegexTests() {}

    /** Verifies that STRICT converts to ComparisonStrategy.STRICT. */
    @Test
    @Tag("normal")
    @DisplayName("should convert STRICT to ComparisonStrategy.STRICT")
    void shouldConvertStrict_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.STRICT.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.STRICT, result, "should be STRICT strategy");
      assertEquals(ComparisonStrategy.Type.STRICT, result.getType(), "should have STRICT type");
    }

    /** Verifies that IGNORE converts to ComparisonStrategy.IGNORE. */
    @Test
    @Tag("normal")
    @DisplayName("should convert IGNORE to ComparisonStrategy.IGNORE")
    void shouldConvertIgnore_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.IGNORE.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.IGNORE, result, "should be IGNORE strategy");
      assertEquals(ComparisonStrategy.Type.IGNORE, result.getType(), "should have IGNORE type");
    }

    /** Verifies that NUMERIC converts to ComparisonStrategy.NUMERIC. */
    @Test
    @Tag("normal")
    @DisplayName("should convert NUMERIC to ComparisonStrategy.NUMERIC")
    void shouldConvertNumeric_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.NUMERIC.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.NUMERIC, result, "should be NUMERIC strategy");
      assertEquals(ComparisonStrategy.Type.NUMERIC, result.getType(), "should have NUMERIC type");
    }

    /** Verifies that CASE_INSENSITIVE converts to ComparisonStrategy.CASE_INSENSITIVE. */
    @Test
    @Tag("normal")
    @DisplayName("should convert CASE_INSENSITIVE to ComparisonStrategy.CASE_INSENSITIVE")
    void shouldConvertCaseInsensitive_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.CASE_INSENSITIVE.toComparisonStrategy("");

      // Then
      assertSame(
          ComparisonStrategy.CASE_INSENSITIVE, result, "should be CASE_INSENSITIVE strategy");
      assertEquals(
          ComparisonStrategy.Type.CASE_INSENSITIVE,
          result.getType(),
          "should have CASE_INSENSITIVE type");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE converts to ComparisonStrategy.TIMESTAMP_FLEXIBLE. */
    @Test
    @Tag("normal")
    @DisplayName("should convert TIMESTAMP_FLEXIBLE to ComparisonStrategy.TIMESTAMP_FLEXIBLE")
    void shouldConvertTimestampFlexible_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.TIMESTAMP_FLEXIBLE.toComparisonStrategy("");

      // Then
      assertSame(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE, result, "should be TIMESTAMP_FLEXIBLE strategy");
      assertEquals(
          ComparisonStrategy.Type.TIMESTAMP_FLEXIBLE,
          result.getType(),
          "should have TIMESTAMP_FLEXIBLE type");
    }

    /** Verifies that NOT_NULL converts to ComparisonStrategy.NOT_NULL. */
    @Test
    @Tag("normal")
    @DisplayName("should convert NOT_NULL to ComparisonStrategy.NOT_NULL")
    void shouldConvertNotNull_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.NOT_NULL.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.NOT_NULL, result, "should be NOT_NULL strategy");
      assertEquals(ComparisonStrategy.Type.NOT_NULL, result.getType(), "should have NOT_NULL type");
    }

    /** Verifies that DATE_FLEXIBLE converts to ComparisonStrategy.DATE_FLEXIBLE. */
    @Test
    @Tag("normal")
    @DisplayName("should convert DATE_FLEXIBLE to ComparisonStrategy.DATE_FLEXIBLE")
    void shouldConvertDateFlexible_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.DATE_FLEXIBLE.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.DATE_FLEXIBLE, result, "should be DATE_FLEXIBLE strategy");
      assertEquals(
          ComparisonStrategy.Type.DATE_FLEXIBLE,
          result.getType(),
          "should have DATE_FLEXIBLE type");
    }

    /** Verifies that JSON_EQUIVALENT converts to ComparisonStrategy.JSON_EQUIVALENT. */
    @Test
    @Tag("normal")
    @DisplayName("should convert JSON_EQUIVALENT to ComparisonStrategy.JSON_EQUIVALENT")
    void shouldConvertJsonEquivalent_toComparisonStrategy() {
      // When
      final ComparisonStrategy result = Strategy.JSON_EQUIVALENT.toComparisonStrategy("");

      // Then
      assertSame(ComparisonStrategy.JSON_EQUIVALENT, result, "should be JSON_EQUIVALENT strategy");
      assertEquals(
          ComparisonStrategy.Type.JSON_EQUIVALENT,
          result.getType(),
          "should have JSON_EQUIVALENT type");
    }
  }

  /** Tests for toComparisonStrategy(String) method. */
  @Nested
  @DisplayName("toComparisonStrategy(String)")
  class ToComparisonStrategyWithPatternTests {

    /** Tests for toComparisonStrategy(String) method. */
    ToComparisonStrategyWithPatternTests() {}

    /** Verifies that REGEX with pattern creates regex strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should convert REGEX with pattern to ComparisonStrategy.regex")
    void shouldConvertRegex_toComparisonStrategyWithPattern() {
      // Given
      final String pattern = "[a-f0-9-]{36}";

      // When
      final ComparisonStrategy result = Strategy.REGEX.toComparisonStrategy(pattern);

      // Then
      assertEquals(ComparisonStrategy.Type.REGEX, result.getType(), "should have REGEX type");
      assertTrue(result.getPattern().isPresent(), "should have pattern");
      assertEquals(
          pattern, result.getPattern().get().pattern(), "should have correct pattern string");
    }

    /** Verifies that REGEX with empty pattern throws exception. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when REGEX pattern is empty")
    void shouldThrowException_whenRegexPatternIsEmpty() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> Strategy.REGEX.toComparisonStrategy(""),
          "should throw IllegalArgumentException for empty pattern");
    }

    /** Verifies that REGEX with null pattern throws exception. */
    @SuppressWarnings("NullAway")
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when REGEX pattern is null")
    void shouldThrowException_whenRegexPatternIsNull() {
      // When & Then
      final String nullPattern = null;
      assertThrows(
          IllegalArgumentException.class,
          () -> Strategy.REGEX.toComparisonStrategy(nullPattern),
          "should throw IllegalArgumentException for null pattern");
    }

    /** Verifies that non-REGEX strategies ignore pattern parameter. */
    @Test
    @Tag("normal")
    @DisplayName("should ignore pattern for non-REGEX strategies")
    void shouldIgnorePattern_whenNonRegexStrategy() {
      // When
      final ComparisonStrategy strictResult = Strategy.STRICT.toComparisonStrategy("ignored");
      final ComparisonStrategy ignoreResult = Strategy.IGNORE.toComparisonStrategy("ignored");

      // Then
      assertSame(ComparisonStrategy.STRICT, strictResult, "should return STRICT strategy");
      assertSame(ComparisonStrategy.IGNORE, ignoreResult, "should return IGNORE strategy");
    }
  }

  /** Tests for toComparisonStrategy(String, String) with options. */
  @Nested
  @DisplayName("toComparisonStrategy(String, String)")
  @SuppressWarnings("removal")
  class ToComparisonStrategyWithOptionsTests {

    /** Tests for toComparisonStrategy with pattern and options. */
    ToComparisonStrategyWithOptionsTests() {}

    /** Verifies that REGEX still works with pattern in two-argument overload. */
    @Test
    @Tag("normal")
    @DisplayName("should convert REGEX with pattern in two-argument overload")
    void shouldConvertRegex_toComparisonStrategyWithPatternAndOptions() {
      // Given
      final var pattern = "\\d{3}-\\d{4}";

      // When
      final ComparisonStrategy result = Strategy.REGEX.toComparisonStrategy(pattern, "");

      // Then
      assertEquals(ComparisonStrategy.Type.REGEX, result.getType(), "should have REGEX type");
      assertTrue(result.getPattern().isPresent(), "should have pattern");
      assertEquals(
          pattern, result.getPattern().get().pattern(), "should have correct pattern string");
    }

    /** Verifies that non-REGEX strategies delegate to single-arg overload. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to single-arg overload for non-REGEX strategies")
    void shouldDelegate_whenNonRegexStrategy() {
      // When
      final ComparisonStrategy strictResult = Strategy.STRICT.toComparisonStrategy("", "ignored");

      // Then
      assertSame(ComparisonStrategy.STRICT, strictResult, "should return STRICT strategy");
    }
  }

  /** Tests for enum values coverage. */
  @Nested
  @DisplayName("enum coverage")
  class EnumCoverageTests {

    /** Tests for enum values coverage. */
    EnumCoverageTests() {}

    /** Verifies all expected enum values exist. */
    @Test
    @Tag("normal")
    @DisplayName("should have all expected values")
    void shouldHaveAllExpectedValues() {
      // When
      final Strategy[] values = Strategy.values();

      // Then
      assertEquals(9, values.length, "should have 9 strategy values");
    }

    /** Verifies valueOf works correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve values by name")
    @SuppressWarnings("removal")
    void shouldResolveValuesByName() {
      // When & Then
      assertEquals(Strategy.STRICT, Strategy.valueOf("STRICT"), "should resolve STRICT");
      assertEquals(Strategy.IGNORE, Strategy.valueOf("IGNORE"), "should resolve IGNORE");
      assertEquals(Strategy.NUMERIC, Strategy.valueOf("NUMERIC"), "should resolve NUMERIC");
      assertEquals(
          Strategy.CASE_INSENSITIVE,
          Strategy.valueOf("CASE_INSENSITIVE"),
          "should resolve CASE_INSENSITIVE");
      assertEquals(
          Strategy.TIMESTAMP_FLEXIBLE,
          Strategy.valueOf("TIMESTAMP_FLEXIBLE"),
          "should resolve TIMESTAMP_FLEXIBLE");
      assertEquals(Strategy.NOT_NULL, Strategy.valueOf("NOT_NULL"), "should resolve NOT_NULL");
      assertEquals(Strategy.REGEX, Strategy.valueOf("REGEX"), "should resolve REGEX");
      assertEquals(
          Strategy.DATE_FLEXIBLE,
          Strategy.valueOf("DATE_FLEXIBLE"),
          "should resolve DATE_FLEXIBLE");
      assertEquals(
          Strategy.JSON_EQUIVALENT,
          Strategy.valueOf("JSON_EQUIVALENT"),
          "should resolve JSON_EQUIVALENT");
    }
  }
}
