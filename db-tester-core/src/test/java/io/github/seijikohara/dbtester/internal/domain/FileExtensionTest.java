package io.github.seijikohara.dbtester.internal.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FileExtension}. */
@DisplayName("FileExtension")
class FileExtensionTest {

  /** Tests for the FileExtension class. */
  FileExtensionTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor normalizes extension with leading dot. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve extension with leading dot")
    void should_preserve_extension_with_leading_dot() {
      // Given
      final var extension = ".csv";

      // When
      final var fileExtension = new FileExtension(extension);

      // Then
      assertEquals(".csv", fileExtension.value(), "should preserve leading dot");
    }

    /** Verifies that constructor adds leading dot if missing. */
    @Test
    @Tag("normal")
    @DisplayName("should add leading dot if missing")
    void should_add_leading_dot_if_missing() {
      // Given
      final var extensionWithoutDot = "csv";

      // When
      final var fileExtension = new FileExtension(extensionWithoutDot);

      // Then
      assertEquals(".csv", fileExtension.value(), "should add leading dot");
    }

    /** Verifies that constructor normalizes to lowercase. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize to lowercase")
    void should_normalize_to_lowercase() {
      // Given
      final var uppercaseExtension = ".CSV";

      // When
      final var fileExtension = new FileExtension(uppercaseExtension);

      // Then
      assertEquals(".csv", fileExtension.value(), "should normalize to lowercase");
    }

    /** Verifies that constructor normalizes mixed case to lowercase. */
    @Test
    @Tag("edge-case")
    @DisplayName("should normalize mixed case to lowercase")
    void should_normalize_mixed_case_to_lowercase() {
      // Given
      final var mixedCaseExtension = "CsV";

      // When
      final var fileExtension = new FileExtension(mixedCaseExtension);

      // Then
      assertEquals(".csv", fileExtension.value(), "should normalize mixed case to lowercase");
    }

    /** Verifies that constructor throws exception for extension with only dot. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException for extension with only dot")
    void should_throw_exception_for_extension_with_only_dot() {
      // Given
      final var dotOnly = ".";

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new FileExtension(dotOnly),
          "should throw for dot only");
    }

    /** Verifies that constructor throws exception for empty extension. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException for empty extension")
    void should_throw_exception_for_empty_extension() {
      // Given
      final var emptyExtension = "";

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new FileExtension(emptyExtension),
          "should throw for empty extension");
    }
  }

  /** Tests for the fromFileName static method. */
  @Nested
  @DisplayName("fromFileName(String) static method")
  class FromFileNameMethod {

    /** Tests for the fromFileName method. */
    FromFileNameMethod() {}

    /** Verifies that fromFileName extracts extension from filename. */
    @Test
    @Tag("normal")
    @DisplayName("should extract extension from filename")
    void should_extract_extension_from_filename() {
      // Given
      final var fileName = "table.csv";

      // When
      final var result = FileExtension.fromFileName(fileName);

      // Then
      assertAll(
          "should extract extension",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertEquals(".csv", result.orElseThrow().value(), "should have correct value"));
    }

    /** Verifies that fromFileName normalizes extracted extension to lowercase. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize extracted extension to lowercase")
    void should_normalize_extracted_extension_to_lowercase() {
      // Given
      final var fileName = "TABLE.CSV";

      // When
      final var result = FileExtension.fromFileName(fileName);

      // Then
      assertAll(
          "should normalize to lowercase",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertEquals(".csv", result.orElseThrow().value(), "should be lowercase"));
    }

    /** Verifies that fromFileName returns empty for filename without extension. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty for filename without extension")
    void should_return_empty_for_filename_without_extension() {
      // Given
      final var fileNameWithoutExtension = "README";

      // When
      final var result = FileExtension.fromFileName(fileNameWithoutExtension);

      // Then
      assertFalse(result.isPresent(), "should return empty");
    }

    /** Verifies that fromFileName returns empty for filename with dot at start. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty for hidden file without extension")
    void should_return_empty_for_hidden_file_without_extension() {
      // Given
      final var hiddenFile = ".gitignore";

      // When
      final var result = FileExtension.fromFileName(hiddenFile);

      // Then
      assertFalse(result.isPresent(), "should return empty for hidden file starting with dot");
    }

    /** Verifies that fromFileName handles multiple dots correctly. */
    @Test
    @Tag("edge-case")
    @DisplayName("should use last dot for files with multiple dots")
    void should_use_last_dot_for_files_with_multiple_dots() {
      // Given
      final var fileWithMultipleDots = "archive.tar.gz";

      // When
      final var result = FileExtension.fromFileName(fileWithMultipleDots);

      // Then
      assertAll(
          "should use last extension",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertEquals(".gz", result.orElseThrow().value(), "should be .gz"));
    }
  }

  /** Tests for the matches method. */
  @Nested
  @DisplayName("matches(String) method")
  class MatchesMethod {

    /** Tests for the matches method. */
    MatchesMethod() {}

    /** Verifies that matches returns true for matching extension. */
    @Test
    @Tag("normal")
    @DisplayName("should return true for matching extension")
    void should_return_true_for_matching_extension() {
      // Given
      final var extension = new FileExtension(".csv");
      final var fileName = "data.csv";

      // When
      final var result = extension.matches(fileName);

      // Then
      assertTrue(result, "should match");
    }

    /** Verifies that matches is case-insensitive. */
    @Test
    @Tag("normal")
    @DisplayName("should match case-insensitively")
    void should_match_case_insensitively() {
      // Given
      final var extension = new FileExtension(".csv");
      final var fileName = "DATA.CSV";

      // When
      final var result = extension.matches(fileName);

      // Then
      assertTrue(result, "should match case-insensitively");
    }

    /** Verifies that matches returns false for non-matching extension. */
    @Test
    @Tag("normal")
    @DisplayName("should return false for non-matching extension")
    void should_return_false_for_non_matching_extension() {
      // Given
      final var extension = new FileExtension(".csv");
      final var fileName = "data.json";

      // When
      final var result = extension.matches(fileName);

      // Then
      assertFalse(result, "should not match");
    }

    /** Verifies that matches returns false for filename without extension. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false for filename without extension")
    void should_return_false_for_filename_without_extension() {
      // Given
      final var extension = new FileExtension(".csv");
      final var fileName = "README";

      // When
      final var result = extension.matches(fileName);

      // Then
      assertFalse(result, "should not match");
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /** Verifies that instances with same normalized value are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when normalized values are the same")
    void should_be_equal_when_normalized_values_are_the_same() {
      // Given
      final var ext1 = new FileExtension(".csv");
      final var ext2 = new FileExtension("csv");
      final var ext3 = new FileExtension(".CSV");

      // When & Then
      assertAll(
          "should be equal after normalization",
          () -> assertEquals(ext1, ext2, "should be equal with/without dot"),
          () -> assertEquals(ext1, ext3, "should be equal regardless of case"),
          () -> assertEquals(ext1.hashCode(), ext2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that instances with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void should_not_be_equal_when_values_differ() {
      // Given
      final var ext1 = new FileExtension(".csv");
      final var ext2 = new FileExtension(".json");

      // When & Then
      assertNotEquals(ext1, ext2, "should not be equal");
    }
  }
}
