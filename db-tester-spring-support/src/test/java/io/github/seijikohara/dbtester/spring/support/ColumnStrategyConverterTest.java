package io.github.seijikohara.dbtester.spring.support;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ColumnStrategyConverter}. */
@DisplayName("ColumnStrategyConverter")
class ColumnStrategyConverterTest {

  /** Tests for the ColumnStrategyConverter class. */
  ColumnStrategyConverterTest() {}

  /** Tests for the toComparisonStrategy() method. */
  @Nested
  @DisplayName("toComparisonStrategy(Type, String) method")
  class ToComparisonStrategyMethod {

    /** Tests for the toComparisonStrategy method. */
    ToComparisonStrategyMethod() {}

    /** Verifies that STRICT type returns STRICT strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return STRICT strategy when STRICT type provided")
    void shouldReturnStrictStrategy_whenStrictTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.STRICT, null);

      // Then
      assertEquals(ComparisonStrategy.STRICT, result, "should return STRICT strategy");
    }

    /** Verifies that IGNORE type returns IGNORE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return IGNORE strategy when IGNORE type provided")
    void shouldReturnIgnoreStrategy_whenIgnoreTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.IGNORE, null);

      // Then
      assertEquals(ComparisonStrategy.IGNORE, result, "should return IGNORE strategy");
    }

    /** Verifies that NUMERIC type returns NUMERIC strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return NUMERIC strategy when NUMERIC type provided")
    void shouldReturnNumericStrategy_whenNumericTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.NUMERIC, null);

      // Then
      assertEquals(ComparisonStrategy.NUMERIC, result, "should return NUMERIC strategy");
    }

    /** Verifies that CASE_INSENSITIVE type returns CASE_INSENSITIVE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return CASE_INSENSITIVE strategy when CASE_INSENSITIVE type provided")
    void shouldReturnCaseInsensitiveStrategy_whenCaseInsensitiveTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(
              ComparisonStrategy.Type.CASE_INSENSITIVE, null);

      // Then
      assertEquals(
          ComparisonStrategy.CASE_INSENSITIVE, result, "should return CASE_INSENSITIVE strategy");
    }

    /** Verifies that TIMESTAMP_FLEXIBLE type returns TIMESTAMP_FLEXIBLE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return TIMESTAMP_FLEXIBLE strategy when TIMESTAMP_FLEXIBLE type provided")
    void shouldReturnTimestampFlexibleStrategy_whenTimestampFlexibleTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(
              ComparisonStrategy.Type.TIMESTAMP_FLEXIBLE, null);

      // Then
      assertEquals(
          ComparisonStrategy.TIMESTAMP_FLEXIBLE,
          result,
          "should return TIMESTAMP_FLEXIBLE strategy");
    }

    /** Verifies that DATE_FLEXIBLE type returns DATE_FLEXIBLE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return DATE_FLEXIBLE strategy when DATE_FLEXIBLE type provided")
    void shouldReturnDateFlexibleStrategy_whenDateFlexibleTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.DATE_FLEXIBLE, null);

      // Then
      assertEquals(
          ComparisonStrategy.DATE_FLEXIBLE, result, "should return DATE_FLEXIBLE strategy");
    }

    /** Verifies that JSON_EQUIVALENT type returns JSON_EQUIVALENT strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return JSON_EQUIVALENT strategy when JSON_EQUIVALENT type provided")
    void shouldReturnJsonEquivalentStrategy_whenJsonEquivalentTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(
              ComparisonStrategy.Type.JSON_EQUIVALENT, null);

      // Then
      assertEquals(
          ComparisonStrategy.JSON_EQUIVALENT, result, "should return JSON_EQUIVALENT strategy");
    }

    /** Verifies that NOT_NULL type returns NOT_NULL strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return NOT_NULL strategy when NOT_NULL type provided")
    void shouldReturnNotNullStrategy_whenNotNullTypeProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.NOT_NULL, null);

      // Then
      assertEquals(ComparisonStrategy.NOT_NULL, result, "should return NOT_NULL strategy");
    }

    /** Verifies that REGEX type with pattern returns REGEX strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return REGEX strategy when REGEX type with pattern provided")
    void shouldReturnRegexStrategy_whenRegexTypeWithPatternProvided() {
      // When
      final var result =
          ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.REGEX, "^[a-z]+$");

      // Then
      assertAll(
          "should return REGEX strategy with pattern",
          () ->
              assertEquals(
                  ComparisonStrategy.Type.REGEX, result.getType(), "should have REGEX type"),
          () ->
              assertEquals(
                  "^[a-z]+$",
                  result.getPattern().orElseThrow().pattern(),
                  "should have correct pattern"));
    }

    /** Verifies that REGEX type without pattern throws exception. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when REGEX type without pattern provided")
    void shouldThrowException_whenRegexTypeWithoutPatternProvided() {
      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  ColumnStrategyConverter.toComparisonStrategy(
                      ComparisonStrategy.Type.REGEX, null));

      assertNotNull(exception.getMessage(), "exception should have a message");
    }

    /** Verifies that REGEX type with blank pattern throws exception. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when REGEX type with blank pattern provided")
    void shouldThrowException_whenRegexTypeWithBlankPatternProvided() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> ColumnStrategyConverter.toComparisonStrategy(ComparisonStrategy.Type.REGEX, "  "));
    }
  }

  /** Tests for the toColumnStrategyMapping() method. */
  @Nested
  @DisplayName("toColumnStrategyMapping(String, Type, String) method")
  class ToColumnStrategyMappingMethod {

    /** Tests for the toColumnStrategyMapping method. */
    ToColumnStrategyMappingMethod() {}

    /** Verifies that mapping is created with correct column name and strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should create mapping when valid column name and strategy provided")
    void shouldCreateMapping_whenValidColumnNameAndStrategyProvided() {
      // When
      final var mapping =
          ColumnStrategyConverter.toColumnStrategyMapping(
              "CREATED_AT", ComparisonStrategy.Type.TIMESTAMP_FLEXIBLE, null);

      // Then
      assertAll(
          "mapping should have correct values",
          () -> assertEquals("CREATED_AT", mapping.columnName(), "should have correct column name"),
          () ->
              assertEquals(
                  ComparisonStrategy.TIMESTAMP_FLEXIBLE,
                  mapping.strategy(),
                  "should have correct strategy"));
    }

    /** Verifies that mapping normalizes column name to uppercase. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize column name to uppercase")
    void shouldNormalizeColumnName_toUppercase() {
      // When
      final var mapping =
          ColumnStrategyConverter.toColumnStrategyMapping(
              "created_at", ComparisonStrategy.Type.IGNORE, null);

      // Then
      assertEquals("CREATED_AT", mapping.columnName(), "should uppercase column name");
    }

    /** Verifies that mapping with REGEX type includes pattern. */
    @Test
    @Tag("normal")
    @DisplayName("should create REGEX mapping when pattern provided")
    void shouldCreateRegexMapping_whenPatternProvided() {
      // When
      final var mapping =
          ColumnStrategyConverter.toColumnStrategyMapping(
              "EMAIL", ComparisonStrategy.Type.REGEX, "^[a-z]+@[a-z]+\\.[a-z]+$");

      // Then
      assertAll(
          "mapping should have REGEX strategy with pattern",
          () -> assertEquals("EMAIL", mapping.columnName(), "should have correct column name"),
          () ->
              assertEquals(
                  ComparisonStrategy.Type.REGEX,
                  mapping.strategy().getType(),
                  "should have REGEX type"));
    }
  }

  /** Tests for the toMapEntry() method. */
  @Nested
  @DisplayName("toMapEntry(String, ColumnStrategyMapping) method")
  class ToMapEntryMethod {

    /** Tests for the toMapEntry method. */
    ToMapEntryMethod() {}

    /** Verifies that map entry has uppercase key. */
    @Test
    @Tag("normal")
    @DisplayName("should create map entry with uppercase key")
    void shouldCreateMapEntry_withUppercaseKey() {
      // Given
      final var mapping =
          ColumnStrategyConverter.toColumnStrategyMapping(
              "created_at", ComparisonStrategy.Type.IGNORE, null);

      // When
      final var entry = ColumnStrategyConverter.toMapEntry("created_at", mapping);

      // Then
      assertAll(
          "entry should have uppercase key",
          () -> assertEquals("CREATED_AT", entry.getKey(), "key should be uppercase"),
          () -> assertEquals(mapping, entry.getValue(), "value should be the mapping"));
    }
  }
}
