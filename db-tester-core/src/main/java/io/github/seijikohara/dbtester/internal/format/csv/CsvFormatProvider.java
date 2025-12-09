package io.github.seijikohara.dbtester.internal.format.csv;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.format.TableOrdering;
import io.github.seijikohara.dbtester.internal.format.parser.DelimitedParser;
import io.github.seijikohara.dbtester.internal.format.parser.DelimiterConfig;
import io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;
import java.nio.file.Path;

/**
 * Format provider for CSV (Comma-Separated Values) files.
 *
 * <p>This provider parses CSV files from a directory and converts them into a {@link DataSet}. Each
 * CSV file represents a single database table, where the filename (without extension) becomes the
 * table name. The CSV format uses comma as the delimiter, with the first row containing column
 * headers, empty cells representing NULL values, and support for double-quoted values.
 *
 * <p>If a {@code load-order.txt} file does not exist in the directory, one is automatically created
 * with tables sorted alphabetically.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see FormatProvider
 * @see DelimitedParser
 * @see DelimiterConfig#CSV
 */
public final class CsvFormatProvider implements FormatProvider {

  /** The file extension for CSV files. */
  private static final FileExtension FILE_EXTENSION = new FileExtension("csv");

  /** The parser for CSV files. */
  private final DelimitedParser parser;

  /** The table ordering manager. */
  private final TableOrdering tableOrdering;

  /**
   * Creates a new CSV format provider.
   *
   * <p>This constructor is used by ServiceLoader for automatic discovery.
   */
  public CsvFormatProvider() {
    this.parser = new DelimitedParser(DelimiterConfig.CSV);
    this.tableOrdering = new TableOrdering(DelimiterConfig.CSV.extension());
  }

  /**
   * {@inheritDoc}
   *
   * @return the CSV file extension
   */
  @Override
  public FileExtension supportedFileExtension() {
    return FILE_EXTENSION;
  }

  /**
   * {@inheritDoc}
   *
   * @param directory the directory containing CSV files
   * @return the parsed dataset containing all tables
   */
  @Override
  public DataSet parse(final Path directory) {
    tableOrdering.ensureTableOrdering(directory);
    return parser.parse(directory);
  }
}
