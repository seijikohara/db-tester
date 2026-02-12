package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
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
import org.jspecify.annotations.Nullable;

/**
 * Executes comparison logic for {@link ComparisonStrategy} descriptors.
 *
 * <p>This class contains all comparison implementations that were previously embedded in {@code
 * ComparisonStrategy}. The API module defines descriptors (what to compare), while this class
 * handles execution (how to compare).
 *
 * @see ComparisonStrategy
 */
public final class ComparisonEngine {

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

  /** Pattern for slash-separated date format (yyyy/MM/dd). */
  private static final DateTimeFormatter SLASH_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");

  /** Pattern for dot-separated date format (yyyy.MM.dd). */
  private static final DateTimeFormatter DOT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy.MM.dd");

  /** Prevents instantiation. */
  private ComparisonEngine() {}

  /**
   * Compares two values according to the specified strategy.
   *
   * @param strategy the comparison strategy descriptor
   * @param expected the expected value
   * @param actual the actual value
   * @return {@code true} if the values match according to the strategy, {@code false} otherwise
   */
  public static boolean matches(
      final ComparisonStrategy strategy,
      final @Nullable Object expected,
      final @Nullable Object actual) {
    return switch (strategy.getType()) {
      case STRICT -> Objects.equals(expected, actual);
      case IGNORE -> true;
      case NUMERIC -> compareNumeric(expected, actual);
      case CASE_INSENSITIVE -> compareCaseInsensitive(expected, actual);
      case TIMESTAMP_FLEXIBLE -> compareTimestamp(expected, actual);
      case DATE_FLEXIBLE -> compareDateFlexible(expected, actual);
      case JSON_EQUIVALENT -> compareJsonEquivalent(expected, actual);
      case NOT_NULL -> actual != null;
      case REGEX -> matchesRegex(strategy, actual);
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
  private static boolean compareNumeric(
      final @Nullable Object expected, final @Nullable Object actual) {
    return compareNullable(expected, actual, ComparisonEngine::compareNumericValues);
  }

  /**
   * Compares two non-null values numerically.
   *
   * @param expected the expected value (non-null)
   * @param actual the actual value (non-null)
   * @return {@code true} if numerically equal, {@code false} otherwise
   */
  private static boolean compareNumericValues(final Object expected, final Object actual) {
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
  private static Optional<Number> toNumber(final Object value) {
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
  private static boolean compareCaseInsensitive(
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
  private static boolean compareTimestamp(
      final @Nullable Object expected, final @Nullable Object actual) {
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
  private static Object parseToEpochSecond(final String timestamp) {
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
  private static boolean compareDateFlexible(
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

  /**
   * Parses a date string to a LocalDate using multiple format attempts.
   *
   * <p>Tries the following formats in order: ISO-8601 (yyyy-MM-dd), slashed (yyyy/MM/dd), dot
   * (yyyy.MM.dd). If a timestamp format is detected (contains 'T' or space followed by time), the
   * date portion is extracted first.
   *
   * @param dateStr the date string to parse
   * @return the parsed LocalDate, or the original string if parsing fails
   */
  private static Object parseToLocalDate(final String dateStr) {
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
  private static String extractDatePortion(final String value) {
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
  private static boolean compareJsonEquivalent(
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

  // ========== Regex comparison ==========

  /**
   * Matches the actual value against the strategy's regex pattern.
   *
   * @param strategy the comparison strategy with regex pattern
   * @param actual the actual value
   * @return {@code true} if the value matches the pattern, {@code false} otherwise
   */
  private static boolean matchesRegex(
      final ComparisonStrategy strategy, final @Nullable Object actual) {
    return strategy
        .getPattern()
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
  private static boolean compareNullable(
      final @Nullable Object expected,
      final @Nullable Object actual,
      final BiFunction<Object, Object, Boolean> comparator) {
    return Optional.ofNullable(expected)
        .map(
            exp -> Optional.ofNullable(actual).map(act -> comparator.apply(exp, act)).orElse(false))
        .orElseGet(() -> actual == null);
  }
}
