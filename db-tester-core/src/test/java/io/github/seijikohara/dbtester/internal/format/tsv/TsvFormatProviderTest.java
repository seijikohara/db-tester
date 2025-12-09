package io.github.seijikohara.dbtester.internal.format.tsv;

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

/** Unit tests for {@link TsvFormatProvider}. */
@DisplayName("TsvFormatProvider")
class TsvFormatProviderTest {

  /** Tests for the TsvFormatProvider class. */
  TsvFormatProviderTest() {}

  /** The provider instance under test. */
  private TsvFormatProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    provider = new TsvFormatProvider();
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
      final var instance = new TsvFormatProvider();

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

    /** Verifies that supportedFileExtension returns tsv extension. */
    @Test
    @Tag("normal")
    @DisplayName("should return tsv extension when called")
    void shouldReturnTsvExtension_whenCalled() {
      // When
      final var result = provider.supportedFileExtension();

      // Then
      assertEquals(new FileExtension("tsv"), result, "should return tsv extension");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(Path) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /**
     * Verifies that parse returns dataset when valid TSV file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when valid TSV file exists")
    void shouldReturnDataSet_whenValidTsvFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createTsvFile(tempDir, "users.tsv", "ID\tNAME\tEMAIL", "1\tJohn\tjohn@example.com");

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
     * Verifies that parse handles multiple TSV files.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple TSV files when multiple files exist")
    void shouldHandleMultipleFiles_whenMultipleFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createTsvFile(tempDir, "users.tsv", "ID\tNAME", "1\tJohn");
      createTsvFile(tempDir, "orders.tsv", "ID\tUSER_ID\tAMOUNT", "1\t1\t100");

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
      createTsvFile(tempDir, "users.tsv", "ID\tNAME\tEMAIL", "1\t\tjohn@example.com");

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameValue = row.getValue(new ColumnName("NAME"));

      assertEquals(CellValue.NULL, nameValue, "empty cell should be NULL");
    }

    /**
     * Verifies that parse creates load-order.txt when not present.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should create load-order.txt when not present")
    void shouldCreateLoadOrderFile_whenNotPresent(final @TempDir Path tempDir) throws IOException {
      // Given
      createTsvFile(tempDir, "users.tsv", "ID\tNAME", "1\tJohn");

      // When
      provider.parse(tempDir);

      // Then
      final var loadOrderFile = tempDir.resolve("load-order.txt");
      assertTrue(Files.exists(loadOrderFile), "load-order.txt should be created");
    }
  }

  /**
   * Creates a TSV file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param lines the TSV lines
   * @throws IOException if file creation fails
   */
  private static void createTsvFile(final Path dir, final String fileName, final String... lines)
      throws IOException {
    final var content = String.join("\n", lines);
    Files.writeString(dir.resolve(fileName), content);
  }
}
