package io.github.seijikohara.dbtester.internal.dbunit.adapter;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

/** Exposes an {@link ITable} as the framework {@link Table} abstraction. */
public final class DbUnitTableAdapter implements Table {

  /** The DbUnit table being adapted. */
  private final ITable delegate;

  /**
   * Creates a new adapter.
   *
   * @param delegate the DbUnit table to adapt
   */
  public DbUnitTableAdapter(final ITable delegate) {
    this.delegate = delegate;
  }

  @Override
  public TableName getName() {
    return new TableName(delegate.getTableMetaData().getTableName());
  }

  @Override
  public List<ColumnName> getColumns() {
    try {
      final var columns = delegate.getTableMetaData().getColumns();
      return Stream.of(columns).map(column -> new ColumnName(column.getColumnName())).toList();
    } catch (final DataSetException e) {
      throw new DataSetLoadException("Failed to get columns", e);
    }
  }

  @Override
  public List<Row> getRows() {
    final var rowCount = getRowCount();
    return IntStream.range(0, rowCount).mapToObj(this::getRowAt).toList();
  }

  @Override
  public int getRowCount() {
    return delegate.getRowCount();
  }

  /**
   * Gets the row at the specified index.
   *
   * @param rowIndex the row index
   * @return the row
   */
  private Row getRowAt(final int rowIndex) {
    return new DbUnitRowAdapter(delegate, rowIndex);
  }
}
