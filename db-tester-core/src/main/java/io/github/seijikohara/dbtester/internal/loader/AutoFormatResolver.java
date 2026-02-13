package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.format.spi.FormatRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves and loads dataset files from a directory by automatically detecting all supported
 * formats.
 *
 * <p>This resolver scans a directory for files with supported extensions (as registered in {@link
 * FormatRegistry}), groups them by format, and delegates parsing to the appropriate format
 * providers. It supports loading files of different formats from the same directory, provided that
 * no two files represent the same table name.
 *
 * <p>If the same table name appears in multiple file formats (e.g., {@code USER.csv} and {@code
 * USER.yaml}), a {@link DataSetLoadException} is thrown with details about all conflicting files.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see FormatRegistry
 */
public final class AutoFormatResolver {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(AutoFormatResolver.class);

  /** Creates a new auto format resolver. */
  public AutoFormatResolver() {}

  /**
   * Resolves and loads all supported dataset files from the specified directory.
   *
   * <p>Processing steps:
   *
   * <ol>
   *   <li>Lists all regular files in the directory
   *   <li>Filters files whose extensions have registered format providers
   *   <li>Detects table name conflicts across different formats
   *   <li>Delegates parsing to each format provider
   *   <li>Merges all tables into a single {@link TableSet}
   * </ol>
   *
   * @param directory the directory containing dataset files
   * @return a TableSet containing tables from all supported files
   * @throws DataSetLoadException if no supported files are found, table name conflicts exist, or
   *     I/O errors occur
   */
  public TableSet resolve(final Path directory) {
    final var supportedFiles = discoverSupportedFiles(directory);

    if (supportedFiles.isEmpty()) {
      final var foundFiles = listAllFileNames(directory);
      throw new DataSetLoadException(buildNoSupportedFilesMessage(directory, foundFiles));
    }

    logger.debug(
        "Discovered {} supported files in directory: {}", supportedFiles.size(), directory);

    detectTableNameConflicts(supportedFiles);

    final var groupedByExtension = groupByExtension(supportedFiles);
    final var allTables = new ArrayList<Table>();

    groupedByExtension.forEach(
        (extension, paths) -> {
          final var provider = FormatRegistry.getProvider(extension);
          logger.debug(
              "Parsing {} files with provider: {}",
              extension.value(),
              provider.getClass().getSimpleName());
          final var tableSet = provider.parse(directory);
          allTables.addAll(tableSet.getTables());
        });

    logger.debug("Loaded {} tables from {} format(s)", allTables.size(), groupedByExtension.size());

    return new SimpleTableSet(allTables);
  }

  /**
   * Discovers all files in the directory that have supported format extensions.
   *
   * @param directory the directory to scan
   * @return list of paths with supported extensions
   * @throws DataSetLoadException if the directory cannot be listed
   */
  private List<Path> discoverSupportedFiles(final Path directory) {
    try (final var paths = Files.list(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(
              path ->
                  FileExtension.fromFileName(path.getFileName().toString())
                      .map(FormatRegistry::hasProvider)
                      .orElse(false))
          .toList();
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list files in directory: %s", directory), e);
    }
  }

  /**
   * Detects table name conflicts where the same table name appears in files with different
   * extensions.
   *
   * @param supportedFiles the list of supported files to check
   * @throws DataSetLoadException if any table name conflicts are detected
   */
  private void detectTableNameConflicts(final List<Path> supportedFiles) {
    final var tableNameToFiles = new TreeMap<String, List<String>>();

    supportedFiles.forEach(
        path -> {
          final var fileName = path.getFileName().toString();
          final var tableName = extractTableName(fileName);
          tableNameToFiles.computeIfAbsent(tableName, k -> new ArrayList<>()).add(fileName);
        });

    final var conflicts =
        tableNameToFiles.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

    if (!conflicts.isEmpty()) {
      throw new DataSetLoadException(buildConflictMessage(conflicts));
    }
  }

  /**
   * Groups supported files by their file extension.
   *
   * @param supportedFiles the files to group
   * @return map of file extension to list of paths
   */
  private Map<FileExtension, List<Path>> groupByExtension(final List<Path> supportedFiles) {
    return supportedFiles.stream()
        .collect(
            Collectors.groupingBy(
                path ->
                    FileExtension.fromFileName(path.getFileName().toString())
                        .orElseThrow(
                            () ->
                                new DataSetLoadException(
                                    String.format(
                                        "Failed to extract extension from file: %s", path)))));
  }

  /**
   * Extracts the table name from a file name by removing the extension.
   *
   * @param fileName the file name (e.g., "USER.csv")
   * @return the table name (e.g., "USER")
   */
  private String extractTableName(final String fileName) {
    final var dotIndex = fileName.lastIndexOf('.');
    return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
  }

  /**
   * Lists all regular file names in the directory for diagnostic purposes.
   *
   * @param directory the directory to scan
   * @return sorted list of file names, or empty list if listing fails
   */
  private List<String> listAllFileNames(final Path directory) {
    try (final var paths = Files.list(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .sorted()
          .toList();
    } catch (final IOException e) {
      logger.debug("Failed to list files for diagnostics: {}", directory, e);
      return List.of();
    }
  }

  /**
   * Builds a detailed error message when no supported data files are found.
   *
   * <p>The message includes the directory path, supported extensions, a hint, and a list of files
   * found in the directory (if any) to help users diagnose the issue.
   *
   * @param directory the directory that was scanned
   * @param foundFiles the list of file names found in the directory
   * @return the formatted error message
   */
  private String buildNoSupportedFilesMessage(final Path directory, final List<String> foundFiles) {
    final var details = new StringBuilder();
    details.append(
        String.format(
            "Dataset directory exists but contains no supported data files: '%s'",
            directory.toAbsolutePath()));
    details.append(System.lineSeparator());
    details.append(
        String.format("Supported file extensions: %s", FormatRegistry.getSupportedExtensions()));
    details.append(System.lineSeparator());
    details.append("Hint: Add at least one data file (for example, TABLE_NAME.csv)...");

    if (!foundFiles.isEmpty()) {
      details.append(System.lineSeparator());
      details.append(String.format("Found files: %s", foundFiles));
    }

    return details.toString();
  }

  /**
   * Builds a detailed error message for table name conflicts.
   *
   * @param conflicts map of conflicting table names to their file names
   * @return the formatted error message
   */
  private String buildConflictMessage(final Map<String, List<String>> conflicts) {
    final var details = new StringBuilder();
    details.append("Table name conflict detected in AUTO format mode.");
    details.append(System.lineSeparator());
    details.append(
        "The following table names are defined in multiple files with different formats:");
    details.append(System.lineSeparator());

    conflicts.forEach(
        (tableName, fileNames) -> {
          details.append(System.lineSeparator());
          details.append(String.format("  Table '%s':", tableName));
          details.append(System.lineSeparator());
          fileNames.forEach(
              fileName -> {
                details.append(String.format("    - %s", fileName));
                details.append(System.lineSeparator());
              });
        });

    details.append(System.lineSeparator());
    details.append("Each table name must be unique across all file formats in a directory.");
    details.append(System.lineSeparator());
    details.append("To resolve, remove duplicate files or specify a concrete format:");
    details.append(System.lineSeparator());
    details.append("  DataFormat.CSV, DataFormat.TSV, DataFormat.JSON, or DataFormat.YAML");

    return details.toString();
  }
}
