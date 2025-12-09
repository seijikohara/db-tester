package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Binds parameter values to JDBC PreparedStatements.
 *
 * <p>This class provides methods to set parameters on PreparedStatements with proper type
 * conversion based on database metadata.
 *
 * <p>When binding string values from CSV files, this class uses database metadata to determine the
 * target SQL type and performs appropriate conversions. This is particularly important for
 * databases like PostgreSQL that perform strict type checking.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class ParameterBinder {

  /** The value parser for type conversions. */
  private final ValueParser valueParser;

  /** Creates a new parameter binder with a default value parser. */
  public ParameterBinder() {
    this.valueParser = new ValueParser();
  }

  /**
   * Creates a new parameter binder with the specified value parser.
   *
   * @param valueParser the value parser to use for type conversions
   */
  public ParameterBinder(final ValueParser valueParser) {
    this.valueParser = valueParser;
  }

  /**
   * Sets a single parameter on a prepared statement.
   *
   * <p>This method sets the parameter using setObject, which allows the JDBC driver to determine
   * the appropriate SQL type. Use {@link #bindWithType} for explicit type conversion.
   *
   * @param stmt the prepared statement
   * @param index the parameter index (1-based)
   * @param dataValue the value to set
   * @throws SQLException if a database error occurs
   */
  public void bind(final PreparedStatement stmt, final int index, final CellValue dataValue)
      throws SQLException {
    // JDBC API requires setNull() for null values; isNull() encapsulates the null check
    if (dataValue.isNull()) {
      stmt.setNull(index, Types.NULL);
    } else {
      stmt.setObject(index, dataValue.value());
    }
  }

  /**
   * Sets all parameters for a prepared statement from a row.
   *
   * @param stmt the prepared statement
   * @param row the row containing values
   * @param columns the column names in order
   * @throws SQLException if a database error occurs
   */
  public void bindRow(
      final PreparedStatement stmt, final Row row, final Collection<ColumnName> columns)
      throws SQLException {
    final List<ColumnName> columnList = List.copyOf(columns);
    IntStream.range(0, columnList.size())
        .forEach(
            i -> {
              try {
                bind(stmt, i + 1, row.getValue(columnList.get(i)));
              } catch (final SQLException e) {
                throw new RuntimeException(e);
              }
            });
  }

  /**
   * Sets all parameters for a prepared statement from a row with type conversion.
   *
   * <p>This method uses the column type map to convert string values to appropriate SQL types.
   *
   * @param stmt the prepared statement
   * @param row the row containing values
   * @param columns the column names in order
   * @param columnTypes map of uppercase column names to SQL types
   * @throws SQLException if a database error occurs
   */
  public void bindRowWithTypes(
      final PreparedStatement stmt,
      final Row row,
      final Collection<ColumnName> columns,
      final Map<String, Integer> columnTypes)
      throws SQLException {
    final List<ColumnName> columnList = List.copyOf(columns);
    IntStream.range(0, columnList.size())
        .forEach(
            i -> {
              try {
                final var column = columnList.get(i);
                final var upperName = column.value().toUpperCase(Locale.ROOT);
                final var sqlType = columnTypes.getOrDefault(upperName, Types.VARCHAR);
                bindWithType(stmt, i + 1, row.getValue(column), sqlType);
              } catch (final SQLException e) {
                throw new RuntimeException(e);
              }
            });
  }

  /**
   * Sets a single parameter on a prepared statement with type conversion.
   *
   * <p>This method converts string values from CSV to appropriate database types based on the
   * column's SQL type, which is necessary for databases like PostgreSQL that perform strict type
   * checking.
   *
   * @param stmt the prepared statement
   * @param index the parameter index (1-based)
   * @param dataValue the value to set
   * @param sqlType the SQL type of the column
   * @throws SQLException if a database error occurs
   */
  public void bindWithType(
      final PreparedStatement stmt, final int index, final CellValue dataValue, final int sqlType)
      throws SQLException {
    // JDBC API requires setNull() for null values; isNull() encapsulates the null check
    if (dataValue.isNull()) {
      stmt.setNull(index, sqlType);
      return;
    }

    final var value = dataValue.value();
    // If not a string, use normal setObject
    if (!(value instanceof String strValue)) {
      stmt.setObject(index, value);
      return;
    }

    // Convert string value to appropriate type based on SQL type
    try {
      switch (sqlType) {
        case Types.INTEGER, Types.SMALLINT, Types.TINYINT ->
            stmt.setInt(index, valueParser.parseInt(strValue));
        case Types.BIGINT -> stmt.setLong(index, valueParser.parseLong(strValue));
        case Types.FLOAT, Types.REAL -> stmt.setFloat(index, valueParser.parseFloat(strValue));
        case Types.DOUBLE -> stmt.setDouble(index, valueParser.parseDouble(strValue));
        case Types.DECIMAL, Types.NUMERIC ->
            stmt.setBigDecimal(index, valueParser.parseBigDecimal(strValue));
        case Types.BIT, Types.BOOLEAN -> stmt.setBoolean(index, valueParser.parseBoolean(strValue));
        case Types.DATE -> stmt.setDate(index, valueParser.parseDate(strValue));
        case Types.TIME, Types.TIME_WITH_TIMEZONE ->
            stmt.setTime(index, valueParser.parseTime(strValue));
        case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
            stmt.setTimestamp(index, valueParser.parseTimestamp(strValue));
        case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB ->
            stmt.setBytes(index, valueParser.parseBlob(strValue));
        default -> stmt.setObject(index, value);
      }
    } catch (final IllegalArgumentException e) {
      // If conversion fails (NumberFormatException is a subclass), try setObject as fallback
      stmt.setObject(index, value);
    }
  }

  /**
   * Extracts column type information from database metadata.
   *
   * <p>Column names are stored in uppercase for case-insensitive lookup.
   *
   * @param metaData the result set metadata
   * @return a map of uppercase column names to SQL types
   * @throws SQLException if a database error occurs
   */
  public Map<String, Integer> extractColumnTypes(final ResultSetMetaData metaData)
      throws SQLException {
    final var columnTypes = new HashMap<String, Integer>();
    final int columnCount = metaData.getColumnCount();

    IntStream.rangeClosed(1, columnCount)
        .forEach(
            i -> {
              try {
                final var columnName = metaData.getColumnName(i).toUpperCase(Locale.ROOT);
                final var columnType = metaData.getColumnType(i);
                columnTypes.put(columnName, columnType);
              } catch (final SQLException e) {
                throw new RuntimeException(e);
              }
            });

    return columnTypes;
  }
}
