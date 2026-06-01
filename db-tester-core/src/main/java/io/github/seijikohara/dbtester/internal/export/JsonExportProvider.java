package io.github.seijikohara.dbtester.internal.export;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Export provider for JSON (JavaScript Object Notation) files.
 *
 * <p>This provider exports tables to JSON files in an array-of-objects format, compatible with
 * {@link io.github.seijikohara.dbtester.internal.format.json.JsonFormatProvider}.
 *
 * <p>Example output:
 *
 * <pre>{@code
 * [
 *   {"ID": 1, "NAME": "Alice", "EMAIL": "alice@example.com"},
 *   {"ID": 2, "NAME": "Bob", "EMAIL": "bob@example.com"}
 * ]
 * }</pre>
 */
public final class JsonExportProvider extends AbstractExportProvider {

  /** The JSON object mapper. */
  private final ObjectMapper objectMapper;

  /** Creates a new JSON export provider. */
  public JsonExportProvider() {
    this.objectMapper = createObjectMapper();
  }

  /**
   * Creates a new JSON export provider with the specified table reader.
   *
   * <p>This constructor is package-private for testing purposes.
   *
   * @param tableReader the table reader to use
   */
  JsonExportProvider(final TableReader tableReader) {
    super(tableReader);
    this.objectMapper = createObjectMapper();
  }

  /**
   * Creates and configures the object mapper.
   *
   * @return the configured object mapper
   */
  private static ObjectMapper createObjectMapper() {
    final var mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

  @Override
  public DataFormat supportedFormat() {
    return DataFormat.JSON;
  }

  @Override
  protected void writeTable(
      final Table table, final Path outputPath, final ExportConfiguration config) {
    final var columns = getExportableColumns(table, config);

    try (final var generator =
        objectMapper.createGenerator(outputPath.toFile(), JsonEncoding.UTF8)) {
      generator.useDefaultPrettyPrinter();
      generator.writeStartArray();

      table.rows().forEach(row -> writeRow(generator, row, columns, config));

      generator.writeEndArray();
    } catch (final IOException e) {
      throw new DatabaseTesterException(
          String.format("Failed to write table to: %s", outputPath), e);
    }
  }

  @Override
  protected String fileExtension() {
    return ".json";
  }

  /**
   * Gets the columns that should be exported.
   *
   * @param table the table
   * @param config the export configuration
   * @return the list of exportable columns
   */
  private List<ColumnName> getExportableColumns(
      final Table table, final ExportConfiguration config) {
    final var columns = table.columns();

    if (!table.rows().isEmpty()) {
      final var firstRow = table.rows().getFirst();
      return columns.stream().filter(col -> !shouldOmitColumn(firstRow, col, config)).toList();
    }

    return columns;
  }

  /**
   * Writes a row as a JSON object.
   *
   * @param generator the JSON generator
   * @param row the row to write
   * @param columns the columns to include
   * @param config the export configuration
   */
  private void writeRow(
      final JsonGenerator generator,
      final Row row,
      final List<ColumnName> columns,
      final ExportConfiguration config) {
    try {
      generator.writeStartObject();

      columns.forEach(
          column -> {
            try {
              generator.writeFieldName(column.value());
              writeValue(generator, row, column, config);
            } catch (final IOException e) {
              throw new DatabaseTesterException(
                  String.format("Failed to write column: %s", column.value()), e);
            }
          });

      generator.writeEndObject();
    } catch (final IOException e) {
      throw new DatabaseTesterException("Failed to write row", e);
    }
  }

  /**
   * Writes a value to the JSON generator.
   *
   * @param generator the JSON generator
   * @param row the row
   * @param column the column
   * @param config the export configuration
   * @throws IOException if writing fails
   */
  private void writeValue(
      final JsonGenerator generator,
      final Row row,
      final ColumnName column,
      final ExportConfiguration config)
      throws IOException {
    final var cellValue = row.values().get(column);

    if (cellValue == null || cellValue.isNull()) {
      generator.writeNull();
      return;
    }

    final var formatted = formatValue(cellValue, config);
    if (formatted == null) {
      generator.writeNull();
    } else {
      generator.writeString(formatted);
    }
  }
}
