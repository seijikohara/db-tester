package io.github.seijikohara.dbtester.internal.jdbc.type;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link UuidTypeHandler}. */
@DisplayName("UuidTypeHandler")
class UuidTypeHandlerTest {

  /** Tests for the UuidTypeHandler class. */
  UuidTypeHandlerTest() {}

  /** The handler under test. */
  private UuidTypeHandler handler;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    handler = new UuidTypeHandler();
  }

  /** Tests for the getJavaType() method. */
  @Nested
  @DisplayName("getJavaType() method")
  class GetJavaTypeMethod {

    /** Tests for the getJavaType method. */
    GetJavaTypeMethod() {}

    /** Verifies that getJavaType returns UUID class. */
    @Test
    @Tag("normal")
    @DisplayName("should return UUID class when called")
    void shouldReturnUuidClass_whenCalled() {
      // When
      final var result = handler.getJavaType();

      // Then
      assertEquals(UUID.class, result, "should return UUID class");
    }
  }

  /** Tests for the getSqlTypes() method. */
  @Nested
  @DisplayName("getSqlTypes() method")
  class GetSqlTypesMethod {

    /** Tests for the getSqlTypes method. */
    GetSqlTypesMethod() {}

    /** Verifies that getSqlTypes returns OTHER type. */
    @Test
    @Tag("normal")
    @DisplayName("should return OTHER type when called")
    void shouldReturnOtherType_whenCalled() {
      // When
      final var result = handler.getSqlTypes();

      // Then
      assertAll(
          "SQL types should be configured correctly",
          () -> assertEquals(1, result.size(), "should have one SQL type"),
          () -> assertTrue(result.contains(Types.OTHER), "should contain Types.OTHER"));
    }
  }

  /** Tests for the getSupportedDatabases() method. */
  @Nested
  @DisplayName("getSupportedDatabases() method")
  class GetSupportedDatabasesMethod {

    /** Tests for the getSupportedDatabases method. */
    GetSupportedDatabasesMethod() {}

    /** Verifies that getSupportedDatabases includes PostgreSQL and H2. */
    @Test
    @Tag("normal")
    @DisplayName("should return PostgreSQL and H2 when called")
    void shouldReturnPostgresAndH2_whenCalled() {
      // When
      final var result = handler.getSupportedDatabases();

      // Then
      assertAll(
          "supported databases should be correct",
          () -> assertEquals(2, result.size(), "should have two databases"),
          () -> assertTrue(result.contains("PostgreSQL"), "should contain PostgreSQL"),
          () -> assertTrue(result.contains("H2"), "should contain H2"));
    }
  }

  /** Tests for the read() method. */
  @Nested
  @DisplayName("read(ResultSet, int) method")
  class ReadMethod {

    /** Tests for the read method. */
    ReadMethod() {}

    /** Mock ResultSet for testing. */
    private ResultSet mockResultSet;

    /** Sets up mock for read tests. */
    @BeforeEach
    void setUp() {
      mockResultSet = mock(ResultSet.class);
    }

    /**
     * Verifies that read returns UUID when ResultSet contains UUID.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return UUID when ResultSet contains UUID object")
    void shouldReturnUuid_whenResultSetContainsUuidObject() throws SQLException {
      // Given
      final var expectedUuid = UUID.randomUUID();
      when(mockResultSet.getObject(1)).thenReturn(expectedUuid);
      when(mockResultSet.wasNull()).thenReturn(false);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertEquals(expectedUuid, result, "should return the UUID from ResultSet");
    }

    /**
     * Verifies that read returns UUID when ResultSet contains String.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return UUID when ResultSet contains String")
    void shouldReturnUuid_whenResultSetContainsString() throws SQLException {
      // Given
      final var expectedUuid = UUID.randomUUID();
      when(mockResultSet.getObject(1)).thenReturn(expectedUuid.toString());
      when(mockResultSet.wasNull()).thenReturn(false);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertEquals(expectedUuid, result, "should parse String to UUID");
    }

    /**
     * Verifies that read returns null when value is null.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return null when value is null")
    void shouldReturnNull_whenValueIsNull() throws SQLException {
      // Given
      when(mockResultSet.getObject(1)).thenReturn(null);
      when(mockResultSet.wasNull()).thenReturn(true);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertNull(result, "should return null for null value");
    }
  }

  /** Tests for the write() method. */
  @Nested
  @DisplayName("write(PreparedStatement, int, UUID) method")
  class WriteMethod {

    /** Tests for the write method. */
    WriteMethod() {}

    /** Mock PreparedStatement for testing. */
    private PreparedStatement mockStatement;

    /** Sets up mock for write tests. */
    @BeforeEach
    void setUp() {
      mockStatement = mock(PreparedStatement.class);
    }

    /**
     * Verifies that write sets UUID with Types.OTHER.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set object with Types.OTHER when writing UUID")
    void shouldSetObjectWithTypesOther_whenWritingUuid() throws SQLException {
      // Given
      final var uuid = UUID.randomUUID();

      // When
      handler.write(mockStatement, 1, uuid);

      // Then
      verify(mockStatement).setObject(1, uuid, Types.OTHER);
    }
  }

  /** Tests for the format() method. */
  @Nested
  @DisplayName("format(UUID) method")
  class FormatMethod {

    /** Tests for the format method. */
    FormatMethod() {}

    /** Verifies that format returns UUID string representation. */
    @Test
    @Tag("normal")
    @DisplayName("should return UUID string when formatting")
    void shouldReturnUuidString_whenFormatting() {
      // Given
      final var uuid = UUID.randomUUID();

      // When
      final var result = handler.format(uuid);

      // Then
      assertEquals(uuid.toString(), result, "should return UUID string representation");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(String) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /** Verifies that parse returns UUID from valid string. */
    @Test
    @Tag("normal")
    @DisplayName("should return UUID when parsing valid string")
    void shouldReturnUuid_whenParsingValidString() {
      // Given
      final var uuid = UUID.randomUUID();
      final var uuidString = uuid.toString();

      // When
      final var result = handler.parse(uuidString);

      // Then
      assertEquals(uuid, result, "should parse string to UUID");
    }

    /** Verifies that parse handles whitespace. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle whitespace when parsing")
    void shouldHandleWhitespace_whenParsing() {
      // Given
      final var uuid = UUID.randomUUID();
      final var uuidString = "  " + uuid + "  ";

      // When
      final var result = handler.parse(uuidString);

      // Then
      assertEquals(uuid, result, "should trim whitespace and parse");
    }

    /** Verifies that parse throws exception for invalid format. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when format is invalid")
    void shouldThrowException_whenFormatIsInvalid() {
      // Given
      final var invalidUuid = "not-a-valid-uuid";

      // When & Then
      final var exception =
          assertThrows(IllegalArgumentException.class, () -> handler.parse(invalidUuid));

      assertNotNull(exception.getMessage(), "exception should have message");
    }
  }
}
