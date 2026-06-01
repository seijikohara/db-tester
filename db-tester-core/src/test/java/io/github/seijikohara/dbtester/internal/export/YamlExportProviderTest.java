package io.github.seijikohara.dbtester.internal.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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

/** Unit tests for {@link YamlExportProvider}. */
@DisplayName("YamlExportProvider")
class YamlExportProviderTest {

  /** Tests for the YamlExportProvider class. */
  YamlExportProviderTest() {}

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

    /** YAML mapper for parsing output. */
    private final YAMLMapper yamlMapper = new YAMLMapper();

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      dataSource = mock(DataSource.class);
      tableReader = mock(TableReader.class);
    }

    /** Verifies that YAML provider returns YAML format. */
    @Test
    @Tag("normal")
    @DisplayName("should return YAML format")
    void shouldReturnYamlFormat() {
      // When
      final var provider = new YamlExportProvider();

      // Then
      assertEquals(DataFormat.YAML, provider.supportedFormat(), "should return YAML format");
    }

    /**
     * Verifies that export creates YAML file with correct content.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should create YAML file with correct content")
    void shouldCreateYamlFile_withCorrectContent() throws Exception {
      // Given
      final var table =
          createMockTable(
              "ORDERS",
              List.of("ID", "CUSTOMER"),
              List.of(
                  createMockRow(Map.of("ID", "1", "CUSTOMER", "Alice")),
                  createMockRow(Map.of("ID", "2", "CUSTOMER", "Bob"))));
      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new YamlExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("ORDERS"), tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("ORDERS.yaml");
      assertTrue(Files.exists(exportedFile), "should create YAML file");

      final JsonNode root = yamlMapper.readTree(exportedFile.toFile());
      assertTrue(root.isArray(), "should be a YAML array");
      assertEquals(2, root.size(), "should have 2 rows");
      assertEquals("Alice", root.get(0).get("CUSTOMER").asText(), "should have correct first row");
      assertEquals("Bob", root.get(1).get("CUSTOMER").asText(), "should have correct second row");
    }

    /**
     * Verifies that null values are represented as YAML null.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should represent null values as YAML null")
    void shouldRepresentNullAsYamlNull() throws Exception {
      // Given
      final var col1 = new ColumnName("ID");
      final var col2 = new ColumnName("CUSTOMER");
      final var row = mock(Row.class);
      when(row.values()).thenReturn(Map.of(col1, new CellValue("1"), col2, CellValue.NULL));

      final var table = mock(Table.class);
      when(table.name()).thenReturn(new TableName("ORDERS"));
      when(table.columns()).thenReturn(List.of(col1, col2));
      when(table.rows()).thenReturn(List.of(row));

      when(tableReader.fetchTable(any(DataSource.class), anyString())).thenReturn(table);

      final var provider = new YamlExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.export(dataSource, List.of("ORDERS"), tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("ORDERS.yaml");
      final JsonNode root = yamlMapper.readTree(exportedFile.toFile());
      assertTrue(root.get(0).get("CUSTOMER").isNull(), "should represent null as YAML null");
    }

    /**
     * Verifies that exportQuery creates YAML file with query results.
     *
     * @throws Exception if test fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should export query result to YAML file")
    void shouldExportQueryResult() throws Exception {
      // Given
      final var table =
          createMockTable(
              "QUERY_RESULT",
              List.of("ID", "CUSTOMER"),
              List.of(createMockRow(Map.of("ID", "1", "CUSTOMER", "Alice"))));
      when(tableReader.executeQuery(any(DataSource.class), anyString(), anyString()))
          .thenReturn(table);

      final var provider = new YamlExportProvider(tableReader);
      final var config = ExportConfiguration.defaults();

      // When
      provider.exportQuery(
          dataSource, "SELECT * FROM ORDERS WHERE ID = 1", "SINGLE_ORDER", tempDir, config);

      // Then
      final var exportedFile = tempDir.resolve("SINGLE_ORDER.yaml");
      assertTrue(Files.exists(exportedFile), "should create YAML file");

      final JsonNode root = yamlMapper.readTree(exportedFile.toFile());
      assertTrue(root.isArray(), "should be a YAML array");
      assertEquals(1, root.size(), "should have 1 row");
      assertEquals("Alice", root.get(0).get("CUSTOMER").asText(), "should have correct value");
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
    when(table.name()).thenReturn(new TableName(tableName));
    when(table.columns()).thenReturn(columnNames.stream().map(ColumnName::new).toList());
    when(table.rows()).thenReturn(rows);
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
    when(row.values()).thenReturn(cellValues);
    return row;
  }
}
