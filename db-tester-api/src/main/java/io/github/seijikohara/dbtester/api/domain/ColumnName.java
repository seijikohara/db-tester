package io.github.seijikohara.dbtester.api.domain;

/**
 * Type-safe representation of a column identifier.
 *
 * <p>Provides compile-time type safety for column names in database operations. All instances are
 * guaranteed to have non-blank, trimmed string values.
 *
 * <p>For richer column modeling including metadata and comparison strategies, use {@link Column}.
 *
 * @param value canonical column identifier (trimmed, non-blank)
 * @see Column
 */
public record ColumnName(String value) implements Comparable<ColumnName> {

  /**
   * Compact constructor that validates and normalizes the column name.
   *
   * <p>Trims whitespace and rejects blank identifiers to ensure all instances contain valid,
   * normalized column names.
   *
   * @throws IllegalArgumentException if value is blank after trimming
   */
  public ColumnName {
    value = validateNonBlankString(value, "Column name");
  }

  /**
   * Converts this ColumnName to a rich Column with default settings.
   *
   * <p>The returned Column has no metadata and uses STRICT comparison strategy.
   *
   * @return a new Column wrapping this name
   */
  public Column toColumn() {
    return Column.of(this);
  }

  /**
   * Creates a Column builder from this name.
   *
   * <p>Use this to create a Column with custom metadata and comparison strategy.
   *
   * @return a new Column.Builder for this column name
   */
  public Column.Builder toColumnBuilder() {
    return Column.builder(this);
  }

  @Override
  public int compareTo(final ColumnName other) {
    return this.value.compareTo(other.value());
  }

  /**
   * Validates that the value is non-blank after trimming.
   *
   * @param value the value to validate
   * @param paramName the parameter name for error messages
   * @return the trimmed value
   * @throws IllegalArgumentException if value is blank after trimming
   */
  private static String validateNonBlankString(final String value, final String paramName) {
    final var trimmed = value.trim();
    if (trimmed.isBlank()) {
      throw new IllegalArgumentException(String.format("%s must not be blank", paramName));
    }
    return trimmed;
  }
}
