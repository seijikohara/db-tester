package io.github.seijikohara.dbtester.internal.jdbc.write;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InsertExecutor}. */
@DisplayName("InsertExecutor")
class InsertExecutorTest {

  /** Tests for the InsertExecutor class. */
  InsertExecutorTest() {}

  /** Mock SQL builder. */
  private SqlBuilder sqlBuilder;

  /** Mock parameter binder. */
  private ParameterBinder parameterBinder;

  /** The executor instance under test. */
  private InsertExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    sqlBuilder = mock(SqlBuilder.class);
    parameterBinder = mock(ParameterBinder.class);
    executor = new InsertExecutor(sqlBuilder, parameterBinder);
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with dependencies")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance = new InsertExecutor(sqlBuilder, parameterBinder);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute() method. */
  @Nested
  @DisplayName("execute(List, Connection) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /**
     * Verifies that execute inserts rows for each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should insert rows for each table")
    void shouldInsertRows_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);

      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(columnName)).thenReturn(new CellValue(1));

      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");

      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(statement).addBatch();
      verify(statement).executeBatch();
    }

    /**
     * Verifies that execute handles empty tables list.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle empty tables list")
    void shouldHandleEmptyTables_whenNoTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);

      // When
      executor.execute(List.of(), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }
  }

  /** Tests for tables with no rows. */
  @Nested
  @DisplayName("execute with empty tables")
  class ExecuteWithEmptyTables {

    /** Tests for empty table handling. */
    ExecuteWithEmptyTables() {}

    /**
     * Verifies that execute skips tables with no rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should skip tables with no rows")
    void shouldSkipTable_whenNoRows() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      when(table.getRows()).thenReturn(List.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }
  }

  /** Tests for the insertRow() method. */
  @Nested
  @DisplayName("insertRow(Table, Row, Connection) method")
  class InsertRowMethod {

    /** Tests for the insertRow method. */
    InsertRowMethod() {}

    /**
     * Verifies that insertRow inserts a single row.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should insert a single row")
    void shouldInsertRow_whenRowProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");

      when(table.getColumns()).thenReturn(List.of(columnName));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(connection.prepareStatement(anyString())).thenReturn(statement);

      // When
      executor.insertRow(table, row, connection);

      // Then
      verify(parameterBinder).bindRow(eq(statement), eq(row), any());
      verify(statement).executeUpdate();
    }

    /**
     * Verifies that insertRow throws exception when prepareStatement fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when prepareStatement fails")
    void shouldThrowException_whenPrepareStatementFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var cause = new SQLException("Connection refused");

      when(table.getColumns()).thenReturn(List.of(new ColumnName("ID")));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(connection.prepareStatement(anyString())).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.insertRow(table, row, connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that insertRow throws exception when executeUpdate fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when executeUpdate fails")
    void shouldThrowException_whenExecuteUpdateFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var cause = new SQLException("Duplicate key");

      when(table.getColumns()).thenReturn(List.of(new ColumnName("ID")));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeUpdate()).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.insertRow(table, row, connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that insertRow throws exception when setQueryTimeout fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when setQueryTimeout fails")
    void shouldThrowException_whenSetQueryTimeoutFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var timeout = Duration.ofSeconds(30);
      final var cause = new SQLException("Timeout not supported");

      when(table.getColumns()).thenReturn(List.of(new ColumnName("ID")));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      doThrow(cause).when(statement).setQueryTimeout(anyInt());

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.insertRow(table, row, connection, timeout));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }
  }

  /** Tests for SQLException handling in execute method. */
  @Nested
  @DisplayName("execute SQLException handling")
  class ExecuteSqlExceptionHandling {

    /** Tests for SQLException handling. */
    ExecuteSqlExceptionHandling() {}

    /**
     * Verifies that execute throws exception when prepareStatement for metadata fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when metadata prepareStatement fails")
    void shouldThrowException_whenMetadataPrepareStatementFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Connection lost");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(mock(Row.class)));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement(anyString())).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that execute throws exception when executeQuery for metadata fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when metadata executeQuery fails")
    void shouldThrowException_whenMetadataExecuteQueryFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Query execution failed");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(mock(Row.class)));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement(anyString())).thenReturn(metadataStatement);
      when(metadataStatement.executeQuery()).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that execute throws exception when getMetaData fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when getMetaData fails")
    void shouldThrowException_whenGetMetaDataFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Metadata unavailable");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(mock(Row.class)));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement(anyString())).thenReturn(metadataStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that execute throws exception when executeBatch fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when executeBatch fails")
    void shouldThrowException_whenExecuteBatchFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Batch execution failed");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(columnName)).thenReturn(new CellValue(1));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());
      when(insertStatement.executeBatch()).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that execute throws exception when addBatch fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when addBatch fails")
    void shouldThrowException_whenAddBatchFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Batch add failed");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(columnName)).thenReturn(new CellValue(1));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());
      doThrow(cause).when(insertStatement).addBatch();

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that execute throws exception when setQueryTimeout fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when execute setQueryTimeout fails")
    void shouldThrowException_whenExecuteSetQueryTimeoutFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");
      final var timeout = Duration.ofSeconds(30);
      final var cause = new SQLException("Timeout not supported");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(row));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());
      doThrow(cause).when(insertStatement).setQueryTimeout(anyInt());

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.execute(List.of(table), connection, timeout));

      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies exception message contains useful context.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should include context in exception message")
    void shouldIncludeContext_whenExceptionThrown() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");
      final var cause = new SQLException("Connection refused");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(mock(Row.class)));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement(anyString())).thenThrow(cause);

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class, () -> executor.execute(List.of(table), connection));

      assertTrue(
          exception.getMessage() != null && !exception.getMessage().isEmpty(),
          "exception message should not be empty");
    }
  }

  /** Tests for the execute() method with batch size control. */
  @Nested
  @DisplayName("execute(List, Connection, Duration, int) method")
  class ExecuteWithBatchSizeMethod {

    /** Tests for the execute method with batch size. */
    ExecuteWithBatchSizeMethod() {}

    /**
     * Verifies that execute flushes batch every N rows when batchSize is positive.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should flush batch every N rows when batchSize is positive")
    void shouldFlushBatchEveryNRows_whenBatchSizeIsPositive() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");

      final var rows =
          IntStream.range(0, 5)
              .mapToObj(
                  i -> {
                    final var row = mock(Row.class);
                    when(row.getValue(columnName)).thenReturn(new CellValue(i));
                    return row;
                  })
              .toList();

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(rows);
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());

      // When: 5 rows with batchSize=2 → flush at rows 2, 4, then remainder at 5
      executor.execute(List.of(table), connection, null, 2);

      // Then: 3 executeBatch calls (at index 1, 3, and final remainder)
      verify(insertStatement, times(5)).addBatch();
      verify(insertStatement, times(3)).executeBatch();
    }

    /**
     * Verifies that execute uses single batch when batchSize is zero.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use single batch when batchSize is zero")
    void shouldUseSingleBatch_whenBatchSizeIsZero() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");

      final var rows =
          IntStream.range(0, 3)
              .mapToObj(
                  i -> {
                    final var row = mock(Row.class);
                    when(row.getValue(columnName)).thenReturn(new CellValue(i));
                    return row;
                  })
              .toList();

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(rows);
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());

      // When: batchSize=0 → single batch
      executor.execute(List.of(table), connection, null, 0);

      // Then: 1 executeBatch call
      verify(insertStatement, times(3)).addBatch();
      verify(insertStatement, times(1)).executeBatch();
    }

    /**
     * Verifies that execute flushes exactly once when rows are evenly divisible by batchSize.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should flush exactly when rows are evenly divisible by batchSize")
    void shouldFlushExactly_whenRowsEvenlyDivisibleByBatchSize() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var metadataStatement = mock(PreparedStatement.class);
      final var insertStatement = mock(PreparedStatement.class);
      final var resultSet = mock(ResultSet.class);
      final var metaData = mock(ResultSetMetaData.class);
      final var table = mock(Table.class);
      final var columnName = new ColumnName("ID");

      final var rows =
          IntStream.range(0, 4)
              .mapToObj(
                  i -> {
                    final var row = mock(Row.class);
                    when(row.getValue(columnName)).thenReturn(new CellValue(i));
                    return row;
                  })
              .toList();

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(rows);
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(sqlBuilder.buildMetadataQuery("USERS")).thenReturn("SELECT * FROM USERS WHERE 1=0");
      when(connection.prepareStatement("SELECT * FROM USERS WHERE 1=0"))
          .thenReturn(metadataStatement);
      when(connection.prepareStatement("INSERT INTO USERS (ID) VALUES (?)"))
          .thenReturn(insertStatement);
      when(metadataStatement.executeQuery()).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());

      // When: 4 rows with batchSize=2 → flush at rows 2 and 4 (no remainder)
      executor.execute(List.of(table), connection, null, 2);

      // Then: 2 executeBatch calls (at index 1 and 3)
      verify(insertStatement, times(4)).addBatch();
      verify(insertStatement, times(2)).executeBatch();
    }
  }
}
