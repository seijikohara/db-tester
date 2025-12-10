package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ForeignKeyResolver}. */
@DisplayName("ForeignKeyResolver")
class ForeignKeyResolverTest {

  /** Tests for the ForeignKeyResolver class. */
  ForeignKeyResolverTest() {}

  /** The resolver instance under test. */
  private ForeignKeyResolver resolver;

  /** Mock dependency extractor. */
  private ForeignKeyDependencyExtractor dependencyExtractor;

  /** Mock database connection. */
  private Connection connection;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    dependencyExtractor = mock(ForeignKeyDependencyExtractor.class);
    connection = mock(Connection.class);
    resolver = new ForeignKeyResolver(dependencyExtractor);
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
      final var instance = new ForeignKeyResolver();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependency extractor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when dependency extractor provided")
    void shouldCreateInstance_whenDependencyExtractorProvided() {
      // Given
      final var extractor = mock(ForeignKeyDependencyExtractor.class);

      // When
      final var instance = new ForeignKeyResolver(extractor);

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

    /** Verifies that tables with no dependencies return original order. */
    @Test
    @Tag("normal")
    @DisplayName("should return original order when no dependencies exist")
    void shouldReturnOriginalOrder_whenNoDependenciesExist() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);

      when(dependencyExtractor.extract(eq(tableNames), eq(connection), any())).thenReturn(Map.of());

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(tableNames, result, "should return original order when no dependencies");
    }

    /** Verifies that tables are sorted when dependencies exist. */
    @Test
    @Tag("normal")
    @DisplayName("should sort tables when dependencies exist")
    void shouldSortTables_whenDependenciesExist() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(orders, users);
      final Map<TableName, Set<TableName>> dependencies = Map.of(orders, Set.of(users));

      when(dependencyExtractor.extract(eq(tableNames), eq(connection), any()))
          .thenReturn(dependencies);

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(List.of(users, orders), result, "USERS should come before ORDERS");
    }

    /** Verifies that DatabaseOperationException falls back to original order. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to original order when DatabaseOperationException occurs")
    void shouldFallBackToOriginalOrder_whenDatabaseOperationExceptionOccurs() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);

      when(dependencyExtractor.extract(eq(tableNames), eq(connection), any()))
          .thenThrow(new DatabaseOperationException("DB error"));

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(
          tableNames, result, "should return original order on DatabaseOperationException");
    }

    /** Verifies that DatabaseTesterException falls back to original order. */
    @Test
    @Tag("edge-case")
    @DisplayName("should fall back to original order when DatabaseTesterException occurs")
    void shouldFallBackToOriginalOrder_whenDatabaseTesterExceptionOccurs() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);

      when(dependencyExtractor.extract(eq(tableNames), eq(connection), any()))
          .thenThrow(new DatabaseTesterException("FK error"));

      // When
      final var result = resolver.resolveOrder(tableNames, connection, null);

      // Then
      assertEquals(tableNames, result, "should return original order on DatabaseTesterException");
    }

    /** Verifies that schema is passed to extractor. */
    @Test
    @Tag("normal")
    @DisplayName("should pass schema to dependency extractor")
    void shouldPassSchemaToExtractor_whenSchemaProvided() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);
      final var schema = "PUBLIC";

      when(dependencyExtractor.extract(tableNames, connection, schema)).thenReturn(Map.of());

      // When
      resolver.resolveOrder(tableNames, connection, schema);

      // Then
      verify(dependencyExtractor).extract(tableNames, connection, schema);
    }
  }
}
