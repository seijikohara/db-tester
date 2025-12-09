package io.github.seijikohara.dbtester.api.domain;

/**
 * Type-safe wrapper for a database table name.
 *
 * <p>All table names are validated, trimmed, and guaranteed to be non-blank.
 *
 * @param value the table name (non-null, non-blank after trimming)
 */
public record TableName(String value) implements Comparable<TableName> {

  /**
   * Validates and normalizes the table name.
   *
   * @throws IllegalArgumentException if value is blank after trimming
   */
  public TableName {
    value = validateNonBlankString(value, "Table name");
  }

  @Override
  public int compareTo(final TableName other) {
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
