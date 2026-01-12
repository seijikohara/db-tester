package io.github.seijikohara.dbtester.internal.jdbc.write;

import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes TRUNCATE TABLE operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to truncate tables with
 * automatic fallback to DELETE when TRUNCATE is not supported.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class TruncateExecutor implements TableExecutor {

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

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> truncateTable(table.getName().value(), connection, null));
  }

  @Override
  public void execute(
      final List<Table> tables,
      final Connection connection,
      final @Nullable Duration queryTimeout) {
    tables.forEach(table -> truncateTable(table.getName().value(), connection, queryTimeout));
  }

  /**
   * Truncates a single table by name.
   *
   * <p>Falls back to DELETE ALL if TRUNCATE is not supported by the database.
   *
   * @param tableName the table name
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  public void truncateTable(final String tableName, final Connection connection) {
    truncateTable(tableName, connection, null);
  }

  /**
   * Truncates a single table by name with a query timeout.
   *
   * <p>Falls back to DELETE ALL if TRUNCATE is not supported by the database.
   *
   * @param tableName the table name
   * @param connection the database connection
   * @param queryTimeout the query timeout, or null for no timeout
   * @throws DatabaseOperationException if a database error occurs
   */
  public void truncateTable(
      final String tableName, final Connection connection, final @Nullable Duration queryTimeout) {
    final var sql = sqlBuilder.buildTruncate(tableName);
    logger.trace("Executing TRUNCATE: {}", sql);

    try {
      try (final var statementResource = open(connection::createStatement)) {
        applyTimeout(statementResource.value(), queryTimeout);
        run(() -> statementResource.value().executeUpdate(sql));
      }
    } catch (final DatabaseOperationException e) {
      logger.debug("TRUNCATE failed, falling back to DELETE for table {}", tableName);
      final var deleteSql = sqlBuilder.buildDeleteAll(tableName);
      try (final var deleteStatementResource = open(connection::createStatement)) {
        applyTimeout(deleteStatementResource.value(), queryTimeout);
        run(() -> deleteStatementResource.value().executeUpdate(deleteSql));
      }
    }
  }

  /**
   * Applies the query timeout to a statement if specified.
   *
   * @param statement the statement
   * @param queryTimeout the query timeout, or null for no timeout
   */
  private void applyTimeout(final Statement statement, final @Nullable Duration queryTimeout) {
    if (queryTimeout != null) {
      run(() -> statement.setQueryTimeout((int) queryTimeout.toSeconds()));
    }
  }
}
