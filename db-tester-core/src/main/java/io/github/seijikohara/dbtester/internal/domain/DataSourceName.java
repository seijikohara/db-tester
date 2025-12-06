package io.github.seijikohara.dbtester.internal.domain;

import javax.sql.DataSource;

/**
 * Value object that identifies a registered {@link DataSource}.
 *
 * @param value canonical data source identifier
 */
public record DataSourceName(String value) implements StringIdentifier<DataSourceName> {

  /**
   * Validates and normalizes the data source name.
   *
   * <p>The name is trimmed and validated to ensure it is non-blank.
   *
   * @param value the raw data source name
   * @throws IllegalArgumentException if value is blank after trimming
   */
  public DataSourceName {
    value = validateNonBlankString(value, "Data source name");
  }
}
