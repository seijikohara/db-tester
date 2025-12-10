package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ParameterBinder}. */
@DisplayName("ParameterBinder")
class ParameterBinderTest {

  /** Tests for the ParameterBinder class. */
  ParameterBinderTest() {}

  /** The binder instance under test. */
  private ParameterBinder binder;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    binder = new ParameterBinder();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that default constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with default value parser")
    void shouldCreateInstance_whenCalledWithDefaultConstructor() {
      // When
      final var instance = new ParameterBinder();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with ValueParser creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with custom value parser")
    void shouldCreateInstance_whenCalledWithValueParser() {
      // Given
      final var valueParser = new ValueParser();

      // When
      final var instance = new ParameterBinder(valueParser);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the bind() method. */
  @Nested
  @DisplayName("bind(PreparedStatement, int, CellValue) method")
  class BindMethod {

    /** Tests for the bind method. */
    BindMethod() {}

    /**
     * Verifies that bind sets null when value is null.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set null when value is null")
    void shouldSetNull_whenValueIsNull() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue(null);

      // When
      binder.bind(statement, 1, dataValue);

      // Then
      verify(statement).setNull(eq(1), eq(Types.NULL));
    }

    /**
     * Verifies that bind sets object when value is not null.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set object when value is not null")
    void shouldSetObject_whenValueIsNotNull() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue("test value");

      // When
      binder.bind(statement, 1, dataValue);

      // Then
      verify(statement).setObject(eq(1), eq("test value"));
    }

    /**
     * Verifies that bind sets integer value.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set integer value correctly")
    void shouldSetInteger_whenValueIsInteger() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue(42);

      // When
      binder.bind(statement, 1, dataValue);

      // Then
      verify(statement).setObject(eq(1), eq(42));
    }
  }

  /** Tests for the bindRow() method. */
  @Nested
  @DisplayName("bindRow(PreparedStatement, Row, Collection) method")
  class BindRowMethod {

    /** Tests for the bindRow method. */
    BindRowMethod() {}

    /**
     * Verifies that bindRow sets all column values.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set all column values in order")
    void shouldSetAllColumnValues_whenColumnsProvided() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var column1 = new ColumnName("ID");
      final var column2 = new ColumnName("NAME");
      final var columns = List.of(column1, column2);

      when(row.getValue(column1)).thenReturn(new CellValue(1));
      when(row.getValue(column2)).thenReturn(new CellValue("John"));

      // When
      binder.bindRow(statement, row, columns);

      // Then
      verify(statement).setObject(eq(1), eq(1));
      verify(statement).setObject(eq(2), eq("John"));
    }

    /**
     * Verifies that bindRow handles empty columns.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle empty columns")
    void shouldHandleEmptyColumns_whenNoColumnsProvided() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var columns = List.<ColumnName>of();

      // When
      binder.bindRow(statement, row, columns);

      // Then - no interactions with statement expected
    }
  }

  /** Tests for the bindRowWithTypes() method. */
  @Nested
  @DisplayName("bindRowWithTypes(PreparedStatement, Row, Collection, Map) method")
  class BindRowWithTypesMethod {

    /** Tests for the bindRowWithTypes method. */
    BindRowWithTypesMethod() {}

    /**
     * Verifies that bindRowWithTypes converts types correctly.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert types based on column type map")
    void shouldConvertTypes_whenColumnTypesProvided() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var column1 = new ColumnName("ID");
      final var column2 = new ColumnName("NAME");
      final var columns = List.of(column1, column2);
      final var columnTypes = Map.of("ID", Types.INTEGER, "NAME", Types.VARCHAR);

      when(row.getValue(column1)).thenReturn(new CellValue("42"));
      when(row.getValue(column2)).thenReturn(new CellValue("John"));

      // When
      binder.bindRowWithTypes(statement, row, columns, columnTypes);

      // Then
      verify(statement).setInt(eq(1), eq(42));
      verify(statement).setObject(eq(2), eq("John"));
    }

    /**
     * Verifies that bindRowWithTypes handles case-insensitive column names.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle case-insensitive column names")
    void shouldHandleCaseInsensitiveColumnNames_whenMixedCase() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var column1 = new ColumnName("id");
      final var columns = List.of(column1);
      final var columnTypes = Map.of("ID", Types.BIGINT);

      when(row.getValue(column1)).thenReturn(new CellValue("12345"));

      // When
      binder.bindRowWithTypes(statement, row, columns, columnTypes);

      // Then
      verify(statement).setLong(eq(1), eq(12345L));
    }

    /**
     * Verifies that bindRowWithTypes defaults to VARCHAR for unknown columns.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should default to VARCHAR for unknown columns")
    void shouldDefaultToVarchar_whenColumnTypeNotFound() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var column1 = new ColumnName("UNKNOWN");
      final var columns = List.of(column1);
      final var columnTypes = Map.<String, Integer>of();

      when(row.getValue(column1)).thenReturn(new CellValue("value"));

      // When
      binder.bindRowWithTypes(statement, row, columns, columnTypes);

      // Then
      verify(statement).setObject(eq(1), eq("value"));
    }
  }

  /** Tests for the bindWithType() method. */
  @Nested
  @DisplayName("bindWithType(PreparedStatement, int, CellValue, int) method")
  class BindWithTypeMethod {

    /** Tests for the bindWithType method. */
    BindWithTypeMethod() {}

    /**
     * Verifies that bindWithType sets null with correct SQL type.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set null with correct SQL type when value is null")
    void shouldSetNullWithType_whenValueIsNull() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue(null);

      // When
      binder.bindWithType(statement, 1, dataValue, Types.INTEGER);

      // Then
      verify(statement).setNull(eq(1), eq(Types.INTEGER));
    }

    /**
     * Verifies that bindWithType sets object for non-string values.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should set object directly for non-string values")
    void shouldSetObject_whenValueIsNotString() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue(42);

      // When
      binder.bindWithType(statement, 1, dataValue, Types.INTEGER);

      // Then
      verify(statement).setObject(eq(1), eq(42));
    }

    /**
     * Verifies that bindWithType converts string to integer.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert string to integer when SQL type is INTEGER")
    void shouldConvertToInteger_whenSqlTypeIsInteger() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue("42");

      // When
      binder.bindWithType(statement, 1, dataValue, Types.INTEGER);

      // Then
      verify(statement).setInt(eq(1), eq(42));
    }

    /**
     * Verifies that bindWithType converts string to long.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert string to long when SQL type is BIGINT")
    void shouldConvertToLong_whenSqlTypeIsBigint() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue("9223372036854775807");

      // When
      binder.bindWithType(statement, 1, dataValue, Types.BIGINT);

      // Then
      verify(statement).setLong(eq(1), eq(Long.MAX_VALUE));
    }

    /**
     * Verifies that bindWithType converts string to boolean.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert string to boolean when SQL type is BOOLEAN")
    void shouldConvertToBoolean_whenSqlTypeIsBoolean() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue("true");

      // When
      binder.bindWithType(statement, 1, dataValue, Types.BOOLEAN);

      // Then
      verify(statement).setBoolean(eq(1), eq(true));
    }

    /**
     * Verifies that bindWithType falls back to setObject on parse error.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should fall back to setObject when parsing fails")
    void shouldFallbackToSetObject_whenParsingFails() throws SQLException {
      // Given
      final var statement = mock(PreparedStatement.class);
      final var dataValue = new CellValue("not-a-number");

      // When
      binder.bindWithType(statement, 1, dataValue, Types.INTEGER);

      // Then
      verify(statement).setObject(eq(1), eq("not-a-number"));
    }
  }

  /** Tests for the extractColumnTypes() method. */
  @Nested
  @DisplayName("extractColumnTypes(ResultSetMetaData) method")
  class ExtractColumnTypesMethod {

    /** Tests for the extractColumnTypes method. */
    ExtractColumnTypesMethod() {}

    /**
     * Verifies that extractColumnTypes extracts column types correctly.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should extract column types from metadata")
    void shouldExtractColumnTypes_whenMetadataProvided() throws SQLException {
      // Given
      final var metaData = mock(ResultSetMetaData.class);
      when(metaData.getColumnCount()).thenReturn(3);
      when(metaData.getColumnName(1)).thenReturn("ID");
      when(metaData.getColumnName(2)).thenReturn("NAME");
      when(metaData.getColumnName(3)).thenReturn("ACTIVE");
      when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
      when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
      when(metaData.getColumnType(3)).thenReturn(Types.BOOLEAN);

      // When
      final var result = binder.extractColumnTypes(metaData);

      // Then
      assertEquals(3, result.size(), "should have 3 column types");
      assertEquals(Types.INTEGER, result.get("ID"), "ID should be INTEGER");
      assertEquals(Types.VARCHAR, result.get("NAME"), "NAME should be VARCHAR");
      assertEquals(Types.BOOLEAN, result.get("ACTIVE"), "ACTIVE should be BOOLEAN");
    }

    /**
     * Verifies that extractColumnTypes converts column names to uppercase.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should convert column names to uppercase")
    void shouldConvertToUppercase_whenColumnNamesAreLowercase() throws SQLException {
      // Given
      final var metaData = mock(ResultSetMetaData.class);
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnName(1)).thenReturn("lowercase_column");
      when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);

      // When
      final var result = binder.extractColumnTypes(metaData);

      // Then
      assertEquals(Types.VARCHAR, result.get("LOWERCASE_COLUMN"), "should be stored in uppercase");
    }

    /**
     * Verifies that extractColumnTypes handles empty metadata.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty map when no columns")
    void shouldReturnEmptyMap_whenNoColumns() throws SQLException {
      // Given
      final var metaData = mock(ResultSetMetaData.class);
      when(metaData.getColumnCount()).thenReturn(0);

      // When
      final var result = binder.extractColumnTypes(metaData);

      // Then
      assertEquals(0, result.size(), "should return empty map");
    }
  }
}
