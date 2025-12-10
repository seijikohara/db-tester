package io.github.seijikohara.dbtester.internal.format;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages table ordering files for dataset directories.
 *
 * <p>This class handles reading of table ordering files. The load order file (default: {@value
 * ConventionSettings#DEFAULT_LOAD_ORDER_FILE_NAME}) contains one table name per line. Empty lines
 * are ignored, lines starting with {@code #} are treated as comments, and table names are
 * case-insensitive for matching.
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

  /** The file name for load order specification. */
  private final String loadOrderFileName;

  /**
   * Creates a table ordering manager for the specified file extension with default load order file
   * name.
   *
   * @param fileExtension the file extension without leading dot (e.g., "csv", "tsv")
   */
  public TableOrdering(final String fileExtension) {
    this(fileExtension, ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME);
  }

  /**
   * Creates a table ordering manager for the specified file extension and load order file name.
   *
   * @param fileExtension the file extension without leading dot (e.g., "csv", "tsv")
   * @param loadOrderFileName the file name for load order specification
   */
  public TableOrdering(final String fileExtension, final String loadOrderFileName) {
    this.fileExtension = fileExtension;
    this.loadOrderFileName = loadOrderFileName;
  }

  /**
   * Checks if the load order file exists in the directory.
   *
   * @param directory the directory path to check
   * @return true if the load order file exists
   */
  public boolean hasLoadOrderFile(final Path directory) {
    final var orderingFile = directory.resolve(loadOrderFileName);
    return Files.exists(orderingFile);
  }

  /**
   * Reads the table order from the ordering file if it exists.
   *
   * @param directory the directory containing the ordering file
   * @return optional list of table names in order, or empty if no ordering file exists
   * @throws DataSetLoadException if reading fails
   */
  public Optional<List<String>> readTableOrder(final Path directory) {
    final var orderingFile = directory.resolve(loadOrderFileName);
    if (!Files.exists(orderingFile)) {
      return Optional.empty();
    }

    try {
      final var tableNames =
          Files.readAllLines(orderingFile, StandardCharsets.UTF_8).stream()
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .filter(line -> !line.startsWith("#"))
              .toList();
      logger.debug("Read {} table names from {}", tableNames.size(), loadOrderFileName);
      return Optional.of(tableNames);
    } catch (final IOException exception) {
      throw new DataSetLoadException(
          String.format("Failed to read table ordering file: %s", orderingFile), exception);
    }
  }

  /**
   * Reads the table order from the ordering file, throwing an error if not found.
   *
   * <p>Use this method when {@link
   * io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy#LOAD_ORDER_FILE} is
   * explicitly specified and the file must exist.
   *
   * @param directory the directory containing the ordering file
   * @return list of table names in order
   * @throws DataSetLoadException if the file does not exist or reading fails
   */
  public List<String> readTableOrderRequired(final Path directory) {
    final var orderingFile = directory.resolve(loadOrderFileName);
    if (!Files.exists(orderingFile)) {
      throw new DataSetLoadException(
          String.format(
              "%s not found in directory: %s. When TableOrderingStrategy.LOAD_ORDER_FILE is specified, the file must exist.",
              loadOrderFileName, directory));
    }

    try {
      final var tableNames =
          Files.readAllLines(orderingFile, StandardCharsets.UTF_8).stream()
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .filter(line -> !line.startsWith("#"))
              .toList();
      logger.debug("Read {} table names from {}", tableNames.size(), loadOrderFileName);
      return tableNames;
    } catch (final IOException exception) {
      throw new DataSetLoadException(
          String.format("Failed to read table ordering file: %s", orderingFile), exception);
    }
  }

  /**
   * Extracts table names alphabetically sorted from data files in a directory.
   *
   * @param directory the directory path containing data files
   * @return list of table names sorted alphabetically
   * @throws DataSetLoadException if directory listing fails
   */
  public List<TableName> extractTableNamesAlphabetically(final Path directory) {
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
