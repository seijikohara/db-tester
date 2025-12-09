package io.github.seijikohara.dbtester.internal.format.parser;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser.Feature;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.SimpleDataSet;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for delimited text files (CSV, TSV, etc.) using Jackson Dataformat CSV.
 *
 * <p>This parser reads delimited files from a directory and converts them into a {@link DataSet}.
 * Each file represents a single database table, where the filename (without extension) becomes the
 * table name. The first row contains column headers, subsequent rows contain data, empty cells
 * represent NULL values, and values can be quoted with double quotes. Tables are loaded in
 * alphabetical order by filename.
 *
 * <p>This class is stateless and thread-safe. Instances can be safely shared between threads.
 *
 * @see DelimiterConfig
 * @see DataSet
 */
public final class DelimitedParser {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DelimitedParser.class);

  /** The delimiter configuration. */
  private final DelimiterConfig config;

  /** Jackson CSV mapper for parsing. */
  private final CsvMapper csvMapper;

  /** Jackson CSV schema for parsing. */
  private final CsvSchema csvSchema;

  /**
   * Creates a new parser with the specified delimiter configuration.
   *
   * @param config the delimiter configuration
   */
  public DelimitedParser(final DelimiterConfig config) {
    this.config = config;
    this.csvMapper = createCsvMapper();
    this.csvSchema = createCsvSchema(config);
  }

  /**
   * Creates a configured CsvMapper instance.
   *
   * @return the configured CsvMapper
   */
  private static CsvMapper createCsvMapper() {
    return CsvMapper.builder().enable(Feature.WRAP_AS_ARRAY).build();
  }

  /**
   * Creates a CsvSchema based on the delimiter configuration.
   *
   * @param config the delimiter configuration
   * @return the configured CsvSchema
   */
  private static CsvSchema createCsvSchema(final DelimiterConfig config) {
    return CsvSchema.emptySchema()
        .withColumnSeparator(config.delimiter())
        .withQuoteChar('"')
        .withEscapeChar('\\');
  }

  /**
   * Parses all matching files in the specified directory into a DataSet.
   *
   * @param directory the directory containing data files
   * @return the parsed dataset containing all tables
   * @throws DataSetLoadException if parsing fails
   */
  public DataSet parse(final Path directory) {
    if (!Files.isDirectory(directory)) {
      throw new DataSetLoadException(
          String.format("Not a directory: %s", directory.toAbsolutePath()));
    }

    logger.debug("Parsing {} files from directory: {}", config.extension(), directory);

    final var files = listDataFiles(directory);
    final var tables = files.stream().sorted().map(this::parseFile).toList();

    logger.debug("Parsed {} tables from directory: {}", tables.size(), directory);

    return new SimpleDataSet(tables);
  }

  /**
   * Lists all data files matching the configured extension in the directory.
   *
   * @param directory the directory to scan
   * @return list of file paths
   */
  private List<Path> listDataFiles(final Path directory) {
    final var extensionSuffix = config.extension();
    try (final Stream<Path> paths = Files.list(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(extensionSuffix))
          .toList();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list files in directory: %s", directory), e);
    }
  }

  /**
   * Parses a single file into a Table.
   *
   * <p>This method uses an imperative loop for iterator processing because the Jackson
   * MappingIterator API is cursor-based and requires sequential access. Converting to streams would
   * not provide meaningful benefits.
   *
   * @param file the file to parse
   * @return the parsed table
   */
  private Table parseFile(final Path file) {
    final var tableName = new TableName(extractTableName(file));

    logger.debug("Parsing file: {} as table: {}", file.getFileName(), tableName.value());

    try (final MappingIterator<String[]> iterator =
        csvMapper.readerFor(String[].class).with(csvSchema).readValues(file.toFile())) {

      if (!iterator.hasNext()) {
        throw new DataSetLoadException(String.format("File is empty: %s", file.toAbsolutePath()));
      }

      // First row is the header
      final var headerRow = iterator.next();
      final var columnNames = parseColumnNames(headerRow);

      // Remaining rows are data
      final var rows = new ArrayList<Row>();
      while (iterator.hasNext()) {
        final var values = iterator.next();
        if (!isEmptyRow(values)) {
          final var row = createRow(columnNames, values);
          rows.add(row);
        }
      }

      logger.debug(
          "Parsed table {} with {} columns and {} rows",
          tableName.value(),
          columnNames.size(),
          rows.size());

      return new SimpleTable(tableName, columnNames, rows);
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to parse file: %s", file.toAbsolutePath()), e);
    }
  }

  /**
   * Parses column names from the header row.
   *
   * @param headerRow the header row array
   * @return list of column names
   */
  private List<ColumnName> parseColumnNames(final String[] headerRow) {
    return java.util.Arrays.stream(headerRow).map(String::trim).map(ColumnName::new).toList();
  }

  /**
   * Checks if a row is empty (all values are blank or null).
   *
   * @param values the row values
   * @return true if the row is empty
   */
  private boolean isEmptyRow(final String[] values) {
    return java.util.Arrays.stream(values)
        .allMatch(value -> value == null || value.trim().isEmpty());
  }

  /**
   * Extracts the table name from a file path.
   *
   * @param file the file path
   * @return the table name (filename without extension)
   */
  private String extractTableName(final Path file) {
    final var fileName = file.getFileName().toString();
    final var dotIndex = fileName.lastIndexOf('.');
    return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
  }

  /**
   * Creates a Row from column names and values.
   *
   * @param columnNames the column names
   * @param values the values array
   * @return the created row
   */
  private Row createRow(final List<ColumnName> columnNames, final String[] values) {
    final Map<ColumnName, CellValue> rowValues = new LinkedHashMap<>();

    java.util.stream.IntStream.range(0, columnNames.size())
        .forEach(
            i -> {
              final var columnName = columnNames.get(i);
              final var rawValue = i < values.length ? values[i] : null;
              final var dataValue = toCellValue(rawValue);
              rowValues.put(columnName, dataValue);
            });

    return new SimpleRow(rowValues);
  }

  /**
   * Converts a raw string value to a CellValue.
   *
   * <p>Empty or null strings are converted to {@link CellValue#NULL}.
   *
   * @param rawValue the raw string value
   * @return the CellValue
   */
  private CellValue toCellValue(final @Nullable String rawValue) {
    if (rawValue == null || rawValue.isEmpty()) {
      return CellValue.NULL;
    }
    return new CellValue(rawValue);
  }
}
