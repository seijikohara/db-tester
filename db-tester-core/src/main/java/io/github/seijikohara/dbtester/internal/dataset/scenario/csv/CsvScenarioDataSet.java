package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.dataset.TableOrderingResolver;
import io.github.seijikohara.dbtester.internal.dbunit.DatabaseBridge;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Collection-based CSV dataset with scenario filtering.
 *
 * <p>This class provides a framework-independent implementation of CSV dataset loading with
 * scenario-based row filtering and table ordering. Unlike the DbUnit-based implementation in the
 * facade package, this class uses Java Collections throughout.
 *
 * <p>Key features include scenario filtering (rows matching specified scenario names), table
 * ordering (via a table ordering file, auto-generated if not present), and collection-based table
 * management using {@link List} instead of arrays.
 *
 * <p>CSV files should follow these conventions: each file represents one table (file name = table
 * name), first row contains column headers, optional scenario marker column for filtering, and
 * empty cells represent NULL values.
 *
 * <p>If a table ordering file exists in the directory, tables are loaded in that order. Otherwise,
 * a default ordering file is created with tables sorted alphabetically.
 *
 * @see ScenarioDataSet
 * @see CsvScenarioTable
 */
public final class CsvScenarioDataSet extends ScenarioDataSet {

  /** Helper for resolving table ordering file. */
  private static final CsvTableOrderingHelper ORDERING_HELPER = new CsvTableOrderingHelper();

  /** Tables in this dataset (immutable list). */
  private final List<Table> tables;

  /**
   * Creates a CSV scenario dataset from the specified directory without scenario filtering.
   *
   * <p>This constructor loads all rows from CSV files without any scenario-based filtering.
   *
   * @param directory the directory path containing CSV files
   * @param scenarioMarker the scenario marker identifying the special column for scenario filtering
   * @param dataSource the data source to associate with this dataset, or {@code null}
   * @throws DataSetLoadException if dataset creation fails
   */
  public CsvScenarioDataSet(
      final Path directory,
      final ScenarioMarker scenarioMarker,
      final @Nullable DataSource dataSource) {
    this(directory, List.of(), scenarioMarker, dataSource);
  }

  /**
   * Creates a CSV scenario dataset from the specified directory with scenario filtering.
   *
   * <p>Only rows matching the specified scenario names will be included in the dataset.
   *
   * @param directory the directory path containing CSV files
   * @param scenarioNames scenario names to filter; if empty, all rows are included
   * @param scenarioMarker the scenario marker identifying the special column for scenario filtering
   * @param dataSource the data source to associate with this dataset, or {@code null}
   * @throws DataSetLoadException if dataset creation fails
   */
  public CsvScenarioDataSet(
      final Path directory,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker,
      final @Nullable DataSource dataSource) {
    super(dataSource);
    final var orderedDirectory = ORDERING_HELPER.ensureTableOrdering(directory);
    final var bridge = DatabaseBridge.getInstance();
    final var dataSet = bridge.loadCsvDataSet(orderedDirectory);
    this.tables = filterByScenario(dataSet.getTables(), scenarioNames, scenarioMarker);
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of scenario-filtered tables
   */
  @Override
  public List<Table> getTables() {
    return tables;
  }

  /**
   * Filters tables by scenario, applying scenario-based row filtering to each table.
   *
   * @param tables the source tables from the CSV dataset
   * @param scenarioNames the scenario names for filtering
   * @param scenarioMarker the scenario marker identifying the special column for scenario filtering
   * @return immutable list of scenario-filtered tables
   */
  private List<Table> filterByScenario(
      final Collection<Table> tables,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker) {
    return tables.stream()
        .map(table -> (Table) new CsvScenarioTable(table, scenarioNames, scenarioMarker))
        .toList();
  }

  /**
   * Helper class for CSV-specific table ordering resolution.
   *
   * <p>This nested class extends the framework's {@link TableOrderingResolver} to provide CSV file
   * extension information for table ordering operations.
   */
  private static final class CsvTableOrderingHelper extends TableOrderingResolver {

    /** Creates a CSV-specific table ordering helper. */
    CsvTableOrderingHelper() {}

    /**
     * {@inheritDoc}
     *
     * @return the file extension "csv"
     */
    @Override
    protected String getSupportedFileExtension() {
      return "csv";
    }
  }
}
