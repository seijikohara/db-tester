package io.github.seijikohara.dbtester.api.spi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Service Provider Interface for custom database type handling.
 *
 * <p>This interface allows extensions to register handlers for database-specific types such as
 * PostgreSQL JSON/JSONB, UUID, ARRAY, or user-defined types. Implementations are discovered via
 * {@link java.util.ServiceLoader}.
 *
 * <p>Each handler declares which SQL types it supports and provides methods for reading values from
 * result sets, writing values to prepared statements, and converting between Java objects and
 * string representations for import/export.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * public class PostgresJsonHandler implements TypeHandler<JsonNode> {
 *     @Override
 *     public Class<JsonNode> javaType() {
 *         return JsonNode.class;
 *     }
 *
 *     @Override
 *     public List<Integer> sqlTypes() {
 *         return List.of(Types.OTHER);  // PostgreSQL JSON/JSONB
 *     }
 *
 *     @Override
 *     public JsonNode read(ResultSet rs, int columnIndex) throws SQLException {
 *         String json = rs.getString(columnIndex);
 *         return json != null ? parseJson(json) : null;
 *     }
 *
 *     @Override
 *     public void write(PreparedStatement ps, int parameterIndex, JsonNode value)
 *         throws SQLException {
 *         ps.setObject(parameterIndex, value.toString(), Types.OTHER);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the Java type this handler produces and consumes
 * @see java.util.ServiceLoader
 */
public interface TypeHandler<T> {

  /**
   * Returns the Java type this handler produces.
   *
   * @return the Java class for the handled type
   */
  Class<T> javaType();

  /**
   * Returns the SQL type codes this handler supports.
   *
   * <p>These values correspond to constants in {@link java.sql.Types}. A handler may support
   * multiple SQL types (e.g., both BLOB and BINARY).
   *
   * @return list of SQL type codes
   */
  List<Integer> sqlTypes();

  /**
   * Returns the database product names this handler is specialized for.
   *
   * <p>Return an empty list if this handler is database-agnostic. For database-specific handlers,
   * return product names as returned by {@code DatabaseMetaData.getDatabaseProductName()}.
   *
   * <p>Examples: "PostgreSQL", "MySQL", "Oracle", "H2"
   *
   * @return list of supported database product names, or empty for all databases
   */
  default List<String> supportedDatabases() {
    return List.of();
  }

  /**
   * Returns the priority of this handler.
   *
   * <p>When multiple handlers support the same SQL type, the handler with the highest priority is
   * selected. Database-specific handlers typically have higher priority than generic handlers.
   *
   * <p>Default priority is 0. Database-specific handlers should use positive values.
   *
   * @return the handler priority (higher values take precedence)
   */
  default int priority() {
    return 0;
  }

  /**
   * Reads a value from the result set.
   *
   * @param resultSet the result set positioned at the current row
   * @param columnIndex the column index (1-based)
   * @return the read value, or null if the database value is null
   * @throws SQLException if reading fails
   */
  T read(ResultSet resultSet, int columnIndex) throws SQLException;

  /**
   * Writes a value to a prepared statement.
   *
   * @param preparedStatement the prepared statement
   * @param parameterIndex the parameter index (1-based)
   * @param value the value to write (never null)
   * @throws SQLException if writing fails
   */
  void write(PreparedStatement preparedStatement, int parameterIndex, T value) throws SQLException;

  /**
   * Formats a value as a string for export (CSV, JSON, etc.).
   *
   * <p>The returned string should be parseable by {@link #parse(String)}.
   *
   * @param value the value to format (never null)
   * @return the string representation
   */
  String format(T value);

  /**
   * Parses a string value from import (CSV, JSON, etc.).
   *
   * @param value the string value to parse (never null or empty)
   * @return the parsed value
   * @throws IllegalArgumentException if parsing fails
   */
  T parse(String value);
}
