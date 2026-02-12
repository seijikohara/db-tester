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
 * @ExpectedDataSet(sources = @DataSetSource(
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
  REGEX,

  /**
   * Flexible date comparison across multiple formats.
   *
   * <p>Parses date values in ISO-8601 (yyyy-MM-dd), slashed (yyyy/MM/dd), and dot (yyyy.MM.dd)
   * formats and compares the resulting dates. Extracts the date portion from timestamp strings if
   * needed.
   *
   * @see ComparisonStrategy#DATE_FLEXIBLE
   */
  DATE_FLEXIBLE,

  /**
   * JSON structural equivalence comparison.
   *
   * <p>Compares JSON values by their structure, ignoring key order in objects and insignificant
   * whitespace. For example, {@code {"b":2,"a":1}} and {@code {"a":1,"b":2}} are considered equal.
   *
   * @see ComparisonStrategy#JSON_EQUIVALENT
   */
  JSON_EQUIVALENT,

  /**
   * Substring containment verification.
   *
   * <p>Checks whether the actual value contains the expected value as a substring. When {@link
   * ColumnStrategy#options()} is specified, the options value is used as the substring to search
   * for instead of the expected value.
   *
   * @deprecated Use {@link #REGEX} with pattern {@code ".*substring.*"} instead. Removed in 2.0.
   * @see ComparisonStrategy#contains()
   */
  @Deprecated(since = "1.1", forRemoval = true)
  CONTAINS,

  /**
   * Numeric range verification.
   *
   * <p>Checks whether the actual numeric value falls within the range specified in {@link
   * ColumnStrategy#options()}. The options must be in the format {@code "min=N,max=M"} where N and
   * M are numeric values. Both bounds are inclusive.
   *
   * @deprecated Use programmatic assertions for range verification instead. Removed in 2.0.
   * @see ComparisonStrategy#range(String)
   */
  @Deprecated(since = "1.1", forRemoval = true)
  RANGE;

  /**
   * Converts this annotation strategy to the corresponding runtime {@link ComparisonStrategy}.
   *
   * <p>This method handles strategies that require no additional parameters. For strategies
   * requiring a pattern or options, use {@link #toComparisonStrategy(String, String)}.
   *
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalStateException if called on a strategy that requires parameters
   */
  @SuppressWarnings("removal")
  private ComparisonStrategy toComparisonStrategy() {
    return switch (this) {
      case STRICT -> ComparisonStrategy.STRICT;
      case IGNORE -> ComparisonStrategy.IGNORE;
      case NUMERIC -> ComparisonStrategy.NUMERIC;
      case CASE_INSENSITIVE -> ComparisonStrategy.CASE_INSENSITIVE;
      case TIMESTAMP_FLEXIBLE -> ComparisonStrategy.TIMESTAMP_FLEXIBLE;
      case DATE_FLEXIBLE -> ComparisonStrategy.DATE_FLEXIBLE;
      case JSON_EQUIVALENT -> ComparisonStrategy.JSON_EQUIVALENT;
      case NOT_NULL -> ComparisonStrategy.NOT_NULL;
      case CONTAINS -> ComparisonStrategy.contains();
      case REGEX ->
          throw new IllegalStateException(
              "REGEX strategy requires a pattern. "
                  + "Use toComparisonStrategy(String, String) instead.");
      case RANGE ->
          throw new IllegalStateException(
              "RANGE strategy requires options. "
                  + "Use toComparisonStrategy(String, String) instead.");
    };
  }

  /**
   * Converts this annotation strategy to the corresponding runtime {@link ComparisonStrategy} with
   * an optional pattern.
   *
   * <p>The pattern parameter is only used for {@link #REGEX} strategy; for other strategies it is
   * ignored. This method delegates to {@link #toComparisonStrategy(String, String)} with an empty
   * options string.
   *
   * @param pattern the regex pattern for REGEX strategy, may be empty for non-REGEX strategies
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if REGEX strategy is used with an empty pattern
   */
  public ComparisonStrategy toComparisonStrategy(final String pattern) {
    return toComparisonStrategy(pattern, "");
  }

  /**
   * Converts this annotation strategy to the corresponding runtime {@link ComparisonStrategy} with
   * pattern and options parameters.
   *
   * <p>The pattern parameter is used for {@link #REGEX} strategy. The options parameter is used for
   * {@link #CONTAINS} and {@link #RANGE} strategies.
   *
   * @param pattern the regex pattern for REGEX strategy, may be empty for non-REGEX strategies
   * @param options the strategy-specific options, may be empty
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if REGEX strategy is used with an empty pattern, or RANGE
   *     strategy is used with empty or invalid options
   */
  @SuppressWarnings("removal")
  public ComparisonStrategy toComparisonStrategy(final String pattern, final String options) {
    if (this == REGEX) {
      if (pattern == null || pattern.isEmpty()) {
        throw new IllegalArgumentException("REGEX strategy requires a non-empty pattern");
      }
      return ComparisonStrategy.regex(pattern);
    }
    if (this == CONTAINS && options != null && !options.isEmpty()) {
      return ComparisonStrategy.contains(options);
    }
    if (this == RANGE) {
      if (options == null || options.isEmpty()) {
        throw new IllegalArgumentException(
            "RANGE strategy requires options in format 'min=N,max=M'");
      }
      return ComparisonStrategy.range(options);
    }
    return toComparisonStrategy();
  }
}
