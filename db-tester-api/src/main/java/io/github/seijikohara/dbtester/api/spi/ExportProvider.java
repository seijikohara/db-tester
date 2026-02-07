package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import java.nio.file.Path;
import java.util.List;
import javax.sql.DataSource;

/**
 * SPI for exporting database content to files in various formats.
 *
 * <p>Implementations of this interface define how to export database tables to specific file
 * formats (e.g., CSV, TSV, JSON, YAML). Each implementation handles a single format and is
 * responsible for formatting values according to the provided configuration.
 *
 * <p>Providers are discovered automatically using Java's {@link java.util.ServiceLoader} mechanism.
 * Configure service providers in {@code
 * META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportProvider}.
 *
 * <p>Implementations must be thread-safe and stateless (or use immutable state).
 *
 * @see DataFormat
 * @see ExportConfiguration
 */
public interface ExportProvider {

  /**
   * Returns the data format supported by this provider.
   *
   * @return the supported data format
   */
  DataFormat supportedFormat();

  /**
   * Exports database tables to files in the specified directory.
   *
   * <p>Each table is exported to a separate file named after the table with the appropriate file
   * extension. The output directory structure follows the convention used by format providers for
   * loading datasets.
   *
   * @param dataSource the data source to read from
   * @param tableNames the names of tables to export
   * @param outputDirectory the directory to write files to
   * @param config the export configuration
   * @throws io.github.seijikohara.dbtester.api.exception.DatabaseTesterException if export fails
   */
  void export(
      DataSource dataSource,
      List<String> tableNames,
      Path outputDirectory,
      ExportConfiguration config);

  /**
   * Exports the result of a SQL query to a file.
   *
   * <p>The query result is exported to a file named after the specified table name with the
   * appropriate file extension.
   *
   * @param dataSource the data source to execute the query on
   * @param query the SQL query to execute
   * @param tableName the table name to use for the output file
   * @param outputDirectory the directory to write the file to
   * @param config the export configuration
   * @throws io.github.seijikohara.dbtester.api.exception.DatabaseTesterException if export fails
   */
  void exportQuery(
      DataSource dataSource,
      String query,
      String tableName,
      Path outputDirectory,
      ExportConfiguration config);
}
