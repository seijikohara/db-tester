package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
      final var stmt = mock(PreparedStatement.class);

      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(pkColumn)).thenReturn(new CellValue(1));
      when(row.getValue(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(stmt);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(stmt).addBatch();
      verify(stmt).executeBatch();
    }
  }

  /** Tests for the updateTable() method. */
  @Nested
  @DisplayName("updateTable(Table, Connection) method")
  class UpdateTableMethod {

    /** Tests for the updateTable method. */
    UpdateTableMethod() {}

    /**
     * Verifies that updateTable skips tables with no rows.
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
      executor.updateTable(table, connection);

      // Then
      verify(connection, never()).prepareStatement(anyString());
    }

    /**
     * Verifies that updateTable skips tables with only primary key column.
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

      when(table.getRows()).thenReturn(List.of(row));
      when(table.getColumns()).thenReturn(List.of(pkColumn));

      // When
      executor.updateTable(table, connection);

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
      final var stmt = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(row.getValue(pkColumn)).thenReturn(new CellValue(1));
      when(row.getValue(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(stmt);
      when(stmt.executeUpdate()).thenReturn(1);

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
      final var stmt = mock(PreparedStatement.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(row.getValue(pkColumn)).thenReturn(new CellValue(999));
      when(row.getValue(nameColumn)).thenReturn(new CellValue("John"));

      when(sqlBuilder.buildUpdate("USERS", pkColumn, List.of(nameColumn)))
          .thenReturn("UPDATE USERS SET NAME = ? WHERE ID = ?");
      when(connection.prepareStatement(anyString())).thenReturn(stmt);
      when(stmt.executeUpdate()).thenReturn(0);

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
}
