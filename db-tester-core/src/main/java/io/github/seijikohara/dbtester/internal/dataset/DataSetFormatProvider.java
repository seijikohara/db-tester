package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.nio.file.Path;
import java.util.Collection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * SPI for constructing {@link ScenarioDataSet} instances from a particular file format.
 *
 * <p>Implementations of this interface define how to load dataset files in specific formats (e.g.,
 * CSV, Excel, JSON) and convert them into {@link ScenarioDataSet} instances that can be used by the
 * database testing framework.
 *
 * <p>Implementations must be thread-safe, support scenario-based filtering via {@link
 * ScenarioMarker}, and perform case-insensitive file extension matching.
 *
 * @see ScenarioDataSet
 * @see FileExtension
 * @see ScenarioMarker
 * @see ScenarioName
 */
public interface DataSetFormatProvider {

  /**
   * Returns the file extension supported by this provider.
   *
   * <p>The extension can be specified with or without a leading dot (e.g., "csv" or ".csv", "tsv"
   * or ".tsv"). Extensions are automatically normalized to include the leading dot internally.
   *
   * <p>For consistency and simplicity, it is recommended to return extensions without the leading
   * dot.
   *
   * @return the file extension (e.g., new FileExtension("csv"), new FileExtension("tsv"))
   */
  FileExtension supportedFileExtension();

  /**
   * Creates a scenario-aware dataset from the files stored in the specified directory.
   *
   * @param directory the directory that contains one file per logical table
   * @param scenarioNames the scenario filters to apply (may be empty for default behavior)
   * @param scenarioMarker the logical name of the marker column used for filtering
   * @param dataSource the data source to associate with this dataset, or {@code null}
   * @return the dataset ready to be executed by the extension
   */
  ScenarioDataSet createDataSet(
      Path directory,
      Collection<ScenarioName> scenarioNames,
      ScenarioMarker scenarioMarker,
      @Nullable DataSource dataSource);
}
