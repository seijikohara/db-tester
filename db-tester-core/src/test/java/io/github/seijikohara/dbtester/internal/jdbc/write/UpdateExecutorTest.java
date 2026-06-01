package io.github.seijikohara.dbtester.internal.jdbc.write;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link UpdateExecutor}. */
@DisplayName("UpdateExecutor")
class UpdateExecutorTest {

  /** Tests for the UpdateExecutor class. */
  UpdateExecutorTest() {}

  /** Mock SQL builder. */
  private SqlBuilder sqlBuilder;

  /** Mock parameter binder. */
  private ParameterBinder parameterBinder;

  /** The executor instance under test. */
  private UpdateExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    sqlBuilder = mock(SqlBuilder.class);
    parameterBinder = mock(ParameterBinder.class);
    executor = new UpdateExecutor(sqlBuilder, parameterBinder);
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
      final var instance = new UpdateExecutor(sqlBuilder, parameterBinder);

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
     * Verifies that execute updates rows for each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should update rows for each table")
    void shouldUpdateRows_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);

      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.name()).thenReturn(new TableName("USERS"));
      when(table.columns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.rows()).thenReturn(List.of(row));
      when(row.value(pkColumn)).thenReturn(new CellValue(1));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(statement).addBatch();
      verify(statement).executeBatch();
    }
  }

  /** Tests for edge cases in execute method. */
  @Nested
  @DisplayName("execute with edge cases")
  class ExecuteEdgeCases {

    /** Tests for edge cases. */
    ExecuteEdgeCases() {}

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
      when(table.rows()).thenReturn(List.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }

    /**
     * Verifies that execute skips tables with only primary key column.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should skip tables with only primary key column")
    void shouldSkipTable_whenOnlyPrimaryKeyColumn() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");

      when(table.rows()).thenReturn(List.of(row));
      when(table.columns()).thenReturn(List.of(pkColumn));

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }
  }

  /** Tests for the tryUpdateRow() method. */
  @Nested
  @DisplayName("tryUpdateRow(String, ColumnName, List, Row, Connection) method")
  class TryUpdateRowMethod {

    /** Tests for the tryUpdateRow method. */
    TryUpdateRowMethod() {}

    /**
     * Verifies that tryUpdateRow returns true when update succeeds.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return true when update affects rows")
    void shouldReturnTrue_whenUpdateAffectsRows() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(row.value(pkColumn)).thenReturn(new CellValue(1));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeUpdate()).thenReturn(1);

      // When
      final var result =
          executor.tryUpdateRow("USERS", pkColumn, List.of(nameColumn), row, connection);

      // Then
      assertTrue(result, "should return true when update affects rows");
    }

    /**
     * Verifies that tryUpdateRow returns false when update affects no rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return false when update affects no rows")
    void shouldReturnFalse_whenUpdateAffectsNoRows() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(row.value(pkColumn)).thenReturn(new CellValue(999));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeUpdate()).thenReturn(0);

      // When
      final var result =
          executor.tryUpdateRow("USERS", pkColumn, List.of(nameColumn), row, connection);

      // Then
      assertFalse(result, "should return false when update affects no rows");
    }

    /**
     * Verifies that tryUpdateRow returns false when no update columns.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when no update columns")
    void shouldReturnFalse_whenNoUpdateColumns() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");

      // When
      final var result = executor.tryUpdateRow("USERS", pkColumn, List.of(), row, connection);

      // Then
      assertFalse(result, "should return false when no update columns");
      verify(connection, never()).prepareStatement(anyString());
    }
  }

  /** Tests for SQLException error handling in execute method. */
  @Nested
  @DisplayName("execute SQLException handling")
  class ExecuteSqlExceptionHandling {

    /** Tests for SQLException handling. */
    ExecuteSqlExceptionHandling() {}

    /**
     * Verifies that exception is thrown when prepareStatement fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when prepareStatement fails")
    void shouldThrowException_whenPrepareStatementFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.name()).thenReturn(new TableName("USERS"));
      when(table.columns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.rows()).thenReturn(List.of(row));
      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString()))
          .thenThrow(new SQLException("Connection closed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.execute(List.of(table), connection),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that exception is thrown when addBatch fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when addBatch fails")
    void shouldThrowException_whenAddBatchFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.name()).thenReturn(new TableName("USERS"));
      when(table.columns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.rows()).thenReturn(List.of(row));
      when(row.value(pkColumn)).thenReturn(new CellValue(1));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));
      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      doThrow(new SQLException("Batch error")).when(statement).addBatch();

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.execute(List.of(table), connection),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that exception is thrown when executeBatch fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when executeBatch fails")
    void shouldThrowException_whenExecuteBatchFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.name()).thenReturn(new TableName("USERS"));
      when(table.columns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.rows()).thenReturn(List.of(row));
      when(row.value(pkColumn)).thenReturn(new CellValue(1));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));
      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeBatch()).thenThrow(new SQLException("Execute batch failed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.execute(List.of(table), connection),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that exception is thrown when setQueryTimeout fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when setQueryTimeout fails")
    void shouldThrowException_whenSetQueryTimeoutFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.name()).thenReturn(new TableName("USERS"));
      when(table.columns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.rows()).thenReturn(List.of(row));
      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      doThrow(new SQLException("Timeout not supported")).when(statement).setQueryTimeout(anyInt());

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.execute(List.of(table), connection, Duration.ofSeconds(30)),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }
  }

  /** Tests for SQLException error handling in tryUpdateRow method. */
  @Nested
  @DisplayName("tryUpdateRow SQLException handling")
  class TryUpdateRowSqlExceptionHandling {

    /** Tests for SQLException handling in tryUpdateRow. */
    TryUpdateRowSqlExceptionHandling() {}

    /**
     * Verifies that exception is thrown when prepareStatement fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when prepareStatement fails")
    void shouldThrowException_whenPrepareStatementFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString()))
          .thenThrow(new SQLException("Connection closed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.tryUpdateRow("USERS", pkColumn, List.of(nameColumn), row, connection),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that exception is thrown when executeUpdate fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should throw DatabaseOperationException when executeUpdate fails")
    void shouldThrowException_whenExecuteUpdateFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(row.value(pkColumn)).thenReturn(new CellValue(1));
      when(row.value(nameColumn)).thenReturn(new CellValue("John"));
      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      when(statement.executeUpdate()).thenThrow(new SQLException("Update failed"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.tryUpdateRow("USERS", pkColumn, List.of(nameColumn), row, connection),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }

    /**
     * Verifies that exception message includes context information.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should include context in exception message")
    void shouldIncludeContext_whenExceptionThrown() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test error"));

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () -> executor.tryUpdateRow("USERS", pkColumn, List.of(nameColumn), row, connection),
              "should throw DatabaseOperationException");
      final var message = exception.getMessage();
      assertTrue(
          message != null && !message.isEmpty(), "exception message should not be null or empty");
    }

    /**
     * Verifies that exception is thrown when setQueryTimeout fails in tryUpdateRow.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName(
        "should throw DatabaseOperationException when setQueryTimeout fails in tryUpdateRow")
    void shouldThrowException_whenSetQueryTimeoutFailsInTryUpdateRow() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);
      doThrow(new SQLException("Timeout not supported")).when(statement).setQueryTimeout(anyInt());

      // When & Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () ->
                  executor.tryUpdateRow(
                      "USERS",
                      pkColumn,
                      List.of(nameColumn),
                      row,
                      connection,
                      Duration.ofSeconds(30)),
              "should throw DatabaseOperationException");
      assertInstanceOf(SQLException.class, exception.getCause(), "cause should be SQLException");
    }
  }
}
