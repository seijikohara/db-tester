package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ForeignKeyDependencyExtractor}. */
@DisplayName("ForeignKeyDependencyExtractor")
class ForeignKeyDependencyExtractorTest {

  /** Tests for the ForeignKeyDependencyExtractor class. */
  ForeignKeyDependencyExtractorTest() {}

  /** The extractor instance under test. */
  private ForeignKeyDependencyExtractor extractor;

  /** Mock database connection. */
  private Connection connection;

  /** Mock database metadata. */
  private DatabaseMetaData metaData;

  /**
   * Sets up test fixtures before each test.
   *
   * @throws SQLException if a database error occurs
   */
  @BeforeEach
  void setUp() throws SQLException {
    extractor = new ForeignKeyDependencyExtractor();
    connection = mock(Connection.class);
    metaData = mock(DatabaseMetaData.class);
    when(connection.getMetaData()).thenReturn(metaData);
    when(connection.getCatalog()).thenReturn(null);
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
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new ForeignKeyDependencyExtractor();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the extract() method. */
  @Nested
  @DisplayName("extract(List, Connection, String) method")
  class ExtractMethod {

    /** Tests for the extract method. */
    ExtractMethod() {}

    /**
     * Verifies that empty list returns empty map.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty map when input is empty")
    void shouldReturnEmptyMap_whenInputIsEmpty() throws SQLException {
      // Given
      final List<TableName> tableNames = List.of();

      // When
      final var result = extractor.extract(tableNames, connection, null);

      // Then
      assertEquals(Map.of(), result, "should return empty map");
    }

    /**
     * Verifies that tables with no foreign keys return empty map.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty map when no foreign keys exist")
    void shouldReturnEmptyMap_whenNoForeignKeysExist() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(users, orders);

      final var emptyResultSet = createEmptyResultSet();
      when(metaData.getImportedKeys(any(), any(), any())).thenReturn(emptyResultSet);

      // When
      final var result = extractor.extract(tableNames, connection, null);

      // Then
      assertTrue(result.isEmpty(), "should return empty map when no foreign keys");
    }

    /**
     * Verifies that foreign key dependency is correctly extracted.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should extract dependency when foreign key exists")
    void shouldExtractDependency_whenForeignKeyExists() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(orders, users);

      final var usersResultSet = createEmptyResultSet();
      when(metaData.getImportedKeys(any(), any(), eq("USERS"))).thenReturn(usersResultSet);

      final var ordersResultSet = createForeignKeyResultSet("USERS");
      when(metaData.getImportedKeys(any(), any(), eq("ORDERS"))).thenReturn(ordersResultSet);

      // When
      final var result = extractor.extract(tableNames, connection, null);

      // Then
      assertEquals(1, result.size(), "should have one dependency");
      assertEquals(Set.of(users), result.get(orders), "ORDERS should depend on USERS");
    }

    /**
     * Verifies that multiple dependencies are extracted.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should extract multiple dependencies when multiple foreign keys exist")
    void shouldExtractMultipleDependencies_whenMultipleForeignKeysExist() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final var products = new TableName("PRODUCTS");
      final var orders = new TableName("ORDERS");
      final List<TableName> tableNames = List.of(orders, users, products);

      final var usersResultSet = createEmptyResultSet();
      when(metaData.getImportedKeys(any(), any(), eq("USERS"))).thenReturn(usersResultSet);

      final var productsResultSet = createEmptyResultSet();
      when(metaData.getImportedKeys(any(), any(), eq("PRODUCTS"))).thenReturn(productsResultSet);

      final var ordersResultSet = createMultipleForeignKeyResultSet("USERS", "PRODUCTS");
      when(metaData.getImportedKeys(any(), any(), eq("ORDERS"))).thenReturn(ordersResultSet);

      // When
      final var result = extractor.extract(tableNames, connection, null);

      // Then
      assertEquals(1, result.size(), "should have one table with dependencies");
      assertEquals(
          Set.of(users, products),
          result.get(orders),
          "ORDERS should depend on USERS and PRODUCTS");
    }

    /**
     * Verifies that SQLException is wrapped in DatabaseTesterException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should throw DatabaseTesterException when SQLException occurs")
    void shouldThrowDatabaseTesterException_whenSqlExceptionOccurs() throws SQLException {
      // Given
      final var users = new TableName("USERS");
      final List<TableName> tableNames = List.of(users);

      when(metaData.getImportedKeys(any(), any(), any())).thenThrow(new SQLException("DB error"));

      // When & Then
      assertThrows(
          DatabaseTesterException.class,
          () -> extractor.extract(tableNames, connection, null),
          "should throw DatabaseTesterException");
    }
  }

  /** Tests for the extractTableDependencies() method. */
  @Nested
  @DisplayName("extractTableDependencies(TableName, Set, DatabaseMetaData, String, String) method")
  class ExtractTableDependenciesMethod {

    /** Tests for the extractTableDependencies method. */
    ExtractTableDependenciesMethod() {}

    /**
     * Verifies that self-referencing foreign keys are ignored.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should ignore self-referencing foreign keys")
    void shouldIgnoreSelfReference_whenTableReferencesSelf() throws SQLException {
      // Given
      final var employees = new TableName("EMPLOYEES");
      final var tableNameSet = Set.of(employees);

      final var resultSet = createForeignKeyResultSet("EMPLOYEES");
      when(metaData.getImportedKeys(any(), any(), eq("EMPLOYEES"))).thenReturn(resultSet);

      // When
      final var result =
          extractor.extractTableDependencies(employees, tableNameSet, metaData, null, null);

      // Then
      assertTrue(result.isEmpty(), "should not include self-reference");
    }

    /**
     * Verifies that foreign keys to tables outside the dataset are ignored.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should ignore foreign keys to tables outside dataset")
    void shouldIgnoreForeignKeys_whenTargetTableNotInDataset() throws SQLException {
      // Given
      final var orders = new TableName("ORDERS");
      final var tableNameSet = Set.of(orders);

      final var resultSet = createForeignKeyResultSet("USERS");
      when(metaData.getImportedKeys(any(), any(), eq("ORDERS"))).thenReturn(resultSet);

      // When
      final var result =
          extractor.extractTableDependencies(orders, tableNameSet, metaData, null, null);

      // Then
      assertTrue(result.isEmpty(), "should not include FK to table outside dataset");
    }
  }

  /** Tests for the extractReferencedTableName() method. */
  @Nested
  @DisplayName("extractReferencedTableName(String, TableName, Set) method")
  class ExtractReferencedTableNameMethod {

    /** Tests for the extractReferencedTableName method. */
    ExtractReferencedTableNameMethod() {}

    /** Verifies that null referenced table returns empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when referenced table is null")
    void shouldReturnEmpty_whenReferencedTableIsNull() {
      // Given
      final var currentTable = new TableName("ORDERS");
      final var tableNameSet = Set.of(currentTable);

      // When
      final var result = extractor.extractReferencedTableName(null, currentTable, tableNameSet);

      // Then
      assertEquals(Optional.empty(), result, "should return empty for null reference");
    }

    /** Verifies that valid referenced table returns the table name. */
    @Test
    @Tag("normal")
    @DisplayName("should return table name when valid reference exists")
    void shouldReturnTableName_whenValidReferenceExists() {
      // Given
      final var users = new TableName("USERS");
      final var orders = new TableName("ORDERS");
      final var tableNameSet = Set.of(users, orders);

      // When
      final var result = extractor.extractReferencedTableName("USERS", orders, tableNameSet);

      // Then
      assertEquals(Optional.of(users), result, "should return referenced table name");
    }

    /** Verifies that self-reference returns empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when self-referencing")
    void shouldReturnEmpty_whenSelfReferencing() {
      // Given
      final var employees = new TableName("EMPLOYEES");
      final var tableNameSet = Set.of(employees);

      // When
      final var result = extractor.extractReferencedTableName("EMPLOYEES", employees, tableNameSet);

      // Then
      assertEquals(Optional.empty(), result, "should return empty for self-reference");
    }

    /** Verifies that reference to table outside dataset returns empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when reference is outside dataset")
    void shouldReturnEmpty_whenReferenceOutsideDataset() {
      // Given
      final var orders = new TableName("ORDERS");
      final var tableNameSet = Set.of(orders);

      // When
      final var result = extractor.extractReferencedTableName("USERS", orders, tableNameSet);

      // Then
      assertEquals(Optional.empty(), result, "should return empty for reference outside dataset");
    }
  }

  /**
   * Creates an empty result set mock.
   *
   * @return a mock ResultSet that returns false on next()
   * @throws SQLException if mock setup fails
   */
  private ResultSet createEmptyResultSet() throws SQLException {
    final var resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(false);
    return resultSet;
  }

  /**
   * Creates a result set mock with a single foreign key reference.
   *
   * @param referencedTable the name of the referenced table
   * @return a mock ResultSet with one foreign key reference
   * @throws SQLException if mock setup fails
   */
  private ResultSet createForeignKeyResultSet(final String referencedTable) throws SQLException {
    final var resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getString("PKTABLE_NAME")).thenReturn(referencedTable);
    return resultSet;
  }

  /**
   * Creates a result set mock with multiple foreign key references.
   *
   * @param firstReference the name of the first referenced table
   * @param secondReference the name of the second referenced table
   * @return a mock ResultSet with two foreign key references
   * @throws SQLException if mock setup fails
   */
  private ResultSet createMultipleForeignKeyResultSet(
      final String firstReference, final String secondReference) throws SQLException {
    final var resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, true, false);
    when(resultSet.getString("PKTABLE_NAME")).thenReturn(firstReference, secondReference);
    return resultSet;
  }
}
