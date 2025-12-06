package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioTable;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Collection-based CSV table implementation with scenario filtering.
 *
 * <p>This class provides a framework-independent implementation of CSV table loading with
 * scenario-based row filtering. Unlike the DbUnit-based implementation, this class uses Java
 * Collections throughout.
 *
 * <p>When the first column matches the configured scenario marker, it is used for row filtering.
 * Only rows with scenario values matching the specified scenario names are included, the scenario
 * marker column is excluded from the final table structure, scenario values are trimmed before
 * comparison, and if no scenario names are specified all rows are included.
 *
 * <p>Empty CSV cells are converted to {@code null} for database compatibility. This matches
 * database NULL semantics and allows proper database comparisons.
 *
 * <p>This implementation uses {@link List} for rows and columns, {@link java.util.Map} for row
 * values, immutable collections by default, and has no DbUnit dependencies.
 *
 * @see ScenarioTable
 * @see CsvScenarioDataSet
 */
public final class CsvScenarioTable extends ScenarioTable {

  /** Table name. */
  private final TableName tableName;

  /** Column names (excluding scenario marker). */
  private final List<ColumnName> columns;

  /** Row data (scenario-filtered). */
  private final List<Row> rows;

  /** Scenario marker identifying the special column for scenario filtering. */
  private final ScenarioMarker scenarioMarker;

  /**
   * Creates a scenario-filtered table from a framework Table.
   *
   * <p>This constructor processes the source table by:
   *
   * <ol>
   *   <li>Detecting if a scenario marker column exists
   *   <li>Filtering rows based on the specified scenario names
   *   <li>Removing the scenario marker column from the final table structure
   *   <li>Using framework Collection-based representation throughout
   * </ol>
   *
   * @param sourceTable the source table from the framework (not DbUnit)
   * @param scenarioNames the scenario names to filter rows; if empty, all rows are included
   * @param scenarioMarker the scenario marker identifying the special column for scenario filtering
   */
  public CsvScenarioTable(
      final Table sourceTable,
      final Collection<ScenarioName> scenarioNames,
      final ScenarioMarker scenarioMarker) {
    this.scenarioMarker = scenarioMarker;
    this.tableName = sourceTable.getName();

    final var allColumns = sourceTable.getColumns();
    final var scenarioColumn = findScenarioColumn(allColumns);
    final var dataColumns = deriveDataColumns(allColumns, scenarioColumn.orElse(null));
    this.columns = dataColumns;

    final var scenarioNameSet = Set.copyOf(scenarioNames);
    this.rows =
        filterRows(
            sourceTable.getRows(), scenarioColumn.orElse(null), scenarioNameSet, dataColumns);
  }

  /**
   * {@inheritDoc}
   *
   * @return the table name
   */
  @Override
  public TableName getName() {
    return tableName;
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of column names (excluding scenario marker)
   */
  @Override
  public List<ColumnName> getColumns() {
    return columns;
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of scenario-filtered rows
   */
  @Override
  public List<Row> getRows() {
    return rows;
  }

  /**
   * Finds the scenario column in the table columns.
   *
   * <p>Checks if the first column has the configured scenario marker name, which indicates it
   * should be used for row filtering.
   *
   * @param columns the collection of columns from the source table
   * @return an {@code Optional} containing the scenario column if present, or empty otherwise
   */
  private Optional<ColumnName> findScenarioColumn(final Collection<ColumnName> columns) {
    return columns.stream()
        .findFirst()
        .filter(column -> scenarioMarker.value().equals(column.value()));
  }

  /**
   * Derives the data columns by excluding the scenario column if present.
   *
   * <p>If a scenario column exists, it is excluded from the resulting list since the column is only
   * used for filtering and should not appear in the final table structure.
   *
   * @param columns the collection of all columns from the source table
   * @param scenarioColumn the scenario column to exclude, or {@code null} if not present
   * @return immutable list of data columns (excluding the scenario column)
   */
  private List<ColumnName> deriveDataColumns(
      final Collection<ColumnName> columns, final @Nullable ColumnName scenarioColumn) {
    return Optional.ofNullable(scenarioColumn)
        .map(column -> columns.stream().skip(1))
        .orElseGet(columns::stream)
        .toList();
  }

  /**
   * Filters rows based on scenario names.
   *
   * <p>If a scenario column exists and scenario names are specified, only rows matching the
   * scenario names are included. Otherwise, all rows are included. The scenario column is removed
   * from the filtered rows.
   *
   * @param sourceRows the source rows from the table
   * @param scenarioColumn the name of the scenario column, or {@code null} if not present
   * @param scenarioNames the set of scenario names to filter by
   * @param dataColumns the data columns (excluding scenario column)
   * @return immutable list of filtered rows
   */
  private List<Row> filterRows(
      final Collection<Row> sourceRows,
      final @Nullable ColumnName scenarioColumn,
      final Set<ScenarioName> scenarioNames,
      final Collection<ColumnName> dataColumns) {
    return sourceRows.stream()
        .filter(row -> shouldIncludeRow(row, scenarioColumn, scenarioNames))
        .map(row -> extractDataColumnsOnly(row, dataColumns))
        .toList();
  }

  /**
   * Determines whether a row should be included based on its scenario value.
   *
   * @param row the source row
   * @param scenarioColumn the name of the scenario column, or {@code null} if not present
   * @param scenarioNames the set of scenario names to match against
   * @return {@code true} if the row should be included, {@code false} otherwise
   */
  private boolean shouldIncludeRow(
      final Row row,
      final @Nullable ColumnName scenarioColumn,
      final Set<ScenarioName> scenarioNames) {
    return Optional.of(scenarioNames)
        .filter(names -> !names.isEmpty())
        .flatMap(
            names ->
                Optional.ofNullable(scenarioColumn)
                    .flatMap(column -> readScenarioName(row, column))
                    .map(names::contains))
        .orElse(true);
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

  /**
   * Extracts only the data columns from a row, excluding the scenario column.
   *
   * @param sourceRow the source row
   * @param dataColumns the data columns to include
   * @return Row instance containing only the data column values
   */
  private Row extractDataColumnsOnly(
      final Row sourceRow, final Collection<ColumnName> dataColumns) {
    final var values =
        dataColumns.stream()
            .collect(
                Collectors.toMap(
                    column -> column,
                    column -> normalizeEmptyStringToNull(sourceRow.getValue(column)),
                    (v1, v2) -> v1,
                    LinkedHashMap::new));
    return new CsvRow(values);
  }

  /**
   * Normalizes empty strings to null for database compatibility.
   *
   * <p>DbUnit's CsvDataSet reads empty CSV cells as empty strings (""). This method converts them
   * to null to match database NULL semantics, meaning empty CSV cells represent NULL values in the
   * database.
   *
   * @param dataValue the data value to normalize
   * @return DataValue containing the normalized value (null if the value was an empty string)
   */
  private DataValue normalizeEmptyStringToNull(final DataValue dataValue) {
    return Optional.ofNullable(dataValue.value())
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(String::isEmpty)
        .map(emptyString -> new DataValue(null))
        .orElse(dataValue);
  }
}
