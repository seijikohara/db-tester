package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import java.util.Map;

/**
 * CSV row implementation.
 *
 * <p>This class represents a single row in a CSV table as a map of column names to values.
 *
 * <h2>Immutability</h2>
 *
 * <p>This class is immutable. The values map is copied defensively in the constructor.
 *
 * <h2>Null Handling</h2>
 *
 * <p>Empty CSV cells are represented as {@link DataValue} wrapping null, following database NULL
 * semantics.
 *
 * @see Row
 * @see CsvScenarioTable
 */
final class CsvRow implements Row {

  /** Column values for this row. */
  private final Map<ColumnName, DataValue> values;

  /**
   * Creates a CSV row with the specified column values.
   *
   * @param values the map of column names to values (copied defensively)
   */
  CsvRow(final Map<ColumnName, DataValue> values) {
    this.values = Map.copyOf(values);
  }

  @Override
  public Map<ColumnName, DataValue> getValues() {
    return values;
  }

  /**
   * Returns the value for the specified column.
   *
   * <p>If the column does not exist in this row, returns a {@link DataValue} wrapping null.
   *
   * @param column the column name
   * @return the value (never null, but may wrap a null value)
   */
  @Override
  public DataValue getValue(final ColumnName column) {
    return values.getOrDefault(column, DataValue.NULL);
  }
}
