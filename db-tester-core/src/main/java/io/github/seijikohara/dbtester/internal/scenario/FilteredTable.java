package io.github.seijikohara.dbtester.internal.scenario;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that applies scenario filtering to a table.
 *
 * <p>This class wraps a source table and applies scenario-based row filtering. The scenario column
 * is removed from the resulting table structure, and only rows matching the specified scenario
 * names are included.
 *
 * <p>If no scenario column exists, all rows are included unchanged. If no scenario names are
 * specified, all rows are included. Otherwise, only rows matching the scenario names are included.
 * The scenario column is removed from the resulting columns and rows.
 *
 * <p>Empty strings in data values are converted to {@code null} for database compatibility.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @see ScenarioFilter
 * @see FilteredTableSet
 */
public final class FilteredTable implements Table {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(FilteredTable.class);

  /** The table name. */
  private final TableName tableName;

  /** The column names (excluding scenario marker). */
  private final List<ColumnName> columns;

  /** The row data (scenario-filtered). */
  private final List<Row> rows;

  /**
   * Creates a filtered table from a source table.
   *
   * @param sourceTable the source table to filter
   * @param filter the scenario filter to apply
   */
  public FilteredTable(final Table sourceTable, final ScenarioFilter filter) {
    this.tableName = sourceTable.getName();

    final var allColumns = sourceTable.getColumns();
    final var scenarioColumn = filter.findScenarioColumn(allColumns).orElse(null);
    this.columns = filter.deriveDataColumns(allColumns, scenarioColumn);

    final var filteredRows = filter.filterRows(sourceTable.getRows(), scenarioColumn);
    this.rows = extractDataColumnsOnly(filteredRows, this.columns, scenarioColumn);

    logger.debug(
        "Filtered table {} from {} to {} rows",
        tableName.value(),
        sourceTable.getRowCount(),
        rows.size());
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
   * {@inheritDoc}
   *
   * @return the number of rows in this table
   */
  @Override
  public int getRowCount() {
    return rows.size();
  }

  /**
   * Extracts only the data columns from rows, excluding the scenario column.
   *
   * @param sourceRows the source rows
   * @param dataColumns the data columns to include
   * @param scenarioColumn the scenario column to exclude, or null if not present
   * @return list of rows with only data columns
   */
  private List<Row> extractDataColumnsOnly(
      final Collection<Row> sourceRows,
      final Collection<ColumnName> dataColumns,
      final @Nullable ColumnName scenarioColumn) {
    return Optional.ofNullable(scenarioColumn)
        .map(column -> sourceRows.stream().map(row -> extractRow(row, dataColumns)).toList())
        .orElseGet(() -> sourceRows.stream().map(row -> normalizeRow(row, dataColumns)).toList());
  }

  /**
   * Normalizes a row by converting empty strings to null.
   *
   * @param sourceRow the source row
   * @param dataColumns the data columns
   * @return normalized row
   */
  private Row normalizeRow(final Row sourceRow, final Collection<ColumnName> dataColumns) {
    final var values =
        dataColumns.stream()
            .collect(
                Collectors.toMap(
                    column -> column,
                    column -> normalizeEmptyStringToNull(sourceRow.getValue(column)),
                    (v1, v2) -> v1,
                    LinkedHashMap::new));
    return new SimpleRow(values);
  }

  /**
   * Extracts data columns from a row.
   *
   * @param sourceRow the source row
   * @param dataColumns the data columns to extract
   * @return row containing only data columns
   */
  private Row extractRow(final Row sourceRow, final Collection<ColumnName> dataColumns) {
    final var values =
        dataColumns.stream()
            .collect(
                Collectors.toMap(
                    column -> column,
                    column -> normalizeEmptyStringToNull(sourceRow.getValue(column)),
                    (v1, v2) -> v1,
                    LinkedHashMap::new));
    return new SimpleRow(values);
  }

  /**
   * Normalizes empty strings to null for database compatibility.
   *
   * <p>Empty CSV/TSV cells are read as empty strings. This method converts them to null to match
   * database NULL semantics.
   *
   * @param dataValue the data value to normalize
   * @return CellValue containing the normalized value (null if the value was an empty string)
   */
  private CellValue normalizeEmptyStringToNull(final CellValue dataValue) {
    return Optional.ofNullable(dataValue.value())
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(String::isEmpty)
        .map(emptyString -> new CellValue(null))
        .orElse(dataValue);
  }
}
