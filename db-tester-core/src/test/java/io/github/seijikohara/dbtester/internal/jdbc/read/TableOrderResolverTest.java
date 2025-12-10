package io.github.seijikohara.dbtester.internal.jdbc.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.domain.TableName;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableOrderResolver}. */
@DisplayName("TableOrderResolver")
class TableOrderResolverTest {

  /** Tests for the TableOrderResolver class. */
  TableOrderResolverTest() {}

  /** The resolver instance under test. */
  private TableOrderResolver resolver;

  /** Mock database connection. */
  private Connection connection;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    connection = mock(Connection.class);
    resolver = new TableOrderResolver();
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
    @DisplayName("should create instance when default constructor called")
    void shouldCreateInstance_whenDefaultConstructorCalled() {
      // When
      final var instance = new TableOrderResolver();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the resolveOrder() method. */
  @Nested
  @DisplayName("resolveOrder(List, Connection, String) method")
  class ResolveOrderMethod {

    /** Tests for the resolveOrder method. */
    ResolveOrderMethod() {}

    /** Verifies that empty list returns empty list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty list when input is empty")
    void shouldReturnEmptyList_whenInputIsEmpty() {
      // Given
      final List<TableName> tableNames = List.of();

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(List.of(), result, "should return empty list");
    }

    /** Verifies that single table returns same list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return same list when only one table provided")
    void shouldReturnSameList_whenOnlyOneTableProvided() {
      // Given
      final var tableName = new TableName("USERS");
      final List<TableName> tableNames = List.of(tableName);

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(tableNames, result, "should return same list for single table");
    }

    /**
     * Verifies that tables with no dependencies return original order.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return original order when no dependencies exist")
    void shouldReturnOriginalOrder_whenNoDependenciesExist() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);

      final var metadata = mock(DatabaseMetaData.class);
      when(connection.getMetaData()).thenReturn(metadata);
      when(connection.getCatalog()).thenReturn(null);

      // Return empty result sets for getImportedKeys
      final var emptyResultSet = mock(ResultSet.class);
      when(emptyResultSet.next()).thenReturn(false);
      when(metadata.getImportedKeys(any(), any(), eq("USERS"))).thenReturn(emptyResultSet);
      when(metadata.getImportedKeys(any(), any(), eq("ORDERS"))).thenReturn(emptyResultSet);

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(tableNames, result, "should return original order when no dependencies");
    }

    /**
     * Verifies that tables are sorted when dependencies exist.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should sort tables when dependencies exist")
    void shouldSortTables_whenDependenciesExist() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(orders, users);

      final var metadata = mock(DatabaseMetaData.class);
      when(connection.getMetaData()).thenReturn(metadata);
      when(connection.getCatalog()).thenReturn(null);

      // USERS has no dependencies
      final var usersResultSet = mock(ResultSet.class);
      when(usersResultSet.next()).thenReturn(false);
      when(metadata.getImportedKeys(any(), any(), eq("USERS"))).thenReturn(usersResultSet);

      // ORDERS depends on USERS
      final var ordersResultSet = mock(ResultSet.class);
      when(ordersResultSet.next()).thenReturn(true, false);
      when(ordersResultSet.getString("PKTABLE_NAME")).thenReturn("USERS");
      when(metadata.getImportedKeys(any(), any(), eq("ORDERS"))).thenReturn(ordersResultSet);

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(List.of(users, orders), result, "USERS should come before ORDERS");
    }
  }
}
