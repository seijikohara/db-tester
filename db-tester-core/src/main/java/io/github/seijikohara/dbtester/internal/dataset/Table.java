package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
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
   * Returns the logical name of this table.
   *
   * @return table identifier, never null
   */
  TableName getName();

  /**
   * Returns the column names in the order expected by the dataset.
   *
   * <p>The column order defines the structure of each row in this table. All rows must provide
   * values corresponding to these columns in the same order.
   *
   * @return immutable list of column identifiers, never null or empty
   */
  List<ColumnName> getColumns();

  /**
   * Returns all rows contained in this table.
   *
   * <p>Each row contains values corresponding to the columns returned by {@link #getColumns()}.
   *
   * @return immutable list of rows, never null (may be empty)
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
}
