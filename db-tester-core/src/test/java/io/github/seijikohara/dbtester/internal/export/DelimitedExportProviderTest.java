package io.github.seijikohara.dbtester.internal.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link DelimitedExportProvider}. */
@DisplayName("DelimitedExportProvider")
class DelimitedExportProviderTest {

  /** Tests for the DelimitedExportProvider class. */
  DelimitedExportProviderTest() {}

  /** Tests for the Csv provider. */
  @Nested
  @DisplayName("Csv")
  class CsvTests {

    /** Tests for the Csv provider. */
    CsvTests() {}

    /** Temporary directory for test output. */
    @TempDir Path tempDir;

    /** Mock data source. */
    private DataSource dataSource;

    /** Mock table reader. */
    private TableReader tableReader;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      dataSource = mock(DataSource.class);
      tableReader = mock(TableReader.class);
    }

    /** Verifies that CSV provider returns CSV format. */
    @Test
    @Tag("normal")
    @DisplayName("should return CSV format")
    void shouldReturnCsvFormat() {
      // When
      final var provider = new DelimitedExportProvider.Csv();

      // Then
      assertEquals(DataFormat.CSV, provider.supportedFormat(), "should return CSV format");
    }

    /**
     * Verifies that export creates CSV file with correct content.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should create CSV file with correct content")
    void shouldCreateCsvFile_withCorrectContent() throws Exception {
      // Given
      final var table =
          createMockTable(
              "USERS",
              List.of("ID", "NAME"),
              List.of(
                  createMockRow(Map.of("ID", "1", "NAME", "Alice")),
                  createMockRow(Map.of("ID", "2", "NAME", "Bob"))));
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new DelimitedExportProvider.Csv(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("USERS.csv");
      assertTrue(Files.exists(exportedFile), "should create CSV file");

      final var lines = Files.readAllLines(exportedFile);
      assertEquals(3, lines.size(), "should have header and 2 data rows");
      assertEquals("ID,NAME", lines.get(0), "should have correct header");
      assertEquals("1,Alice", lines.get(1), "should have correct first row");
      assertEquals("2,Bob", lines.get(2), "should have correct second row");
    }

    /**
     * Verifies that values with commas are escaped.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should escape values with commas")
    void shouldEscapeValuesWithCommas() throws Exception {
      // Given
      final var table =
          createMockTable(
              "USERS",
              List.of("ID", "NAME"),
              List.of(createMockRow(Map.of("ID", "1", "NAME", "Doe, John"))));
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new DelimitedExportProvider.Csv(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      final var content = Files.readString(tempDir.resolve("USERS.csv"));
      assertTrue(content.contains("\"Doe, John\""), "should escape value with comma");
    }

    /**
     * Verifies that null values are represented as empty strings.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should represent null values as empty strings")
    void shouldRepresentNullAsEmptyString() throws Exception {
      // Given
      final var col1 = new ColumnName("ID");
      final var col2 = new ColumnName("NAME");
      final var row = mock(Row.class);
      when(row.getValues()).thenReturn(Map.of(col1, new CellValue("1"), col2, CellValue.NULL));

      final var table = mock(Table.class);
      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(col1, col2));
      when(table.getRows()).thenReturn(List.of(row));

      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new DelimitedExportProvider.Csv(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      final var lines = Files.readAllLines(tempDir.resolve("USERS.csv"));
      assertEquals("1,", lines.get(1), "should represent null as empty string");
    }

    /**
     * Verifies that load order file is generated when configured.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should generate load order file when configured")
    void shouldGenerateLoadOrderFile_whenConfigured() throws Exception {
      // Given
      final var table = createMockTable("USERS", List.of("ID"), List.of());
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new DelimitedExportProvider.Csv(tableReader);
      final var config = ExportConfiguration.builder().writeLoadOrderFile(true).build();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      final var loadOrderFile = tempDir.resolve("load-order.txt");
      assertTrue(Files.exists(loadOrderFile), "should create load order file");
      assertEquals("USERS", Files.readAllLines(loadOrderFile).get(0), "should contain table name");
    }

    /**
     * Verifies that load order file is not generated by default.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should not generate load order file by default")
    void shouldNotGenerateLoadOrderFile_byDefault() throws Exception {
      // Given
      final var table = createMockTable("USERS", List.of("ID"), List.of());
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new DelimitedExportProvider.Csv(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      assertFalse(
          Files.exists(tempDir.resolve("load-order.txt")),
          "should not create load order file by default");
    }
  }

  /** Tests for the Tsv provider. */
  @Nested
  @DisplayName("Tsv")
  class TsvTests {

    /** Tests for the Tsv provider. */
    TsvTests() {}

    /** Verifies that TSV provider returns TSV format. */
    @Test
    @Tag("normal")
    @DisplayName("should return TSV format")
    void shouldReturnTsvFormat() {
      // When
      final var provider = new DelimitedExportProvider.Tsv();

      // Then
      assertEquals(DataFormat.TSV, provider.supportedFormat(), "should return TSV format");
    }
  }

  /**
   * Creates a mock table with the specified properties.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param rows the rows
   * @return the mock table
   */
  private static Table createMockTable(
      final String tableName, final List<String> columnNames, final List<Row> rows) {
    final var table = mock(Table.class);
    when(table.getName()).thenReturn(new TableName(tableName));
    when(table.getColumns()).thenReturn(columnNames.stream().map(ColumnName::new).toList());
    when(table.getRows()).thenReturn(rows);
    return table;
  }

  /**
   * Creates a mock row with the specified values.
   *
   * @param values the column-value pairs
   * @return the mock row
   */
  private static Row createMockRow(final Map<String, String> values) {
    final var row = mock(Row.class);
    final Map<ColumnName, CellValue> cellValues =
        values.entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> new ColumnName(e.getKey()), e -> new CellValue(e.getValue())));
    when(row.getValues()).thenReturn(cellValues);
    return row;
  }
}
