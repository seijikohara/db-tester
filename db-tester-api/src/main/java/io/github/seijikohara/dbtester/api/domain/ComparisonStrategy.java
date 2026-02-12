package io.github.seijikohara.dbtester.api.domain;

import java.util.Objects;
import java.util.Optional;
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
 *   <li>{@link #DATE_FLEXIBLE} - Flexible date comparison (handles multiple date formats)
 *   <li>{@link #JSON_EQUIVALENT} - JSON structural comparison (ignores key order and whitespace)
 *   <li>{@link #NOT_NULL} - Only verify the value is not null
 *   <li>{@link #regex(String)} - Match against a regular expression pattern
 * </ul>
 *
 * @see Column
 */
public final class ComparisonStrategy {

  /** Exact match using equals(). This is the default strategy. */
  public static final ComparisonStrategy STRICT = new ComparisonStrategy(Type.STRICT, null, null);

  /** Skip comparison entirely. Useful for auto-generated columns. */
  public static final ComparisonStrategy IGNORE = new ComparisonStrategy(Type.IGNORE, null, null);

  /** Type-aware numeric comparison. Handles Integer vs Long, precision differences, etc. */
  public static final ComparisonStrategy NUMERIC = new ComparisonStrategy(Type.NUMERIC, null, null);

  /** Case-insensitive string comparison. */
  public static final ComparisonStrategy CASE_INSENSITIVE =
      new ComparisonStrategy(Type.CASE_INSENSITIVE, null, null);

  /**
   * Flexible timestamp comparison. Converts timestamps to UTC and ignores sub-second precision.
   *
   * <p>This strategy properly handles timezone differences by converting all timestamps to UTC
   * before comparison. For example, "2024-01-15T10:30:00+09:00" and "2024-01-15T01:30:00Z" are
   * considered equal because they represent the same instant in time.
   */
  public static final ComparisonStrategy TIMESTAMP_FLEXIBLE =
      new ComparisonStrategy(Type.TIMESTAMP_FLEXIBLE, null, null);

  /**
   * Flexible date comparison. Handles multiple date formats.
   *
   * <p>This strategy parses date strings in various formats (ISO-8601, SQL date, slashed formats)
   * and compares the resulting dates. For example, "2024-01-15" and "2024/01/15" are considered
   * equal.
   */
  public static final ComparisonStrategy DATE_FLEXIBLE =
      new ComparisonStrategy(Type.DATE_FLEXIBLE, null, null);

  /**
   * JSON structural equivalence comparison.
   *
   * <p>This strategy compares JSON values by their structure rather than their string
   * representation. Key order in objects is ignored, and insignificant whitespace differences are
   * normalized. For example, {@code {"b":2,"a":1}} and {@code {"a": 1, "b": 2}} are considered
   * equal.
   */
  public static final ComparisonStrategy JSON_EQUIVALENT =
      new ComparisonStrategy(Type.JSON_EQUIVALENT, null, null);

  /** Only verify the value is not null. Useful for auto-generated values. */
  public static final ComparisonStrategy NOT_NULL =
      new ComparisonStrategy(Type.NOT_NULL, null, null);

  /** The strategy type. */
  private final Type type;

  /** The regex pattern for REGEX type, null otherwise. */
  private final @Nullable Pattern pattern;

  /** The strategy-specific options string, null when not applicable. */
  private final @Nullable String options;

  /**
   * Creates a comparison strategy.
   *
   * @param type the strategy type
   * @param pattern the regex pattern (for REGEX type only)
   * @param options the strategy-specific options string
   */
  private ComparisonStrategy(
      final Type type, final @Nullable Pattern pattern, final @Nullable String options) {
    this.type = type;
    this.pattern = pattern;
    this.options = options;
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
    return new ComparisonStrategy(Type.REGEX, Pattern.compile(regex), null);
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
   * @return the pattern, or empty if not a REGEX strategy
   */
  public Optional<Pattern> getPattern() {
    return Optional.ofNullable(pattern);
  }

  /**
   * Returns the strategy-specific options string.
   *
   * @return the options, or empty if not applicable
   */
  public Optional<String> getOptions() {
    return Optional.ofNullable(options);
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
    return thisPattern.equals(otherPattern) && Objects.equals(options, other.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        type, Optional.ofNullable(pattern).map(Pattern::pattern).orElse(null), options);
  }

  @Override
  public String toString() {
    if (pattern != null && type == Type.REGEX) {
      return String.format("ComparisonStrategy[REGEX:%s]", pattern.pattern());
    }
    if (options != null) {
      return String.format("ComparisonStrategy[%s:%s]", type, options);
    }
    return String.format("ComparisonStrategy[%s]", type);
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

    /** Flexible date comparison. */
    DATE_FLEXIBLE,

    /** JSON structural equivalence comparison. */
    JSON_EQUIVALENT,

    /** Only verify the value is not null. */
    NOT_NULL,

    /** Match against a regular expression. */
    REGEX
  }
}
