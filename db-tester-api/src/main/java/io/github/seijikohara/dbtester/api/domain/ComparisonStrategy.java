package io.github.seijikohara.dbtester.api.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Defines how column values are compared during assertion.
 *
 * <p>This class is the runtime value object that pairs a {@link Strategy} with an optional compiled
 * regex pattern. It provides fine-grained control over how expected and actual values are matched.
 *
 * <p>Available strategies:
 *
 * <ul>
 *   <li>{@link #STRICT} - Exact match using equals() (default)
 *   <li>{@link #IGNORE} - Skip comparison entirely
 *   <li>{@link #NUMERIC} - Type-aware numeric comparison (handles precision differences)
 *   <li>{@link #CASE_INSENSITIVE} - Case-insensitive string comparison
 *   <li>{@link #TIMESTAMP_FLEXIBLE} - Flexible timestamp comparison (ignores sub-second precision)
 *   <li>{@link #DATE_FLEXIBLE} - Flexible date comparison (handles multiple date formats)
 *   <li>{@link #JSON_EQUIVALENT} - JSON structural comparison (ignores key order and whitespace)
 *   <li>{@link #NOT_NULL} - Only verify the value is not null
 *   <li>{@link #regex(String)} - Match against a regular expression pattern
 * </ul>
 *
 * @see Strategy
 */
public final class ComparisonStrategy {

  /** Exact match using equals(). This is the default strategy. */
  public static final ComparisonStrategy STRICT = new ComparisonStrategy(Strategy.STRICT, null);

  /** Skip comparison entirely. Useful for auto-generated columns. */
  public static final ComparisonStrategy IGNORE = new ComparisonStrategy(Strategy.IGNORE, null);

  /** Type-aware numeric comparison. Handles Integer vs Long, precision differences, etc. */
  public static final ComparisonStrategy NUMERIC = new ComparisonStrategy(Strategy.NUMERIC, null);

  /** Case-insensitive string comparison. */
  public static final ComparisonStrategy CASE_INSENSITIVE =
      new ComparisonStrategy(Strategy.CASE_INSENSITIVE, null);

  /**
   * Flexible timestamp comparison. Converts timestamps to UTC and ignores sub-second precision.
   *
   * <p>This strategy properly handles timezone differences by converting all timestamps to UTC
   * before comparison. For example, "2024-01-15T10:30:00+09:00" and "2024-01-15T01:30:00Z" are
   * considered equal because they represent the same instant in time.
   */
  public static final ComparisonStrategy TIMESTAMP_FLEXIBLE =
      new ComparisonStrategy(Strategy.TIMESTAMP_FLEXIBLE, null);

  /**
   * Flexible date comparison. Handles multiple date formats.
   *
   * <p>This strategy parses date strings in various formats (ISO-8601, SQL date, slashed formats)
   * and compares the resulting dates. For example, "2024-01-15" and "2024/01/15" are considered
   * equal.
   */
  public static final ComparisonStrategy DATE_FLEXIBLE =
      new ComparisonStrategy(Strategy.DATE_FLEXIBLE, null);

  /**
   * JSON structural equivalence comparison.
   *
   * <p>This strategy compares JSON values by their structure rather than their string
   * representation. Key order in objects is ignored, and insignificant whitespace differences are
   * normalized. For example, {@code {"b":2,"a":1}} and {@code {"a": 1, "b": 2}} are considered
   * equal.
   */
  public static final ComparisonStrategy JSON_EQUIVALENT =
      new ComparisonStrategy(Strategy.JSON_EQUIVALENT, null);

  /** Only verify the value is not null. Useful for auto-generated values. */
  public static final ComparisonStrategy NOT_NULL = new ComparisonStrategy(Strategy.NOT_NULL, null);

  /** The strategy type. */
  private final Strategy type;

  /** The regex pattern for REGEX type, null otherwise. */
  private final @Nullable Pattern pattern;

  /**
   * Creates a comparison strategy.
   *
   * @param type the strategy type
   * @param pattern the regex pattern (for REGEX type only)
   */
  private ComparisonStrategy(final Strategy type, final @Nullable Pattern pattern) {
    this.type = type;
    this.pattern = pattern;
  }

  /**
   * Returns the comparison strategy for the specified type.
   *
   * <p>For {@link Strategy#REGEX}, use {@link #of(Strategy, String)} or {@link #regex(String)}
   * instead, because REGEX requires a pattern.
   *
   * @param type the strategy type
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if type is {@link Strategy#REGEX}
   */
  public static ComparisonStrategy of(final Strategy type) {
    return of(type, null);
  }

  /**
   * Returns the comparison strategy for the specified type and optional pattern.
   *
   * <p>The pattern is required for {@link Strategy#REGEX} and ignored for all other types.
   *
   * @param type the strategy type
   * @param pattern the regex pattern for REGEX, may be null or empty for other types
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if type is {@link Strategy#REGEX} and pattern is null or blank
   */
  public static ComparisonStrategy of(final Strategy type, final @Nullable String pattern) {
    return switch (type) {
      case STRICT -> STRICT;
      case IGNORE -> IGNORE;
      case NUMERIC -> NUMERIC;
      case CASE_INSENSITIVE -> CASE_INSENSITIVE;
      case TIMESTAMP_FLEXIBLE -> TIMESTAMP_FLEXIBLE;
      case DATE_FLEXIBLE -> DATE_FLEXIBLE;
      case JSON_EQUIVALENT -> JSON_EQUIVALENT;
      case NOT_NULL -> NOT_NULL;
      case REGEX -> {
        if (pattern == null || pattern.isBlank()) {
          throw new IllegalArgumentException("REGEX strategy requires a non-empty pattern");
        }
        yield regex(pattern);
      }
    };
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
    return new ComparisonStrategy(Strategy.REGEX, Pattern.compile(regex));
  }

  /**
   * Returns the strategy type.
   *
   * @return the type
   */
  public Strategy type() {
    return type;
  }

  /**
   * Returns the regex pattern for REGEX strategies.
   *
   * @return the pattern, or empty if not a REGEX strategy
   */
  public Optional<Pattern> pattern() {
    return Optional.ofNullable(pattern);
  }

  /**
   * Checks if this strategy ignores comparison.
   *
   * @return {@code true} if this is an IGNORE strategy, {@code false} otherwise
   */
  public boolean isIgnore() {
    return type == Strategy.IGNORE;
  }

  /**
   * Checks if this is a strict (exact match) strategy.
   *
   * @return {@code true} if this is a STRICT strategy, {@code false} otherwise
   */
  public boolean isStrict() {
    return type == Strategy.STRICT;
  }

  // ========== Object methods ==========

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
    if (pattern != null && type == Strategy.REGEX) {
      return String.format("ComparisonStrategy[REGEX:%s]", pattern.pattern());
    }
    return String.format("ComparisonStrategy[%s]", type);
  }
}
