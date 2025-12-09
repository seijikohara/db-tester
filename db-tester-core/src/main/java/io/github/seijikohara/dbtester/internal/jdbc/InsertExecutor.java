package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes INSERT operations on database tables.
 *
 * <p>This class provides methods to insert rows into database tables using batch operations for
 * optimal performance.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class InsertExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(InsertExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for setting PreparedStatement parameters. */
  private final ParameterBinder parameterBinder;

  /**
   * Creates a new insert executor with the specified dependencies.
   *
   * @param sqlBuilder the SQL builder
   * @param parameterBinder the parameter binder
   */
  public InsertExecutor(final SqlBuilder sqlBuilder, final ParameterBinder parameterBinder) {
    this.sqlBuilder = sqlBuilder;
    this.parameterBinder = parameterBinder;
  }

  /**
   * Executes INSERT operations for all tables.
   *
   * @param tables the tables to insert
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void execute(final List<Table> tables, final Connection connection) throws SQLException {
    for (final var table : tables) {
      insertTable(table, connection);
    }
  }

  /**
   * Inserts all rows for a single table.
   *
   * @param table the table to insert into
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void insertTable(final Table table, final Connection connection) throws SQLException {
    if (table.getRows().isEmpty()) {
      return;
    }

    final var sql = sqlBuilder.buildInsert(table);
    logger.trace("Executing INSERT: {}", sql);

    final var columnTypes = getColumnTypes(connection, table.getName().value());

    try (final var stmt = connection.prepareStatement(sql)) {
      for (final var row : table.getRows()) {
        parameterBinder.bindRowWithTypes(stmt, row, table.getColumns(), columnTypes);
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  /**
   * Inserts a single row into a table.
   *
   * @param table the table to insert into
   * @param row the row to insert
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void insertRow(final Table table, final Row row, final Connection connection)
      throws SQLException {
    final var sql = sqlBuilder.buildInsert(table);
    try (final var stmt = connection.prepareStatement(sql)) {
      parameterBinder.bindRow(stmt, row, table.getColumns());
      stmt.executeUpdate();
    }
  }

  /**
   * Gets column types from database metadata.
   *
   * @param connection the database connection
   * @param tableName the table name
   * @return a map of uppercase column names to SQL types
   * @throws SQLException if a database error occurs
   */
  private Map<String, Integer> getColumnTypes(final Connection connection, final String tableName)
      throws SQLException {
    final var sql = sqlBuilder.buildMetadataQuery(tableName);
    try (final var stmt = connection.prepareStatement(sql);
        final var rs = stmt.executeQuery()) {
      return parameterBinder.extractColumnTypes(rs.getMetaData());
    }
  }
}
