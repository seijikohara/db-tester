package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultQueryAssertionProvider}. */
@DisplayName("DefaultQueryAssertionProvider")
class DefaultQueryAssertionProviderTest {

  /** Tests for the DefaultQueryAssertionProvider class. */
  DefaultQueryAssertionProviderTest() {}

  /** Mock comparator. */
  private DataSetComparator mockComparator;

  /** Mock table reader. */
  private TableReader mockTableReader;

  /** The provider instance under test. */
  private DefaultQueryAssertionProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockComparator = mock(DataSetComparator.class);
    mockTableReader = mock(TableReader.class);
    provider = new DefaultQueryAssertionProvider(mockComparator, mockTableReader);
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
      final var instance = new DefaultQueryAssertionProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependencies creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when dependencies provided")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance = new DefaultQueryAssertionProvider(mockComparator, mockTableReader);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the assertEqualsByQuery(TableSet, DataSource, String, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsByQuery(TableSet, DataSource, String, String, Collection) method")
  class AssertEqualsByQueryDataSetMethod {

    /** Tests for the assertEqualsByQuery method. */
    AssertEqualsByQueryDataSetMethod() {}

    /** Verifies that assertEqualsByQuery executes query and compares. */
    @Test
    @Tag("normal")
    @DisplayName("should execute query and compare when called")
    void shouldExecuteQueryAndCompare_whenCalled() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var expectedTable = mock(Table.class);
      final var actualTable = mock(Table.class);
      final var dataSource = mock(DataSource.class);
      final var query = "SELECT * FROM users";
      final var tableName = "users";
      final Collection<String> ignoreColumns = List.of("CREATED_AT");

      when(expectedDataSet.getTable(new TableName(tableName)))
          .thenReturn(Optional.of(expectedTable));
      when(mockTableReader.executeQuery(dataSource, query, tableName)).thenReturn(actualTable);
      doNothing()
          .when(mockComparator)
          .assertEqualsIgnoreColumns(any(Table.class), any(Table.class), any());

      // When
      provider.assertEqualsByQuery(expectedDataSet, dataSource, tableName, query, ignoreColumns);

      // Then
      verify(mockTableReader).executeQuery(dataSource, query, tableName);
      verify(mockComparator).assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumns);
    }

    /** Verifies that assertEqualsByQuery throws exception when expected table not found. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when expected table not found")
    void shouldThrowException_whenExpectedTableNotFound() {
      // Given
      final var expectedDataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var query = "SELECT * FROM users";
      final var tableName = "nonexistent";
      final Collection<String> ignoreColumns = List.of();

      when(expectedDataSet.getTable(new TableName(tableName))).thenReturn(Optional.empty());

      // When & Then
      assertThrows(
          AssertionError.class,
          () ->
              provider.assertEqualsByQuery(
                  expectedDataSet, dataSource, tableName, query, ignoreColumns),
          "should throw AssertionError");
    }
  }

  /** Tests for the assertEqualsByQuery(Table, DataSource, String, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsByQuery(Table, DataSource, String, String, Collection) method")
  class AssertEqualsByQueryTableMethod {

    /** Tests for the assertEqualsByQuery method with table. */
    AssertEqualsByQueryTableMethod() {}

    /** Verifies that assertEqualsByQuery executes query and compares. */
    @Test
    @Tag("normal")
    @DisplayName("should execute query and compare when called")
    void shouldExecuteQueryAndCompare_whenCalled() {
      // Given
      final var expectedTable = mock(Table.class);
      final var actualTable = mock(Table.class);
      final var dataSource = mock(DataSource.class);
      final var tableName = "users";
      final var query = "SELECT * FROM users";
      final Collection<String> ignoreColumns = List.of("CREATED_AT");

      when(mockTableReader.executeQuery(dataSource, query, tableName)).thenReturn(actualTable);
      doNothing()
          .when(mockComparator)
          .assertEqualsIgnoreColumns(any(Table.class), any(Table.class), any());

      // When
      provider.assertEqualsByQuery(expectedTable, dataSource, tableName, query, ignoreColumns);

      // Then
      verify(mockTableReader).executeQuery(dataSource, query, tableName);
      verify(mockComparator).assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumns);
    }
  }
}
