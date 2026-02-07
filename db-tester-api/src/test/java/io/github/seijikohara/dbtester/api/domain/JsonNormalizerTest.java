package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JsonNormalizer}. */
@DisplayName("JsonNormalizer")
class JsonNormalizerTest {

  /** Tests for the JsonNormalizer class. */
  JsonNormalizerTest() {}

  /** Tests for the normalize() method. */
  @Nested
  @DisplayName("normalize(String) method")
  class NormalizeMethod {

    /** Tests for the normalize method. */
    NormalizeMethod() {}

    /** Verifies that normalize sorts object keys. */
    @Test
    @Tag("normal")
    @DisplayName("should sort object keys alphabetically")
    void shouldSortObjectKeys_whenObjectHasMultipleKeys() {
      // Given
      final var input = "{\"b\":2,\"a\":1,\"c\":3}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"a\":1,\"b\":2,\"c\":3}", result, "should sort keys alphabetically");
    }

    /** Verifies that normalize removes insignificant whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should remove insignificant whitespace")
    void shouldRemoveWhitespace_whenInputHasExtraSpaces() {
      // Given
      final var input = "{ \"a\" : 1 , \"b\" : 2 }";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"a\":1,\"b\":2}", result, "should remove whitespace");
    }

    /** Verifies that normalize handles nested objects. */
    @Test
    @Tag("normal")
    @DisplayName("should sort keys in nested objects")
    void shouldSortNestedKeys_whenObjectsAreNested() {
      // Given
      final var input = "{\"b\":{\"d\":4,\"c\":3},\"a\":1}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"a\":1,\"b\":{\"c\":3,\"d\":4}}", result, "should sort nested keys");
    }

    /** Verifies that normalize handles arrays. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve array order")
    void shouldPreserveArrayOrder_whenArrayProvided() {
      // Given
      final var input = "[3,1,2]";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("[3,1,2]", result, "should preserve array element order");
    }

    /** Verifies that normalize handles empty objects. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle empty object")
    void shouldHandleEmptyObject_whenEmptyObjectProvided() {
      // When
      final var result = JsonNormalizer.normalize("{}");

      // Then
      assertEquals("{}", result, "should normalize empty object");
    }

    /** Verifies that normalize handles empty arrays. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle empty array")
    void shouldHandleEmptyArray_whenEmptyArrayProvided() {
      // When
      final var result = JsonNormalizer.normalize("[]");

      // Then
      assertEquals("[]", result, "should normalize empty array");
    }

    /** Verifies that normalize preserves string values with escapes. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve escape sequences in strings")
    void shouldPreserveEscapes_whenStringContainsEscapes() {
      // Given
      final var input = "{\"msg\":\"hello\\nworld\"}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"msg\":\"hello\\nworld\"}", result, "should preserve escape sequences");
    }

    /** Verifies that normalize handles all escape types. */
    @Test
    @Tag("normal")
    @DisplayName("should handle all JSON escape types")
    void shouldHandleAllEscapeTypes_whenVariousEscapesUsed() {
      // Given
      final var input = "{\"a\":\"\\\"\\\\\\b\\f\\n\\r\\t\"}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"a\":\"\\\"\\\\\\b\\f\\n\\r\\t\"}", result, "should handle all escape types");
    }

    /** Verifies that normalize handles unicode escapes. */
    @Test
    @Tag("normal")
    @DisplayName("should handle unicode escape sequences")
    void shouldHandleUnicodeEscapes_whenUnicodeEscapesPresent() {
      // Given
      final var input = "{\"char\":\"\\u0041\"}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"char\":\"A\"}", result, "should decode unicode escape to character");
    }

    /** Verifies that normalize handles surrogate pairs. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle surrogate pairs in unicode escapes")
    void shouldHandleSurrogatePairs_whenSurrogatePairPresent() {
      // Given - U+1F600 (grinning face emoji) encoded as surrogate pair
      final var input = "{\"emoji\":\"\\uD83D\\uDE00\"}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      // The emoji should be preserved in the output
      assertTrue(
          result.contains("\uD83D\uDE00") || result.contains("\\uD83D\\uDE00"),
          "should handle surrogate pair correctly");
    }

    /** Verifies that normalize distinguishes string values from literals. */
    @Test
    @Tag("normal")
    @DisplayName("should distinguish string values from boolean literals")
    void shouldDistinguishStringFromBoolean_whenBothPresent() {
      // Given
      final var withStringTrue = "{\"value\":\"true\"}";
      final var withBoolTrue = "{\"value\":true}";

      // When
      final var normalizedString = JsonNormalizer.normalize(withStringTrue);
      final var normalizedBool = JsonNormalizer.normalize(withBoolTrue);

      // Then
      assertEquals("{\"value\":\"true\"}", normalizedString, "should keep string true quoted");
      assertEquals("{\"value\":true}", normalizedBool, "should keep boolean true unquoted");
      assertFalse(
          normalizedString.equals(normalizedBool),
          "string and boolean should normalize differently");
    }

    /** Verifies that normalize distinguishes string values from number literals. */
    @Test
    @Tag("normal")
    @DisplayName("should distinguish string values from number literals")
    void shouldDistinguishStringFromNumber_whenBothPresent() {
      // Given
      final var withStringNum = "{\"value\":\"123\"}";
      final var withNumber = "{\"value\":123}";

      // When
      final var normalizedString = JsonNormalizer.normalize(withStringNum);
      final var normalizedNum = JsonNormalizer.normalize(withNumber);

      // Then
      assertEquals("{\"value\":\"123\"}", normalizedString, "should keep string number quoted");
      assertEquals("{\"value\":123}", normalizedNum, "should keep number unquoted");
      assertFalse(
          normalizedString.equals(normalizedNum), "string and number should normalize differently");
    }

    /** Verifies that normalize distinguishes string "null" from null literal. */
    @Test
    @Tag("normal")
    @DisplayName("should distinguish string null from null literal")
    void shouldDistinguishStringFromNull_whenBothPresent() {
      // Given
      final var withStringNull = "{\"value\":\"null\"}";
      final var withNull = "{\"value\":null}";

      // When
      final var normalizedString = JsonNormalizer.normalize(withStringNull);
      final var normalizedNull = JsonNormalizer.normalize(withNull);

      // Then
      assertEquals("{\"value\":\"null\"}", normalizedString, "should keep string null quoted");
      assertEquals("{\"value\":null}", normalizedNull, "should keep null literal unquoted");
      assertFalse(
          normalizedString.equals(normalizedNull),
          "string null and null literal should normalize differently");
    }

    /** Verifies that normalize handles negative numbers. */
    @Test
    @Tag("normal")
    @DisplayName("should handle negative numbers")
    void shouldHandleNegativeNumbers_whenNegativeNumberProvided() {
      // Given
      final var input = "{\"value\":-42}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"value\":-42}", result, "should preserve negative numbers");
    }

    /** Verifies that normalize handles scientific notation. */
    @Test
    @Tag("normal")
    @DisplayName("should handle scientific notation numbers")
    void shouldHandleScientificNotation_whenExponentialNumberProvided() {
      // Given
      final var input = "{\"value\":1.5e10}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"value\":1.5e10}", result, "should preserve scientific notation");
    }

    /** Verifies that normalize handles decimal numbers. */
    @Test
    @Tag("normal")
    @DisplayName("should handle decimal numbers")
    void shouldHandleDecimalNumbers_whenDecimalProvided() {
      // Given
      final var input = "{\"value\":3.14}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"value\":3.14}", result, "should preserve decimal numbers");
    }

    /** Verifies that normalize handles mixed types in arrays. */
    @Test
    @Tag("normal")
    @DisplayName("should handle mixed types in arrays")
    void shouldHandleMixedTypes_whenArrayContainsMixedTypes() {
      // Given
      final var input = "[1,\"two\",true,null,{\"a\":3}]";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("[1,\"two\",true,null,{\"a\":3}]", result, "should handle mixed types in array");
    }

    /** Verifies that normalize returns original string for invalid JSON. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return original string when JSON is invalid")
    void shouldReturnOriginal_whenJsonIsInvalid() {
      // Given
      final var invalid = "not valid json";

      // When
      final var result = JsonNormalizer.normalize(invalid);

      // Then
      assertEquals(invalid, result, "should return original for invalid JSON");
    }

    /** Verifies that normalize returns original for partial JSON. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return original string when JSON has trailing content")
    void shouldReturnOriginal_whenJsonHasTrailingContent() {
      // Given
      final var input = "{\"a\":1} extra";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals(input, result, "should return original when trailing content exists");
    }

    /** Verifies that normalize returns original for oversized input. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return original string when input exceeds size limit")
    void shouldReturnOriginal_whenInputExceedsSizeLimit() {
      // Given
      final var large = "{\"a\":\"" + "x".repeat(JsonNormalizer.MAX_INPUT_LENGTH) + "\"}";

      // When
      final var result = JsonNormalizer.normalize(large);

      // Then
      assertEquals(large, result, "should return original for oversized input");
    }

    /** Verifies that normalize handles deeply nested structures up to the limit. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle nesting up to the depth limit")
    void shouldHandleDeepNesting_whenNestingIsWithinLimit() {
      // Given - create nesting at depth 10 (well within limit)
      final var sb = new StringBuilder();
      for (var i = 0; i < 10; i++) {
        sb.append("{\"a\":");
      }
      sb.append("1");
      for (var i = 0; i < 10; i++) {
        sb.append("}");
      }
      final var input = sb.toString();

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals(input, result, "should handle nesting within limit");
    }

    /** Verifies that normalize returns original for excessively deep nesting. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return original string when nesting exceeds depth limit")
    void shouldReturnOriginal_whenNestingExceedsDepthLimit() {
      // Given - create nesting exceeding MAX_NESTING_DEPTH
      final var sb = new StringBuilder();
      for (var i = 0; i < JsonNormalizer.MAX_NESTING_DEPTH + 1; i++) {
        sb.append("[");
      }
      sb.append("1");
      for (var i = 0; i < JsonNormalizer.MAX_NESTING_DEPTH + 1; i++) {
        sb.append("]");
      }
      final var input = sb.toString();

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals(input, result, "should return original for excessive nesting");
    }

    /** Verifies that normalize handles boolean values. */
    @Test
    @Tag("normal")
    @DisplayName("should handle boolean values")
    void shouldHandleBooleans_whenBooleanValuesProvided() {
      // Given
      final var input = "{\"yes\":true,\"no\":false}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"no\":false,\"yes\":true}", result, "should handle booleans and sort keys");
    }

    /** Verifies that normalize handles null values. */
    @Test
    @Tag("normal")
    @DisplayName("should handle null values")
    void shouldHandleNulls_whenNullValuesProvided() {
      // Given
      final var input = "{\"value\":null}";

      // When
      final var result = JsonNormalizer.normalize(input);

      // Then
      assertEquals("{\"value\":null}", result, "should handle null values");
    }
  }

  /** Tests for the looksLikeJson() method. */
  @Nested
  @DisplayName("looksLikeJson(String) method")
  class LooksLikeJsonMethod {

    /** Tests for the looksLikeJson method. */
    LooksLikeJsonMethod() {}

    /** Verifies that looksLikeJson returns true for object syntax. */
    @Test
    @Tag("normal")
    @DisplayName("should return true for JSON object syntax")
    void shouldReturnTrue_whenValueLooksLikeJsonObject() {
      // When & Then
      assertTrue(JsonNormalizer.looksLikeJson("{\"a\":1}"), "should detect JSON object");
      assertTrue(JsonNormalizer.looksLikeJson("{}"), "should detect empty JSON object");
    }

    /** Verifies that looksLikeJson returns true for array syntax. */
    @Test
    @Tag("normal")
    @DisplayName("should return true for JSON array syntax")
    void shouldReturnTrue_whenValueLooksLikeJsonArray() {
      // When & Then
      assertTrue(JsonNormalizer.looksLikeJson("[1,2,3]"), "should detect JSON array");
      assertTrue(JsonNormalizer.looksLikeJson("[]"), "should detect empty JSON array");
    }

    /** Verifies that looksLikeJson handles whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should handle leading and trailing whitespace")
    void shouldHandleWhitespace_whenInputHasExtraSpaces() {
      // When & Then
      assertTrue(
          JsonNormalizer.looksLikeJson("  {\"a\":1}  "),
          "should detect JSON with surrounding whitespace");
    }

    /** Verifies that looksLikeJson returns false for non-JSON. */
    @Test
    @Tag("normal")
    @DisplayName("should return false for non-JSON strings")
    void shouldReturnFalse_whenValueIsNotJson() {
      // When & Then
      assertFalse(JsonNormalizer.looksLikeJson("hello"), "should reject plain string");
      assertFalse(JsonNormalizer.looksLikeJson("123"), "should reject number string");
      assertFalse(JsonNormalizer.looksLikeJson("true"), "should reject boolean string");
    }

    /** Verifies that looksLikeJson returns false for mismatched brackets. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false for mismatched brackets")
    void shouldReturnFalse_whenBracketsAreMismatched() {
      // When & Then
      assertFalse(
          JsonNormalizer.looksLikeJson("{\"a\":1]"),
          "should reject mismatched curly-square brackets");
      assertFalse(
          JsonNormalizer.looksLikeJson("[1,2,3}"),
          "should reject mismatched square-curly brackets");
    }
  }
}
