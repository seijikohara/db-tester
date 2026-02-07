package io.github.seijikohara.dbtester.internal.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/** Unit tests for {@link JsonExportProvider}. */
@DisplayName("JsonExportProvider")
class JsonExportProviderTest {

  /** Tests for the JsonExportProvider class. */
  JsonExportProviderTest() {}

  /** Tests for the export functionality. */
  @Nested
  @DisplayName("export()")
  class ExportMethod {

    /** Tests for the export method. */
    ExportMethod() {}

    /** Temporary directory for test output. */
    @TempDir Path tempDir;

    /** Mock data source. */
    private DataSource dataSource;

    /** Mock table reader. */
    private TableReader tableReader;

    /** JSON object mapper. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      dataSource = mock(DataSource.class);
      tableReader = mock(TableReader.class);
    }

    /** Verifies that JSON provider returns JSON format. */
    @Test
    @Tag("normal")
    @DisplayName("should return JSON format")
    void shouldReturnJsonFormat() {
      // When
      final var provider = new JsonExportProvider();

      // Then
      assertEquals(DataFormat.JSON, provider.supportedFormat(), "should return JSON format");
    }

    /**
     * Verifies that export creates JSON file with correct content.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should create JSON file with correct content")
    void shouldCreateJsonFile_withCorrectContent() throws Exception {
      // Given
      final var table =
          createMockTable(
              "PRODUCTS",
              List.of("ID", "NAME"),
              List.of(
                  createMockRow(Map.of("ID", "1", "NAME", "Widget")),
                  createMockRow(Map.of("ID", "2", "NAME", "Gadget"))));
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new JsonExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("PRODUCTS"), tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("PRODUCTS.json");
      assertTrue(Files.exists(exportedFile), "should create JSON file");

      final JsonNode root = objectMapper.readTree(exportedFile.toFile());
      assertTrue(root.isArray(), "should be a JSON array");
      assertEquals(2, root.size(), "should have 2 rows");
      assertEquals("Widget", root.get(0).get("NAME").asText(), "should have correct first row");
      assertEquals("Gadget", root.get(1).get("NAME").asText(), "should have correct second row");
    }

    /**
     * Verifies that null values are represented as JSON null.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should represent null values as JSON null")
    void shouldRepresentNullAsJsonNull() throws Exception {
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

      final var provider = new JsonExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("USERS"), tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("USERS.json");
      final JsonNode root = objectMapper.readTree(exportedFile.toFile());
      assertTrue(root.get(0).get("NAME").isNull(), "should represent null as JSON null");
    }

    /**
     * Verifies that exportQuery creates JSON file with query results.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should export query result to JSON file")
    void shouldExportQueryResult() throws Exception {
      // Given
      final var table =
          createMockTable(
              "QUERY_RESULT",
              List.of("ID", "NAME"),
              List.of(createMockRow(Map.of("ID", "1", "NAME", "Alice"))));
      when(tableReader.executeQuery(any(DataSource.class), anyString(), anyString()))
          .thenReturn(table);

      final var provider = new JsonExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.exportQuery(
          dataSource, "SELECT * FROM USERS WHERE ID = 1", "SINGLE_USER", tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("SINGLE_USER.json");
      assertTrue(Files.exists(exportedFile), "should create JSON file");

      final JsonNode root = objectMapper.readTree(exportedFile.toFile());
      assertTrue(root.isArray(), "should be a JSON array");
      assertEquals(1, root.size(), "should have 1 row");
      assertEquals("Alice", root.get(0).get("NAME").asText(), "should have correct value");
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
