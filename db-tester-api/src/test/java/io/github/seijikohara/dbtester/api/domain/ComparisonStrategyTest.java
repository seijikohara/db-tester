package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
      assertEquals(
          ComparisonStrategy.Type.STRICT,
          ComparisonStrategy.STRICT.getType(),
          "should have STRICT type");
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
      assertEquals(
          ComparisonStrategy.Type.IGNORE,
          ComparisonStrategy.IGNORE.getType(),
          "should have IGNORE type");
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
      assertEquals(
          ComparisonStrategy.Type.NUMERIC,
          ComparisonStrategy.NUMERIC.getType(),
          "should have NUMERIC type");
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
          ComparisonStrategy.Type.CASE_INSENSITIVE,
          ComparisonStrategy.CASE_INSENSITIVE.getType(),
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
          ComparisonStrategy.Type.TIMESTAMP_FLEXIBLE,
          ComparisonStrategy.TIMESTAMP_FLEXIBLE.getType(),
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
          ComparisonStrategy.Type.NOT_NULL,
          ComparisonStrategy.NOT_NULL.getType(),
          "should have NOT_NULL type");
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
      assertEquals(ComparisonStrategy.Type.REGEX, strategy.getType(), "should have REGEX type");
      assertTrue(strategy.getPattern().isPresent(), "should have pattern");
      assertEquals(
          "test.*",
          strategy.getPattern().map(p -> p.pattern()).orElse(""),
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
          ComparisonStrategy.Type.DATE_FLEXIBLE,
          ComparisonStrategy.DATE_FLEXIBLE.getType(),
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
          ComparisonStrategy.Type.JSON_EQUIVALENT,
          ComparisonStrategy.JSON_EQUIVALENT.getType(),
          "should have JSON_EQUIVALENT type");
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
