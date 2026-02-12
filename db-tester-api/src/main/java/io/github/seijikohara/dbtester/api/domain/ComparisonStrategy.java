package io.github.seijikohara.dbtester.api.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import java.util.regex.Matcher;
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
 *   <li>{@link #contains()} - Substring containment check
 *   <li>{@link #range(double, double)} - Numeric range verification
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

  /** Pattern for parsing RANGE options in format "min=N,max=M". */
  private static final Pattern RANGE_OPTIONS_PATTERN =
      Pattern.compile("min\\s*=\\s*([\\d.eE+\\-]+)\\s*,\\s*max\\s*=\\s*([\\d.eE+\\-]+)");

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
   * Creates a CONTAINS comparison strategy without a specific substring.
   *
   * <p>The expected value is checked as a substring of the actual value.
   *
   * @return a new CONTAINS comparison strategy
   * @deprecated Use {@link #regex(String)} with pattern {@code ".*substring.*"} instead. Removed in
   *     2.0.
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public static ComparisonStrategy contains() {
    return new ComparisonStrategy(Type.CONTAINS, null, null);
  }

  /**
   * Creates a CONTAINS comparison strategy with a specific substring.
   *
   * <p>The actual value must contain the specified substring.
   *
   * @param substring the substring to search for in the actual value
   * @return a new CONTAINS comparison strategy
   * @deprecated Use {@link #regex(String)} with pattern {@code ".*substring.*"} instead. Removed in
   *     2.0.
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public static ComparisonStrategy contains(final String substring) {
    return new ComparisonStrategy(Type.CONTAINS, null, substring);
  }

  /**
   * Creates a RANGE comparison strategy from an options string.
   *
   * <p>The options string must be in the format {@code "min=N,max=M"} where N and M are numeric
   * values. Both min and max are inclusive.
   *
   * @param rangeOptions the range options string (e.g., "min=100,max=200")
   * @return a new RANGE comparison strategy
   * @throws IllegalArgumentException if the options string format is invalid
   * @deprecated Use programmatic assertions for range verification instead. Removed in 2.0.
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public static ComparisonStrategy range(final String rangeOptions) {
    final var trimmed = rangeOptions.trim();
    final Matcher matcher = RANGE_OPTIONS_PATTERN.matcher(trimmed);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid RANGE options format: '%s'. Expected format: 'min=N,max=M'", rangeOptions));
    }
    try {
      final var min = new BigDecimal(matcher.group(1));
      final var max = new BigDecimal(matcher.group(2));
      if (min.compareTo(max) > 0) {
        throw new IllegalArgumentException(
            String.format("min (%s) must not be greater than max (%s)", min, max));
      }
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid numeric values in RANGE options: '%s'. Expected format: 'min=N,max=M'",
              rangeOptions),
          e);
    }
    return new ComparisonStrategy(Type.RANGE, null, trimmed);
  }

  /**
   * Creates a RANGE comparison strategy with explicit min and max values.
   *
   * @param min the minimum value (inclusive)
   * @param max the maximum value (inclusive)
   * @return a new RANGE comparison strategy
   * @throws IllegalArgumentException if min is greater than max
   * @deprecated Use programmatic assertions for range verification instead. Removed in 2.0.
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public static ComparisonStrategy range(final double min, final double max) {
    if (min > max) {
      throw new IllegalArgumentException(
          String.format("min (%s) must not be greater than max (%s)", min, max));
    }
    final var rangeOptions = String.format("min=%s,max=%s", min, max);
    return new ComparisonStrategy(Type.RANGE, null, rangeOptions);
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

  /**
   * Compares two values according to this strategy.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if the values match according to this strategy, {@code false} otherwise
   * @deprecated Use {@code ComparisonEngine.matches(strategy, expected, actual)} in the core module
   *     instead. Comparison logic is moving from API descriptors to core execution. Removed in 2.0.
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public boolean matches(final @Nullable Object expected, final @Nullable Object actual) {
    return switch (type) {
      case STRICT -> Objects.equals(expected, actual);
      case IGNORE -> true;
      case NUMERIC -> compareNumeric(expected, actual);
      case CASE_INSENSITIVE -> compareCaseInsensitive(expected, actual);
      case TIMESTAMP_FLEXIBLE -> compareTimestamp(expected, actual);
      case DATE_FLEXIBLE -> compareDateFlexible(expected, actual);
      case JSON_EQUIVALENT -> compareJsonEquivalent(expected, actual);
      case NOT_NULL -> actual != null;
      case REGEX -> matchesRegex(actual);
      case CONTAINS -> matchesContains(expected, actual);
      case RANGE -> matchesRange(actual);
    };
  }

  // ========== Numeric comparison ==========

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
      return toNumber(expected)
          .flatMap(
              expNum ->
                  toNumber(actual)
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
   * @return the number, or empty if not convertible
   */
  private Optional<Number> toNumber(final Object value) {
    if (value instanceof Number num) {
      return Optional.of(num);
    }
    if (value instanceof String str) {
      try {
        return Optional.of(new BigDecimal(str.trim()));
      } catch (final NumberFormatException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  // ========== Case-insensitive comparison ==========

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

  // ========== Timestamp comparison ==========

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

  // ========== Date flexible comparison ==========

  /**
   * Compares two date values with format flexibility.
   *
   * <p>Supports ISO-8601 (yyyy-MM-dd), slashed (yyyy/MM/dd), and SQL date formats. Both values are
   * parsed to {@link LocalDate} and compared.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if both values represent the same date, {@code false} otherwise
   */
  private boolean compareDateFlexible(
      final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(
        expected,
        actual,
        (exp, act) -> {
          final var expectedDate = parseToLocalDate(exp.toString());
          final var actualDate = parseToLocalDate(act.toString());
          return expectedDate.equals(actualDate);
        });
  }

  /** Pattern for slash-separated date format (yyyy/MM/dd). */
  private static final DateTimeFormatter SLASH_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");

  /** Pattern for dot-separated date format (yyyy.MM.dd). */
  private static final DateTimeFormatter DOT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy.MM.dd");

  /**
   * Parses a date string to a LocalDate using multiple format attempts.
   *
   * <p>Tries the following formats in order: ISO-8601 (yyyy-MM-dd), slashed (yyyy/MM/dd), dot
   * (yyyy.MM.dd). If a timestamp format is detected (contains 'T' or space followed by time), the
   * date portion is extracted first.
   *
   * @param dateStr the date string to parse
   * @return the parsed LocalDate, or a sentinel value if parsing fails
   */
  private Object parseToLocalDate(final String dateStr) {
    final var trimmed = dateStr.trim();

    // Extract date portion from timestamp if needed
    final var datePart = extractDatePortion(trimmed);

    // Try ISO-8601 format (yyyy-MM-dd)
    try {
      return LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (final DateTimeParseException ignored) {
      // Continue to next format
    }

    // Try slashed format (yyyy/MM/dd)
    try {
      return LocalDate.parse(datePart, SLASH_DATE_FORMATTER);
    } catch (final DateTimeParseException ignored) {
      // Continue to next format
    }

    // Try dot format (yyyy.MM.dd)
    try {
      return LocalDate.parse(datePart, DOT_DATE_FORMATTER);
    } catch (final DateTimeParseException ignored) {
      // Parsing failed
    }

    return dateStr;
  }

  /**
   * Extracts the date portion from a timestamp string.
   *
   * @param value the timestamp or date string
   * @return the date portion only
   */
  private String extractDatePortion(final String value) {
    // Handle ISO format with T separator
    final var tIndex = value.indexOf('T');
    if (tIndex > 0) {
      return value.substring(0, tIndex);
    }
    // Handle SQL format with space separator (only if time-like pattern follows)
    final var spaceIndex = value.indexOf(' ');
    if (spaceIndex > 0 && spaceIndex < value.length() - 1) {
      final var afterSpace = value.charAt(spaceIndex + 1);
      if (afterSpace >= '0' && afterSpace <= '9') {
        return value.substring(0, spaceIndex);
      }
    }
    return value;
  }

  // ========== JSON equivalent comparison ==========

  /**
   * Compares two JSON values by structural equivalence.
   *
   * <p>Object key order and insignificant whitespace are ignored. Values are normalized before
   * comparison by parsing and re-serializing with sorted keys.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if the JSON structures are equivalent, {@code false} otherwise
   */
  private boolean compareJsonEquivalent(
      final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(
        expected,
        actual,
        (exp, act) -> {
          final var expStr = exp.toString();
          final var actStr = act.toString();
          if (JsonNormalizer.looksLikeJson(expStr) && JsonNormalizer.looksLikeJson(actStr)) {
            return JsonNormalizer.normalize(expStr).equals(JsonNormalizer.normalize(actStr));
          }
          return Objects.equals(expStr, actStr);
        });
  }

  // ========== Contains comparison ==========

  /**
   * Checks if the actual value contains the expected value (or the configured substring).
   *
   * <p>If an options string is configured, the actual value is checked for that substring.
   * Otherwise, the expected value is used as the substring to find in the actual value.
   *
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if the actual value contains the substring, {@code false} otherwise
   */
  private boolean matchesContains(final @Nullable Object expected, final @Nullable Object actual) {
    if (actual == null) {
      return false;
    }
    final var actualStr = actual.toString();
    if (options != null && !options.isEmpty()) {
      return actualStr.contains(options);
    }
    if (expected == null) {
      return false;
    }
    return actualStr.contains(expected.toString());
  }

  // ========== Range comparison ==========

  /**
   * Checks if the actual value falls within the configured numeric range.
   *
   * @param actual the actual value
   * @return {@code true} if the value is within range (inclusive), {@code false} otherwise
   */
  private boolean matchesRange(final @Nullable Object actual) {
    if (actual == null || options == null) {
      return false;
    }
    final Matcher matcher = RANGE_OPTIONS_PATTERN.matcher(options);
    if (!matcher.matches()) {
      return false;
    }
    try {
      final var min = new BigDecimal(matcher.group(1));
      final var max = new BigDecimal(matcher.group(2));
      final var actualValue = toBigDecimalFromObject(actual);
      return actualValue.map(v -> v.compareTo(min) >= 0 && v.compareTo(max) <= 0).orElse(false);
    } catch (final NumberFormatException e) {
      return false;
    }
  }

  /**
   * Converts an object to BigDecimal if possible.
   *
   * @param value the value to convert
   * @return the BigDecimal, or empty if not convertible
   */
  private Optional<BigDecimal> toBigDecimalFromObject(final Object value) {
    if (value instanceof Number num) {
      try {
        return Optional.of(new BigDecimal(num.toString()));
      } catch (final NumberFormatException e) {
        return Optional.empty();
      }
    }
    if (value instanceof String str) {
      try {
        return Optional.of(new BigDecimal(str.trim()));
      } catch (final NumberFormatException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  // ========== Regex comparison ==========

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

  // ========== Null-safe comparison utility ==========

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
    REGEX,

    /**
     * Substring containment check.
     *
     * @deprecated Use {@link #REGEX} instead. Removed in 2.0.
     */
    @Deprecated(since = "1.1", forRemoval = true)
    CONTAINS,

    /**
     * Numeric range verification.
     *
     * @deprecated Use programmatic assertions instead. Removed in 2.0.
     */
    @Deprecated(since = "1.1", forRemoval = true)
    RANGE
  }
}
