package io.github.seijikohara.dbtester.internal.jdbc.write;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Binds parameter values to JDBC PreparedStatements with type conversion.
 *
 * <p>This class provides methods to set parameters on PreparedStatements with proper type
 * conversion based on database metadata. It handles string-to-SQL-type conversions for values read
 * from CSV files.
 *
 * <p>Supported conversions include:
 *
 * <ul>
 *   <li>Boolean: "true", "1", "yes", "y" (case-insensitive)
 *   <li>Date: "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss" (date portion only)
 *   <li>Time: "HH:mm:ss" or "HH:mm:ss.SSS"
 *   <li>Timestamp: "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd HH:mm:ss.SSS"
 *   <li>BLOB: Base64-encoded with "[BASE64]" prefix, or UTF-8 text
 *   <li>Numeric: Integer, Long, Float, Double, BigDecimal
 * </ul>
 *
 * <p>This class is stateless and thread-safe.
 */
public final class ParameterBinder {

  /** Prefix for Base64-encoded blob data in CSV files. */
  private static final String BASE64_PREFIX = "[BASE64]";

  /** Creates a new parameter binder. */
  public ParameterBinder() {}

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
    if (dataValue.isNull()) {
      stmt.setNull(index, sqlType);
      return;
    }

    final var value = dataValue.value();
    if (!(value instanceof String strValue)) {
      stmt.setObject(index, value);
      return;
    }

    try {
      switch (sqlType) {
        case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> stmt.setInt(index, parseInt(strValue));
        case Types.BIGINT -> stmt.setLong(index, parseLong(strValue));
        case Types.FLOAT, Types.REAL -> stmt.setFloat(index, parseFloat(strValue));
        case Types.DOUBLE -> stmt.setDouble(index, parseDouble(strValue));
        case Types.DECIMAL, Types.NUMERIC -> stmt.setBigDecimal(index, parseBigDecimal(strValue));
        case Types.BIT, Types.BOOLEAN -> stmt.setBoolean(index, parseBoolean(strValue));
        case Types.DATE -> stmt.setDate(index, parseDate(strValue));
        case Types.TIME, Types.TIME_WITH_TIMEZONE -> stmt.setTime(index, parseTime(strValue));
        case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
            stmt.setTimestamp(index, parseTimestamp(strValue));
        case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB ->
            stmt.setBytes(index, parseBlob(strValue));
        default -> stmt.setObject(index, value);
      }
    } catch (final IllegalArgumentException e) {
      stmt.setObject(index, value);
    }
  }

  /**
   * Extracts column type information from database metadata.
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

  // ========== Value parsing methods (integrated from ValueParser) ==========

  /**
   * Parses a string value to boolean.
   *
   * @param value the string value to parse
   * @return true if value represents true, false otherwise
   */
  private boolean parseBoolean(final String value) {
    final var normalized = value.trim().toLowerCase(Locale.ROOT);
    return "true".equals(normalized)
        || "1".equals(normalized)
        || "yes".equals(normalized)
        || "y".equals(normalized);
  }

  /**
   * Parses a string value to SQL Date.
   *
   * @param value the string value to parse
   * @return the parsed Date object
   */
  private Date parseDate(final String value) {
    final var trimmed = value.trim();
    final var spaceIndex = trimmed.indexOf(' ');
    final var datePart = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
    return Date.valueOf(datePart);
  }

  /**
   * Parses a string value to SQL Time.
   *
   * @param value the string value to parse
   * @return the parsed Time object
   */
  private Time parseTime(final String value) {
    final var trimmed = value.trim();
    final var dotIndex = trimmed.indexOf('.');
    final var timePart = dotIndex > 0 ? trimmed.substring(0, dotIndex) : trimmed;
    final var spaceIndex = timePart.indexOf(' ');
    final var timeOnly = spaceIndex > 0 ? timePart.substring(spaceIndex + 1) : timePart;
    return Time.valueOf(timeOnly);
  }

  /**
   * Parses a string value to SQL Timestamp.
   *
   * @param value the string value to parse
   * @return the parsed Timestamp object
   */
  private Timestamp parseTimestamp(final String value) {
    return Timestamp.valueOf(value.trim());
  }

  /**
   * Parses a string value to byte array (BLOB).
   *
   * @param value the string value to parse
   * @return the parsed byte array
   */
  private byte[] parseBlob(final String value) {
    final var trimmed = value.trim();
    if (trimmed.startsWith(BASE64_PREFIX)) {
      final var base64Content = trimmed.substring(BASE64_PREFIX.length());
      return Base64.getDecoder().decode(base64Content);
    }
    return trimmed.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Parses a string value to int.
   *
   * @param value the string value to parse
   * @return the parsed int
   */
  private int parseInt(final String value) {
    return Integer.parseInt(value.trim());
  }

  /**
   * Parses a string value to long.
   *
   * @param value the string value to parse
   * @return the parsed long
   */
  private long parseLong(final String value) {
    return Long.parseLong(value.trim());
  }

  /**
   * Parses a string value to float.
   *
   * @param value the string value to parse
   * @return the parsed float
   */
  private float parseFloat(final String value) {
    return Float.parseFloat(value.trim());
  }

  /**
   * Parses a string value to double.
   *
   * @param value the string value to parse
   * @return the parsed double
   */
  private double parseDouble(final String value) {
    return Double.parseDouble(value.trim());
  }

  /**
   * Parses a string value to BigDecimal.
   *
   * @param value the string value to parse
   * @return the parsed BigDecimal
   */
  private BigDecimal parseBigDecimal(final String value) {
    return new BigDecimal(value.trim());
  }
}
