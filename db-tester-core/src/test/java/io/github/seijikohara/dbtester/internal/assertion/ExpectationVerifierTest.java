package io.github.seijikohara.dbtester.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpectationVerifier}. */
@DisplayName("ExpectationVerifier")
class ExpectationVerifierTest {

  /** Tests for the ExpectationVerifier class. */
  ExpectationVerifierTest() {}

  /** Mock table reader for tests. */
  private TableReader mockTableReader;

  /** Mock data source for tests. */
  private DataSource mockDataSource;

  /** The verifier instance under test. */
  private ExpectationVerifier verifier;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockTableReader = mock(TableReader.class);
    mockDataSource = mock(DataSource.class);
    verifier = new ExpectationVerifier(mockTableReader, OperationDefaults.standard());
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
      final var instance = new ExpectationVerifier();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with OperationDefaults creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when table reader and operation defaults provided")
    void shouldCreateInstance_whenTableReaderAndOperationDefaultsProvided() {
      // When
      final var instance = new ExpectationVerifier(mockTableReader, OperationDefaults.standard());

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /**
   * Tests for the verifyExpectation(TableSet, DataSource, Collection, Map, RowOrdering,
   * OperationDefaults) method.
   */
  @Nested
  @DisplayName(
      "verifyExpectation(TableSet, DataSource, Collection, Map, RowOrdering, OperationDefaults)"
          + " method")
  class VerifyExpectationWithOperationDefaultsMethod {

    /** Tests for the verifyExpectation method with OperationDefaults. */
    VerifyExpectationWithOperationDefaultsMethod() {}

    /** Verifies that verification passes when datasets match with standard defaults. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when datasets match with standard operation defaults")
    void shouldPass_whenDataSetsMatchWithStandardDefaults() {
      // Given
      final var table =
          createMatchingTablePair("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var expectedTableSet = new SimpleTableSet(List.of(table));

      when(mockTableReader.fetchTable(any(DataSource.class), eq("USERS"), anyCollection()))
          .thenReturn(table);

      final var operationDefaults = OperationDefaults.standard();

      // When & Then
      assertDoesNotThrow(
          () ->
              verifier.verifyExpectation(
                  expectedTableSet,
                  mockDataSource,
                  null,
                  null,
                  RowOrdering.ORDERED,
                  operationDefaults),
          "should not throw when datasets match");

      verify(mockTableReader).fetchTable(eq(mockDataSource), eq("USERS"), anyCollection());
    }

    /** Verifies that verification passes with custom epsilon for matching data. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when datasets match with custom epsilon")
    void shouldPass_whenDataSetsMatchWithCustomEpsilon() {
      // Given
      final var table =
          createMatchingTablePair("ITEMS", List.of("ID", "PRICE"), List.of("1", "9.99"));
      final var expectedTableSet = new SimpleTableSet(List.of(table));

      when(mockTableReader.fetchTable(any(DataSource.class), eq("ITEMS"), anyCollection()))
          .thenReturn(table);

      final var operationDefaults = OperationDefaults.builder().floatingPointEpsilon(1e-9).build();

      // When & Then
      assertDoesNotThrow(
          () ->
              verifier.verifyExpectation(
                  expectedTableSet,
                  mockDataSource,
                  null,
                  null,
                  RowOrdering.ORDERED,
                  operationDefaults),
          "should not throw when datasets match with custom epsilon");
    }

    /** Verifies that verification handles exclude columns with operation defaults. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when exclude columns specified with operation defaults")
    void shouldPass_whenExcludeColumnsSpecifiedWithOperationDefaults() {
      // Given
      final var table =
          createMatchingTablePair("ORDERS", List.of("ID", "TOTAL"), List.of("1", "100"));
      final var expectedTableSet = new SimpleTableSet(List.of(table));

      when(mockTableReader.fetchTable(any(DataSource.class), eq("ORDERS"), anyCollection()))
          .thenReturn(table);

      final var operationDefaults = OperationDefaults.standard();

      // When & Then
      assertDoesNotThrow(
          () ->
              verifier.verifyExpectation(
                  expectedTableSet,
                  mockDataSource,
                  Set.of("CREATED_AT"),
                  Map.of(),
                  RowOrdering.ORDERED,
                  operationDefaults),
          "should not throw when exclude columns specified");
    }
  }

  /**
   * Creates a table with specified data.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values (one value per column)
   * @return the created table
   */
  private static SimpleTable createMatchingTablePair(
      final String tableName, final List<String> columnNames, final List<String> values) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rowValues = new LinkedHashMap<ColumnName, CellValue>();
    for (var i = 0; i < columns.size(); i++) {
      rowValues.put(columns.get(i), new CellValue(values.get(i)));
    }
    final var row = new SimpleRow(rowValues);
    return new SimpleTable(new TableName(tableName), columns, List.of(row));
  }
}
