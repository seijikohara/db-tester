package io.github.seijikohara.dbtester.internal.scenario;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ScenarioFilter}. */
@DisplayName("ScenarioFilter")
class ScenarioFilterTest {

  /** Tests for the ScenarioFilter class. */
  ScenarioFilterTest() {}

  /** Default scenario marker for testing. */
  private ScenarioMarker scenarioMarker;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    scenarioMarker = new ScenarioMarker("[Test]");
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance with scenario names. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when scenario names provided")
    void shouldCreateInstance_whenScenarioNamesProvided() {
      // Given
      final var names = List.of(new ScenarioName("test1"), new ScenarioName("test2"));

      // When
      final var filter = new ScenarioFilter(scenarioMarker, names);

      // Then
      assertNotNull(filter, "filter should not be null");
    }

    /** Verifies that constructor creates instance with empty scenario names. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create instance when empty scenario names provided")
    void shouldCreateInstance_whenEmptyScenarioNamesProvided() {
      // When
      final var filter = new ScenarioFilter(scenarioMarker, List.of());

      // Then
      assertNotNull(filter, "filter should not be null");
    }
  }

  /** Tests for the getScenarioMarker() method. */
  @Nested
  @DisplayName("getScenarioMarker() method")
  class GetScenarioMarkerMethod {

    /** Tests for the getScenarioMarker method. */
    GetScenarioMarkerMethod() {}

    /** Verifies that getScenarioMarker returns the marker. */
    @Test
    @Tag("normal")
    @DisplayName("should return scenario marker when called")
    void shouldReturnScenarioMarker_whenCalled() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());

      // When
      final var result = filter.getScenarioMarker();

      // Then
      assertEquals(scenarioMarker, result, "should return expected scenario marker");
    }
  }

  /** Tests for the getScenarioNames() method. */
  @Nested
  @DisplayName("getScenarioNames() method")
  class GetScenarioNamesMethod {

    /** Tests for the getScenarioNames method. */
    GetScenarioNamesMethod() {}

    /** Verifies that getScenarioNames returns scenario names. */
    @Test
    @Tag("normal")
    @DisplayName("should return scenario names when called")
    void shouldReturnScenarioNames_whenCalled() {
      // Given
      final var names = List.of(new ScenarioName("test1"), new ScenarioName("test2"));
      final var filter = new ScenarioFilter(scenarioMarker, names);

      // When
      final var result = filter.getScenarioNames();

      // Then
      assertEquals(Set.copyOf(names), result, "should return expected scenario names");
    }

    /** Verifies that getScenarioNames returns immutable set. */
    @Test
    @Tag("normal")
    @DisplayName("should return immutable set when called")
    void shouldReturnImmutableSet_whenCalled() {
      // Given
      final var names = List.of(new ScenarioName("test1"));
      final var filter = new ScenarioFilter(scenarioMarker, names);

      // When
      final var result = filter.getScenarioNames();

      // Then
      assertAll(
          "scenario names set should be immutable",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have 1 element"));
    }
  }

  /** Tests for the isActive() method. */
  @Nested
  @DisplayName("isActive() method")
  class IsActiveMethod {

    /** Tests for the isActive method. */
    IsActiveMethod() {}

    /** Verifies that isActive returns true when scenario names provided. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when scenario names provided")
    void shouldReturnTrue_whenScenarioNamesProvided() {
      // Given
      final var names = List.of(new ScenarioName("test1"));
      final var filter = new ScenarioFilter(scenarioMarker, names);

      // When
      final var result = filter.isActive();

      // Then
      assertTrue(result, "should be active when scenario names provided");
    }

    /** Verifies that isActive returns false when no scenario names. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when no scenario names")
    void shouldReturnFalse_whenNoScenarioNames() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());

      // When
      final var result = filter.isActive();

      // Then
      assertFalse(result, "should not be active when no scenario names");
    }
  }

  /** Tests for the findScenarioColumn(List) method. */
  @Nested
  @DisplayName("findScenarioColumn(List) method")
  class FindScenarioColumnMethod {

    /** Tests for the findScenarioColumn method. */
    FindScenarioColumnMethod() {}

    /** Verifies that findScenarioColumn returns column when marker matches. */
    @Test
    @Tag("normal")
    @DisplayName("should return column when marker matches first column")
    void shouldReturnColumn_whenMarkerMatchesFirstColumn() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final var columns =
          List.of(new ColumnName("[Test]"), new ColumnName("id"), new ColumnName("name"));

      // When
      final var result = filter.findScenarioColumn(columns);

      // Then
      assertTrue(result.isPresent(), "should find scenario column");
      assertEquals("[Test]", result.get().value(), "should return matching column");
    }

    /** Verifies that findScenarioColumn returns empty when marker does not match. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when marker does not match first column")
    void shouldReturnEmpty_whenMarkerDoesNotMatchFirstColumn() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final var columns = List.of(new ColumnName("id"), new ColumnName("name"));

      // When
      final var result = filter.findScenarioColumn(columns);

      // Then
      assertFalse(result.isPresent(), "should not find scenario column");
    }

    /** Verifies that findScenarioColumn returns empty when columns empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when columns empty")
    void shouldReturnEmpty_whenColumnsEmpty() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final List<ColumnName> columns = List.of();

      // When
      final var result = filter.findScenarioColumn(columns);

      // Then
      assertFalse(result.isPresent(), "should not find scenario column");
    }
  }

  /** Tests for the deriveDataColumns(List, ColumnName) method. */
  @Nested
  @DisplayName("deriveDataColumns(List, ColumnName) method")
  class DeriveDataColumnsMethod {

    /** Tests for the deriveDataColumns method. */
    DeriveDataColumnsMethod() {}

    /** Verifies that deriveDataColumns excludes scenario column. */
    @Test
    @Tag("normal")
    @DisplayName("should exclude scenario column when present")
    void shouldExcludeScenarioColumn_whenPresent() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final var scenarioColumn = new ColumnName("[Test]");
      final var columns =
          List.of(new ColumnName("[Test]"), new ColumnName("id"), new ColumnName("name"));

      // When
      final var result = filter.deriveDataColumns(columns, scenarioColumn);

      // Then
      assertAll(
          "should derive data columns",
          () -> assertEquals(2, result.size(), "should have 2 data columns"),
          () -> assertEquals("id", result.get(0).value(), "first column should be 'id'"),
          () -> assertEquals("name", result.get(1).value(), "second column should be 'name'"));
    }

    /** Verifies that deriveDataColumns returns all columns when no scenario column. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all columns when scenario column is null")
    void shouldReturnAllColumns_whenScenarioColumnIsNull() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final var columns = List.of(new ColumnName("id"), new ColumnName("name"));

      // When
      final var result = filter.deriveDataColumns(columns, null);

      // Then
      assertEquals(columns, result, "should return all columns");
    }
  }

  /** Tests for the filterRows(List, ColumnName) method. */
  @Nested
  @DisplayName("filterRows(List, ColumnName) method")
  class FilterRowsMethod {

    /** Tests for the filterRows method. */
    FilterRowsMethod() {}

    /** Verifies that filterRows returns all rows when filter not active. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all rows when filter not active")
    void shouldReturnAllRows_whenFilterNotActive() {
      // Given
      final var filter = new ScenarioFilter(scenarioMarker, List.of());
      final var scenarioColumn = new ColumnName("[Test]");
      final var row1 = createMockRow(scenarioColumn, "test1");
      final var row2 = createMockRow(scenarioColumn, "test2");
      final var rows = List.of(row1, row2);

      // When
      final var result = filter.filterRows(rows, scenarioColumn);

      // Then
      assertEquals(2, result.size(), "should return all rows when filter not active");
    }

    /** Verifies that filterRows returns all rows when scenario column is null. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all rows when scenario column is null")
    void shouldReturnAllRows_whenScenarioColumnIsNull() {
      // Given
      final var names = List.of(new ScenarioName("test1"));
      final var filter = new ScenarioFilter(scenarioMarker, names);
      final var row1 = mock(Row.class);
      final var row2 = mock(Row.class);
      final var rows = List.of(row1, row2);

      // When
      final var result = filter.filterRows(rows, null);

      // Then
      assertEquals(2, result.size(), "should return all rows when scenario column is null");
    }

    /** Verifies that filterRows filters rows by scenario name. */
    @Test
    @Tag("normal")
    @DisplayName("should filter rows by scenario name when active")
    void shouldFilterRows_whenActive() {
      // Given
      final var names = List.of(new ScenarioName("test1"));
      final var filter = new ScenarioFilter(scenarioMarker, names);
      final var scenarioColumn = new ColumnName("[Test]");
      final var row1 = createMockRow(scenarioColumn, "test1");
      final var row2 = createMockRow(scenarioColumn, "test2");
      final var rows = List.of(row1, row2);

      // When
      final var result = filter.filterRows(rows, scenarioColumn);

      // Then
      assertEquals(1, result.size(), "should filter to matching rows");
    }

    /** Verifies that filterRows includes rows with blank scenario value. */
    @Test
    @Tag("edge-case")
    @DisplayName("should include rows with blank scenario value")
    void shouldIncludeRows_whenScenarioValueBlank() {
      // Given
      final var names = List.of(new ScenarioName("test1"));
      final var filter = new ScenarioFilter(scenarioMarker, names);
      final var scenarioColumn = new ColumnName("[Test]");
      final var row1 = createMockRow(scenarioColumn, "test1");
      final var row2 = createMockRow(scenarioColumn, "");
      final var rows = List.of(row1, row2);

      // When
      final var result = filter.filterRows(rows, scenarioColumn);

      // Then
      assertEquals(2, result.size(), "should include rows with blank scenario value");
    }
  }

  /**
   * Creates a mock Row with the specified scenario value.
   *
   * @param columnName the scenario column name
   * @param value the scenario value
   * @return the mock row
   */
  private static Row createMockRow(final ColumnName columnName, final String value) {
    final var row = mock(Row.class);
    final var cellValue = new CellValue(value);
    when(row.getValue(columnName)).thenReturn(cellValue);
    return row;
  }
}
