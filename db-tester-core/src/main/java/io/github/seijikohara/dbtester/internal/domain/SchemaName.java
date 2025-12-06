package io.github.seijikohara.dbtester.internal.domain;

/**
 * Type-safe representation of a database schema name.
 *
 * @param value canonical schema identifier
 */
public record SchemaName(String value) implements StringIdentifier<SchemaName> {

  /** Validates and normalizes the schema name. */
  public SchemaName {
    value = validateNonBlankString(value, "Schema name");
  }
}
