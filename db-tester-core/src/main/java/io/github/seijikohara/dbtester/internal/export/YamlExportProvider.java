package io.github.seijikohara.dbtester.internal.export;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
 * Export provider for YAML (YAML Ain't Markup Language) files.
 *
 * <p>This provider exports tables to YAML files in a list-of-mappings format, compatible with
 * {@link io.github.seijikohara.dbtester.internal.format.yaml.YamlFormatProvider}.
 *
 * <p>Example output:
 *
 * <pre>{@code
 * - ID: 1
 *   NAME: Alice
 *   EMAIL: alice@example.com
 * - ID: 2
 *   NAME: Bob
 *   EMAIL: bob@example.com
 * }</pre>
 */
public final class YamlExportProvider extends AbstractExportProvider {

  /** The YAML object mapper. */
  private final YAMLMapper yamlMapper;

  /** Creates a new YAML export provider. */
  public YamlExportProvider() {
    this.yamlMapper = createYamlMapper();
  }

  /**
   * Creates a new YAML export provider with the specified table reader.
   *
   * <p>This constructor is package-private for testing purposes.
   *
   * @param tableReader the table reader to use
   */
  YamlExportProvider(final TableReader tableReader) {
    super(tableReader);
    this.yamlMapper = createYamlMapper();
  }

  /**
   * Creates and configures the YAML mapper.
   *
   * @return the configured YAML mapper
   */
  private static YAMLMapper createYamlMapper() {
    final var factory = new YAMLFactory();
    factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
    return new YAMLMapper(factory);
  }

  @Override
  public DataFormat supportedFormat() {
    return DataFormat.YAML;
  }

  @Override
  protected void writeTable(
      final Table table, final Path outputPath, final ExportConfiguration config) {
    final var columns = getExportableColumns(table, config);

    try (final var generator = yamlMapper.createGenerator(outputPath.toFile(), JsonEncoding.UTF8)) {
      generator.writeStartArray();

      table.getRows().forEach(row -> writeRow(generator, row, columns, config));

      generator.writeEndArray();
    } catch (final IOException e) {
      throw new DatabaseTesterException(
          String.format("Failed to write table to: %s", outputPath), e);
    }
  }

  @Override
  protected String fileExtension() {
    return ".yaml";
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
    final var columns = table.getColumns();

    if (!table.getRows().isEmpty()) {
      final var firstRow = table.getRows().getFirst();
      return columns.stream().filter(col -> !shouldOmitColumn(firstRow, col, config)).toList();
    }

    return columns;
  }

  /**
   * Writes a row as a YAML mapping.
   *
   * @param generator the JSON generator (YAML uses Jackson's JSON generator)
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
   * Writes a value to the YAML generator.
   *
   * @param generator the generator
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
    final var cellValue = row.getValues().get(column);

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
