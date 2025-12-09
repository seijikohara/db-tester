package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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
      final var stmt = mock(PreparedStatement.class);
      final var rs = mock(ResultSet.class);
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

      when(connection.prepareStatement(anyString())).thenReturn(stmt);
      when(stmt.executeQuery()).thenReturn(rs);
      when(rs.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
      when(parameterBinder.extractColumnTypes(metaData)).thenReturn(Map.of());

      // When
      executor.execute(List.of(table), connection);

      // Then
      verify(stmt).addBatch();
      verify(stmt).executeBatch();
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

  /** Tests for the insertTable() method. */
  @Nested
  @DisplayName("insertTable(Table, Connection) method")
  class InsertTableMethod {

    /** Tests for the insertTable method. */
    InsertTableMethod() {}

    /**
     * Verifies that insertTable skips tables with no rows.
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
      executor.insertTable(table, connection);

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
      final var stmt = mock(PreparedStatement.class);
      final var table = mock(Table.class);
      final var row = mock(Row.class);
      final var columnName = new ColumnName("ID");

      when(table.getColumns()).thenReturn(List.of(columnName));
      when(sqlBuilder.buildInsert(table)).thenReturn("INSERT INTO USERS (ID) VALUES (?)");
      when(connection.prepareStatement(anyString())).thenReturn(stmt);

      // When
      executor.insertRow(table, row, connection);

      // Then
      verify(parameterBinder).bindRow(eq(stmt), eq(row), any());
      verify(stmt).executeUpdate();
    }
  }
}
