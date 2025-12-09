package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes UPDATE operations on database tables.
 *
 * <p>This class provides methods to update rows in database tables. It assumes the first column in
 * the table is the primary key.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class UpdateExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(UpdateExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for setting PreparedStatement parameters. */
  private final ParameterBinder parameterBinder;

  /**
   * Creates a new update executor with the specified dependencies.
   *
   * @param sqlBuilder the SQL builder
   * @param parameterBinder the parameter binder
   */
  public UpdateExecutor(final SqlBuilder sqlBuilder, final ParameterBinder parameterBinder) {
    this.sqlBuilder = sqlBuilder;
    this.parameterBinder = parameterBinder;
  }

  /**
   * Executes UPDATE operations for all tables.
   *
   * @param tables the tables to update
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void execute(final List<Table> tables, final Connection connection) throws SQLException {
    for (final var table : tables) {
      updateTable(table, connection);
    }
  }

  /**
   * Updates all rows for a single table.
   *
   * <p>Assumes the first column is the primary key.
   *
   * @param table the table to update
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void updateTable(final Table table, final Connection connection) throws SQLException {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var columns = table.getColumns();
    final var pkColumn = columns.getFirst();
    final var updateColumns = columns.subList(1, columns.size());

    if (updateColumns.isEmpty()) {
      return;
    }

    final var sql = sqlBuilder.buildUpdate(table.getName().value(), pkColumn, updateColumns);
    logger.trace("Executing UPDATE: {}", sql);

    try (final var stmt = connection.prepareStatement(sql)) {
      table
          .getRows()
          .forEach(
              row -> {
                try {
                  IntStream.range(0, updateColumns.size())
                      .forEach(
                          i -> {
                            try {
                              parameterBinder.bind(stmt, i + 1, row.getValue(updateColumns.get(i)));
                            } catch (final SQLException e) {
                              throw new RuntimeException(e);
                            }
                          });
                  parameterBinder.bind(stmt, updateColumns.size() + 1, row.getValue(pkColumn));
                  stmt.addBatch();
                } catch (final SQLException e) {
                  throw new RuntimeException(e);
                }
              });
      stmt.executeBatch();
    }
  }

  /**
   * Attempts to update a single row.
   *
   * @param tableName the table name
   * @param pkColumn the primary key column
   * @param updateColumns the columns to update
   * @param row the row data
   * @param connection the database connection
   * @return true if the update affected at least one row
   * @throws SQLException if a database error occurs
   */
  public boolean tryUpdateRow(
      final String tableName,
      final ColumnName pkColumn,
      final List<ColumnName> updateColumns,
      final Row row,
      final Connection connection)
      throws SQLException {
    if (updateColumns.isEmpty()) {
      return false;
    }

    final var sql = sqlBuilder.buildUpdate(tableName, pkColumn, updateColumns);
    try (final var stmt = connection.prepareStatement(sql)) {
      IntStream.range(0, updateColumns.size())
          .forEach(
              i -> {
                try {
                  parameterBinder.bind(stmt, i + 1, row.getValue(updateColumns.get(i)));
                } catch (final SQLException e) {
                  throw new RuntimeException(e);
                }
              });
      parameterBinder.bind(stmt, updateColumns.size() + 1, row.getValue(pkColumn));
      return stmt.executeUpdate() > 0;
    }
  }
}
