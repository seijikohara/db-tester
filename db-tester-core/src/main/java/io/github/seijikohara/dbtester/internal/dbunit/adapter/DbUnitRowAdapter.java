package io.github.seijikohara.dbtester.internal.dbunit.adapter;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

/** Adapts a row from an {@link ITable} to the framework {@link Row} contract. */
public final class DbUnitRowAdapter implements Row {

  /** The DbUnit table containing the row. */
  private final ITable table;

  /** The index of the row within the table. */
  private final int rowIndex;

  /**
   * Creates a new adapter.
   *
   * @param table the DbUnit table
   * @param rowIndex the row index
   */
  public DbUnitRowAdapter(final ITable table, final int rowIndex) {
    this.table = table;
    this.rowIndex = rowIndex;
  }

  @Override
  public Map<ColumnName, DataValue> getValues() {
    final var columns = getColumns();
    return Arrays.stream(columns)
        .collect(
            Collectors.toUnmodifiableMap(
                column -> new ColumnName(column.getColumnName()),
                column -> {
                  try {
                    return new DataValue(table.getValue(rowIndex, column.getColumnName()));
                  } catch (final DataSetException e) {
                    throw new DataSetLoadException("Failed to get column value", e);
                  }
                }));
  }

  /**
   * Retrieves the columns from the table metadata.
   *
   * @return array of columns
   * @throws DataSetLoadException if metadata retrieval fails
   */
  private Column[] getColumns() {
    try {
      return table.getTableMetaData().getColumns();
    } catch (final DataSetException e) {
      throw new DataSetLoadException("Failed to get table metadata", e);
    }
  }

  @Override
  public DataValue getValue(final ColumnName column) {
    try {
      final var value = table.getValue(rowIndex, column.value());
      return new DataValue(value);
    } catch (final DataSetException e) {
      // Column not found, return DataValue wrapping null
      return new DataValue(null);
    }
  }
}
