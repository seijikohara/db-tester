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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link AutoFormatResolver}. */
@DisplayName("AutoFormatResolver")
class AutoFormatResolverTest {

  /** Tests for the AutoFormatResolver class. */
  AutoFormatResolverTest() {}

  /** The resolver instance under test. */
  private AutoFormatResolver resolver;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    resolver = new AutoFormatResolver();
  }

  /** Tests for the resolve() method. */
  @Nested
  @DisplayName("resolve(Path) method")
  class ResolveMethod {

    /** Tests for the resolve method. */
    ResolveMethod() {}

    /**
     * Verifies that resolve loads CSV files when only CSV files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load CSV files when only CSV files exist")
    void shouldLoadCsvFiles_whenOnlyCsvFilesExist(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1,COL2", "A,B");
      createCsvFile(tempDir, "TABLE2.csv", "COL1,COL2", "C,D");

      // When
      final var result = resolver.resolve(tempDir);

      // Then
      assertAll(
          "should load all CSV tables",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(2, result.getTables().size(), "should have 2 tables"));
    }

    /**
     * Verifies that resolve loads JSON files when only JSON files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load JSON files when only JSON files exist")
    void shouldLoadJsonFiles_whenOnlyJsonFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createJsonFile(
          tempDir,
          "TABLE1.json",
          "[{\"COL1\": \"A\", \"COL2\": \"B\"}, {\"COL1\": \"C\", \"COL2\": \"D\"}]");

      // When
      final var result = resolver.resolve(tempDir);

      // Then
      assertAll(
          "should load JSON table",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getTables().size(), "should have 1 table"));
    }

    /**
     * Verifies that resolve loads mixed format files when table names are different.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load mixed formats when different table names")
    void shouldLoadMixedFormats_whenDifferentTableNames(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "USER.csv", "ID,NAME", "1,Alice");
      createJsonFile(tempDir, "PRODUCT.json", "[{\"ID\": \"1\", \"NAME\": \"Widget\"}]");

      // When
      final var result = resolver.resolve(tempDir);

      // Then
      assertAll(
          "should load tables from both formats",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(2, result.getTables().size(), "should have 2 tables"));
    }

    /**
     * Verifies that resolve loads multiple formats when many files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load multiple formats when many files exist")
    void shouldLoadMultipleFormats_whenManyFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE2.csv", "COL1", "B");
      createYamlFile(tempDir, "TABLE3.yaml", "- COL1: C");

      // When
      final var result = resolver.resolve(tempDir);

      // Then
      assertAll(
          "should load all tables from all formats",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(3, result.getTables().size(), "should have 3 tables"));
    }

    /**
     * Verifies that resolve ignores unsupported files when mixed with supported files.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should ignore unsupported files when mixed with supported files")
    void shouldIgnoreUnsupportedFiles_whenMixed(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      Files.writeString(tempDir.resolve("README.txt"), "This is not a data file");
      Files.writeString(tempDir.resolve("load-order.txt"), "TABLE1");

      // When
      final var result = resolver.resolve(tempDir);

      // Then
      assertAll(
          "should load only supported files",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getTables().size(), "should have 1 table"));
    }

    /**
     * Verifies that resolve throws exception when table name conflict exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when table name conflict exists")
    void shouldThrowException_whenTableNameConflict(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "USER.csv", "ID,NAME", "1,Alice");
      createJsonFile(tempDir, "USER.json", "[{\"ID\": \"1\", \"NAME\": \"Alice\"}]");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> resolver.resolve(tempDir),
              "should throw DataSetLoadException for table name conflict");

      final var message = exception.getMessage();
      assertNotNull(message, "message should not be null");
      assertAll(
          "error message should contain conflict details",
          () ->
              assertTrue(
                  message.contains("Table name conflict"), "should mention table name conflict"),
          () -> assertTrue(message.contains("USER"), "should mention conflicting table name"),
          () -> assertTrue(message.contains("USER.csv"), "should mention CSV file"),
          () -> assertTrue(message.contains("USER.json"), "should mention JSON file"),
          () ->
              assertTrue(
                  message.contains("DataFormat.CSV"), "should suggest concrete format resolution"));
    }

    /**
     * Verifies that resolve throws exception when multiple table name conflicts exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when multiple table name conflicts exist")
    void shouldThrowException_whenMultipleConflicts(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "USER.csv", "ID,NAME", "1,Alice");
      createJsonFile(tempDir, "USER.json", "[{\"ID\": \"1\", \"NAME\": \"Alice\"}]");
      createCsvFile(tempDir, "ORDER.csv", "ID,AMOUNT", "1,100");
      createYamlFile(tempDir, "ORDER.yaml", "- ID: 1\n  AMOUNT: 100");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> resolver.resolve(tempDir),
              "should throw DataSetLoadException for multiple conflicts");

      final var message = exception.getMessage();
      assertNotNull(message, "message should not be null");
      assertAll(
          "error message should contain all conflict details",
          () ->
              assertTrue(
                  message.contains("Table name conflict"), "should mention table name conflict"),
          () -> assertTrue(message.contains("USER"), "should mention USER conflict"),
          () -> assertTrue(message.contains("ORDER"), "should mention ORDER conflict"));
    }

    /**
     * Verifies that resolve throws exception when no supported files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when no supported files exist")
    void shouldThrowException_whenNoSupportedFiles(final @TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("README.txt"), "Not a data file");
      Files.writeString(tempDir.resolve("notes.md"), "Some notes");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> resolver.resolve(tempDir),
              "should throw DataSetLoadException when no supported files");

      final var message = exception.getMessage();
      assertNotNull(message, "message should not be null");
      assertAll(
          "error message should contain supported extensions",
          () ->
              assertTrue(
                  message.contains("No supported data files found"),
                  "should mention no files found"),
          () ->
              assertTrue(
                  message.contains("Supported file extensions"),
                  "should mention supported extensions"));
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

  /**
   * Creates a JSON file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param jsonContent the JSON content
   * @throws IOException if file creation fails
   */
  private static void createJsonFile(
      final Path dir, final String fileName, final String jsonContent) throws IOException {
    Files.writeString(dir.resolve(fileName), jsonContent);
  }

  /**
   * Creates a YAML file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param yamlContent the YAML content
   * @throws IOException if file creation fails
   */
  private static void createYamlFile(
      final Path dir, final String fileName, final String yamlContent) throws IOException {
    Files.writeString(dir.resolve(fileName), yamlContent);
  }
}
