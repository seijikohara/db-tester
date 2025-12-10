package io.github.seijikohara.dbtester.internal.jdbc.executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.jdbc.SqlBuilder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TruncateExecutor}. */
@DisplayName("TruncateExecutor")
class TruncateExecutorTest {

  /** Tests for the TruncateExecutor class. */
  TruncateExecutorTest() {}

  /** Mock SQL builder. */
  private SqlBuilder sqlBuilder;

  /** The executor instance under test. */
  private TruncateExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    sqlBuilder = mock(SqlBuilder.class);
    executor = new TruncateExecutor(sqlBuilder);
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
    @DisplayName("should create instance with SQL builder")
    void shouldCreateInstance_whenSqlBuilderProvided() {
      // When
      final var instance = new TruncateExecutor(sqlBuilder);

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
     * Verifies that execute truncates each table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should truncate each table")
    void shouldTruncateTables_whenTablesProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(Statement.class);

      final var table = mock(Table.class);
      when(table.getName()).thenReturn(new TableName("USERS"));

      when(sqlBuilder.buildTruncate("USERS")).thenReturn("TRUNCATE TABLE USERS");
      when(connection.createStatement()).thenReturn(statement);

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(statement).executeUpdate("TRUNCATE TABLE USERS");
    }
  }

  /** Tests for the truncateTable() method. */
  @Nested
  @DisplayName("truncateTable(String, Connection) method")
  class TruncateTableMethod {

    /** Tests for the truncateTable method. */
    TruncateTableMethod() {}

    /**
     * Verifies that truncateTable truncates the table.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should truncate the table")
    void shouldTruncateTable_whenTableNameProvided() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var statement = mock(Statement.class);

      when(sqlBuilder.buildTruncate("USERS")).thenReturn("TRUNCATE TABLE USERS");
      when(connection.createStatement()).thenReturn(statement);

      // When
      executor.truncateTable("USERS", connection);

      // Then
      verify(statement).executeUpdate("TRUNCATE TABLE USERS");
    }

    /**
     * Verifies that truncateTable falls back to DELETE when TRUNCATE fails.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to DELETE when TRUNCATE fails")
    void shouldFallbackToDelete_whenTruncateFails() throws SQLException {
      // Given
      final var connection = mock(Connection.class);
      final var truncateStmt = mock(Statement.class);
      final var deleteStmt = mock(Statement.class);

      when(sqlBuilder.buildTruncate("USERS")).thenReturn("TRUNCATE TABLE USERS");
      when(sqlBuilder.buildDeleteAll("USERS")).thenReturn("DELETE FROM USERS");

      when(connection.createStatement()).thenReturn(truncateStmt, deleteStmt);
      when(truncateStmt.executeUpdate("TRUNCATE TABLE USERS"))
          .thenThrow(new SQLException("TRUNCATE not supported"));

      // When
      executor.truncateTable("USERS", connection);

      // Then
      verify(deleteStmt).executeUpdate("DELETE FROM USERS");
    }
  }
}
