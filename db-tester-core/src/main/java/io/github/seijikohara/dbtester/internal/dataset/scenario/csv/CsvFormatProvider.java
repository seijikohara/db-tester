package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import io.github.seijikohara.dbtester.internal.dataset.DataSetFormatProvider;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.nio.file.Path;
import java.util.Collection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * CSV format provider for creating datasets from CSV files.
 *
 * <p>This provider handles CSV (Comma-Separated Values) format files. It creates {@link
 * CsvScenarioDataSet} instances that support scenario-based filtering through a special scenario
 * marker column.
 *
 * <p>CSV files should follow these conventions: file name matches the table name, first row
 * contains column headers, optional scenario marker column for scenario-based filtering, standard
 * CSV format with comma separators, and empty cells represent NULL values in the database.
 *
 * <p>This provider is discovered automatically via {@link java.util.ServiceLoader}. No manual
 * registration is required when the module descriptor or {@code META-INF/services} entry is on the
 * classpath.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see CsvScenarioDataSet
 * @see DataSetFormatProvider
 * @see io.github.seijikohara.dbtester.internal.dataset.DataSetFormatRegistry
 */
public final class CsvFormatProvider implements DataSetFormatProvider {

  /** Creates a new CSV format provider. */
  public CsvFormatProvider() {}

  /**
   * {@inheritDoc}
   *
   * @return the file extension "csv"
   */
  @Override
  public FileExtension supportedFileExtension() {
    return new FileExtension("csv");
  }

  /**
   * {@inheritDoc}
   *
   * @param directory the directory containing CSV files (must not be null)
   * @param scenarioNames the scenario filters to apply (must not be null, may be empty)
   * @param scenarioMarker the marker column name for scenario filtering (must not be null)
   * @param dataSource the data source to associate, or {@code null} for none
   * @return a new CsvScenarioDataSet instance
   */
  @Override
  public ScenarioDataSet createDataSet(
      final Path directory,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker,
      final @Nullable DataSource dataSource) {
    return new CsvScenarioDataSet(directory, scenarioNames, scenarioMarker, dataSource);
  }
}
