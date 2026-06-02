package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /** Verifies that isStrict returns true. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when isStrict called")
    void shouldReturnTrue_whenIsStrictCalled() {
      // When & Then
      assertTrue(ComparisonStrategy.STRICT.isStrict(), "should return true for isStrict");
      assertFalse(ComparisonStrategy.STRICT.isIgnore(), "should return false for isIgnore");
    }

    /** Verifies that STRICT has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have STRICT type")
    void shouldHaveStrictType_whenCreated() {
      // When & Then
      assertEquals(Strategy.STRICT, ComparisonStrategy.STRICT.type(), "should have STRICT type");
    }
  }

  /** Tests for IGNORE strategy. */
  @Nested
  @DisplayName("IGNORE strategy")
  class IgnoreStrategyTests {

    /** Tests for IGNORE strategy. */
    IgnoreStrategyTests() {}

    /** Verifies that isIgnore returns true. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when isIgnore called")
    void shouldReturnTrue_whenIsIgnoreCalled() {
      // When & Then
      assertTrue(ComparisonStrategy.IGNORE.isIgnore(), "should return true for isIgnore");
      assertFalse(ComparisonStrategy.IGNORE.isStrict(), "should return false for isStrict");
    }

    /** Verifies that IGNORE has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have IGNORE type")
    void shouldHaveIgnoreType_whenCreated() {
      // When & Then
      assertEquals(Strategy.IGNORE, ComparisonStrategy.IGNORE.type(), "should have IGNORE type");
    }
  }

  /** Tests for NUMERIC strategy. */
  @Nested
  @DisplayName("NUMERIC strategy")
  class NumericStrategyTests {

    /** Tests for NUMERIC strategy. */
    NumericStrategyTests() {}

    /** Verifies that NUMERIC has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have NUMERIC type")
    void shouldHaveNumericType_whenCreated() {
      // When & Then
      assertEquals(Strategy.NUMERIC, ComparisonStrategy.NUMERIC.type(), "should have NUMERIC type");
    }
  }

  /** Tests for CASE_INSENSITIVE strategy. */
  @Nested
  @DisplayName("CASE_INSENSITIVE strategy")
  class CaseInsensitiveStrategyTests {

    /** Tests for CASE_INSENSITIVE strategy. */
    CaseInsensitiveStrategyTests() {}

    /** Verifies that CASE_INSENSITIVE has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have CASE_INSENSITIVE type")
    void shouldHaveCaseInsensitiveType_whenCreated() {
      // When & Then
      assertEquals(
          Strategy.CASE_INSENSITIVE,
          ComparisonStrategy.CASE_INSENSITIVE.type(),
          "should have CASE_INSENSITIVE type");
    }
  }

  /** Tests for TIMESTAMP_FLEXIBLE strategy. */
  @Nested
  @DisplayName("TIMESTAMP_FLEXIBLE strategy")
  class TimestampFlexibleStrategyTests {

    /** Tests for TIMESTAMP_FLEXIBLE strategy. */
    TimestampFlexibleStrategyTests() {}

    /** Verifies that TIMESTAMP_FLEXIBLE has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have TIMESTAMP_FLEXIBLE type")
    void shouldHaveTimestampFlexibleType_whenCreated() {
      // When & Then
      assertEquals(
          Strategy.TIMESTAMP_FLEXIBLE,
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.type(),
          "should have TIMESTAMP_FLEXIBLE type");
    }
  }

  /** Tests for NOT_NULL strategy. */
  @Nested
  @DisplayName("NOT_NULL strategy")
  class NotNullStrategyTests {

    /** Tests for NOT_NULL strategy. */
    NotNullStrategyTests() {}

    /** Verifies that NOT_NULL has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have NOT_NULL type")
    void shouldHaveNotNullType_whenCreated() {
      // When & Then
      assertEquals(
          Strategy.NOT_NULL, ComparisonStrategy.NOT_NULL.type(), "should have NOT_NULL type");
    }
  }

  /** Tests for REGEX strategy. */
  @Nested
  @DisplayName("REGEX strategy")
  class RegexStrategyTests {

    /** Tests for REGEX strategy. */
    RegexStrategyTests() {}

    /** Verifies that REGEX has correct type and pattern. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct type and pattern")
    void shouldHaveCorrectTypeAndPattern_whenCreated() {
      // Given
      final var strategy = ComparisonStrategy.regex("test.*");

      // When & Then
      assertEquals(Strategy.REGEX, strategy.type(), "should have REGEX type");
      assertTrue(strategy.pattern().isPresent(), "should have pattern");
      assertEquals(
          "test.*",
          strategy.pattern().map(p -> p.pattern()).orElse(""),
          "should have correct pattern string");
    }
  }

  /** Tests for DATE_FLEXIBLE strategy. */
  @Nested
  @DisplayName("DATE_FLEXIBLE strategy")
  class DateFlexibleStrategyTests {

    /** Tests for DATE_FLEXIBLE strategy. */
    DateFlexibleStrategyTests() {}

    /** Verifies that DATE_FLEXIBLE has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have DATE_FLEXIBLE type")
    void shouldHaveDateFlexibleType_whenCreated() {
      // When & Then
      assertEquals(
          Strategy.DATE_FLEXIBLE,
          ComparisonStrategy.DATE_FLEXIBLE.type(),
          "should have DATE_FLEXIBLE type");
    }
  }

  /** Tests for JSON_EQUIVALENT strategy. */
  @Nested
  @DisplayName("JSON_EQUIVALENT strategy")
  class JsonEquivalentStrategyTests {

    /** Tests for JSON_EQUIVALENT strategy. */
    JsonEquivalentStrategyTests() {}

    /** Verifies that JSON_EQUIVALENT has correct type. */
    @Test
    @Tag("normal")
    @DisplayName("should have JSON_EQUIVALENT type")
    void shouldHaveJsonEquivalentType_whenCreated() {
      // When & Then
      assertEquals(
          Strategy.JSON_EQUIVALENT,
          ComparisonStrategy.JSON_EQUIVALENT.type(),
          "should have JSON_EQUIVALENT type");
    }
  }

  /** Tests for the of() factory methods. */
  @Nested
  @DisplayName("of(Strategy) and of(Strategy, String) methods")
  class OfMethod {

    /** Tests for the of methods. */
    OfMethod() {}

    /** Verifies that of returns the shared constant for non-regex strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should return shared constant when non-regex strategy")
    void shouldReturnSharedConstant_whenNonRegexStrategy() {
      // When & Then
      assertSame(ComparisonStrategy.STRICT, ComparisonStrategy.of(Strategy.STRICT), "STRICT");
      assertSame(ComparisonStrategy.IGNORE, ComparisonStrategy.of(Strategy.IGNORE), "IGNORE");
      assertSame(ComparisonStrategy.NUMERIC, ComparisonStrategy.of(Strategy.NUMERIC), "NUMERIC");
      assertSame(
          ComparisonStrategy.CASE_INSENSITIVE,
          ComparisonStrategy.of(Strategy.CASE_INSENSITIVE),
          "CASE_INSENSITIVE");
      assertSame(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE,
          ComparisonStrategy.of(Strategy.TIMESTAMP_FLEXIBLE),
          "TIMESTAMP_FLEXIBLE");
      assertSame(
          ComparisonStrategy.DATE_FLEXIBLE,
          ComparisonStrategy.of(Strategy.DATE_FLEXIBLE),
          "DATE_FLEXIBLE");
      assertSame(
          ComparisonStrategy.JSON_EQUIVALENT,
          ComparisonStrategy.of(Strategy.JSON_EQUIVALENT),
          "JSON_EQUIVALENT");
      assertSame(ComparisonStrategy.NOT_NULL, ComparisonStrategy.of(Strategy.NOT_NULL), "NOT_NULL");
    }

    /** Verifies that of ignores the pattern for non-regex strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should ignore pattern when non-regex strategy")
    void shouldIgnorePattern_whenNonRegexStrategy() {
      // When & Then
      assertSame(
          ComparisonStrategy.STRICT,
          ComparisonStrategy.of(Strategy.STRICT, "ignored"),
          "pattern is ignored for STRICT");
    }

    /** Verifies that of builds a regex strategy when a pattern is supplied. */
    @Test
    @Tag("normal")
    @DisplayName("should build regex strategy when pattern supplied")
    void shouldBuildRegexStrategy_whenPatternSupplied() {
      // When
      final var strategy = ComparisonStrategy.of(Strategy.REGEX, "\\d+");

      // Then
      assertEquals(Strategy.REGEX, strategy.type(), "should have REGEX type");
      assertEquals(
          "\\d+", strategy.pattern().map(p -> p.pattern()).orElse(""), "should keep the pattern");
    }

    /** Verifies that of(Strategy) rejects REGEX without a pattern. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when regex without pattern")
    void shouldThrowException_whenRegexWithoutPattern() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> ComparisonStrategy.of(Strategy.REGEX),
          "REGEX without a pattern should throw");
      assertThrows(
          IllegalArgumentException.class,
          () -> ComparisonStrategy.of(Strategy.REGEX, " "),
          "REGEX with a blank pattern should throw");
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
