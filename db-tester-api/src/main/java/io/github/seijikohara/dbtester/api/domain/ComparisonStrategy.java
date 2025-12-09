package io.github.seijikohara.dbtester.api.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Defines how column values should be compared during assertion.
 *
 * <p>This class provides various comparison strategies for database testing, allowing fine-grained
 * control over how expected and actual values are matched.
 *
 * <p>Available strategies:
 *
 * <ul>
 *   <li>{@link #STRICT} - Exact match using equals() (default)
 *   <li>{@link #IGNORE} - Skip comparison entirely
 *   <li>{@link #NUMERIC} - Type-aware numeric comparison (handles precision differences)
 *   <li>{@link #CASE_INSENSITIVE} - Case-insensitive string comparison
 *   <li>{@link #TIMESTAMP_FLEXIBLE} - Flexible timestamp comparison (ignores sub-second precision)
 *   <li>{@link #NOT_NULL} - Only verify the value is not null
 *   <li>{@link #regex(String)} - Match against a regular expression pattern
 * </ul>
 *
 * @see Column
 */
public final class ComparisonStrategy {

  /** Exact match using equals(). This is the default strategy. */
  public static final ComparisonStrategy STRICT = new ComparisonStrategy(Type.STRICT, null);

  /** Skip comparison entirely. Useful for auto-generated columns. */
  public static final ComparisonStrategy IGNORE = new ComparisonStrategy(Type.IGNORE, null);

  /** Type-aware numeric comparison. Handles Integer vs Long, precision differences, etc. */
  public static final ComparisonStrategy NUMERIC = new ComparisonStrategy(Type.NUMERIC, null);

  /** Case-insensitive string comparison. */
  public static final ComparisonStrategy CASE_INSENSITIVE =
      new ComparisonStrategy(Type.CASE_INSENSITIVE, null);

  /** Flexible timestamp comparison. Ignores sub-second precision and timezone differences. */
  public static final ComparisonStrategy TIMESTAMP_FLEXIBLE =
      new ComparisonStrategy(Type.TIMESTAMP_FLEXIBLE, null);

  /** Only verify the value is not null. Useful for auto-generated values. */
  public static final ComparisonStrategy NOT_NULL = new ComparisonStrategy(Type.NOT_NULL, null);

  /** The strategy type. */
  private final Type type;

  /** The regex pattern for REGEX type, null otherwise. */
  private final @Nullable Pattern pattern;

  /**
   * Creates a comparison strategy.
   *
   * @param type the strategy type
   * @param pattern the regex pattern (for REGEX type only)
   */
  private ComparisonStrategy(final Type type, final @Nullable Pattern pattern) {
    this.type = type;
    this.pattern = pattern;
  }

  /**
   * Creates a regex comparison strategy.
   *
   * <p>The actual value must match the provided regex pattern for comparison to succeed.
   *
   * @param regex the regex pattern string
   * @return a new REGEX comparison strategy
   * @throws java.util.regex.PatternSyntaxException if the regex is invalid
   */
  public static ComparisonStrategy regex(final String regex) {
    return new ComparisonStrategy(Type.REGEX, Pattern.compile(regex));
  }

  /**
   * Returns the strategy type.
   *
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the regex pattern for REGEX strategies.
   *
   * @return the pattern, or null if not a REGEX strategy
   */
  public @Nullable Pattern getPattern() {
    return pattern;
  }

  /**
   * Checks if this strategy ignores comparison.
   *
   * @return {@code true} if this is an IGNORE strategy, {@code false} otherwise
   */
  public boolean isIgnore() {
    return type == Type.IGNORE;
  }

  /**
   * Checks if this is a strict (exact match) strategy.
   *
   * @return {@code true} if this is a STRICT strategy, {@code false} otherwise
   */
  public boolean isStrict() {
    return type == Type.STRICT;
  }

  /**
   * Compares two values according to this strategy.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if the values match according to this strategy, {@code false} otherwise
   */
  public boolean matches(final @Nullable Object expected, final @Nullable Object actual) {
    return switch (type) {
      case STRICT -> Objects.equals(expected, actual);
      case IGNORE -> true;
      case NUMERIC -> compareNumeric(expected, actual);
      case CASE_INSENSITIVE -> compareCaseInsensitive(expected, actual);
      case TIMESTAMP_FLEXIBLE -> compareTimestamp(expected, actual);
      case NOT_NULL -> actual != null;
      case REGEX -> matchesRegex(actual);
    };
  }

  /**
   * Compares two values numerically.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if numerically equal, {@code false} otherwise
   */
  private boolean compareNumeric(final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(expected, actual, this::compareNumericValues);
  }

  /**
   * Compares two non-null values numerically.
   *
   * @param expected the expected value (non-null)
   * @param actual the actual value (non-null)
   * @return {@code true} if numerically equal, {@code false} otherwise
   */
  private boolean compareNumericValues(final Object expected, final Object actual) {
    try {
      return Optional.ofNullable(toNumber(expected))
          .flatMap(
              expNum ->
                  Optional.ofNullable(toNumber(actual))
                      .map(
                          actNum -> {
                            final var expectedDecimal = new BigDecimal(expNum.toString());
                            final var actualDecimal = new BigDecimal(actNum.toString());
                            return expectedDecimal.compareTo(actualDecimal) == 0;
                          }))
          .orElseGet(() -> Objects.equals(expected, actual));
    } catch (final NumberFormatException e) {
      return Objects.equals(expected, actual);
    }
  }

  /**
   * Converts an object to a Number if possible.
   *
   * @param value the value to convert
   * @return the number, or null if not convertible
   */
  private @Nullable Number toNumber(final Object value) {
    if (value instanceof Number num) {
      return num;
    }
    if (value instanceof String str) {
      try {
        return new BigDecimal(str.trim());
      } catch (final NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Compares two values case-insensitively.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if case-insensitively equal, {@code false} otherwise
   */
  private boolean compareCaseInsensitive(
      final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(
        expected, actual, (exp, act) -> exp.toString().equalsIgnoreCase(act.toString()));
  }

  /**
   * Compares two timestamp values with flexible precision.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if timestamps match (ignoring sub-second precision), {@code false}
   *     otherwise
   */
  private boolean compareTimestamp(final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(
        expected,
        actual,
        (exp, act) ->
            normalizeTimestamp(exp.toString()).equals(normalizeTimestamp(act.toString())));
  }

  /**
   * Normalizes a timestamp string by removing sub-second precision.
   *
   * @param timestamp the timestamp string
   * @return normalized timestamp
   */
  private String normalizeTimestamp(final String timestamp) {
    // Remove fractional seconds and timezone info for flexible comparison
    return timestamp
        .replaceAll("\\.\\d+", "") // Remove fractional seconds
        .replaceAll("[+-]\\d{2}:?\\d{2}$", "") // Remove timezone offset
        .replaceAll("Z$", "") // Remove UTC indicator
        .trim();
  }

  /**
   * Matches the actual value against the regex pattern.
   *
   * @param actual the actual value
   * @return {@code true} if the value matches the pattern, {@code false} otherwise
   */
  private boolean matchesRegex(final @Nullable Object actual) {
    return Optional.ofNullable(pattern)
        .flatMap(p -> Optional.ofNullable(actual).map(a -> p.matcher(a.toString()).matches()))
        .orElse(false);
  }

  /**
   * Compares two nullable values using the provided comparator function.
   *
   * <p>Returns {@code true} if both values are null (both absent means equal). Returns {@code
   * false} if exactly one value is null (one absent means not equal). Otherwise, applies the
   * comparator function to the non-null values.
   *
   * @param expected the expected value (nullable)
   * @param actual the actual value (nullable)
   * @param comparator the function to compare non-null values
   * @return {@code true} if the values are considered equal, {@code false} otherwise
   */
  private boolean compareNullable(
      final @Nullable Object expected,
      final @Nullable Object actual,
      final BiFunction<Object, Object, Boolean> comparator) {
    return Optional.ofNullable(expected)
        .map(
            exp -> Optional.ofNullable(actual).map(act -> comparator.apply(exp, act)).orElse(false))
        .orElseGet(() -> actual == null);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ComparisonStrategy other)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    final var thisPattern = Optional.ofNullable(pattern).map(Pattern::pattern);
    final var otherPattern = Optional.ofNullable(other.pattern).map(Pattern::pattern);
    return thisPattern.equals(otherPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, Optional.ofNullable(pattern).map(Pattern::pattern).orElse(null));
  }

  @Override
  public String toString() {
    return Optional.ofNullable(pattern)
        .filter(p -> type == Type.REGEX)
        .map(p -> "ComparisonStrategy[REGEX:" + p.pattern() + "]")
        .orElseGet(() -> "ComparisonStrategy[" + type + "]");
  }

  /** Enum defining the available comparison strategy types. */
  public enum Type {
    /** Exact match using equals(). */
    STRICT,

    /** Skip comparison entirely. */
    IGNORE,

    /** Type-aware numeric comparison. */
    NUMERIC,

    /** Case-insensitive string comparison. */
    CASE_INSENSITIVE,

    /** Flexible timestamp comparison. */
    TIMESTAMP_FLEXIBLE,

    /** Only verify the value is not null. */
    NOT_NULL,

    /** Match against a regular expression. */
    REGEX
  }
}
