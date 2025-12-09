package io.github.seijikohara.dbtester.api.domain;

import java.sql.JDBCType;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Immutable metadata describing the schema properties of a database column.
 *
 * <p>This record captures essential column metadata retrieved from database schema information,
 * including type, constraints, and precision details. It can be used for:
 *
 * <ul>
 *   <li>Schema validation between expected and actual database structures
 *   <li>Type-aware value comparison (e.g., numeric precision handling)
 *   <li>Generating appropriate SQL for insert/update operations
 * </ul>
 *
 * @param jdbcType the SQL type of the column, or {@code null} if unknown
 * @param nullable whether the column allows NULL values
 * @param primaryKey whether the column is part of the primary key
 * @param ordinalPosition the 1-based position of the column in the table (0 if unknown)
 * @param precision the numeric precision or character length (0 if not applicable)
 * @param scale the numeric scale (0 if not applicable)
 * @param defaultValue the default value as a string, or {@code null} if none
 * @see Column
 * @see JDBCType
 */
public record ColumnMetadata(
    @Nullable JDBCType jdbcType,
    boolean nullable,
    boolean primaryKey,
    int ordinalPosition,
    int precision,
    int scale,
    @Nullable String defaultValue) {

  /**
   * Creates metadata with minimal information.
   *
   * @param jdbcType the SQL type
   * @param nullable whether NULL is allowed
   * @return a new ColumnMetadata instance
   */
  public static ColumnMetadata of(final JDBCType jdbcType, final boolean nullable) {
    return new ColumnMetadata(jdbcType, nullable, false, 0, 0, 0, null);
  }

  /**
   * Creates metadata for a primary key column.
   *
   * @param jdbcType the SQL type
   * @return a new ColumnMetadata instance
   */
  public static ColumnMetadata primaryKey(final JDBCType jdbcType) {
    return new ColumnMetadata(jdbcType, false, true, 0, 0, 0, null);
  }

  /**
   * Checks if this column has a numeric type.
   *
   * @return {@code true} if the column type is numeric, {@code false} otherwise
   */
  public boolean isNumeric() {
    return Optional.ofNullable(jdbcType)
        .map(
            type ->
                switch (type) {
                  case TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE, DECIMAL, NUMERIC ->
                      true;
                  default -> false;
                })
        .orElse(false);
  }

  /**
   * Checks if this column has a textual type.
   *
   * @return {@code true} if the column type is text-based, {@code false} otherwise
   */
  public boolean isTextual() {
    return Optional.ofNullable(jdbcType)
        .map(
            type ->
                switch (type) {
                  case CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR, CLOB, NCLOB ->
                      true;
                  default -> false;
                })
        .orElse(false);
  }

  /**
   * Checks if this column has a temporal type.
   *
   * @return {@code true} if the column type is date/time related, {@code false} otherwise
   */
  public boolean isTemporal() {
    return Optional.ofNullable(jdbcType)
        .map(
            type ->
                switch (type) {
                  case DATE, TIME, TIMESTAMP, TIME_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE -> true;
                  default -> false;
                })
        .orElse(false);
  }

  /**
   * Checks if this column has a binary type.
   *
   * @return {@code true} if the column type is binary, {@code false} otherwise
   */
  public boolean isBinary() {
    return Optional.ofNullable(jdbcType)
        .map(
            type ->
                switch (type) {
                  case BINARY, VARBINARY, LONGVARBINARY, BLOB -> true;
                  default -> false;
                })
        .orElse(false);
  }

  /**
   * Checks if this column has a boolean type.
   *
   * @return {@code true} if the column type is boolean, {@code false} otherwise
   */
  public boolean isBoolean() {
    return jdbcType == JDBCType.BOOLEAN || jdbcType == JDBCType.BIT;
  }

  /**
   * Checks if this column is an auto-increment or identity column.
   *
   * <p>This is a heuristic based on common patterns. Primary key columns with integer types and no
   * default value are often auto-increment.
   *
   * @return {@code true} if this appears to be an auto-increment column, {@code false} otherwise
   */
  public boolean isLikelyAutoIncrement() {
    return Optional.ofNullable(jdbcType)
        .filter(type -> primaryKey)
        .map(
            type ->
                switch (type) {
                  case TINYINT, SMALLINT, INTEGER, BIGINT -> defaultValue == null;
                  default -> false;
                })
        .orElse(false);
  }
}
