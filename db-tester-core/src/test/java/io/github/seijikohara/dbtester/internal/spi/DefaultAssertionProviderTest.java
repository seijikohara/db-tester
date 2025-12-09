package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.jdbc.TableReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultAssertionProvider}. */
@DisplayName("DefaultAssertionProvider")
class DefaultAssertionProviderTest {

  /** Tests for the DefaultAssertionProvider class. */
  DefaultAssertionProviderTest() {}

  /** Mock comparator. */
  private DataSetComparator mockComparator;

  /** Mock table reader. */
  private TableReader mockTableReader;

  /** The provider instance under test. */
  private DefaultAssertionProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockComparator = mock(DataSetComparator.class);
    mockTableReader = mock(TableReader.class);
    provider = new DefaultAssertionProvider(mockComparator, mockTableReader);
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
      final var instance = new DefaultAssertionProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependencies creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when dependencies provided")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance = new DefaultAssertionProvider(mockComparator, mockTableReader);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the assertEquals(DataSet, DataSet) method. */
  @Nested
  @DisplayName("assertEquals(DataSet, DataSet) method")
  class AssertEqualsDataSetMethod {

    /** Tests for the assertEquals method. */
    AssertEqualsDataSetMethod() {}

    /** Verifies that assertEquals delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(DataSet.class);
      final var actual = mock(DataSet.class);
      doNothing().when(mockComparator).assertEquals(any(DataSet.class), any(DataSet.class), any());

      // When
      provider.assertEquals(expected, actual);

      // Then
      verify(mockComparator).assertEquals(expected, actual, null);
    }
  }

  /** Tests for the assertEquals(DataSet, DataSet, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(DataSet, DataSet, AssertionFailureHandler) method")
  class AssertEqualsDataSetWithHandlerMethod {

    /** Tests for the assertEquals method with handler. */
    AssertEqualsDataSetWithHandlerMethod() {}

    /** Verifies that assertEquals passes handler to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should pass handler to comparator when handler provided")
    void shouldPassHandlerToComparator_whenHandlerProvided() {
      // Given
      final var expected = mock(DataSet.class);
      final var actual = mock(DataSet.class);
      final var handler = mock(AssertionFailureHandler.class);
      doNothing().when(mockComparator).assertEquals(any(DataSet.class), any(DataSet.class), any());

      // When
      provider.assertEquals(expected, actual, handler);

      // Then
      verify(mockComparator).assertEquals(expected, actual, handler);
    }
  }

  /** Tests for the assertEquals(Table, Table) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table) method")
  class AssertEqualsTableMethod {

    /** Tests for the assertEquals method with tables. */
    AssertEqualsTableMethod() {}

    /** Verifies that assertEquals delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      doNothing().when(mockComparator).assertEquals(any(Table.class), any(Table.class), any());

      // When
      provider.assertEquals(expected, actual);

      // Then
      verify(mockComparator).assertEquals(expected, actual, null);
    }
  }

  /** Tests for the assertEquals(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table, Collection) method")
  class AssertEqualsTableWithAdditionalColumnsMethod {

    /** Tests for the assertEquals method with additional columns. */
    AssertEqualsTableWithAdditionalColumnsMethod() {}

    /** Verifies that assertEquals passes additional columns to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should pass additional columns to comparator when called")
    void shouldPassAdditionalColumnsToComparator_whenCalled() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final Collection<String> additionalColumns = List.of("CREATED_AT", "UPDATED_AT");
      doNothing()
          .when(mockComparator)
          .assertEqualsWithAdditionalColumns(any(Table.class), any(Table.class), any());

      // When
      provider.assertEquals(expected, actual, additionalColumns);

      // Then
      verify(mockComparator).assertEqualsWithAdditionalColumns(expected, actual, additionalColumns);
    }
  }

  /** Tests for the assertEqualsIgnoreColumns(DataSet, DataSet, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(DataSet, DataSet, String, Collection) method")
  class AssertEqualsIgnoreColumnsDataSetMethod {

    /** Tests for the assertEqualsIgnoreColumns method. */
    AssertEqualsIgnoreColumnsDataSetMethod() {}

    /** Verifies that assertEqualsIgnoreColumns delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(DataSet.class);
      final var actual = mock(DataSet.class);
      final var tableName = "users";
      final Collection<String> ignoreColumns = List.of("CREATED_AT");
      doNothing()
          .when(mockComparator)
          .assertEqualsIgnoreColumns(
              any(DataSet.class), any(DataSet.class), any(String.class), any());

      // When
      provider.assertEqualsIgnoreColumns(expected, actual, tableName, ignoreColumns);

      // Then
      verify(mockComparator).assertEqualsIgnoreColumns(expected, actual, tableName, ignoreColumns);
    }
  }

  /** Tests for the assertEqualsIgnoreColumns(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(Table, Table, Collection) method")
  class AssertEqualsIgnoreColumnsTableMethod {

    /** Tests for the assertEqualsIgnoreColumns method with tables. */
    AssertEqualsIgnoreColumnsTableMethod() {}

    /** Verifies that assertEqualsIgnoreColumns delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final Collection<String> ignoreColumns = List.of("CREATED_AT");
      doNothing()
          .when(mockComparator)
          .assertEqualsIgnoreColumns(any(Table.class), any(Table.class), any());

      // When
      provider.assertEqualsIgnoreColumns(expected, actual, ignoreColumns);

      // Then
      verify(mockComparator).assertEqualsIgnoreColumns(expected, actual, ignoreColumns);
    }
  }

  /** Tests for the assertEqualsByQuery(DataSet, DataSource, String, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsByQuery(DataSet, DataSource, String, String, Collection) method")
  class AssertEqualsByQueryDataSetMethod {

    /** Tests for the assertEqualsByQuery method. */
    AssertEqualsByQueryDataSetMethod() {}

    /** Verifies that assertEqualsByQuery executes query and compares. */
    @Test
    @Tag("normal")
    @DisplayName("should execute query and compare when called")
    void shouldExecuteQueryAndCompare_whenCalled() {
      // Given
      final var expectedDataSet = mock(DataSet.class);
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
      provider.assertEqualsByQuery(expectedDataSet, dataSource, query, tableName, ignoreColumns);

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
      final var expectedDataSet = mock(DataSet.class);
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
                  expectedDataSet, dataSource, query, tableName, ignoreColumns),
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
