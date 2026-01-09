package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ColumnStrategyMapping}. */
@DisplayName("ColumnStrategyMapping")
class ColumnStrategyMappingTest {

  /** Tests for ColumnStrategyMapping record. */
  ColumnStrategyMappingTest() {}

  /** Tests for constructor normalization. */
  @Nested
  @DisplayName("constructor")
  class ConstructorTests {

    /** Tests for constructor. */
    ConstructorTests() {}

    /** Verifies that column name is normalized to uppercase. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize column name to uppercase")
    void shouldNormalizeColumnName_toUppercase() {
      // When
      final ColumnStrategyMapping mapping =
          new ColumnStrategyMapping("email", ComparisonStrategy.STRICT);

      // Then
      assertEquals("EMAIL", mapping.columnName(), "column name should be uppercase");
    }

    /** Verifies that mixed case column name is normalized. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize mixed case column name")
    void shouldNormalizeMixedCaseColumnName() {
      // When
      final ColumnStrategyMapping mapping =
          new ColumnStrategyMapping("CreatedAt", ComparisonStrategy.IGNORE);

      // Then
      assertEquals("CREATEDAT", mapping.columnName(), "column name should be uppercase");
    }

    /** Verifies that strategy is preserved. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve strategy")
    void shouldPreserveStrategy() {
      // When
      final ColumnStrategyMapping mapping =
          new ColumnStrategyMapping("id", ComparisonStrategy.NUMERIC);

      // Then
      assertSame(ComparisonStrategy.NUMERIC, mapping.strategy(), "strategy should be preserved");
    }
  }

  /** Tests for factory methods. */
  @Nested
  @DisplayName("factory methods")
  class FactoryMethodTests {

    /** Tests for factory methods. */
    FactoryMethodTests() {}

    /** Verifies of() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create mapping with of()")
    void shouldCreateMapping_withOf() {
      // When
      final ColumnStrategyMapping mapping =
          ColumnStrategyMapping.of("email", ComparisonStrategy.CASE_INSENSITIVE);

      // Then
      assertEquals("EMAIL", mapping.columnName(), "column name should be normalized");
      assertSame(ComparisonStrategy.CASE_INSENSITIVE, mapping.strategy(), "strategy should be set");
    }

    /** Verifies ignore() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create IGNORE mapping with ignore()")
    void shouldCreateIgnoreMapping() {
      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.ignore("created_at");

      // Then
      assertEquals("CREATED_AT", mapping.columnName(), "column name should be normalized");
      assertSame(ComparisonStrategy.IGNORE, mapping.strategy(), "strategy should be IGNORE");
    }

    /** Verifies caseInsensitive() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create CASE_INSENSITIVE mapping with caseInsensitive()")
    void shouldCreateCaseInsensitiveMapping() {
      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.caseInsensitive("name");

      // Then
      assertEquals("NAME", mapping.columnName(), "column name should be normalized");
      assertSame(
          ComparisonStrategy.CASE_INSENSITIVE,
          mapping.strategy(),
          "strategy should be CASE_INSENSITIVE");
    }

    /** Verifies numeric() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create NUMERIC mapping with numeric()")
    void shouldCreateNumericMapping() {
      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.numeric("amount");

      // Then
      assertEquals("AMOUNT", mapping.columnName(), "column name should be normalized");
      assertSame(ComparisonStrategy.NUMERIC, mapping.strategy(), "strategy should be NUMERIC");
    }

    /** Verifies timestampFlexible() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create TIMESTAMP_FLEXIBLE mapping with timestampFlexible()")
    void shouldCreateTimestampFlexibleMapping() {
      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.timestampFlexible("updated_at");

      // Then
      assertEquals("UPDATED_AT", mapping.columnName(), "column name should be normalized");
      assertSame(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE,
          mapping.strategy(),
          "strategy should be TIMESTAMP_FLEXIBLE");
    }

    /** Verifies notNull() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create NOT_NULL mapping with notNull()")
    void shouldCreateNotNullMapping() {
      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.notNull("id");

      // Then
      assertEquals("ID", mapping.columnName(), "column name should be normalized");
      assertSame(ComparisonStrategy.NOT_NULL, mapping.strategy(), "strategy should be NOT_NULL");
    }

    /** Verifies regex() factory method. */
    @Test
    @Tag("normal")
    @DisplayName("should create REGEX mapping with regex()")
    void shouldCreateRegexMapping() {
      // Given
      final String pattern = "[a-f0-9-]{36}";

      // When
      final ColumnStrategyMapping mapping = ColumnStrategyMapping.regex("uuid", pattern);

      // Then
      assertEquals("UUID", mapping.columnName(), "column name should be normalized");
      assertEquals(ComparisonStrategy.Type.REGEX, mapping.strategy().getType(), "should be REGEX");
      assertTrue(mapping.strategy().getPattern().isPresent(), "should have pattern");
      assertEquals(
          pattern, mapping.strategy().getPattern().get().pattern(), "pattern should match");
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualityTests {

    /** Tests for equals and hashCode. */
    EqualityTests() {}

    /** Verifies equality based on column name only. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when column names match")
    void shouldBeEqual_whenColumnNamesMatch() {
      // Given
      final ColumnStrategyMapping mapping1 =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);
      final ColumnStrategyMapping mapping2 =
          ColumnStrategyMapping.of("EMAIL", ComparisonStrategy.IGNORE);

      // When & Then
      assertEquals(mapping1, mapping2, "mappings with same column name should be equal");
      assertEquals(
          mapping1.hashCode(), mapping2.hashCode(), "hash codes should match for equal mappings");
    }

    /** Verifies inequality when column names differ. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when column names differ")
    void shouldNotBeEqual_whenColumnNamesDiffer() {
      // Given
      final ColumnStrategyMapping mapping1 =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);
      final ColumnStrategyMapping mapping2 =
          ColumnStrategyMapping.of("name", ComparisonStrategy.STRICT);

      // When & Then
      assertNotEquals(
          mapping1, mapping2, "mappings with different column names should not be equal");
    }

    /** Verifies reflexive equality. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      // Given
      final ColumnStrategyMapping mapping =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);

      // When & Then
      assertEquals(mapping, mapping, "mapping should be equal to itself");
    }

    /** Verifies null inequality. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not be equal to null")
    void shouldNotBeEqualToNull() {
      // Given
      final ColumnStrategyMapping mapping =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);

      // When & Then
      assertNotEquals(null, mapping, "mapping should not be equal to null");
    }

    /** Verifies different type inequality. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
      // Given
      final ColumnStrategyMapping mapping =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);

      // When & Then
      assertNotEquals("EMAIL", mapping, "mapping should not be equal to String");
    }
  }

  /** Tests for case insensitivity. */
  @Nested
  @DisplayName("case insensitivity")
  class CaseInsensitivityTests {

    /** Tests for case insensitivity. */
    CaseInsensitivityTests() {}

    /** Verifies that different cases produce same column name. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize different cases to same value")
    void shouldNormalizeDifferentCases() {
      // Given
      final ColumnStrategyMapping lower =
          ColumnStrategyMapping.of("email", ComparisonStrategy.STRICT);
      final ColumnStrategyMapping upper =
          ColumnStrategyMapping.of("EMAIL", ComparisonStrategy.STRICT);
      final ColumnStrategyMapping mixed =
          ColumnStrategyMapping.of("Email", ComparisonStrategy.STRICT);

      // When & Then
      assertEquals(lower.columnName(), upper.columnName(), "should have same column name");
      assertEquals(lower.columnName(), mixed.columnName(), "should have same column name");
      assertEquals("EMAIL", lower.columnName(), "should be uppercase");
    }
  }
}
