package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import java.util.Map;

/**
 * Represents a single logical row within a {@link Table}.
 *
 * <p>A row is an immutable collection of column-value pairs that correspond to a single database
 * record. Each row belongs to a specific table and contains values for one or more columns.
 *
 * <p>All row instances are immutable. The {@link #getValues()} method returns an immutable map,
 * ensuring that the row data cannot be modified after creation.
 *
 * @see Table
 * @see ColumnName
 * @see DataValue
 */
public interface Row {

  /**
   * Returns the column/value pairs that compose this row.
   *
   * @return immutable mapping of columns to their values
   */
  Map<ColumnName, DataValue> getValues();

  /**
   * Resolves the value associated with the specified column.
   *
   * <p>If the column is absent, the method returns a {@link DataValue} encapsulating {@code null}.
   *
   * @param column the identifier of the column to look up
   * @return the data value for the requested column, wrapping {@code null} when absent
   */
  DataValue getValue(ColumnName column);
}
