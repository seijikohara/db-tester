package io.github.seijikohara.dbtester.internal.jdbc.read;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableReader}. */
@DisplayName("TableReader")
class TableReaderTest {

  /** Tests for the TableReader class. */
  TableReaderTest() {}

  /** Mock data source. */
  private DataSource dataSource;

  /** Mock connection. */
  private Connection connection;

  /** Mock prepared statement. */
  private PreparedStatement statement;

  /** Mock result set. */
  private ResultSet resultSet;

  /** Mock result set metadata. */
  private ResultSetMetaData metaData;

  /** Mock type converter. */
  private TypeConverter typeConverter;

  /** The reader instance under test. */
  private TableReader reader;

  /**
   * Sets up test fixtures before each test.
   *
   * @throws SQLException if a database error occurs
   */
  @BeforeEach
  void setUp() throws SQLException {
    dataSource = mock(DataSource.class);
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    metaData = mock(ResultSetMetaData.class);
    typeConverter = mock(TypeConverter.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.getMetaData()).thenReturn(metaData);

    reader = new TableReader(typeConverter);
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
    @DisplayName("should create instance with default constructor")
    void shouldCreateInstance_whenDefaultConstructorUsed() {
      // When
      final var instance = new TableReader();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with type converter creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with type converter")
    void shouldCreateInstance_whenTypeConverterProvided() {
      // Given
      final var converter = new TypeConverter();

      // When
      final var instance = new TableReader(converter);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the fetchTable(DataSource, String) method. */
  @Nested
  @DisplayName("fetchTable(DataSource, String) method")
  class FetchTableMethod {

    /** Tests for the fetchTable method. */
    FetchTableMethod() {}

    /**
     * Verifies that fetchTable returns table with data.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return table with data when rows exist")
    void shouldReturnTableWithData_whenRowsExist() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(2);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(metaData.getColumnLabel(2)).thenReturn("NAME");
      when(resultSet.next()).thenReturn(true, false);
      when(resultSet.getObject(1)).thenReturn(1);
      when(resultSet.getObject(2)).thenReturn("John");
      when(typeConverter.convert(1)).thenReturn(1);
      when(typeConverter.convert("John")).thenReturn("John");

      // When
      final var result = reader.fetchTable(dataSource, "USERS");

      // Then
      assertAll(
          "fetch table results",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals("USERS", result.getName().value(), "table name should match"),
          () -> assertEquals(2, result.getColumns().size(), "should have 2 columns"),
          () -> assertEquals(1, result.getRows().size(), "should have 1 row"));
    }

    /**
     * Verifies that fetchTable returns empty table when no rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty table when no rows exist")
    void shouldReturnEmptyTable_whenNoRowsExist() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(2);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(metaData.getColumnLabel(2)).thenReturn("NAME");
      when(resultSet.next()).thenReturn(false);

      // When
      final var result = reader.fetchTable(dataSource, "USERS");

      // Then
      assertAll(
          "empty table results",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals("USERS", result.getName().value(), "table name should match"),
          () -> assertEquals(2, result.getColumns().size(), "should have 2 columns"),
          () -> assertTrue(result.getRows().isEmpty(), "should have no rows"));
    }

    /**
     * Verifies that fetchTable handles null values.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null values correctly")
    void shouldHandleNullValues_whenColumnValueIsNull() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(2);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(metaData.getColumnLabel(2)).thenReturn("NAME");
      when(resultSet.next()).thenReturn(true, false);
      when(resultSet.getObject(1)).thenReturn(1);
      when(resultSet.getObject(2)).thenReturn(null);
      when(typeConverter.convert(1)).thenReturn(1);
      when(typeConverter.convert(null)).thenReturn(null);

      // When
      final var result = reader.fetchTable(dataSource, "USERS");

      // Then
      assertAll(
          "null value handling",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getRows().size(), "should have 1 row"),
          () -> {
            final var row = result.getRows().getFirst();
            assertEquals(
                CellValue.NULL,
                row.getValue(new ColumnName("NAME")),
                "null value should be CellValue.NULL");
          });
    }

    /**
     * Verifies that fetchTable throws exception on connection error.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when connection fails")
    void shouldThrowException_whenConnectionFails() throws SQLException {
      // Given
      when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.fetchTable(dataSource, "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "exception verification",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that fetchTable uses correct SQL.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use SELECT * FROM table SQL")
    void shouldUseCorrectSql_whenFetchingTable() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenReturn(false);

      // When
      reader.fetchTable(dataSource, "USERS");

      // Then
      verify(connection).prepareStatement("SELECT * FROM USERS");
    }
  }

  /** Tests for the fetchTable(DataSource, String, Collection) method. */
  @Nested
  @DisplayName("fetchTable(DataSource, String, Collection) method")
  class FetchTableWithColumnsMethod {

    /** Tests for the fetchTable with columns method. */
    FetchTableWithColumnsMethod() {}

    /**
     * Verifies that fetchTable with columns uses correct SQL.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use SELECT with specified columns")
    void shouldUseCorrectSql_whenColumnsSpecified() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(2);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(metaData.getColumnLabel(2)).thenReturn("NAME");
      when(resultSet.next()).thenReturn(false);

      final var columns = List.of(new ColumnName("ID"), new ColumnName("NAME"));

      // When
      reader.fetchTable(dataSource, "USERS", columns);

      // Then
      verify(connection).prepareStatement("SELECT ID, NAME FROM USERS");
    }

    /**
     * Verifies that fetchTable uses SELECT * when columns is empty.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should use SELECT * when columns is empty")
    void shouldUseSelectAll_whenColumnsEmpty() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenReturn(false);

      // When
      reader.fetchTable(dataSource, "USERS", List.of());

      // Then
      verify(connection).prepareStatement("SELECT * FROM USERS");
    }
  }

  /** Tests for the fetchDataSet method. */
  @Nested
  @DisplayName("fetchDataSet(DataSource, List) method")
  class FetchDataSetMethod {

    /** Tests for the fetchDataSet method. */
    FetchDataSetMethod() {}

    /**
     * Verifies that fetchDataSet returns dataset with multiple tables.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset with multiple tables")
    void shouldReturnDataSet_whenMultipleTablesProvided() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenReturn(false);

      // When
      final var result = reader.fetchTableSet(dataSource, List.of("USERS", "PRODUCTS"));

      // Then
      final var tableNames = result.getTables().stream().map(t -> t.getName().value()).toList();
      assertAll(
          "fetch dataset results",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(2, result.getTables().size(), "should have 2 tables"),
          () -> assertTrue(tableNames.contains("USERS"), "should contain USERS table"),
          () -> assertTrue(tableNames.contains("PRODUCTS"), "should contain PRODUCTS table"));
    }
  }

  /** Tests for the executeQuery method. */
  @Nested
  @DisplayName("executeQuery(DataSource, String, String) method")
  class ExecuteQueryMethod {

    /** Tests for the executeQuery method. */
    ExecuteQueryMethod() {}

    /**
     * Verifies that executeQuery handles multiple rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple rows")
    void shouldHandleMultipleRows_whenResultSetHasMultipleRows() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenReturn(true, true, true, false);
      when(resultSet.getObject(1)).thenReturn(1, 2, 3);
      when(typeConverter.convert(1)).thenReturn(1);
      when(typeConverter.convert(2)).thenReturn(2);
      when(typeConverter.convert(3)).thenReturn(3);

      // When
      final var result = reader.executeQuery(dataSource, "SELECT ID FROM USERS", "USERS");

      // Then
      assertEquals(3, result.getRows().size(), "should have 3 rows");
    }

    /**
     * Verifies that executeQuery throws exception on column label retrieval failure.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when column label retrieval fails")
    void shouldThrowException_whenColumnLabelRetrievalFails() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenThrow(new SQLException("Column label error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT ID FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "column label retrieval exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to retrieve column label"),
                  "exception message should indicate column label failure"));
    }

    /**
     * Verifies that executeQuery throws exception on column read failure.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when column read fails")
    void shouldThrowException_whenColumnReadFails() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenReturn(true);
      when(resultSet.getObject(1)).thenThrow(new SQLException("Read error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT ID FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "column read exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to read column"),
                  "exception message should indicate column read failure"));
    }

    /**
     * Verifies that executeQuery converts LOB values.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert LOB values through type converter")
    void shouldConvertLobValues_whenLobColumnExists() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("DATA");
      when(resultSet.next()).thenReturn(true, false);
      final var mockBlob = new byte[] {1, 2, 3};
      when(resultSet.getObject(1)).thenReturn(mockBlob);
      when(typeConverter.convert(mockBlob)).thenReturn("[BASE64]AQID");

      // When
      final var result = reader.executeQuery(dataSource, "SELECT DATA FROM BLOBS", "BLOBS");

      // Then
      assertEquals(1, result.getRows().size(), "should have 1 row");
      verify(typeConverter).convert(mockBlob);
    }

    /**
     * Verifies that exception is thrown when prepareStatement fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when prepareStatement fails")
    void shouldThrowException_whenPrepareStatementFails() throws SQLException {
      // Given
      when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Statement error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT * FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "prepareStatement exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that exception is thrown when executeQuery fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when executeQuery on statement fails")
    void shouldThrowException_whenExecuteQueryOnStatementFails() throws SQLException {
      // Given
      when(statement.executeQuery()).thenThrow(new SQLException("Query failed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT * FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "executeQuery exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that exception is thrown when getMetaData fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when getMetaData fails")
    void shouldThrowException_whenGetMetaDataFails() throws SQLException {
      // Given
      when(resultSet.getMetaData()).thenThrow(new SQLException("Metadata error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT * FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "getMetaData exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that exception is thrown when getColumnCount fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when getColumnCount fails")
    void shouldThrowException_whenGetColumnCountFails() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenThrow(new SQLException("Column count error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT * FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "getColumnCount exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that exception is thrown when resultSet.next fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when resultSet.next fails")
    void shouldThrowException_whenResultSetNextFails() throws SQLException {
      // Given
      when(metaData.getColumnCount()).thenReturn(1);
      when(metaData.getColumnLabel(1)).thenReturn("ID");
      when(resultSet.next()).thenThrow(new SQLException("Cursor error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.executeQuery(dataSource, "SELECT * FROM USERS", "USERS"),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "resultSet.next exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }

    /**
     * Verifies that exception is thrown when setQueryTimeout fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when setQueryTimeout fails")
    void shouldThrowException_whenSetQueryTimeoutFails() throws SQLException {
      // Given
      doThrow(new SQLException("Timeout not supported")).when(statement).setQueryTimeout(anyInt());

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.fetchTable(dataSource, "USERS", java.time.Duration.ofSeconds(30)),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertAll(
          "setQueryTimeout exception",
          () -> assertNotNull(message, "exception message should not be null"),
          () ->
              assertTrue(
                  message != null && message.contains("Failed to execute query"),
                  "exception message should indicate query failure"));
    }
  }

  /** Tests for SQLException error handling in fetchTableSet method. */
  @Nested
  @DisplayName("fetchTableSet SQLException handling")
  class FetchTableSetSqlExceptionHandling {

    /** Tests for SQLException handling in fetchTableSet. */
    FetchTableSetSqlExceptionHandling() {}

    /**
     * Verifies that exception is thrown when connection fails for one table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when connection fails during fetchTableSet")
    void shouldThrowException_whenConnectionFailsDuringFetchTableSet() throws SQLException {
      // Given
      when(dataSource.getConnection()).thenThrow(new SQLException("Connection pool exhausted"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseTesterException.class,
              () -> reader.fetchTableSet(dataSource, List.of("USERS", "PRODUCTS")),
              "should throw DatabaseTesterException");
      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("Failed to execute query"),
          "exception message should indicate query failure");
    }
  }
}
