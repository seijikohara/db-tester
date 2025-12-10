package io.github.seijikohara.dbtester.internal.jdbc.executor;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import io.github.seijikohara.dbtester.internal.jdbc.ParameterBinder;
import io.github.seijikohara.dbtester.internal.jdbc.SqlBuilder;
import java.sql.Connection;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes UPDATE operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to update rows in database
 * tables. It assumes the first column in the table is the primary key.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class UpdateExecutor implements TableExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(UpdateExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for binding values to prepared statements. */
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

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> updateTable(table, connection));
  }

  /**
   * Updates all rows for a single table.
   *
   * <p>Assumes the first column is the primary key.
   *
   * @param table the table to update
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  private void updateTable(final Table table, final Connection connection) {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var columns = table.getColumns();
    final var primaryKeyColumn = columns.getFirst();
    final var updateColumns = columns.subList(1, columns.size());

    if (updateColumns.isEmpty()) {
      return;
    }

    final var sql =
        sqlBuilder.buildUpdate(table.getName().value(), primaryKeyColumn, updateColumns);
    logger.trace("Executing UPDATE: {}", sql);

    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
      table
          .getRows()
          .forEach(
              row -> {
                IntStream.range(0, updateColumns.size())
                    .forEach(
                        index ->
                            run(
                                () ->
                                    parameterBinder.bind(
                                        preparedStatement,
                                        index + 1,
                                        row.getValue(updateColumns.get(index)))));
                run(
                    () ->
                        parameterBinder.bind(
                            preparedStatement,
                            updateColumns.size() + 1,
                            row.getValue(primaryKeyColumn)));
                run(preparedStatement::addBatch);
              });
      run(preparedStatement::executeBatch);
    }
  }

  /**
   * Attempts to update a single row.
   *
   * @param tableName the table name
   * @param primaryKeyColumn the primary key column
   * @param updateColumns the columns to update
   * @param row the row data
   * @param connection the database connection
   * @return true if the update affected at least one row
   * @throws DatabaseOperationException if a database error occurs
   */
  public boolean tryUpdateRow(
      final String tableName,
      final ColumnName primaryKeyColumn,
      final List<ColumnName> updateColumns,
      final Row row,
      final Connection connection) {
    if (updateColumns.isEmpty()) {
      return false;
    }

    final var sql = sqlBuilder.buildUpdate(tableName, primaryKeyColumn, updateColumns);
    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
      IntStream.range(0, updateColumns.size())
          .forEach(
              index ->
                  run(
                      () ->
                          parameterBinder.bind(
                              preparedStatement,
                              index + 1,
                              row.getValue(updateColumns.get(index)))));
      run(
          () ->
              parameterBinder.bind(
                  preparedStatement, updateColumns.size() + 1, row.getValue(primaryKeyColumn)));
      return get(preparedStatement::executeUpdate) > 0;
    }
  }
}
