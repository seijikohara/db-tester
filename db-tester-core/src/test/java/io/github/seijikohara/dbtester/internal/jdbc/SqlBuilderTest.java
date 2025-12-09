package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SqlBuilder}. */
@DisplayName("SqlBuilder")
class SqlBuilderTest {

  /** Tests for the SqlBuilder class. */
  SqlBuilderTest() {}

  /** The builder instance under test. */
  private SqlBuilder builder;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    builder = new SqlBuilder();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new SqlBuilder();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the buildInsert() method. */
  @Nested
  @DisplayName("buildInsert(Table) method")
  class BuildInsertMethod {

    /** Tests for the buildInsert method. */
    BuildInsertMethod() {}

    /** Verifies that buildInsert returns correct SQL when table has columns. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when table has columns")
    void shouldReturnCorrectSql_whenTableHasColumns() {
      // Given
      final var mockTable = mock(Table.class);
      when(mockTable.getName()).thenReturn(new TableName("USERS"));
      when(mockTable.getColumns())
          .thenReturn(
              List.of(new ColumnName("ID"), new ColumnName("NAME"), new ColumnName("EMAIL")));

      // When
      final var result = builder.buildInsert(mockTable);

      // Then
      assertEquals(
          "INSERT INTO USERS (ID, NAME, EMAIL) VALUES (?, ?, ?)",
          result,
          "should build correct INSERT SQL");
    }

    /** Verifies that buildInsert handles single column. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle single column")
    void shouldHandleSingleColumn_whenTableHasOneColumn() {
      // Given
      final var mockTable = mock(Table.class);
      when(mockTable.getName()).thenReturn(new TableName("SIMPLE"));
      when(mockTable.getColumns()).thenReturn(List.of(new ColumnName("VALUE")));

      // When
      final var result = builder.buildInsert(mockTable);

      // Then
      assertEquals("INSERT INTO SIMPLE (VALUE) VALUES (?)", result, "should handle single column");
    }
  }

  /** Tests for the buildUpdate() method. */
  @Nested
  @DisplayName("buildUpdate(String, ColumnName, List) method")
  class BuildUpdateMethod {

    /** Tests for the buildUpdate method. */
    BuildUpdateMethod() {}

    /** Verifies that buildUpdate returns correct SQL when columns provided. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when columns provided")
    void shouldReturnCorrectSql_whenColumnsProvided() {
      // Given
      final var tableName = "USERS";
      final var pkColumn = new ColumnName("ID");
      final var updateColumns = List.of(new ColumnName("NAME"), new ColumnName("EMAIL"));

      // When
      final var result = builder.buildUpdate(tableName, pkColumn, updateColumns);

      // Then
      assertEquals(
          "UPDATE USERS SET NAME = ?, EMAIL = ? WHERE ID = ?",
          result,
          "should build correct UPDATE SQL");
    }

    /** Verifies that buildUpdate handles single update column. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle single update column")
    void shouldHandleSingleColumn_whenOneUpdateColumn() {
      // Given
      final var tableName = "USERS";
      final var pkColumn = new ColumnName("ID");
      final var updateColumns = List.of(new ColumnName("NAME"));

      // When
      final var result = builder.buildUpdate(tableName, pkColumn, updateColumns);

      // Then
      assertEquals(
          "UPDATE USERS SET NAME = ? WHERE ID = ?", result, "should handle single update column");
    }
  }

  /** Tests for the buildDelete() method. */
  @Nested
  @DisplayName("buildDelete(String, ColumnName) method")
  class BuildDeleteMethod {

    /** Tests for the buildDelete method. */
    BuildDeleteMethod() {}

    /** Verifies that buildDelete returns correct SQL. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when called")
    void shouldReturnCorrectSql_whenCalled() {
      // Given
      final var tableName = "USERS";
      final var pkColumn = new ColumnName("ID");

      // When
      final var result = builder.buildDelete(tableName, pkColumn);

      // Then
      assertEquals("DELETE FROM USERS WHERE ID = ?", result, "should build correct DELETE SQL");
    }
  }

  /** Tests for the buildDeleteAll() method. */
  @Nested
  @DisplayName("buildDeleteAll(String) method")
  class BuildDeleteAllMethod {

    /** Tests for the buildDeleteAll method. */
    BuildDeleteAllMethod() {}

    /** Verifies that buildDeleteAll returns correct SQL. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when called")
    void shouldReturnCorrectSql_whenCalled() {
      // Given
      final var tableName = "USERS";

      // When
      final var result = builder.buildDeleteAll(tableName);

      // Then
      assertEquals("DELETE FROM USERS", result, "should build correct DELETE ALL SQL");
    }
  }

  /** Tests for the buildTruncate() method. */
  @Nested
  @DisplayName("buildTruncate(String) method")
  class BuildTruncateMethod {

    /** Tests for the buildTruncate method. */
    BuildTruncateMethod() {}

    /** Verifies that buildTruncate returns correct SQL. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when called")
    void shouldReturnCorrectSql_whenCalled() {
      // Given
      final var tableName = "USERS";

      // When
      final var result = builder.buildTruncate(tableName);

      // Then
      assertEquals("TRUNCATE TABLE USERS", result, "should build correct TRUNCATE SQL");
    }
  }

  /** Tests for the buildMetadataQuery() method. */
  @Nested
  @DisplayName("buildMetadataQuery(String) method")
  class BuildMetadataQueryMethod {

    /** Tests for the buildMetadataQuery method. */
    BuildMetadataQueryMethod() {}

    /** Verifies that buildMetadataQuery returns correct SQL. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct SQL when called")
    void shouldReturnCorrectSql_whenCalled() {
      // Given
      final var tableName = "USERS";

      // When
      final var result = builder.buildMetadataQuery(tableName);

      // Then
      assertEquals(
          "SELECT * FROM USERS WHERE 1=0", result, "should build correct metadata query SQL");
    }
  }
}
