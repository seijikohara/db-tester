package io.github.seijikohara.dbtester.internal.export;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import io.github.seijikohara.dbtester.api.export.LobHandling;
import io.github.seijikohara.dbtester.api.spi.ExportProvider;
import io.github.seijikohara.dbtester.internal.domain.InternalConstants;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for export providers.
 *
 * <p>This class provides common functionality for exporting database tables to files, including
 * table reading, value formatting, and load order file generation.
 *
 * <p>Subclasses must implement {@link #writeTable(Table, Path, ExportConfiguration)} to handle
 * format-specific file writing.
 */
abstract class AbstractExportProvider implements ExportProvider {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(AbstractExportProvider.class);

  /** The table reader for fetching data from the database. */
  private final TableReader tableReader;

  /** Creates a new export provider with a default table reader. */
  protected AbstractExportProvider() {
    this.tableReader = new TableReader();
  }

  /**
   * Creates a new export provider with the specified table reader.
   *
   * @param tableReader the table reader to use
   */
  protected AbstractExportProvider(final TableReader tableReader) {
    this.tableReader = tableReader;
  }

  @Override
  public void export(
      final DataSource dataSource,
      final List<String> tableNames,
      final Path outputDirectory,
      final ExportConfiguration config) {
    logger.debug("Exporting {} tables to {}", tableNames.size(), outputDirectory);

    createDirectoryIfNeeded(outputDirectory);

    tableNames.forEach(
        tableName -> {
          final var table = tableReader.fetchTable(dataSource, tableName);
          final var outputPath = resolveOutputPath(outputDirectory, tableName);
          writeTable(table, outputPath, config);
          logger.debug("Exported table {} to {}", tableName, outputPath);
        });

    if (config.writeLoadOrderFile()) {
      writeLoadOrderFile(outputDirectory, tableNames, config.loadOrderFileName());
    }
  }

  @Override
  public void exportQuery(
      final DataSource dataSource,
      final String query,
      final String tableName,
      final Path outputDirectory,
      final ExportConfiguration config) {
    logger.debug("Exporting query result as {} to {}", tableName, outputDirectory);

    createDirectoryIfNeeded(outputDirectory);

    final var table = tableReader.executeQuery(dataSource, query, tableName);
    final var outputPath = resolveOutputPath(outputDirectory, tableName);
    writeTable(table, outputPath, config);
    logger.debug("Exported query result to {}", outputPath);
  }

  /**
   * Writes a table to a file.
   *
   * <p>Subclasses must implement this method to handle format-specific file writing.
   *
   * @param table the table to write
   * @param outputPath the output file path
   * @param config the export configuration
   */
  protected abstract void writeTable(Table table, Path outputPath, ExportConfiguration config);

  /**
   * Returns the file extension for this format.
   *
   * @return the file extension including the leading dot (e.g., ".csv")
   */
  protected abstract String fileExtension();

  /**
   * Resolves the output file path for a table.
   *
   * @param outputDirectory the output directory
   * @param tableName the table name
   * @return the output file path
   */
  protected Path resolveOutputPath(final Path outputDirectory, final String tableName) {
    return outputDirectory.resolve(tableName + fileExtension());
  }

  /**
   * Formats a cell value for export.
   *
   * @param value the cell value
   * @param config the export configuration
   * @return the formatted value, or null if the value should be omitted
   */
  protected @Nullable String formatValue(final CellValue value, final ExportConfiguration config) {
    if (value.isNull()) {
      return null;
    }

    final var rawValue = value.value();
    if (rawValue == null) {
      return null;
    }

    return formatRawValue(rawValue, config);
  }

  /**
   * Formats a raw value for export.
   *
   * @param value the raw value
   * @param config the export configuration
   * @return the formatted value, or null if the value should be omitted
   */
  protected @Nullable String formatRawValue(final Object value, final ExportConfiguration config) {
    // Handle Base64-encoded LOB values
    if (value instanceof String stringValue
        && stringValue.startsWith(InternalConstants.BASE64_PREFIX)) {
      if (config.lobHandling() == LobHandling.OMIT) {
        return null;
      }
      return stringValue;
    }

    // Handle byte arrays (BLOB)
    if (value instanceof byte[] bytes) {
      if (config.lobHandling() == LobHandling.OMIT) {
        return null;
      }
      return InternalConstants.BASE64_PREFIX + Base64.getEncoder().encodeToString(bytes);
    }

    // Handle date/time types
    if (value instanceof Timestamp timestamp) {
      return config.timestampFormatter().format(timestamp.toLocalDateTime());
    }
    if (value instanceof Date date) {
      return config.dateFormatter().format(date.toLocalDate());
    }
    if (value instanceof Time time) {
      return config.timeFormatter().format(time.toLocalTime());
    }
    if (value instanceof LocalDateTime localDateTime) {
      return config.timestampFormatter().format(localDateTime);
    }
    if (value instanceof LocalDate localDate) {
      return config.dateFormatter().format(localDate);
    }
    if (value instanceof LocalTime localTime) {
      return config.timeFormatter().format(localTime);
    }

    return value.toString();
  }

  /**
   * Checks if a column should be omitted from export.
   *
   * @param row a sample row to check
   * @param columnName the column name
   * @param config the export configuration
   * @return true if the column should be omitted
   */
  protected boolean shouldOmitColumn(
      final Row row, final ColumnName columnName, final ExportConfiguration config) {
    if (config.lobHandling() != LobHandling.OMIT) {
      return false;
    }

    final var value = row.values().get(columnName);
    if (value == null || value.isNull()) {
      return false;
    }

    final var rawValue = value.value();
    if (rawValue == null) {
      return false;
    }

    // Check if the value is a LOB
    if (rawValue instanceof byte[]) {
      return true;
    }
    if (rawValue instanceof String stringValue
        && stringValue.startsWith(InternalConstants.BASE64_PREFIX)) {
      return true;
    }

    return false;
  }

  /**
   * Creates the output directory if it does not exist.
   *
   * @param directory the directory to create
   */
  private void createDirectoryIfNeeded(final Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (final IOException e) {
      throw new DatabaseTesterException(
          String.format("Failed to create output directory: %s", directory), e);
    }
  }

  /**
   * Writes a load order file.
   *
   * @param outputDirectory the output directory
   * @param tableNames the table names in order
   * @param fileName the load order file name
   */
  private void writeLoadOrderFile(
      final Path outputDirectory, final List<String> tableNames, final String fileName) {
    final var loadOrderPath = outputDirectory.resolve(fileName);
    try {
      Files.write(loadOrderPath, tableNames, StandardCharsets.UTF_8);
      logger.debug("Wrote load order file to {}", loadOrderPath);
    } catch (final IOException e) {
      throw new DatabaseTesterException(
          String.format("Failed to write load order file: %s", loadOrderPath), e);
    }
  }
}
