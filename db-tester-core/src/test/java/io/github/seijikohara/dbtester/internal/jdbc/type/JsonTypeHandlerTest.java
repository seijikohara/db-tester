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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JsonTypeHandler}. */
@DisplayName("JsonTypeHandler")
class JsonTypeHandlerTest {

  /** Tests for the JsonTypeHandler class. */
  JsonTypeHandlerTest() {}

  /** The handler under test. */
  private JsonTypeHandler handler;

  /** ObjectMapper for creating test data. */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    handler = new JsonTypeHandler();
  }

  /** Tests for the getJavaType() method. */
  @Nested
  @DisplayName("getJavaType() method")
  class GetJavaTypeMethod {

    /** Tests for the getJavaType method. */
    GetJavaTypeMethod() {}

    /** Verifies that getJavaType returns JsonNode class. */
    @Test
    @Tag("normal")
    @DisplayName("should return JsonNode class when called")
    void shouldReturnJsonNodeClass_whenCalled() {
      // When
      final var result = handler.getJavaType();

      // Then
      assertEquals(JsonNode.class, result, "should return JsonNode class");
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

    /** Verifies that getSupportedDatabases includes PostgreSQL. */
    @Test
    @Tag("normal")
    @DisplayName("should return PostgreSQL when called")
    void shouldReturnPostgres_whenCalled() {
      // When
      final var result = handler.getSupportedDatabases();

      // Then
      assertAll(
          "supported databases should be correct",
          () -> assertEquals(1, result.size(), "should have one database"),
          () -> assertTrue(result.contains("PostgreSQL"), "should contain PostgreSQL"));
    }
  }

  /** Tests for the getPriority() method. */
  @Nested
  @DisplayName("getPriority() method")
  class GetPriorityMethod {

    /** Tests for the getPriority method. */
    GetPriorityMethod() {}

    /** Verifies that getPriority returns higher than UUID handler. */
    @Test
    @Tag("normal")
    @DisplayName("should return higher priority than UUID handler")
    void shouldReturnHigherPriority_thanUuidHandler() {
      // When
      final var jsonPriority = handler.getPriority();
      final var uuidPriority = new UuidTypeHandler().getPriority();

      // Then
      assertTrue(
          jsonPriority > uuidPriority,
          "JSON handler should have higher priority than UUID handler");
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
     * Verifies that read returns JsonNode when ResultSet contains JSON string.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return JsonNode when ResultSet contains JSON string")
    void shouldReturnJsonNode_whenResultSetContainsJsonString() throws SQLException {
      // Given
      final var jsonString = "{\"name\":\"test\",\"value\":123}";
      when(mockResultSet.getString(1)).thenReturn(jsonString);
      when(mockResultSet.wasNull()).thenReturn(false);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertNotNull(result, "result should not be null");
      final var jsonNode = result;
      assertAll(
          "JsonNode should be parsed correctly",
          () -> assertEquals("test", jsonNode.get("name").asText(), "name should be test"),
          () -> assertEquals(123, jsonNode.get("value").asInt(), "value should be 123"));
    }

    /**
     * Verifies that read handles JSON array.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle JSON array when reading")
    void shouldHandleJsonArray_whenReading() throws SQLException {
      // Given
      final var jsonString = "[1,2,3]";
      when(mockResultSet.getString(1)).thenReturn(jsonString);
      when(mockResultSet.wasNull()).thenReturn(false);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertNotNull(result, "result should not be null");
      final var jsonArray = result;
      assertAll(
          "JSON array should be parsed correctly",
          () -> assertTrue(jsonArray.isArray(), "should be an array"),
          () -> assertEquals(3, jsonArray.size(), "should have 3 elements"));
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
      when(mockResultSet.getString(1)).thenReturn(null);
      when(mockResultSet.wasNull()).thenReturn(true);

      // When
      final var result = handler.read(mockResultSet, 1);

      // Then
      assertNull(result, "should return null for null value");
    }

    /**
     * Verifies that read throws SQLException for invalid JSON.
     *
     * @throws SQLException if database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw SQLException when JSON is invalid")
    void shouldThrowSqlException_whenJsonIsInvalid() throws SQLException {
      // Given
      when(mockResultSet.getString(1)).thenReturn("not valid json");
      when(mockResultSet.wasNull()).thenReturn(false);

      // When & Then
      assertThrows(SQLException.class, () -> handler.read(mockResultSet, 1));
    }
  }

  /** Tests for the write() method. */
  @Nested
  @DisplayName("write(PreparedStatement, int, JsonNode) method")
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
     * Verifies that write sets JSON string with Types.OTHER.
     *
     * @throws Exception if error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set JSON string with Types.OTHER when writing")
    void shouldSetJsonStringWithTypesOther_whenWriting() throws Exception {
      // Given
      final var jsonNode = objectMapper.readTree("{\"key\":\"value\"}");

      // When
      handler.write(mockStatement, 1, jsonNode);

      // Then
      verify(mockStatement).setObject(1, "{\"key\":\"value\"}", Types.OTHER);
    }
  }

  /** Tests for the format() method. */
  @Nested
  @DisplayName("format(JsonNode) method")
  class FormatMethod {

    /** Tests for the format method. */
    FormatMethod() {}

    /**
     * Verifies that format returns JSON string representation.
     *
     * @throws Exception if parsing fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should return JSON string when formatting")
    void shouldReturnJsonString_whenFormatting() throws Exception {
      // Given
      final var jsonNode = objectMapper.readTree("{\"name\":\"test\"}");

      // When
      final var result = handler.format(jsonNode);

      // Then
      assertEquals("{\"name\":\"test\"}", result, "should return JSON string");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(String) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /** Verifies that parse returns JsonNode from valid JSON string. */
    @Test
    @Tag("normal")
    @DisplayName("should return JsonNode when parsing valid JSON")
    void shouldReturnJsonNode_whenParsingValidJson() {
      // Given
      final var jsonString = "{\"key\":\"value\"}";

      // When
      final var result = handler.parse(jsonString);

      // Then
      assertEquals("value", result.get("key").asText(), "should parse JSON correctly");
    }

    /** Verifies that parse throws exception for invalid JSON. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when JSON is invalid")
    void shouldThrowException_whenJsonIsInvalid() {
      // Given
      final var invalidJson = "not valid json";

      // When & Then
      final var exception =
          assertThrows(IllegalArgumentException.class, () -> handler.parse(invalidJson));

      assertNotNull(exception.getMessage(), "exception should have message");
    }
  }
}
