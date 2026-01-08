package io.github.seijikohara.dbtester.api.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
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

  /**
   * Flexible timestamp comparison. Converts timestamps to UTC and ignores sub-second precision.
   *
   * <p>This strategy properly handles timezone differences by converting all timestamps to UTC
   * before comparison. For example, "2024-01-15T10:30:00+09:00" and "2024-01-15T01:30:00Z" are
   * considered equal because they represent the same instant in time.
   */
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
   * <p>Converts both timestamps to UTC epoch seconds for comparison, properly handling timezone
   * differences. If timezone information is not present, the timestamp is treated as UTC.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if timestamps represent the same instant (ignoring sub-second precision),
   *     {@code false} otherwise
   */
  private boolean compareTimestamp(final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(
        expected,
        actual,
        (exp, act) -> {
          final var expectedEpoch = parseToEpochSecond(exp.toString());
          final var actualEpoch = parseToEpochSecond(act.toString());
          return expectedEpoch.equals(actualEpoch);
        });
  }

  /**
   * Parses a timestamp string to epoch seconds (UTC).
   *
   * <p>Supports various timestamp formats:
   *
   * <ul>
   *   <li>ISO-8601 with offset: "2024-01-15T10:30:00+09:00"
   *   <li>ISO-8601 with Z: "2024-01-15T10:30:00Z"
   *   <li>SQL timestamp with offset: "2024-01-15 10:30:00+09:00"
   *   <li>SQL timestamp without offset: "2024-01-15 10:30:00" (treated as UTC)
   *   <li>With fractional seconds: "2024-01-15T10:30:00.123456+09:00"
   * </ul>
   *
   * @param timestamp the timestamp string
   * @return epoch seconds in UTC, or the original string if parsing fails
   */
  private Object parseToEpochSecond(final String timestamp) {
    final var normalized = timestamp.trim().replace(' ', 'T');

    // Try parsing as OffsetDateTime (with timezone)
    try {
      final var odt = OffsetDateTime.parse(normalized, FLEXIBLE_OFFSET_FORMATTER);
      return odt.toEpochSecond();
    } catch (final DateTimeParseException ignored) {
      // Continue to next format
    }

    // Try parsing as LocalDateTime (without timezone, treat as UTC)
    try {
      final var ldt = LocalDateTime.parse(normalized, FLEXIBLE_LOCAL_FORMATTER);
      return ldt.toEpochSecond(ZoneOffset.UTC);
    } catch (final DateTimeParseException ignored) {
      // Continue to next format
    }

    // Try parsing as Instant
    try {
      return Instant.parse(normalized).getEpochSecond();
    } catch (final DateTimeParseException ignored) {
      // Parsing failed, return original string for equals comparison
    }

    return timestamp;
  }

  /** Formatter for timestamps with timezone offset. */
  private static final DateTimeFormatter FLEXIBLE_OFFSET_FORMATTER =
      new DateTimeFormatterBuilder()
          .append(DateTimeFormatter.ISO_LOCAL_DATE)
          .appendLiteral('T')
          .appendValue(ChronoField.HOUR_OF_DAY, 2)
          .appendLiteral(':')
          .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
          .optionalStart()
          .appendLiteral(':')
          .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
          .optionalEnd()
          .optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
          .optionalEnd()
          .appendOffset("+HH:MM", "Z")
          .toFormatter();

  /** Formatter for timestamps without timezone (treated as UTC). */
  private static final DateTimeFormatter FLEXIBLE_LOCAL_FORMATTER =
      new DateTimeFormatterBuilder()
          .append(DateTimeFormatter.ISO_LOCAL_DATE)
          .appendLiteral('T')
          .appendValue(ChronoField.HOUR_OF_DAY, 2)
          .appendLiteral(':')
          .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
          .optionalStart()
          .appendLiteral(':')
          .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
          .optionalEnd()
          .optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
          .optionalEnd()
          .toFormatter();

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
        .map(p -> String.format("ComparisonStrategy[REGEX:%s]", p.pattern()))
        .orElseGet(() -> String.format("ComparisonStrategy[%s]", type));
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
