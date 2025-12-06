package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Handles creation and reuse of table ordering files for dataset directories.
 *
 * <p>The table ordering file name is {@value #TABLE_ORDERING_FILE}.
 */
public abstract class TableOrderingResolver {

  /** The name of the table ordering file. */
  private static final String TABLE_ORDERING_FILE = "table-ordering.txt";

  /** Creates a table ordering resolver. */
  protected TableOrderingResolver() {}

  /**
   * Returns the file extension supported by this dataset format.
   *
   * <p>This extension identifies which data files belong to this format. The extension should be
   * returned without the leading dot (e.g., "csv", "tsv").
   *
   * @return the supported file extension without leading dot
   */
  protected abstract String getSupportedFileExtension();

  /**
   * Ensures the table ordering file exists in the directory.
   *
   * <p>If the table ordering file exists, this method does nothing. If the file does not exist, a
   * default ordering file is created with tables sorted alphabetically.
   *
   * @param directory the directory path containing data files
   * @return the directory path (possibly with a generated table ordering file)
   * @throws DataSetLoadException if file operations fail
   */
  public final Path ensureTableOrdering(final Path directory) {
    final var orderingFile = directory.resolve(TABLE_ORDERING_FILE);
    if (!Files.exists(orderingFile)) {
      createDefaultTableOrdering(directory, orderingFile);
    }
    return directory;
  }

  /**
   * Creates a default table ordering file with alphabetically sorted table names.
   *
   * <p>This method scans the directory for data files with the format-specific extension, extracts
   * table names (file names without extension), sorts them alphabetically, and writes them to the
   * table ordering file.
   *
   * @param directory the directory path containing data files
   * @param orderingFile the table ordering file path to create
   * @throws DataSetLoadException if file operations fail
   */
  private void createDefaultTableOrdering(final Path directory, final Path orderingFile) {
    final var tableNames = extractTableNames(directory);
    writeTableOrdering(orderingFile, tableNames);
  }

  /**
   * Extracts table names from data files in a directory.
   *
   * @param directory the directory path containing data files
   * @return list of table names sorted alphabetically
   * @throws DataSetLoadException if directory listing fails
   */
  private List<TableName> extractTableNames(final Path directory) {
    final var extension = new FileExtension(String.format(".%s", getSupportedFileExtension()));
    return getDataFileStream(directory, extension)
        .map(Path::getFileName)
        .map(Path::toString)
        .filter(extension::matches)
        .map(name -> name.substring(0, name.length() - extension.value().length()))
        .map(TableName::new)
        .sorted()
        .toList();
  }

  /**
   * Writes table names to the table ordering file.
   *
   * @param orderingFile the table ordering file path to write
   * @param tableNames the list of table names
   * @throws DataSetLoadException if writing to the file fails
   */
  private static void writeTableOrdering(
      final Path orderingFile, final Collection<TableName> tableNames) {
    try {
      final var tableNameStrings = tableNames.stream().map(TableName::value).toList();
      Files.write(orderingFile, tableNameStrings);
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to write table ordering file: %s", orderingFile), e);
    }
  }

  /**
   * Gets a stream of data files in a directory.
   *
   * @param directory the directory path to scan
   * @param extension the file extension to filter
   * @return stream of data file paths
   * @throws DataSetLoadException if directory listing fails
   */
  private static Stream<Path> getDataFileStream(
      final Path directory, final FileExtension extension) {
    try (final var files = Files.list(directory)) {
      return files
          .filter(path -> extension.matches(path.getFileName().toString()))
          .toList()
          .stream();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list data files in directory: %s", directory), e);
    }
  }
}
