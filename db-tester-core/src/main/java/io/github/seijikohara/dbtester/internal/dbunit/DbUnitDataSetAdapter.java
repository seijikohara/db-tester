package io.github.seijikohara.dbtester.internal.dbunit;

import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.datatype.DataType;
import org.jspecify.annotations.Nullable;

/**
 * Adapter that wraps a db-tester {@link DataSet} as a DbUnit {@link IDataSet}.
 *
 * <p>This adapter allows db-tester datasets to be used with DbUnit operations.
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This class is thread-safe. The table cache uses {@link ConcurrentHashMap} for safe concurrent
 * access.
 */
final class DbUnitDataSetAdapter implements IDataSet {

  /** The wrapped db-tester dataset. */
  private final DataSet dataSet;

  /** Cache of adapted tables for efficient lookup. */
  private final Map<String, ITable> tableCache = new ConcurrentHashMap<>();

  /**
   * Creates a new adapter for the given dataset.
   *
   * @param dataSet the dataset to wrap
   */
  DbUnitDataSetAdapter(final DataSet dataSet) {
    this.dataSet = dataSet;
  }

  @Override
  public String[] getTableNames() {
    return dataSet.getTables().stream()
        .map(table -> table.getName().value())
        .toArray(String[]::new);
  }

  @Override
  public ITableMetaData getTableMetaData(final String tableName) throws DataSetException {
    final Table table = findTable(tableName);
    return createTableMetaData(table);
  }

  @Override
  public ITable getTable(final String tableName) throws DataSetException {
    return tableCache.computeIfAbsent(tableName, this::createTable);
  }

  @Override
  @Deprecated
  public ITable[] getTables() throws DataSetException {
    return dataSet.getTables().stream()
        .map(table -> createTable(table.getName().value()))
        .toArray(ITable[]::new);
  }

  @Override
  public ITableIterator iterator() {
    return new DbUnitTableIterator(dataSet.getTables());
  }

  @Override
  public ITableIterator reverseIterator() {
    final List<Table> tables = dataSet.getTables();
    return new DbUnitTableIterator(tables.reversed());
  }

  @Override
  public boolean isCaseSensitiveTableNames() {
    return false;
  }

  /**
   * Finds a table by name in the dataset.
   *
   * @param tableName the table name to find
   * @return the table
   * @throws DataSetException if the table is not found
   */
  private Table findTable(final String tableName) throws DataSetException {
    return dataSet.getTables().stream()
        .filter(t -> t.getName().value().equalsIgnoreCase(tableName))
        .findFirst()
        .orElseThrow(() -> new DataSetException("Table not found: " + tableName));
  }

  /**
   * Creates a DbUnit table adapter for the given table name.
   *
   * @param tableName the table name
   * @return the adapted table
   */
  private ITable createTable(final String tableName) {
    try {
      final Table table = findTable(tableName);
      return new DbUnitTableAdapter(table);
    } catch (final DataSetException e) {
      throw new IllegalArgumentException("Table not found: " + tableName, e);
    }
  }

  /**
   * Creates DbUnit table metadata from a db-tester table.
   *
   * @param table the table
   * @return the table metadata
   */
  private ITableMetaData createTableMetaData(final Table table) {
    final Column[] columns =
        table.getColumns().stream()
            .map(col -> new Column(col.value(), DataType.UNKNOWN))
            .toArray(Column[]::new);
    return new DefaultTableMetaData(table.getName().value(), columns);
  }

  /** Inner adapter for a single table. */
  private static final class DbUnitTableAdapter implements ITable {

    /** The wrapped db-tester table. */
    private final Table table;

    /** The table metadata. */
    private final ITableMetaData metaData;

    /**
     * Creates a new adapter for the given table.
     *
     * @param table the table to wrap
     */
    DbUnitTableAdapter(final Table table) {
      this.table = table;
      this.metaData = createMetaData();
    }

    @Override
    public ITableMetaData getTableMetaData() {
      return metaData;
    }

    @Override
    public int getRowCount() {
      return table.getRows().size();
    }

    @Override
    @SuppressWarnings("NullAway") // DbUnit ITable.getValue() can return null for null cell values
    public @Nullable Object getValue(final int row, final String column) throws DataSetException {
      if (row < 0 || row >= table.getRows().size()) {
        // Must throw RowOutOfBoundsException for DbUnit's AbstractBatchOperation to properly
        // detect end-of-data during iteration. DbUnit uses infinite loops that rely on this
        // specific exception type to terminate.
        throw new RowOutOfBoundsException("Row index out of bounds: " + row);
      }

      final var rowData = table.getRows().get(row);
      final var columnName = new ColumnName(column);
      final var value = rowData.getValue(columnName);
      return value != null ? value.value() : null;
    }

    /**
     * Creates table metadata from the wrapped table.
     *
     * @return the table metadata
     */
    private ITableMetaData createMetaData() {
      final Column[] columns =
          table.getColumns().stream()
              .map(col -> new Column(col.value(), DataType.UNKNOWN))
              .toArray(Column[]::new);
      return new DefaultTableMetaData(table.getName().value(), columns);
    }
  }

  /** Iterator over tables in the dataset. */
  private class DbUnitTableIterator implements ITableIterator {

    /** The list of tables to iterate. */
    private final List<Table> tables;

    /** The current index in the iteration. */
    private int currentIndex = -1;

    /**
     * Creates a new iterator over the given tables.
     *
     * @param tables the tables to iterate
     */
    DbUnitTableIterator(final List<Table> tables) {
      this.tables = tables;
    }

    @Override
    public boolean next() {
      currentIndex++;
      return currentIndex < tables.size();
    }

    @Override
    public ITableMetaData getTableMetaData() {
      return createTableMetaData(tables.get(currentIndex));
    }

    @Override
    public ITable getTable() {
      return new DbUnitTableAdapter(tables.get(currentIndex));
    }
  }
}
