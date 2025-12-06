package io.github.seijikohara.dbtester.internal.dbunit;

import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.SimpleDataSet;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DbUnit operations bridge.
 *
 * <p>This class provides the bridge between the db-tester API and DbUnit operations.
 */
public class DbUnitOperations {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DbUnitOperations.class);

  /** The comparator for dataset assertions. */
  private final DataSetComparator comparator = new DataSetComparator();

  /** Creates a new instance of DbUnit operations bridge. */
  public DbUnitOperations() {
    // Default constructor
  }

  /**
   * Executes a database operation on the given dataset.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param dataSource the data source
   */
  public void execute(
      final Operation operation, final DataSet dataSet, final DataSource dataSource) {
    final var dbUnitOperation = toDbUnitOperation(operation);
    final var dbUnitDataSet = toDbUnitDataSet(dataSet);

    try (final var connection = dataSource.getConnection()) {
      // Resolve schema from the connection to avoid AmbiguousTableNameException (especially in H2)
      final var schema = connection.getSchema();
      final var dbConnection = new DatabaseConnection(connection, schema);
      dbUnitOperation.execute(dbConnection, dbUnitDataSet);
      logger.debug("Executed {} operation on dataset with schema: {}", operation, schema);
    } catch (final SQLException | DatabaseUnitException e) {
      throw new DatabaseTesterException("Failed to execute database operation: " + operation, e);
    }
  }

  /**
   * Asserts that the results of a SQL query match the expected dataset.
   *
   * @param expected the expected dataset
   * @param dataSource the data source
   * @param sqlQuery the SQL query to execute
   * @param tableName the table name in the expected dataset
   * @param ignoreColumnNames columns to exclude from comparison
   */
  public void assertEqualsByQuery(
      final DataSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var actualTable = executeQuery(dataSource, sqlQuery, tableName);
    final var expectedTable =
        expected
            .getTable(new TableName(tableName))
            .orElseThrow(() -> new AssertionError("Expected table not found: " + tableName));

    comparator.assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumnNames);
  }

  /**
   * Asserts that the results of a SQL query match the expected table.
   *
   * @param expected the expected table
   * @param dataSource the data source
   * @param tableName the table name
   * @param sqlQuery the SQL query to execute
   * @param ignoreColumnNames columns to exclude from comparison
   */
  public void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    final var actualTable = executeQuery(dataSource, sqlQuery, tableName);
    comparator.assertEqualsIgnoreColumns(expected, actualTable, ignoreColumnNames);
  }

  /**
   * Fetches the current state of a table from the database.
   *
   * @param dataSource the data source
   * @param tableName the table name
   * @return the current table data
   */
  public Table fetchTable(final DataSource dataSource, final String tableName) {
    return executeQuery(dataSource, "SELECT * FROM " + tableName, tableName);
  }

  /**
   * Fetches the current state of multiple tables from the database.
   *
   * @param dataSource the data source
   * @param tableNames the table names
   * @return the current dataset
   */
  public DataSet fetchDataSet(final DataSource dataSource, final List<String> tableNames) {
    final var tables =
        tableNames.stream().map(tableName -> fetchTable(dataSource, tableName)).toList();
    return new SimpleDataSet(tables);
  }

  /**
   * Executes a SQL query and returns the results as a Table.
   *
   * @param dataSource the data source
   * @param sqlQuery the SQL query to execute
   * @param tableName the table name for the results
   * @return the query results as a Table
   */
  private Table executeQuery(
      final DataSource dataSource, final String sqlQuery, final String tableName) {
    try (final var connection = dataSource.getConnection();
        final var stmt = connection.prepareStatement(sqlQuery);
        final var rs = stmt.executeQuery()) {

      final var metaData = rs.getMetaData();
      final var columnCount = metaData.getColumnCount();

      final var columnNames =
          IntStream.rangeClosed(1, columnCount)
              .mapToObj(
                  i -> {
                    try {
                      return new ColumnName(metaData.getColumnName(i));
                    } catch (final SQLException e) {
                      throw new DatabaseTesterException(
                          "Failed to retrieve column name at index: " + i, e);
                    }
                  })
              .toList();

      final var rows = readAllRows(rs, columnNames, columnCount);

      return new SimpleTable(new TableName(tableName), columnNames, rows);
    } catch (final SQLException e) {
      throw new DatabaseTesterException("Failed to execute query: " + sqlQuery, e);
    }
  }

  /**
   * Reads all rows from a ResultSet.
   *
   * <p>This method uses an imperative loop because ResultSet iteration is inherently stateful and
   * side-effecting. The JDBC ResultSet API requires sequential cursor-based access, which cannot be
   * effectively modeled with functional stream operations without introducing hidden side effects.
   *
   * @param rs the result set to read
   * @param columnNames the column names
   * @param columnCount the number of columns
   * @return list of rows
   * @throws SQLException if reading fails
   */
  private List<Row> readAllRows(
      final ResultSet rs, final List<ColumnName> columnNames, final int columnCount)
      throws SQLException {
    final var rows = new ArrayList<Row>();
    while (rs.next()) {
      final var values =
          IntStream.rangeClosed(1, columnCount)
              .boxed()
              .collect(
                  Collectors.toMap(
                      i -> columnNames.get(i - 1),
                      i -> {
                        try {
                          final var value = rs.getObject(i);
                          // Use DataValue.NULL for null database values instead of raw null
                          // to prevent NullPointerException in Map.copyOf() within SimpleRow
                          return value != null ? new DataValue(value) : DataValue.NULL;
                        } catch (final SQLException e) {
                          throw new DatabaseTesterException(
                              "Failed to retrieve value at column index: " + i, e);
                        }
                      },
                      (v1, v2) -> v1,
                      LinkedHashMap::new));
      rows.add(new SimpleRow(values));
    }
    return List.copyOf(rows);
  }

  /**
   * Converts a db-tester operation to a DbUnit operation.
   *
   * @param operation the db-tester operation
   * @return the corresponding DbUnit operation
   */
  private DatabaseOperation toDbUnitOperation(final Operation operation) {
    return OperationConverter.toDbUnitOperation(operation);
  }

  /**
   * Wraps a db-tester dataset as a DbUnit dataset.
   *
   * @param dataSet the db-tester dataset
   * @return the DbUnit dataset adapter
   */
  private IDataSet toDbUnitDataSet(final DataSet dataSet) {
    return new DbUnitDataSetAdapter(dataSet);
  }
}
