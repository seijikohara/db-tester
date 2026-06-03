package io.github.seijikohara.dbtester.internal.jdbc.read;

import static io.github.seijikohara.dbtester.internal.jdbc.SqlIdentifier.validate;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import io.github.seijikohara.dbtester.internal.jdbc.type.TypeHandlerRegistry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Reads table data from a database using JDBC.
 *
 * <p>This class provides methods to retrieve table data for comparison and verification purposes.
 * It delegates type conversion to {@link TypeConverter}.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class TableReader {

  /** The type converter for handling CLOB/BLOB values. */
  private final TypeConverter typeConverter;

  /** The registry that resolves custom type handlers by SQL type and database product. */
  private final TypeHandlerRegistry typeHandlerRegistry;

  /** Creates a new table reader with a default type converter. */
  public TableReader() {
    this(new TypeConverter());
  }

  /**
   * Creates a new table reader with the specified type converter.
   *
   * @param typeConverter the type converter to use
   */
  public TableReader(final TypeConverter typeConverter) {
    this(typeConverter, TypeHandlerRegistry.getInstance());
  }

  /**
   * Creates a new table reader with the specified type converter and type handler registry.
   *
   * @param typeConverter the type converter to use
   * @param typeHandlerRegistry the registry that resolves custom type handlers
   */
  TableReader(final TypeConverter typeConverter, final TypeHandlerRegistry typeHandlerRegistry) {
    this.typeConverter = typeConverter;
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  /**
   * Fetches the current state of a table from the database.
   *
   * @param dataSource the data source
   * @param tableName the table name
   * @return the current table data
   * @throws DatabaseTesterException if fetching fails
   */
  public Table fetchTable(final DataSource dataSource, final String tableName) {
    return fetchTable(dataSource, tableName, (Duration) null);
  }

  /**
   * Fetches the current state of a table from the database with a query timeout.
   *
   * @param dataSource the data source
   * @param tableName the table name
   * @param queryTimeout the query timeout, or null for no timeout
   * @return the current table data
   * @throws DatabaseTesterException if fetching fails
   */
  public Table fetchTable(
      final DataSource dataSource, final String tableName, final @Nullable Duration queryTimeout) {
    return executeQuery(
        dataSource,
        String.format("SELECT * FROM %s", validate(tableName)),
        tableName,
        queryTimeout);
  }

  /**
   * Fetches the current state of a table, including only specified columns.
   *
   * @param dataSource the data source
   * @param tableName the table name
   * @param columns the columns to include
   * @return the current table data with only the specified columns
   * @throws DatabaseTesterException if fetching fails
   */
  public Table fetchTable(
      final DataSource dataSource, final String tableName, final Collection<ColumnName> columns) {
    return fetchTable(dataSource, tableName, columns, null);
  }

  /**
   * Fetches the current state of a table with query timeout, including only specified columns.
   *
   * @param dataSource the data source
   * @param tableName the table name
   * @param columns the columns to include
   * @param queryTimeout the query timeout, or null for no timeout
   * @return the current table data with only the specified columns
   * @throws DatabaseTesterException if fetching fails
   */
  public Table fetchTable(
      final DataSource dataSource,
      final String tableName,
      final Collection<ColumnName> columns,
      final @Nullable Duration queryTimeout) {
    if (columns.isEmpty()) {
      return fetchTable(dataSource, tableName, queryTimeout);
    }

    final var columnList =
        columns.stream().map(col -> validate(col.value())).collect(Collectors.joining(", "));
    final var sql = String.format("SELECT %s FROM %s", columnList, validate(tableName));
    return executeQuery(dataSource, sql, tableName, queryTimeout);
  }

  /**
   * Fetches the current state of multiple tables from the database.
   *
   * @param dataSource the data source
   * @param tableNames the table names
   * @return the current dataset
   * @throws DatabaseTesterException if fetching fails
   */
  public TableSet fetchTableSet(final DataSource dataSource, final List<String> tableNames) {
    return fetchTableSet(dataSource, tableNames, null);
  }

  /**
   * Fetches the current state of multiple tables from the database with a query timeout.
   *
   * @param dataSource the data source
   * @param tableNames the table names
   * @param queryTimeout the query timeout, or null for no timeout
   * @return the current dataset
   * @throws DatabaseTesterException if fetching fails
   */
  public TableSet fetchTableSet(
      final DataSource dataSource,
      final List<String> tableNames,
      final @Nullable Duration queryTimeout) {
    final var tables =
        tableNames.stream().map(name -> fetchTable(dataSource, name, queryTimeout)).toList();
    return new SimpleTableSet(tables);
  }

  /**
   * Executes a SQL query and returns the results as a Table.
   *
   * @param dataSource the data source
   * @param sqlQuery the SQL query to execute
   * @param tableName the table name for the results
   * @return the query results as a Table
   * @throws DatabaseTesterException if the query fails
   */
  public Table executeQuery(
      final DataSource dataSource, final String sqlQuery, final String tableName) {
    return executeQuery(dataSource, sqlQuery, tableName, null);
  }

  /**
   * Executes a SQL query and returns the results as a Table with a query timeout.
   *
   * @param dataSource the data source
   * @param sqlQuery the SQL query to execute
   * @param tableName the table name for the results
   * @param queryTimeout the query timeout, or null for no timeout
   * @return the query results as a Table
   * @throws DatabaseTesterException if the query fails
   */
  public Table executeQuery(
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final @Nullable Duration queryTimeout) {
    try (final var connection = dataSource.getConnection();
        final var statement = connection.prepareStatement(sqlQuery)) {

      if (queryTimeout != null) {
        statement.setQueryTimeout((int) queryTimeout.toSeconds());
      }

      final var databaseProductName = resolveDatabaseProductName(connection);

      try (final var resultSet = statement.executeQuery()) {
        final var metaData = resultSet.getMetaData();
        final var columnCount = metaData.getColumnCount();

        final var columnNames =
            IntStream.rangeClosed(1, columnCount)
                .mapToObj(
                    i -> {
                      try {
                        // Use the column label so SQL aliases (SELECT col AS alias) and computed
                        // columns (SELECT SUM(x) AS total) match the expected dataset. For a plain
                        // SELECT * the label defaults to the physical column name, so full-table
                        // reads keep their previous behavior.
                        return new ColumnName(metaData.getColumnLabel(i));
                      } catch (final SQLException e) {
                        throw new DatabaseTesterException(
                            String.format("Failed to retrieve column label at index: %d", i), e);
                      }
                    })
                .toList();

        final var rows =
            readAllRows(resultSet, columnNames, columnCount, metaData, databaseProductName);

        return new SimpleTable(new TableName(tableName), columnNames, rows);
      }
    } catch (final SQLException e) {
      throw new DatabaseTesterException(String.format("Failed to execute query: %s", sqlQuery), e);
    }
  }

  /**
   * Reads all rows from a ResultSet.
   *
   * <p>This method uses an imperative loop because ResultSet iteration is inherently stateful and
   * side-effecting. The JDBC ResultSet API requires sequential cursor-based access, which cannot be
   * effectively modeled with functional stream operations without introducing hidden side effects.
   *
   * <p>CLOB and BLOB values are converted immediately to avoid issues with closed connections.
   *
   * @param resultSet the result set to read
   * @param columnNames the column names
   * @param columnCount the number of columns
   * @param metaData the result set metadata for resolving column SQL types
   * @param databaseProductName the database product name for type-handler resolution
   * @return list of rows
   * @throws SQLException if reading fails
   */
  private List<Row> readAllRows(
      final ResultSet resultSet,
      final List<ColumnName> columnNames,
      final int columnCount,
      final ResultSetMetaData metaData,
      final String databaseProductName)
      throws SQLException {
    final var rows = new ArrayList<Row>();
    while (resultSet.next()) {
      final var values = new LinkedHashMap<ColumnName, CellValue>();
      IntStream.rangeClosed(1, columnCount)
          .forEach(
              i -> {
                try {
                  final var columnName = columnNames.get(i - 1);
                  values.put(columnName, readCell(resultSet, metaData, i, databaseProductName));
                } catch (final SQLException e) {
                  throw new DatabaseTesterException(
                      String.format("Failed to read column at index: %d", i), e);
                }
              });
      rows.add(new SimpleRow(values));
    }
    return List.copyOf(rows);
  }

  /**
   * Reads a single column value, applying a custom type handler when one is registered.
   *
   * <p>When a {@link TypeHandler} is registered for the column SQL type and database product, the
   * handler reads and formats the value. Otherwise the value is read with {@link
   * ResultSet#getObject(int)} and normalized by the {@link TypeConverter}.
   *
   * @param resultSet the result set positioned at the current row
   * @param metaData the result set metadata
   * @param columnIndex the column index (1-based)
   * @param databaseProductName the database product name for type-handler resolution
   * @return the cell value, or {@link CellValue#NULL} for a null database value
   * @throws SQLException if reading fails
   */
  private CellValue readCell(
      final ResultSet resultSet,
      final ResultSetMetaData metaData,
      final int columnIndex,
      final String databaseProductName)
      throws SQLException {
    final var sqlType = metaData.getColumnType(columnIndex);
    final var handler = typeHandlerRegistry.findHandler(sqlType, databaseProductName);
    if (handler.isPresent()) {
      final var formatted = readWithHandler(handler.get(), resultSet, columnIndex);
      return formatted != null ? new CellValue(formatted) : CellValue.NULL;
    }
    // Convert LOB types immediately to avoid issues with closed connections.
    final var value = typeConverter.convert(resultSet.getObject(columnIndex));
    return value != null ? new CellValue(value) : CellValue.NULL;
  }

  /**
   * Reads a value with a resolved custom type handler and formats it for the dataset.
   *
   * @param handler the type handler resolved for the column
   * @param resultSet the result set positioned at the current row
   * @param columnIndex the column index (1-based)
   * @return the formatted value, or null if the database value is null
   * @throws SQLException if reading fails
   */
  @SuppressWarnings("unchecked")
  private @Nullable String readWithHandler(
      final TypeHandler<?> handler, final ResultSet resultSet, final int columnIndex)
      throws SQLException {
    final var typedHandler = (TypeHandler<Object>) handler;
    final var value = typedHandler.read(resultSet, columnIndex);
    return value != null ? typedHandler.format(value) : null;
  }

  /**
   * Resolves the database product name from the connection metadata.
   *
   * <p>The product name selects database-specific {@link TypeHandler} implementations. An empty
   * result disables custom type handling for the read, falling back to standard JDBC reads.
   *
   * @param connection the database connection
   * @return the database product name, or an empty string if it cannot be determined
   * @throws SQLException if metadata access fails
   */
  private String resolveDatabaseProductName(final Connection connection) throws SQLException {
    final var metaData = connection.getMetaData();
    final var name = metaData != null ? metaData.getDatabaseProductName() : null;
    return name != null ? name : "";
  }
}
