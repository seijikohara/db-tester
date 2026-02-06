package io.github.seijikohara.dbtester.internal.format.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for structured format files (JSON, YAML) using Jackson ObjectMapper.
 *
 * <p>This parser reads structured files from a directory and converts them into a {@link TableSet}.
 * Each file represents a single database table, where the filename (without extension) becomes the
 * table name. The file contains an array of objects, where each object represents a row with
 * key-value pairs for columns.
 *
 * <p>Column order is determined by the first row's key order. Null values in JSON/YAML are
 * converted to {@link CellValue#NULL}. All non-null values are converted to strings.
 *
 * <p>This class is stateless and thread-safe. Instances can be safely shared between threads.
 *
 * @see TableSet
 * @see ObjectMapper
 */
public final class StructuredFormatParser {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(StructuredFormatParser.class);

  /** Type reference for parsing array of maps. */
  private static final TypeReference<List<Map<String, Object>>> ROWS_TYPE = new RowsTypeReference();

  /**
   * Type reference for parsing array of maps.
   *
   * <p>Jackson requires a concrete subclass of TypeReference to preserve generic type information
   * at runtime for proper deserialization of the nested generic structure.
   */
  private static final class RowsTypeReference extends TypeReference<List<Map<String, Object>>> {

    /** Creates a new instance. */
    RowsTypeReference() {
      super();
    }
  }

  /** The file extension (including dot). */
  private final String extension;

  /** Jackson ObjectMapper for parsing. */
  private final ObjectMapper objectMapper;

  /**
   * Creates a new parser with the specified configuration.
   *
   * @param extension the file extension (e.g., ".json", ".yaml")
   * @param objectMapper the Jackson ObjectMapper configured for the format
   */
  public StructuredFormatParser(final String extension, final ObjectMapper objectMapper) {
    this.extension = extension;
    this.objectMapper = objectMapper;
  }

  /**
   * Parses all matching files in the specified directory into a TableSet.
   *
   * @param directory the directory containing data files
   * @return the parsed dataset containing all tables
   * @throws DataSetLoadException if parsing fails
   */
  public TableSet parse(final Path directory) {
    if (!Files.isDirectory(directory)) {
      throw new DataSetLoadException(
          String.format("Not a directory: %s", directory.toAbsolutePath()));
    }

    logger.debug("Parsing {} files from directory: {}", extension, directory);

    final var files = listDataFiles(directory);
    final var tables = files.stream().sorted().map(this::parseFile).toList();

    logger.debug("Parsed {} tables from directory: {}", tables.size(), directory);

    return new SimpleTableSet(tables);
  }

  /**
   * Lists all data files matching the configured extension in the directory.
   *
   * @param directory the directory to scan
   * @return list of file paths
   */
  private List<Path> listDataFiles(final Path directory) {
    try (final Stream<Path> paths = Files.list(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(extension))
          .toList();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list files in directory: %s", directory), e);
    }
  }

  /**
   * Parses a single file into a Table.
   *
   * @param file the file to parse
   * @return the parsed table
   */
  private Table parseFile(final Path file) {
    final var tableName = new TableName(extractTableName(file));

    logger.debug("Parsing file: {} as table: {}", file.getFileName(), tableName.value());

    try {
      final List<Map<String, Object>> rawRows = objectMapper.readValue(file.toFile(), ROWS_TYPE);

      if (rawRows.isEmpty()) {
        throw new DataSetLoadException(
            String.format(
                "File contains no rows (column definition required): %s", file.toAbsolutePath()));
      }

      // Determine column order from the first row
      final var columnNames = extractColumnNames(rawRows.getFirst());

      // Convert all rows
      final var rows = rawRows.stream().map(rawRow -> createRow(columnNames, rawRow)).toList();

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
   * Extracts column names from the first row's keys.
   *
   * @param firstRow the first row map
   * @return list of column names in key order
   */
  private List<ColumnName> extractColumnNames(final Map<String, Object> firstRow) {
    return firstRow.keySet().stream().map(ColumnName::new).toList();
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
   * Creates a Row from column names and a raw row map.
   *
   * @param columnNames the column names
   * @param rawRow the raw row map from JSON/YAML
   * @return the created row
   */
  private Row createRow(final List<ColumnName> columnNames, final Map<String, Object> rawRow) {
    final var rowValues = new LinkedHashMap<ColumnName, CellValue>();

    for (final var columnName : columnNames) {
      final var rawValue = rawRow.get(columnName.value());
      rowValues.put(columnName, toCellValue(rawValue));
    }

    return new SimpleRow(rowValues);
  }

  /**
   * Converts a raw object value to a CellValue.
   *
   * <p>Null values are converted to {@link CellValue#NULL}. All non-null values are converted to
   * their string representation.
   *
   * @param rawValue the raw object value
   * @return the CellValue
   */
  private CellValue toCellValue(final @Nullable Object rawValue) {
    if (rawValue == null) {
      return CellValue.NULL;
    }
    return new CellValue(rawValue.toString());
  }
}
