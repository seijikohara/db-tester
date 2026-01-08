package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
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
    @Tag("normal")
    @DisplayName("should match when objects are equal")
    void shouldMatch_whenObjectsAreEqual() {
      // When & Then
      assertTrue(ComparisonStrategy.STRICT.matches("hello", "hello"), "should match equal strings");
      assertTrue(ComparisonStrategy.STRICT.matches(123, 123), "should match equal integers");
      assertTrue(ComparisonStrategy.STRICT.matches(null, null), "should match null values");
    }

    /** Verifies that STRICT does not match different objects. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when objects are different")
    void shouldNotMatch_whenObjectsAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.STRICT.matches("hello", "world"),
          "should not match different strings");
      assertFalse(
          ComparisonStrategy.STRICT.matches(123, 456), "should not match different integers");
      assertFalse(
          ComparisonStrategy.STRICT.matches("123", 123), "should not match string and integer");
      assertFalse(
          ComparisonStrategy.STRICT.matches(null, "hello"), "should not match null and string");
    }

    /** Verifies that isStrict returns true. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when isStrict called")
    void shouldReturnTrue_whenIsStrictCalled() {
      // When & Then
      assertTrue(ComparisonStrategy.STRICT.isStrict(), "should return true for isStrict");
      assertFalse(ComparisonStrategy.STRICT.isIgnore(), "should return false for isIgnore");
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
    @Tag("normal")
    @DisplayName("should always match regardless of values")
    void shouldAlwaysMatch_whenCalled() {
      // When & Then
      assertTrue(
          ComparisonStrategy.IGNORE.matches("hello", "world"), "should match different strings");
      assertTrue(ComparisonStrategy.IGNORE.matches(null, 123), "should match null and integer");
      assertTrue(
          ComparisonStrategy.IGNORE.matches("anything", null), "should match string and null");
    }

    /** Verifies that isIgnore returns true. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when isIgnore called")
    void shouldReturnTrue_whenIsIgnoreCalled() {
      // When & Then
      assertTrue(ComparisonStrategy.IGNORE.isIgnore(), "should return true for isIgnore");
      assertFalse(ComparisonStrategy.IGNORE.isStrict(), "should return false for isStrict");
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
    @Tag("normal")
    @DisplayName("should match when numbers are equal")
    void shouldMatch_whenNumbersAreEqual() {
      // When & Then
      assertTrue(ComparisonStrategy.NUMERIC.matches(123, 123), "should match equal integers");
      assertTrue(ComparisonStrategy.NUMERIC.matches(123L, 123), "should match Long and Integer");
      assertTrue(
          ComparisonStrategy.NUMERIC.matches(new BigDecimal("99.99"), new BigDecimal("99.99")),
          "should match equal BigDecimals");
    }

    /** Verifies that NUMERIC matches string and number representations. */
    @Test
    @Tag("normal")
    @DisplayName("should match when string represents same number")
    void shouldMatch_whenStringRepresentsSameNumber() {
      // When & Then
      assertTrue(
          ComparisonStrategy.NUMERIC.matches("123", 123),
          "should match string '123' and integer 123");
      assertTrue(
          ComparisonStrategy.NUMERIC.matches("99.99", new BigDecimal("99.99")),
          "should match string and BigDecimal");
      assertTrue(ComparisonStrategy.NUMERIC.matches(123, "123"), "should match integer and string");
    }

    /** Verifies that NUMERIC does not match different numbers. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when numbers are different")
    void shouldNotMatch_whenNumbersAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.NUMERIC.matches(123, 456), "should not match different integers");
      assertFalse(
          ComparisonStrategy.NUMERIC.matches("123", "456"),
          "should not match different number strings");
    }

    /** Verifies that NUMERIC handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(ComparisonStrategy.NUMERIC.matches(null, null), "should match null with null");
      assertFalse(
          ComparisonStrategy.NUMERIC.matches(null, 123), "should not match null with number");
      assertFalse(
          ComparisonStrategy.NUMERIC.matches(123, null), "should not match number with null");
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
    @Tag("normal")
    @DisplayName("should match when strings differ only in case")
    void shouldMatch_whenStringsIgnoringCase() {
      // When & Then
      assertTrue(
          ComparisonStrategy.CASE_INSENSITIVE.matches("Hello", "HELLO"),
          "should match 'Hello' and 'HELLO'");
      assertTrue(
          ComparisonStrategy.CASE_INSENSITIVE.matches("WORLD", "world"),
          "should match 'WORLD' and 'world'");
      assertTrue(
          ComparisonStrategy.CASE_INSENSITIVE.matches("Test", "test"),
          "should match 'Test' and 'test'");
    }

    /** Verifies that CASE_INSENSITIVE does not match different strings. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when strings are different")
    void shouldNotMatch_whenStringsAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.CASE_INSENSITIVE.matches("Hello", "World"),
          "should not match different strings");
    }

    /** Verifies that CASE_INSENSITIVE handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonStrategy.CASE_INSENSITIVE.matches(null, null), "should match null with null");
      assertFalse(
          ComparisonStrategy.CASE_INSENSITIVE.matches(null, "hello"),
          "should not match null with string");
      assertFalse(
          ComparisonStrategy.CASE_INSENSITIVE.matches("hello", null),
          "should not match string with null");
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
    @Tag("normal")
    @DisplayName("should match when timestamps differ only in fractional seconds")
    void shouldMatch_whenTimestampsIgnoringFractionalSeconds() {
      // When & Then
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15 10:30:00.123"),
          "should match timestamp without and with fractional seconds");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.000", "2024-01-15 10:30:00"),
          "should match timestamp with .000 and without");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00.123456789Z", "2024-01-15T10:30:00Z"),
          "should match ISO timestamps ignoring fractional seconds");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE properly converts timezones to UTC for comparison. */
    @Test
    @Tag("normal")
    @DisplayName("should match when timestamps represent same instant in different timezones")
    void shouldMatch_whenTimestampsRepresentSameInstantInDifferentTimezones() {
      // When & Then
      // JST (UTC+9) 10:30 = UTC 01:30
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00+09:00", "2024-01-15T01:30:00Z"),
          "should match JST and UTC representing same instant");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00+09:00", "2024-01-15 01:30:00Z"),
          "should match space-separated JST and UTC");

      // EST (UTC-5) 10:30 = UTC 15:30
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00-05:00", "2024-01-15T15:30:00Z"),
          "should match EST and UTC representing same instant");

      // JST 19:30 = EST 05:30 (same instant)
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T19:30:00+09:00", "2024-01-15T05:30:00-05:00"),
          "should match JST and EST representing same instant");
    }

    /** Verifies that timestamps without timezone are treated as UTC. */
    @Test
    @Tag("edge-case")
    @DisplayName("should treat timestamps without timezone as UTC")
    void shouldTreatAsUtc_whenTimezoneNotSpecified() {
      // When & Then
      // Timestamp without timezone should match UTC timestamp with same time
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15T10:30:00Z"),
          "should match local timestamp with UTC");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00", "2024-01-15 10:30:00Z"),
          "should match T-separated local with space-separated UTC");

      // Should NOT match different timezone that results in different UTC time
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15T10:30:00+09:00"),
          "should not match local time with different timezone");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE does not match different timestamps. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when timestamps are different")
    void shouldNotMatch_whenTimestampsAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00", "2024-01-15 10:31:00"),
          "should not match timestamps with different minutes");
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00Z", "2024-01-15T10:30:01Z"),
          "should not match timestamps with different seconds");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles null values correctly. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(null, null), "should match null with null");
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(null, "2024-01-15 10:30:00"),
          "should not match null with timestamp");
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("2024-01-15 10:30:00", null),
          "should not match timestamp with null");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles various ISO-8601 formats. */
    @Test
    @Tag("normal")
    @DisplayName("should handle various ISO-8601 formats")
    void shouldHandle_whenVariousIso8601FormatsProvided() {
      // When & Then
      // T separator vs space separator
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00Z", "2024-01-15 10:30:00Z"),
          "should match T-separated and space-separated");

      // With and without seconds
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30Z", "2024-01-15T10:30:00Z"),
          "should match with and without seconds");

      // Different fractional second precisions
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15T10:30:00.1Z", "2024-01-15T10:30:00.123456Z"),
          "should match different fractional second precisions");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE handles java.sql.Timestamp.toString() format. */
    @Test
    @Tag("normal")
    @DisplayName("should handle java.sql.Timestamp.toString() format")
    void shouldHandle_whenJavaSqlTimestampFormatProvided() {
      // When & Then
      // java.sql.Timestamp.toString() produces format like "2024-01-15 10:30:00.0"
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15 10:30:00"),
          "should match Timestamp format with regular format");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15T10:30:00Z"),
          "should match Timestamp format with ISO format");
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.123", "2024-01-15 10:30:00.0"),
          "should match different Timestamp formats");

      // Comparing java.sql.Timestamp format with OffsetDateTime format
      // Both should be treated as UTC when no timezone is specified
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches(
              "2024-01-15 10:30:00.0", "2024-01-15 10:30:00.123456"),
          "should match different precisions");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE falls back to string comparison for invalid formats. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to string comparison when format is invalid")
    void shouldFallBackToStringComparison_whenFormatIsInvalid() {
      // When & Then
      // Invalid formats should fall back to string equals comparison
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("invalid-timestamp", "invalid-timestamp"),
          "should match identical invalid formats");
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("invalid-timestamp", "other-invalid"),
          "should not match different invalid formats");

      // Partial timestamps (date only) - should fall back to string comparison
      assertTrue(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("2024-01-15", "2024-01-15"),
          "should match identical date-only strings");
      assertFalse(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.matches("2024-01-15", "2024-01-16"),
          "should not match different date-only strings");
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
    @Tag("normal")
    @DisplayName("should match when actual value is not null")
    void shouldMatch_whenActualIsNotNull() {
      // When & Then
      assertTrue(
          ComparisonStrategy.NOT_NULL.matches("anything", "hello"),
          "should match when actual is string");
      assertTrue(
          ComparisonStrategy.NOT_NULL.matches(null, 123), "should match when actual is number");
      assertTrue(
          ComparisonStrategy.NOT_NULL.matches("expected", "actual"),
          "should match any non-null actual");
    }

    /** Verifies that NOT_NULL does not match when actual is null. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when actual value is null")
    void shouldNotMatch_whenActualIsNull() {
      // When & Then
      assertFalse(
          ComparisonStrategy.NOT_NULL.matches("hello", null),
          "should not match when actual is null");
      assertFalse(
          ComparisonStrategy.NOT_NULL.matches(null, null), "should not match when both are null");
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
    @Tag("normal")
    @DisplayName("should match when value matches pattern")
    void shouldMatch_whenValueMatchesPattern() {
      // Given
      final var strategy = ComparisonStrategy.regex("[a-f0-9-]{36}");

      // When & Then
      assertTrue(
          strategy.matches(null, "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
          "should match UUID pattern");
    }

    /** Verifies that REGEX does not match non-matching values. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when value does not match pattern")
    void shouldNotMatch_whenValueDoesNotMatchPattern() {
      // Given
      final var strategy = ComparisonStrategy.regex("\\d+");

      // When & Then
      assertFalse(strategy.matches(null, "abc"), "should not match non-numeric string");
    }

    /** Verifies that REGEX does not match null actual value. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not match when actual value is null")
    void shouldNotMatch_whenActualValueIsNull() {
      // Given
      final var strategy = ComparisonStrategy.regex(".*");

      // When & Then
      assertFalse(strategy.matches("expected", null), "should not match null actual");
    }

    /** Verifies that REGEX has correct type and pattern. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct type and pattern")
    void shouldHaveCorrectTypeAndPattern_whenCreated() {
      // Given
      final var strategy = ComparisonStrategy.regex("test.*");

      // When & Then
      assertEquals(ComparisonStrategy.Type.REGEX, strategy.getType(), "should have REGEX type");
      assertTrue(strategy.getPattern().isPresent(), "should have pattern");
      assertEquals(
          "test.*",
          strategy.getPattern().map(p -> p.pattern()).orElse(""),
          "should have correct pattern string");
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
    @Tag("normal")
    @DisplayName("should be same instance when using constants")
    void shouldBeSameInstance_whenUsingConstants() {
      // When & Then
      assertSame(
          ComparisonStrategy.STRICT, ComparisonStrategy.STRICT, "STRICT should be same instance");
      assertSame(
          ComparisonStrategy.IGNORE, ComparisonStrategy.IGNORE, "IGNORE should be same instance");
    }

    /** Verifies that regex strategies with same pattern are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when regex patterns are same")
    void shouldBeEqual_whenRegexPatternsAreSame() {
      // Given
      final var strategy1 = ComparisonStrategy.regex("test");
      final var strategy2 = ComparisonStrategy.regex("test");

      // When & Then
      assertEquals(strategy1, strategy2, "strategies with same pattern should be equal");
      assertEquals(
          strategy1.hashCode(),
          strategy2.hashCode(),
          "strategies with same pattern should have same hashCode");
    }

    /** Verifies that regex strategies with different patterns are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when regex patterns are different")
    void shouldNotBeEqual_whenRegexPatternsAreDifferent() {
      // Given
      final var strategy1 = ComparisonStrategy.regex("test");
      final var strategy2 = ComparisonStrategy.regex("other");

      // When & Then
      assertNotEquals(
          strategy1, strategy2, "strategies with different patterns should not be equal");
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
    @Tag("normal")
    @DisplayName("should return readable representation for constants")
    void shouldReturnReadableRepresentation_whenCalledOnConstants() {
      // When & Then
      assertEquals(
          "ComparisonStrategy[STRICT]",
          ComparisonStrategy.STRICT.toString(),
          "should return STRICT representation");
      assertEquals(
          "ComparisonStrategy[IGNORE]",
          ComparisonStrategy.IGNORE.toString(),
          "should return IGNORE representation");
    }

    /** Verifies that toString returns pattern for regex strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return pattern for regex strategy")
    void shouldReturnPattern_whenCalledOnRegexStrategy() {
      // Given
      final var strategy = ComparisonStrategy.regex("\\d+");

      // When & Then
      assertEquals(
          "ComparisonStrategy[REGEX:\\d+]",
          strategy.toString(),
          "should return REGEX representation with pattern");
    }
  }
}
