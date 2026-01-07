package io.github.seijikohara.dbtester.internal.jdbc;

import java.util.regex.Pattern;

/**
 * Validates and quotes SQL identifiers to prevent SQL injection.
 *
 * <p>This utility class provides methods to validate table names and column names before
 * interpolating them into SQL statements. While the framework primarily receives identifiers from
 * trusted test code, validation prevents accidental SQL injection from malformed inputs.
 *
 * <p>The class supports two approaches:
 *
 * <ul>
 *   <li><strong>Validation</strong>: Ensures identifiers match a safe pattern (alphanumeric and
 *       underscore only)
 *   <li><strong>Quoting</strong>: Escapes identifiers using standard SQL double-quote syntax
 * </ul>
 *
 * <p>This class is stateless and thread-safe.
 */
public final class SqlIdentifier {

  /**
   * Pattern for valid SQL identifiers.
   *
   * <p>Accepts identifiers that:
   *
   * <ul>
   *   <li>Start with a letter (a-z, A-Z) or underscore
   *   <li>Contain only letters, digits (0-9), and underscores
   *   <li>Optionally include a schema prefix (schema.table)
   * </ul>
   */
  private static final Pattern VALID_IDENTIFIER =
      Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)?$");

  /** Prevents instantiation of this utility class. */
  private SqlIdentifier() {
    // Utility class - prevent instantiation
  }

  /**
   * Validates that the identifier matches the safe pattern.
   *
   * <p>Valid identifiers:
   *
   * <ul>
   *   <li>{@code USERS}
   *   <li>{@code user_accounts}
   *   <li>{@code _temp_table}
   *   <li>{@code schema.table}
   * </ul>
   *
   * <p>Invalid identifiers:
   *
   * <ul>
   *   <li>{@code 123table} (starts with digit)
   *   <li>{@code user-accounts} (contains hyphen)
   *   <li>{@code "; DROP TABLE users; --"} (SQL injection attempt)
   * </ul>
   *
   * @param identifier the identifier to validate
   * @return the validated identifier (unchanged)
   * @throws IllegalArgumentException if the identifier is null, empty, or contains invalid
   *     characters
   */
  public static String validate(final String identifier) {
    if (identifier == null || identifier.isEmpty()) {
      throw new IllegalArgumentException("SQL identifier must not be null or empty");
    }
    if (!VALID_IDENTIFIER.matcher(identifier).matches()) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid SQL identifier: '%s'. "
                  + "Identifiers must start with a letter or underscore and contain only "
                  + "letters, digits, and underscores.",
              identifier));
    }
    return identifier;
  }

  /**
   * Quotes an identifier using standard SQL double-quote syntax.
   *
   * <p>This method escapes any embedded double quotes by doubling them, following SQL standard
   * escaping rules. The resulting identifier is safe for interpolation into SQL statements.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code quote("USERS")} returns {@code "\"USERS\""}
   *   <li>{@code quote("user name")} returns {@code "\"user name\""}
   *   <li>{@code quote("table\"name")} returns {@code "\"table\"\"name\""}
   * </ul>
   *
   * @param identifier the identifier to quote
   * @return the quoted identifier
   * @throws IllegalArgumentException if the identifier is null or empty
   */
  public static String quote(final String identifier) {
    if (identifier == null || identifier.isEmpty()) {
      throw new IllegalArgumentException("SQL identifier must not be null or empty");
    }
    // Escape embedded double quotes by doubling them
    final var escaped = identifier.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }

  /**
   * Validates and quotes an identifier for safe SQL interpolation.
   *
   * <p>This method combines validation and quoting for defense in depth:
   *
   * <ol>
   *   <li>Validates the identifier matches the safe pattern
   *   <li>Quotes the identifier for additional safety
   * </ol>
   *
   * @param identifier the identifier to process
   * @return the validated and quoted identifier
   * @throws IllegalArgumentException if the identifier is invalid
   */
  public static String validateAndQuote(final String identifier) {
    validate(identifier);
    return quote(identifier);
  }
}
