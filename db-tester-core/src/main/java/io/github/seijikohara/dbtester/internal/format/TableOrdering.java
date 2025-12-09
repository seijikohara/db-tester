package io.github.seijikohara.dbtester.internal.format;

import static io.github.seijikohara.dbtester.internal.dataset.LoadOrderConstants.LOAD_ORDER_FILE;

import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages table ordering files for dataset directories.
 *
 * <p>This class handles the creation and reading of table ordering files. When a dataset directory
 * does not contain a table ordering file, one is automatically generated with tables sorted
 * alphabetically by filename.
 *
 * <p>The load order file ({@value
 * io.github.seijikohara.dbtester.internal.dataset.LoadOrderConstants#LOAD_ORDER_FILE}) contains one
 * table name per line. Empty lines are ignored, lines starting with {@code #} are treated as
 * comments, and table names are case-insensitive for matching.
 *
 * <p>This class is thread-safe and immutable.
 *
 * @see io.github.seijikohara.dbtester.internal.format.parser.DelimitedParser
 */
public final class TableOrdering {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TableOrdering.class);

  /** The file extension for data files. */
  private final String fileExtension;

  /**
   * Creates a table ordering manager for the specified file extension.
   *
   * @param fileExtension the file extension without leading dot (e.g., "csv", "tsv")
   */
  public TableOrdering(final String fileExtension) {
    this.fileExtension = fileExtension;
  }

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
  public Path ensureTableOrdering(final Path directory) {
    final var orderingFile = directory.resolve(LOAD_ORDER_FILE);
    if (!Files.exists(orderingFile)) {
      logger.debug("Creating table ordering file: {}", orderingFile);
      createDefaultTableOrdering(directory, orderingFile);
    }
    return directory;
  }

  /**
   * Reads the table order from the ordering file.
   *
   * @param directory the directory containing the ordering file
   * @return list of table names in order, or empty list if no ordering file exists
   * @throws DataSetLoadException if reading fails
   */
  public List<String> readTableOrder(final Path directory) {
    final var orderingFile = directory.resolve(LOAD_ORDER_FILE);
    if (!Files.exists(orderingFile)) {
      return List.of();
    }

    try {
      return Files.readAllLines(orderingFile, StandardCharsets.UTF_8).stream()
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .filter(line -> !line.startsWith("#"))
          .toList();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to read table ordering file: %s", orderingFile), e);
    }
  }

  /**
   * Creates a default table ordering file with alphabetically sorted table names.
   *
   * @param directory the directory path containing data files
   * @param orderingFile the table ordering file path to create
   * @throws DataSetLoadException if file operations fail
   */
  private void createDefaultTableOrdering(final Path directory, final Path orderingFile) {
    final var tableNames = extractTableNames(directory);
    writeTableOrdering(orderingFile, tableNames);
    logger.debug("Created table ordering file with {} tables", tableNames.size());
  }

  /**
   * Extracts table names from data files in a directory.
   *
   * @param directory the directory path containing data files
   * @return list of table names sorted alphabetically
   * @throws DataSetLoadException if directory listing fails
   */
  private List<TableName> extractTableNames(final Path directory) {
    final var extensionSuffix = String.format(".%s", fileExtension);
    return getDataFileStream(directory, extensionSuffix)
        .map(Path::getFileName)
        .map(Path::toString)
        .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(extensionSuffix))
        .map(name -> name.substring(0, name.length() - extensionSuffix.length()))
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
  private void writeTableOrdering(final Path orderingFile, final Collection<TableName> tableNames) {
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
   * @param extensionSuffix the file extension suffix to filter (e.g., ".csv")
   * @return stream of data file paths
   * @throws DataSetLoadException if directory listing fails
   */
  private Stream<Path> getDataFileStream(final Path directory, final String extensionSuffix) {
    try (final var files = Files.list(directory)) {
      return files
          .filter(
              path ->
                  path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(extensionSuffix))
          .toList()
          .stream();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list data files in directory: %s", directory), e);
    }
  }
}
