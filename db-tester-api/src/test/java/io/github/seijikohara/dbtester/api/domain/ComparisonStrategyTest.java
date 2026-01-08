package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ComparisonStrategy}. */
@DisplayName("ComparisonStrategy")
class ComparisonStrategyTest {

  /** Tests for the ComparisonStrategy class. */
  ComparisonStrategyTest() {}

  /** Tests for STRICT strategy. */
  @Nested
  @DisplayName("STRICT strategy")
  class StrictStrategyTests {

    /** Tests for STRICT strategy. */
    StrictStrategyTests() {}

    /** Verifies that STRICT matches equal objects. */
    @Test
    @DisplayName("matches equal objects")
    void matchesEqualObjects() {
      assertTrue(ComparisonStrategy.STRICT.matches("hello", "hello"));
      assertTrue(ComparisonStrategy.STRICT.matches(123, 123));
      assertTrue(ComparisonStrategy.STRICT.matches(null, null));
    }

    /** Verifies that STRICT does not match different objects. */
    @Test
    @DisplayName("does not match different objects")
    void doesNotMatchDifferentObjects() {
      assertFalse(ComparisonStrategy.STRICT.matches("hello", "world"));
      assertFalse(ComparisonStrategy.STRICT.matches(123, 456));
      assertFalse(ComparisonStrategy.STRICT.matches("123", 123));
      assertFalse(ComparisonStrategy.STRICT.matches(null, "hello"));
    }

    /** Verifies that isStrict returns true. */
    @Test
    @DisplayName("isStrict returns true")
    void isStrictReturnsTrue() {
      assertTrue(ComparisonStrategy.STRICT.isStrict());
      assertFalse(ComparisonStrategy.STRICT.isIgnore());
    }
  }

  /** Tests for IGNORE strategy. */
  @Nested
  @DisplayName("IGNORE strategy")
  class IgnoreStrategyTests {

    /** Tests for IGNORE strategy. */
    IgnoreStrategyTests() {}

    /** Verifies that IGNORE always matches. */
    @Test
    @DisplayName("always matches")
    void alwaysMatches() {
      assertTrue(ComparisonStrategy.IGNORE.matches("hello", "world"));
      assertTrue(ComparisonStrategy.IGNORE.matches(null, 123));
      assertTrue(ComparisonStrategy.IGNORE.matches("anything", null));
    }

    /** Verifies that isIgnore returns true. */
    @Test
    @DisplayName("isIgnore returns true")
    void isIgnoreReturnsTrue() {
      assertTrue(ComparisonStrategy.IGNORE.isIgnore());
      assertFalse(ComparisonStrategy.IGNORE.isStrict());
    }
  }

  /** Tests for NUMERIC strategy. */
  @Nested
  @DisplayName("NUMERIC strategy")
  class NumericStrategyTests {

    /** Tests for NUMERIC strategy. */
    NumericStrategyTests() {}

    /** Verifies that NUMERIC matches equal numbers. */
    @Test
    @DisplayName("matches equal numbers")
    void matchesEqualNumbers() {
      assertTrue(ComparisonStrategy.NUMERIC.matches(123, 123));
      assertTrue(ComparisonStrategy.NUMERIC.matches(123L, 123));
      assertTrue(
          ComparisonStrategy.NUMERIC.matches(new BigDecimal("99.99"), new BigDecimal("99.99")));
    }

    /** Verifies that NUMERIC matches string and number representations. */
    @Test
    @DisplayName("matches string and number representations")
    void matchesStringAndNumber() {
      assertTrue(ComparisonStrategy.NUMERIC.matches("123", 123));
      assertTrue(ComparisonStrategy.NUMERIC.matches("99.99", new BigDecimal("99.99")));
      assertTrue(ComparisonStrategy.NUMERIC.matches(123, "123"));
    }

    /** Verifies that NUMERIC does not match different numbers. */
    @Test
    @DisplayName("does not match different numbers")
    void doesNotMatchDifferentNumbers() {
      assertFalse(ComparisonStrategy.NUMERIC.matches(123, 456));
      assertFalse(ComparisonStrategy.NUMERIC.matches("123", "456"));
    }

    /** Verifies that NUMERIC handles null values. */
    @Test
    @DisplayName("handles null values")
    void handlesNullValues() {
      assertTrue(ComparisonStrategy.NUMERIC.matches(null, null));
      assertFalse(ComparisonStrategy.NUMERIC.matches(null, 123));
      assertFalse(ComparisonStrategy.NUMERIC.matches(123, null));
    }
  }

  /** Tests for CASE_INSENSITIVE strategy. */
  @Nested
  @DisplayName("CASE_INSENSITIVE strategy")
  class CaseInsensitiveStrategyTests {

    /** Tests for CASE_INSENSITIVE strategy. */
    CaseInsensitiveStrategyTests() {}

    /** Verifies that CASE_INSENSITIVE matches strings ignoring case. */
    @Test
    @DisplayName("matches strings ignoring case")
    void matchesStringsIgnoringCase() {
      assertTrue(ComparisonStrategy.CASE_INSENSITIVE.matches("Hello", "HELLO"));
      assertTrue(ComparisonStrategy.CASE_INSENSITIVE.matches("WORLD", "world"));
      assertTrue(ComparisonStrategy.CASE_INSENSITIVE.matches("Test", "test"));
    }

    /** Verifies that CASE_INSENSITIVE does not match different strings. */
    @Test
    @DisplayName("does not match different strings")
    void doesNotMatchDifferentStrings() {
      assertFalse(ComparisonStrategy.CASE_INSENSITIVE.matches("Hello", "World"));
    }

    /** Verifies that CASE_INSENSITIVE handles null values. */
    @Test
    @DisplayName("handles null values")
    void handlesNullValues() {
      assertTrue(ComparisonStrategy.CASE_INSENSITIVE.matches(null, null));
      assertFalse(ComparisonStrategy.CASE_INSENSITIVE.matches(null, "hello"));
      assertFalse(ComparisonStrategy.CASE_INSENSITIVE.matches("hello", null));
    }
  }

  /** Tests for TIMESTAMP_FLEXIBLE strategy. */
  @Nested
  @DisplayName("TIMESTAMP_FLEXIBLE strategy")
  class TimestampFlexibleStrategyTests {

    /** Tests for TIMESTAMP_FLEXIBLE strategy. */
    TimestampFlexibleStrategyTests() {}

    /** Verifies that TIMESTAMP_FLEXIBLE matches timestamps ignoring fractional seconds. */
    @Test
    @DisplayName("matches timestamps ignoring fractional seconds")
    void matchesTimestampsIgnoringFractionalSeconds() {
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15 10:30:00.123"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.000", "2024-01-15 10:30:00"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00.123456789Z", "2024-01-15T10:30:00Z"));
    }

    /** Verifies that TIMESTAMP_FLEXIBLE properly converts timezones to UTC for comparison. */
    @Test
    @DisplayName("matches timestamps representing same instant in different timezones")
    void matchesTimestampsRepresentingSameInstantInDifferentTimezones() {
      // JST (UTC+9) 10:30 = UTC 01:30
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00+09:00", "2024-01-15T01:30:00Z"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00+09:00", "2024-01-15 01:30:00Z"));

      // EST (UTC-5) 10:30 = UTC 15:30
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00-05:00", "2024-01-15T15:30:00Z"));

      // JST 19:30 = EST 05:30 (same instant)
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T19:30:00+09:00", "2024-01-15T05:30:00-05:00"));
    }

    /** Verifies that timestamps without timezone are treated as UTC. */
    @Test
    @DisplayName("treats timestamps without timezone as UTC")
    void treatsTimestampsWithoutTimezoneAsUtc() {
      // Timestamp without timezone should match UTC timestamp with same time
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15T10:30:00Z"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00", "2024-01-15 10:30:00Z"));

      // Should NOT match different timezone that results in different UTC time
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15T10:30:00+09:00"));
    }

    /** Verifies that TIMESTAMP_FLEXIBLE does not match different timestamps. */
    @Test
    @DisplayName("does not match different timestamps")
    void doesNotMatchDifferentTimestamps() {
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15 10:31:00"));
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00Z", "2024-01-15T10:30:01Z"));
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles null values correctly. */
    @Test
    @DisplayName("handles null values")
    void handlesNullValues() {
      assertTrue(ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(null, null));
      assertFalse(ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(null, "2024-01-15 10:30:00"));
      assertFalse(ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("2024-01-15 10:30:00", null));
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles various ISO-8601 formats. */
    @Test
    @DisplayName("handles various ISO-8601 formats")
    void handlesVariousIso8601Formats() {
      // T separator vs space separator
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00Z", "2024-01-15 10:30:00Z"));

      // With and without seconds
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30Z", "2024-01-15T10:30:00Z"));

      // Different fractional second precisions
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00.1Z", "2024-01-15T10:30:00.123456Z"));
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles java.sql.Timestamp.toString() format. */
    @Test
    @DisplayName("handles java.sql.Timestamp.toString() format")
    void handlesJavaSqlTimestampFormat() {
      // java.sql.Timestamp.toString() produces format like "2024-01-15 10:30:00.0"
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15 10:30:00"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15T10:30:00Z"));
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.123", "2024-01-15 10:30:00.0"));

      // Comparing java.sql.Timestamp format with OffsetDateTime format
      // Both should be treated as UTC when no timezone is specified
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15 10:30:00.123456"));
    }
  }

  /** Tests for NOT_NULL strategy. */
  @Nested
  @DisplayName("NOT_NULL strategy")
  class NotNullStrategyTests {

    /** Tests for NOT_NULL strategy. */
    NotNullStrategyTests() {}

    /** Verifies that NOT_NULL matches when actual is not null. */
    @Test
    @DisplayName("matches when actual is not null")
    void matchesWhenActualIsNotNull() {
      assertTrue(ComparisonStrategy.NOT_NULL.matches("anything", "hello"));
      assertTrue(ComparisonStrategy.NOT_NULL.matches(null, 123));
      assertTrue(ComparisonStrategy.NOT_NULL.matches("expected", "actual"));
    }

    /** Verifies that NOT_NULL does not match when actual is null. */
    @Test
    @DisplayName("does not match when actual is null")
    void doesNotMatchWhenActualIsNull() {
      assertFalse(ComparisonStrategy.NOT_NULL.matches("hello", null));
      assertFalse(ComparisonStrategy.NOT_NULL.matches(null, null));
    }
  }

  /** Tests for REGEX strategy. */
  @Nested
  @DisplayName("REGEX strategy")
  class RegexStrategyTests {

    /** Tests for REGEX strategy. */
    RegexStrategyTests() {}

    /** Verifies that REGEX matches values against pattern. */
    @Test
    @DisplayName("matches values against pattern")
    void matchesValuesAgainstPattern() {
      final var strategy = ComparisonStrategy.regex("[a-f0-9-]{36}");
      assertTrue(strategy.matches(null, "a1b2c3d4-e5f6-7890-abcd-ef1234567890"));
    }

    /** Verifies that REGEX does not match non-matching values. */
    @Test
    @DisplayName("does not match non-matching values")
    void doesNotMatchNonMatchingValues() {
      final var strategy = ComparisonStrategy.regex("\\d+");
      assertFalse(strategy.matches(null, "abc"));
    }

    /** Verifies that REGEX does not match null actual value. */
    @Test
    @DisplayName("does not match null actual value")
    void doesNotMatchNullActualValue() {
      final var strategy = ComparisonStrategy.regex(".*");
      assertFalse(strategy.matches("expected", null));
    }

    /** Verifies that REGEX has correct type and pattern. */
    @Test
    @DisplayName("has correct type and pattern")
    void hasCorrectTypeAndPattern() {
      final var strategy = ComparisonStrategy.regex("test.*");
      assertEquals(ComparisonStrategy.Type.REGEX, strategy.getType());
      assertNotNull(strategy.getPattern());
      assertEquals("test.*", strategy.getPattern().pattern());
    }
  }

  /** Tests for equality and hashCode. */
  @Nested
  @DisplayName("equality and hashCode")
  class EqualityTests {

    /** Tests for equality and hashCode. */
    EqualityTests() {}

    /** Verifies that same strategy constants are equal. */
    @Test
    @DisplayName("same strategy constants are equal")
    void sameStrategyConstantsAreEqual() {
      assertSame(ComparisonStrategy.STRICT, ComparisonStrategy.STRICT);
      assertSame(ComparisonStrategy.IGNORE, ComparisonStrategy.IGNORE);
    }

    /** Verifies that regex strategies with same pattern are equal. */
    @Test
    @DisplayName("regex strategies with same pattern are equal")
    void regexStrategiesWithSamePatternAreEqual() {
      final var strategy1 = ComparisonStrategy.regex("test");
      final var strategy2 = ComparisonStrategy.regex("test");

      assertEquals(strategy1, strategy2);
      assertEquals(strategy1.hashCode(), strategy2.hashCode());
    }

    /** Verifies that regex strategies with different patterns are not equal. */
    @Test
    @DisplayName("regex strategies with different patterns are not equal")
    void regexStrategiesWithDifferentPatternsAreNotEqual() {
      final var strategy1 = ComparisonStrategy.regex("test");
      final var strategy2 = ComparisonStrategy.regex("other");

      assertNotEquals(strategy1, strategy2);
    }
  }

  /** Tests for toString method. */
  @Nested
  @DisplayName("toString")
  class ToStringTests {

    /** Tests for toString method. */
    ToStringTests() {}

    /** Verifies that toString returns readable representation for constants. */
    @Test
    @DisplayName("returns readable representation for constants")
    void returnsReadableRepresentationForConstants() {
      assertEquals("ComparisonStrategy[STRICT]", ComparisonStrategy.STRICT.toString());
      assertEquals("ComparisonStrategy[IGNORE]", ComparisonStrategy.IGNORE.toString());
    }

    /** Verifies that toString returns pattern for regex strategy. */
    @Test
    @DisplayName("returns pattern for regex strategy")
    void returnsPatternForRegexStrategy() {
      final var strategy = ComparisonStrategy.regex("\\d+");
      assertEquals("ComparisonStrategy[REGEX:\\d+]", strategy.toString());
    }
  }
}
