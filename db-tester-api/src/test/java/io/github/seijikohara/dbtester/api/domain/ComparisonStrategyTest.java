package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  /** Tests for DATE_FLEXIBLE strategy. */
  @Nested
  @DisplayName("DATE_FLEXIBLE strategy")
  class DateFlexibleStrategyTests {

    /** Tests for DATE_FLEXIBLE strategy. */
    DateFlexibleStrategyTests() {}

    /** Verifies that DATE_FLEXIBLE matches dates in different formats. */
    @Test
    @Tag("normal")
    @DisplayName("should match when dates are in different formats")
    void shouldMatch_whenDatesInDifferentFormats() {
      // When & Then
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15", "2024/01/15"),
          "should match ISO and slashed formats");
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024/01/15", "2024.01.15"),
          "should match slashed and dot formats");
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024.01.15", "2024-01-15"),
          "should match dot and ISO formats");
    }

    /** Verifies that DATE_FLEXIBLE extracts dates from timestamps. */
    @Test
    @Tag("normal")
    @DisplayName("should match when dates are extracted from timestamps")
    void shouldMatch_whenDatesExtractedFromTimestamps() {
      // When & Then
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15", "2024-01-15T10:30:00"),
          "should match date with timestamp");
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15T10:30:00", "2024-01-15 10:30:00"),
          "should match timestamps with different separators");
    }

    /** Verifies that DATE_FLEXIBLE does not match different dates. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when dates are different")
    void shouldNotMatch_whenDatesAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15", "2024-01-16"),
          "should not match different dates");
      assertFalse(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15", "2024/02/15"),
          "should not match different months");
    }

    /** Verifies that DATE_FLEXIBLE handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches(null, null), "should match null with null");
      assertFalse(
          ComparisonStrategy.DATE_FLEXIBLE.matches(null, "2024-01-15"),
          "should not match null with date");
      assertFalse(
          ComparisonStrategy.DATE_FLEXIBLE.matches("2024-01-15", null),
          "should not match date with null");
    }

    /** Verifies that DATE_FLEXIBLE falls back to string comparison for invalid formats. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to string comparison when format is invalid")
    void shouldFallBackToStringComparison_whenFormatIsInvalid() {
      // When & Then
      assertTrue(
          ComparisonStrategy.DATE_FLEXIBLE.matches("invalid", "invalid"),
          "should match identical invalid formats");
      assertFalse(
          ComparisonStrategy.DATE_FLEXIBLE.matches("invalid", "other"),
          "should not match different invalid formats");
    }
  }

  /** Tests for JSON_EQUIVALENT strategy. */
  @Nested
  @DisplayName("JSON_EQUIVALENT strategy")
  class JsonEquivalentStrategyTests {

    /** Tests for JSON_EQUIVALENT strategy. */
    JsonEquivalentStrategyTests() {}

    /** Verifies that JSON_EQUIVALENT matches JSON with different key order. */
    @Test
    @Tag("normal")
    @DisplayName("should match when JSON objects have different key order")
    void shouldMatch_whenJsonHasDifferentKeyOrder() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches("{\"b\":2,\"a\":1}", "{\"a\":1,\"b\":2}"),
          "should match objects with different key order");
    }

    /** Verifies that JSON_EQUIVALENT matches JSON with whitespace differences. */
    @Test
    @Tag("normal")
    @DisplayName("should match when JSON has whitespace differences")
    void shouldMatch_whenJsonHasWhitespaceDifferences() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches(
              "{\"name\":\"John\"}", "{ \"name\" : \"John\" }"),
          "should match JSON ignoring whitespace");
    }

    /** Verifies that JSON_EQUIVALENT matches nested JSON objects. */
    @Test
    @Tag("normal")
    @DisplayName("should match when nested JSON objects have different key order")
    void shouldMatch_whenNestedJsonHasDifferentKeyOrder() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches(
              "{\"b\":{\"d\":4,\"c\":3},\"a\":1}", "{\"a\":1,\"b\":{\"c\":3,\"d\":4}}"),
          "should match nested objects with different key order");
    }

    /** Verifies that JSON_EQUIVALENT matches JSON arrays. */
    @Test
    @Tag("normal")
    @DisplayName("should match when JSON arrays are equal")
    void shouldMatch_whenJsonArraysAreEqual() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches("[1,2,3]", "[1,2,3]"),
          "should match equal arrays");
    }

    /** Verifies that JSON_EQUIVALENT does not match different JSON. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when JSON structures are different")
    void shouldNotMatch_whenJsonStructuresAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("{\"a\":1}", "{\"a\":2}"),
          "should not match different values");
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("[1,2,3]", "[1,3,2]"),
          "should not match arrays with different order");
    }

    /** Verifies that JSON_EQUIVALENT distinguishes string values from literals. */
    @Test
    @Tag("normal")
    @DisplayName("should distinguish string values from boolean and number literals")
    void shouldDistinguishStringFromLiteral_whenValueTypeDiffers() {
      // When & Then
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("{\"value\":\"true\"}", "{\"value\":true}"),
          "should not match string \"true\" with boolean true");
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("{\"value\":\"123\"}", "{\"value\":123}"),
          "should not match string \"123\" with number 123");
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("{\"value\":\"null\"}", "{\"value\":null}"),
          "should not match string \"null\" with null literal");
    }

    /** Verifies that JSON_EQUIVALENT handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches(null, null), "should match null with null");
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches(null, "{\"a\":1}"),
          "should not match null with JSON");
    }

    /** Verifies that JSON_EQUIVALENT falls back to string comparison for non-JSON. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to string comparison for non-JSON values")
    void shouldFallBackToStringComparison_whenValuesAreNotJson() {
      // When & Then
      assertTrue(
          ComparisonStrategy.JSON_EQUIVALENT.matches("hello", "hello"),
          "should match identical non-JSON strings");
      assertFalse(
          ComparisonStrategy.JSON_EQUIVALENT.matches("hello", "world"),
          "should not match different non-JSON strings");
    }
  }

  /** Tests for CONTAINS strategy. */
  @Nested
  @DisplayName("CONTAINS strategy")
  class ContainsStrategyTests {

    /** Tests for CONTAINS strategy. */
    ContainsStrategyTests() {}

    /** Verifies that CONTAINS matches when actual contains expected. */
    @Test
    @Tag("normal")
    @DisplayName("should match when actual contains expected value")
    void shouldMatch_whenActualContainsExpected() {
      // Given
      final var strategy = ComparisonStrategy.contains();

      // When & Then
      assertTrue(
          strategy.matches("world", "hello world"), "should match when actual contains expected");
      assertTrue(
          strategy.matches("test", "this is a test string"),
          "should match substring in longer string");
    }

    /** Verifies that CONTAINS with explicit substring matches. */
    @Test
    @Tag("normal")
    @DisplayName("should match when actual contains configured substring")
    void shouldMatch_whenActualContainsConfiguredSubstring() {
      // Given
      final var strategy = ComparisonStrategy.contains("admin");

      // When & Then
      assertTrue(
          strategy.matches("ignored", "user_admin_role"),
          "should match configured substring regardless of expected");
    }

    /** Verifies that CONTAINS does not match when substring is absent. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when actual does not contain expected")
    void shouldNotMatch_whenActualDoesNotContainExpected() {
      // Given
      final var strategy = ComparisonStrategy.contains();

      // When & Then
      assertFalse(
          strategy.matches("xyz", "hello world"), "should not match when substring is absent");
    }

    /** Verifies that CONTAINS handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // Given
      final var strategy = ComparisonStrategy.contains();

      // When & Then
      assertFalse(strategy.matches("test", null), "should not match when actual is null");
      assertFalse(strategy.matches(null, "test"), "should not match when expected is null");
    }

    /** Verifies that CONTAINS has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have CONTAINS type")
    void shouldHaveContainsType_whenCreated() {
      // When & Then
      assertEquals(
          ComparisonStrategy.Type.CONTAINS,
          ComparisonStrategy.contains().getType(),
          "should have CONTAINS type");
    }
  }

  /** Tests for RANGE strategy. */
  @Nested
  @DisplayName("RANGE strategy")
  class RangeStrategyTests {

    /** Tests for RANGE strategy. */
    RangeStrategyTests() {}

    /** Verifies that RANGE matches values within range. */
    @Test
    @Tag("normal")
    @DisplayName("should match when value is within range")
    void shouldMatch_whenValueIsWithinRange() {
      // Given
      final var strategy = ComparisonStrategy.range(100.0, 200.0);

      // When & Then
      assertTrue(strategy.matches(null, 150), "should match integer within range");
      assertTrue(strategy.matches(null, 100), "should match min boundary");
      assertTrue(strategy.matches(null, 200), "should match max boundary");
      assertTrue(strategy.matches(null, "150.5"), "should match string number within range");
    }

    /** Verifies that RANGE does not match values outside range. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when value is outside range")
    void shouldNotMatch_whenValueIsOutsideRange() {
      // Given
      final var strategy = ComparisonStrategy.range(100.0, 200.0);

      // When & Then
      assertFalse(strategy.matches(null, 99), "should not match below min");
      assertFalse(strategy.matches(null, 201), "should not match above max");
    }

    /** Verifies that RANGE handles null actual value. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not match when actual is null")
    void shouldNotMatch_whenActualIsNull() {
      // Given
      final var strategy = ComparisonStrategy.range(0.0, 100.0);

      // When & Then
      assertFalse(strategy.matches(null, null), "should not match null actual");
    }

    /** Verifies that RANGE can be created from options string. */
    @Test
    @Tag("normal")
    @DisplayName("should create from options string")
    void shouldCreateFromOptionsString_whenValidFormat() {
      // Given
      final var strategy = ComparisonStrategy.range("min=10,max=20");

      // When & Then
      assertTrue(strategy.matches(null, 15), "should match value within range");
      assertFalse(strategy.matches(null, 25), "should not match value outside range");
    }

    /** Verifies that RANGE throws on invalid options format. */
    @Test
    @Tag("error")
    @DisplayName("should throw when options format is invalid")
    void shouldThrowException_whenOptionsFormatIsInvalid() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> ComparisonStrategy.range("invalid"),
          "should throw on invalid format");
    }

    /** Verifies that RANGE throws when min is greater than max. */
    @Test
    @Tag("error")
    @DisplayName("should throw when min is greater than max")
    void shouldThrowException_whenMinGreaterThanMax() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> ComparisonStrategy.range(200.0, 100.0),
          "should throw when min > max");
    }

    /** Verifies that RANGE from options string throws when min is greater than max. */
    @Test
    @Tag("error")
    @DisplayName("should throw when options string has min greater than max")
    void shouldThrowException_whenOptionsStringHasMinGreaterThanMax() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> ComparisonStrategy.range("min=200,max=100"),
          "should throw when options string has min > max");
    }

    /** Verifies that RANGE has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have RANGE type")
    void shouldHaveRangeType_whenCreated() {
      // When & Then
      assertEquals(
          ComparisonStrategy.Type.RANGE,
          ComparisonStrategy.range(0.0, 100.0).getType(),
          "should have RANGE type");
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

    /** Verifies that toString returns options for parameterized strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return options for parameterized strategies")
    void shouldReturnOptions_whenCalledOnParameterizedStrategy() {
      // Given
      final var rangeStrategy = ComparisonStrategy.range(10.0, 20.0);
      final var containsStrategy = ComparisonStrategy.contains("test");

      // When & Then
      assertTrue(rangeStrategy.toString().contains("RANGE"), "should contain RANGE in toString");
      assertTrue(
          containsStrategy.toString().contains("CONTAINS"), "should contain CONTAINS in toString");
    }
  }
}
