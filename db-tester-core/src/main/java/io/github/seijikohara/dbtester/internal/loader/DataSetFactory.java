package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.format.spi.FormatRegistry;
import io.github.seijikohara.dbtester.internal.scenario.FilteredDataSet;
import io.github.seijikohara.dbtester.internal.scenario.ScenarioFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating dataset instances from various file formats.
 *
 * <p>This class encapsulates the logic for loading datasets with scenario filtering. It
 * automatically detects the file format based on file extensions and delegates to the appropriate
 * format provider from the registry.
 *
 * <p>The factory detects the format by examining files in the test data directory. It looks for the
 * first data file and determines the format from its file extension. Supported formats are
 * determined by registered {@link
 * io.github.seijikohara.dbtester.internal.format.spi.FormatProvider} implementations.
 *
 * <p>The factory creates datasets by detecting the file format from directory contents, retrieving
 * the appropriate format provider from {@link FormatRegistry}, parsing the data files into a raw
 * DataSet, and applying scenario filtering to include only relevant rows.
 *
 * <p>When data files include a scenario marker column, only rows matching the specified scenario
 * names are included in the dataset. This allows organizing multiple test scenarios within the same
 * data files.
 *
 * <p>New file formats can be supported by implementing {@link
 * io.github.seijikohara.dbtester.internal.format.spi.FormatProvider} and registering it with {@link
 * FormatRegistry}. The registry auto-discovers providers via ServiceLoader.
 *
 * <p>This class is stateless and thread-safe. All methods are side-effect free.
 *
 * @see DataSet
 * @see FormatRegistry
 * @see io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider
 * @see io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider
 */
public final class DataSetFactory {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DataSetFactory.class);

  /** Creates a new dataset factory. */
  public DataSetFactory() {}

  /**
   * Loads a scenario dataset from a directory using the specified data format.
   *
   * <p>This method uses the configured data format to load files, retrieves the appropriate
   * provider from the registry, parses the data files, and applies scenario filtering. Each data
   * file becomes a table in the dataset.
   *
   * <p>If scenario names are provided, only rows with a matching scenario marker column value are
   * included. If files do not have a scenario marker column, all rows are included.
   *
   * @param directory the directory path containing data files (one file per table)
   * @param scenarioNames the scenario names to filter rows; if empty, all rows are included
   * @param scenarioMarker the scenario marker identifying the special column for scenario filtering
   * @param dataFormat the file format to use (CSV or TSV)
   * @param dataSource the data source to associate with this dataset, or {@code null}
   * @return the loaded dataset with scenario filtering applied
   * @throws DataSetLoadException if the dataset cannot be created or loaded (unsupported format,
   *     empty directory, I/O errors, etc.)
   */
  DataSet createDataSet(
      final Path directory,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker,
      final DataFormat dataFormat,
      final @Nullable DataSource dataSource) {

    logger.debug("Creating dataset from directory: {} with format: {}", directory, dataFormat);

    validateDirectory(directory);
    validateDataFilesExist(directory, dataFormat.getExtension());

    final var fileExtension = new FileExtension(dataFormat.getExtension());
    final var provider = FormatRegistry.getProvider(fileExtension);

    logger.debug(
        "Using format provider: {} for extension: {}",
        provider.getClass().getSimpleName(),
        fileExtension.value());

    final var rawDataSet = provider.parse(directory);
    final var filter = new ScenarioFilter(scenarioMarker, scenarioNames);

    logger.debug("Applied scenario filter with {} scenario names", scenarioNames.size());

    return new FilteredDataSet(rawDataSet, filter, dataSource);
  }

  /**
   * Validates that the specified path is an existing directory.
   *
   * @param directory the directory path to validate
   * @throws DataSetLoadException if the directory does not exist
   */
  private void validateDirectory(final Path directory) {
    if (!Files.isDirectory(directory)) {
      throw new DataSetLoadException(
          String.format("Directory does not exist: %s", directory.toAbsolutePath()));
    }
  }

  /**
   * Validates that the directory contains at least one data file with the specified extension.
   *
   * @param directory the directory to check
   * @param extension the file extension to look for
   * @throws DataSetLoadException if no matching files exist or if an I/O error occurs
   */
  private void validateDataFilesExist(final Path directory, final String extension) {
    final boolean hasDataFiles;
    try (final var paths = Files.list(directory)) {
      hasDataFiles =
          paths.filter(Files::isRegularFile).anyMatch(path -> hasExtension(path, extension));
    } catch (final IOException exception) {
      throw new DataSetLoadException(
          String.format("Failed to list files in directory: %s", directory), exception);
    }

    if (!hasDataFiles) {
      throw new DataSetLoadException(
          String.format(
              "No data files with extension '%s' found in directory: %s",
              extension, directory.toAbsolutePath()));
    }
  }

  /**
   * Checks if a path has the specified file extension (case-insensitive).
   *
   * @param path the path to check
   * @param extension the extension to match
   * @return true if the path ends with the extension
   */
  private boolean hasExtension(final Path path, final String extension) {
    return path.toString().toLowerCase(Locale.ROOT).endsWith(extension);
  }
}
