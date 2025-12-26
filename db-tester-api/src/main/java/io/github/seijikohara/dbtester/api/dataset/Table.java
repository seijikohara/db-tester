package io.github.seijikohara.dbtester.api.dataset;

import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;

/**
 * Represents the structure and data of a single database table.
 *
 * <p>This interface provides access to table metadata (name, columns) and data (rows). All
 * collection return types are immutable, ensuring thread safety and preventing unintended
 * modifications.
 *
 * <p>Implementations must guarantee that:
 *
 * <ul>
 *   <li>Column order is consistent across all rows
 *   <li>All returned collections are immutable
 *   <li>Row count matches the size of the rows collection
 * </ul>
 */
public interface Table {

  /**
   * Creates a new table with the given name, columns, and rows.
   *
   * @param name the table name
   * @param columns the column names in declaration order
   * @param rows the rows in this table
   * @return a new immutable table instance
   */
  static Table of(final TableName name, final List<ColumnName> columns, final List<Row> rows) {
    return new SimpleTable(name, List.copyOf(columns), List.copyOf(rows));
  }

  /**
   * Creates a new table with the given name (as string), columns (as strings), and rows.
   *
   * @param name the table name
   * @param columns the column names in declaration order
   * @param rows the rows in this table
   * @return a new immutable table instance
   */
  static Table of(final String name, final List<String> columns, final List<Row> rows) {
    return new SimpleTable(
        new TableName(name), columns.stream().map(ColumnName::new).toList(), List.copyOf(rows));
  }

  /**
   * Returns the logical name of this table.
   *
   * @return the table identifier
   */
  TableName getName();

  /**
   * Returns the column names in the order expected by the dataset.
   *
   * <p>The column order defines the structure of each row in this table. All rows must provide
   * values corresponding to these columns in the same order.
   *
   * @return immutable list of column identifiers (non-empty)
   */
  List<ColumnName> getColumns();

  /**
   * Returns all rows contained in this table.
   *
   * <p>Each row contains values corresponding to the columns returned by {@link #getColumns()}.
   *
   * @return immutable list of rows (may be empty)
   */
  List<Row> getRows();

  /**
   * Returns the number of rows in this table.
   *
   * <p>Equivalent to {@code getRows().size()}.
   *
   * @return number of rows contained in the table (zero or positive)
   */
  int getRowCount();

  /**
   * Simple immutable implementation of {@link Table}.
   *
   * @param name the table name
   * @param columns the column names
   * @param rows the rows in this table
   */
  record SimpleTable(TableName name, List<ColumnName> columns, List<Row> rows) implements Table {

    /**
     * {@inheritDoc}
     *
     * @return the table name
     */
    @Override
    public TableName getName() {
      return name;
    }

    /**
     * {@inheritDoc}
     *
     * @return immutable list of column names
     */
    @Override
    public List<ColumnName> getColumns() {
      return columns;
    }

    /**
     * {@inheritDoc}
     *
     * @return immutable list of rows
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
  }
}
