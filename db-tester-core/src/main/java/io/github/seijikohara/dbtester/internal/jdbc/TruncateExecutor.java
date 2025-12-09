package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes TRUNCATE TABLE operations on database tables.
 *
 * <p>This class provides methods to truncate tables with automatic fallback to DELETE when TRUNCATE
 * is not supported.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class TruncateExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TruncateExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /**
   * Creates a new truncate executor with the specified SQL builder.
   *
   * @param sqlBuilder the SQL builder
   */
  public TruncateExecutor(final SqlBuilder sqlBuilder) {
    this.sqlBuilder = sqlBuilder;
  }

  /**
   * Executes TRUNCATE TABLE operations for all tables.
   *
   * <p>Falls back to DELETE ALL if TRUNCATE is not supported by the database.
   *
   * @param tables the tables to truncate
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void execute(final List<Table> tables, final Connection connection) throws SQLException {
    for (final var table : tables) {
      truncateTable(table.getName().value(), connection);
    }
  }

  /**
   * Truncates a single table.
   *
   * <p>Falls back to DELETE ALL if TRUNCATE is not supported by the database.
   *
   * @param tableName the table name
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void truncateTable(final String tableName, final Connection connection)
      throws SQLException {
    final var sql = sqlBuilder.buildTruncate(tableName);
    logger.trace("Executing TRUNCATE: {}", sql);

    try (final var stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
    } catch (final SQLException e) {
      logger.debug("TRUNCATE failed, falling back to DELETE for table {}", tableName);
      final var deleteSql = sqlBuilder.buildDeleteAll(tableName);
      try (final var deleteStmt = connection.createStatement()) {
        deleteStmt.executeUpdate(deleteSql);
      }
    }
  }
}
