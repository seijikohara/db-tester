package io.github.seijikohara.dbtester.internal.export;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import io.github.seijikohara.dbtester.internal.format.parser.DelimiterConfig;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Export provider for delimited file formats (CSV, TSV).
 *
 * <p>This class handles exporting tables to comma-separated or tab-separated files with proper
 * escaping and quoting of values.
 */
abstract class DelimitedExportProvider extends AbstractExportProvider {

  /** The delimiter configuration. */
  private final DelimiterConfig delimiterConfig;

  /**
   * Creates a new delimited export provider.
   *
   * @param delimiterConfig the delimiter configuration
   */
  protected DelimitedExportProvider(final DelimiterConfig delimiterConfig) {
    this.delimiterConfig = delimiterConfig;
  }

  /**
   * Creates a new delimited export provider with the specified table reader.
   *
   * <p>This constructor is package-private for testing purposes.
   *
   * @param delimiterConfig the delimiter configuration
   * @param tableReader the table reader to use
   */
  DelimitedExportProvider(final DelimiterConfig delimiterConfig, final TableReader tableReader) {
    super(tableReader);
    this.delimiterConfig = delimiterConfig;
  }

  @Override
  protected void writeTable(
      final Table table, final Path outputPath, final ExportConfiguration config) {
    final var columns = getExportableColumns(table, config);

    try (final var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
      // Write header row
      writeHeaderRow(writer, columns.stream().map(ColumnName::value).toList());

      // Write data rows
      table
          .getRows()
          .forEach(
              row -> {
                final List<@Nullable String> values = new java.util.ArrayList<>();
                columns.forEach(col -> values.add(formatCellValue(row, col, config)));
                writeDataRow(writer, values);
              });
    } catch (final IOException e) {
      throw new DatabaseTesterException(
          String.format("Failed to write table to: %s", outputPath), e);
    }
  }

  @Override
  protected String fileExtension() {
    return "." + delimiterConfig.extension();
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

    // If not omitting LOBs, return all columns
    if (!table.getRows().isEmpty()) {
      final var firstRow = table.getRows().getFirst();
      return columns.stream().filter(col -> !shouldOmitColumn(firstRow, col, config)).toList();
    }

    return columns;
  }

  /**
   * Formats a cell value for the delimited output.
   *
   * @param row the row containing the value
   * @param columnName the column name
   * @param config the export configuration
   * @return the formatted value
   */
  private @Nullable String formatCellValue(
      final Row row, final ColumnName columnName, final ExportConfiguration config) {
    final var cellValue = row.getValues().get(columnName);
    if (cellValue == null || cellValue.isNull()) {
      return config.nullValue();
    }
    final var formatted = formatValue(cellValue, config);
    return formatted != null ? formatted : config.nullValue();
  }

  /**
   * Writes a header row to the output.
   *
   * @param writer the writer
   * @param values the header values to write
   */
  private void writeHeaderRow(final BufferedWriter writer, final List<String> values) {
    try {
      final var line =
          values.stream()
              .map(this::escapeValue)
              .reduce((a, b) -> a + delimiterConfig.delimiter() + b)
              .orElse("");
      writer.write(line);
      writer.newLine();
    } catch (final IOException e) {
      throw new DatabaseTesterException("Failed to write header row", e);
    }
  }

  /**
   * Writes a data row to the output.
   *
   * @param writer the writer
   * @param values the values to write (may contain nulls)
   */
  private void writeDataRow(final BufferedWriter writer, final List<@Nullable String> values) {
    try {
      final var line =
          values.stream()
              .map(v -> v != null ? escapeValue(v) : "")
              .reduce((a, b) -> a + delimiterConfig.delimiter() + b)
              .orElse("");
      writer.write(line);
      writer.newLine();
    } catch (final IOException e) {
      throw new DatabaseTesterException("Failed to write data row", e);
    }
  }

  /**
   * Escapes a value for the delimited format.
   *
   * <p>Values containing the delimiter, quotes, or newlines are quoted and internal quotes are
   * doubled.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private String escapeValue(final String value) {
    final var delimiter = delimiterConfig.delimiter();
    final var needsQuoting =
        value.indexOf(delimiter) >= 0
            || value.indexOf('"') >= 0
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0;

    if (!needsQuoting) {
      return value;
    }

    // Escape quotes by doubling them and wrap in quotes
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  /**
   * CSV export provider.
   *
   * <p>Exports tables to comma-separated value files.
   */
  public static final class Csv extends DelimitedExportProvider {

    /** Creates a new CSV export provider. */
    public Csv() {
      super(DelimiterConfig.CSV);
    }

    /**
     * Creates a new CSV export provider with the specified table reader.
     *
     * <p>This constructor is package-private for testing purposes.
     *
     * @param tableReader the table reader to use
     */
    Csv(final TableReader tableReader) {
      super(DelimiterConfig.CSV, tableReader);
    }

    @Override
    public DataFormat supportedFormat() {
      return DataFormat.CSV;
    }
  }

  /**
   * TSV export provider.
   *
   * <p>Exports tables to tab-separated value files.
   */
  public static final class Tsv extends DelimitedExportProvider {

    /** Creates a new TSV export provider. */
    public Tsv() {
      super(DelimiterConfig.TSV);
    }

    /**
     * Creates a new TSV export provider with the specified table reader.
     *
     * <p>This constructor is package-private for testing purposes.
     *
     * @param tableReader the table reader to use
     */
    Tsv(final TableReader tableReader) {
      super(DelimiterConfig.TSV, tableReader);
    }

    @Override
    public DataFormat supportedFormat() {
      return DataFormat.TSV;
    }
  }
}
