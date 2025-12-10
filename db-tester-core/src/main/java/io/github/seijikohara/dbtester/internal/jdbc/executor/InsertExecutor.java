package io.github.seijikohara.dbtester.internal.jdbc.executor;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import io.github.seijikohara.dbtester.internal.jdbc.ParameterBinder;
import io.github.seijikohara.dbtester.internal.jdbc.SqlBuilder;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
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
    tables.forEach(table -> insertTable(table, connection));
  }

  /**
   * Inserts all rows for a single table.
   *
   * @param table the table to insert into
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  private void insertTable(final Table table, final Connection connection) {
    if (table.getRows().isEmpty()) {
      return;
    }

    final var sql = sqlBuilder.buildInsert(table);
    logger.trace("Executing INSERT: {}", sql);

    final var columnTypes = getColumnTypes(connection, table.getName().value());

    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
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
    final var sql = sqlBuilder.buildInsert(table);
    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      run(() -> parameterBinder.bindRow(statementResource.value(), row, table.getColumns()));
      run(statementResource.value()::executeUpdate);
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
}
