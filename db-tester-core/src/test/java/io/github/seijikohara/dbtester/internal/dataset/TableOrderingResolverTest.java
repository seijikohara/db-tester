package io.github.seijikohara.dbtester.internal.dataset;

import static io.github.seijikohara.dbtester.internal.dataset.LoadOrderConstants.LOAD_ORDER_FILE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link TableOrderingResolver}. */
@DisplayName("TableOrderingResolver")
class TableOrderingResolverTest {

  /** Tests for the TableOrderingResolver abstract class. */
  TableOrderingResolverTest() {}

  /** Tests for the ensureTableOrdering(Path) method. */
  @Nested
  @DisplayName("ensureTableOrdering(Path) method")
  class EnsureTableOrderingMethod {

    /** Test instance of TableOrderingResolver. */
    private TableOrderingResolver resolver;

    /** Tests for the ensureTableOrdering method. */
    EnsureTableOrderingMethod() {}

    /** Sets up test fixtures before each test. */
    @BeforeEach
    void setUp() {
      resolver = new TestTableOrderingResolver("csv");
    }

    /**
     * Verifies that ensureTableOrdering returns directory when table ordering file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return directory when table ordering file exists")
    void shouldReturnDirectory_whenTableOrderingFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createTableOrderingFile(tempDir, "CUSTOM_TABLE");
      createDataFile(tempDir, "TABLE1.csv");
      createDataFile(tempDir, "TABLE2.csv");

      // When
      final var result = resolver.ensureTableOrdering(tempDir);

      // Then
      assertAll(
          "table ordering file should remain unchanged",
          () -> assertEquals(tempDir, result, "should return the directory"),
          () -> assertTrue(Files.exists(getTableOrderingPath(tempDir)), "file should exist"),
          () ->
              assertEquals(
                  List.of("CUSTOM_TABLE"),
                  readTableOrderingFile(tempDir),
                  "content should remain unchanged"));
    }

    /**
     * Verifies that ensureTableOrdering creates sorted table ordering when file does not exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should create sorted table ordering when file does not exist")
    void shouldCreateSortedTableOrdering_whenFileDoesNotExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createDataFile(tempDir, "TABLE3.csv");
      createDataFile(tempDir, "TABLE1.csv");
      createDataFile(tempDir, "TABLE2.csv");

      // When
      final var result = resolver.ensureTableOrdering(tempDir);

      // Then
      assertAll(
          "table ordering file should be created with sorted table names",
          () -> assertEquals(tempDir, result, "should return the directory"),
          () -> assertTrue(Files.exists(getTableOrderingPath(tempDir)), "file should be created"),
          () ->
              assertEquals(
                  List.of("TABLE1", "TABLE2", "TABLE3"),
                  readTableOrderingFile(tempDir),
                  "tables should be sorted alphabetically"));
    }

    /**
     * Verifies that ensureTableOrdering creates empty table ordering when no data files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty table ordering when no data files exist")
    void shouldCreateEmptyTableOrdering_whenNoDataFiles(final @TempDir Path tempDir)
        throws IOException {
      // When
      final var result = resolver.ensureTableOrdering(tempDir);

      // Then
      assertAll(
          "table ordering file should be created as empty",
          () -> assertEquals(tempDir, result, "should return the directory"),
          () -> assertTrue(Files.exists(getTableOrderingPath(tempDir)), "file should be created"),
          () ->
              assertTrue(
                  readTableOrderingFile(tempDir).isEmpty(),
                  "content should be empty when no data files"));
    }

    /**
     * Verifies that ensureTableOrdering filters files by extension when creating table ordering.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should filter by extension when creating table ordering")
    void shouldFilterByExtension_whenCreatingTableOrdering(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createDataFile(tempDir, "TABLE1.csv");
      createDataFile(tempDir, "TABLE2.txt");
      createDataFile(tempDir, "TABLE3.csv");

      // When
      resolver.ensureTableOrdering(tempDir);

      // Then
      final var tableNames = readTableOrderingFile(tempDir);
      assertAll(
          "only matching extension files should be included",
          () -> assertEquals(2, tableNames.size(), "should include only csv files"),
          () -> assertTrue(tableNames.contains("TABLE1"), "should include TABLE1"),
          () -> assertTrue(tableNames.contains("TABLE3"), "should include TABLE3"),
          () -> assertFalse(tableNames.contains("TABLE2"), "should not include txt file"));
    }

    /**
     * Verifies that ensureTableOrdering throws exception when directory does not exist.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory does not exist")
    void shouldThrowException_whenDirectoryDoesNotExist(final @TempDir Path tempDir) {
      // Given
      final var nonExistentDir = tempDir.resolve("nonexistent");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class, () -> resolver.ensureTableOrdering(nonExistentDir));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("Failed to list data files"),
          "exception message should indicate failure to list files");
    }

    /**
     * Creates a data file with the specified name in the temp directory.
     *
     * @param tempDir the temporary directory
     * @param name the file name
     * @return the created file path
     * @throws IOException if file creation fails
     */
    private Path createDataFile(final Path tempDir, final String name) throws IOException {
      return Files.createFile(tempDir.resolve(name));
    }

    /**
     * Creates a table ordering file with the specified table names.
     *
     * @param tempDir the temporary directory
     * @param tableNames the table names to write
     * @throws IOException if file writing fails
     */
    private void createTableOrderingFile(final Path tempDir, final String... tableNames)
        throws IOException {
      Files.write(getTableOrderingPath(tempDir), List.of(tableNames));
    }

    /**
     * Reads the content of the table ordering file.
     *
     * @param tempDir the temporary directory
     * @return list of table names
     * @throws IOException if file reading fails
     */
    private List<String> readTableOrderingFile(final Path tempDir) throws IOException {
      return Files.readAllLines(getTableOrderingPath(tempDir));
    }

    /**
     * Returns the path to the table ordering file.
     *
     * @param tempDir the temporary directory
     * @return the table ordering file path
     */
    private Path getTableOrderingPath(final Path tempDir) {
      return tempDir.resolve(LOAD_ORDER_FILE);
    }
  }

  /** Test implementation of TableOrderingResolver for testing purposes. */
  private static class TestTableOrderingResolver extends TableOrderingResolver {

    /** The file extension supported by this resolver. */
    private final String fileExtension;

    /**
     * Creates a test table ordering resolver.
     *
     * @param fileExtension the file extension to support
     */
    TestTableOrderingResolver(final String fileExtension) {
      this.fileExtension = fileExtension;
    }

    @Override
    protected String getSupportedFileExtension() {
      return fileExtension;
    }
  }
}
