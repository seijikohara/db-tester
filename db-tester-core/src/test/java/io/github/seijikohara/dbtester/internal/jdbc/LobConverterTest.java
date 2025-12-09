package io.github.seijikohara.dbtester.internal.jdbc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LobConverter}. */
@DisplayName("LobConverter")
class LobConverterTest {

  /** Tests for the LobConverter class. */
  LobConverterTest() {}

  /** The converter instance under test. */
  private LobConverter converter;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    converter = new LobConverter();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new LobConverter();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the convert() method. */
  @Nested
  @DisplayName("convert(Object) method")
  class ConvertMethod {

    /** Tests for the convert method. */
    ConvertMethod() {}

    /** Verifies that convert returns null when value is null. */
    @Test
    @Tag("normal")
    @DisplayName("should return null when value is null")
    void shouldReturnNull_whenValueIsNull() {
      // When
      final var result = converter.convert(null);

      // Then
      assertNull(result, "should return null for null input");
    }

    /** Verifies that convert returns same value for non-LOB types. */
    @Test
    @Tag("normal")
    @DisplayName("should return same value for non-LOB types")
    void shouldReturnSameValue_whenValueIsNotLob() {
      // Given
      final var stringValue = "test string";
      final var intValue = Integer.valueOf(42);

      // When
      final var stringResult = converter.convert(stringValue);
      final var intResult = converter.convert(intValue);

      // Then
      assertEquals(stringValue, stringResult, "should return same string value");
      assertEquals(intValue, intResult, "should return same integer value");
    }

    /**
     * Verifies that convert handles Clob correctly.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert Clob to String")
    void shouldConvertClob_whenValueIsClob() throws SQLException {
      // Given
      final var clob = mock(Clob.class);
      final var content = "Hello, World!";
      when(clob.getCharacterStream()).thenReturn(new StringReader(content));

      // When
      final var result = converter.convert(clob);

      // Then
      assertEquals(content, result, "should convert CLOB to String");
    }

    /**
     * Verifies that convert handles Blob correctly.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert Blob to Base64 String")
    void shouldConvertBlob_whenValueIsBlob() throws SQLException {
      // Given
      final var blob = mock(Blob.class);
      final var bytes = "Hello".getBytes(UTF_8);
      when(blob.getBinaryStream()).thenReturn(new ByteArrayInputStream(bytes));

      // When
      final var result = converter.convert(blob);

      // Then
      final var expected = "[BASE64]" + Base64.getEncoder().encodeToString(bytes);
      assertEquals(expected, result, "should convert BLOB to Base64 String with prefix");
    }

    /** Verifies that convert handles byte array correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should convert byte array to Base64 String")
    void shouldConvertByteArray_whenValueIsByteArray() {
      // Given
      final var bytes = "Hello".getBytes(UTF_8);

      // When
      final var result = converter.convert(bytes);

      // Then
      final var expected = "[BASE64]" + Base64.getEncoder().encodeToString(bytes);
      assertEquals(expected, result, "should convert byte array to Base64 String with prefix");
    }
  }

  /** Tests for the convertClob() method. */
  @Nested
  @DisplayName("convertClob(Clob) method")
  class ConvertClobMethod {

    /** Tests for the convertClob method. */
    ConvertClobMethod() {}

    /**
     * Verifies that convertClob returns correct string content.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return correct string content")
    void shouldReturnStringContent_whenClobHasContent() throws SQLException {
      // Given
      final var clob = mock(Clob.class);
      final var content = "Test CLOB content with special chars: äöü";
      when(clob.getCharacterStream()).thenReturn(new StringReader(content));

      // When
      final var result = converter.convertClob(clob);

      // Then
      assertEquals(content, result, "should convert CLOB content correctly");
    }

    /**
     * Verifies that convertClob handles empty CLOB.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty string when CLOB is empty")
    void shouldReturnEmptyString_whenClobIsEmpty() throws SQLException {
      // Given
      final var clob = mock(Clob.class);
      when(clob.getCharacterStream()).thenReturn(new StringReader(""));

      // When
      final var result = converter.convertClob(clob);

      // Then
      assertEquals("", result, "should return empty string for empty CLOB");
    }

    /**
     * Verifies that convertClob handles large CLOB content.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle large CLOB content")
    void shouldHandleLargeContent_whenClobIsLarge() throws SQLException {
      // Given
      final var clob = mock(Clob.class);
      final var content = "A".repeat(10000);
      when(clob.getCharacterStream()).thenReturn(new StringReader(content));

      // When
      final var result = converter.convertClob(clob);

      // Then
      assertEquals(content, result, "should handle large CLOB content");
      assertEquals(10000, result.length(), "should preserve content length");
    }

    /**
     * Verifies that convertClob throws exception when read fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseTesterException when read fails")
    void shouldThrowException_whenReadFails() throws SQLException {
      // Given
      final var clob = mock(Clob.class);
      when(clob.getCharacterStream()).thenThrow(new SQLException("Connection closed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> converter.convertClob(clob),
              "should throw DatabaseTesterException");
      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("Failed to read CLOB content"),
          "exception message should indicate CLOB read failure");
    }

    /**
     * Verifies that convertClob throws exception on IOException.
     *
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseTesterException when IOException occurs")
    void shouldThrowException_whenIOExceptionOccurs() throws SQLException, IOException {
      // Given
      final var clob = mock(Clob.class);
      final var reader = mock(Reader.class);
      when(clob.getCharacterStream()).thenReturn(reader);
      when(reader.read(new char[1024])).thenThrow(new IOException("Read error"));

      // When & Then
      assertThrows(
          DatabaseTesterException.class,
          () -> converter.convertClob(clob),
          "should throw DatabaseTesterException on IOException");
    }
  }

  /** Tests for the convertBlob() method. */
  @Nested
  @DisplayName("convertBlob(Blob) method")
  class ConvertBlobMethod {

    /** Tests for the convertBlob method. */
    ConvertBlobMethod() {}

    /**
     * Verifies that convertBlob returns Base64 encoded string with prefix.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return Base64 encoded string with prefix")
    void shouldReturnBase64WithPrefix_whenBlobHasContent() throws SQLException {
      // Given
      final var blob = mock(Blob.class);
      final var bytes = "Hello, World!".getBytes(UTF_8);
      when(blob.getBinaryStream()).thenReturn(new ByteArrayInputStream(bytes));

      // When
      final var result = converter.convertBlob(blob);

      // Then
      assertTrue(result.startsWith("[BASE64]"), "should start with [BASE64] prefix");
      final var decoded = Base64.getDecoder().decode(result.substring(8));
      assertArrayEquals(bytes, decoded, "should encode content correctly");
    }

    /**
     * Verifies that convertBlob handles empty BLOB.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return prefix only when BLOB is empty")
    void shouldReturnPrefixOnly_whenBlobIsEmpty() throws SQLException {
      // Given
      final var blob = mock(Blob.class);
      when(blob.getBinaryStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

      // When
      final var result = converter.convertBlob(blob);

      // Then
      assertEquals("[BASE64]", result, "should return prefix only for empty BLOB");
    }

    /**
     * Verifies that convertBlob throws exception when read fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseTesterException when read fails")
    void shouldThrowException_whenReadFails() throws SQLException {
      // Given
      final var blob = mock(Blob.class);
      when(blob.getBinaryStream()).thenThrow(new SQLException("Connection closed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> converter.convertBlob(blob),
              "should throw DatabaseTesterException");
      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("Failed to read BLOB content"),
          "exception message should indicate BLOB read failure");
    }
  }

  /** Tests for the convertBytes() method. */
  @Nested
  @DisplayName("convertBytes(byte[]) method")
  class ConvertBytesMethod {

    /** Tests for the convertBytes method. */
    ConvertBytesMethod() {}

    /** Verifies that convertBytes returns Base64 encoded string with prefix. */
    @Test
    @Tag("normal")
    @DisplayName("should return Base64 encoded string with prefix")
    void shouldReturnBase64WithPrefix_whenBytesProvided() {
      // Given
      final var bytes = "Hello, World!".getBytes(UTF_8);

      // When
      final var result = converter.convertBytes(bytes);

      // Then
      assertTrue(result.startsWith("[BASE64]"), "should start with [BASE64] prefix");
      final var decoded = Base64.getDecoder().decode(result.substring(8));
      assertArrayEquals(bytes, decoded, "should encode content correctly");
    }

    /** Verifies that convertBytes handles empty byte array. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return prefix only when byte array is empty")
    void shouldReturnPrefixOnly_whenBytesEmpty() {
      // Given
      final var bytes = new byte[0];

      // When
      final var result = converter.convertBytes(bytes);

      // Then
      assertEquals("[BASE64]", result, "should return prefix only for empty byte array");
    }

    /** Verifies that convertBytes handles binary data. */
    @Test
    @Tag("normal")
    @DisplayName("should handle binary data correctly")
    void shouldHandleBinaryData_whenBytesContainBinaryData() {
      // Given
      final var bytes = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};

      // When
      final var result = converter.convertBytes(bytes);

      // Then
      final var decoded = Base64.getDecoder().decode(result.substring(8));
      assertArrayEquals(bytes, decoded, "should encode binary data correctly");
    }
  }
}
