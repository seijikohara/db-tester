package io.github.seijikohara.dbtester.internal.scenario;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters rows based on scenario names.
 *
 * <p>This class provides scenario-based filtering functionality that can be applied to any dataset,
 * regardless of the underlying file format. The scenario marker column identifies which column
 * contains scenario names for filtering.
 *
 * <p>If no scenario names are specified, all rows are included. If scenario names are specified,
 * only rows matching one of the names are included. Scenario values are trimmed before comparison,
 * and rows without a scenario column are always included.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @see FilteredDataSet
 * @see FilteredTable
 */
public final class ScenarioFilter {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(ScenarioFilter.class);

  /** The scenario marker identifying the scenario column. */
  private final ScenarioMarker scenarioMarker;

  /** The set of scenario names to filter by. */
  private final Set<ScenarioName> scenarioNames;

  /**
   * Creates a scenario filter with the specified marker and names.
   *
   * @param scenarioMarker the scenario marker identifying the scenario column
   * @param scenarioNames the scenario names to filter by (empty for no filtering)
   */
  public ScenarioFilter(
      final ScenarioMarker scenarioMarker, final Collection<ScenarioName> scenarioNames) {
    this.scenarioMarker = scenarioMarker;
    this.scenarioNames = Set.copyOf(scenarioNames);
    logger.debug(
        "Created scenario filter with marker: {}, names: {}",
        scenarioMarker.value(),
        this.scenarioNames);
  }

  /**
   * Returns the scenario marker used by this filter.
   *
   * @return the scenario marker
   */
  public ScenarioMarker getScenarioMarker() {
    return scenarioMarker;
  }

  /**
   * Returns the scenario names used for filtering.
   *
   * @return immutable set of scenario names
   */
  public Set<ScenarioName> getScenarioNames() {
    return scenarioNames;
  }

  /**
   * Checks if filtering is active.
   *
   * <p>Filtering is active when at least one scenario name is specified.
   *
   * @return true if filtering is active, false otherwise
   */
  public boolean isActive() {
    return !scenarioNames.isEmpty();
  }

  /**
   * Finds the scenario column in the list of columns.
   *
   * <p>The scenario column is identified by matching the first column's name against the scenario
   * marker.
   *
   * @param columns the list of columns to search
   * @return an Optional containing the scenario column if found, or empty otherwise
   */
  public Optional<ColumnName> findScenarioColumn(final List<ColumnName> columns) {
    return columns.stream()
        .findFirst()
        .filter(column -> scenarioMarker.value().equals(column.value()));
  }

  /**
   * Derives the data columns by excluding the scenario column if present.
   *
   * @param columns the list of all columns
   * @param scenarioColumn the scenario column to exclude, or null if not present
   * @return list of data columns (excluding the scenario column)
   */
  public List<ColumnName> deriveDataColumns(
      final List<ColumnName> columns, final @Nullable ColumnName scenarioColumn) {
    return Optional.ofNullable(scenarioColumn)
        .map(col -> columns.stream().skip(1).toList())
        .orElse(columns);
  }

  /**
   * Filters rows based on scenario names.
   *
   * @param rows the rows to filter
   * @param scenarioColumn the scenario column, or null if not present
   * @return filtered list of rows
   */
  public List<Row> filterRows(final List<Row> rows, final @Nullable ColumnName scenarioColumn) {
    return Optional.ofNullable(scenarioColumn)
        .filter(col -> isActive())
        .map(col -> rows.stream().filter(row -> shouldIncludeRow(row, col)).toList())
        .orElse(rows);
  }

  /**
   * Determines whether a row should be included based on its scenario value.
   *
   * @param row the row to check
   * @param scenarioColumn the scenario column
   * @return true if the row should be included, false otherwise
   */
  private boolean shouldIncludeRow(final Row row, final ColumnName scenarioColumn) {
    return readScenarioName(row, scenarioColumn).map(scenarioNames::contains).orElse(true);
  }

  /**
   * Reads a scenario name from a row.
   *
   * @param row the row
   * @param columnName the column name
   * @return optional containing the ScenarioName, or empty if null or blank
   */
  private Optional<ScenarioName> readScenarioName(final Row row, final ColumnName columnName) {
    final var dataValue = row.getValue(columnName);
    return Optional.ofNullable(dataValue.value())
        .map(Object::toString)
        .map(String::trim)
        .filter(Predicate.not(String::isBlank))
        .map(ScenarioName::new);
  }
}
