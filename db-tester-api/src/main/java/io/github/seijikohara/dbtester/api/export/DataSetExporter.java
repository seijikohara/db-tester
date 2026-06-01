package io.github.seijikohara.dbtester.api.export;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.spi.ExportProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import javax.sql.DataSource;

/**
 * Facade for exporting database content to files.
 *
 * <p>This class provides static methods for exporting database tables to various file formats. It
 * uses the {@link ExportProvider} SPI to delegate the actual export work to format-specific
 * implementations.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Export tables to CSV files
 * DataSetExporter.csv(dataSource, List.of("USERS", "ORDERS"), Paths.get("export"));
 *
 * // Export with custom configuration
 * var config = ExportConfiguration.builder()
 *     .lobHandling(LobHandling.OMIT)
 *     .writeLoadOrderFile(true)
 *     .build();
 * DataSetExporter.export(dataSource, List.of("USERS"), Paths.get("export"), DataFormat.JSON, config);
 *
 * // Export query result
 * DataSetExporter.exportQuery(
 *     dataSource,
 *     "SELECT * FROM USERS WHERE active = true",
 *     "ACTIVE_USERS",
 *     Paths.get("export"),
 *     DataFormat.CSV);
 * }</pre>
 *
 * @see ExportProvider
 * @see ExportConfiguration
 * @see DataFormat
 */
public final class DataSetExporter {

  /** Private constructor to prevent instantiation. */
  private DataSetExporter() {}

  /**
   * Exports database tables to files in the specified format.
   *
   * <p>Each table is exported to a separate file in the output directory. The file name is the
   * table name with the format-specific extension.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @param format the export format
   * @throws DatabaseTesterException if export fails or no provider is found for the format
   * @throws IllegalArgumentException if format is {@link DataFormat#AUTO}
   */
  public static void export(
      final DataSource dataSource,
      final List<String> tableNames,
      final Path outputDirectory,
      final DataFormat format) {
    export(dataSource, tableNames, outputDirectory, format, ExportConfiguration.defaults());
  }

  /**
   * Exports database tables to files in the specified format with custom configuration.
   *
   * <p>Each table is exported to a separate file in the output directory. The file name is the
   * table name with the format-specific extension.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @param format the export format
   * @param config the export configuration
   * @throws DatabaseTesterException if export fails or no provider is found for the format
   * @throws IllegalArgumentException if format is {@link DataFormat#AUTO}
   */
  public static void export(
      final DataSource dataSource,
      final List<String> tableNames,
      final Path outputDirectory,
      final DataFormat format,
      final ExportConfiguration config) {
    final var provider = findProvider(format);
    provider.export(dataSource, tableNames, outputDirectory, config);
  }

  /**
   * Exports the result of a SQL query to a file in the specified format.
   *
   * <p>The query result is exported to a file named after the specified table name.
   *
   * @param dataSource the data source to execute the query on
   * @param query the SQL query to execute
   * @param tableName the table name to use for the output file
   * @param outputDirectory the directory to write the file to
   * @param format the export format
   * @throws DatabaseTesterException if export fails or no provider is found for the format
   * @throws IllegalArgumentException if format is {@link DataFormat#AUTO}
   */
  public static void exportQuery(
      final DataSource dataSource,
      final String query,
      final String tableName,
      final Path outputDirectory,
      final DataFormat format) {
    exportQuery(
        dataSource, query, tableName, outputDirectory, format, ExportConfiguration.defaults());
  }

  /**
   * Exports the result of a SQL query to a file in the specified format with custom configuration.
   *
   * <p>The query result is exported to a file named after the specified table name.
   *
   * @param dataSource the data source to execute the query on
   * @param query the SQL query to execute
   * @param tableName the table name to use for the output file
   * @param outputDirectory the directory to write the file to
   * @param format the export format
   * @param config the export configuration
   * @throws DatabaseTesterException if export fails or no provider is found for the format
   * @throws IllegalArgumentException if format is {@link DataFormat#AUTO}
   */
  public static void exportQuery(
      final DataSource dataSource,
      final String query,
      final String tableName,
      final Path outputDirectory,
      final DataFormat format,
      final ExportConfiguration config) {
    final var provider = findProvider(format);
    provider.exportQuery(dataSource, query, tableName, outputDirectory, config);
  }

  /**
   * Exports database tables to CSV files.
   *
   * <p>Convenience method equivalent to {@code export(dataSource, tableNames, outputDirectory,
   * DataFormat.CSV)}.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @throws DatabaseTesterException if export fails
   */
  public static void csv(
      final DataSource dataSource, final List<String> tableNames, final Path outputDirectory) {
    export(dataSource, tableNames, outputDirectory, DataFormat.CSV);
  }

  /**
   * Exports database tables to TSV files.
   *
   * <p>Convenience method equivalent to {@code export(dataSource, tableNames, outputDirectory,
   * DataFormat.TSV)}.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @throws DatabaseTesterException if export fails
   */
  public static void tsv(
      final DataSource dataSource, final List<String> tableNames, final Path outputDirectory) {
    export(dataSource, tableNames, outputDirectory, DataFormat.TSV);
  }

  /**
   * Exports database tables to JSON files.
   *
   * <p>Convenience method equivalent to {@code export(dataSource, tableNames, outputDirectory,
   * DataFormat.JSON)}.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @throws DatabaseTesterException if export fails
   */
  public static void json(
      final DataSource dataSource, final List<String> tableNames, final Path outputDirectory) {
    export(dataSource, tableNames, outputDirectory, DataFormat.JSON);
  }

  /**
   * Exports database tables to YAML files.
   *
   * <p>Convenience method equivalent to {@code export(dataSource, tableNames, outputDirectory,
   * DataFormat.YAML)}.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @throws DatabaseTesterException if export fails
   */
  public static void yaml(
      final DataSource dataSource, final List<String> tableNames, final Path outputDirectory) {
    export(dataSource, tableNames, outputDirectory, DataFormat.YAML);
  }

  /**
   * Finds an export provider for the specified format.
   *
   * @param format the data format
   * @return the export provider
   * @throws DatabaseTesterException if no provider is found for the format
   */
  private static ExportProvider findProvider(final DataFormat format) {
    if (format == DataFormat.AUTO) {
      throw new IllegalArgumentException(
          "AUTO format cannot be used for export."
              + " Specify a concrete format: CSV, TSV, JSON, or YAML.");
    }
    return ServiceLoader.load(ExportProvider.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(provider -> provider.supportedFormat() == format)
        .findFirst()
        .orElseThrow(
            () ->
                new DatabaseTesterException(
                    String.format("No ExportProvider found for format: %s", format)));
  }
}
