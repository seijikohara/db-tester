package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Factory for creating scenario-aware datasets.
 *
 * <p>This factory uses format providers to parse dataset files and create scenario-filtered
 * datasets.
 */
public final class ScenarioDataSetFactory {

  /**
   * Private constructor to prevent instantiation.
   *
   * @throws UnsupportedOperationException always thrown to prevent instantiation
   */
  private ScenarioDataSetFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Creates a scenario dataset from the given directory.
   *
   * @param directory the directory containing dataset files
   * @param scenarioNames the scenario names for filtering
   * @param scenarioMarker the scenario marker column name
   * @param dataSource the data source (optional)
   * @return the created scenario dataset
   */
  public static ScenarioDataSet create(
      final Path directory,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker,
      final @Nullable DataSource dataSource) {
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException("Path is not a directory: " + directory);
    }

    // Detect the file format based on files in the directory
    final FileExtension extension = detectFileExtension(directory);
    final DataSetFormatProvider provider = DataSetFormatRegistry.getProvider(extension);

    return provider.createDataSet(directory, scenarioNames, scenarioMarker, dataSource);
  }

  /**
   * Creates a scenario dataset from the given directory with a single scenario name.
   *
   * @param directory the directory containing dataset files
   * @param scenarioName the scenario name for filtering
   * @param scenarioMarker the scenario marker column name
   * @param dataSource the data source (optional)
   * @return the created scenario dataset
   */
  public static ScenarioDataSet create(
      final Path directory,
      final ScenarioName scenarioName,
      final ScenarioMarker scenarioMarker,
      final @Nullable DataSource dataSource) {
    return create(directory, List.of(scenarioName), scenarioMarker, dataSource);
  }

  /**
   * Detects the file extension of files in the given directory.
   *
   * @param directory the directory to scan
   * @return the detected file extension
   * @throws IllegalArgumentException if no files are found or an I/O error occurs
   */
  private static FileExtension detectFileExtension(final Path directory) {
    try (final var stream = Files.list(directory)) {
      return stream
          .filter(Files::isRegularFile)
          .map(Path::getFileName)
          .map(Path::toString)
          .filter(name -> name.contains("."))
          .map(name -> name.substring(name.lastIndexOf('.') + 1))
          .findFirst()
          .map(FileExtension::new)
          .orElseThrow(
              () -> new IllegalArgumentException("No files found in directory: " + directory));
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to detect file extension in: " + directory, e);
    }
  }
}
