package io.github.seijikohara.dbtester.internal.jdbc.read;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.internal.dataset.SimpleDataSet;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;

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

  /** Creates a new table reader with a default type converter. */
  public TableReader() {
    this.typeConverter = new TypeConverter();
  }

  /**
   * Creates a new table reader with the specified type converter.
   *
   * @param typeConverter the type converter to use
   */
  public TableReader(final TypeConverter typeConverter) {
    this.typeConverter = typeConverter;
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
    return executeQuery(dataSource, String.format("SELECT * FROM %s", tableName), tableName);
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
    if (columns.isEmpty()) {
      return fetchTable(dataSource, tableName);
    }

    final var columnList =
        columns.stream().map(ColumnName::value).collect(Collectors.joining(", "));
    final var sql = String.format("SELECT %s FROM %s", columnList, tableName);
    return executeQuery(dataSource, sql, tableName);
  }

  /**
   * Fetches the current state of multiple tables from the database.
   *
   * @param dataSource the data source
   * @param tableNames the table names
   * @return the current dataset
   * @throws DatabaseTesterException if fetching fails
   */
  public DataSet fetchDataSet(final DataSource dataSource, final List<String> tableNames) {
    final var tables = tableNames.stream().map(name -> fetchTable(dataSource, name)).toList();
    return new SimpleDataSet(tables);
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
    try (final var connection = dataSource.getConnection();
        final var statement = connection.prepareStatement(sqlQuery);
        final var resultSet = statement.executeQuery()) {

      final var metaData = resultSet.getMetaData();
      final var columnCount = metaData.getColumnCount();

      final var columnNames =
          IntStream.rangeClosed(1, columnCount)
              .mapToObj(
                  i -> {
                    try {
                      return new ColumnName(metaData.getColumnName(i));
                    } catch (final SQLException e) {
                      throw new DatabaseTesterException(
                          String.format("Failed to retrieve column name at index: %d", i), e);
                    }
                  })
              .toList();

      final var rows = readAllRows(resultSet, columnNames, columnCount);

      return new SimpleTable(new TableName(tableName), columnNames, rows);
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
   * @return list of rows
   * @throws SQLException if reading fails
   */
  private List<Row> readAllRows(
      final ResultSet resultSet, final List<ColumnName> columnNames, final int columnCount)
      throws SQLException {
    final var rows = new ArrayList<Row>();
    while (resultSet.next()) {
      final var values = new LinkedHashMap<ColumnName, CellValue>();
      IntStream.rangeClosed(1, columnCount)
          .forEach(
              i -> {
                try {
                  final var columnName = columnNames.get(i - 1);
                  final var rawValue = resultSet.getObject(i);
                  // Convert LOB types immediately to avoid issues with closed connections
                  final var value = typeConverter.convert(rawValue);
                  // Use CellValue.NULL for null database values
                  values.put(columnName, value != null ? new CellValue(value) : CellValue.NULL);
                } catch (final SQLException e) {
                  throw new DatabaseTesterException(
                      String.format("Failed to read column at index: %d", i), e);
                }
              });
      rows.add(new SimpleRow(values));
    }
    return List.copyOf(rows);
  }
}
