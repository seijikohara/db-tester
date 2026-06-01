package io.github.seijikohara.dbtester.internal.scenario;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
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
 * <p>Cell values are preserved as parsed. An empty CSV or TSV cell already parses to NULL, while a
 * JSON or YAML empty string parses to a non-null empty string. This class does not collapse empty
 * strings to NULL, so the NULL versus empty-string distinction reaches the database unchanged.
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
    this.tableName = sourceTable.name();

    final var allColumns = sourceTable.columns();
    final var scenarioColumn = filter.findScenarioColumn(allColumns).orElse(null);
    this.columns = filter.deriveDataColumns(allColumns, scenarioColumn);

    final var filteredRows = filter.filterRows(sourceTable.rows(), scenarioColumn);
    this.rows = extractDataColumnsOnly(filteredRows, this.columns);

    logger.debug(
        "Filtered table {} from {} to {} rows",
        tableName.value(),
        sourceTable.rowCount(),
        rows.size());
  }

  /**
   * {@inheritDoc}
   *
   * @return the table name
   */
  @Override
  public TableName name() {
    return tableName;
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of column names (excluding scenario marker)
   */
  @Override
  public List<ColumnName> columns() {
    return columns;
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of scenario-filtered rows
   */
  @Override
  public List<Row> rows() {
    return rows;
  }

  /**
   * {@inheritDoc}
   *
   * @return the number of rows in this table
   */
  @Override
  public int rowCount() {
    return rows.size();
  }

  /**
   * Extracts only the data columns from rows, excluding the scenario column.
   *
   * @param sourceRows the source rows
   * @param dataColumns the data columns to include
   * @return list of rows with only data columns
   */
  private List<Row> extractDataColumnsOnly(
      final Collection<Row> sourceRows, final Collection<ColumnName> dataColumns) {
    return sourceRows.stream().map(row -> projectDataColumns(row, dataColumns)).toList();
  }

  /**
   * Projects a row onto the data columns, preserving each cell value as parsed.
   *
   * <p>The cell values keep the representation chosen by the format parser. An empty CSV or TSV
   * cell already parses to NULL, while a JSON or YAML empty string parses to a non-null empty
   * string. This method does not collapse empty strings to NULL, so the NULL versus empty-string
   * distinction expressed in JSON and YAML reaches the database unchanged.
   *
   * @param sourceRow the source row
   * @param dataColumns the data columns to include
   * @return a row containing only the data columns
   */
  private Row projectDataColumns(final Row sourceRow, final Collection<ColumnName> dataColumns) {
    final var values =
        dataColumns.stream()
            .collect(
                Collectors.toMap(
                    column -> column, sourceRow::value, (v1, v2) -> v1, LinkedHashMap::new));
    return new SimpleRow(values);
  }
}
