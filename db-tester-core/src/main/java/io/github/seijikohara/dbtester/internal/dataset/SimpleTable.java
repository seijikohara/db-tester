package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;

/**
 * Simple immutable implementation of {@link Table}.
 *
 * <p>This implementation stores table metadata and rows in immutable collections.
 */
public final class SimpleTable implements Table {

  /** The table name. */
  private final TableName name;

  /** The column names. */
  private final List<ColumnName> columns;

  /** The rows in this table. */
  private final List<Row> rows;

  /**
   * Creates a new table with the given name, columns, and rows.
   *
   * @param name the table name
   * @param columns the column names
   * @param rows the rows
   */
  public SimpleTable(final TableName name, final List<ColumnName> columns, final List<Row> rows) {
    this.name = name;
    this.columns = List.copyOf(columns);
    this.rows = List.copyOf(rows);
  }

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
