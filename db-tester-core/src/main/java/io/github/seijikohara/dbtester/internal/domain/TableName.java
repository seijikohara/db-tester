package io.github.seijikohara.dbtester.internal.domain;

/**
 * Type-safe wrapper for a database table name.
 *
 * <p>All table names are validated, trimmed, and guaranteed to be non-blank.
 *
 * @param value the table name (non-null, non-blank after trimming)
 */
public record TableName(String value) implements StringIdentifier<TableName> {

  /** Validates and normalizes the table name. */
  public TableName {
    value = validateNonBlankString(value, "Table name");
  }
}
