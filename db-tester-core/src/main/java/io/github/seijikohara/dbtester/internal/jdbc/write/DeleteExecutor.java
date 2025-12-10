package io.github.seijikohara.dbtester.internal.jdbc.write;

import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes DELETE operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to delete specific rows or
 * all rows from database tables.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class DeleteExecutor implements TableExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DeleteExecutor.class);

  /** The SQL builder for constructing SQL statements. */
  private final SqlBuilder sqlBuilder;

  /** The parameter binder for binding values to prepared statements. */
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

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> deleteTable(table, connection));
  }

  /**
   * Deletes specific rows from a table based on primary key.
   *
   * <p>Assumes the first column is the primary key.
   *
   * @param table the table to delete from
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  private void deleteTable(final Table table, final Connection connection) {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var primaryKeyColumn = table.getColumns().getFirst();
    final var sql = sqlBuilder.buildDelete(table.getName().value(), primaryKeyColumn);
    logger.trace("Executing DELETE: {}", sql);

    try (final var statementResource = open(() -> connection.prepareStatement(sql))) {
      final var preparedStatement = statementResource.value();
      table
          .getRows()
          .forEach(
              row -> {
                run(
                    () ->
                        parameterBinder.bind(preparedStatement, 1, row.getValue(primaryKeyColumn)));
                run(preparedStatement::addBatch);
              });
      run(preparedStatement::executeBatch);
    }
  }

  /**
   * Deletes all rows from all tables.
   *
   * @param tables the tables to delete from
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  public void executeDeleteAll(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> deleteAllRows(table.getName().value(), connection));
  }

  /**
   * Deletes all rows from a single table.
   *
   * @param tableName the table name
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  public void deleteAllRows(final String tableName, final Connection connection) {
    final var sql = sqlBuilder.buildDeleteAll(tableName);
    logger.trace("Executing DELETE ALL: {}", sql);
    try (final var statementResource = open(connection::createStatement)) {
      run(() -> statementResource.value().executeUpdate(sql));
    }
  }
}
