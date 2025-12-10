package io.github.seijikohara.dbtester.internal.jdbc.executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RefreshExecutor}. */
@DisplayName("RefreshExecutor")
class RefreshExecutorTest {

  /** Tests for the RefreshExecutor class. */
  RefreshExecutorTest() {}

  /** Mock insert executor. */
  private InsertExecutor insertExecutor;

  /** Mock update executor. */
  private UpdateExecutor updateExecutor;

  /** The executor instance under test. */
  private RefreshExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    insertExecutor = mock(InsertExecutor.class);
    updateExecutor = mock(UpdateExecutor.class);
    executor = new RefreshExecutor(insertExecutor, updateExecutor);
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
      final var instance = new RefreshExecutor(insertExecutor, updateExecutor);

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
     * Verifies that execute refreshes each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should refresh each table")
    void shouldRefreshTables_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);

      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.getRows()).thenReturn(List.of(row));
      when(row.getValue(pkColumn)).thenReturn(new CellValue(1));
      when(row.getValue(nameColumn)).thenReturn(new CellValue("John"));

      when(updateExecutor.tryUpdateRow(anyString(), any(), anyList(), any(), any()))
          .thenReturn(true);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(updateExecutor)
          .tryUpdateRow(
              eq("USERS"), eq(pkColumn), eq(List.of(nameColumn)), eq(row), eq(connection));
    }
  }

  /** Tests for edge cases and specific behaviors in execute method. */
  @Nested
  @DisplayName("execute edge cases and behaviors")
  class ExecuteEdgeCasesAndBehaviors {

    /** Tests for edge cases. */
    ExecuteEdgeCasesAndBehaviors() {}

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
      verify(updateExecutor, never()).tryUpdateRow(anyString(), any(), anyList(), any(), any());
      verify(insertExecutor, never()).insertRow(any(), any(), any());
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
      verify(updateExecutor, never()).tryUpdateRow(anyString(), any(), anyList(), any(), any());
      verify(insertExecutor, never()).insertRow(any(), any(), any());
    }

    /**
     * Verifies that execute only updates when update affects rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should only update when update affects rows")
    void shouldOnlyUpdate_whenUpdateAffectsRows() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.getRows()).thenReturn(List.of(row));

      when(updateExecutor.tryUpdateRow(anyString(), any(), anyList(), any(), any()))
          .thenReturn(true);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(updateExecutor)
          .tryUpdateRow(
              eq("USERS"), eq(pkColumn), eq(List.of(nameColumn)), eq(row), eq(connection));
      verify(insertExecutor, never()).insertRow(any(), any(), any());
    }

    /**
     * Verifies that execute inserts when update affects no rows.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should insert when update affects no rows")
    void shouldInsert_whenUpdateAffectsNoRows() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var pkColumn = new ColumnName("ID");
      final var nameColumn = new ColumnName("NAME");

      when(table.getName()).thenReturn(new TableName("USERS"));
      when(table.getColumns()).thenReturn(List.of(pkColumn, nameColumn));
      when(table.getRows()).thenReturn(List.of(row));

      when(updateExecutor.tryUpdateRow(anyString(), any(), anyList(), any(), any()))
          .thenReturn(false);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(updateExecutor)
          .tryUpdateRow(
              eq("USERS"), eq(pkColumn), eq(List.of(nameColumn)), eq(row), eq(connection));
      verify(insertExecutor).insertRow(eq(table), eq(row), eq(connection));
    }
  }
}
