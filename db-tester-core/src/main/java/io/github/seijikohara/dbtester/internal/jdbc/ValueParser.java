package io.github.seijikohara.dbtester.internal.jdbc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Locale;

/**
 * Parses string values from CSV files to appropriate SQL types.
 *
 * <p>This class provides methods to convert string representations of values to their corresponding
 * Java/SQL types for database insertion.
 *
 * <p>Supported conversions include: Boolean ("true", "1", "yes", "y"), Date ("yyyy-MM-dd"), Time
 * ("HH:mm:ss"), Timestamp ("yyyy-MM-dd HH:mm:ss"), BLOB (Base64-encoded or UTF-8 text), and
 * standard numeric types (Integer, Long, Float, Double, BigDecimal).
 *
 * <p>This class is stateless and thread-safe.
 */
public final class ValueParser {

  /** Prefix for Base64-encoded blob data in CSV files. */
  private static final String BASE64_PREFIX = "[BASE64]";

  /** Creates a new value parser. */
  public ValueParser() {
    // Default constructor
  }

  /**
   * Parses a string value to boolean, supporting common representations.
   *
   * <p>Recognized true values (case-insensitive): "true", "1", "yes", "y". All other values return
   * false.
   *
   * @param value the string value
   * @return the boolean value
   */
  public boolean parseBoolean(final String value) {
    final var normalized = value.trim().toLowerCase(Locale.ROOT);
    return "true".equals(normalized)
        || "1".equals(normalized)
        || "yes".equals(normalized)
        || "y".equals(normalized);
  }

  /**
   * Parses a string value to SQL Date.
   *
   * <p>Supports formats: "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss". If the input contains a time
   * component, only the date portion is used.
   *
   * @param value the string value in date format
   * @return the SQL Date
   * @throws IllegalArgumentException if the format is invalid
   */
  public Date parseDate(final String value) {
    final var trimmed = value.trim();
    // If it contains time, extract only the date part
    final var spaceIndex = trimmed.indexOf(' ');
    final var datePart = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
    return Date.valueOf(datePart);
  }

  /**
   * Parses a string value to SQL Time.
   *
   * <p>Supports formats: "HH:mm:ss" or "HH:mm:ss.SSS". Fractional seconds are removed. If the input
   * contains a date portion (e.g., from a datetime string), only the time portion is used.
   *
   * @param value the string value in time format
   * @return the SQL Time
   * @throws IllegalArgumentException if the format is invalid
   */
  public Time parseTime(final String value) {
    final var trimmed = value.trim();
    // Remove fractional seconds if present
    final var dotIndex = trimmed.indexOf('.');
    final var timePart = dotIndex > 0 ? trimmed.substring(0, dotIndex) : trimmed;
    // If it contains date, extract only the time part
    final var spaceIndex = timePart.indexOf(' ');
    final var timeOnly = spaceIndex > 0 ? timePart.substring(spaceIndex + 1) : timePart;
    return Time.valueOf(timeOnly);
  }

  /**
   * Parses a string value to SQL Timestamp.
   *
   * <p>Supports formats: "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd HH:mm:ss.SSS".
   *
   * @param value the string value in timestamp format
   * @return the SQL Timestamp
   * @throws IllegalArgumentException if the format is invalid
   */
  public Timestamp parseTimestamp(final String value) {
    return Timestamp.valueOf(value.trim());
  }

  /**
   * Parses a string value to a byte array for BLOB storage.
   *
   * <p>Supports Base64-encoded values with the "[BASE64]" prefix. If the prefix is present, the
   * remaining content is decoded from Base64. Otherwise, the string is converted to bytes using
   * UTF-8 encoding.
   *
   * @param value the string value
   * @return the byte array
   * @throws IllegalArgumentException if Base64 decoding fails
   */
  public byte[] parseBlob(final String value) {
    final var trimmed = value.trim();
    if (trimmed.startsWith(BASE64_PREFIX)) {
      final var base64Content = trimmed.substring(BASE64_PREFIX.length());
      return Base64.getDecoder().decode(base64Content);
    }
    // Treat as plain text if not Base64-encoded
    return trimmed.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Parses a string value to Integer.
   *
   * @param value the string value
   * @return the Integer value
   * @throws NumberFormatException if parsing fails
   */
  public int parseInt(final String value) {
    return Integer.parseInt(value.trim());
  }

  /**
   * Parses a string value to Long.
   *
   * @param value the string value
   * @return the Long value
   * @throws NumberFormatException if parsing fails
   */
  public long parseLong(final String value) {
    return Long.parseLong(value.trim());
  }

  /**
   * Parses a string value to Float.
   *
   * @param value the string value
   * @return the Float value
   * @throws NumberFormatException if parsing fails
   */
  public float parseFloat(final String value) {
    return Float.parseFloat(value.trim());
  }

  /**
   * Parses a string value to Double.
   *
   * @param value the string value
   * @return the Double value
   * @throws NumberFormatException if parsing fails
   */
  public double parseDouble(final String value) {
    return Double.parseDouble(value.trim());
  }

  /**
   * Parses a string value to BigDecimal.
   *
   * @param value the string value
   * @return the BigDecimal value
   * @throws NumberFormatException if parsing fails
   */
  public BigDecimal parseBigDecimal(final String value) {
    return new BigDecimal(value.trim());
  }
}
