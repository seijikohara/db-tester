package io.github.seijikohara.dbtester.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.ComparisonMode;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ComparisonEngine}. */
@DisplayName("ComparisonEngine")
class ComparisonEngineTest {

  /** Tests for the ComparisonEngine class. */
  ComparisonEngineTest() {}

  /** Tests for STRICT strategy via ComparisonEngine. */
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
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, "hello", "hello"),
          "should match equal strings");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, 123, 123),
          "should match equal integers");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, null, null),
          "should match null values");
    }

    /** Verifies that STRICT does not match different objects. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when objects are different")
    void shouldNotMatch_whenObjectsAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, "hello", "world"),
          "should not match different strings");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, "123", 123),
          "should not match string and integer");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.STRICT, null, "hello"),
          "should not match null and string");
    }
  }

  /** Tests for IGNORE strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(ComparisonStrategy.IGNORE, "hello", "world"),
          "should match different strings");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.IGNORE, null, 123),
          "should match null and integer");
    }
  }

  /** Tests for NUMERIC strategy via ComparisonEngine. */
  @Nested
  @DisplayName("NUMERIC strategy")
  class NumericStrategyTests {

    /** Tests for NUMERIC strategy. */
    NumericStrategyTests() {}

    /** Verifies that NUMERIC matches equal numbers across types. */
    @Test
    @Tag("normal")
    @DisplayName("should match when numbers are equal across types")
    void shouldMatch_whenNumbersAreEqual() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.NUMERIC, 123L, 123),
          "should match Long and Integer");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.NUMERIC, "123", 123),
          "should match string and integer");
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.NUMERIC, new BigDecimal("99.99"), new BigDecimal("99.99")),
          "should match equal BigDecimals");
    }

    /** Verifies that NUMERIC handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.NUMERIC, null, null),
          "should match null with null");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.NUMERIC, null, 123),
          "should not match null with number");
    }
  }

  /** Tests for CASE_INSENSITIVE strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(ComparisonStrategy.CASE_INSENSITIVE, "Hello", "HELLO"),
          "should match 'Hello' and 'HELLO'");
    }

    /** Verifies that CASE_INSENSITIVE handles null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandle_whenNullValuesProvided() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.CASE_INSENSITIVE, null, null),
          "should match null with null");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.CASE_INSENSITIVE, null, "hello"),
          "should not match null with string");
    }
  }

  /** Tests for TIMESTAMP_FLEXIBLE strategy via ComparisonEngine. */
  @Nested
  @DisplayName("TIMESTAMP_FLEXIBLE strategy")
  class TimestampFlexibleStrategyTests {

    /** Tests for TIMESTAMP_FLEXIBLE strategy. */
    TimestampFlexibleStrategyTests() {}

    /** Verifies that TIMESTAMP_FLEXIBLE matches timestamps with timezone conversion. */
    @Test
    @Tag("normal")
    @DisplayName("should match when timestamps represent same instant in different timezones")
    void shouldMatch_whenTimestampsRepresentSameInstant() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.TIMESTAMP_FLEXIBLE,
              "2024-01-15T10:30:00+09:00",
              "2024-01-15T01:30:00Z"),
          "should match JST and UTC representing same instant");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE ignores fractional seconds. */
    @Test
    @Tag("normal")
    @DisplayName("should match when timestamps differ only in fractional seconds")
    void shouldMatch_whenTimestampsIgnoringFractionalSeconds() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.TIMESTAMP_FLEXIBLE,
              "2024-01-15 10:30:00",
              "2024-01-15 10:30:00.123"),
          "should match timestamp without and with fractional seconds");
    }

    /** Verifies that timestamps without timezone are treated as UTC. */
    @Test
    @Tag("edge-case")
    @DisplayName("should treat timestamps without timezone as UTC")
    void shouldTreatAsUtc_whenTimezoneNotSpecified() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.TIMESTAMP_FLEXIBLE, "2024-01-15 10:30:00", "2024-01-15T10:30:00Z"),
          "should match local timestamp with UTC");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE does not match different timestamps. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when timestamps are different")
    void shouldNotMatch_whenTimestampsAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonEngine.matches(
              ComparisonStrategy.TIMESTAMP_FLEXIBLE, "2024-01-15 10:30:00", "2024-01-15 10:31:00"),
          "should not match timestamps with different minutes");
    }
  }

  /** Tests for DATE_FLEXIBLE strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(ComparisonStrategy.DATE_FLEXIBLE, "2024-01-15", "2024/01/15"),
          "should match ISO and slashed formats");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.DATE_FLEXIBLE, "2024/01/15", "2024.01.15"),
          "should match slashed and dot formats");
    }

    /** Verifies that DATE_FLEXIBLE extracts dates from timestamps. */
    @Test
    @Tag("normal")
    @DisplayName("should match when dates are extracted from timestamps")
    void shouldMatch_whenDatesExtractedFromTimestamps() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.DATE_FLEXIBLE, "2024-01-15", "2024-01-15T10:30:00"),
          "should match date with timestamp");
    }

    /** Verifies that DATE_FLEXIBLE does not match different dates. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when dates are different")
    void shouldNotMatch_whenDatesAreDifferent() {
      // When & Then
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.DATE_FLEXIBLE, "2024-01-15", "2024-01-16"),
          "should not match different dates");
    }
  }

  /** Tests for JSON_EQUIVALENT strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(
              ComparisonStrategy.JSON_EQUIVALENT, "{\"b\":2,\"a\":1}", "{\"a\":1,\"b\":2}"),
          "should match objects with different key order");
    }

    /** Verifies that JSON_EQUIVALENT distinguishes string values from literals. */
    @Test
    @Tag("normal")
    @DisplayName("should distinguish string values from boolean and number literals")
    void shouldDistinguishStringFromLiteral_whenValueTypeDiffers() {
      // When & Then
      assertFalse(
          ComparisonEngine.matches(
              ComparisonStrategy.JSON_EQUIVALENT, "{\"value\":\"true\"}", "{\"value\":true}"),
          "should not match string \"true\" with boolean true");
    }

    /** Verifies that JSON_EQUIVALENT falls back to string comparison for non-JSON. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to string comparison for non-JSON values")
    void shouldFallBackToStringComparison_whenValuesAreNotJson() {
      // When & Then
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.JSON_EQUIVALENT, "hello", "hello"),
          "should match identical non-JSON strings");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.JSON_EQUIVALENT, "hello", "world"),
          "should not match different non-JSON strings");
    }
  }

  /** Tests for NOT_NULL strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(ComparisonStrategy.NOT_NULL, "anything", "hello"),
          "should match when actual is string");
      assertTrue(
          ComparisonEngine.matches(ComparisonStrategy.NOT_NULL, null, 123),
          "should match when actual is number");
    }

    /** Verifies that NOT_NULL does not match when actual is null. */
    @Test
    @Tag("normal")
    @DisplayName("should not match when actual value is null")
    void shouldNotMatch_whenActualIsNull() {
      // When & Then
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.NOT_NULL, "hello", null),
          "should not match when actual is null");
      assertFalse(
          ComparisonEngine.matches(ComparisonStrategy.NOT_NULL, null, null),
          "should not match when both are null");
    }
  }

  /** Tests for REGEX strategy via ComparisonEngine. */
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
          ComparisonEngine.matches(strategy, null, "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
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
      assertFalse(
          ComparisonEngine.matches(strategy, null, "abc"), "should not match non-numeric string");
    }

    /** Verifies that REGEX does not match null actual value. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not match when actual value is null")
    void shouldNotMatch_whenActualValueIsNull() {
      // Given
      final var strategy = ComparisonStrategy.regex(".*");

      // When & Then
      assertFalse(
          ComparisonEngine.matches(strategy, "expected", null), "should not match null actual");
    }
  }

  /** Tests for STRICT/LENIENT ComparisonMode handling around parse failures. */
  @Nested
  @DisplayName("ComparisonMode handling")
  class ComparisonModeTests {

    /** Tests for STRICT vs LENIENT mode behaviour. */
    ComparisonModeTests() {}

    /** Verifies that NUMERIC strategy throws in STRICT mode when expected is unparseable. */
    @Test
    @Tag("error")
    @DisplayName("should throw ValidationException when NUMERIC parse fails in STRICT mode")
    void shouldThrowValidationException_whenNumericParseFailsInStrictMode() {
      // When & Then
      assertThrows(
          ValidationException.class,
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.NUMERIC, "not-a-number", "100", ComparisonMode.STRICT),
          "STRICT NUMERIC should throw on non-numeric input");
    }

    /** Verifies that NUMERIC strategy falls back to equals in LENIENT mode. */
    @Test
    @Tag("normal")
    @DisplayName("should fall back to equals when NUMERIC parse fails in LENIENT mode")
    void shouldFallBackToEquals_whenNumericParseFailsInLenientMode() {
      // When & Then
      assertDoesNotThrow(
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.NUMERIC, "not-a-number", "100", ComparisonMode.LENIENT),
          "LENIENT NUMERIC should not throw on non-numeric input");
      assertTrue(
          ComparisonEngine.matches(
              ComparisonStrategy.NUMERIC, "abc", "abc", ComparisonMode.LENIENT),
          "LENIENT NUMERIC should return true when fallback equals matches");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE throws in STRICT mode when parsing fails. */
    @Test
    @Tag("error")
    @DisplayName("should throw ValidationException when TIMESTAMP_FLEXIBLE parse fails in STRICT")
    void shouldThrowValidationException_whenTimestampParseFailsInStrictMode() {
      // When & Then
      assertThrows(
          ValidationException.class,
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.TIMESTAMP_FLEXIBLE,
                  "not-a-timestamp",
                  "2024-01-01T00:00:00",
                  ComparisonMode.STRICT),
          "STRICT TIMESTAMP_FLEXIBLE should throw on unparseable timestamp");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE falls back to string comparison in LENIENT mode. */
    @Test
    @Tag("normal")
    @DisplayName("should fall back to string comparison when TIMESTAMP_FLEXIBLE parse fails")
    void shouldFallBackToString_whenTimestampParseFailsInLenientMode() {
      // When & Then
      assertDoesNotThrow(
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.TIMESTAMP_FLEXIBLE,
                  "garbage",
                  "garbage",
                  ComparisonMode.LENIENT),
          "LENIENT TIMESTAMP_FLEXIBLE should not throw on unparseable timestamp");
    }

    /** Verifies that DATE_FLEXIBLE throws in STRICT mode when parsing fails. */
    @Test
    @Tag("error")
    @DisplayName("should throw ValidationException when DATE_FLEXIBLE parse fails in STRICT mode")
    void shouldThrowValidationException_whenDateParseFailsInStrictMode() {
      // When & Then
      assertThrows(
          ValidationException.class,
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.DATE_FLEXIBLE,
                  "not-a-date",
                  "2024-01-01",
                  ComparisonMode.STRICT),
          "STRICT DATE_FLEXIBLE should throw on unparseable date");
    }

    /** Verifies that DATE_FLEXIBLE falls back to string comparison in LENIENT mode. */
    @Test
    @Tag("normal")
    @DisplayName("should fall back to string comparison when DATE_FLEXIBLE parse fails")
    void shouldFallBackToString_whenDateParseFailsInLenientMode() {
      // When & Then
      assertDoesNotThrow(
          () ->
              ComparisonEngine.matches(
                  ComparisonStrategy.DATE_FLEXIBLE, "garbage", "garbage", ComparisonMode.LENIENT),
          "LENIENT DATE_FLEXIBLE should not throw on unparseable date");
    }

    /** Verifies that the legacy matches overload defaults to STRICT mode. */
    @Test
    @Tag("normal")
    @DisplayName("should default to STRICT mode for the legacy matches overload")
    void shouldDefaultToStrict_forLegacyOverload() {
      // When & Then
      assertThrows(
          ValidationException.class,
          () -> ComparisonEngine.matches(ComparisonStrategy.NUMERIC, "not-a-number", "100"),
          "Legacy matches(strategy, expected, actual) should behave like STRICT");
    }
  }
}
