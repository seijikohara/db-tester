package io.github.seijikohara.dbtester.api.annotation;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;

/**
 * Defines the comparison strategy types for use in annotations.
 *
 * <p>This enum mirrors {@link ComparisonStrategy.Type} but is designed for use in annotation
 * attributes where class instances cannot be used directly. Each value corresponds to a predefined
 * {@link ComparisonStrategy} constant.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ExpectedDataSet(dataSets = @DataSetSource(
 *     columnStrategies = {
 *         @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
 *         @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE)
 *     }
 * ))
 * void testMethod() { }
 * }</pre>
 *
 * @see ColumnStrategy
 * @see ComparisonStrategy
 */
public enum Strategy {

  /**
   * Exact match using equals().
   *
   * <p>This is the default strategy. Values must be exactly equal for comparison to succeed.
   *
   * @see ComparisonStrategy#STRICT
   */
  STRICT,

  /**
   * Skip comparison entirely.
   *
   * <p>Useful for auto-generated columns (timestamps, version numbers, auto-increment IDs) that
   * cannot be predicted in test data. The column will be ignored during verification.
   *
   * @see ComparisonStrategy#IGNORE
   */
  IGNORE,

  /**
   * Type-aware numeric comparison.
   *
   * <p>Handles differences between numeric types (Integer vs Long, BigDecimal precision
   * differences). Values are compared by their numeric value rather than exact type match.
   *
   * @see ComparisonStrategy#NUMERIC
   */
  NUMERIC,

  /**
   * Case-insensitive string comparison.
   *
   * <p>Compares string values ignoring case differences. Useful for case-normalized data where the
   * database may store values in a different case than the test data.
   *
   * @see ComparisonStrategy#CASE_INSENSITIVE
   */
  CASE_INSENSITIVE,

  /**
   * Flexible timestamp comparison.
   *
   * <p>Converts timestamps to UTC and ignores sub-second precision. Properly handles timezone
   * differences by converting all timestamps to UTC before comparison. For example,
   * "2024-01-15T10:30:00+09:00" and "2024-01-15T01:30:00Z" are considered equal.
   *
   * @see ComparisonStrategy#TIMESTAMP_FLEXIBLE
   */
  TIMESTAMP_FLEXIBLE,

  /**
   * Only verify the value is not null.
   *
   * <p>Useful for auto-generated values where you want to verify a value exists but cannot predict
   * its exact value. The comparison succeeds if the actual value is not null.
   *
   * @see ComparisonStrategy#NOT_NULL
   */
  NOT_NULL,

  /**
   * Match against a regular expression pattern.
   *
   * <p>The actual value must match the regex pattern specified in {@link ColumnStrategy#pattern()}.
   * Useful for validating format patterns like UUIDs, email addresses, or timestamps.
   *
   * @see ComparisonStrategy#regex(String)
   */
  REGEX;

  /**
   * Converts this annotation strategy to the corresponding runtime {@link ComparisonStrategy}.
   *
   * <p>For {@link #REGEX} strategy, use {@link #toComparisonStrategy(String)} instead to provide
   * the pattern.
   *
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalStateException if called on REGEX without a pattern (use the overloaded method)
   */
  public ComparisonStrategy toComparisonStrategy() {
    return switch (this) {
      case STRICT -> ComparisonStrategy.STRICT;
      case IGNORE -> ComparisonStrategy.IGNORE;
      case NUMERIC -> ComparisonStrategy.NUMERIC;
      case CASE_INSENSITIVE -> ComparisonStrategy.CASE_INSENSITIVE;
      case TIMESTAMP_FLEXIBLE -> ComparisonStrategy.TIMESTAMP_FLEXIBLE;
      case NOT_NULL -> ComparisonStrategy.NOT_NULL;
      case REGEX ->
          throw new IllegalStateException(
              "REGEX strategy requires a pattern. Use toComparisonStrategy(String pattern) instead.");
    };
  }

  /**
   * Converts this annotation strategy to the corresponding runtime {@link ComparisonStrategy} with
   * an optional pattern.
   *
   * <p>The pattern parameter is only used for {@link #REGEX} strategy; for other strategies it is
   * ignored.
   *
   * @param pattern the regex pattern for REGEX strategy, may be empty for non-REGEX strategies
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if REGEX strategy is used with an empty pattern
   */
  public ComparisonStrategy toComparisonStrategy(final String pattern) {
    if (this == REGEX) {
      if (pattern == null || pattern.isEmpty()) {
        throw new IllegalArgumentException("REGEX strategy requires a non-empty pattern");
      }
      return ComparisonStrategy.regex(pattern);
    }
    return toComparisonStrategy();
  }
}
