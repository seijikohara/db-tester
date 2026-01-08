package io.github.seijikohara.dbtester.api.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Represents a single cell value within a database row, combining column information with its
 * value.
 *
 * <p>This class provides a rich model for individual cell values, including:
 *
 * <ul>
 *   <li>Column reference with metadata and comparison strategy
 *   <li>Actual value wrapped in {@link CellValue}
 *   <li>Comparison operations that respect the column's strategy
 * </ul>
 *
 * @see Column
 * @see CellValue
 */
public final class Cell {

  /** The column this cell belongs to. */
  private final Column column;

  /** The cell value. */
  private final CellValue value;

  /**
   * Creates a new cell.
   *
   * @param column the column
   * @param value the value
   */
  private Cell(final Column column, final CellValue value) {
    this.column = column;
    this.value = value;
  }

  /**
   * Creates a cell with the specified column and value.
   *
   * @param column the column
   * @param value the value
   * @return a new Cell instance
   */
  public static Cell of(final Column column, final CellValue value) {
    return new Cell(column, value);
  }

  /**
   * Creates a cell with a column name and value.
   *
   * @param columnName the column name
   * @param value the value
   * @return a new Cell instance
   */
  public static Cell of(final String columnName, final CellValue value) {
    return new Cell(Column.of(columnName), value);
  }

  /**
   * Creates a cell with a column name and raw value.
   *
   * @param columnName the column name
   * @param rawValue the raw value, or {@code null} for NULL cell
   * @return a new Cell instance
   */
  public static Cell of(final String columnName, final @Nullable Object rawValue) {
    return new Cell(
        Column.of(columnName), rawValue != null ? new CellValue(rawValue) : CellValue.NULL);
  }

  /**
   * Creates a NULL cell for the specified column.
   *
   * @param column the column
   * @return a new Cell with NULL value
   */
  public static Cell nullCell(final Column column) {
    return new Cell(column, CellValue.NULL);
  }

  /**
   * Returns the column this cell belongs to.
   *
   * @return the column
   */
  public Column getColumn() {
    return column;
  }

  /**
   * Returns the column name.
   *
   * <p>Convenience method equivalent to {@code getColumn().getName()}.
   *
   * @return the column name
   */
  public ColumnName getColumnName() {
    return column.getName();
  }

  /**
   * Returns the cell value.
   *
   * @return the value
   */
  public CellValue getValue() {
    return value;
  }

  /**
   * Returns the raw value, or empty if the cell is NULL.
   *
   * @return the raw value or empty
   */
  public Optional<Object> getRawValue() {
    return Optional.ofNullable(value.value());
  }

  /**
   * Checks if this cell contains a NULL value.
   *
   * @return {@code true} if the value is NULL, {@code false} otherwise
   */
  public boolean isNull() {
    return value.isNull();
  }

  /**
   * Checks if this column should be ignored during comparison.
   *
   * @return {@code true} if the column's comparison strategy is IGNORE, {@code false} otherwise
   */
  public boolean shouldIgnore() {
    return column.isIgnored();
  }

  /**
   * Compares this cell's value with another CellValue using the column's comparison strategy.
   *
   * @param other the value to compare with
   * @return {@code true} if the values match according to the column's strategy, {@code false}
   *     otherwise
   */
  public boolean matches(final CellValue other) {
    return column.getComparisonStrategy().matches(value.value(), other.value());
  }

  /**
   * Compares this cell's value with another Cell's value.
   *
   * <p>Uses this cell's column comparison strategy for the comparison.
   *
   * @param other the cell to compare with
   * @return {@code true} if the values match according to this column's strategy, {@code false}
   *     otherwise
   */
  public boolean matches(final Cell other) {
    return matches(other.value);
  }

  /**
   * Returns the value as a String, or empty if the value is NULL.
   *
   * @return the string representation or empty
   */
  public Optional<String> getValueAsString() {
    return Optional.ofNullable(value.value()).map(Object::toString);
  }

  /**
   * Returns the value as a Number, or empty if not applicable.
   *
   * @return the value as Number, or empty
   */
  public Optional<Number> getValueAsNumber() {
    final var rawValue = value.value();
    if (rawValue instanceof Number number) {
      return Optional.of(number);
    }
    if (rawValue instanceof String string) {
      try {
        return Optional.of(new BigDecimal(string.trim()));
      } catch (final NumberFormatException exception) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Cell other)) {
      return false;
    }
    return column.equals(other.column) && Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(column, value);
  }

  @Override
  public String toString() {
    return String.format("Cell[%s=%s]", column.getNameValue(), isNull() ? "NULL" : value.value());
  }
}
