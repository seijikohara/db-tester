package io.github.seijikohara.dbtester.internal.dbunit;

import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.SchemaName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.jspecify.annotations.Nullable;

/** Utility class that maps framework dataset abstractions to their DbUnit equivalents. */
final class TypeConverter {

  /** Private constructor to prevent instantiation. */
  private TypeConverter() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Converts framework DataSet to DbUnit IDataSet.
   *
   * <p>Tables with duplicate names are merged by combining their rows while preserving insertion
   * order (important for foreign key constraints).
   *
   * @param dataSet the framework dataset
   * @return DbUnit dataset with merged tables
   * @throws ValidationException if conversion fails
   */
  static IDataSet toDbUnitDataSet(final DataSet dataSet) {
    try {
      // Group tables by name and merge duplicates
      // Use LinkedHashMap to preserve insertion order
      final var mergedTables =
          dataSet.getTables().stream()
              .collect(
                  Collectors.groupingBy(Table::getName, LinkedHashMap::new, Collectors.toList()))
              .values()
              .stream()
              .map(TypeConverter::mergeTables)
              .map(TypeConverter::toDbUnitTableImpl)
              .toArray(ITable[]::new);

      return new DefaultDataSet(mergedTables);
    } catch (final DataSetException e) {
      throw new ValidationException("Failed to convert DataSet to DbUnit format", e);
    }
  }

  /**
   * Converts framework Table to DbUnit ITable.
   *
   * <p>Delegates to private implementation method. Used when operations require a single table
   * instead of a full dataset.
   *
   * @param table the framework table
   * @return DbUnit table
   */
  static ITable toDbUnitTable(final Table table) {
    return toDbUnitTableImpl(table);
  }

  /**
   * Creates a DbUnit database connection from an existing JDBC connection.
   *
   * @param jdbcConnection the JDBC connection to wrap
   * @param schemaName the database schema name, or {@code null} to use the connection default
   * @return DbUnit database connection
   * @throws ValidationException if the DbUnit wrapper cannot be created
   */
  static IDatabaseConnection toDbUnitConnection(
      final Connection jdbcConnection, final @Nullable SchemaName schemaName) {
    try {
      final var schemaNameString =
          Optional.ofNullable(schemaName).map(SchemaName::value).orElse(null);
      return new DatabaseConnection(jdbcConnection, schemaNameString);
    } catch (final DatabaseUnitException e) {
      throw new ValidationException("Failed to create database connection with schema", e);
    }
  }

  /**
   * Converts collection of column names to DbUnit Column array.
   *
   * <p>Creates columns with UNKNOWN type; DbUnit infers actual types from table data.
   *
   * @param columnNames collection of column names
   * @return DbUnit Column array
   */
  static Column[] toDbUnitColumns(final Collection<String> columnNames) {
    return columnNames.stream()
        .map(name -> new Column(name, DataType.UNKNOWN))
        .toArray(Column[]::new);
  }

  /**
   * Merges multiple tables with identical names into a single table.
   *
   * <p>Merge strategy:
   *
   * <ul>
   *   <li>All tables must have identical names and column structures
   *   <li>Column definitions taken from first table
   *   <li>All rows combined in order
   * </ul>
   *
   * @param tables list of tables with the same name (must not be empty)
   * @return merged table containing all rows
   * @throws IllegalArgumentException if tables have mismatched names or columns
   */
  private static Table mergeTables(final List<Table> tables) {
    final var representativeTable = tables.getFirst();
    final var expectedTableName = representativeTable.getName();
    final var expectedColumns = representativeTable.getColumns();

    // Validate all tables have same name and columns
    tables.forEach(
        table -> {
          if (!table.getName().equals(expectedTableName)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot merge tables with different names: expected '%s' but found '%s'",
                    expectedTableName.value(), table.getName().value()));
          }
          if (!table.getColumns().equals(expectedColumns)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot merge tables with different column structures for table '%s': expected %s but found %s",
                    expectedTableName.value(),
                    expectedColumns.stream().map(ColumnName::value).toList(),
                    table.getColumns().stream().map(ColumnName::value).toList()));
          }
        });

    // Collect all rows
    final var allRows = tables.stream().flatMap(table -> table.getRows().stream()).toList();

    return new MergedTable(expectedTableName, expectedColumns, allRows);
  }

  /**
   * Converts framework Table to DbUnit ITable (internal implementation).
   *
   * @param table the framework table
   * @return DbUnit ITable
   */
  private static ITable toDbUnitTableImpl(final Table table) {
    final var tableName = table.getName().value();
    final var columns =
        table.getColumns().stream()
            .map(col -> new Column(col.value(), DataType.UNKNOWN))
            .toArray(Column[]::new);

    final var metadata = new DefaultTableMetaData(tableName, columns);
    final var dbTable = new DefaultTable(metadata);

    // Add rows
    table
        .getRows()
        .forEach(
            row -> {
              final var rowValues = buildRowValues(row, table.getColumns());
              try {
                dbTable.addRow(rowValues);
              } catch (final DataSetException e) {
                throw new RuntimeException("Failed to add row to table: " + tableName, e);
              }
            });

    return dbTable;
  }

  /**
   * Builds row values array from Row object.
   *
   * <p>NullAway suppression required because DbUnit's {@code addRow()} explicitly accepts null
   * values to represent database NULL.
   *
   * @param row the row
   * @param columns the column names
   * @return array of values (may contain nulls)
   */
  @SuppressWarnings("NullAway") // DbUnit accepts null in Object[] for database NULL values
  private static Object[] buildRowValues(final Row row, final List<ColumnName> columns) {
    return columns.stream().map(row::getValue).map(DataValue::value).toArray(Object[]::new);
  }

  /**
   * Immutable merged table implementation.
   *
   * <p>Represents a table created by merging multiple tables with the same name. Combines rows from
   * multiple source tables while maintaining a single column structure.
   *
   * <h2>Use Case</h2>
   *
   * <p>When multiple {@code @DataSet} annotations use different scenario names, they each load the
   * same tables independently. This results in multiple Table instances with identical names but
   * different rows. MergedTable combines these to prevent DbUnit's {@code
   * AmbiguousTableNameException}.
   */
  private static final class MergedTable implements Table {

    /** Table name. */
    private final TableName name;

    /** Column names. */
    private final List<ColumnName> columns;

    /** Combined rows from all source tables. */
    private final List<Row> rows;

    /**
     * Creates merged table.
     *
     * @param name the table name
     * @param columns the column names
     * @param rows the combined rows
     */
    MergedTable(final TableName name, final List<ColumnName> columns, final List<Row> rows) {
      this.name = name;
      this.columns = List.copyOf(columns);
      this.rows = List.copyOf(rows);
    }

    @Override
    public TableName getName() {
      return name;
    }

    @Override
    public List<ColumnName> getColumns() {
      return columns;
    }

    @Override
    public List<Row> getRows() {
      return rows;
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }
  }
}
