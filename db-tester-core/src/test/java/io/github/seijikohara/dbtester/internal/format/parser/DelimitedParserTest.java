package io.github.seijikohara.dbtester.internal.format.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
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

/** Unit tests for {@link DelimitedParser}. */
@DisplayName("DelimitedParser")
class DelimitedParserTest {

  /** Tests for the DelimitedParser class. */
  DelimitedParserTest() {}

  /** The parser instance under test. */
  private DelimitedParser parser;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    parser = new DelimitedParser(DelimiterConfig.CSV);
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
      final var instance = new DelimitedParser(DelimiterConfig.CSV);

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor accepts TSV configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when TSV config provided")
    void shouldCreateInstance_whenTsvConfigProvided() {
      // When
      final var instance = new DelimitedParser(DelimiterConfig.TSV);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(Path) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /**
     * Verifies that parse returns empty dataset when directory has no matching files.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty dataset when no matching files exist")
    void shouldReturnEmptyDataSet_whenNoMatchingFilesExist(final @TempDir Path tempDir) {
      // When
      final var result = parser.parse(tempDir);

      // Then
      assertAll(
          "dataset should be empty",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(0, result.getTables().size(), "should have no tables"));
    }

    /**
     * Verifies that parse returns dataset when valid file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when valid file exists")
    void shouldReturnDataSet_whenValidFileExists(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME,EMAIL", "1,John,john@example.com");

      // When
      final var result = parser.parse(tempDir);

      // Then
      assertEquals(1, result.getTables().size(), "should have one table");

      final var table = result.getTables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.getName().value(), "should have correct table name"),
          () -> assertEquals(3, table.getColumns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.getRowCount(), "should have 1 row"));
    }

    /**
     * Verifies that parse handles multiple rows.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple rows when multiple data rows exist")
    void shouldHandleMultipleRows_whenMultipleDataRowsExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME", "1,John", "2,Jane", "3,Bob");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertEquals(3, table.getRowCount(), "should have 3 rows");
    }

    /**
     * Verifies that parse handles NULL values.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle NULL values when empty cells exist")
    void shouldHandleNullValues_whenEmptyCellsExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME,EMAIL", "1,,john@example.com");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameValue = row.getValue(new ColumnName("NAME"));

      assertEquals(CellValue.NULL, nameValue, "empty cell should be NULL");
    }

    /**
     * Verifies that parse skips empty rows.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should skip empty rows when blank lines exist")
    void shouldSkipEmptyRows_whenBlankLinesExist(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME", "1,John", ",", "2,Jane");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertEquals(2, table.getRowCount(), "should have 2 non-empty rows");
    }

    /**
     * Verifies that parse throws exception when path is not a directory.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when path is not a directory")
    void shouldThrowException_whenPathIsNotDirectory(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var file = tempDir.resolve("file.csv");
      Files.writeString(file, "ID,NAME");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> parser.parse(file),
              "should throw DataSetLoadException");

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("Not a directory"),
          "exception should mention not a directory");
    }

    /**
     * Verifies that parse throws exception when file is empty.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when file is empty")
    void shouldThrowException_whenFileIsEmpty(final @TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("empty.csv"), "");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> parser.parse(tempDir),
              "should throw DataSetLoadException");

      final var message = exception.getMessage();
      assertTrue(
          message != null && (message.contains("empty") || message.contains("File")),
          "exception should mention file issue");
    }

    /**
     * Verifies that parse extracts table name from filename.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should extract table name from filename")
    void shouldExtractTableName_fromFilename(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "MY_TABLE.csv", "ID,NAME", "1,John");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertEquals("MY_TABLE", table.getName().value(), "should extract table name from filename");
    }

    /**
     * Verifies that parse handles quoted values with commas.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle quoted values when values contain delimiter")
    void shouldHandleQuotedValues_whenValuesContainDelimiter(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,DESCRIPTION", "1,\"Hello, World\"");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var descValue = row.getValue(new ColumnName("DESCRIPTION"));

      assertNotNull(descValue.value(), "description value should not be null");
      assertEquals("Hello, World", descValue.value().toString(), "should preserve quoted content");
    }

    /**
     * Verifies that parse sorts tables alphabetically.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should sort tables alphabetically by filename")
    void shouldSortTablesAlphabetically_byFilename(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "zebra.csv", "ID", "1");
      createCsvFile(tempDir, "alpha.csv", "ID", "1");
      createCsvFile(tempDir, "beta.csv", "ID", "1");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var tableNames = result.getTables().stream().map(t -> t.getName().value()).toList();

      assertAll(
          "tables should be sorted alphabetically",
          () -> assertEquals("alpha", tableNames.get(0), "first table should be alpha"),
          () -> assertEquals("beta", tableNames.get(1), "second table should be beta"),
          () -> assertEquals("zebra", tableNames.get(2), "third table should be zebra"));
    }
  }

  /** Tests for TSV parsing. */
  @Nested
  @DisplayName("TSV parsing")
  class TsvParsing {

    /** Tests for TSV parsing. */
    TsvParsing() {}

    /** The TSV parser instance. */
    private DelimitedParser tsvParser;

    /** Sets up TSV parser before each test. */
    @BeforeEach
    void setUp() {
      tsvParser = new DelimitedParser(DelimiterConfig.TSV);
    }

    /**
     * Verifies that TSV parser handles tab-separated values.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should parse tab-separated values correctly")
    void shouldParseTabSeparatedValues_correctly(final @TempDir Path tempDir) throws IOException {
      // Given
      createTsvFile(tempDir, "users.tsv", "ID\tNAME\tEMAIL", "1\tJohn\tjohn@example.com");

      // When
      final var result = tsvParser.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.getName().value(), "should have correct table name"),
          () -> assertEquals(3, table.getColumns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.getRowCount(), "should have 1 row"));
    }

    /**
     * Creates a TSV file with the specified content.
     *
     * @param dir the directory to create the file in
     * @param fileName the file name
     * @param lines the TSV lines
     * @throws IOException if file creation fails
     */
    private void createTsvFile(final Path dir, final String fileName, final String... lines)
        throws IOException {
      final var content = String.join("\n", lines);
      Files.writeString(dir.resolve(fileName), content);
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
