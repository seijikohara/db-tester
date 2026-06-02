package io.github.seijikohara.dbtester.api.domain;

/**
 * Identifies a column comparison strategy.
 *
 * <p>This enum is the single source of truth for the available comparison strategies. It serves
 * three roles: the attribute type of {@link
 * io.github.seijikohara.dbtester.api.annotation.ColumnStrategy#strategy()}, the binding target for
 * Spring Boot {@code db-tester.verification.column-strategies} properties, and the discriminator of
 * the {@link ComparisonStrategy} value object returned by {@link ComparisonStrategy#type()}.
 *
 * <p>Use {@link ComparisonStrategy#of(Strategy)} or {@link ComparisonStrategy#of(Strategy, String)}
 * to obtain the runtime {@link ComparisonStrategy} instance for a strategy.
 *
 * @see ComparisonStrategy
 * @see io.github.seijikohara.dbtester.api.annotation.ColumnStrategy
 */
public enum Strategy {

  /**
   * Exact match using {@code equals()}.
   *
   * <p>This is the default strategy. Values must be exactly equal for comparison to succeed.
   */
  STRICT,

  /**
   * Skip comparison entirely.
   *
   * <p>Useful for auto-generated columns (timestamps, version numbers, auto-increment IDs) that
   * cannot be predicted in test data. The column is ignored during verification.
   */
  IGNORE,

  /**
   * Type-aware numeric comparison.
   *
   * <p>Handles differences between numeric types (Integer vs Long, BigDecimal precision
   * differences). Values are compared by numeric value rather than exact type match.
   */
  NUMERIC,

  /**
   * Case-insensitive string comparison.
   *
   * <p>Compares string values ignoring case differences. Useful for case-normalized data where the
   * database stores values in a different case than the test data.
   */
  CASE_INSENSITIVE,

  /**
   * Flexible timestamp comparison.
   *
   * <p>Converts timestamps to UTC and ignores sub-second precision. Properly handles timezone
   * differences by converting all timestamps to UTC before comparison. For example,
   * "2024-01-15T10:30:00+09:00" and "2024-01-15T01:30:00Z" are considered equal.
   */
  TIMESTAMP_FLEXIBLE,

  /**
   * Flexible date comparison across multiple formats.
   *
   * <p>Parses date values in ISO-8601 (yyyy-MM-dd), slashed (yyyy/MM/dd), and dot (yyyy.MM.dd)
   * formats and compares the resulting dates. Extracts the date portion from timestamp strings when
   * needed. For example, "2024-01-15" and "2024/01/15" are considered equal.
   */
  DATE_FLEXIBLE,

  /**
   * JSON structural equivalence comparison.
   *
   * <p>Compares JSON values by structure, ignoring key order in objects and insignificant
   * whitespace. For example, {@code {"b":2,"a":1}} and {@code {"a":1,"b":2}} are considered equal.
   */
  JSON_EQUIVALENT,

  /**
   * Only verify the value is not null.
   *
   * <p>Useful for auto-generated values where the test verifies a value exists but cannot predict
   * its exact value. The comparison succeeds if the actual value is not null.
   */
  NOT_NULL,

  /**
   * Match against a regular expression pattern.
   *
   * <p>The actual value must match the regex pattern supplied via {@link
   * io.github.seijikohara.dbtester.api.annotation.ColumnStrategy#pattern()} or {@link
   * ComparisonStrategy#regex(String)}. Useful for validating format patterns such as UUIDs, email
   * addresses, or timestamps.
   */
  REGEX
}
