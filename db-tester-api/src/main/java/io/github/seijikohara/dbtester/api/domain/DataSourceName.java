package io.github.seijikohara.dbtester.api.domain;

import javax.sql.DataSource;

/**
 * Value object that identifies a registered {@link DataSource}.
 *
 * <p>All data source names are validated, trimmed, and guaranteed to be non-blank.
 *
 * @param value canonical data source identifier (trimmed, non-blank)
 */
public record DataSourceName(String value) implements Comparable<DataSourceName> {

  /**
   * Validates and normalizes the data source name.
   *
   * <p>The name is trimmed and validated to ensure it is non-blank.
   *
   * @throws IllegalArgumentException if value is blank after trimming
   */
  public DataSourceName {
    value = validateNonBlankString(value, "Data source name");
  }

  @Override
  public int compareTo(final DataSourceName other) {
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
