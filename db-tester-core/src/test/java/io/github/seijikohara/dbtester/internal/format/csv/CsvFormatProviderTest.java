package io.github.seijikohara.dbtester.internal.format.csv;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link CsvFormatProvider}. */
@DisplayName("CsvFormatProvider")
class CsvFormatProviderTest {

  /** Tests for the CsvFormatProvider class. */
  CsvFormatProviderTest() {}

  /** The provider instance under test. */
  private CsvFormatProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    provider = new CsvFormatProvider();
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
      final var instance = new CsvFormatProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the supportedFileExtension() method. */
  @Nested
  @DisplayName("supportedFileExtension() method")
  class SupportedFileExtensionMethod {

    /** Tests for the supportedFileExtension method. */
    SupportedFileExtensionMethod() {}

    /** Verifies that supportedFileExtension returns csv extension. */
    @Test
    @Tag("normal")
    @DisplayName("should return csv extension when called")
    void shouldReturnCsvExtension_whenCalled() {
      // When
      final var result = provider.supportedFileExtension();

      // Then
      assertEquals(new FileExtension("csv"), result, "should return csv extension");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(Path) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /**
     * Verifies that parse returns dataset when valid CSV file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when valid CSV file exists")
    void shouldReturnDataSet_whenValidCsvFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME,EMAIL", "1,John,john@example.com");

      // When
      final var result = provider.parse(tempDir);

      // Then
      assertAll(
          "dataset should contain parsed table",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getTables().size(), "should have one table"));

      final var table = result.getTables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.getName().value(), "should have correct table name"),
          () -> assertEquals(3, table.getColumns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.getRowCount(), "should have 1 row"));
    }

    /**
     * Verifies that parse handles multiple CSV files.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple CSV files when multiple files exist")
    void shouldHandleMultipleFiles_whenMultipleFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME", "1,John");
      createCsvFile(tempDir, "orders.csv", "ID,USER_ID,AMOUNT", "1,1,100");

      // When
      final var result = provider.parse(tempDir);

      // Then
      assertEquals(2, result.getTables().size(), "should have two tables");
    }

    /**
     * Verifies that parse handles NULL values correctly.
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
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameValue = row.getValue(new ColumnName("NAME"));

      assertEquals(CellValue.NULL, nameValue, "empty cell should be NULL");
    }

    /**
     * Verifies that parse handles quoted values.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle quoted values when values contain commas")
    void shouldHandleQuotedValues_whenValuesContainCommas(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME,ADDRESS", "1,John,\"123 Main St, Apt 4\"");

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var addressValue = row.getValue(new ColumnName("ADDRESS"));

      assertNotNull(addressValue.value(), "address value should not be null");
      assertTrue(
          addressValue.value().toString().contains("123 Main St"),
          "should preserve quoted value content");
    }

    /**
     * Verifies that parse does not create load-order.txt when not present.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should not create load-order.txt when not present")
    void shouldNotCreateLoadOrderFile_whenNotPresent(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "users.csv", "ID,NAME", "1,John");

      // When
      provider.parse(tempDir);

      // Then
      final var loadOrderFile = tempDir.resolve("load-order.txt");
      assertTrue(!Files.exists(loadOrderFile), "load-order.txt should not be created");
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
