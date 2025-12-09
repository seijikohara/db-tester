package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes DELETE operations on database tables.
 *
 * <p>This class provides methods to delete specific rows or all rows from database tables.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class DeleteExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DeleteExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for setting PreparedStatement parameters. */
  private final ParameterBinder parameterBinder;

  /**
   * Creates a new delete executor with the specified dependencies.
   *
   * @param sqlBuilder the SQL builder
   * @param parameterBinder the parameter binder
   */
  public DeleteExecutor(final SqlBuilder sqlBuilder, final ParameterBinder parameterBinder) {
    this.sqlBuilder = sqlBuilder;
    this.parameterBinder = parameterBinder;
  }

  /**
   * Executes DELETE operations for specific rows in all tables.
   *
   * <p>Deletes rows based on the primary key (first column).
   *
   * @param tables the tables to delete from
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void execute(final List<Table> tables, final Connection connection) throws SQLException {
    for (final var table : tables) {
      deleteRows(table, connection);
    }
  }

  /**
   * Deletes specific rows from a table based on primary key.
   *
   * <p>Assumes the first column is the primary key.
   *
   * @param table the table to delete from
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void deleteRows(final Table table, final Connection connection) throws SQLException {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var pkColumn = table.getColumns().getFirst();
    final var sql = sqlBuilder.buildDelete(table.getName().value(), pkColumn);
    logger.trace("Executing DELETE: {}", sql);

    try (final var stmt = connection.prepareStatement(sql)) {
      for (final var row : table.getRows()) {
        parameterBinder.bind(stmt, 1, row.getValue(pkColumn));
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  /**
   * Deletes all rows from all tables.
   *
   * @param tables the tables to delete from
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void executeDeleteAll(final List<Table> tables, final Connection connection)
      throws SQLException {
    for (final var table : tables) {
      deleteAllRows(table.getName().value(), connection);
    }
  }

  /**
   * Deletes all rows from a single table.
   *
   * @param tableName the table name
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void deleteAllRows(final String tableName, final Connection connection)
      throws SQLException {
    final var sql = sqlBuilder.buildDeleteAll(tableName);
    logger.trace("Executing DELETE ALL: {}", sql);
    try (final var stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
    }
  }
}
