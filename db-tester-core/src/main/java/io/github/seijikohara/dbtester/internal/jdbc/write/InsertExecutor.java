package io.github.seijikohara.dbtester.internal.jdbc.write;

import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes INSERT operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to insert rows into database
 * tables using batch operations for optimal performance.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class InsertExecutor implements TableExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(InsertExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for binding values to prepared statements. */
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

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> insertTable(table, connection, null));
  }

  @Override
  public void execute(
      final List<Table> tables,
      final Connection connection,
      final @Nullable Duration queryTimeout) {
    tables.forEach(table -> insertTable(table, connection, queryTimeout));
  }

  /**
   * Inserts all rows for a single table.
   *
   * @param table the table to insert into
   * @param connection the database connection
   * @param queryTimeout the query timeout, or null for no timeout
   * @throws DatabaseOperationException if a database error occurs
   */
  private void insertTable(
      final Table table, final Connection connection, final @Nullable Duration queryTimeout) {
    if (table.getRows().isEmpty()) {
      return;
    }

    final var sql = sqlBuilder.buildInsert(table);
    logger.trace("Executing INSERT: {}", sql);

    final var columnTypes = getColumnTypes(connection, table.getName().value());

    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
      applyTimeout(preparedStatement, queryTimeout);
      table
          .getRows()
          .forEach(
              row -> {
                run(
                    () ->
                        parameterBinder.bindRowWithTypes(
                            preparedStatement, row, table.getColumns(), columnTypes));
                run(preparedStatement::addBatch);
              });
      run(preparedStatement::executeBatch);
    }
  }

  /**
   * Inserts a single row into a table.
   *
   * @param table the table to insert into
   * @param row the row to insert
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  public void insertRow(final Table table, final Row row, final Connection connection) {
    insertRow(table, row, connection, null);
  }

  /**
   * Inserts a single row into a table with a query timeout.
   *
   * @param table the table to insert into
   * @param row the row to insert
   * @param connection the database connection
   * @param queryTimeout the query timeout, or null for no timeout
   * @throws DatabaseOperationException if a database error occurs
   */
  public void insertRow(
      final Table table,
      final Row row,
      final Connection connection,
      final @Nullable Duration queryTimeout) {
    final var sql = sqlBuilder.buildInsert(table);
    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
      applyTimeout(preparedStatement, queryTimeout);
      run(() -> parameterBinder.bindRow(preparedStatement, row, table.getColumns()));
      run(preparedStatement::executeUpdate);
    }
  }

  /**
   * Gets column types from database metadata.
   *
   * @param connection the database connection
   * @param tableName the table name
   * @return a map of uppercase column names to SQL types
   * @throws DatabaseOperationException if a database error occurs
   */
  private Map<String, Integer> getColumnTypes(final Connection connection, final String tableName) {
    final var sql = sqlBuilder.buildMetadataQuery(tableName);
    try (final var statementResource = open(() -> connection.prepareStatement(sql));
        final var resultSetResource = open(statementResource.value()::executeQuery)) {
      return get(() -> parameterBinder.extractColumnTypes(resultSetResource.value().getMetaData()));
    }
  }

  /**
   * Applies the query timeout to a prepared statement if specified.
   *
   * @param statement the prepared statement
   * @param queryTimeout the query timeout, or null for no timeout
   */
  private void applyTimeout(
      final PreparedStatement statement, final @Nullable Duration queryTimeout) {
    if (queryTimeout != null) {
      run(() -> statement.setQueryTimeout((int) queryTimeout.toSeconds()));
    }
  }
}
