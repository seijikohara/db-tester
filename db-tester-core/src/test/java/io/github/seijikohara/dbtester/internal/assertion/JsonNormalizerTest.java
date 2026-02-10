package io.github.seijikohara.dbtester.internal.assertion;

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

    /** Verifies that looksLikeJson returns false for non-JSON. */
    @Test
    @Tag("normal")
    @DisplayName("should return false for non-JSON strings")
    void shouldReturnFalse_whenValueIsNotJson() {
      // When & Then
      assertFalse(JsonNormalizer.looksLikeJson("hello"), "should reject plain string");
      assertFalse(JsonNormalizer.looksLikeJson("123"), "should reject number string");
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
