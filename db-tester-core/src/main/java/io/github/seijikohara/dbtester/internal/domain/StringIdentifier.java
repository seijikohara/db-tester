package io.github.seijikohara.dbtester.internal.domain;

/**
 * Sealed interface for string-based domain identifiers that support natural ordering.
 *
 * <p>This interface provides a common contract for domain identifiers that are backed by a
 * non-null, non-blank string value, are immutable and type-safe, support natural ordering through
 * {@link Comparable} implementation, and provide validation logic for consistent error handling.
 *
 * <p>Only specific domain identifier records are permitted to implement this interface, ensuring
 * type safety and preventing uncontrolled extension.
 *
 * <p>This interface uses the Curiously Recurring Template Pattern (F-bounded polymorphism) to
 * ensure type-safe comparisons. Each implementing record declares itself as the type parameter.
 *
 * <p>The {@link #validateNonBlankString(String, String)} method provides consistent validation
 * logic that all implementing records should use in their compact constructors.
 *
 * <p>All identifiers are compared lexicographically by their string {@link #value()}. Type-safe
 * comparisons are enforced at compile time.
 *
 * @param <T> the concrete identifier type implementing this interface (self-type)
 */
public sealed interface StringIdentifier<T extends StringIdentifier<T>> extends Comparable<T>
    permits ColumnName, DataSourceName, ScenarioMarker, SchemaName, ScenarioName, TableName {

  /**
   * Returns the string value of this identifier.
   *
   * <p>The value is guaranteed to be:
   *
   * <ul>
   *   <li>Non-null
   *   <li>Non-blank (not empty after trimming)
   *   <li>Trimmed (no leading or trailing whitespace)
   * </ul>
   *
   * @return the string value of this identifier
   */
  String value();

  /**
   * Compares this identifier with another for natural ordering.
   *
   * <p>Identifiers are ordered lexicographically by their string {@link #value()}. This default
   * implementation is inherited by all implementing records, eliminating the need for each record
   * to provide its own {@code compareTo()} implementation.
   *
   * @param other the other identifier to compare to
   * @return negative if this &lt; other, zero if equal, positive if this &gt; other
   */
  @Override
  default int compareTo(final T other) {
    return this.value().compareTo(other.value());
  }

  /**
   * Validates and normalizes a non-null, non-blank string identifier.
   *
   * <p>Performs two validation steps: trimming to remove leading and trailing whitespace, followed
   * by a blank check that throws IllegalArgumentException if the trimmed value is blank.
   *
   * <p>All implementing records should call this method in their compact constructor to ensure
   * consistent validation.
   *
   * @param value the raw string value to validate
   * @param paramName the parameter name used in error messages
   * @return the trimmed, validated string value
   * @throws IllegalArgumentException if value is blank after trimming
   */
  default String validateNonBlankString(final String value, final String paramName) {
    final var trimmed = value.trim();
    if (trimmed.isBlank()) {
      throw new IllegalArgumentException(String.format("%s must not be blank", paramName));
    }
    return trimmed;
  }
}
