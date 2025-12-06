package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.List;

/**
 * Base implementation of {@link Table} that supports removing scenario columns from the payload.
 *
 * <p>This abstract class provides a foundation for table implementations that filter scenario
 * columns before returning data. Subclasses must implement methods to provide table metadata and
 * filtered row data.
 */
public abstract class ScenarioTable implements Table {

  /**
   * Initializes a new scenario table instance.
   *
   * <p>Subclasses must call this constructor via {@code super()}. The subclass is responsible for
   * filtering scenario columns from the original table data and providing the filtered rows via
   * {@link #getRows()}.
   */
  protected ScenarioTable() {}

  /**
   * Gets the name of this table.
   *
   * @return the table name
   */
  @Override
  public abstract TableName getName();

  /**
   * Gets the column names of this table, excluding scenario columns.
   *
   * @return immutable list of column names
   */
  @Override
  public abstract List<ColumnName> getColumns();

  /**
   * Gets the rows of this table with scenario columns filtered out.
   *
   * @return immutable list of rows
   */
  @Override
  public abstract List<Row> getRows();

  /**
   * Gets the number of rows in this table.
   *
   * @return the row count
   */
  @Override
  public int getRowCount() {
    return getRows().size();
  }
}
