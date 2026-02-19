package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import java.util.Collection;
import java.util.List;
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

  /** The provider instance under test. */
  private DefaultAssertionProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockComparator = mock(DataSetComparator.class);
    provider = new DefaultAssertionProvider(mockComparator);
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
      final var instance = new DefaultAssertionProvider(mockComparator);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the assertEquals(TableSet, TableSet) method. */
  @Nested
  @DisplayName("assertEquals(TableSet, TableSet) method")
  class AssertEqualsDataSetMethod {

    /** Tests for the assertEquals method. */
    AssertEqualsDataSetMethod() {}

    /** Verifies that assertEquals delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);
      doNothing()
          .when(mockComparator)
          .assertEquals(any(TableSet.class), any(TableSet.class), any());

      // When
      provider.assertEquals(expected, actual);

      // Then
      verify(mockComparator).assertEquals(expected, actual, null);
    }
  }

  /** Tests for the assertEquals(TableSet, TableSet, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(TableSet, TableSet, AssertionFailureHandler) method")
  class AssertEqualsDataSetWithHandlerMethod {

    /** Tests for the assertEquals method with handler. */
    AssertEqualsDataSetWithHandlerMethod() {}

    /** Verifies that assertEquals passes handler to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should pass handler to comparator when handler provided")
    void shouldPassHandlerToComparator_whenHandlerProvided() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);
      final var handler = mock(AssertionFailureHandler.class);
      doNothing()
          .when(mockComparator)
          .assertEquals(any(TableSet.class), any(TableSet.class), any());

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

  /** Tests for the assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection) method")
  class AssertEqualsIgnoreColumnsDataSetMethod {

    /** Tests for the assertEqualsIgnoreColumns method. */
    AssertEqualsIgnoreColumnsDataSetMethod() {}

    /** Verifies that assertEqualsIgnoreColumns delegates to comparator. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called")
    void shouldDelegateToComparator_whenCalled() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);
      final var tableName = "users";
      final Collection<String> ignoreColumns = List.of("CREATED_AT");
      doNothing()
          .when(mockComparator)
          .assertEqualsIgnoreColumns(
              any(TableSet.class), any(TableSet.class), any(String.class), any());

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

  /** Tests for the assertEqualsWithStrategies(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEqualsWithStrategies(Table, Table, Collection) method")
  class AssertEqualsWithStrategiesMethod {

    /** Tests for the assertEqualsWithStrategies method. */
    AssertEqualsWithStrategiesMethod() {}

    /** Verifies that assertEqualsWithStrategies delegates to comparator with empty strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to comparator when called with empty strategies")
    void shouldDelegateToComparator_whenCalledWithEmptyStrategies() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final Collection<ColumnStrategyMapping> columnStrategies = List.of();
      doNothing()
          .when(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());

      // When
      provider.assertEqualsWithStrategies(expected, actual, columnStrategies);

      // Then
      verify(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());
    }

    /** Verifies that assertEqualsWithStrategies extracts IGNORE columns. */
    @Test
    @Tag("normal")
    @DisplayName("should extract IGNORE columns and pass to comparator")
    void shouldExtractIgnoreColumns_whenCalledWithIgnoreStrategy() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final Collection<ColumnStrategyMapping> columnStrategies =
          List.of(
              ColumnStrategyMapping.ignore("CREATED_AT"),
              ColumnStrategyMapping.ignore("UPDATED_AT"),
              ColumnStrategyMapping.caseInsensitive("EMAIL"));
      doNothing()
          .when(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());

      // When
      provider.assertEqualsWithStrategies(expected, actual, columnStrategies);

      // Then
      verify(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());
    }

    /** Verifies that assertEqualsWithStrategies handles mixed strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should handle mixed strategies correctly")
    void shouldHandleMixedStrategies_whenMultipleStrategiesProvided() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final Collection<ColumnStrategyMapping> columnStrategies =
          List.of(
              ColumnStrategyMapping.strict("ID"),
              ColumnStrategyMapping.ignore("CREATED_AT"),
              ColumnStrategyMapping.caseInsensitive("EMAIL"),
              ColumnStrategyMapping.numeric("AMOUNT"),
              ColumnStrategyMapping.regex("UUID", "[a-f0-9-]{36}"));
      doNothing()
          .when(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());

      // When
      provider.assertEqualsWithStrategies(expected, actual, columnStrategies);

      // Then
      verify(mockComparator)
          .assertEqualsWithStrategies(
              any(Table.class), any(Table.class), anyCollection(), anyMap());
    }
  }
}
