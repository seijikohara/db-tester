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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
          () -> assertEquals(0, result.tables().size(), "should have no tables"));
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
      assertEquals(1, result.tables().size(), "should have one table");

      final var table = result.tables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.name().value(), "should have correct table name"),
          () -> assertEquals(3, table.columns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.rowCount(), "should have 1 row"));
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
      final var table = result.tables().getFirst();
      assertEquals(3, table.rowCount(), "should have 3 rows");
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
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var nameValue = row.value(new ColumnName("NAME"));

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
      final var table = result.tables().getFirst();
      assertEquals(2, table.rowCount(), "should have 2 non-empty rows");
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
     * Verifies that parse throws exception when the header row contains duplicate column names.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when header has duplicate column names")
    void shouldThrowException_whenHeaderHasDuplicateColumns(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME,ID", "1,John,2");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> parser.parse(tempDir),
              "should throw DataSetLoadException");

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("Duplicate column header") && message.contains("ID"),
          "exception should mention the duplicated column header");
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
      final var table = result.tables().getFirst();
      assertEquals("MY_TABLE", table.name().value(), "should extract table name from filename");
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
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var descValue = row.value(new ColumnName("DESCRIPTION"));

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
      final var tableNames = result.tables().stream().map(t -> t.name().value()).toList();

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
      final var table = result.tables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.name().value(), "should have correct table name"),
          () -> assertEquals(3, table.columns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.rowCount(), "should have 1 row"));
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

  /** Tests for UTF-8 special character handling. */
  @Nested
  @DisplayName("special character handling")
  class SpecialCharacterTests {

    /** Tests for special character handling. */
    SpecialCharacterTests() {}

    /**
     * Verifies that parse handles Japanese characters.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle Japanese characters when UTF-8 data provided")
    void shouldHandleJapaneseCharacters_whenUtf8DataProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME,DESCRIPTION", "1,田中太郎,日本語テスト");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var nameValue = row.value(new ColumnName("NAME"));
      final var descValue = row.value(new ColumnName("DESCRIPTION"));
      assertNotNull(nameValue.value(), "NAME value should not be null");
      assertNotNull(descValue.value(), "DESCRIPTION value should not be null");
      final var nameStr = nameValue.value().toString();
      final var descStr = descValue.value().toString();
      assertAll(
          "row should contain Japanese characters",
          () -> assertEquals("田中太郎", nameStr, "should preserve Japanese name"),
          () -> assertEquals("日本語テスト", descStr, "should preserve Japanese description"));
    }

    /**
     * Verifies that parse handles accented characters.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle accented characters when Latin extended data provided")
    void shouldHandleAccentedCharacters_whenLatinExtendedDataProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME,CITY", "1,café,Zürich", "2,naïve,São Paulo");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var rows = table.rows();
      final var row1Name = rows.getFirst().value(new ColumnName("NAME"));
      final var row1City = rows.getFirst().value(new ColumnName("CITY"));
      final var row2City = rows.get(1).value(new ColumnName("CITY"));
      assertNotNull(row1Name.value(), "first row NAME value should not be null");
      assertNotNull(row1City.value(), "first row CITY value should not be null");
      assertNotNull(row2City.value(), "second row CITY value should not be null");
      final var nameStr = row1Name.value().toString();
      final var cityStr1 = row1City.value().toString();
      final var cityStr2 = row2City.value().toString();
      assertAll(
          "rows should contain accented characters",
          () -> assertEquals("café", nameStr, "should preserve accented name"),
          () -> assertEquals("Zürich", cityStr1, "should preserve accented city"),
          () -> assertEquals("São Paulo", cityStr2, "should preserve Portuguese city name"));
    }

    /**
     * Verifies that parse handles Chinese characters.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle Chinese characters when CJK data provided")
    void shouldHandleChineseCharacters_whenCjkDataProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME", "1,张伟", "2,李四");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      assertEquals(2, table.rowCount(), "should have 2 rows");
      final var nameValue = table.rows().getFirst().value(new ColumnName("NAME"));
      assertNotNull(nameValue.value(), "NAME value should not be null");
      assertEquals("张伟", nameValue.value().toString(), "should preserve Chinese name");
    }

    /**
     * Verifies that parse handles emoji characters.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle emoji characters when emoji data provided")
    void shouldHandleEmojiCharacters_whenEmojiDataProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      Files.writeString(
          tempDir.resolve("data.csv"), "ID,STATUS\n1,✅ done\n2,❌ failed", StandardCharsets.UTF_8);

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var statusValue = table.rows().getFirst().value(new ColumnName("STATUS"));
      assertNotNull(statusValue.value(), "STATUS value should not be null");
      assertEquals("✅ done", statusValue.value().toString(), "should preserve emoji characters");
    }
  }

  /** Tests for quoted field handling. */
  @Nested
  @DisplayName("quoted field handling")
  class QuotedFieldTests {

    /** Tests for quoted field handling. */
    QuotedFieldTests() {}

    /**
     * Verifies that parse handles escaped double quotes.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle escaped double quotes when doubled quotes exist")
    void shouldHandleEscapedDoubleQuotes_whenDoubledQuotesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,DESCRIPTION", "1,\"John \"\"IT Manager\"\" Smith\"");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var descValue = row.value(new ColumnName("DESCRIPTION"));
      assertNotNull(descValue.value(), "DESCRIPTION value should not be null");
      assertEquals(
          "John \"IT Manager\" Smith",
          descValue.value().toString(),
          "should unescape doubled quotes");
    }

    /**
     * Verifies that parse handles newlines within quoted fields.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle newlines within quoted fields")
    void shouldHandleNewlines_withinQuotedFields(final @TempDir Path tempDir) throws IOException {
      // Given
      Files.writeString(tempDir.resolve("data.csv"), "ID,DESCRIPTION\n1,\"Line1\nLine2\"");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var descValue = row.value(new ColumnName("DESCRIPTION"));
      assertNotNull(descValue.value(), "DESCRIPTION value should not be null");
      assertEquals(
          "Line1\nLine2",
          descValue.value().toString(),
          "should preserve newlines within quoted fields");
    }

    /**
     * Verifies that parse handles quoted fields containing only whitespace.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle quoted whitespace fields")
    void shouldHandleQuotedWhitespace_whenWhitespaceInQuotes(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,VALUE", "1,\"  \"");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      final var cellValue = row.value(new ColumnName("VALUE"));
      assertNotNull(cellValue.value(), "VALUE value should not be null");
      assertEquals(
          "  ", cellValue.value().toString(), "should preserve whitespace in quoted fields");
    }
  }

  /** Tests for large dataset processing. */
  @Nested
  @DisplayName("large dataset processing")
  class LargeDatasetTests {

    /** Tests for large dataset processing. */
    LargeDatasetTests() {}

    /**
     * Verifies that parse handles 1000-row dataset.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle 1000-row dataset when large file provided")
    void shouldHandle1000RowDataset_whenLargeFileProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var header = "ID,NAME,EMAIL,AMOUNT";
      final var rows =
          IntStream.rangeClosed(1, 1000)
              .mapToObj(
                  i ->
                      String.format(
                          "%d,User%d,user%d@example.com,%d.%02d", i, i, i, i * 10, i % 100))
              .collect(Collectors.joining("\n"));
      Files.writeString(tempDir.resolve("large.csv"), header + "\n" + rows);

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      assertAll(
          "large dataset should be parsed correctly",
          () -> assertEquals(1000, table.rowCount(), "should have 1000 rows"),
          () -> assertEquals(4, table.columns().size(), "should have 4 columns"));
    }

    /**
     * Verifies that parse handles multiple tables with many rows.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple large tables when several files provided")
    void shouldHandleMultipleLargeTables_whenSeveralFilesProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var header = "ID,VALUE";
      final var rows =
          IntStream.rangeClosed(1, 500)
              .mapToObj(i -> String.format("%d,Value%d", i, i))
              .collect(Collectors.joining("\n"));
      Files.writeString(tempDir.resolve("table_a.csv"), header + "\n" + rows);
      Files.writeString(tempDir.resolve("table_b.csv"), header + "\n" + rows);

      // When
      final var result = parser.parse(tempDir);

      // Then
      assertAll(
          "multiple tables should be parsed",
          () -> assertEquals(2, result.tables().size(), "should have 2 tables"),
          () ->
              assertEquals(
                  500, result.tables().getFirst().rowCount(), "first table should have 500 rows"),
          () ->
              assertEquals(
                  500, result.tables().get(1).rowCount(), "second table should have 500 rows"));
    }
  }

  /** Tests for malformed CSV handling. */
  @Nested
  @DisplayName("malformed CSV handling")
  class MalformedCsvTests {

    /** Tests for malformed CSV handling. */
    MalformedCsvTests() {}

    /**
     * Verifies that parse handles header-only file as empty table.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty table when file has header only")
    void shouldReturnEmptyTable_whenFileHasHeaderOnly(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME,EMAIL");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      assertAll(
          "table should have header but no rows",
          () -> assertEquals(3, table.columns().size(), "should have 3 columns"),
          () -> assertEquals(0, table.rowCount(), "should have 0 rows"));
    }

    /**
     * Verifies that parse handles rows with fewer columns than header.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle rows with fewer columns when column count differs")
    void shouldHandleRowsWithFewerColumns_whenColumnCountDiffers(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME,EMAIL", "1,John");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var row = table.rows().getFirst();
      assertEquals(
          CellValue.NULL, row.value(new ColumnName("EMAIL")), "missing column should be NULL");
    }

    /**
     * Verifies that parse handles rows with more columns than header.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should ignore extra columns when row has more columns than header")
    void shouldIgnoreExtraColumns_whenRowHasMoreColumnsThanHeader(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "data.csv", "ID,NAME", "1,John,extra_value,another_extra");

      // When
      final var result = parser.parse(tempDir);

      // Then
      final var table = result.tables().getFirst();
      final var nameValue = table.rows().getFirst().value(new ColumnName("NAME"));
      assertNotNull(nameValue.value(), "NAME value should not be null");
      final var nameStr = nameValue.value().toString();
      assertAll(
          "table should only have header columns",
          () -> assertEquals(2, table.columns().size(), "should have 2 columns"),
          () -> assertEquals("John", nameStr, "should have correct NAME value"));
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
