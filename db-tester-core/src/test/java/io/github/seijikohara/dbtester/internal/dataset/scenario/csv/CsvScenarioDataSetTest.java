package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link CsvScenarioDataSet}. */
@DisplayName("CsvScenarioDataSet")
class CsvScenarioDataSetTest {

  /** Tests for the CsvScenarioDataSet class. */
  CsvScenarioDataSetTest() {}

  /** Table ordering file name. */
  private static final String TABLE_ORDERING_FILE = "table-ordering.txt";

  /** Tests for the constructor without scenario names. */
  @Nested
  @DisplayName("constructor without scenario names")
  class ConstructorWithoutScenarioNamesMethod {

    /** Tests for the constructor without scenario names. */
    ConstructorWithoutScenarioNamesMethod() {}

    /**
     * Verifies that constructor loads all rows without scenario filtering when scenario names not
     * provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load all rows without scenario filtering when scenario names not provided")
    void shouldLoadAllRowsWithoutScenarioFiltering_whenScenarioNamesNotProvided(
        final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1,COL2,COL3", "A,B,C", "D,E,F");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var tables = dataSet.getTables();
      assertAll(
          "should load all rows without filtering",
          () -> assertEquals(1, tables.size(), "should have one table"),
          () -> {
            final var table = tables.get(0);
            assertEquals("TABLE1", table.getName().value(), "table name should be TABLE1");
            assertEquals(
                List.of(new ColumnName("COL1"), new ColumnName("COL2"), new ColumnName("COL3")),
                table.getColumns(),
                "should have all columns");
            assertEquals(2, table.getRows().size(), "should have all rows");
          });
    }

    /**
     * Verifies that constructor creates table ordering file when file does not exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should create table ordering file when file does not exist")
    void shouldCreateTableOrderingFile_whenFileDoesNotExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var orderingFile = tempDir.resolve(TABLE_ORDERING_FILE);
      assertAll(
          "table ordering file should be created",
          () -> assertTrue(Files.exists(orderingFile), "file should exist"),
          () -> {
            final var content = Files.readAllLines(orderingFile);
            assertEquals(List.of("TABLE1"), content, "should contain TABLE1");
          });
    }

    /**
     * Verifies that constructor creates empty dataset when no CSV files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty dataset when no CSV files exist")
    void shouldCreateEmptyDataSet_whenNoCsvFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      assertEquals(0, dataSet.getTables().size(), "should have no tables");
    }
  }

  /** Tests for the constructor with scenario names. */
  @Nested
  @DisplayName("constructor with scenario names")
  class ConstructorWithScenarioNamesMethod {

    /** Tests for the constructor with scenario names. */
    ConstructorWithScenarioNamesMethod() {}

    /**
     * Verifies that constructor filters rows by scenario names when provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should filter rows by scenario names when scenario names provided")
    void shouldFilterRowsByScenarioNames_whenScenarioNamesProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(
          tempDir,
          "TABLE1.csv",
          "SCENARIO,COL1,COL2",
          "scenario1,A,B",
          "scenario2,C,D",
          "scenario1,E,F");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioNames, scenarioMarker, null);

      // Then
      final var tables = dataSet.getTables();
      assertAll(
          "should filter rows by scenario names",
          () -> assertEquals(1, tables.size(), "should have one table"),
          () -> {
            final var table = tables.get(0);
            final var rows = table.getRows();
            assertEquals(2, rows.size(), "should have only matching rows");
            assertEquals(
                new DataValue("A"),
                rows.get(0).getValue(new ColumnName("COL1")),
                "first row should have value A");
            assertEquals(
                new DataValue("E"),
                rows.get(1).getValue(new ColumnName("COL1")),
                "second row should have value E");
          });
    }

    /**
     * Verifies that constructor excludes scenario column when scenario marker matches.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should exclude scenario column when scenario marker matches")
    void shouldExcludeScenarioColumn_whenScenarioMarkerMatches(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "SCENARIO,COL1,COL2", "scenario1,A,B");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, List.of(), scenarioMarker, null);

      // Then
      final var table = dataSet.getTables().get(0);
      final var columns = table.getColumns();
      assertAll(
          "scenario column should be excluded",
          () -> assertEquals(2, columns.size(), "should have 2 columns"),
          () ->
              assertEquals(
                  List.of(new ColumnName("COL1"), new ColumnName("COL2")),
                  columns,
                  "should not include SCENARIO column"));
    }

    /**
     * Verifies that constructor normalizes empty strings to null when loading CSV data.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should normalize empty strings to null when loading CSV data")
    void shouldNormalizeEmptyStringsToNull_whenLoadingCsvData(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1,COL2,COL3", "A,,C", ",,", "D,E,F");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var table = dataSet.getTables().get(0);
      final var rows = table.getRows();
      final var col1 = new ColumnName("COL1");
      final var col2 = new ColumnName("COL2");
      final var col3 = new ColumnName("COL3");

      assertAll(
          "empty strings should be normalized to null",
          () -> assertEquals(3, rows.size(), "should have 3 rows"),
          () -> {
            final var row1 = rows.get(0);
            assertEquals(new DataValue("A"), row1.getValue(col1), "COL1 should be 'A'");
            assertTrue(row1.getValue(col2).isNull(), "COL2 should be null");
            assertEquals(new DataValue("C"), row1.getValue(col3), "COL3 should be 'C'");
          },
          () -> {
            final var row2 = rows.get(1);
            assertTrue(row2.getValue(col1).isNull(), "COL1 should be null");
            assertTrue(row2.getValue(col2).isNull(), "COL2 should be null");
            assertTrue(row2.getValue(col3).isNull(), "COL3 should be null");
          },
          () -> {
            final var row3 = rows.get(2);
            assertEquals(new DataValue("D"), row3.getValue(col1), "COL1 should be 'D'");
            assertEquals(new DataValue("E"), row3.getValue(col2), "COL2 should be 'E'");
            assertEquals(new DataValue("F"), row3.getValue(col3), "COL3 should be 'F'");
          });
    }
  }

  /** Tests for multiple tables scenarios. */
  @Nested
  @DisplayName("multiple tables")
  class MultipleTablesTest {

    /** Tests for multiple tables scenarios. */
    MultipleTablesTest() {}

    /**
     * Verifies that constructor loads multiple tables when multiple CSV files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should load multiple tables when multiple CSV files exist")
    void shouldLoadMultipleTables_whenMultipleCsvFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE2.csv", "COL2", "B");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var tables = dataSet.getTables();
      assertAll(
          "should load multiple tables",
          () -> assertEquals(2, tables.size(), "should have 2 tables"),
          () ->
              assertEquals(
                  "TABLE1", tables.get(0).getName().value(), "first table should be TABLE1"),
          () ->
              assertEquals(
                  "TABLE2", tables.get(1).getName().value(), "second table should be TABLE2"));
    }

    /**
     * Verifies that constructor uses existing table ordering file when file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should use existing table ordering file when file exists")
    void shouldUseExistingTableOrderingFile_whenFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE2.csv", "COL2", "B");
      createTableOrderingFile(tempDir, "TABLE2", "TABLE1");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var tables = dataSet.getTables();
      assertAll(
          "should use existing table ordering",
          () -> assertEquals(2, tables.size(), "should have 2 tables"),
          () ->
              assertEquals(
                  "TABLE2", tables.get(0).getName().value(), "first table should be TABLE2"),
          () ->
              assertEquals(
                  "TABLE1", tables.get(1).getName().value(), "second table should be TABLE1"));
    }

    /**
     * Verifies that constructor orders tables alphabetically when no ordering file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should order tables alphabetically when no ordering file exists")
    void shouldOrderTablesAlphabetically_whenNoOrderingFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE_C.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE_A.csv", "COL2", "B");
      createCsvFile(tempDir, "TABLE_B.csv", "COL3", "C");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // Then
      final var tables = dataSet.getTables();
      assertAll(
          "should order tables alphabetically",
          () -> assertEquals(3, tables.size(), "should have 3 tables"),
          () ->
              assertEquals(
                  "TABLE_A", tables.get(0).getName().value(), "first table should be TABLE_A"),
          () ->
              assertEquals(
                  "TABLE_B", tables.get(1).getName().value(), "second table should be TABLE_B"),
          () ->
              assertEquals(
                  "TABLE_C", tables.get(2).getName().value(), "third table should be TABLE_C"));
    }
  }

  /** Tests for the getTables() method. */
  @Nested
  @DisplayName("getTables() method")
  class GetTablesMethod {

    /** Tests for the getTables method. */
    GetTablesMethod() {}

    /**
     * Verifies that getTables returns all tables when called.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return all tables when called")
    void shouldReturnAllTables_whenCalled(final @TempDir Path tempDir) throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE2.csv", "COL2", "B");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var dataSet = new CsvScenarioDataSet(tempDir, scenarioMarker, null);

      // When
      final var tables = dataSet.getTables();

      // Then
      assertEquals(2, tables.size(), "should return all tables");
    }
  }

  /** Tests for error handling. */
  @Nested
  @DisplayName("error handling")
  class ErrorHandling {

    /** Tests for error handling. */
    ErrorHandling() {}

    /**
     * Verifies that constructor throws exception when directory does not exist.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory does not exist")
    void shouldThrowException_whenDirectoryDoesNotExist(final @TempDir Path tempDir) {
      // Given
      final var nonExistentDir = tempDir.resolve("nonexistent");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When & Then
      assertThrows(
          DataSetLoadException.class,
          () -> new CsvScenarioDataSet(nonExistentDir, scenarioMarker, null));
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
   * Creates a table ordering file with the specified table names.
   *
   * @param dir the directory to create the file in
   * @param tableNames the table names
   * @throws IOException if file creation fails
   */
  private static void createTableOrderingFile(final Path dir, final String... tableNames)
      throws IOException {
    Files.write(dir.resolve(TABLE_ORDERING_FILE), List.of(tableNames));
  }
}
