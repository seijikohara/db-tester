package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.util.Map;

/**
 * Simple immutable implementation of {@link Row}.
 *
 * <p>This implementation stores column values in an immutable map.
 */
public final class SimpleRow implements Row {

  /** The column values. */
  private final Map<ColumnName, CellValue> values;

  /**
   * Creates a new row with the given values.
   *
   * @param values the column values
   */
  public SimpleRow(final Map<ColumnName, CellValue> values) {
    this.values = Map.copyOf(values);
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable mapping of columns to their values
   */
  @Override
  public Map<ColumnName, CellValue> getValues() {
    return values;
  }

  /**
   * {@inheritDoc}
   *
   * @param column the identifier of the column to look up
   * @return the data value for the requested column, or {@link CellValue#NULL} when absent
   */
  @Override
  public CellValue getValue(final ColumnName column) {
    return values.getOrDefault(column, CellValue.NULL);
  }
}
