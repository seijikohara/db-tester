package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link OperationExecutor}. */
@DisplayName("OperationExecutor")
class OperationExecutorTest {

  /** Tests for the OperationExecutor class. */
  OperationExecutorTest() {}

  /** Mock insert executor. */
  private InsertExecutor insertExecutor;

  /** Mock update executor. */
  private UpdateExecutor updateExecutor;

  /** Mock delete executor. */
  private DeleteExecutor deleteExecutor;

  /** Mock truncate executor. */
  private TruncateExecutor truncateExecutor;

  /** Mock refresh executor. */
  private RefreshExecutor refreshExecutor;

  /** The executor instance under test. */
  private OperationExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    insertExecutor = mock(InsertExecutor.class);
    updateExecutor = mock(UpdateExecutor.class);
    deleteExecutor = mock(DeleteExecutor.class);
    truncateExecutor = mock(TruncateExecutor.class);
    refreshExecutor = mock(RefreshExecutor.class);
    executor =
        new OperationExecutor(
            insertExecutor, updateExecutor, deleteExecutor, truncateExecutor, refreshExecutor);
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
    @DisplayName("should create instance with default dependencies")
    void shouldCreateInstance_whenDefaultConstructorUsed() {
      // When
      final var instance = new OperationExecutor();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependencies creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance with provided dependencies")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance =
          new OperationExecutor(
              insertExecutor, updateExecutor, deleteExecutor, truncateExecutor, refreshExecutor);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute() method. */
  @Nested
  @DisplayName("execute(Operation, DataSet, DataSource) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /**
     * Verifies that execute commits transaction on success.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should commit transaction on success")
    void shouldCommitTransaction_whenOperationSucceeds() throws SQLException {
      // Given
      final var dataSource = mock(DataSource.class);
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);

      when(dataSource.getConnection()).thenReturn(connection);
      when(dataSet.getTables()).thenReturn(List.of());

      // When
      executor.execute(Operation.NONE, dataSet, dataSource);

      // Then
      verify(connection).setAutoCommit(false);
      verify(connection).commit();
      verify(connection, never()).rollback();
    }

    /**
     * Verifies that execute rolls back transaction on failure.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should rollback transaction on failure")
    void shouldRollbackTransaction_whenOperationFails() throws SQLException {
      // Given
      final var dataSource = mock(DataSource.class);
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var table = mock(Table.class);

      when(dataSource.getConnection()).thenReturn(connection);
      when(dataSet.getTables()).thenReturn(List.of(table));
      doThrow(new SQLException("Insert failed")).when(insertExecutor).execute(any(), any());

      // When & Then
      assertThrows(
          DatabaseTesterException.class,
          () -> executor.execute(Operation.INSERT, dataSet, dataSource));
      verify(connection).rollback();
      verify(connection, never()).commit();
    }
  }

  /** Tests for the executeOperation() method. */
  @Nested
  @DisplayName("executeOperation(Operation, DataSet, Connection) method")
  class ExecuteOperationMethod {

    /** Tests for the executeOperation method. */
    ExecuteOperationMethod() {}

    /**
     * Verifies that NONE operation does nothing.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should do nothing for NONE operation")
    void shouldDoNothing_whenNoneOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);

      // When
      executor.executeOperation(Operation.NONE, dataSet, connection);

      // Then
      verify(insertExecutor, never()).execute(any(), any());
      verify(updateExecutor, never()).execute(any(), any());
      verify(deleteExecutor, never()).execute(any(), any());
      verify(truncateExecutor, never()).execute(any(), any());
      verify(refreshExecutor, never()).execute(any(), any());
    }

    /**
     * Verifies that INSERT operation delegates to insert executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to insert executor for INSERT operation")
    void shouldDelegateToInsertExecutor_whenInsertOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.INSERT, dataSet, connection);

      // Then
      verify(insertExecutor).execute(tables, connection);
    }

    /**
     * Verifies that UPDATE operation delegates to update executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to update executor for UPDATE operation")
    void shouldDelegateToUpdateExecutor_whenUpdateOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.UPDATE, dataSet, connection);

      // Then
      verify(updateExecutor).execute(tables, connection);
    }

    /**
     * Verifies that DELETE operation delegates to delete executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to delete executor for DELETE operation")
    void shouldDelegateToDeleteExecutor_whenDeleteOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.DELETE, dataSet, connection);

      // Then
      verify(deleteExecutor).execute(tables, connection);
    }

    /**
     * Verifies that DELETE_ALL operation delegates to delete executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to delete executor for DELETE_ALL operation")
    void shouldDelegateToDeleteExecutor_whenDeleteAllOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.DELETE_ALL, dataSet, connection);

      // Then
      verify(deleteExecutor).executeDeleteAll(tables, connection);
    }

    /**
     * Verifies that REFRESH operation delegates to refresh executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to refresh executor for REFRESH operation")
    void shouldDelegateToRefreshExecutor_whenRefreshOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.REFRESH, dataSet, connection);

      // Then
      verify(refreshExecutor).execute(tables, connection);
    }

    /**
     * Verifies that TRUNCATE_TABLE operation delegates to truncate executor.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to truncate executor for TRUNCATE_TABLE operation")
    void shouldDelegateToTruncateExecutor_whenTruncateOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.TRUNCATE_TABLE, dataSet, connection);

      // Then
      verify(truncateExecutor).execute(tables, connection);
    }

    /**
     * Verifies that CLEAN_INSERT operation deletes all then inserts.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should delete all then insert for CLEAN_INSERT operation")
    void shouldDeleteAllThenInsert_whenCleanInsertOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var table = mock(Table.class);
      final var tables = List.of(table);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.CLEAN_INSERT, dataSet, connection);

      // Then
      verify(deleteExecutor).executeDeleteAll(tables.reversed(), connection);
      verify(insertExecutor).execute(tables, connection);
    }

    /**
     * Verifies that TRUNCATE_INSERT operation truncates then inserts.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should truncate then insert for TRUNCATE_INSERT operation")
    void shouldTruncateThenInsert_whenTruncateInsertOperation() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(DataSet.class);
      final var table = mock(Table.class);
      final var tables = List.of(table);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(Operation.TRUNCATE_INSERT, dataSet, connection);

      // Then
      verify(truncateExecutor).execute(tables.reversed(), connection);
      verify(insertExecutor).execute(tables, connection);
    }
  }
}
