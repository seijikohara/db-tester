package io.github.seijikohara.dbtester.internal.domain;

/**
 * Type-safe representation of a column identifier.
 *
 * <p>Provides compile-time type safety for column names in database operations. All instances are
 * guaranteed to have non-blank, trimmed string values.
 *
 * <p>Implements {@link StringIdentifier} to ensure natural ordering and consistent validation
 * across all string-based domain identifiers.
 *
 * @param value canonical column identifier (trimmed, non-blank)
 */
public record ColumnName(String value) implements StringIdentifier<ColumnName> {

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
}
