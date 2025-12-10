package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ValueParser}. */
@DisplayName("ValueParser")
class ValueParserTest {

  /** Tests for the ValueParser class. */
  ValueParserTest() {}

  /** The parser instance under test. */
  private ValueParser parser;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    parser = new ValueParser();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new ValueParser();

      // Then
      assertTrue(instance != null, "instance should not be null");
    }
  }

  /** Tests for the parseBoolean() method. */
  @Nested
  @DisplayName("parseBoolean(String) method")
  class ParseBooleanMethod {

    /** Tests for the parseBoolean method. */
    ParseBooleanMethod() {}

    /** Verifies that parseBoolean returns true when value is "true". */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is 'true'")
    void shouldReturnTrue_whenValueIsTrue() {
      // When
      final var result = parser.parseBoolean("true");

      // Then
      assertTrue(result, "should return true for 'true'");
    }

    /** Verifies that parseBoolean returns true when value is "TRUE". */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is 'TRUE'")
    void shouldReturnTrue_whenValueIsTrueUppercase() {
      // When
      final var result = parser.parseBoolean("TRUE");

      // Then
      assertTrue(result, "should return true for 'TRUE'");
    }

    /** Verifies that parseBoolean returns true when value is "1". */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is '1'")
    void shouldReturnTrue_whenValueIsOne() {
      // When
      final var result = parser.parseBoolean("1");

      // Then
      assertTrue(result, "should return true for '1'");
    }

    /** Verifies that parseBoolean returns true when value is "yes". */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is 'yes'")
    void shouldReturnTrue_whenValueIsYes() {
      // When
      final var result = parser.parseBoolean("yes");

      // Then
      assertTrue(result, "should return true for 'yes'");
    }

    /** Verifies that parseBoolean returns true when value is "y". */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is 'y'")
    void shouldReturnTrue_whenValueIsY() {
      // When
      final var result = parser.parseBoolean("y");

      // Then
      assertTrue(result, "should return true for 'y'");
    }

    /** Verifies that parseBoolean returns false when value is "false". */
    @Test
    @Tag("normal")
    @DisplayName("should return false when value is 'false'")
    void shouldReturnFalse_whenValueIsFalse() {
      // When
      final var result = parser.parseBoolean("false");

      // Then
      assertFalse(result, "should return false for 'false'");
    }

    /** Verifies that parseBoolean returns false when value is "0". */
    @Test
    @Tag("normal")
    @DisplayName("should return false when value is '0'")
    void shouldReturnFalse_whenValueIsZero() {
      // When
      final var result = parser.parseBoolean("0");

      // Then
      assertFalse(result, "should return false for '0'");
    }

    /** Verifies that parseBoolean returns false when value is unrecognized. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when value is unrecognized")
    void shouldReturnFalse_whenValueIsUnrecognized() {
      // When
      final var result = parser.parseBoolean("maybe");

      // Then
      assertFalse(result, "should return false for unrecognized values");
    }

    /** Verifies that parseBoolean handles whitespace. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle whitespace in value")
    void shouldHandleWhitespace_whenValueHasWhitespace() {
      // When
      final var result = parser.parseBoolean("  true  ");

      // Then
      assertTrue(result, "should handle whitespace and return true");
    }
  }

  /** Tests for the parseDate() method. */
  @Nested
  @DisplayName("parseDate(String) method")
  class ParseDateMethod {

    /** Tests for the parseDate method. */
    ParseDateMethod() {}

    /** Verifies that parseDate returns Date when format is yyyy-MM-dd. */
    @Test
    @Tag("normal")
    @DisplayName("should return Date when format is 'yyyy-MM-dd'")
    void shouldReturnDate_whenFormatIsYyyyMmDd() {
      // When
      final var result = parser.parseDate("2024-01-15");

      // Then
      assertEquals(Date.valueOf("2024-01-15"), result, "should parse date correctly");
    }

    /** Verifies that parseDate extracts date from datetime format. */
    @Test
    @Tag("normal")
    @DisplayName("should extract date when format is 'yyyy-MM-dd HH:mm:ss'")
    void shouldExtractDate_whenFormatIncludesTime() {
      // When
      final var result = parser.parseDate("2024-01-15 10:30:00");

      // Then
      assertEquals(Date.valueOf("2024-01-15"), result, "should extract date part only");
    }

    /** Verifies that parseDate handles whitespace. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle whitespace in value")
    void shouldHandleWhitespace_whenValueHasWhitespace() {
      // When
      final var result = parser.parseDate("  2024-01-15  ");

      // Then
      assertEquals(Date.valueOf("2024-01-15"), result, "should handle whitespace");
    }

    /** Verifies that parseDate throws exception when format is invalid. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when format is invalid")
    void shouldThrowException_whenFormatIsInvalid() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> parser.parseDate("invalid-date"),
          "should throw exception for invalid format");
    }
  }

  /** Tests for the parseTime() method. */
  @Nested
  @DisplayName("parseTime(String) method")
  class ParseTimeMethod {

    /** Tests for the parseTime method. */
    ParseTimeMethod() {}

    /** Verifies that parseTime returns Time when format is HH:mm:ss. */
    @Test
    @Tag("normal")
    @DisplayName("should return Time when format is 'HH:mm:ss'")
    void shouldReturnTime_whenFormatIsHhMmSs() {
      // When
      final var result = parser.parseTime("10:30:45");

      // Then
      assertEquals(Time.valueOf("10:30:45"), result, "should parse time correctly");
    }

    /** Verifies that parseTime removes fractional seconds. */
    @Test
    @Tag("normal")
    @DisplayName("should remove fractional seconds when present")
    void shouldRemoveFractionalSeconds_whenPresent() {
      // When
      final var result = parser.parseTime("10:30:45.123");

      // Then
      assertEquals(Time.valueOf("10:30:45"), result, "should remove fractional seconds");
    }

    /** Verifies that parseTime extracts time from datetime format. */
    @Test
    @Tag("normal")
    @DisplayName("should extract time when format includes date")
    void shouldExtractTime_whenFormatIncludesDate() {
      // When
      final var result = parser.parseTime("2024-01-15 10:30:45");

      // Then
      assertEquals(Time.valueOf("10:30:45"), result, "should extract time part only");
    }
  }

  /** Tests for the parseTimestamp() method. */
  @Nested
  @DisplayName("parseTimestamp(String) method")
  class ParseTimestampMethod {

    /** Tests for the parseTimestamp method. */
    ParseTimestampMethod() {}

    /** Verifies that parseTimestamp returns Timestamp when format is valid. */
    @Test
    @Tag("normal")
    @DisplayName("should return Timestamp when format is valid")
    void shouldReturnTimestamp_whenFormatIsValid() {
      // When
      final var result = parser.parseTimestamp("2024-01-15 10:30:45");

      // Then
      assertEquals(
          Timestamp.valueOf("2024-01-15 10:30:45"), result, "should parse timestamp correctly");
    }

    /** Verifies that parseTimestamp handles fractional seconds. */
    @Test
    @Tag("normal")
    @DisplayName("should handle fractional seconds")
    void shouldHandleFractionalSeconds_whenPresent() {
      // When
      final var result = parser.parseTimestamp("2024-01-15 10:30:45.123");

      // Then
      assertEquals(
          Timestamp.valueOf("2024-01-15 10:30:45.123"),
          result,
          "should parse timestamp with fractional seconds");
    }
  }

  /** Tests for the parseBlob() method. */
  @Nested
  @DisplayName("parseBlob(String) method")
  class ParseBlobMethod {

    /** Tests for the parseBlob method. */
    ParseBlobMethod() {}

    /** Verifies that parseBlob decodes Base64 when prefix is present. */
    @Test
    @Tag("normal")
    @DisplayName("should decode Base64 when [BASE64] prefix is present")
    void shouldDecodeBase64_whenPrefixIsPresent() {
      // Given
      final var originalBytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
      final var base64Content = Base64.getEncoder().encodeToString(originalBytes);
      final var input = String.format("[BASE64]%s", base64Content);

      // When
      final var result = parser.parseBlob(input);

      // Then
      assertArrayEquals(originalBytes, result, "should decode Base64 content");
    }

    /** Verifies that parseBlob returns UTF-8 bytes when no prefix. */
    @Test
    @Tag("normal")
    @DisplayName("should return UTF-8 bytes when no prefix")
    void shouldReturnUtf8Bytes_whenNoPrefixPresent() {
      // Given
      final var input = "Hello, World!";

      // When
      final var result = parser.parseBlob(input);

      // Then
      assertArrayEquals(
          input.getBytes(StandardCharsets.UTF_8), result, "should return UTF-8 encoded bytes");
    }

    /** Verifies that parseBlob handles whitespace. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle whitespace in value")
    void shouldHandleWhitespace_whenValueHasWhitespace() {
      // Given
      final var originalBytes = "test".getBytes(StandardCharsets.UTF_8);
      final var base64Content = Base64.getEncoder().encodeToString(originalBytes);
      final var input = String.format("  [BASE64]%s  ", base64Content);

      // When
      final var result = parser.parseBlob(input);

      // Then
      assertArrayEquals(originalBytes, result, "should handle whitespace");
    }
  }

  /** Tests for the numeric parsing methods. */
  @Nested
  @DisplayName("numeric parsing methods")
  class NumericParsingMethods {

    /** Tests for the numeric parsing methods. */
    NumericParsingMethods() {}

    /** Verifies that parseInt parses valid integer. */
    @Test
    @Tag("normal")
    @DisplayName("should parse valid integer")
    void shouldParseInt_whenValueIsValid() {
      // When
      final var result = parser.parseInt("42");

      // Then
      assertEquals(42, result, "should parse integer correctly");
    }

    /** Verifies that parseLong parses valid long. */
    @Test
    @Tag("normal")
    @DisplayName("should parse valid long")
    void shouldParseLong_whenValueIsValid() {
      // When
      final var result = parser.parseLong("9223372036854775807");

      // Then
      assertEquals(Long.MAX_VALUE, result, "should parse long correctly");
    }

    /** Verifies that parseFloat parses valid float. */
    @Test
    @Tag("normal")
    @DisplayName("should parse valid float")
    void shouldParseFloat_whenValueIsValid() {
      // When
      final var result = parser.parseFloat("3.14");

      // Then
      assertEquals(3.14f, result, 0.001f, "should parse float correctly");
    }

    /** Verifies that parseDouble parses valid double. */
    @Test
    @Tag("normal")
    @DisplayName("should parse valid double")
    void shouldParseDouble_whenValueIsValid() {
      // When
      final var result = parser.parseDouble("3.14159265359");

      // Then
      assertEquals(3.14159265359, result, 0.0000000001, "should parse double correctly");
    }

    /** Verifies that parseBigDecimal parses valid BigDecimal. */
    @Test
    @Tag("normal")
    @DisplayName("should parse valid BigDecimal")
    void shouldParseBigDecimal_whenValueIsValid() {
      // When
      final var result = parser.parseBigDecimal("123.456789");

      // Then
      assertEquals(new BigDecimal("123.456789"), result, "should parse BigDecimal correctly");
    }

    /** Verifies that numeric parsing handles whitespace. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle whitespace in numeric values")
    void shouldHandleWhitespace_whenNumericValueHasWhitespace() {
      // When & Then
      assertAll(
          "should handle whitespace in all numeric types",
          () -> assertEquals(42, parser.parseInt("  42  "), "parseInt should handle whitespace"),
          () -> assertEquals(42L, parser.parseLong("  42  "), "parseLong should handle whitespace"),
          () ->
              assertEquals(
                  3.14f,
                  parser.parseFloat("  3.14  "),
                  0.001f,
                  "parseFloat should handle whitespace"),
          () ->
              assertEquals(
                  3.14,
                  parser.parseDouble("  3.14  "),
                  0.001,
                  "parseDouble should handle whitespace"),
          () ->
              assertEquals(
                  new BigDecimal("3.14"),
                  parser.parseBigDecimal("  3.14  "),
                  "parseBigDecimal should handle whitespace"));
    }
  }
}
