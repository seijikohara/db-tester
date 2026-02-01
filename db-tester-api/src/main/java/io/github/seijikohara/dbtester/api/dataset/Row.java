package io.github.seijikohara.dbtester.api.dataset;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

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
 * @see CellValue
 */
public interface Row {

  /**
   * Creates a new row with the given column-value pairs.
   *
   * @param values the column-value pairs for this row
   * @return a new immutable row instance
   */
  static Row of(final Map<ColumnName, CellValue> values) {
    return new SimpleRow(Map.copyOf(values));
  }

  /**
   * Creates a new row by pairing column names with raw values.
   *
   * <p>Each column name is paired with the value at the same index. Null values in the values list
   * are mapped to {@link CellValue#NULL}.
   *
   * @param columns the column names as strings
   * @param values the raw values corresponding to each column
   * @return a new immutable row instance
   * @throws IllegalArgumentException if columns and values have different sizes
   */
  static Row of(final List<String> columns, final List<? extends @Nullable Object> values) {
    if (columns.size() != values.size()) {
      throw new IllegalArgumentException(
          String.format(
              "Column count (%d) does not match value count (%d)", columns.size(), values.size()));
    }
    final var map =
        IntStream.range(0, columns.size())
            .boxed()
            .collect(
                Collectors.toUnmodifiableMap(
                    i -> new ColumnName(columns.get(i)),
                    i -> {
                      final var val = values.get(i);
                      return val == null ? CellValue.NULL : new CellValue(val);
                    }));
    return new SimpleRow(map);
  }

  /**
   * Returns the column/value pairs that compose this row.
   *
   * @return immutable mapping of columns to their values
   */
  Map<ColumnName, CellValue> getValues();

  /**
   * Resolves the value associated with the specified column.
   *
   * <p>If the column is absent, the method returns a {@link CellValue} encapsulating {@code null}.
   *
   * @param column the identifier of the column to look up
   * @return the data value for the requested column, wrapping {@code null} when absent
   */
  CellValue getValue(ColumnName column);

  /**
   * Simple immutable implementation of {@link Row}.
   *
   * @param values the column-value pairs for this row
   */
  record SimpleRow(Map<ColumnName, CellValue> values) implements Row {

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
}
