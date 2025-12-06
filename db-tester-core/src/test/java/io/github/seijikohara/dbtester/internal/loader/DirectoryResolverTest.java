package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link DirectoryResolver}. */
@DisplayName("DirectoryResolver")
class DirectoryResolverTest {

  /** Tests for the DirectoryResolver class. */
  DirectoryResolverTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor stores test class when provided. */
    @Test
    @Tag("normal")
    @DisplayName("should store test class when provided")
    void shouldStoreTestClass_whenProvided() {
      // Given
      final var testClass = DirectoryResolverTest.class;

      // When
      final var resolver = new DirectoryResolver(testClass);

      // Then
      assertAll(
          "resolver should store test class",
          () -> assertNotNull(resolver, "resolver should not be null"),
          () -> assertEquals(testClass, resolver.testClass(), "should store test class"));
    }
  }

  /** Tests for the resolveDirectory() method. */
  @Nested
  @DisplayName("resolveDirectory(String, String) method")
  class ResolveDirectoryMethod {

    /** Tests for the resolveDirectory method. */
    ResolveDirectoryMethod() {}

    /**
     * Verifies that resolveDirectory returns resolved directory when custom location provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return resolved directory when custom location provided")
    void shouldReturnResolvedDirectory_whenCustomLocationProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      final var customLocation = tempDir.toString();
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");

      // When
      final var result = resolver.resolveDirectory(customLocation, null);

      // Then
      assertAll(
          "should return custom location directory",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(tempDir, result, "should return custom location"));
    }

    /**
     * Verifies that resolveDirectory returns convention-based directory when no location provided.
     */
    @Test
    @Tag("normal")
    @DisplayName("should return convention-based directory when no location provided")
    void shouldReturnConventionBasedDirectory_whenNoLocationProvided() {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);

      // When
      final var result =
          resolver.resolveDirectory(null, "/testMethod"); // Use existing test resource directory

      // Then
      assertAll(
          "should return convention-based directory",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertTrue(Files.exists(result), "directory should exist"),
          () -> assertTrue(Files.isDirectory(result), "path should be directory"));
    }

    /**
     * Verifies that resolveDirectory returns directory with suffix when suffix provided with
     * convention-based path.
     */
    @Test
    @Tag("normal")
    @DisplayName(
        "should return directory with suffix when suffix provided with convention-based path")
    void shouldReturnDirectoryWithSuffix_whenSuffixProvidedWithConventionBasedPath() {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);

      // When
      final var result =
          resolver.resolveDirectory(
              null, "/testMethod/expected"); // Use existing test resource directory

      // Then
      assertAll(
          "should return directory with suffix",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertTrue(Files.exists(result), "directory should exist"),
          () -> assertTrue(Files.isDirectory(result), "path should be directory"),
          () ->
              assertTrue(
                  result.toString().endsWith("testMethod/expected")
                      || result.toString().endsWith("testMethod\\expected"),
                  "path should end with testMethod/expected"));
    }

    /**
     * Verifies that resolveDirectory uses custom location when both location and suffix provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should use custom location when both location and suffix provided")
    void shouldUseCustomLocation_whenBothLocationAndSuffixProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      final var customLocation = tempDir.toString();
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");

      // When
      final var result = resolver.resolveDirectory(customLocation, "/ignored");

      // Then
      assertEquals(tempDir, result, "should use custom location and ignore suffix");
    }

    /** Verifies that resolveDirectory throws exception when classpath directory not found. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when classpath directory not found")
    void shouldThrowException_whenClasspathDirectoryNotFound() {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> resolver.resolveDirectory("classpath:nonexistent/path/", null));

      final var message = exception.getMessage();
      assertAll(
          "exception should contain expected information",
          () ->
              assertTrue(
                  message != null && message.contains("not found"), "should mention not found"),
          () ->
              assertTrue(
                  message != null && message.contains("classpath"), "should mention classpath"));
    }

    /**
     * Verifies that resolveDirectory throws exception when file system directory not found.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when file system directory not found")
    void shouldThrowException_whenFileSystemDirectoryNotFound(final @TempDir Path tempDir) {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      final var nonExistentPath = tempDir.resolve("nonexistent").toString();

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class, () -> resolver.resolveDirectory(nonExistentPath, null));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("does not exist"),
          "exception should mention directory does not exist");
    }

    /**
     * Verifies that resolveDirectory throws exception when path is not directory.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when path is not directory")
    void shouldThrowException_whenPathIsNotDirectory(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      final var filePath = tempDir.resolve("file.txt");
      Files.writeString(filePath, "content");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> resolver.resolveDirectory(filePath.toString(), null));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("not a directory"),
          "exception should mention path is not a directory");
    }
  }

  /** Tests for the validateDirectoryContainsSupportedFiles() method. */
  @Nested
  @DisplayName("validateDirectoryContainsSupportedFiles(Path) method")
  class ValidateDirectoryContainsSupportedFilesMethod {

    /** Tests for the validateDirectoryContainsSupportedFiles method. */
    ValidateDirectoryContainsSupportedFilesMethod() {}

    /**
     * Verifies that validateDirectoryContainsSupportedFiles passes validation when directory
     * contains supported files.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should pass validation when directory contains supported files")
    void shouldPassValidation_whenDirectoryContainsSupportedFiles(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");

      // When & Then (no exception)
      resolver.validateDirectoryContainsSupportedFiles(tempDir);
    }

    /**
     * Verifies that validateDirectoryContainsSupportedFiles throws exception when no supported
     * files found.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when no supported files found")
    void shouldThrowException_whenNoSupportedFilesFound(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      Files.writeString(tempDir.resolve("unsupported.txt"), "data");

      // When & Then
      final var exception =
          assertThrows(
              IllegalStateException.class,
              () -> resolver.validateDirectoryContainsSupportedFiles(tempDir));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("no supported data files"),
          "exception should mention no supported data files");
    }

    /**
     * Verifies that validateDirectoryContainsSupportedFiles throws exception when directory read
     * fails.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory read fails")
    void shouldThrowException_whenDirectoryReadFails(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var testClass = DirectoryResolverTest.class;
      final var resolver = new DirectoryResolver(testClass);
      final var nonExistentDir = tempDir.resolve("nonexistent");

      // When & Then
      assertThrows(
          DataSetLoadException.class,
          () -> resolver.validateDirectoryContainsSupportedFiles(nonExistentDir));
    }
  }

  /**
   * Creates a CSV file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param lines the CSV lines
   * @throws IOException if file creation fails
   */
  private static void createCsvFile(final Path dir, final String fileName, final String... lines)
      throws IOException {
    final var content = String.join("\n", lines);
    Files.writeString(dir.resolve(fileName), content);
  }
}
