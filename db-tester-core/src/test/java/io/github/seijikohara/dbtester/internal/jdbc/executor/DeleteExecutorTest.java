package io.github.seijikohara.dbtester.internal.jdbc.executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.jdbc.ParameterBinder;
import io.github.seijikohara.dbtester.internal.jdbc.SqlBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DeleteExecutor}. */
@DisplayName("DeleteExecutor")
class DeleteExecutorTest {

  /** Tests for the DeleteExecutor class. */
  DeleteExecutorTest() {}

  /** Mock SQL builder. */
  private SqlBuilder sqlBuilder;

  /** Mock parameter binder. */
  private ParameterBinder parameterBinder;

  /** The executor instance under test. */
  private DeleteExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    sqlBuilder = mock(SqlBuilder.class);
    parameterBinder = mock(ParameterBinder.class);
    executor = new DeleteExecutor(sqlBuilder, parameterBinder);
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
      final var instance = new DeleteExecutor(sqlBuilder, parameterBinder);

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
     * Verifies that execute deletes rows for each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delete rows for each table")
    void shouldDeleteRows_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(PreparedStatement.class);

      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(columnName));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(columnName)).thenReturn(new CellValue(1));

      when(sqlBuilder.buildDelete("USERS", columnName))
          .thenReturn("DELETE FROM USERS WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(statement);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(parameterBinder).bind(eq(statement), eq(1), eq(new CellValue(1)));
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
      when(table.getRows()).thenReturn(List.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }

    /**
     * Verifies that execute skips tables with no columns.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should skip tables with no columns")
    void shouldSkipTable_whenNoColumns() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      when(table.getRows()).thenReturn(List.of(row));
      when(table.getColumns()).thenReturn(List.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }
  }

  /** Tests for the executeDeleteAll() method. */
  @Nested
  @DisplayName("executeDeleteAll(List, Connection) method")
  class ExecuteDeleteAllMethod {

    /** Tests for the executeDeleteAll method. */
    ExecuteDeleteAllMethod() {}

    /**
     * Verifies that executeDeleteAll deletes all rows from each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delete all rows from each table")
    void shouldDeleteAllRows_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(Statement.class);

      final var table = mock(Table.class);
      when(table.getName()).thenReturn(new TableName("USERS"));

      when(sqlBuilder.buildDeleteAll("USERS")).thenReturn("DELETE FROM USERS");
      when(connection.createStatement()).thenReturn(statement);

      // When
      executor.executeDeleteAll(List.of(table), connection);

      // Then
      verify(statement).executeUpdate("DELETE FROM USERS");
    }
  }

  /** Tests for the deleteAllRows() method. */
  @Nested
  @DisplayName("deleteAllRows(String, Connection) method")
  class DeleteAllRowsMethod {

    /** Tests for the deleteAllRows method. */
    DeleteAllRowsMethod() {}

    /**
     * Verifies that deleteAllRows deletes all rows from a table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delete all rows from a table")
    void shouldDeleteAllRows_whenTableNameProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(Statement.class);

      when(sqlBuilder.buildDeleteAll("USERS")).thenReturn("DELETE FROM USERS");
      when(connection.createStatement()).thenReturn(statement);

      // When
      executor.deleteAllRows("USERS", connection);

      // Then
      verify(statement).executeUpdate("DELETE FROM USERS");
    }
  }
}
