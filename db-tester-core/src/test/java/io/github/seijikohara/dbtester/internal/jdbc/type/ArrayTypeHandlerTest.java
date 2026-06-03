package io.github.seijikohara.dbtester.internal.jdbc.type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ArrayTypeHandler}. */
@DisplayName("ArrayTypeHandler")
class ArrayTypeHandlerTest {

  /** Tests for the ArrayTypeHandler class. */
  ArrayTypeHandlerTest() {}

  /** The handler instance under test. */
  private final ArrayTypeHandler handler = new ArrayTypeHandler();

  /** Tests for the SPI metadata accessors. */
  @Nested
  @DisplayName("metadata accessors")
  class MetadataAccessors {

    /** Tests for the metadata accessors. */
    MetadataAccessors() {}

    /** Verifies that the handler declares the expected SPI metadata. */
    @Test
    @Tag("normal")
    @DisplayName("should declare ARRAY support for PostgreSQL and H2")
    void shouldDeclareArraySupport_whenQueried() {
      // When & Then
      assertEquals(Object[].class, handler.javaType(), "javaType should be Object[]");
      assertTrue(handler.sqlTypes().contains(Types.ARRAY), "should support Types.ARRAY");
      assertTrue(handler.supportedDatabases().contains("PostgreSQL"), "should support PostgreSQL");
      assertTrue(handler.supportedDatabases().contains("H2"), "should support H2");
      assertEquals(10, handler.priority(), "priority should be 10");
    }
  }

  /** Tests for the format() method. */
  @Nested
  @DisplayName("format(Object[]) method")
  class FormatMethod {

    /** Tests for the format method. */
    FormatMethod() {}

    /** Verifies that format renders a PostgreSQL array literal. */
    @Test
    @Tag("normal")
    @DisplayName("should render array literal for simple values")
    void shouldRenderArrayLiteral_whenSimpleValues() {
      // When
      final var result = handler.format(new Object[] {1, 2, 3});

      // Then
      assertEquals("{1,2,3}", result, "should render comma-separated values in braces");
    }

    /** Verifies that format quotes elements containing a comma. */
    @Test
    @Tag("edge-case")
    @DisplayName("should quote elements containing a comma")
    void shouldQuoteElement_whenContainsComma() {
      // When
      final var result = handler.format(new Object[] {"a,b", "c"});

      // Then
      assertEquals("{\"a,b\",c}", result, "should quote the element containing a comma");
    }

    /** Verifies that format renders NULL for null elements. */
    @Test
    @Tag("edge-case")
    @DisplayName("should render NULL for null elements")
    void shouldRenderNull_whenElementIsNull() {
      // When
      final var result = handler.format(new Object[] {"a", null});

      // Then
      assertEquals("{a,NULL}", result, "should render NULL literal for null element");
    }

    /** Verifies that format renders empty braces for an empty array. */
    @Test
    @Tag("edge-case")
    @DisplayName("should render empty braces for an empty array")
    void shouldRenderEmptyBraces_whenArrayIsEmpty() {
      // When
      final var result = handler.format(new Object[0]);

      // Then
      assertEquals("{}", result, "should render empty braces");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(String) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /** Verifies that parse reads a PostgreSQL array literal. */
    @Test
    @Tag("normal")
    @DisplayName("should parse array literal into elements")
    void shouldParseArrayLiteral_whenBracedContent() {
      // When
      final var result = handler.parse("{a,b,c}");

      // Then
      assertArrayEquals(new Object[] {"a", "b", "c"}, result, "should split braced content");
    }

    /** Verifies that parse honors quoted commas. */
    @Test
    @Tag("edge-case")
    @DisplayName("should keep quoted commas within a single element")
    void shouldKeepQuotedComma_whenElementQuoted() {
      // When
      final var result = handler.parse("{\"a,b\",c}");

      // Then
      assertArrayEquals(new Object[] {"a,b", "c"}, result, "quoted comma should not split");
    }

    /** Verifies that parse honors escaped characters. */
    @Test
    @Tag("edge-case")
    @DisplayName("should unescape escaped characters")
    void shouldUnescape_whenBackslashEscaped() {
      // When
      final var result = handler.parse("{a\\,b,c}");

      // Then
      assertArrayEquals(new Object[] {"a,b", "c"}, result, "escaped comma should not split");
    }

    /** Verifies that parse maps the NULL literal to a null element. */
    @Test
    @Tag("edge-case")
    @DisplayName("should map NULL literal to null element")
    void shouldMapNull_whenNullLiteral() {
      // When
      final var result = handler.parse("{a,NULL}");

      // Then
      assertArrayEquals(new Object[] {"a", null}, result, "NULL literal should become null");
    }

    /** Verifies that parse returns an empty array for empty braces. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty array for empty braces")
    void shouldReturnEmptyArray_whenEmptyBraces() {
      // When
      final var result = handler.parse("{}");

      // Then
      assertEquals(0, result.length, "should return an empty array");
    }

    /** Verifies that parse handles comma-separated values without braces. */
    @Test
    @Tag("edge-case")
    @DisplayName("should parse comma-separated values without braces")
    void shouldParseCommaSeparated_whenNoBraces() {
      // When
      final var result = handler.parse("a, b, c");

      // Then
      assertArrayEquals(new Object[] {"a", "b", "c"}, result, "should split on commas");
    }

    /** Verifies that parse wraps a single value. */
    @Test
    @Tag("edge-case")
    @DisplayName("should wrap a single value")
    void shouldWrapSingleValue_whenNoDelimiter() {
      // When
      final var result = handler.parse("solo");

      // Then
      assertArrayEquals(new Object[] {"solo"}, result, "should wrap the single value");
    }
  }

  /** Tests for the read() method. */
  @Nested
  @DisplayName("read(ResultSet, int) method")
  class ReadMethod {

    /** Tests for the read method. */
    ReadMethod() {}

    /**
     * Verifies that read returns the JDBC array contents.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return object array when value present")
    void shouldReturnObjectArray_whenValuePresent() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      final var sqlArray = mock(Array.class);
      when(resultSet.getArray(1)).thenReturn(sqlArray);
      when(resultSet.wasNull()).thenReturn(false);
      when(sqlArray.getArray()).thenReturn(new Object[] {"a", "b"});

      // When
      final var result = handler.read(resultSet, 1);

      // Then
      assertArrayEquals(new Object[] {"a", "b"}, result, "should return the array contents");
      verify(sqlArray).free();
    }

    /**
     * Verifies that read boxes a primitive int array.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should box a primitive int array")
    void shouldBoxPrimitiveArray_whenIntArrayReturned() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      final var sqlArray = mock(Array.class);
      when(resultSet.getArray(1)).thenReturn(sqlArray);
      when(resultSet.wasNull()).thenReturn(false);
      when(sqlArray.getArray()).thenReturn(new int[] {1, 2, 3});

      // When
      final var result = handler.read(resultSet, 1);

      // Then
      assertArrayEquals(new Object[] {1, 2, 3}, result, "should box the primitive array");
    }

    /**
     * Verifies that read returns null for a null array.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return null when array is null")
    void shouldReturnNull_whenArrayIsNull() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.getArray(1)).thenReturn(null);

      // When
      final var result = handler.read(resultSet, 1);

      // Then
      assertNull(result, "should return null for a null array");
    }
  }

  /** Tests for the write() method. */
  @Nested
  @DisplayName("write(PreparedStatement, int, Object[]) method")
  class WriteMethod {

    /** Tests for the write method. */
    WriteMethod() {}

    /**
     * Verifies that write creates and sets a JDBC array.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should create and set a JDBC array")
    void shouldCreateAndSetArray_whenWriting() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var connection = mock(Connection.class);
      final var sqlArray = mock(Array.class);
      final var value = new Object[] {1, 2, 3};
      when(statement.getConnection()).thenReturn(connection);
      when(connection.createArrayOf(eq("integer"), eq(value))).thenReturn(sqlArray);

      // When
      handler.write(statement, 1, value);

      // Then
      verify(connection).createArrayOf("integer", value);
      verify(statement).setArray(1, sqlArray);
    }
  }
}
