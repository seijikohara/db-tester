package io.github.seijikohara.dbtester.internal.jdbc.write;

import static io.github.seijikohara.dbtester.internal.jdbc.SqlIdentifier.validate;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds SQL statements for database operations.
 *
 * <p>This class provides methods to construct INSERT, UPDATE, and DELETE SQL statements from table
 * and column information.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class SqlBuilder {

  /** Creates a new SQL builder. */
  public SqlBuilder() {
    // Default constructor
  }

  /**
   * Builds an INSERT SQL statement for the given table.
   *
   * @param table the table
   * @return the INSERT SQL statement
   */
  public String buildInsert(final Table table) {
    final var columns =
        table.getColumns().stream()
            .map(col -> validate(col.value()))
            .collect(Collectors.joining(", "));
    final var placeholders =
        table.getColumns().stream().map(c -> "?").collect(Collectors.joining(", "));
    return String.format(
        "INSERT INTO %s (%s) VALUES (%s)",
        validate(table.getName().value()), columns, placeholders);
  }

  /**
   * Builds an UPDATE SQL statement.
   *
   * @param tableName the table name
   * @param pkColumn the primary key column
   * @param updateColumns the columns to update
   * @return the UPDATE SQL statement
   */
  public String buildUpdate(
      final String tableName, final ColumnName pkColumn, final List<ColumnName> updateColumns) {
    final var setClause =
        updateColumns.stream()
            .map(c -> String.format("%s = ?", validate(c.value())))
            .collect(Collectors.joining(", "));
    return String.format(
        "UPDATE %s SET %s WHERE %s = ?",
        validate(tableName), setClause, validate(pkColumn.value()));
  }

  /**
   * Builds a DELETE SQL statement with a WHERE clause.
   *
   * @param tableName the table name
   * @param pkColumn the primary key column for the WHERE clause
   * @return the DELETE SQL statement
   */
  public String buildDelete(final String tableName, final ColumnName pkColumn) {
    return String.format(
        "DELETE FROM %s WHERE %s = ?", validate(tableName), validate(pkColumn.value()));
  }

  /**
   * Builds a DELETE ALL SQL statement.
   *
   * @param tableName the table name
   * @return the DELETE ALL SQL statement
   */
  public String buildDeleteAll(final String tableName) {
    return String.format("DELETE FROM %s", validate(tableName));
  }

  /**
   * Builds a TRUNCATE TABLE SQL statement.
   *
   * @param tableName the table name
   * @return the TRUNCATE TABLE SQL statement
   */
  public String buildTruncate(final String tableName) {
    return String.format("TRUNCATE TABLE %s", validate(tableName));
  }

  /**
   * Builds a SELECT query for fetching column metadata.
   *
   * @param tableName the table name
   * @return the SELECT SQL statement that returns no rows but includes all column metadata
   */
  public String buildMetadataQuery(final String tableName) {
    return String.format("SELECT * FROM %s WHERE 1=0", validate(tableName));
  }
}
