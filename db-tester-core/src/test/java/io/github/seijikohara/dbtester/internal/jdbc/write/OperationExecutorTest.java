package io.github.seijikohara.dbtester.internal.jdbc.write;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableOrderResolver;
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

  /** Mock table order resolver. */
  private TableOrderResolver tableOrderResolver;

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
    tableOrderResolver = mock(TableOrderResolver.class);
    executor =
        new OperationExecutor(
            insertExecutor,
            updateExecutor,
            deleteExecutor,
            truncateExecutor,
            refreshExecutor,
            tableOrderResolver);
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
              insertExecutor,
              updateExecutor,
              deleteExecutor,
              truncateExecutor,
              refreshExecutor,
              tableOrderResolver);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute() method. */
  @Nested
  @DisplayName("execute(Operation, TableSet, DataSource, TableOrderingStrategy) method")
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
      final var dataSet = mock(TableSet.class);

      when(dataSource.getConnection()).thenReturn(connection);
      when(dataSet.getTables()).thenReturn(List.of());

      // When
      executor.execute(Operation.NONE, dataSet, dataSource, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var table = mock(Table.class);

      when(dataSource.getConnection()).thenReturn(connection);
      when(dataSet.getTables()).thenReturn(List.of(table));
      doThrow(new DatabaseOperationException("Insert failed", new SQLException("Insert failed")))
          .when(insertExecutor)
          .execute(any(), any());

      // When & Then
      assertThrows(
          DatabaseTesterException.class,
          () ->
              executor.execute(
                  Operation.INSERT, dataSet, dataSource, TableOrderingStrategy.ALPHABETICAL));
      verify(connection).rollback();
      verify(connection, never()).commit();
    }
  }

  /** Tests for the executeOperation() method. */
  @Nested
  @DisplayName("executeOperation(Operation, TableSet, Connection, TableOrderingStrategy) method")
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
      final var dataSet = mock(TableSet.class);
      when(dataSet.getTables()).thenReturn(List.of());

      // When
      executor.executeOperation(
          Operation.NONE, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.UPDATE, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.DELETE, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.DELETE_ALL, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.REFRESH, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var tables = List.<Table>of();
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.TRUNCATE_TABLE, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var table = mock(Table.class);
      final var tables = List.of(table);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.CLEAN_INSERT, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

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
      final var dataSet = mock(TableSet.class);
      final var table = mock(Table.class);
      final var tables = List.of(table);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.TRUNCATE_INSERT, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

      // Then
      verify(truncateExecutor).execute(tables.reversed(), connection);
      verify(insertExecutor).execute(tables, connection);
    }
  }

  /** Tests for table ordering strategies. */
  @Nested
  @DisplayName("table ordering strategy tests")
  class TableOrderingStrategyTests {

    /** Tests for table ordering strategies. */
    TableOrderingStrategyTests() {}

    /**
     * Verifies that single table returns as-is regardless of strategy.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return single table as-is")
    void shouldReturnSingleTableAsIs_whenOnlyOneTable() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var table = mock(Table.class);
      final var tables = List.of(table);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.FOREIGN_KEY);

      // Then - tableOrderResolver should not be called for single table
      verify(tableOrderResolver, never()).resolveOrder(any(), any(), any());
      verify(insertExecutor).execute(tables, connection);
    }

    /**
     * Verifies that LOAD_ORDER_FILE strategy uses tables as-is.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use tables as-is for LOAD_ORDER_FILE strategy")
    void shouldUseTablesAsIs_whenLoadOrderFileStrategy() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var table1 = mock(Table.class);
      final var table2 = mock(Table.class);
      final var tables = List.of(table1, table2);
      when(dataSet.getTables()).thenReturn(tables);

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.LOAD_ORDER_FILE);

      // Then - tableOrderResolver should not be called for LOAD_ORDER_FILE
      verify(tableOrderResolver, never()).resolveOrder(any(), any(), any());
      verify(insertExecutor).execute(tables, connection);
    }

    /**
     * Verifies that ALPHABETICAL strategy sorts tables alphabetically.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should sort tables alphabetically for ALPHABETICAL strategy")
    void shouldSortAlphabetically_whenAlphabeticalStrategy() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableC = mock(Table.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      when(tableC.getName()).thenReturn(new TableName("C_TABLE"));
      when(tableA.getName()).thenReturn(new TableName("A_TABLE"));
      when(tableB.getName()).thenReturn(new TableName("B_TABLE"));
      when(dataSet.getTables()).thenReturn(List.of(tableC, tableA, tableB));

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.ALPHABETICAL);

      // Then - verify insert is called (with sorted order, but we verify the sorted list)
      verify(insertExecutor).execute(List.of(tableA, tableB, tableC), connection);
    }

    /**
     * Verifies that FOREIGN_KEY strategy uses table order resolver.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use table order resolver for FOREIGN_KEY strategy")
    void shouldUseTableOrderResolver_whenForeignKeyStrategy() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      // Return reversed order (B before A)
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenReturn(List.of(tableNameB, tableNameA));

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.FOREIGN_KEY);

      // Then - verify insert is called with reordered tables (B, A)
      verify(tableOrderResolver)
          .resolveOrder(List.of(tableNameA, tableNameB), connection, "PUBLIC");
      verify(insertExecutor).execute(List.of(tableB, tableA), connection);
    }

    /**
     * Verifies that FOREIGN_KEY strategy falls back to original order when no dependencies.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should use original order when no foreign key dependencies found")
    void shouldUseOriginalOrder_whenNoForeignKeyDependencies() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      // Return same order (no reordering needed)
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenReturn(List.of(tableNameA, tableNameB));

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.FOREIGN_KEY);

      // Then - verify insert is called with original order
      verify(insertExecutor).execute(List.of(tableA, tableB), connection);
    }

    /**
     * Verifies that FOREIGN_KEY strategy falls back to original order on exception.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should fall back to original order when foreign key resolution fails")
    void shouldFallbackToOriginalOrder_whenForeignKeyResolutionFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenThrow(
              new DatabaseOperationException(
                  "FK resolution failed", new SQLException("Metadata error")));

      // When
      executor.executeOperation(
          Operation.INSERT, dataSet, connection, TableOrderingStrategy.FOREIGN_KEY);

      // Then - verify insert is called with original order (fallback)
      verify(insertExecutor).execute(List.of(tableA, tableB), connection);
    }

    /**
     * Verifies that AUTO strategy uses foreign key resolution.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should use foreign key resolution for AUTO strategy")
    void shouldUseForeignKeyResolution_whenAutoStrategy() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      // Return reversed order (B before A)
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenReturn(List.of(tableNameB, tableNameA));

      // When
      executor.executeOperation(Operation.INSERT, dataSet, connection, TableOrderingStrategy.AUTO);

      // Then - verify insert is called with reordered tables
      verify(insertExecutor).execute(List.of(tableB, tableA), connection);
    }

    /**
     * Verifies that AUTO strategy falls back to original order when FK resolution fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to original order for AUTO when FK resolution fails")
    void shouldFallbackToOriginalOrder_whenAutoStrategyAndForeignKeyFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenThrow(
              new DatabaseOperationException(
                  "FK resolution failed", new SQLException("Metadata error")));

      // When
      executor.executeOperation(Operation.INSERT, dataSet, connection, TableOrderingStrategy.AUTO);

      // Then - verify insert is called with original order (fallback)
      verify(insertExecutor).execute(List.of(tableA, tableB), connection);
    }

    /**
     * Verifies that AUTO strategy returns original order when no FK reordering needed.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return original order for AUTO when no FK reordering needed")
    void shouldReturnOriginalOrder_whenAutoStrategyAndNoReorderingNeeded() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var dataSet = mock(TableSet.class);
      final var tableA = mock(Table.class);
      final var tableB = mock(Table.class);
      final var tableNameA = new TableName("TABLE_A");
      final var tableNameB = new TableName("TABLE_B");
      when(tableA.getName()).thenReturn(tableNameA);
      when(tableB.getName()).thenReturn(tableNameB);
      when(dataSet.getTables()).thenReturn(List.of(tableA, tableB));
      when(connection.getSchema()).thenReturn("PUBLIC");
      // Return same order
      when(tableOrderResolver.resolveOrder(any(), any(), any()))
          .thenReturn(List.of(tableNameA, tableNameB));

      // When
      executor.executeOperation(Operation.INSERT, dataSet, connection, TableOrderingStrategy.AUTO);

      // Then - verify insert is called with original order
      verify(insertExecutor).execute(List.of(tableA, tableB), connection);
    }
  }
}
