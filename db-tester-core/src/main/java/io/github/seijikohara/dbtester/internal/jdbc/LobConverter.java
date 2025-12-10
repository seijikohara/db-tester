package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Converts LOB (Large Object) types to comparable Java types.
 *
 * <p>This class provides methods to convert CLOB and BLOB database objects to String
 * representations that can be compared after the database connection is closed.
 *
 * <p>CLOB values are converted to plain Strings. BLOB and byte array values are converted to
 * Base64-encoded Strings with a "[BASE64]" prefix.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class LobConverter {

  /** Prefix for Base64-encoded blob data. */
  private static final String BASE64_PREFIX = "[BASE64]";

  /** Creates a new LOB converter. */
  public LobConverter() {
    // Default constructor
  }

  /**
   * Converts a database value to a comparable type.
   *
   * <p>CLOB values are converted to strings and BLOB values are converted to Base64-encoded strings
   * to enable comparison after the connection is closed.
   *
   * @param value the raw database value
   * @return the converted value, or null if the input is null
   */
  @SuppressWarnings("NullAway")
  public @Nullable Object convert(final @Nullable Object value) {
    return Optional.ofNullable(value).map(this::convertNonNull).orElse(null);
  }

  /**
   * Converts a non-null database value to a comparable type.
   *
   * @param value the non-null database value
   * @return the converted value
   */
  private Object convertNonNull(final Object value) {
    // Handle CLOB - convert to String immediately
    if (value instanceof Clob clob) {
      return convertClob(clob);
    }

    // Handle BLOB - convert to Base64 String with marker
    if (value instanceof Blob blob) {
      return convertBlob(blob);
    }

    // Handle byte arrays - convert to Base64 String with marker
    if (value instanceof byte[] bytes) {
      return convertBytes(bytes);
    }

    return value;
  }

  /**
   * Converts a CLOB to a String.
   *
   * <p>This method uses an imperative loop because I/O operations require sequential buffered
   * reading. Stream-based alternatives would not improve readability or correctness for this
   * character stream processing pattern.
   *
   * @param clob the CLOB to convert
   * @return the CLOB content as a String
   * @throws DatabaseTesterException if reading fails
   */
  public String convertClob(final Clob clob) {
    try (final Reader reader = clob.getCharacterStream()) {
      final var stringBuilder = new StringBuilder();
      final var buffer = new char[1024];
      int length;
      while ((length = reader.read(buffer)) != -1) {
        stringBuilder.append(buffer, 0, length);
      }
      return stringBuilder.toString();
    } catch (final SQLException | IOException e) {
      throw new DatabaseTesterException("Failed to read CLOB content", e);
    }
  }

  /**
   * Converts a BLOB to a Base64-encoded String.
   *
   * @param blob the BLOB to convert
   * @return the BLOB content as a Base64-encoded String with "[BASE64]" prefix
   * @throws DatabaseTesterException if reading fails
   */
  public String convertBlob(final Blob blob) {
    try (final InputStream is = blob.getBinaryStream()) {
      final byte[] bytes = is.readAllBytes();
      return BASE64_PREFIX + Base64.getEncoder().encodeToString(bytes);
    } catch (final SQLException | IOException e) {
      throw new DatabaseTesterException("Failed to read BLOB content", e);
    }
  }

  /**
   * Converts a byte array to a Base64-encoded String.
   *
   * @param bytes the byte array to convert
   * @return the byte array as a Base64-encoded String with "[BASE64]" prefix
   */
  public String convertBytes(final byte[] bytes) {
    return BASE64_PREFIX + Base64.getEncoder().encodeToString(bytes);
  }
}
