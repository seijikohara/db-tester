package io.github.seijikohara.dbtester.internal.format.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DelimiterConfig}. */
@DisplayName("DelimiterConfig")
class DelimiterConfigTest {

  /** Tests for the DelimiterConfig record. */
  DelimiterConfigTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when valid parameters provided. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when valid parameters provided")
    void shouldCreateInstance_whenValidParametersProvided() {
      // When
      final var config = new DelimiterConfig(',', "csv");

      // Then
      assertAll(
          "config should have correct values",
          () -> assertEquals(',', config.delimiter(), "should have comma delimiter"),
          () -> assertEquals("csv", config.extension(), "should have csv extension"));
    }

    /** Verifies that constructor throws exception when extension is blank. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when extension is blank")
    void shouldThrowException_whenExtensionIsBlank() {
      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new DelimiterConfig(',', ""),
              "should throw IllegalArgumentException");

      assertNotNull(exception.getMessage(), "exception should have message");
    }

    /** Verifies that constructor throws exception when extension is whitespace only. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when extension is whitespace only")
    void shouldThrowException_whenExtensionIsWhitespaceOnly() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new DelimiterConfig(',', "   "),
          "should throw IllegalArgumentException");
    }
  }

  /** Tests for the CSV constant. */
  @Nested
  @DisplayName("CSV constant")
  class CsvConstant {

    /** Tests for CSV constant. */
    CsvConstant() {}

    /** Verifies that CSV constant has correct configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct configuration for CSV")
    void shouldHaveCorrectConfiguration_forCsv() {
      // When
      final var csv = DelimiterConfig.CSV;

      // Then
      assertAll(
          "CSV config should have correct values",
          () -> assertEquals(',', csv.delimiter(), "should have comma delimiter"),
          () -> assertEquals("csv", csv.extension(), "should have csv extension"));
    }
  }

  /** Tests for the TSV constant. */
  @Nested
  @DisplayName("TSV constant")
  class TsvConstant {

    /** Tests for TSV constant. */
    TsvConstant() {}

    /** Verifies that TSV constant has correct configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct configuration for TSV")
    void shouldHaveCorrectConfiguration_forTsv() {
      // When
      final var tsv = DelimiterConfig.TSV;

      // Then
      assertAll(
          "TSV config should have correct values",
          () -> assertEquals('\t', tsv.delimiter(), "should have tab delimiter"),
          () -> assertEquals("tsv", tsv.extension(), "should have tsv extension"));
    }
  }
}
